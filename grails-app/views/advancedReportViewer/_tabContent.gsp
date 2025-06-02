<%@ page import="com.rxlogix.util.ViewHelper; groovy.json.JsonOutput;com.rxlogix.Constants" %>
<style>
.dt-paging-button {
    margin: 0 !important;
    padding: 0 !important;
}
.chartSelected{
    -webkit-filter: drop-shadow( -3px -3px 2px rgba(0, 0, 0, .7));
    filter: drop-shadow( -3px -3px 2px rgba(0, 0, 0, .7));
}
.chartNotSelected{
    opacity: 0.3;
}
.chartWidgetTableDiv .dt-type-numeric .dt-column-title {
    text-align: center;
}
.chartWidgetTableDiv.pv-caselist .pv-list-table .supressed-header-row th.supressed-header-col-content {
    border-radius: 0;
    border-color: #fff;
    border-right: 1px solid #fff;
    background-color: #f3f3f3 !important;
}
.chartWidgetTableDiv.pv-caselist .pv-list-table thead tr:last-child th {
    padding-top: 10px;
    padding-bottom: 10px;
}
.chartWidgetTableDiv .dt-layout-row:first-child {
    margin-top: initial;
    padding-right: initial;
}
.chartWidgetTableDiv .dt-search {
    margin-top: 3px;
    margin-bottom: -4px;
}
.dt-container {
    overflow-x: auto;
}
table.pvtUi {
    margin: 0 auto;
}
table th{
    vertical-align: top;
    text-align: center !important;
    padding: 2px 4px !important;
}
.dataTables_wrapper {
    overflow-x: auto;
}
table td {
    cursor: default;
    font-size: 13px !important;
    vertical-align: middle !important;
    padding: 2px 4px !important;
    line-height: 1.4 !important;
}
.paginate_button {
    margin: 0 !important;
    padding: 0 !important;
}
</style>
<g:render template="warn" model="[warn:warn]"/>
<div class="col-md-12">
    <g:set var="index" value="${index ?: ""}"/>
    <g:set var="groupFieldsCount" value="${groupFields ? groupFields.size() : 0}"/>
    <g:if test="${widget && !params.showpage}">
    <g:if test="${params.boolean("showIndicators")}">
        <div id="indicator${index}" class="indicator" style="width: 100%">

        </div>
    </g:if>
    <g:else>
        <div id="chart${index}" class="tabChart" style="width: 100%; position: relative;"></div>
    </g:else>

    <g:if test="${supressedHeader}">
        <table id="table${index}SupressedHeader" style="display: block;">
            <thead>
                <g:each var="row" in="${supressedHeader}" status="i">
                    <g:if test="${i < supressedHeader.size() - 1}">
                        <tr class="supressed-header-row">
                            <g:each var="col" in="${row}" status="ind">
                                <g:if test="${i > 0 || ind > groupFieldsCount - 1}">
                                    <g:if test="${col.rowspan > 1}">
                                        <th class="supressed-header-col-content" style="text-align:center" colspan="${col.colspan}" rowspan="${col.rowspan - 1}"></th>
                                    </g:if>
                                    <g:else>
                                        <th class="supressed-header-col-content" style="text-align:center" colspan="${col.colspan}" rowspan="${col.rowspan}">${col.label}</th>
                                    </g:else>
                                </g:if>
                            </g:each>
                        </tr>
                    </g:if>
                </g:each>
            </thead>
        </table>
    </g:if>
    <div id="tableDiv${index}" class="chartWidgetTableDiv pv-caselist" style=" ${(params.boolean("showIndicators") || hideTable) ? 'display:none' : ''} ">
        <table id="table${index}" class="table table-striped display order-column list-table pv-list-table dataTable <g:if test="${supressedHeader}">supressed-header-table</g:if>" style="width: 100%; ">
            <thead>
            </thead>
            <tbody></tbody>
            <tfoot></tfoot>
        </table>
    </div>
    </g:if>
    <g:else>
        <g:if test="${showChartSheet}">
            <rx:container id="qualityTableContainer" title="${message(code: 'app.label.chart', default:"Chart")}:  ${sectionName ?: ""}"  >
                <div id="chart${index}" class="tabChart" style="width: 100%; position: relative;height: 400px"></div>
            </rx:container>
        </g:if>
        <div id="tableDiv${index}" class="chartWidgetTableDiv basicDataTable pv-caselist" style=" ${(params.boolean("showIndicators") || hideTable) ? 'display:none' : ''} ">
            <rx:container id="qualityTableContainer" title="${message(code: 'app.label.reportRequestField.section')}:  ${sectionName ?: ""}"  options="${true}"  customButtons="${g.render(template: "includes/interactiveTableButtons", model:pageScope.variables )}">
            <g:if test="${supressedHeader}">
                <table id="table${index}SupressedHeader" style="display: block;">
                    <thead>
                    <g:each var="row" in="${supressedHeader}" status="i">
                        <g:if test="${i < supressedHeader.size() - 1}">
                            <tr class="supressed-header-row">
                                <g:each var="col" in="${row}" status="ind">
                                    <g:if test="${i > 0 || ind > groupFieldsCount - 1}">
                                        <g:if test="${col.rowspan > 1}">
                                            <th class="supressed-header-col-content" style="text-align:center" colspan="${col.colspan}" rowspan="${col.rowspan - 1}"></th>
                                        </g:if>
                                        <g:else>
                                            <th class="supressed-header-col-content" style="text-align:center" colspan="${col.colspan}" rowspan="${col.rowspan}">${col.label}</th>
                                        </g:else>
                                    </g:if>
                                </g:each>
                            </tr>
                        </g:if>
                    </g:each>
                    </thead>
                </table>
            </g:if>
                <table id="table${index}" class="table table-striped display order-column list-table pv-list-table dataTable <g:if test="${supressedHeader}">supressed-header-table</g:if>" style="width: 100%; ">
                    <thead>
                    </thead>
                    <tbody></tbody>
                    <tfoot></tfoot>
                </table>
            </rx:container>
        </div>
    </g:else>
