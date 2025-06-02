$(function() {

    var advanced_assignment_list_url = "list";

    function init_advanced_assignment_table(url) {

        //Initialize the datatable
        advanced_assignment_table = $("#advancedAssignmentList").DataTable({
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
                actionButton( '#advancedAssignmentList' );
            },

            "ajax": {
                "url": url,
                "dataSrc": ""
            },
            "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
            "aaSorting": [[1, "asc"]],
            "bLengthChange": true,
            "iDisplayLength": 10,
            "stateSave": true,
            "stateDuration": -1,
            "aoColumns": [
                {
                    "mData": "id",
                    "visible": false,
                    "mRender" : function(data, type, row) {
                        return '<span class="advancedAssignmentId">'+row.id+'</span>';
                    }
                },
                {
                    "mData": "name",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": "category",
                    mRender: function (data, type, row) {
                        if(data !== null) {
                            return encodeToHTML($.i18n._('app.advanced.assignment.report.category.' + data));
                        }
                        return null;
                    }
                },
                {
                    "mData" : "assignedUser",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData" : "qualityChecked",
                    mRender: function (data, type, row) {
                        return data ? encodeToHTML($.i18n._('yes')): encodeToHTML($.i18n._('no'));
                    }
                },
                {
                    "mData" : "description",
                    mRender: function (data, type, row) {
                        return encodeToHTML(data);
                    }
                },
                {
                    "mData": null,
                    "bSortable": false,
                    "mRender" : function(data, type, row) {
                        var actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs" href="show?id='+row.id+'" data-value="'+row.id+'">' +$.i18n._('view')+'</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="work-flow-edit" role="menuitem" href="edit?id='+row.id+'" data-value="'+row.id+'">' +$.i18n._('edit')+'</a></li> \
                                <li role="presentation"><a class="work-flow-edit hide-delete" role="menuitem" href="#" data-toggle="modal" \
                                    data-target="#deleteModal" data-instancetype="' + $.i18n._('"advanced.assignment"') + '" data-instanceid="' + row.id + '" data-instancename="' + replaceBracketsAndQuotes(row.name) + '">' + $.i18n._('delete') + '</a></li> \
                            </ul> \
                        </div>';
                        return actionButton;
                    }
                }
            ]
        });
        return advanced_assignment_table;
    }

    if($("#advancedAssignmentList").length > 0) {
        init_advanced_assignment_table(advanced_assignment_list_url);
    }

    $("#saveButton").on('click', function(e){
        e.preventDefault();
        if(validateAssignmentQuery($("#assignmentQuery").val().toLowerCase())){
            $("form#advancedAssignmentForm").submit();
        }else{
            $('#WarningDiv').show();
            $('#WarningDiv p').text($.i18n._('advanced.assignment.assignment.query.alert'));
            $('.WarningDivclose').on('click', function () {
                $('#WarningDiv').hide();
            });
        }
    });

    function validateAssignmentQuery(assignmentQuery){
        return ((assignmentQuery.indexOf('update') > -1 || assignmentQuery.indexOf('set') > -1) && !/\bdelete\b/.test(assignmentQuery)  && !/\binsert\b/.test(assignmentQuery) );
    }

    if (window.location.href.indexOf("show") > -1) {
        $('input').attr("disabled", true);
        $('select').attr('disabled', true);
        $('textarea').attr('disabled', true);
    }
});