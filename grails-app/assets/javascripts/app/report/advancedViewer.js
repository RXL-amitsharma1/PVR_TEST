function initAdvancedViewerCLL(config) {
    var header=config.header;
    var rowColumns=config.rowColumns;
    var serviceColumns=config.serviceColumns;
    var groupColumns=config.groupColumns;
    var stacked=config.stacked?config.stacked:{};
    var fieldTypeMap=config.fieldTypeMap;
    var getChartDataUrl=config.getChartDataUrl;
    var showChartSheet=config.showChartSheet;
    var showIndicators=config.showIndicators;
    var fieldsCodeNameMap=config.fieldsCodeNameMap;
    var drillDownFilerColumns=config.drillDownFilerColumns;
    var sectionId=config.sectionId;
    var externalFilter=config.externalFilter;
    var ciomsLink=config.ciomsLink;
    var suppressLabels=config.suppressLabels;
    var EMPTY_LABEL=config.EMPTY_LABEL;
    var index=config.index;
    var widget=config.widget;
    var templateHeader=config.templateHeader;
    var reportRecordAjaxURL = config.reportrecordajaxurl;
    var caseFormUrl = config.caseFormUrl;
    var reportResultId = config.reportResultId;

    var reportDataTable;
    var reportDataTableFilter = []
    var tableData = [];
    var tableDataAjax = [];
    var columns = [];
    var groupColumnsIndexes = [];
    var drillDownCfg = drillDownFilerColumns ? drillDownFilerColumns : [];
    var selectAll;
    var recordCount;
    var pageFilter;
    var selectedCheckboxes = []



    showDataTableLoader($('#tableDiv'+index))
    if (rowColumns.length > 0) {
        columns.push({
            "className": 'details-control bold-font',
            "width": '30px',
            "orderable": false,
            "data": null,
            "defaultContent": '<span class=\'fa fa-angle-down\'></span>'
        })
    }

    var versionIndex = _.indexOf(header, "masterVersionNum")
    var specialFieldsIgnoreList = ['cllRowId', 'actionItemStatus', 'latestComment', 'workFlowState', 'finalState', 'assignedToUser', 'assignedToUserId', 'assignedToGroup', 'assignedToGroupId', 'dueInDays', 'indicator', 'hasAttachments', 'hasIssues'];
    for (var i in header) {
        if (specialFieldsIgnoreList.indexOf(header[i]) > -1 || (serviceColumns && serviceColumns.indexOf(header[i]) > -1)) continue;

        if(i == 0){
            columns.push({
                title: '<div class="checkbox checkbox-primary" > ' +
                '<input type="checkbox" name="selectAll"  class="selectAllCheckbox"/>' +
                '<label for="selectAll"></label>' +
                '</div>',
                width: '25px',
                bSortable: false,
                visible: (drillDownCfg && drillDownCfg.length > 0),
                render: function (data, type, row, meta) {
                    var cllRowId = row.cllRowId ?  row.cllRowId : -1;
                    var checked = '';
                    $.each(selectedCheckboxes, function (index, checkBox) {
                        if (checkBox.attr('data-cllRowId') == cllRowId) checked='checked';
                    });
                    return '<div class="checkbox checkbox-primary" > ' +
                        '<input type="checkbox" class="selectCheckbox"  name="selected" data-cllRowId="'+ cllRowId + '"' + checked + '/>' +
                        '<label></label></div>'
                }
            });
        }
        if (!_.contains(rowColumns, header[i])) {
            var inlineFilterType =  {type: "text"}
            if(fieldTypeMap[header[i]]==="Number") inlineFilterType = {type: "number"}
            if(fieldTypeMap[header[i]]==="Date") inlineFilterType = {type: "date-range"}
            const colFieldName = header[i];
            var col = {
                title: fieldsCodeNameMap[header[i]],
                mData: header[i],
                fieldName: colFieldName,
                stackId: stacked[header[i]],
                inlineFilter: inlineFilterType,
                width: '110px',
                sClass: "dataTableColumnCenter",
                render: function (data, type, row, meta) {
                    var fieldName = colFieldName;
                    return renderCell(row[fieldName], row, fieldName);
                }
            }
            columns.push(col)
        }
    }

    l1:for (var i = 0; i < groupColumns.length; i++) {
        for (var j = 0; j < columns.length; j++) {
            if (columns[j].mData == groupColumns[i]) {
                groupColumnsIndexes.push(j);
                continue l1;
            }
        }
    }

    function renderCell(val, row, fieldName) {
        var value = nvl(val);
        if (fieldTypeMap[fieldName] == "Date") {
            if (value === EMPTY_LABEL) {
                value = nvl("")
            } else {
                if(value.indexOf("+")>0)value=value.split("+")[0];
                var v = moment(value).format(DEFAULT_DATE_DISPLAY_FORMAT);
                if (v != "Invalid date") value = v;
                value = '<span class="date-min-100">' + value + '</span>';
            }
        }
        for (var i in drillDownCfg) {
            if (drillDownCfg[i].drillDownColumn == fieldName) {
                var filter = []
                for (var j in drillDownCfg[i].drillDownFilterColumn) {
                    var doc = new DOMParser().parseFromString(row[drillDownCfg[i].drillDownFilterColumn[j]], 'text/html');
                    filter.push({
                        field: drillDownCfg[i].drillDownFilterColumn[j],
                        value: escape(doc.body.textContent || '')
                    })
                }
                var v = "<a href='javascript:void(0)' class='drilldown"+index+"' data-field='" + drillDownCfg[i].drillDownColumn + "' data-reportId='" + drillDownCfg[i].drillDownReportId + "' " +
                    "data-filter='" + JSON.stringify(filter) + "'>" + value + "</a>";
                return v;
            }
        }
        if (drillDownCfg.length == 0 && fieldName == "masterCaseNum") {
            value = "<a href='" + ciomsLink + "?caseNumber=" + val.replace(/<i[^>]*>.*?<\/i>/g, '') + "&versionNumber=" + row.masterVersionNum + "' target='_blank'>" + val + "</a>"
        }
        return value ? ('<div class="one-row-dot-overflow">' + replaceAndFormatRows(value) + "</div>") : '';
    }

    $(document).on("click", ".exportButton", function (e) {
        e.preventDefault();
        e.stopPropagation();
        $("#exportForm").attr("action", $(this).attr("href"));
        if (pageFilter) {
            $("#direction").val(pageFilter.direction);
            $("#sort").val(pageFilter.sort);
            $("#globalSearch").val(pageFilter.globalSearch);
            $("#searchData").val(pageFilter.tableFilter);
            if(pageFilter.rowIdFilter != undefined) {
                $("#rowIdFilter").val(pageFilter.rowIdFilter);
            } else {
                var cllRowIds = []
                var checkedBoxes = $(".selectCheckbox:checked");
                if (checkedBoxes.length > 0) {
                    checkedBoxes.each(function (index) {
                        var selectedFilter = $(checkedBoxes[index]).attr("data-cllRowId");
                        cllRowIds.push(selectedFilter);
                    });
                }
                $("#rowIdFilter").val(cllRowIds);
            }
        }
        $("#exportForm").trigger('submit');
    });

    $(document).on("change", ".selectCheckbox", function () {
        var currentCllRowId = $(this).attr('data-cllRowId');
        if ($(this).is(":checked"))
            selectedCheckboxes.push($(this));
        else {
            var newArray = [];
            $.each(selectedCheckboxes, function (index, checkBox) {
                if (checkBox.attr('data-cllRowId') != currentCllRowId) {
                    newArray.push(checkBox);
                }
            });
            selectedCheckboxes = newArray;
        }
        $(this).trigger('tableSelectedRowsCountChanged', [(selectedCheckboxes ? selectedCheckboxes.length : 0)]);
    });

    $(document).on("click", ".drilldown"+index, function () {
        var url = $(this).attr("data-url");
        var drillDownForm = $("#drillDownForm"+index);
        if (url) drillDownForm.attr("action", url);
        var f = JSON.parse($(this).attr("data-filter"));

        var dataField = $(this).attr("data-field");
        $("#drillDownFormField"+index).val($(this).attr("data-field"));
        var checkedBoxes = $(".selectCheckbox:checked");
        if (selectedCheckboxes.length > 0) {
            for (var i in f) {
                f[i].value = []
            }

            $.each(selectedCheckboxes,function (index, checkBox) {
                var selectedFilter = JSON.parse($(checkBox.closest('tr')).find("[data-field='" + dataField + "']").attr("data-filter"))
                l1:for (var i in f)
                    for (var j in selectedFilter) {
                        if (f[i].field == selectedFilter[j].field) {
                            f[i].value.push(unescape(selectedFilter[j].value));
                            continue l1;
                        }
                    }
            });

        }

        $("#drillDownFormId"+index).val($(this).attr("data-reportId"));
        $("#drillDownFormFilter"+index).val(unescape(JSON.stringify(f)));
        drillDownForm.attr("method", (( $("#drillDownFormFilter"+index).val().length>1000)?"post":"get"));
        drillDownForm.trigger('submit');
    });

    var rowGroup = null
    var columnDefs = null
    var order = null
    var suppressList =[]
    if(suppressLabels){
        suppressList=suppressLabels.split(',');
    }
    if (groupColumns.length > 0) {
        rowGroup = {
            dataSrc: groupColumns,
            startRender: function ( rows, group, level ) {
                if (suppressList!== [] && suppressList.includes(fieldsCodeNameMap[groupColumns[level]])){
                    return $('<tr/>')
                        .append( '<td colspan="'+columns.length+'">'+group+'</td>' );
                }
                else {
                    return $('<tr/>')
                        .append('<td colspan="' + columns.length + '">' + fieldsCodeNameMap[groupColumns[level]] + ': ' + group + '</td>');
                }
            },
            endRender: null
        }
        columnDefs = [{
            targets: groupColumnsIndexes,
            visible: false
        }]
        order = _.map(groupColumnsIndexes, function (num) {
            return [num, 'asc'];
        });
    }
    reportDataTable = $('#table'+index).ExtendedDataTable({
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
            }}],
            bottomEnd: null,
        },
        language: { search: ''},
        order: order ? order : [],

        stateSaving: {
            isEnabled: true,
            stateDataKey: 'pvcAdvancedViewerTableStateKey'
        },
        autoWidth: false,
        fixedHeader: {
            isEnabled: true,
            topOffset: 70
        },
        colResize: {
            isEnabled: true,
            isResizable: function (column) {
                if (column.idx === 0) {
                    return false;
                }
                return true;
            }
        },

        rowGroup: rowGroup,
        columnDefs: columnDefs,
        columns: columns,
        "aLengthMenu": [[10, 50, 100, 200, 500], [10, 50, 100, 200, 500]],
        "pagination": true,
        "iDisplayLength": widget && (typeof showpage == 'undefined' || !showpage) ? 10 : 50,
        inlineFilterConfig: true,

        drawCallback: function (settings) {
            pageDictionary($('#table'+index+'_wrapper')[0], settings.aLengthMenu[0][0], recordCount);
        },
        "rowCallback": function (row, data, index) {
            colorCell(row);
        },
        "searching": true,
        "serverSide": true,
        "processing": true,
        "ajax": {
            "url": reportRecordAjaxURL,
            "type": "POST",
            "data":  function(d) {
                d.filterData =  JSON.stringify(externalFilter);
                d.reportResultId = reportResultId;
                d.globalSearch = $(".globalSearch").val();
                d.rowIdFilter = config.rowIdFilter;
                pageFilter= d;
            },
            'beforeSend': function (request) {
                var token = $("meta[name='_csrf']").attr("content");
                var header = $("meta[name='_csrf_header']").attr("content");
                var parameter = $("meta[name='_csrf_parameter']").attr("content");
                request.setRequestHeader(header, token);
            },
            "dataSrc": function(res) {
                tableDataAjax = [];
                recordCount = res.recordsFiltered;
                for (var i in res.aaData) {
                    var rowAjax = {}
                    for (var j in res.aaData[i]) {
                        rowAjax[header[j]] = res.aaData[i][j];
                    }
                    tableDataAjax.push(rowAjax);
                }
                return tableDataAjax;
            },
            "error": function(e){
                alert("Error in fetching CLL information");
                console.log(e);
            },
            "complete": function(){
                hideDataTableLoader($('#tableDiv'+index))
            }
        }
    }).on('draw.dt', function () {
        updateTitleForThreeRowDotElements();
    });
    if(!( widget && (typeof showpage == 'undefined' || !showpage) )) loadTableOption('#table' + index);

    var executeColumnSearch;
    $(document).on('keyup clear', '.globalSearch', function () {
        clearTimeout(executeColumnSearch);
        executeColumnSearch = setTimeout(function () {
            reportDataTable.draw();
        }, 1500);
    });

    $('#table'+index+' tbody').on('click', 'td.details-control', function () {
        var tr = $(this).closest('tr');
        var row = reportDataTable.row(tr);

        if (row.child.isShown()) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
            $(this).html("<span class='fa fa-angle-down'></span>");
        } else {
            // Open this row
            var span = $(spanRowFormat(row.data()));
            colorCell(span);
            row.child(span).show();
            tr.addClass('shown');
            $(this).html("<span class='fa fa-angle-up'></span>")
        }
    });

    function colorCell(container) {
        $(container).find('i[c]').each(function () {
            var color = $(this).attr("c")
            $(this).closest("td").attr("style", "background: "+color +" !important;");
        });
    }

    function spanRowFormat(d) {
        // `d` is the original data object for the row
        var result = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px; border-bottom: 1px solid #ccc;border-top: 1px solid #ccc; width: 100%">'
        for (var i in rowColumns) {

            var value = d[rowColumns[i]] ? replaceAndFormatRows(d[rowColumns[i]]) : '';
            result += '<tr>' +
                '<td nowrap="true" style="width:0%;"><b>' + fieldsCodeNameMap[rowColumns[i]] + ':</b></td>' +
                '<td style="text-align: left;">' + value + '</td>' +
                '</tr>'
        }
        result += '</table>';
        return result;
    }

    $(".backLink").on("click", function () {
        $("#backForm"+index).attr("action", $(this).attr("data-href"));
        $("#backFormId"+index).val($(this).attr("data-id"));
        $("#backFormFilter"+index).val(sessionStorage.getItem("breadcrumbs_" + sectionId + "_" + $(this).attr("data-id")));
        $("#backForm"+index).attr("method", (( $("#backFormFilter"+index).val().length>1000)?"post":"get"));
        $("#backForm"+index).trigger('submit');
    });

    function nvl(val) {
        if ((typeof val == 'undefined') ||(val === null) || (val === "")) return EMPTY_LABEL;
        return ""+val
    }

    $("#table"+index).on('draw.dt', function (e) {
        if ($(".selectAllCheckbox").is(":checked")) {
            $('.selectAllCheckbox').prop("checked", false);
        }
    }).on("change", 'input.selectAllCheckbox', function (e) {
        if ($(this).is(":checked")) {
            selectAll = true;
            $(".selectCheckbox").prop("checked", true);
            $.each($('.selectCheckbox'), function () {
                selectedCheckboxes.push($(this));
            });
        } else {
            selectAll = false;
            $(".selectCheckbox").prop("checked", false).trigger('change');
        }
        $(this).trigger('tableSelectedRowsCountChanged', [(selectedCheckboxes ? selectedCheckboxes.length : 0)]);
    });

    if (showChartSheet) {
        loadChart(getChartDataUrl, index);
    } else {
        $("#chart" + index).detach();
    }
    if (showIndicators) {
        $.ajax({
            url: reportRecordAjaxURL,
            data: {reportResultId: reportResultId, start: 0, length: 100},
            success: function (data) {
                for (var i in data.aaData) {
                    var row = data.aaData[i]
                    var label = row[0];
                    var value = row[1];
                    createIndicator(label, value, "#indicator" + index)
                }
                $("#indicator" + index).find('i').each(function () {
                    var color = $(this).attr("c")
                    $(this).parent().css('color', color);
                });
            }
        });

    }
    return {
        tableData: tableData,
        reportDataTable:reportDataTable,
    }
}

