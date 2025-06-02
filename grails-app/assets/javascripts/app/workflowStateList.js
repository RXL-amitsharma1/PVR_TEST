var workflowState = workflowState || {};

workflowState.workflowStateList = (function() {

    //Action item table.
    var workflow_state_table;

    //The function for initializing the action item data tables.
    var init_work_state_table = function(url) {

        //Initialize the datatable
        workflow_state_table = $("#workflowStateList").DataTable({
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
            // "sPaginationType": "bootstrap",
            initComplete: function() {
                //Toggle the action buttons on the action item list.
                actionButton( '#workflowStateList' );
            },
            "ajax": {
                "url": url,
                "dataSrc": ""
            },

            "aaSorting": [[0, "desc"]],
            "bLengthChange": true,
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "iDisplayLength": 10,

            "aoColumns": [
                {
                    "mData": "workflowStateId",
                    "visible": false,
                    "mRender" : function(data, type, row) {
                        return '<span id="actionItemId">'+row.workflowStateId+'</span>';
                    }
                },
                {
                    "mData": "name",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "description",
                    "sType": "workflowStateList-string",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData" : "display"
                },
                {
                    "mData" : "finalState"
                },
                {
                    "mData": null,
                    "bSortable": false,
                    "mRender" : function(data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id='+row.workflowStateId+'" data-value="'+row.workflowStateId+'">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id='+row.workflowStateId+'" data-value="'+row.workflowStateId+'">' +$.i18n._('edit')+'</a></li> \
                                <li role="presentation"><a class="work-flow-edit hide-delete" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('workFlowState') + '" data-instanceid="' + row.workflowStateId + '" data-instancename="' + replaceBracketsAndQuotes(row.name) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        }).on('draw.dt', function () {
            setTimeout(function () {
                var lastTh = $('.pv-caselist.basicDataTable table th:last-child');
                lastTh.removeClass('sorting');
                lastTh.off();
            }, 100);
        });
        return workflow_state_table;
    };
    return {
        init_work_state_table : init_work_state_table
    }

})();