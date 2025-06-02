$(function () {


    //Initiate the datatable
    init_report_request_Type_table("list", "#reportRequestTypeList", "type");
    init_report_request_Type_table("listPriority", "#reportRequestPriorityList", "priority");
    init_report_request_Type_table("listLink", "#reportRequestLinkList", "link");
    init_report_request_Type_table("listFields", "#reportRequestFieldList", "field");
    init_report_request_Type_table("listUsedDictionary?type=PSR_TYPE_FILE", "#psrTypeFileList", "PSR_TYPE_FILE");
    init_report_request_Type_table("listUsedDictionary?type=INN", "#innList", "INN");
    init_report_request_Type_table("listUsedDictionary?type=DRUG", "#drugCodeList", "DRUG");

});

var init_report_request_Type_table = function (url, tableId, tableType) {
    var sortColumnIndex = tableType === 'field' ? 6 : 3;
    var columnList = [
        {
            "mData": "id",
            "visible": false,
            "mRender": function (data, type, row) {
                return '<span id="actionItemId">' + row.id + '</span>';
            }
        },
        {
            "mData": "name",
            mRender: function (data, type, row) {
                return encodeToHTML(data);
            }
        }]

    if (tableId === "#reportRequestFieldList") {
        columnList = columnList.concat([
            {
                "mData": "label",
                "mRender": $.fn.dataTable.render.text()
            },
            {"mData": "index"},
            {"mData": "type"},
            {"mData": "section"}])
    } else {
        columnList.push({
            "mData": "description",
            mRender: function (data, type, row) {
                return encodeToHTML(data);
            }
        })
    }
    columnList = columnList.concat([
        {
            "mData": "lastUpdated",
            "sClass": "dataTableColumnCenter forceLineWrapDate",
            "mRender": function (data, type, full) {
                return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
            }
        },
        {
            "mData": "modifiedBy",
            "mRender": $.fn.dataTable.render.text()
        },
        {
            "mData": null,
            "bSortable": false,
            "mRender": function (data, type, row) {
                var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id=' + row.id + '&type=' + tableType + '" data-value="' + row.id + '">' + $.i18n._('view') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id=' + row.id + '&type=' + tableType + '" data-value="' + row.id + '">' + $.i18n._('edit') + '</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('reportRequestType') + '" data-instanceid="' + row.id + '" \
                                    data-evt-clk=\'{\"method\": \"setDataValue\", \"params\":[\"#type\", \"'+tableType+'\"]}\' data-instancename="' + replaceBracketsAndQuotes(encodeToHTML(row.name)) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                return actionButton;
            }
        }
    ]);
    //Initialize the datatable
    report_request_type__table = $(tableId).DataTable({

        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        language: { search: ''},
        //"sPaginationType": "bootstrap",

        initComplete: function () {
            //Toggle the action buttons on the action item list.
            actionButton(tableId);
        },

        "ajax": {
            "url": url,
            "dataSrc": ""
        },
        "pagingType": "full_numbers",
        "aaSorting": [[sortColumnIndex, "desc"]],
        "bLengthChange": true,
        "iDisplayLength": 10,
        "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
        "aoColumns": columnList,
        "pagingType": "full_numbers",
    }).on('draw.dt', function () {
        setTimeout(function () {
            var lastTh = $('.pv-caselist table th:last-child');
            lastTh.removeClass('sorting');
            lastTh.off();
        }, 100);
    });
    return report_request_type__table;
};
