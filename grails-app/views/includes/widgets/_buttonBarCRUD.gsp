<div class="buttonBarTop">

    <g:set var="controller" value="${controller ?: controllerName}"/>

    <g:form controller="${controller}" method="delete">

    %{--Add button--}%
        <g:if test="${showAddButton != false}">
            <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["${controller}", "create"]}' id="createButton">
                <span class="glyphicon glyphicon-plus icon-white"></span>
                <g:message code="default.new.label" args="[entityName]"/>
            </button>
        </g:if>

    %{--Edit button--}%
        <g:if test="${showEditButton != false}">
            <g:hiddenField name="id" value="${theInstance?.id}"/>
            <button type="button" class="btn btn-primary" data-evt-clk='{"method": "goToUrl", "params": ["${controller}", "edit", {"id": ${theInstance.id}}]}' id="${theInstance?.id}">
                <span class="glyphicon glyphicon-pencil icon-white"></span>
                <g:message code="default.button.edit.label"/>
            </button>
        </g:if>

    %{--Delete button--}%
        <g:if test="${showDeleteButton != false}">
            <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#deleteModal" data-instancetype="${controller}" data-instanceid="${theInstance?.id}" data-instancename="${whatIsBeingDeleted}">
                <span class="glyphicon glyphicon-trash icon-white"></span>
                ${message(code: 'default.button.delete.label')}
            </button>
            <g:render template="/includes/widgets/deleteRecord"/>
        </g:if>

    </g:form>
</div>