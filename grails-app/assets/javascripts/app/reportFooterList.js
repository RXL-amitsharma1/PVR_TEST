var reportFooter = reportFooter || {};

reportFooter.reportFooterList = (function() {

    //Action item table.
    var report_footer__table;

    //The function for initializing the action item data tables.
    var init_report_footer_table = function(url) {

        //Initialize the datatable
        report_footer__table = $("#reportFooterList").DataTable({
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
            //"sPaginationType": "bootstrap",

            initComplete: function() {
                //Toggle the action buttons on the action item list.
                actionButton( '#reportFooterList' );
            },

            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "stateSave": true,
            "stateDuration": -1,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,
            "pagingType": "full_numbers",

            "aoColumns": [
                {
                    "mData": "reportFooterId",
                    "visible": false,
                    "mRender" : function(data, type, row) {
                        return '<span id="actionItemId">'+row.reportFooterId+'</span>';
                    }
                },
                {
                    "mData": "footer",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "description",
                    "mRender": function (data, type, row) {
                        if (!_.isEmpty(data)) {
                            var val = (data.length > 50 ? (data.substring(0, 50) + "...") : data);
                            return '<span title="' + encodeToHTML(data) + '">' + encodeToHTML(val) + '</span>';
                        } else {
                            return ""
                        }
                    }
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
                    "bSortable": false,
                    "mRender" : function(data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id='+row.reportFooterId+'" data-value="'+row.reportFooterId+'">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id='+row.reportFooterId+'" data-value="'+row.reportFooterId+'">' +$.i18n._('edit')+'</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('reportFooter') + '" data-instanceid="' + row.reportFooterId + '" data-instancename="' + replaceBracketsAndQuotes(row.footer) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
        return report_footer__table;
    };

    return {
        init_report_footer_table : init_report_footer_table
    }

})();