var email = email || {};

email.emailList = (function() {

    //Action item table.
    var email__table;

    //The function for initializing the action item data tables.
    var init_email_table = function(url) {

        //Initialize the datatable
        email__table = $("#emailList").DataTable({
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
                actionButton( '#emailList' );
            },

            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,

            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,
            "pagination": true,
            "pagingType": "full_numbers",
            "stateSave": true,
            "stateDuration": -1,
            "aoColumns": [
                {
                    "mData": "id",
                    "visible": false,
                    "mRender" : function(data, type, row) {
                        return '<span id="actionItemId">'+row.id+'</span>';
                    }
                },
                {
                    "mData": "email"
                },
                {
                    "mData": "description",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
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
                    "bSortable": false,
                    "mData": null,
                    "mRender" : function(data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id='+row.id+'" data-value="'+row.id+'">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id='+row.id+'" data-value="'+row.id+'">' +$.i18n._('edit')+'</a></li> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('email') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.email) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
        return email__table;
    };

    return {
        init_email_table : init_email_table
    }

})();