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

    let DataTable = $.fn.dataTable;

    let FixedHeader = function (dt, opts) {
        opts = $.extend(FixedHeader.defaults, opts || {});

        let settings = new $.fn.dataTable.Api(dt).settings()[0];
        dt = settings;

        // ensure that we can't initialise on the same table twice
        if (settings._fixedHeader) {
            return settings._fixedHeader;
        }

        // convert from camelCase to Hungarian, just as DataTables does
        let camelToHungarian = $.fn.dataTable.camelToHungarian;
        if (camelToHungarian) {
            camelToHungarian(FixedHeader.defaults, FixedHeader.defaults, true);
            camelToHungarian(FixedHeader.defaults, opts || {});
        }
        this.s = {
            dt: dt,
            dtInstance: settings.oInstance,
            opts: opts,
            originalTop: null
        };
        this.s.dt._fixedHeader = this;

        if (this.s.opts.isEnabled) {
            this._fnConstruct();
        }

        return this;
    };

    $.extend(FixedHeader.prototype, {
        fnEnable: function () {
            if (this.isEnabled) {
                console.warn("FixedHeader: attempted to enable again. Ignoring");
                return;
            }
            this._fnConstruct();
        },

        _fnConstruct: function () {
            const self = this;

            const table = $(self.s.dt.nTable);
            table.addClass('fixed-header-table');
            table.find('thead').css('top', 0);

            const topOffset = self.s.opts.topOffset;
            //if topOffset is defined then the following workaround,
            //cause sticky position does not work for nested elements (e.g. in case of a table content is not scrollable, but a page (which contains the table) is scrollable)
            if (topOffset !== null && topOffset !== undefined) {
                function getPosition(element) {
                    var xPosition = 0,
                        yPosition = 0;

                    while (element) {
                        xPosition += (element.offsetLeft + element.clientLeft);
                        yPosition += (element.offsetTop + element.clientTop);
                        element = element.offsetParent;
                    }
                    return {
                        x: xPosition,
                        y: yPosition
                    };
                }

                function getScroll() {
                    return {
                        x: document.documentElement.scrollLeft || document.body.scrollLeft,
                        y: document.documentElement.scrollTop || document.body.scrollTop
                    };
                }

                function getWindowOffset(element) {
                    var pos = getPosition(element),
                        scroll = getScroll();

                    return {
                        x: (pos.x - scroll.x),
                        y: (pos.y - scroll.y)
                    };
                }

                $(window).on('scroll', function () {
                    const thead = table.find('thead');

                    const topOffsetValue = parseInt(topOffset);

                    const windowOffset = getWindowOffset(table[0]).y;
                    if (windowOffset <= topOffsetValue) {
                        if (self.s.originalTop === null) {
                            self.s.originalTop = thead.css('top');
                        }
                        const offset = windowOffset > 0 ? 0 : -windowOffset;
                        if (windowOffset > 0) {
                            thead.css('top', (topOffsetValue - windowOffset) + 'px');
                        } else {
                            thead.css('top', (topOffsetValue + offset) + 'px');
                        }
                    } else {
                        if (self.s.originalTop !== null) {
                            thead.css('top', self.s.originalTop);
                            self.s.originalTop = null;
                        }
                    }
                });

            }
            this.isEnabled = true;
        }
    });



    /* static parameters */

    FixedHeader.defaults = {
        isEnabled: true,
        topOffset: null
    };

    /* constants */

    FixedHeader.version = "1.0";


    /* dataTables interfaces */

    // expose
    $.fn.dataTable.FixedHeader = FixedHeader;
    $.fn.DataTable.FixedHeader = FixedHeader;


    // register a new feature with DataTables
    if (typeof $.fn.dataTable == "function") {
        $.fn.dataTableExt.aoFeatures.push({
            "fnInit": function (settings) {

                if (!settings._fixedHeader) {
                    let init = settings.oInit.fixedHeader;
                    let opts = $.extend({}, init, DataTable.defaults.fixedHeader);
                    new FixedHeader(settings, opts);
                }
                else {
                    console.warn("FixedHeader: attempted to initialise twice. Ignoring second");
                }

                return null; /* no node for DataTables to insert */
            },
            "sFeature": "FixedHeader"
        });
    }


    // attach a listener to the document which listens for DataTables initialisation events so we can automatically initialise
    $(document).on('preInit.dt.fixedHeader', function (e, settings) {
        if (e.namespace !== 'dt') {
            return;
        }

        let init = settings.oInit.fixedHeader;
        let defaults = DataTable.defaults.fixedHeader;

        if (init || defaults) {
            let opts = $.extend({}, init, defaults);

            if (init !== false) {
                new FixedHeader(settings, opts);
            }
        }
    });
}));