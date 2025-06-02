var taskTemplate = taskTemplate || {};

taskTemplate.taskTemplateList = (function() {

    //Action item table.
    var task_template_table;

    //The function for initializing the action item data tables.
    var init_task_template_table = function(url) {

        //Initialize the datatable
        task_template_table = $("#taskTemplateList").DataTable({
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
                actionButton( '#taskTemplateList' );
            },

            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "aaSorting": [[0, "desc"]],
            "bLengthChange": true,
            "stateSave": true,
            "stateDuration": -1,

            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,

            "aoColumns": [
                {
                    "mData": "id",
                    "visible": false,
                    "mRender" : function(data, type, row) {
                        return '<span id="taskTemplateId">'+row.id+'</span>';
                    }
                },
                {
                    "mData": "name",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "type"
                },
                {
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
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('taskTemplate') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.name) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
        return task_template_table;
    };

    return {
        init_task_template_table : init_task_template_table
    }

})();