(function (factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD
        define(['jquery', 'datatables.net'], function ($) {
            return factory($, window, document);
        });
    }
    else if (typeof exports === 'object') {
        // CommonJS
        module.exports = function (root, $) {
            if (!root) {
                root = window;
            }

            if (!$ || !$.fn.dataTable) {
                $ = require('datatables.net')(root, $).$;
            }

            return factory($, root, root.document);
        };
    }
    else {
        // Browser
        factory(jQuery, window, document);
    }
}(function ($, window, document) {
    'use strict';
    function settingsFallback(userSetting, fallBackSetting) {
        let resultObject = {};
        for (let prop in fallBackSetting) {
            if (!fallBackSetting.hasOwnProperty(prop)) {
                continue;
            }
            if (Object.prototype.hasOwnProperty.call(userSetting, prop)) {
                let userObject = userSetting[prop];
                if (typeof userObject === 'object') {
                    resultObject[prop] = settingsFallback(userObject, fallBackSetting[prop]);
                } else {
                    resultObject[prop] = userObject;
                }
            } else {
                resultObject[prop] = fallBackSetting[prop];
            }
        }
        return resultObject;
    }

    let DataTable = $.fn.dataTable;

    let ColResize = function (dt, opts) {
        opts = settingsFallback(opts || {}, ColResize.defaults);

        let settings = new $.fn.dataTable.Api(dt).settings()[0];
        dt = settings;

        // ensure that we can't initialise on the same table twice
        if (settings._colResize) {
            return settings._colResize;
        }

        // convert from camelCase to Hungarian, just as DataTables does
        let camelToHungarian = $.fn.dataTable.camelToHungarian;
        if (camelToHungarian) {
            camelToHungarian(ColResize.defaults, ColResize.defaults, true);
            camelToHungarian(ColResize.defaults, opts || {});
        }

        if (dt.oFeatures && dt.oFeatures.bStateSave === true) {
            opts.tableStateSave = true;
        }

        this.s = {
            dt: dt,
            dtInstance: settings.oInstance,
            state: {
                isDragging: false,
                startX: 0,
                originalTableWidth: 0,
                originalWidth: [],
                appliedWidth: [],
                minWidth: 0,
                maxWidth: 0,
                $element: null,
                column: null,
                isLastColumnDragging: false
            },
            opts: opts
        };
        this.s.dt._colResize = this;

        if (this.s.opts.isEnabled) {
            this._fnConstruct();
        }

        return this;
    };

    $.extend(ColResize.prototype, {
        fnEnable: function () {
            if (this.isEnabled) {
                console.warn("ColResize: attempted to enable again. Ignoring");
                return;
            }
            this._fnConstruct();
        },
        fnReset: function () {
            let self = this;
            this._fnGetAllColumns().forEach(function (column) {
                const columnWidth = column.width || column.sWidth;
                if (!columnWidth) {
                    return;
                }

                let oldWidth = self._fnParseColumnWidth(columnWidth),
                    newWidth = self._fnParseColumnWidth(column._sResizableWidth),
                    $node = $(self._fnGetColumnTh(column));

                self.s.state.originalWidth[column.idx] = oldWidth;
                column.width = column._sResizableWidth;
                column.sWidth = column._sResizableWidth;
                self._fnApplyWidth(newWidth - oldWidth, $node, column);
            });
            this.s.opts.onResizeEnd(null, this._fnGetAllColumns().map(this._fnMapColumn), this.s.dt);
        },
        fnRestoreState: function (columnSizeMap) {
            let self = this,
                sizeMap = columnSizeMap || self.s.dt.columnSizeMap || this.s.opts.stateLoadCallback(this.s.opts),
                cols = this._fnGetAllColumns();

            if (sizeMap == null) return;

            self.s.state.originalTableWidth = this._fnGetTable().width();

            cols.forEach(function (column) {
                const columnWidth = column.width || column.sWidth;
                if (!columnWidth || !sizeMap[column.idx]) {
                    return;
                }

                let oldWidth = self._fnParseColumnWidth(columnWidth),
                    newWidth = sizeMap[column.idx],
                    $node = $(self._fnGetColumnTh(column));

                if (oldWidth !== newWidth) {
                    self.s.state.appliedWidth[column.idx] = sizeMap[column.idx];
                    $node.outerWidth(sizeMap[column.idx], true);
                    let $table = self._fnGetTable();
                    $table.width(($table.width() + (newWidth - oldWidth)));
                    //self._fnAdjustColumns();
                }
            });

            self.s.opts.onResizeEnd(null, this._fnGetAllColumns().map(this._fnMapColumn), self.s.dt);
        },
        fnSaveState: function () {
            this.s.opts.stateSaveCallback(this.s.opts, this._fnGetColumnSizeMap());
        },
        fnColumnSizeMap: function () {
            return this._fnGetColumnSizeMap();
        },
        fnDisable: function () {
            if (!this.isEnabled) {
                console.warn("ColResize: attempted to disable again. Ignoring");
                return;
            }

            $(document).off('.ColResize');
            const self = this;
            this._fnGetAllColumns().forEach(function (column) {
                let $columnNode = $(self._fnGetColumnTh(column));
                $columnNode.off('.ColResize');
                $columnNode.removeAttr('data-is-resizable');
            });
            this.isEnabled = false;
        },

        _fnParseColumnWidth: function (columnWidth) {
            const matchResult = columnWidth.match(/(\d+)/i);
            return matchResult != null ? parseFloat(matchResult[0]) : 0;
        },

        _fnConstruct: function () {
            let self = this;

            //set some width to columns with missed width property
            $.each(self._fnGetAllColumns() || [], function (index, config) {
                if (!config.width && !config.sWidth) {
                    config.sWidthOrig = config.sWidth = config.width = '70px';
                }
            });


            // register instance events
            self.s.dtInstance.on('column-visibility.dt', function (event, settings, columnIndex, visible) {
                const column = self._fnGetAllColumns()[columnIndex];
                let widthOffset;
                if (visible === true) {
                    const $columnNode = $(self._fnGetColumnTh(column));
                    const columnWidth = self._fnParseColumnWidth(column.width || column.sWidth);
                    //case of a column was hidden on initialization and shown after
                    if (!$columnNode.attr('data-is-resizable')) {
                        prepareColumn(column);
                    }
                    widthOffset = columnWidth;
                } else {
                    widthOffset = -(self._fnParseColumnWidth(column.width || column.sWidth));
                }
                self.s.state.originalTableWidth += widthOffset;
                $(event.target).closest('table').width(self.s.state.originalTableWidth);
            });


            // register document events
            $(document).on('mousemove.ColResize touchmove.ColResize', function (e) {
                //TODO check self._fnGetXCoords(e) % 2 optimization works well:
                if (self.s.state.isDragging && self._fnGetXCoords(e) % 2 === 0) {
                //if (self.s.state.isDragging) {
                    let changedWidth = self._fnGetXCoords(e) - self.s.state.startX;

                    self._fnApplyWidth(changedWidth, self.s.state.$element, self.s.state.column);

                    self.s.opts.onResize(self._fnMapColumn(self.s.state.column));

                    if (self.s.state.isLastColumnDragging) {
                        //TODO: process the last column moving
                        //? scroll if the last element gets resized
                    }
                }
            });
            $(document).on('mouseup.ColResize touchend.ColResize', function (e) {
                if (self.s.state.isDragging) {

                    // workaround to prevent sorting on column click
                    setTimeout(function () {
                        //disable sorting
                        self._fnGetAllColumns().forEach(function (column) {
                            column.bSortable = column._bSortableTempHolder;
                        });
                    }, 100);
                    // callback
                    let mappedColumns = self._fnGetAllColumns().map(self._fnMapColumn);
                    self.s.opts.onResizeEnd(self._fnMapColumn(self.s.state.column), mappedColumns, self.s.dt);

                    //TODO: check real size changed before do fnSaveState
                    if (self.s.opts.saveState) {
                        self.fnSaveState();
                    }
                }
                self._fnGetAllColumns().forEach(function (column) {
                    $(self._fnGetColumnTh(column)).removeClass(self.s.opts.hoverClass);
                });
                self.s.state.isDragging = false;
            });

            //register column events
            this._fnGetAllColumns().forEach(function (column) {
                prepareColumn(column);
            });

            function prepareColumn(column) {
                let $columnNode = $(self._fnGetColumnTh(column));
                let isResizable = self._fnIsColumnResizable(column);
                $columnNode.attr('data-is-resizable', isResizable.toString());
                //save the original value (server) somewhere, we want the size of all of them.
                column._sResizableWidth = column.sWidth;
                if (isResizable) {
                    $columnNode.on('mousemove.ColResize touchmove.ColResize', function (e) {
                        let $node = $(e.currentTarget);
                        if (self._fnIsInDragArea($node, e)) {
                            $node.addClass(self.s.opts.hoverClass);
                        } else {
                            if (!self.s.state.isDragging) {
                                $node.removeClass(self.s.opts.hoverClass);
                            }
                        }
                    });
                    $columnNode.on('mouseout.ColResize', function (e) {
                        if (!self.s.state.isDragging) {
                            let $node = $(e.currentTarget);
                            $node.removeClass(self.s.opts.hoverClass);
                        }
                    });
                    $columnNode.on('mousedown.ColResize touchstart.ColResize', function (e) {
                        let $node = $(e.currentTarget);
                        if (self._fnIsInDragArea($node, e)) {
                            //disable sorting
                            self._fnGetAllColumns().forEach(function (column) {
                                column._bSortableTempHolder = column.bSortable;
                                column.bSortable = false;
                                self._fnRemovePercentWidths(column, $(self._fnGetColumnTh(column)));
                            });

                            self.s.state.isDragging = true;
                            self.s.state.startX = self._fnGetXCoords(e);
                            self.s.state.originalTableWidth = $node.closest('table').width();
                            self.s.state.originalWidth[column.idx] = self._fnGetCurrentWidth($node);
                            self.s.state.minWidth = self._fnGetMinWidthOf($node);
                            self.s.state.maxWidth = self._fnGetMaxWidthOf($node);
                            self.s.state.$element = $node;
                            self.s.state.column = column;
                            self.s.state.isLastColumnDragging = self._fnIsLastResizableColumnDragging(column);

                            self.s.opts.onResizeStart(null, self._fnGetAllColumns().map(self._fnMapColumn));
                        }
                    });
                }
            }

            this.isEnabled = true;

            if (this.s.opts.saveState || this.s.opts.tableStateSave) {
                this.fnRestoreState();
            }
        },
        _fnGetColumnTh: function (column) {
            return $(this.s.dt.nTHead).find('th[data-dt-column=' + column.idx + ']');
        },
        _fnGetAllColumns: function () {
            return this.s.dt.aoColumns;
        },
        _fnGetTable: function () {
            return $(this.s.dt.nTable);
        },
        _fnRemovePercentWidths: function (column, $node) {
            if ($node.attr('style') && $node.attr('style').indexOf('%') !== -1) {
                console.warn("ColResize: column styles in percentages is not supported, trying to convert to px on the fly");
                let width = $node.width();
                $node.removeAttr('style');
                column.sWidth = width + 'px';
                column.width = width + 'px';
                $node.width(width);
            } else {
                //$node.width($node.width());
            }
        },
        _fnIsInDragArea: function ($th, e) {
            let rightSide = $th.offset().left + $th.outerWidth();
            let xCoord = this._fnGetXCoords(e);
            return (rightSide + 10) > xCoord && (rightSide - 10) < xCoord;
        },
        _fnGetXCoords: function (e) {
            return e.type.indexOf('touch') !== -1 ? e.originalEvent.touches[0].pageX : e.pageX;
        },
        _fnGetColumnSizeMap: function () {
            let sizeMap = [];
            let self = this;
            this._fnGetAllColumns().forEach(function (column) {
                if (column.bVisible === true) {
                    const appliedWidth = self.s.state.appliedWidth[column.idx];
                    if (appliedWidth) {
                        sizeMap[column.idx] = appliedWidth;
                    } else {
                        sizeMap[column.idx] = self._fnParseColumnWidth(column.width || column.sWidth);
                    }
                }
            });
            return sizeMap;
        },
        _fnApplyWidth: function (changedWidth, element, column) {
            let self = this;

            //apply widths
            let thWidth = this.s.state.originalWidth[column.idx] + changedWidth;
            element.outerWidth(thWidth);
            column.sWidth = thWidth + 'px';
            column.width = thWidth + 'px';
            this.s.state.appliedWidth[column.idx] = thWidth;

            //change table size
            let $table = element.closest('table');
            //let shouldChangeTableWidth = element.closest('.dt-layout-table').length > 0 &&
            //    ($table.width() + changedWidth) > element.closest('.dt-layout-table').width();
            //if (shouldChangeTableWidth) {
                $table.width(self.s.state.originalTableWidth + changedWidth);
                this._fnAdjustColumns();
            //}
        },
        _fnAdjustColumns: function () {
            this.s.dt.api.columns.adjust();
        },
        _fnGetCurrentWidth: function ($node) {
            if ($node.attr('style')) {
                let possibleWidths = $node.attr('style').split(';')
                    .map(function (cssPart) {
                        return cssPart.trim();
                    })
                    .filter(function (cssPart) {
                        return cssPart !== '';
                    })
                    .map(function (cssPart) {
                        let widthResult = cssPart.match(/^width: (\d+)px/i);
                        return widthResult != null ? parseInt(widthResult[1]) : 0;
                    })
                    .filter(function (possibleWidth) {
                        return !Number.isNaN(possibleWidth) && possibleWidth > 0;
                    });

                if (possibleWidths.length > 0) {
                    return possibleWidths[0];
                }
            }
            return $node.width();
        },
        _fnGetMinWidthOf: function ($node) {
            if (this.s.opts.getMinWidthOf != null) {
                return this.s.opts.getMinWidthOf($node);
            }
            let minWidthFromCss = this._fnGetWidthOfValue($node.css('min-width'));
            if (!isNaN(minWidthFromCss) && minWidthFromCss > 0) {
                return minWidthFromCss;
            }
            return 50;
        },
        _fnGetMaxWidthOf: function ($node) {
            return this._fnGetWidthOfValue($node.css('max-width'));
        },
        _fnGetWidthOfValue: function (widthStr) {
            if (widthStr === 'none') {
                return -1;
            }
            return parseInt(widthStr.match(/(\d+)px/ig));
        },
        _fnMapColumn: function (column) {
            return { idx: column.idx, width: column.sWidth };
        },
        _fnIsLastResizableColumnDragging: function (draggingColumn) {
            const self = this;
            let visibleColumns = this._fnGetAllColumns().filter(function (column) {
                return $(self._fnGetColumnTh(column)).is(':visible');
            });
            let indexOfColumn = visibleColumns.indexOf(draggingColumn);
            if (indexOfColumn === visibleColumns.length - 1) {
                return true;
            }
            for (let counter = indexOfColumn + 1; counter < visibleColumns.length; counter++) {
                let column = visibleColumns[counter];
                if (this._fnIsColumnResizable(column)) {
                    return false;
                }
            }
            return true;
        },
        _fnIsColumnResizable: function (column) {
            return this.s.opts.isResizable(column);
        }
    });



    /* static parameters */

    ColResize.defaults = {
        isEnabled: true,
        hoverClass: 'dt-colresizable-hover',
        saveState: false,
        tableStateSave: false,
        isResizable: function (column) {
            if (typeof column.isResizable === 'undefined') {
                return true;
            }
            return column.isResizable;
        },
        onResizeStart: function (column, columns) {
        },
        onResize: function (column) {
        },
        onResizeEnd: function (column, columns, table) {
            if (table.oFeatures.bStateSave === true) {
                table.api.state.save();
            }
        },
        stateSaveCallback: function (settings, data) {
            let stateStorageName = window.location.pathname + "/colResizeStateData";
            localStorage.setItem(stateStorageName, JSON.stringify(data));
        },
        stateLoadCallback: function (settings) {
            let stateStorageName = window.location.pathname + "/colResizeStateData",
                data = localStorage.getItem(stateStorageName);
            return data != null ? JSON.parse(data) : null;
        },
        getMinWidthOf: null
    };


     /* constants */

    ColResize.version = "1.0";


    /* dataTables interfaces */

    // expose
    $.fn.dataTable.ColResize = ColResize;
    $.fn.DataTable.ColResize = ColResize;


    // register a new feature with DataTables
    if (typeof $.fn.dataTable == "function") {
        $.fn.dataTableExt.aoFeatures.push({
            "fnInit": function (settings) {

                if (!settings._colResize) {
                    let init = settings.oInit.colResize;
                    let opts = $.extend({}, init, DataTable.defaults.colResize);
                    new ColResize(settings, opts);
                }
                else {
                    console.warn("ColResize: attempted to initialise twice. Ignoring second");
                }

                return null; /* no node for DataTables to insert */
            },
            "sFeature": "ColResize"
        });
    }


    // attach a listener to the document which listens for DataTables initialisation events so we can automatically initialise
    $(document).on('preInit.dt.colResize', function (e, settings) {
        if (e.namespace !== 'dt') {
            return;
        }

        let init = settings.oInit.colResize;
        let defaults = DataTable.defaults.colResize;

        if (init || defaults) {
            let opts = $.extend({}, init, defaults);

            if (init !== false) {
                new ColResize(settings, opts);
            }
        }
    });

    // API augmentation
    $.fn.dataTable.Api.register('colResize.enable()', function () {
        return this.iterator('table', function (ctx) {
            ctx._colResize.fnEnable();
        });
    });
    $.fn.dataTable.Api.register('colResize.disable()', function () {
        return this.iterator('table', function (ctx) {
            ctx._colResize.fnDisable();
        });
    });
    $.fn.dataTable.Api.register('colResize.reset()', function () {
        return this.iterator('table', function (ctx) {
            ctx._colResize.fnReset();
        });
    });
    $.fn.dataTable.Api.register('colResize.save()', function () {
        return this.iterator('table', function (ctx) {
            ctx._colResize.fnSaveState();
        });
    });
    $.fn.dataTable.Api.register('colResize.columnSizeMap()', function () {
        return this.iterator('table', function (ctx) {
            return ctx._colResize.fnColumnSizeMap();
        });
    });
    $.fn.dataTable.Api.register('colResize.restore()', function (columnSizeMap) {
        return this.iterator('table', function (ctx) {
            ctx._colResize.fnRestoreState(columnSizeMap);
        });
    });
}));