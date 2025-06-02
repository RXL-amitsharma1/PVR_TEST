<div id="assignedWithEditDiv" class="popupBox" style="position: absolute; display: none; width: 300px;z-index: 100">
    <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.currentUser}"/>
    <div class="row">
        <div class="col-xs-12">
            <label><g:message code="app.label.action.item.assigned.to"/></label>
            <select class="assignedWithControlEdit form-control select2Control" id="assignedWithEdit" name="assignedWith"></select>
        </div>
    </div>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="priorityEditDiv" class="popupBox" style="position: absolute; display: none; width: 300px">
    <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>
    <g:set var="qualityService" bean="qualityService"/>
    <div class="row">
        <div class="col-xs-12">

            <label><g:message code="app.label.report.request.priority"/></label>
            <select  class="priorityControlEdit form-control" id="priorityEdit" >
                <option value="-1"> </option>
                <g:each var="priority" in="${qualityService.getQualityPriorityList()}">
                    <option value="${priority}">${priority}</option>
                </g:each>
            </select>

        </div>
    </div>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="errorTypeEditDiv" class="popupBox" style="position: absolute; display: none; width: 300px">
    <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>
    <div class="row">
        <div class="col-xs-12">

            <label><g:message code="qualityModule.manualAdd.errorType.label"/></label>
            <select  class="editErrorTypeSelect form-control" id="editErrorTypeSelect" >

            </select>

        </div>
    </div>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>