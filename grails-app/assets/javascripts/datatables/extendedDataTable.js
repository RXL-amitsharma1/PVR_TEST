(function( factory ) {
    "use strict";

    if ( typeof define === 'function' && define.amd ) {
        // AMD
        define( ['jquery'], function ( $ ) {
            return factory( $, window, document );
        } );
    }
    else if ( typeof exports === 'object' ) {
        // CommonJS
        module.exports = function (root, $) {
            if ( ! root ) {
                // CommonJS environments without a window global must pass a
                // root. This will give an error otherwise
                root = window;
            }

            if ( ! $ ) {
                $ = typeof window !== 'undefined' ? // jQuery's factory checks for a global window
                    require('jquery') :
                    require('jquery')( root );
            }

            return factory( $, root, root.document );
        };
    }
    else {
        // Browser
        factory( jQuery, window, document );
    }
}

(function( $, window, document, undefined ) {
    "use strict";

    //following https://datatables.net/upgrade/1.10-convert
    const LEGACY_COLUMN_CONFIGS_MAP = {
        'aDataSort': 'orderData',
        'asSorting': 'orderSequence',
        'bSearchable': 'searchable',
        'bSortable': 'orderable',
        'bVisible': 'visible',
        'fnCreatedCell': 'createdCell',
        'fnRender': 'render',
        'iDataSort': 'orderData',
        'mData': 'data',
        'mRender': 'render',
        'sCellType': 'cellType',
        'sClass': 'className',
        'sDefaultContent': 'defaultContent',
        'sName': 'name',
        'sSortDataType': 'orderDataType',
        'sTitle': 'title',
        'sType': 'type',
        'sWidth': 'width'
    };
    const STACKED_HEADER_DELIMITER = "<div class='edt-stacked-separator'></div>";
    const STACKED_EMPTY_VALUE = "&nbsp;";

    let ExtendedDataTable = function (options) {
        const tableId = $(this).attr('id');

        //need to get disabledOrderIndexes for prepareColumns before prepareColumnIndexes (cause need to get original indexes)
        const disabledOrderIndexes = getOrderDisabledIndexes((options[getColumnDefsPropertyName(options)]));

        prepareInitialSorting(options);
        prepareColumnIndexes(options);

        prepareColumnsFiltersState(options, tableId, disabledOrderIndexes);

        prepareListeners(options, tableId);
        prepareOptionalListeners(options, tableId);

        //needs to be called before DataTable because of ajax data handler bind
        if (options.ajax && _.isFunction(options.ajax.data)) {
            const dataFn = options.ajax.data.bind({});

            options.ajax.data = function (data) {
                prepareCommonAjaxData(data, options, tableId);
                prepareOrderData(data, options, tableId);

                dataFn(data);
            }
        } else if (options.ajax) {
            options.ajax.data = function (data) {
                prepareCommonAjaxData(data, options, tableId);
            }
        }

        const dataTable = $(this).DataTable(options);

        updateHeadersAriaLabel(dataTable.table().header());

        dataTable.on('draw', function (e, settings) {
            const table = $('#' + tableId);
            updateOrderStyles(table, settings);

            prepareFilterDatePickers(tableId);

            //set permanentInlineFilters values - on table draw cause the corresponding filter fields are ready
            if (!_.isEmpty(options.permanentInlineFiltersData)) {
                fillFilterValues($(table.DataTable().table().header()), options.permanentInlineFiltersData);
            }

            //update search button state in case of stored inline filters
            table.find('thead .edt-inline-search-box').each(function () {
                updateSearchButtonState($(this));
            });
        });

        dataTable.on('column-visibility.dt', function (e, settings, column, state) {
            if (state) {
                const table = $('#' + tableId);
                const tableHeader = table.DataTable().table().header();
                const appliedOrderKey = $(tableHeader).attr('data-applied-order-key');

                if (appliedOrderKey) {
                    const headerItem = $(tableHeader).find('th[data-stack-index=' + column + '] .edt-header-item[table-data-key=' + appliedOrderKey + ']');
                    if (headerItem.length > 0) {
                        const appliedOrderDir = $(tableHeader).attr('data-applied-order-dir');
                        updateSortControlElements(table, appliedOrderKey, appliedOrderDir);
                        updateOrderStyles(table, {});
                    }
                }
            }
        });

        dataTable.on('init.dt', function (e, settings) {
            options.edtStateReadyToSave = true;
            updateTableColumns();
        });

        dataTable.on('order.dt', function (e, settings, orderArray) {
            if (!_.isEmpty(orderArray)) {
                updateOrderSessionStorage(tableId, true);
            }
        });

        return dataTable;
    };

    $.fn.ExtendedDataTable = ExtendedDataTable;
    $.fn.extendedDataTable = ExtendedDataTable;

    function prepareFilterDatePickers(tableId) {
        const tableWrapperSelector = '#' + tableId + '_wrapper';
        $(tableWrapperSelector).find('.edt-inline-search-box').hide();

        //<xtable> tag back to <table> with lost content restoring before datepicker() using
        $(tableWrapperSelector + ' xtable').each(function () {
            $(this).replaceWith('<table class="datepicker-calendar-days"><thead>' +
                '<tr><th>Su</th><th>Mo</th><th>Tu</th><th>We</th><th>Th</th><th>Fr</th><th>Sa</th></tr>' +
                '</thead><tbody></tbody></table>');
        });
        $(tableWrapperSelector + ' .edt-header-item .filter-date-picker').each(function () {
            $(this).datepicker({
                allowPastDates: true,
                //use init value from template filter-input or null (null to exclude default current date value)
                date: $(this).find('input[data-type="date-range"]').attr('value') || null,

                momentConfig: {
                    culture: userLocale,
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                }
            });
        });
        $(tableWrapperSelector + ' .edt-header-item .datepicker input.form-control').addClass('inline-filter-input');
    }

    function updateHeadersAriaLabel(tableHeader) {
        $(tableHeader).find('th[data-dt-column]').each(function () {
            const th = $(this);
            const headerTitle = $.map($(th.find('.edt-header-item-value')), function(value) {
                return $(value).text()
            }).join('/');
            th.attr('aria-label', headerTitle || ' ');
        });
    }

    function updateTableColumns() {
        //TODO: fix for multiple tables page (e.g #tableColumns, #tableColumns2 of case form)

        $("#tableColumns").find("[name=selectAll]").closest("td").text($.i18n._("oneDrive.select"));

        $("#tableColumns tr").each(function () {
            $(this).find("td:first").each(function () {
                const columnLabelItem = $(this);
                const labels = [];
                if (columnLabelItem.find(".edt-header-item").length > 0) {
                    columnLabelItem.find(".edt-header-item .edt-header-item-value").each(function () {
                        const customLabel = $(this).closest('.edt-header-item').attr('data-custom-menu-label');
                        labels.push(customLabel || $(this).text());
                    });
                    columnLabelItem.html(labels.join("<br>"));
                } else {
                    columnLabelItem.html(columnLabelItem.text());
                }
            });
        });
    }

    function prepareColumnsFiltersState(options, tableId, disabledOrderIndexes) {
        const columnsConfig = options[getColumnsPropertyName(options)];

        if (_.isEmpty(columnsConfig)) {
            return;
        }

        const table = $('#' + tableId);
        const tableHeaders = prepareTableHeaders(table, columnsConfig);

        $.each(columnsConfig, function(index, config) {
            if (_.isEmpty(config.title) && _.isEmpty(config.sTitle)) {
                config.title = config.sTitle = tableHeaders[index].innerHTML || '';
            }
            config.originalTitle = _.isEmpty(config.title) ? config.sTitle : config.title;
        });

        //needs to be called after config.title set to get correct filter field labels
        prepareAdvancedFilter(options, tableId);

        //prepare state save after prepareAdvancedFilter because filter panel (in case of it is enabled) should be ready
        //but before columns preparing because initial inline filter values are needed
        //(get async filter items before columns preparing)
        const asyncFilterItems = getAsyncFilterItems(options);
        prepareStateSave(options, asyncFilterItems, tableId);

        prepareColumnsData(columnsConfig, tableHeaders, disabledOrderIndexes, options);

        prepareStackedColumnsData(columnsConfig, tableHeaders, disabledOrderIndexes, options);

        options[getColumnsPropertyName(options)] = _.filter(columnsConfig, function (config) {
            return stackIdIsEmpty(config.stackId);
        });

        table.find('thead tr:first').children().replaceWith(table.find('thead tr:first').children().not('[stacked="true"]'));
    }

    function prepareColumnsData(columnsConfig, tableHeaders, disabledOrderIndexes, options) {
        $.each(columnsConfig, function(index, config) {
            if (stackIdIsEmpty(config.stackId)) {
                const dataKey = config['mData'] || config['data'] || config['dataKey'];
                const headerContent = getHeaderItemContent({
                    title: config.title,
                    customMenuLabel: config.customMenuLabel,
                    dataKey: dataKey,
                    orderable: checkColumnIsOrderable(config, index, disabledOrderIndexes),
                    inlineFilterConfig: getInlineColumnFilterConfig(config, dataKey, options)
                });

                $(tableHeaders[index]).empty();
                const $headerContentElement = $(headerContent);
                $headerContentElement.appendTo($(tableHeaders[index]));

                $(tableHeaders[index]).addClass('edt-column');
                delete config.sTitle;
                config.title = headerContent;
                config['bSortable'] = config['orderable'] = false;
            }
        });
    }

    function getStackedData(columnsConfig) {
        const stackedData = {};
        $.each(columnsConfig, function(index, config) {
            const stackId = config.stackId;
            if (!stackIdIsEmpty(stackId)) {
                if (stackedData[stackId]) {
                    stackedData[stackId].push({columnConfig: config, columnIndex: index});
                } else {
                    stackedData[stackId] = [{columnConfig: config, columnIndex: index}];
                }
            }
        });
        return stackedData;
    }

    function prepareStackedColumnsData(columnsConfig, tableHeaders, disabledOrderIndexes, options) {
        const stackedData = getStackedData(columnsConfig);
        if (_.isEmpty(stackedData)) {
            return;
        }

        for (let stackId in stackedData) {
            const stackedConfigItems = stackedData[stackId];
            const stackedHeaderItems = [];
            const stackedCellRenderers = [];

            var stackIndex = stackedConfigItems[0].columnIndex;

            $.each(stackedConfigItems, function(stackedConfigIndex, stackedConfig) {
                const columnConfig = stackedConfig.columnConfig;
                const dataKey = columnConfig['mData'] || columnConfig['data'] || columnConfig['dataKey'];

                const headerTitle = columnConfig.title;
                if (!_.isEmpty(headerTitle)) {
                    stackedHeaderItems.push({
                        title: headerTitle,
                        customMenuLabel: columnConfig.customMenuLabel,
                        dataKey: dataKey,
                        orderable: checkColumnIsOrderable(columnConfig, stackedConfig.columnIndex, disabledOrderIndexes),
                        inlineFilterConfig: getInlineColumnFilterConfig(columnConfig, dataKey, options)
                    });
                }

                const stackedConfigRenderer = columnConfig['mRender'] || columnConfig['render'];
                if ($.isFunction(stackedConfigRenderer)) {
                    stackedCellRenderers.push({fn: stackedConfigRenderer, dataKey: dataKey});
                } else {
                    stackedCellRenderers.push({fn: function (data) {return isRenderValueEmpty(data) ? STACKED_EMPTY_VALUE : data}, dataKey: dataKey});
                }

                if (stackedConfigIndex > 0) {
                    $(tableHeaders[stackedConfig.columnIndex]).attr('stacked', 'true');
                    columnsConfig[stackedConfig.columnIndex].stacked = true;

                }
            });

            let stackedHeaderContent = '';
            $.each(stackedHeaderItems, function(headerItemIndex, headerItem) {
                var isLast = headerItemIndex === stackedHeaderItems.length - 1;

                stackedHeaderContent += getHeaderItemContent(headerItem);
                if (!isLast) {
                    stackedHeaderContent += STACKED_HEADER_DELIMITER;
                }
            });

            $(tableHeaders[stackIndex]).html(stackedHeaderContent);
            $(tableHeaders[stackIndex]).attr('data-stack-index', stackIndex);
            $(tableHeaders[stackIndex]).addClass('edt-stacked-column');

            columnsConfig[stackIndex] = generateStackConfig(stackIndex, stackedCellRenderers, stackedHeaderContent, columnsConfig, options['stackedConfig']);
        }
    }

    function prepareAdvancedFilter(options, tableId) {
        const columnsConfig = options[getColumnsPropertyName(options)] || [];
        prepareFilterOrder(columnsConfig, options);

        const filterDefs = getFilterItems(columnsConfig, options);

        const sortedFilterDefs = _.sortBy(filterDefs, function (filterDef) {
            return filterDef.order || 0;
        });

        if (_.isEmpty(sortedFilterDefs)) {
            return;
        }

        const advancedFilterConfig = options.advancedFilterConfig || {};
        try {
            pvr.filter_util.construct_right_filter_panel({
                table_id: '#' + tableId,
                container_id: advancedFilterConfig.containerId || 'config-filter-panel',
                filter_defs: sortedFilterDefs,
                column_count: advancedFilterConfig.columnCount || 1,
                panel_width: advancedFilterConfig.panelWidth || null,
                filter_group: advancedFilterConfig.filterGroup || null,
                done_func: function (filter) {
                    options['tableFilter'] = filter;
                    const $table = $('#' + tableId);
                    const dataTable = $table.DataTable();
                    dataTable.ajax.reload(function () {
                        if ($.isFunction(advancedFilterConfig.callback)) {
                            advancedFilterConfig.callback();
                        }
                    });
                    pvr.filter_util.update_filter_icon_state($table, filter);
                }
            });
        } catch (e) {
            console.error(e.message);
        }
    }

    function getAsyncFilterItems(options) {
        const columnsConfig = options[getColumnsPropertyName(options)] || [];
        const filterItems = getFilterItems(columnsConfig, options);

        return _.filter((_.isArray(filterItems) ? filterItems : []), function (item) {
            return !_.isEmpty(item.ajax) &&  item.ajax.async === true;
        });
    }

    function getFilterItems(columnsConfig, options) {
        let filterDefs = generateFilterItems(columnsConfig);
        const advancedFilterConfig = options.advancedFilterConfig || {};
        const advancedExtraFilters = advancedFilterConfig.extraFilters;

        if (!_.isEmpty(advancedExtraFilters) && _.isArray(advancedExtraFilters)) {
            const extraFilterDefs = generateFilterItems(_.map(advancedExtraFilters, function (item) {
                return {advancedFilter: item};
            }));
            filterDefs = $.merge(filterDefs, extraFilterDefs);
        }

        return filterDefs;
    }

    function prepareFilterOrder(columnsConfig, options) {
        let order = 0;

        _.filter(columnsConfig, function (column) {
            return !_.isEmpty(column.advancedFilter) || (column.advancedFilter === true && (!_.isEmpty(column.mData) || !_.isEmpty(column.data)));
        }).forEach(function (filterColumn) {
            if (_.isEmpty(filterColumn.advancedFilter.group)) {
                if (filterColumn.advancedFilter === true) {
                    filterColumn.advancedFilter = {};
                }
                filterColumn.advancedFilter.order = ++order;
            } else {
                filterColumn.advancedFilter.group.forEach(function (groupItem) {
                    groupItem.order = ++order;
                });
            }
        });

        const advancedExtraFilters = (options.advancedFilterConfig || {}).extraFilters || [];
        advancedExtraFilters.forEach(function (extraFilter) {
            if (_.isEmpty(extraFilter.group)) {
                extraFilter.order = ++order;
            } else {
                extraFilter.group.forEach(function (groupItem) {
                    groupItem.order = ++order;
                });
            }
        });
    }

    function generateFilterItems(columnsConfig) {
        const filterColumnsConfig = _.filter(columnsConfig, function (column) {
            return !_.isEmpty(column.advancedFilter) && _.isEmpty(column.advancedFilter.group) || (column.advancedFilter === true && (!_.isEmpty(column.mData) || !_.isEmpty(column.data)));
        });
        const groupFilterColumnsConfig = _.filter(columnsConfig, function (column) {
            return !_.isEmpty(column.advancedFilter) && !_.isEmpty(column.advancedFilter.group);
        });

        let filterDefs = _.map(filterColumnsConfig, function (columnConfig) {
            const columnFilterConfig = columnConfig.advancedFilter === true ? {} : columnConfig.advancedFilter;
            return $.extend({
                label: columnConfig.originalTitle || columnConfig.title || columnConfig.sTitle,
                type: 'text',
                name: columnConfig.originalData || columnConfig.data || columnConfig.mData
            }, columnFilterConfig);
        });
        _.each(groupFilterColumnsConfig, function (groupColumnConfig) {
            let groupOrder = 0;
            const filterGroupItems = _.map(groupColumnConfig.advancedFilter.group, function (groupItem) {
                return $.extend({
                    label: groupColumnConfig.originalTitle || groupColumnConfig.title || groupColumnConfig.sTitle,
                    group: groupColumnConfig.advancedFilter.name || groupColumnConfig.originalData || groupColumnConfig.data || groupColumnConfig.mData,
                    type: groupColumnConfig.advancedFilter.type,
                    group_order: ++groupOrder
                }, groupItem);
            });
            filterDefs = $.merge(filterDefs, filterGroupItems);
        });
        return filterDefs;
    }

    function prepareTableHeaders(table, columnsConfig) {
        const tableHead = table.find('thead');
        if (tableHead.length === 0) {
            table.append('<thead><tr>' + getTableHeaderTags(columnsConfig).join() + '</tr></thead>');
        } else {
            const headersRow = tableHead.find('tr:first');
            if (headersRow.length === 0) {
                tableHead.append('<tr>' + getTableHeaderTags(columnsConfig).join() + '</tr>');
            } else {
                const headers = headersRow.find('th');
                if (headers.length === 0) {
                    headersRow.append(getTableHeaderTags(columnsConfig).join());
                }
            }
        }
        return table.find('thead tr:first').children();
    }

    function getTableHeaderTags(columnsConfig) {
        return _.map(columnsConfig || [], function (config) {
            return '<th></th>';
        });
    }

    function generateStackConfig(stackIndex, stackedCellRenderers, stackedHeaderContent, columnsConfig, stackedConfig) {
        const config = function(cellRenderers) {
            return {
                "title": stackedHeaderContent,
                "stackIndex": stackIndex,
                "orderable": false,
                "data": null,
                "originalData": columnsConfig[stackIndex].data || columnsConfig[stackIndex].mData,
                "originalTitle": columnsConfig[stackIndex].title,
                "render": function (data, type, row, meta) {
                    let value = '';
                    const values = [];
                    $.each(cellRenderers, function (rendererIndex, renderer) {
                        if (renderer.fn) {
                            const renderResult = renderer.fn(row[renderer.dataKey], type, row, meta);
                            let preparedRenderResult = '';
                            try {
                                let stackedRenderData = $(renderResult).find('.edt-stacked-render-data');
                                if (stackedRenderData.length === 0) {
                                    stackedRenderData = $(renderResult).closest('.edt-stacked-render-data');
                                }
                                if (stackedRenderData.length > 0 && isRenderValueEmpty($(stackedRenderData[0]).html())) {
                                    preparedRenderResult = STACKED_EMPTY_VALUE;
                                } else {
                                    preparedRenderResult = renderResult;
                                }
                            } catch (e) {
                                preparedRenderResult = renderResult;
                            }
                            values.push(wrapHeaderItemValue(preparedRenderResult));
                        } else {
                            values.push(wrapHeaderItemValue(isRenderValueEmpty(row[renderer.dataKey]) ? STACKED_EMPTY_VALUE : isRenderValueEmpty(row[renderer.dataKey])));
                        }
                    });

                    const notEmptyValueContent = _.find(values, function (valueItem) {
                        const valueText = $(valueItem).text() || '';
                        return !_.isEmpty(valueText.trim());
                    });

                    value = notEmptyValueContent ? values.join("<div class='edt-stacked-separator'>") : STACKED_EMPTY_VALUE;

                    return value;
                }
            };
        }(stackedCellRenderers);

        const customConfig = !_.isEmpty(stackedConfig) ? stackedConfig[columnsConfig[stackIndex].stackId] : {};
        prepareColumnConfigProperties(customConfig);

        const stackIndexColumnConfig = columnsConfig[stackIndex];
        prepareColumnConfigProperties(stackIndexColumnConfig, true);

        return $.extend($.extend(stackIndexColumnConfig, config), customConfig);
    }

    function prepareColumnConfigProperties(columnConfig, isStackRoot) {
        if (_.isEmpty(columnConfig)) {
            return {};
        }

        if (isStackRoot) {
            delete columnConfig.stackId;
        }

        for (let key in LEGACY_COLUMN_CONFIGS_MAP) {
            if (columnConfig[key] !== null && columnConfig[key] !== undefined) {
                columnConfig[LEGACY_COLUMN_CONFIGS_MAP[key]] = columnConfig[key];
                delete columnConfig[key];
            }
        }
    }

    function stackIdIsEmpty(stackId) {
        return stackId === null || stackId === undefined || stackId === '';
    }

    function wrapHeaderItemValue(value) {
        return '<div class="edt-header-item-value"><span class="edt-header-item-value-inner">' + value + '</span></div>';
    }

    function getColumnsPropertyName(options) {
        return !_.isEmpty(options['aoColumns']) ? 'aoColumns' : 'columns';
    }

    function getColumnDefsPropertyName(options) {
        return !_.isEmpty(options['aoColumnDefs']) ? 'aoColumnDefs' : 'columnDefs';
    }

    function getTargetsPropertyName(options) {
        return !_.isEmpty(options['aTargets']) ? 'aTargets' : 'targets';
    }

    function isRenderValueEmpty(value) {
        return value === '' || (!value && value !== 0 && value !== false);
    }

    function getHeaderItemContent(headerItemData) {
        if (_.isEmpty(headerItemData.dataKey)) {
            return '<div class="edt-header-item" data-custom-menu-label="' + (headerItemData.customMenuLabel || '') + '">' + wrapHeaderItemValue(headerItemData.title) + '</div>';
        }

        var controls = '<span class="edt-header-item-controls-container"><span class="edt-header-item-controls">';
        if (headerItemData.orderable) {
            controls += getHeaderControl('arrow-up');
            controls += getHeaderControl('arrow-down');
        }
        if (!_.isEmpty(headerItemData.inlineFilterConfig)) {
            controls += getHeaderControl('search');
        }
        controls += '</span></span>';
        return '<div class="edt-header-item" data-custom-menu-label="' + (headerItemData.customMenuLabel || '') + '" table-data-key="' + headerItemData.dataKey + '">' + wrapHeaderItemValue(headerItemData.title) + controls + getInlineFilterContent(headerItemData) + '</div>'
    }

    function getHeaderControl(type) {
        var attributes = '';
        var controlClassName = '';
        if (type === 'arrow-up') {
            attributes = 'data-sort-direction="asc"';
            controlClassName = 'table-sort-control';
        } else if (type === 'arrow-down') {
            attributes = 'data-sort-direction="desc"';
            controlClassName = 'table-sort-control';
        } else if (type === 'search') {
            controlClassName = 'edt-search-control';
        }
        return '<i class="edt-header-item-control ' + controlClassName + ' fa fa-' + type + '" ' + attributes + '></i>';
    }

    function prepareColumnIndexes(options) {
        var columnsConfig = options[getColumnsPropertyName(options)];
        var columnDefs = options[getColumnDefsPropertyName(options)];

        if (!_.isEmpty(columnDefs)) {
            $.each(columnDefs, function (index, value) {
                var targetsPropertyName = getTargetsPropertyName(value);
                var targets = value[targetsPropertyName];
                if (_.isArray(targets) || targets === 0) {
                    value[targetsPropertyName] = getActualColumnIndexes(targets, columnsConfig);
                }
            });
        }
    }

    function getActualColumnIndexes(indexes, columnConfigs) {
        var columnIndexes = _.isArray(indexes) ? indexes : [indexes];
        for (var i = 0; i < columnIndexes.length; i++) {
            columnIndexes[i] = getActualColumnIndex(columnIndexes[i], columnConfigs);
        }
        return _.filter(columnIndexes, function (value) {return value !== -1});
    }

    function getActualColumnIndex(index, columnsConfig) {
        if (_.isEmpty(columnsConfig) || index > columnsConfig.length - 1) {
            return -1;
        }
        var stackMap = {};
        var resultIndex = index;
        for (var i = 0; i <= index; i++) {
            var configStackId = columnsConfig[i].stackId;
            if (!stackIdIsEmpty(configStackId)) {
                if (_.isEmpty(stackMap[configStackId])) {
                    stackMap[configStackId] = [i - (index - resultIndex)];
                } else {
                    resultIndex--;
                    stackMap[configStackId].push(i);
                }
            }
        }
        var columnStackId = columnsConfig[index].stackId;
        if (!stackIdIsEmpty(columnStackId) && !_.isEmpty(stackMap[columnStackId])) {
            //return stackMap[columnStackId][0];
            return -1;
        }
        return resultIndex;
    }

    function checkColumnIsOrderable(columnConfig, columnIndex, disabledOrderIndexes) {
        var isOrderDisabled = columnConfig['bSortable'] === false || columnConfig['orderable'] === false;
        if (!isOrderDisabled) {
            if (disabledOrderIndexes.includes(columnIndex)) {
                isOrderDisabled = true;
            }
        }
        return !isOrderDisabled;
    }

    function prepareListeners(options, tableId) {
        var selectorPrefix = '#' + tableId + '_wrapper';

        $(document).on('mouseover', selectorPrefix + ' .edt-header-item, ' + selectorPrefix + ' .edt-column', function (e) {
            var headerItem = $(e.target).find('.edt-header-item');
            if (headerItem.length === 0) {
                headerItem = $(e.target).closest('.edt-header-item');
            }

            //manipulations to show only one sort icon
            headerItem.find('.edt-header-item-control').hide();
            if (headerItem.find('.edt-active-control[data-sort-direction=desc]').length > 0) {
                headerItem.find('.edt-header-item-control:not([data-sort-direction=desc])').show();
            } else if (headerItem.find('.edt-active-control[data-sort-direction=asc]').length > 0) {
                headerItem.find('.edt-header-item-control:not([data-sort-direction=asc])').show();
            } else {
                headerItem.find('.edt-header-item-control:not([data-sort-direction=desc])').show();
            }
        });
        $(document).on('mouseout', selectorPrefix + ' .edt-stacked-column, ' + selectorPrefix + ' .edt-column', function (e) {
            var headerItem = $(e.target).find('.edt-header-item');
            if (headerItem.length === 0) {
                headerItem = $(e.target).closest('.edt-header-item');
            }
            headerItem.find('.edt-header-item-control:not([class*="edt-active-control"])').hide();
            headerItem.find('.edt-header-item-control[class*="edt-active-control"]').show();
        });

        $(document).on('click', selectorPrefix + ' .table-sort-control', function (e) {
            var dataKey = $(e.target).closest('.edt-header-item').attr('table-data-key');
            var sortDirection = $(e.target).closest('.table-sort-control').attr('data-sort-direction');
            var table = $('#' + tableId);

            updateSortControlElements(table, dataKey, sortDirection);

            //calling order([]) with empty data cause custom logic was implemented
            table.DataTable().order([]).draw();
        });

        //enable sorting by click on column or stacked section if column is stacked
        $(document).on('click', selectorPrefix + ' .edt-header-item', function (e) {
            if (!$(e.target).hasClass('edt-header-item-control') && $(e.target).closest('.edt-inline-search-box').length < 1) {
                const headerItem = $(e.target).closest('.edt-header-item');
                const dataKey = headerItem.attr('table-data-key');

                //do sorting if column is sortable
                if (headerItem.find('.table-sort-control').length > 0) {
                    const sortDirection = headerItem.find('.table-sort-control:not([class*="edt-active-control"])').attr('data-sort-direction');
                    const table = $('#' + tableId);

                    updateSortControlElements(table, dataKey, sortDirection);

                    //calling order([]) with empty data cause custom logic was implemented
                    table.DataTable().order([]).draw();
                }
            }
        });

        $(document).on('input', selectorPrefix + ' .inline-filter-input', function (e) {
            var value = $(this).val();
            $(this).attr('data-filter-changed', 'yes');
            if (value || value === 0) {
                $(this).closest('.inline-filter-wrapper').find('.edt-clear-inline-filter').addClass('active-clear-filter');
            } else {
                $(this).closest('.inline-filter-wrapper').find('.edt-clear-inline-filter').removeClass('active-clear-filter');
            }
        });

        $(document).on('dateClicked.fu.datepicker', selectorPrefix + ' .edt-header-item .datepicker', function (e) {
            $(e.target).find('input.inline-filter-input').attr('data-filter-changed', 'yes');
            $(e.target).closest('.inline-filter-wrapper').find('.edt-clear-inline-filter').addClass('active-clear-filter');
        });

        $(document).on('click', selectorPrefix + ' .edt-search-control', function (e) {
            var searchControl = $(e.target).closest('.edt-search-control');
            var headerItem = searchControl.closest('.edt-header-item');
            var searchBox = headerItem.find('.edt-inline-search-box');
            var callback = !_.isEmpty(options.inlineFilterConfig) ? options.inlineFilterConfig.callback : null;

            if (searchBox.is(":visible")) {
                searchBox.hide();
                reloadTableDataIfInlineFilterChanged(tableId, searchBox, callback);
            } else {
                var visibleSearchBox = $(e.target).closest('table').find('.edt-inline-search-box:visible');

                if (visibleSearchBox.length > 0) {
                    updateSearchButtonState(visibleSearchBox.closest('.edt-header-item'));
                    reloadTableDataIfInlineFilterChanged(tableId, visibleSearchBox, callback);
                    visibleSearchBox.hide();
                }

                var itemValueElement = headerItem.find('.edt-header-item-value');
                var innerValueItemWidth = itemValueElement.outerWidth();
                var columnSearchContainer = searchBox.closest('.edt-inline-search-box');

                var minWidth = (columnSearchContainer.attr('data-filter-type') === 'date-range' ? 180 : 150);

                columnSearchContainer.css('width', (innerValueItemWidth > minWidth ? innerValueItemWidth : minWidth) + 'px');

                //show before offset
                headerItem.find('.edt-inline-search-box').show();

                headerItem.find('.edt-inline-search-box .inline-filter-input').each(function () {
                    var filterInput = $(this);
                    filterInput.attr('data-filter-changed', 'no');
                    if (!filterInput.val() && filterInput.val() !== 0) {
                        filterInput.closest('.inline-filter-wrapper').find('.edt-clear-inline-filter').removeClass('active-clear-filter');
                    } else {
                        filterInput.closest('.inline-filter-wrapper').find('.edt-clear-inline-filter').addClass('active-clear-filter');
                    }
                });

                var leftOffset = innerValueItemWidth < minWidth ? Math.round((minWidth - innerValueItemWidth) / 2) : 0;
                columnSearchContainer.offset({top: itemValueElement.offset().top + itemValueElement.outerHeight(), left: itemValueElement.offset().left - leftOffset});

                headerItem.find('.edt-inline-search-box input').first().focus();
            }
            updateSearchButtonState(headerItem.find('.edt-inline-search-box'));
        });

        $(document).on('click', selectorPrefix + ' .edt-clear-inline-filter', function (e) {
            var filterInput = $(e.target).parent().find('.inline-filter-input');
            if (filterInput.val() || filterInput.val() === 0) {
                filterInput.val(null);
                filterInput.attr('data-filter-changed', 'yes');
            } else {
                filterInput.val(null);
            }

            filterInput.closest('.inline-filter-wrapper').find('.edt-clear-inline-filter').removeClass('active-clear-filter');
        });

        inlineFilterOutListener(tableId, options);
    }

    function prepareOptionalListeners(options, tableId) {
        const selectorPrefix = '#' + tableId + '_wrapper';

        if ($.isFunction(options.searchCallback)) {
            $(document).on('keyup paste cut', selectorPrefix + ' .dt-search input[type="search"], input[type="search"].globalSearch', function (e) {
                if (e.type !== 'keyup' || (typeof e.which === 'undefined' ||
                    (typeof e.which === 'number' && e.which > 0 && !e.ctrlKey && !e.metaKey && !e.altKey && ![37, 38, 39, 40].includes(e.which)))) {
                    options.searchCallback();
                }
            });
        }
    }

    function fillFilterValues(parentPanel, tableFilter) {
        $.each(tableFilter, function (key, value) {
            if (!_.isEmpty(value)) {
                if (value.type === 'range') {
                    parentPanel.find('[data-name=' + value.name + '][data-order=1]').val(value.value1).change();
                    parentPanel.find('[data-name=' + value.name + '][data-order=2]').val(value.value2).change();
                } else {
                    parentPanel.find('[data-name=' + value.name + ']').val(value.value).change();
                }
            }
        });
    }

    function prepareStateSave(options, asyncFilterItems, tableId) {
        if (_.isEmpty(options.stateSaving) || !options.stateSaving.isEnabled) {
            prepareAsyncFilters(asyncFilterItems, {}, {});
            return;
        }
        if (!options.stateSaving.stateDataKey) {
            prepareAsyncFilters(asyncFilterItems, {}, {});
            console.log("Unique 'stateDataKey' should be defined in case of table state saving using");
            return;
        }
        //set standard data table config to enable state save callbacks
        options.stateSave = true;

        const stateDataKey = options.stateSaving.stateDataKey;

        if (!$.isFunction(options.stateSaveCallback)) {
            options.stateSaveCallback = function (settings, data) {
                if (options.edtStateReadyToSave === true) {
                    //add columns size into data to store
                    if (options.colResize && options.colResize.isEnabled === true) {
                        const columnSizeMap = settings.api.table().colResize.columnSizeMap()[0] || [];
                        $.each(data.columns, function (index, item) {
                            item.width = columnSizeMap[index];
                        });
                    }

                    //add permanent filters data to store in preferences
                    data.permanentAdvancedFiltersData = !_.isEmpty(options.permanentAdvancedFiltersData) ? options.permanentAdvancedFiltersData : {};
                    data.permanentInlineFiltersData = !_.isEmpty(options.permanentInlineFiltersData) ? options.permanentInlineFiltersData : {};

                    //keep session values in session storage
                    sessionStorage.setItem(stateDataKey + '_session', JSON.stringify({
                        search: data.search,
                        start: data.start,
                        length: data.length
                    }));

                    const preferencesData = $.extend({}, data);
                    //exclude session data from preferences (need to keep it only in session storage)
                    delete preferencesData.search;
                    delete preferencesData.start;
                    delete preferencesData.length;

                    if (options.saveStateTimer) {
                        clearTimeout(options.saveStateTimer);
                    }

                    options.saveStateTimer = setTimeout(function () {

                        $.ajax({
                            url: UPDATE_USER_PREFERENCES_URL,
                            type: 'post',
                            data: {key: stateDataKey, value: JSON.stringify(preferencesData)},
                            error: function (data) {
                                errorNotification($.i18n._('app.ajax.unrecognized.error'));
                            }
                        });

                    }, 1000);
                }
            }
        }

        if (!$.isFunction(options.stateLoadCallback)) {
            //restore inlineTableFilter not in stateLoadCallback cause the data needs to be ready before columns preparing
            const inlineTableFilter = JSON.parse(sessionStorage.getItem(stateDataKey + '_inlineTableFilter'));
            if (!_.isEmpty(inlineTableFilter)) {
                options.initialInlineTableFilter = inlineTableFilter;
            }

            options.stateLoadCallback = function (settings) {
                let data = {};

                //restore filters from session storage
                const sessionTableFilter = JSON.parse(sessionStorage.getItem(stateDataKey + '_tableFilter'));
                const filterPanelId = (options.advancedFilterConfig || {}).containerId || 'config-filter-panel';
                const filterPanel = $('#' + filterPanelId);
                if (!_.isEmpty(sessionTableFilter)) {
                    options.tableFilter = sessionTableFilter;
                    fillFilterValues(filterPanel, sessionTableFilter);
                    pvr.filter_util.update_filter_icon_state($('#' + tableId), sessionTableFilter);
                }

                showLoader();
                //restore columns state from preferences
                $.ajax({
                    url: GET_USER_PREFERENCES_URL,
                    data: {key: stateDataKey},
                    dataType: 'json',
                    async: false,
                    success: function (response) {
                        hideLoader();
                        if (response.success) {
                            data = JSON.parse(response.data);

                            //restore session data from the session storage
                            if (sessionStorage.getItem(stateDataKey + '_session')) {
                                const sessionData = JSON.parse(sessionStorage.getItem(stateDataKey + '_session'));
                                if (!_.isEmpty(sessionData)) {
                                    data.search = sessionData.search;
                                    if (sessionData.start !== null && sessionData.start !== undefined
                                        && sessionData.length !== null && sessionData.length !== undefined) {
                                        data.start = sessionData.start;
                                        data.length = sessionData.length;
                                    }
                                }
                            }

                            //restore permanent filters
                            if (!_.isEmpty(data.permanentAdvancedFiltersData)) {
                                options.tableFilter = $.extend(options.tableFilter || {}, data.permanentAdvancedFiltersData);
                                fillFilterValues(filterPanel, data.permanentAdvancedFiltersData);
                                pvr.filter_util.update_filter_icon_state($('#' + tableId), data.permanentAdvancedFiltersData);
                            }

                            if (!_.isEmpty(data.permanentInlineFiltersData)) {
                                options.initialInlineTableFilter = $.extend(options.initialInlineTableFilter || {}, data.permanentInlineFiltersData);
                                //fill filter values will be done on table draw cause inline filter fields are not ready
                            }

                            if (options.colResize && options.colResize.isEnabled === true) {
                                settings.columnSizeMap = $.map(data.columns, function (val, index) {
                                    return val.width || parseInt(settings.aoColumns[index].width || settings.aoColumns[index].sWidth);
                                });
                            }

                            //prepare async filters when table state is ready and user preferences are loaded
                            prepareAsyncFilters(asyncFilterItems, sessionTableFilter, data.permanentAdvancedFiltersData);
                        } else {
                            errorNotification(response.message);
                        }
                    },
                    error: function (data) {
                        hideLoader();
                        errorNotification($.i18n._('app.ajax.unrecognized.error'));
                    }
                });

                return data;
            }
        }
    }

    function prepareAsyncFilters(asyncFilterItems, sessionFiltersData, permanentFiltersData) {
        _.each(asyncFilterItems || [], function (filterItem) {
            if (!filterItem || !filterItem.ajax) {
                return;
            }

            let initValue = null;
            let initData = null;
            if (!_.isEmpty(sessionFiltersData) && sessionFiltersData[filterItem.name]) {
                initValue = sessionFiltersData[filterItem.name].value;
                initData = sessionFiltersData[filterItem.name].initData || [];
            } else if (!_.isEmpty(permanentFiltersData) && permanentFiltersData[filterItem.name]) {
                initValue = permanentFiltersData[filterItem.name].value;
                initData = permanentFiltersData[filterItem.name].initData || [];
            }

            $('.filter-panel .filter-select2[data-name="' + filterItem.name + '"]').select2({
                placeholder: '',
                allowClear: true,
                data: initData || [],
                ajax: {
                    url: filterItem.ajax.url,
                    dataType: "json",
                    processResults: function (data) {
                        return {
                            results: $.map(data, function (dataItem) {
                                return {id: dataItem[filterItem.ajax.valueField], text: dataItem[filterItem.ajax.displayField]};
                            })
                        };
                    }
                }
            }).val(initValue).trigger("change");
        });
    }

    function prepareCommonAjaxData(data, options, tableId) {
        if (options.bFilter !== false && options.searching !== false && !_.isEmpty(data.search)) {
            data.searchString = data.search.value;
        }
        if (data.order.length > 0) {
            data.direction = data.order[0].dir;
            data.sort = data.columns[data.order[0].column].data;
        }

        let tableFilter = !_.isEmpty(options.tableFilter) ? options.tableFilter : {};

        //use options.initialInlineTableFilter for the first time data loading cause date range datepickers can be not initialized by the moment of the first page loading
        //so filter input selector can be unusable here
        //about datepickers - they will get correct values on table draw from corresponding generated templates values (see prepareFilterDatePickers method)
        let inlineTableFilter = {};

        if (!_.isEmpty(options.initialInlineTableFilter)) {
            inlineTableFilter = options.initialInlineTableFilter;
        } else {
            inlineTableFilter = pvr.filter_util.compose_filter('#' + tableId + '_wrapper thead .inline-filter-input');
        }
        options.initialInlineTableFilter = {};

        if (!_.isEmpty(options.stateSaving) && options.stateSaving.isEnabled) {
            const stateDataKey = options.stateSaving.stateDataKey;
            //keep filters in session storage
            sessionStorage.setItem(stateDataKey + '_tableFilter', JSON.stringify(tableFilter));
            sessionStorage.setItem(stateDataKey + '_inlineTableFilter', JSON.stringify(inlineTableFilter));
        }

        preparePermanentFiltersData(options, tableFilter, inlineTableFilter);

        //merge tableFilter and inlineTableFilter to send to server
        if (!_.isEmpty(inlineTableFilter)) {
            tableFilter = $.extend(inlineTableFilter, tableFilter);
        }

        if (!_.isEmpty(tableFilter)) {
            data.advancedFilter = true;
            data.tableFilter = JSON.stringify(tableFilter);
        }
    }

    function preparePermanentFiltersData(options, tableFilter, inlineTableFilter) {
        function buildPermanentFiltersData(key, filter) {
            const permanentFiltersData = {};
            if (!options.stateSaving) {
                return permanentFiltersData;
            }
            if (!_.isEmpty(options.stateSaving[key])) {
                $.each(options.stateSaving[key], function (index, value) {
                    if (filter && !_.isEmpty(filter[value])) {
                        permanentFiltersData[value] = filter[value];
                    }
                });
            }
            return permanentFiltersData;
        }

        options.permanentAdvancedFiltersData = buildPermanentFiltersData('permanentAdvancedFilters', tableFilter);
        options.permanentInlineFiltersData = buildPermanentFiltersData('permanentInlineFilters', inlineTableFilter);
    }

    function reloadTableDataIfInlineFilterChanged(tableId, inlineFilterContainer, callback) {
        if (inlineFilterContainer.find('.inline-filter-input[data-filter-changed=yes]').length > 0) {
            $('#' + tableId).DataTable().ajax.reload($.isFunction(callback) ? callback : function () {});
        }
    }

    function updateTableHeaderColumnState(column) {
        if (column.find('.edt-active-control').length > 0) {
            column.addClass('edt-active-column');
        } else {
            column.removeClass('edt-active-column');
        }
    }



    /**********
     <inline-filter>
     **********/
    function getInlineColumnFilterConfig(columnConfig, dataKey, options) {
        var resultConfig = {};
        var commonConfig = options['inlineFilterConfig'] || {};
        var commonCallback = $.isFunction(commonConfig.callback) ? commonConfig.callback : function () {};

        if (!_.isEmpty(columnConfig.inlineFilter)) {
            const inlineFilterDataKey = columnConfig.inlineFilter.name || dataKey;
            resultConfig = {
                name: inlineFilterDataKey,
                type: !_.isEmpty(columnConfig.inlineFilter.type) ? columnConfig.inlineFilter.type : 'text',
                maxlength: columnConfig.inlineFilter.maxlength ? columnConfig.inlineFilter.maxlength : undefined,
                initData: options.initialInlineTableFilter ? (options.initialInlineTableFilter[inlineFilterDataKey] || {}) : {},
                callback: $.isFunction(columnConfig.inlineFilter.callback) ? columnConfig.inlineFilter.callback : commonCallback
            };
        } else if (columnConfig.inlineFilter === true) {
            resultConfig = {
                name: dataKey,
                type: 'text',
                maxlength: 255,
                initData: options.initialInlineTableFilter ? (options.initialInlineTableFilter[dataKey] || {}) : {},
                callback: commonCallback
            };
        }
        return resultConfig;
    }

    function inlineFilterOutListener(tableId, options) {
        var callback = !_.isEmpty(options.inlineFilterConfig) ? options.inlineFilterConfig.callback : null;
        var selectorPrefix = '#' + tableId + '_wrapper';
        $(document).mouseup(function(e) {
            var container = $(selectorPrefix + ' .edt-inline-search-box:visible');

            // if the target of the click isn't the container nor a descendant of the container nor copy-paste modal
            if (!$(e.target).hasClass('edt-search-control') && !container.is(e.target) && container.has(e.target).length === 0 && ($(e.target).closest(".copyAndPasteModal").length === 0) && ($(e.target).closest(".importValueModal").length === 0)) {
                updateSearchButtonState(container);
                container.hide();
                reloadTableDataIfInlineFilterChanged(tableId, container, callback);
            }
        });

        $(document).on('keydown', selectorPrefix + ' .edt-inline-search-box', function (e) {
            if (e.which === 13 || e.key === "Escape") {
                var searchBox = $(e.target).closest('.edt-inline-search-box');
                updateSearchButtonState(searchBox);
                searchBox.hide();
                reloadTableDataIfInlineFilterChanged(tableId, searchBox, callback);
                return false;
            }
        });
    }

    function updateSearchButtonState(container) {
        var hasValue = false;
        container.find('input').each(function (index, element) {
            if ($(element).val()) {
                hasValue = true;
            }
        });

        var searchButton = container.closest('.edt-header-item').find('.edt-search-control');
        if (hasValue) {
            searchButton.addClass('edt-active-control').show();
        } else {
            searchButton.removeClass('edt-active-control').hide();
        }
        updateTableHeaderColumnState(searchButton.closest('th'));
    }

    function getInlineFilterContent(headerItemData) {
        var inlineFilterConfig = headerItemData.inlineFilterConfig;
        if (_.isEmpty(inlineFilterConfig)) {
            return '';
        }

        return '<div class="edt-inline-search-box" tabindex="1" data-filter-type="' + inlineFilterConfig.type + '" style="display: none"> ' + getInlineFilterControlsContent(inlineFilterConfig) + '</div>';
    }

    function getInlineFilterControlsContent(inlineFilterConfig) {
        const initData = !_.isEmpty(inlineFilterConfig.initData)
            ? inlineFilterConfig.initData : {value: '', value1: '', value2: ''};
        if (inlineFilterConfig.type === 'text') {
            return '<span class="inline-filter-wrapper"><input type="text" value="' + initData.value + '" data-type="text" data-name="' + inlineFilterConfig.name + '" placeholder="' + $.i18n._("app.advancedFilter.value") + '" class="form-control edt-inline-search inline-filter-input" maxlength="' + (!_.isEmpty(inlineFilterConfig.maxlength) ? inlineFilterConfig.maxlength : 255) + '">' + '<i class="fa fa-close edt-clear-inline-filter"></i></span>';
        } if (inlineFilterConfig.type === 'multi-value-text') {
            return '<span class="inline-filter-wrapper"><input type="text" value="' + initData.value + '" data-type="multi-value-text" data-name="' + inlineFilterConfig.name + '" placeholder="' + $.i18n._("app.advancedFilter.value") + '" class="form-control edt-inline-search inline-filter-input" maxlength="' + (!_.isEmpty(inlineFilterConfig.maxlength) ? inlineFilterConfig.maxlength : 255) + '">' + '<i class="fa fa-close edt-clear-inline-filter"></i></span>';
        } else if (inlineFilterConfig.type === 'number') {
            return '<span class="inline-filter-wrapper"><input type="number" value="' + initData.value + '" data-type="number" data-name="' + inlineFilterConfig.name + '" placeholder="' + $.i18n._("app.advancedFilter.value") + '" class="form-control edt-inline-search inline-filter-input">' + '<i class="fa fa-close edt-clear-inline-filter"></i></span>';
        } else if (inlineFilterConfig.type === 'date-range') {
            return '<div class="inline-filter-datepicker inline-filter-wrapper">' + getDatePickerTemplateHtml(inlineFilterConfig, 'from', initData.value1) + '<i class="fa fa-close edt-clear-inline-filter"></i></div>'
                + '<div class="inline-filter-datepicker inline-filter-wrapper">' +  getDatePickerTemplateHtml(inlineFilterConfig, 'to', initData.value2) + '<i class="fa fa-close edt-clear-inline-filter"></i></div>';
        } else {
            return '';
        }
    }

    function getDatePickerTemplateHtml(inlineFilterConfig, rangeEntity, initValue) {
        var placeholder = rangeEntity === 'from' ? $.i18n._('app.advancedFilter.from') : $.i18n._('app.advancedFilter.to');
        var order = rangeEntity === 'from' ? 1 : 2;
        var extraAttrs = "data-name='" + inlineFilterConfig.name + "' data-type='" + inlineFilterConfig.type + "' data-order='" + order + "' value='" + (initValue || '') + "' placeholder='" + placeholder + "'";
        var template = pvr.common_util.render_tmpl('datepicker', {
            id: inlineFilterConfig.name + '_' + rangeEntity,
            extra_attrs: extraAttrs
        });
        //temporary replace table tags before data table draw to exclude rendering conflicts
        return template.replaceAll(/<table[^>]*>/g,'<xtable>').replaceAll(/<\/table>/g,'</xtable>');
    }
    /**********
     </inline-filter>
     **********/




    /**********
     <order>
     **********/
    function getOrderPropertyName(options) {
        return !_.isEmpty(options['aaSorting']) ? 'aaSorting' : 'order';
    }

    function prepareInitialSorting(options) {
        var sortConfig = options[getOrderPropertyName(options)];
        if (_.isEmpty(sortConfig)) {
            options[getOrderPropertyName(options)] = [];
            return;
        }
        var columnsConfig = options[getColumnsPropertyName(options)] || [];

        function setStackedInitialOrder(options, columnConfig, dir) {
            var dataKey = columnConfig['data'] || columnConfig['mData'];
            if (_.isEmpty(columnConfig) || stackIdIsEmpty(columnConfig.stackId) || !dataKey) {
                return;
            }
            options['stackedInitialOrder'] = {
                dataKey: dataKey,
                dir: dir || 'asc'
            }
        }

        if (!_.isArray(sortConfig)) {
            if (sortConfig['idx'] || sortConfig['idx'] === 0) {
                setStackedInitialOrder(options, columnsConfig[sortConfig['idx']], sortConfig.dir)
                sortConfig['idx'] = getActualColumnIndex(sortConfig['idx'], columnsConfig);
                if (sortConfig['idx'] === -1) {
                    options[getOrderPropertyName(options)] = [];
                    return;
                }
            } else if (sortConfig['name']) {
                setStackedInitialOrder(options, _.find(columnsConfig, function (config) {
                    return config['name'] === sortConfig['name'];
                }), sortConfig.dir);
            } else if (sortConfig['data']) {
                var relatedColumnConfig = _.find(columnsConfig, function (config) {
                    return config['data'] === sortConfig['data'] || config['mData'] === sortConfig['data'];
                }) || {};
                setStackedInitialOrder(options, relatedColumnConfig, sortConfig.dir);

                if (!_.isEmpty(relatedColumnConfig) && _.isEmpty(options['stackedInitialOrder'])) {
                    var colIndex = getActualColumnIndex(_.indexOf(columnsConfig, relatedColumnConfig), columnsConfig);
                    if (colIndex !== -1) {
                        options[getOrderPropertyName(options)] = [[colIndex, sortConfig.dir]];
                    } else {
                        options[getOrderPropertyName(options)] = [];
                    }
                }
            }
        } else {
            var sortConfigItem = sortConfig[0];
            if (!_.isArray(sortConfigItem)) {
                return;
            }
            setStackedInitialOrder(options, columnsConfig[sortConfigItem[0]], sortConfigItem[1])
            sortConfigItem[0] = getActualColumnIndex(sortConfigItem[0], columnsConfig);
            if (sortConfigItem[0] === -1) {
                options[getOrderPropertyName(options)] = [];
                return;
            }
        }

        if (_.isEmpty(options['stackedInitialOrder'])) {
            options['initialOrder'] = options[getOrderPropertyName(options)];
        }
        options[getOrderPropertyName(options)] = [];
    }

    function prepareOrderData(data, options, tableId) {
        if (_.isEmpty(data.order)) {
            var table = $('#' + tableId);
            var tableHeader = table.DataTable().table().header();
            var sortControl = $(tableHeader).find('th .table-sort-control[class*="edt_sorting"]');

            if (sortControl.length > 0) {
                data.direction = sortControl.attr('data-sort-direction');
                data.sort = sortControl.closest('.edt-header-item').attr('table-data-key');
            } else if (sessionStorage.getItem(tableId + '_order')) {
                applyOrderFromSessionStorage(tableId, data);
            } else if (!_.isEmpty(options['stackedInitialOrder'])) {
                var stackedInitialOrder = options['stackedInitialOrder'];
                data.direction = stackedInitialOrder.dir;
                data.sort = stackedInitialOrder.dataKey;
                updateSortControlElements(table, stackedInitialOrder.dataKey, stackedInitialOrder.dir);
            } else if (!_.isEmpty(options['initialOrder']) && _.isArray(options['initialOrder'])) {
                var initialOrder = options['initialOrder'];
                data.order = [{column: initialOrder[0][0], dir: initialOrder[0][1]}];
                data.direction = data.order[0].dir;
                data.sort = data.columns[data.order[0].column].data;
                updateSortControlElements(table, data.sort, data.direction);
            }

            if (data.sort) {
                $(tableHeader).attr('data-applied-order-key', data.sort);
                $(tableHeader).attr('data-applied-order-dir', data.direction);
                updateOrderSessionStorage(tableId);
            }
        } else if (sessionStorage.getItem(tableId + '_order')) {
            applyOrderFromSessionStorage(tableId, data);
        }
    }

    function updateOrderSessionStorage(tableId, clear) {
        if (clear) {
            sessionStorage.removeItem(tableId + '_order');
            return;
        }

        var tableHeader = $('#' + tableId).DataTable().table().header();
        var sortControl = $(tableHeader).find('th .table-sort-control[class*="edt_sorting"]');
        var order = {};

        if (sortControl.length > 0) {
            order.dir = sortControl.attr('data-sort-direction');
            order.dataKey = sortControl.closest('.edt-header-item').attr('table-data-key');
        }

        if (!_.isEmpty(order)) {
            sessionStorage.setItem(tableId + '_order', JSON.stringify(order));
        }
    }

    function applyOrderFromSessionStorage(tableId, data) {
        var order = JSON.parse(sessionStorage.getItem(tableId + '_order'));
        data.direction = order.dir;
        data.sort = order.dataKey;
        data.order = [];
        updateSortControlElements($('#' + tableId), order.dataKey, order.dir);
    }

    function updateSortControlElements(table, dataKey, sortDirection) {
        var tableHeader = table.DataTable().table().header();

        var activeSortControl = $(tableHeader).find('th .edt-header-item .table-sort-control.edt-active-control');
        if (activeSortControl.length > 0) {
            activeSortControl.removeClass('edt-active-control');
            updateTableHeaderColumnState(activeSortControl.closest('th'));
        }

        var sortingClass = sortDirection === 'asc' ? 'edt_sorting_asc' : 'edt_sorting_desc';
        var sortControl = $(tableHeader).find('th .edt-header-item[table-data-key="' + dataKey + '"] .table-sort-control[data-sort-direction="' + sortDirection + '"]');

        if (sortControl.hasClass('edt_sorting_asc')) {
            sortingClass = 'edt_sorting_desc';
        } else if (sortControl.hasClass('edt_sorting_desc')) {
            sortingClass = 'edt_sorting_asc';
        }

        $(tableHeader).find('th .table-sort-control')
            .removeClass('edt_sorting_desc')
            .removeClass('edt_sorting_asc')
            .removeClass('edt-active-control');

        sortControl.addClass(sortingClass)
            .addClass('edt-active-control');

        updateTableHeaderColumnState(sortControl.closest('th'));
    }

    function updateOrderStyles(table, settings) {
        var tableHeader = table.DataTable().table().header();

        $(tableHeader).find('th .table-sort-control').hide();

        var sortControl = $(tableHeader).find('th .table-sort-control[class*="edt_sorting"]');
        if (sortControl.length > 0) {
            var headerItem = sortControl.closest('.edt-header-item');
            if (!_.isEmpty(settings[getOrderPropertyName(settings)])) {
                $(tableHeader).find('th .table-sort-control')
                    .removeClass('edt_sorting_desc')
                    .removeClass('edt_sorting_asc')
                    .removeClass('edt-active-control');

                sortControl.hide();
            } else {
                sortControl.show();
            }
            updateTableHeaderColumnState(headerItem.closest('th'));
        }
    }

    function getOrderDisabledIndexes(originalColumnDefs) {
        var indexes = [];
        if (_.isEmpty(originalColumnDefs)) {
            return indexes;
        }
        $.each(originalColumnDefs || [], function (index, columnDef) {
            if (columnDef['bSortable'] === false || columnDef['orderable'] === false) {
                var targets = columnDef[getTargetsPropertyName(columnDef)];
                if (_.isArray(targets) || targets === 0) {
                    if (targets === 0) {
                        targets = [targets];
                    }
                    indexes = $.merge(indexes, targets);
                }
            }
        });

        return indexes;
    }
    /**********
     </order>
     **********/


    return $.fn.ExtendedDataTable;
}));