var customField = customField || {};

customField.customFieldList = (function() {

    var custom_field_table;

    var init_custom_field_table = function(url) {

        //Initialize the datatable
        custom_field_table = $("#customFieldList").DataTable({
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
                actionButton( '#customFieldList' );
            },
            "ajax": {
                "url": url,
                fail: function (err) {
                    errorNotification((err.responseJSON.message ? err.responseJSON.message : "") +
                        (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                }
            },
            "pagination": true,
            "pagingType": "full_numbers",
            "aaSorting": [[3, "desc"]],
            "bLengthChange": true,
            "stateSave": true,
            "stateDuration": -1,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,

            "aoColumns": [
                {
                    "mData": "name",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "description",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "reportFieldLabel"
                },
                {
                    "mData": "lastUpdated",
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
                    "bSortable":false,
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
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('app.customField.label') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.name) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
        return custom_field_table;
    };

    return {
        init_custom_field_table : init_custom_field_table
    }

})();

$(function() {

    var init_custom_field_list_url = "list";
    //Initiate the datatable
    customField.customFieldList.init_custom_field_table(init_custom_field_list_url);



});