$(function () {

    $("#odataSourcesList").DataTable({
        //"sPaginationType": "bootstrap",
        initComplete: function () {
            actionButton('#odataSourcesList');
        },
        "ajax": {
            "url": odataConfigListUrl,
            "dataSrc": ""
        },
        "stateSave": true,
        "stateDuration": -1,
        "aaSorting": [[3, "desc"]],
        "aoColumns": [
            {
                "mData": "dsName"
            },
            {
                "mData": "dsUrl"
            },
            {
                "mData": "dsLogin"
            },
            {
                "mData": "lastUpdated",
                "sClass": "dataTableColumnCenter forceLineWrapDate",
                "mRender": function (data, type, full) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
                }
            },
            {
                "mData": "modifiedBy"
            },
            {
                "mData": null,
                "mRender": function (data, type, row) {
                    var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="odataConfig?id=' + row.id + '" >' + $.i18n._('edit') + '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-action="deleteOdataSource" data-instancetype="' + $.i18n._('"app.odataConfig.dataSource') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.dsName) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                    return actionButton;
                }
            }
        ]
    });
});