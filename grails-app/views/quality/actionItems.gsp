<head>
    <asset:stylesheet src="quality.css"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>
    <asset:javascript src="app/actionItem/actionItemList.js"/>
    <asset:javascript src="app/actionItem/actionItem.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <meta name="layout" content="main"/>
    <title><g:message code="app.quality.task.actionitems.title"/></title>

    <script>
        var saveActionItemUrl = "${createLink(controller: "actionItem", action: "save")}";
        var updateActionItemUrl = "${createLink(controller: "actionItem", action: "update")}";
        var actionItemUrl = "${createLink(controller: 'actionItemRest', action: 'index')}?pvq=true";
        var deleteActionItemUrl = "${createLink(controller: 'actionItem', action: 'delete')}";
        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}"
    </script>
</head>
<body>
<div class="col-md-12">
    <g:if test="${params.id}">
        <script>
            $(function() {
                actionItem.actionItemModal.view_action_item(${params.id});
            })
        </script>
    </g:if>

    <rx:container title="${message(code: "actionItem.label")}">
        <div class="body">
            <div id="action-list-conainter" class="list pv-caselist">

                <div class="alert alert-danger hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <strong><g:message code="app.label.icsr.error"/> !</strong> <span id="errorNotification"></span>
                </div>

                <div class="alert alert-success hide">
                    <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
                    <strong><g:message code="app.label.success"/> !</strong> <span id="successNotification"></span>
                </div>


                <div class="pv-caselist">
                    <table id="actionItemList" class="table table-striped pv-list-table dataTable no-footer">
                        <thead>
                        <tr>
                            <th></th>
                            <th><g:message default="Action Category" code="app.label.action.item.action.category"/></th>
                            <th><g:message default="Assigned To" code="app.label.action.item.assigned.to"/></th>
                            <th><g:message default="Description" code="app.label.action.item.description"/></th>
                            <th><g:message default="Due Date" code="app.label.action.item.due.date"/></th>
                            <th><g:message default="Completion Date" code="app.label.action.item.completion.date"/></th>
                            <th><g:message default="Priority" code="app.label.action.item.priority"/></th>
                            <th><g:message default="Status" code="app.label.action.item.status"/></th>
                            <th><g:message default="Status" code="app.label.application"/></th>
                            <th><g:message default="Action" code="app.label.action.item.action"/></th>
                        </tr>
                        </thead>
                    </table>
                </div>
                <g:render template="/actionItem/includes/actionItemModal" model="[]" ></g:render>
            </div>
        </div>
    </rx:container>

    <g:render template="/actionItem/includes/createActionItem" model="[aiheight:20, aiwidth:20]" ></g:render>
    <g:render template="/includes/widgets/deleteRecord"/>
</div>

</body>