</div>
<form id="drillDownForm${index}" target='_blank' action="${createLink(controller: "advancedReportViewer", action: "viewCll")}">
    <input id="drillDownFormId${index}" type="hidden" name="parentId" value="${params.id}">
    <input id="drillDownFormFilter${index}" type="hidden" name="filter">
    <input id="drillDownFormCell${index}" type="hidden" name="cell">
</form>
<script>
    var rowIdFilter${index};
    var data${index} = ${raw(data)};
    var showTotal${index} = true;
    var reportDataTable${index};
    var drillDownToCaseList = ${drillDownToCaseList};
    var reportResultId = ${reportResultId};
    var exConfigId = ${exConfigId};
    var isFullData = true;

    function clickCallBack${index}(e) {
        e.stopPropagation();
        var pie = (e.point? (e.point.series.type=='pie'):(e.xAxis[0].axis.series[0].type=='pie'));
        var id = (e.point ? e.point : e.yAxis[0].axis.series[0].data[Math.round(e.xAxis[0].value)]).rowId;
        $("#chart${index} .chartSelected").removeClass("chartSelected");
        $("#chart${index} .chartNotSelected").removeClass("chartNotSelected");
        if (rowIdFilter${index} == id) {
            rowIdFilter${index} = null;
            isFullData = true;
        } else {
            rowIdFilter${index} = id;
            isFullData = false;
            if(!pie) {
                var yAxis = e.point ? e.point.series.chart.yAxis : e.yAxis
                $("#chart${index} .highcharts-point").addClass("chartNotSelected");
                for (var i in yAxis) {
                    var series = yAxis[i].series ? yAxis[i].series : yAxis[i].axis.series;
                    for (var j in series) {
                        var point = series[j].data[e.point ? e.point.x : Math.round(e.xAxis[0].value)];
                        if (point && point.graphic && point.graphic.element) {
                            point.graphic.element.classList.add("chartSelected")
                        }
                    }
                }
                $("#chart${index} .chartSelected").removeClass("chartNotSelected");
            }
        }
        reportDataTable${index}.draw();
    }

    (function () {
        var widget = ${!!widget};
        var header = ${raw(header)};
        var drilldownHeader = ${raw(drilldownHeader)};
        var fieldsCodeNameMap = ${raw(JsonOutput.toJson(fieldsCodeNameMap))};
        var columns = [];
        var rowFields = ${raw(JsonOutput.toJson(rowFields))};
        var groupFields = ${raw(JsonOutput.toJson(groupFields))};
        var columnFields = ${raw(JsonOutput.toJson(columnFields))};
        var measures = ${raw(JsonOutput.toJson(measures))};
        var getChartDataUrl = "${createLink(controller: 'advancedReportViewer', action: 'getChartWidgetDataAjax')}?id=${params.id}&click=clickCallBack${index}"
        var showChartSheet =${params.boolean("showIndicators")?false:showChartSheet};
        var totalRow = null;
        var totalRowIndex;
        var groupColumnsIndexes = [];
        function isNumeric(val){
            return !isNaN(val) && !isNaN(parseFloat(val));
        };
        function isHTML(str){
                var newStr = str.replace(/<([^>]+?)([^>]*?)>(.*?)<\/\1>/ig, '')
                .replace(/(<([^>]+)>)/ig, '')
                .trim();
            return (!newStr || isNumeric(newStr))
        };
        function parseDataValue(val){
            return val.replace(/<(.|\n)*?>/g, '');
        }
        function dataValue(data){
            if (typeof data != 'number' && isHTML(data)){
                 return parseDataValue(data);
            }
            return data;
        };

        totalLoop: for (var dataRow in data${index}) {
            var rowRecord = data${index}[dataRow];
            for (var dataIndex in rowRecord) {
                if ((rowRecord[dataIndex] === "Grand Total")||(rowRecord[dataIndex] === "総計")) {
                    totalRow = rowRecord;
                    totalRowIndex = dataRow;
                    break totalLoop;
                }
            }
        }
        if (totalRow == null) {
            totalLoop: for (var dataRow in data${index}) {
                var rowRecord = data${index}[dataRow];
                for (var dataIndex in rowRecord) {
                    if ((rowRecord[dataIndex] === "Total") || (rowRecord[dataIndex] === "総計")) {
                        totalRow = rowRecord;
                        totalRowIndex = dataRow;
                        break totalLoop;
                    }
                }
            }
        }

        if(totalRowIndex > -1){
            data${index}.splice(totalRowIndex, 1);
        }

        function hasDrillDown(cellName) {
            if ((cellName.indexOf("GP_") !== 0)&&(cellName.indexOf("_PA") ===-1)) return false;
            for (var i in measures) {
                if (cellName.indexOf(measures[i]) > -1) return true
            }
            return false
        }
        function getAdvancedViewSearchSortIcons(columnIndex) {
            return '<span class="sortSearchIcons" style="z-index: 100"> <i data-columnIndex="' + columnIndex + '" class="fa fa-arrow-down sortColumnIcon sortColumnIcon' + columnIndex + '"></i> <i class="fa fa-search searchColumnIcon"></i></span>';
        }
        for (var i in header) {
            var searchHtml = getAdvancedViewSearchSortIcons(i);
            var col = toNameLabel(header[i]);
            var cfg = {
                title: "<div><span class='cllHeaderLabel'>" + col.label.trim().replace(/(?:\r\n|\r|\n)/g, '<br>') +"</span>" + searchHtml + "<input  placeholder='Search' type='text' style='display: none' class='columnSearchCll'/></div>",
                mData: col.name,
                type:"num",
                sClass: (col.name.indexOf("ROW")>-1?"":"dataTableColumnCenter"),
                mRender: function (data, type, row, meta) {
                    var cellName = meta.settings.aoColumns[meta.col].mData;
                    try {
                        if (type == "sort" || type == 'type' || type == 'filter') return data
                        return renderDTValue(data, row, cellName)
                    } catch (e) {

                    }
                    return nvl(data);
                }
            };
            columns.push(cfg);
        }

        function formLocalNumber(data) {
            if (typeof data == 'number') return data.toLocaleString(userLocale)
            if (isNumeric(data)) {
               return parseFloat(data).toLocaleString(userLocale)
            }
            return data;
        }

        function renderDTValue(data, row, cellName) {
            if ((data == "Total") || (data == "Subtotal")|| (data == "総計")|| (data == "小計")) {
                if (groupFields && (groupFields.length > 0) && (cellName == ("ROW_" + (groupFields.length + 1))) && ((data == "Subtotal") || (data == "小計"))) {
                    data = "<b>"+$.i18n._('app.total')+"</b>";
                } else {
                    if((data == "Subtotal")|| (data == "小計"))
                        data = "<b>"+$.i18n._('app.subtotal')+"</b>";
                    else
                        data = "<b>"+$.i18n._('app.total')+"</b>";
                }
            }
            let dataLabel =((!cellName.startsWith("ROW") && data !== 0 && data != "0.00") ? formLocalNumber(data) : data)
            if (drillDownToCaseList && cellName.startsWith("GP") && data !== 0) {
                if (window.location !== window.parent.location) {
                    return '<a class="dropdown-item" href="${createLink(controller: 'caseSeries',action: 'previewCrosstabCases')}/' + reportResultId + '?rowId=' + row['ID'] + '' +
                        '&columnName=' + cellName + '&count=' + dataValue(data) + '" target="blank">' + dataLabel + '</a>';
                } else {
                    return '<div class="btn-group">' +
                        '<a href="javascript:void(0)" class="dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' + dataLabel +
                        '</a>' +
                        '<div class="dropdown-menu">' +
                        '<li><a class="dropdown-item" href="${createLink(controller: 'caseSeries',action: 'previewCrosstabCases')}/' + reportResultId + '?rowId=' + row['ID'] + '' +
                        '&columnName=' + cellName + '&count=' + dataValue(data) + '">Case Series</a></li>\n' +
                        '<li class="addSectionLink"><a data-url="${createLink(controller: 'report', action: 'addTemplateSection')}/' + exConfigId + '?rowId=' + row['ID'] + '' +
                        '&columnName=' + cellName + '&count=' + dataValue(data) + '&executeRptFromCount=true&isInDraftMode=${isInDraftMode?:false}&reportResultId=' + reportResultId + '" href="#" data-toggle="modal" data-target="#addTemplateSectionModal">Report Template</a></li>' +
                        '</div>' +
                        '</div>';
                }
            } else if (hasDrillDown(cellName) && cellName.startsWith("GP") && data !== 0 && data !== "${Constants.NA}") {
                return "<a href='javascript:void(0)' class='drilldown${index}' data-id='" + row.ID + "' data-cell='" + cellName + "'" + ">" + dataLabel + "</a>"
            } else {
                if(cellName.indexOf("ROW")>-1) return "<div class='two-row-dot-overflow'>"+nvl(data)+"</div>";
                return dataLabel??"";
            }
        }

        function nvl(val) {
            if ((val == null)) return "${ViewHelper.getEmptyLabel()}";
            var sval = ("" + val);
            if ((sval.indexOf(".") == 0) && (sval.indexOf("%") == sval.length - 1)) return "0" + sval;

            return val
        }

        $(document).on("click", ".drilldown${index}", function () {
            var id = $(this).attr("data-id");
            var cell = $(this).attr("data-cell");
            var filter = getCellFilter(id, cell);
            // filter.push({additionalData: $(this).attr("data-additional")})
            $("#drillDownFormFilter${index}").val(JSON.stringify(filter));
            $("#drillDownFormCell${index}").val(cell);
            $("#drillDownForm${index}").submit();

        });

        function getCellFilter(id, cell) {
            var blockIndex = parseInt(cell.substring(cell.length - 1))
            //  var blockType = parseInt(cell.substring(cell.length - 2), cell.substring(cell.length - 1))
            var row;
            for (var i in data${index}) {
                if (data${index}[i].ID == id) {
                    row = data${index}[i];
                    break
                }
            }
            var filter = [];
            for (var k = 1; k < rowFields.length + 1; k++) {
                var fieldName = "ROW_" + k
                if (!row || row[fieldName] == null) break;
                if ((row[fieldName] == "Total") || (row[fieldName] == "Subtotal") || (row[fieldName] == "Grand Total")|| (row[fieldName] == "総計")|| (row[fieldName] == "小計")) break;
                if (fieldName.indexOf("ROW") === -1) continue;
                for (j in drilldownHeader) {
                    var n = drilldownHeader[j][fieldName]
                    if (n) {
                        filter.push({field: rowFields[j], value: fixValue(row[fieldName])});
                    }
                }
            }
            var columns = []
            for (var i in columnFields) {
                if (columnFields[i].blockIndex == blockIndex) {
                    columns = columnFields[i].columns;
                    break;
                }
            }

            for (j in drilldownHeader) {
                var n = drilldownHeader[j][cell]
                if (n) {
                    vals = n.trim().split("\n");
                    for (var i in columns) {
                        filter.push({field: columns[i].trim(), value: fixValue(vals[i])})
                    }
                }
            }
            return filter;
        }

        function fixValue(val) {
            if (val == "Blank" || val == '(empty)') return ""
            return val;
        }

        function toNameLabel(obj) {
            for (var k in obj) {
                return {name: k, label: obj[k]}
            }
        }

        $.fn.dataTable.ext.search.push(
            function (settings, row, dataIndex) {
                if (settings.nTable.id == ("table${index}")) {
                    var index = settings.nTable.id.substring(5);
                    if (rowIdFilter${index} != null && (!rowIdFilter${index}.split(",").includes("" + data${index}[dataIndex].ID))) {
                        return false;
                    }
                    if (showTotal${index}) return true;
                    for (var i = 0; i < row.length; i++) {
                        if ((row[i] == "Subtotal") || (row[i] == "Total")|| (row[i] == "小計")|| (row[i] == "総計")) return false;
                    }
                }
                return true;
            }
        );
        var tableFooterElement = $('#table${index} tfoot');
        tableFooterElement.append('<tr>');
        for(var iter=0;iter<header.length;iter++){
            tableFooterElement.find('tr').append('<td class="totalRowFooter"></td>');
        }
        var rowGroup = null;
        var columnDefs = []
        var order = []
        var dataSrc = []
        for (var i = 0; i < groupFields.length; i++) {// in DT grouping fields going first
            groupColumnsIndexes.push(i);
            dataSrc.push(toNameLabel(header[i]).name);
        }
        if (groupFields.length > 0) {
            rowGroup = {
                dataSrc: dataSrc,
                startRender: function (rows, group, level) {
                    return $('<tr/>')
                        .append('<td colspan="' + columns.length + '">' + toNameLabel(header[level]).label + ': ' + group + '</td>');
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
        tableFooterElement.append('</tr>');
        reportDataTable${index} = $('#table${index}').DataTable({
            "layout": {
                topStart: null,
                topEnd: {search: {placeholder: 'Search'}},
                bottomStart: ['pageLength', 'info', {
                    paging: {
                        type: 'full_numbers'
                    }
                }],
                bottomEnd: null,
            },
            language: { search: ''},
            "bInfo": true,
            "order": order,
            columnDefs: columnDefs,
            rowGroup: rowGroup,
            "lengthMenu": [[10, 20, 50, -1], [10, 20, 50, "All"]],
            "iDisplayLength": ((widget && (typeof showpage == 'undefined' || !showpage)) ? 10 : 50),
            data: data${index},
            columns: columns,
            fixedHeader: {headerOffset: 62},
            colResize: {hasBoundCheck: false},
            initComplete: function () {
                onCompleteTableInit($('#table${index}'), reportDataTable${index}, null);

                this.api().columns().every(function () {
                    var that = this;
                    $('input', this.header()).on('keyup clear', function () {
                        if (that.search() !== this.value) {
                            that
                                .search(this.value)
                                .draw();
                        }
                    });
                });
            },
            "rowCallback": function (row, data, index) {
                $(row).find('i[c]').each(function () {
                    var color = $(this).attr("c")
                    $(this).closest("td").attr("style", "background: "+color +" !important;");
                });
            },
            "pagination": true,
            "pagingType": "full_numbers",

            drawCallback: function (settings) {
                pageDictionary($('#table${index}_wrapper')[0], settings._iDisplayLength, data${index}.length);
                $(".sorting_1").removeClass("sorting_1");
                updateTitleForThreeRowDotElements();

                if (settings.nTable.classList.contains('supressed-header-table')) {
                    const supressedHeaderRows = $('#table${index}SupressedHeader thead tr');
                    const tableHeaderRow = $('#table${index} thead tr').first();
                    supressedHeaderRows.each(function (index, headerRow) {
                        tableHeaderRow.before(headerRow);
                    });
                }
            },
            footerCallback: function (tfoot, data, start, end, display) {
                if(totalRow) {
                    var info = reportDataTable${index}.page.info();
                    if ((info.page + 1) == info.pages) {
                        var api = this.api();
                        var key;
                        var firstVisible = rowGroup?.dataSrc?.length ? rowGroup?.dataSrc?.length : 0;
                        for (var footerColumn = 0; footerColumn < columns.length; footerColumn++) {
                            key = Object.keys(header[footerColumn])[0];
                            var html = (((footerColumn == firstVisible) && (groupFields.length > 0)) ? $.i18n._("app.grand.total") :  renderDTValue(totalRow[key], totalRow, key))
                            $(api.column(footerColumn).footer()).html('<b style="word-break: normal;">' + html + '</b>');
                        }
                        $(tfoot).show();
                    } else {
                        $(tfoot).hide();
                    }
                }
            }
        });
        if (!(widget && (typeof showpage == 'undefined' || !showpage))) {
            loadTableOption('#table${index}');
            $("#tableColumns tbody").append("<tr class='rxmain-dropdown-settings-table-enabled'></tr>")//table option hides the last column, so adding fake one
            $(document).on("resize", ".highcharts-container", function () {
                setTimeout(function () {//wait for chart animation completed
                    reportDataTable${index}.fixedHeader.adjust();
                }, 1000)
            })
        }
        initAdvancedViewerSearch(${index});

        $(".columnSearch").on('click', function (e) {
            e.stopPropagation();
        });
        var row = $("#tableDiv${index}").find(".dataTables_length").parent().parent();
        row.find(".col-xs-10").removeClass("col-xs-10").addClass(".col-xs-8")
        row.find(".col-xs-2").after("<div class='col-xs-2'><div class=\"checkbox checkbox-primary showTotalDiv\">\n" +
            "    <input type=\"checkbox\" id=\"showTotal${index}\" checked/>\n" +
            "    <label for=\"showTotal${index}\">Show Total</label>\n" +
            "</div></div>");

        if (!widget || ((typeof showpage !== 'undefined') && showpage)) {
            var executeColumnSearch;
            $(document).on('keyup clear', '.globalSearch', function () {
                clearTimeout(executeColumnSearch);
                executeColumnSearch = setTimeout(function () {
                    var searchInput = $(".dt-search input")
                    searchInput.val($(".globalSearch").val());
                    searchInput.trigger("keyup");
                }, 200);
            });
        }

        $(document).on("click", "#showTotal${index}", function () {
            showTotal${index} = !showTotal${index};
            reportDataTable${index}.draw();
        });
        if (showChartSheet) {
            loadChart(getChartDataUrl, '${index}', ${params.frame?"true":"false"});
        } else {
            $("#chart${index}").detach();
        }
        ${params.frame?"window.onresize =resizeToFrame;":""}
        $(".tabChart").on("click", function(){
            $("#chart${index} .chartNotSelected").removeClass("chartNotSelected");
            $("#chart${index} .chartSelected").removeClass("chartSelected");
            if(!isFullData){
                isFullData = true;
                rowIdFilter${index} = null;
                reportDataTable${index}.draw();
            }
        });
        if (${""+!!params.boolean("showIndicators")}) {
            for (var i in data${index}) {
                var row = data${index}[i]
                var label = row["ROW_1"];
                var value
                for (var k in row) {
                    if (k.indexOf("GP") > -1) {
                        value = row[k];
                        break
                    }
                }
                createIndicator(label, value, "#indicator${index}")
            }
            $("#indicator${index}").find('i').each(function () {
                var color = $(this).attr("c")
                $(this).parent().css('color', color);
            });
        }

        function initAdvancedViewerSearch(_index) {
            var index = typeof _index == "undefined" ? "" : _index;
            $(document).on("mouseover", '#table' + index + ' th', function () {
                updateSortableColumnHeaderStyles(this);
            });

            $(document).on("mouseout", '#table' + index + ' th', function () {
                $(this).closest("th").find(".sortSearchIcons").hide();
            });
        }

        function updateSortableColumnHeaderStyles(headerElement) {
            const header = $(headerElement);
            const sortColumnIcon = header.find(".sortColumnIcon");
            if (header.hasClass("dt-ordering-desc")) {
                sortColumnIcon.removeClass("fa-arrow-down").addClass("fa-arrow-up");
                header.find("i").css("background", "#ebebeb");
            } else if (header.hasClass("dt-ordering-asc")) {
                sortColumnIcon.removeClass("fa-arrow-up").addClass("fa-arrow-down");
                header.find("i").css("background", "#ebebeb");
            } else {
                sortColumnIcon.removeClass("fa-arrow-down").addClass("fa-arrow-up");
                header.find("i").css("background", "#FFFFFF");
            }
            header.closest("th").find(".sortSearchIcons").show();
        }

        function onCompleteTableInit(table, dataTable, rowColumns) {
            table.find('thead').addClass('filter-head').addClass('custom_sort_search');
            table.find('th').off().on('click', function (event) {
                const eventTarget = $(event.target);
                const headerElement = eventTarget.closest('th');
                if (eventTarget.hasClass('sortColumnIcon')) {
                    doTableSort(eventTarget, dataTable, rowColumns);
                    updateSortableColumnHeaderStyles(headerElement);
                } else if (eventTarget.hasClass('searchColumnIcon')) {
                    const searchBox = headerElement.find(".columnSearchCll");
                    if (searchBox.is(":visible")) {
                        if (!_.isEmpty(searchBox.val())) {
                            searchBox.val('').trigger("change");
                        }
                        searchBox.hide();
                    } else {
                        searchBox.show();
                    }
                } else if (!eventTarget.hasClass('searchColumnIcon') && !eventTarget.hasClass('columnSearchCll')) {
                    doTableSort(headerElement.find('.sortColumnIcon'), dataTable, rowColumns);
                    updateSortableColumnHeaderStyles(headerElement);
                }
                event.stopPropagation();
            });
            if (!widget || ((typeof showpage !== 'undefined') && showpage)) {
                $(".dt-search").parent().hide();
            }
        }

        function doTableSort(sortElement, dataTable, rowColumns) {
            const columnIndex = parseInt(sortElement.attr("data-columnIndex"));
            let order = 'asc';
            let shift = 0;
            if (rowColumns && rowColumns.length > 0) {
                shift = 1;
            }
            const orderData = Array.isArray(dataTable.order()[0]) ? dataTable.order() : [dataTable.order()];
            if (orderData[0] && (orderData[0][0] === (shift + columnIndex)) && (orderData[0][1] === 'asc')) {
                order = 'desc';
            }
            dataTable.order([shift + columnIndex, order]).draw();
        }
    })();
</script>
