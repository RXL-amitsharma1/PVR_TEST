<%@ page import="com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.config.publisher.GanttItem; com.rxlogix.GanttService" %>
<style>
.selectTask {
    cursor: alias;
}
</style>

    <div class="gantt" id="gantt" style="width: 100%"></div>

<div id="taskDetail" class="modal fade">
    <div class="modal-dialog ">
        <div class="modal-content">
            <form action="${createLink(controller: "gantt", action: "updateTask")}" method="post">
                <input type="hidden" name="id" id="id">
                <input type="hidden" name="backUrl" id="backUrl" >
                <input type="hidden" name="type" id="type">
                <input type="hidden" name="cfgId" id="cfgId">

                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span></button>
                    <h4 id="modalTitle" class="modal-title"><g:message code="app.label.gantt.item.task.details"/></h4>
                </div>
                <div class="modal-body">
                    <div class="row"><div class="col-md-4"><label><g:message code="app.label.gantt.item.type"/></label>:
                    </div><div class="col-md-8 type"></div>
                    </div>
                    <div class="row"><div class="col-md-4"><label><g:message code="app.label.status"/></label>:
                    </div>
                        <div class="col-md-8 status">
                            <select class="form-control" name="complete" id="complete">
                                <option value="0"><g:message code="app.label.gantt.item.task.notcompleted"/></option>
                                <option value="100"><g:message code="app.label.gantt.item.task.completed"/></option>
                            </select>
                        </div>
                    </div>

                    <label><g:message code="app.label.gantt.item.task"/></label>

                    <textarea name="name" style="height: 70px" id="name" class="form-control"></textarea>

                    <label><g:message code="app.label.gantt.item.task.duration"/>:</label>
                    <div class="row">
                        <div class="col-md-1" style="margin-top: 6px;"><g:message code="app.dateFilter.from"/></div>
                        <div class="col-md-5">
                            <div class="fuelux">
                                <div class="datepicker" id="dateStartDiv">
                                    <div class="input-group">
                                        <g:textField id="dateStart" class="form-control" name="startDate"/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>

                        </div>
                        <div class="col-md-1" style="margin-top: 6px;"><g:message code="app.dateFilter.to"/></div>
                        <div class="col-md-5">
                            <div class="fuelux">

                                <div class="datepicker " id="dateEndDiv">
                                    <div class="input-group">
                                        <g:textField id="dateEnd" class="form-control " name="endDate"/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                    <label><g:message code="app.label.action.item.assigned.to"/>:</label>
                    <g:set var="users" value="${User.findAllByEnabled(true).sort { it.username }}"/>
                    <g:set var="userGroups" value="${UserGroup.findAllByIsDeleted(false).sort { it.name }}"/>
                    <select name="assignedTo" id="assignedTo" class="form-control select2-box taskTemplateField">
                        <option value="" data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToOwner')}</option>
                        <g:if test="${userGroups}">
                            <optgroup label="${g.message(code: 'user.group.label')}" data-blinded="false">
                                <g:each in="${userGroups}" var="userGroup">
                                    <option value="${userGroup.getReportRequestorKey()}" data-blinded="${userGroup.isBlinded}">${userGroup.getReportRequestorValue()}</option>
                                </g:each>
                            </optgroup>
                        </g:if>
                        <optgroup label="${g.message(code: 'user.label')}" data-blinded="false">
                            <g:each in="${users}" var="user">
                                <option value="${user.getReportRequestorKey()}" data-blinded="${user.isBlinded}">${user.getReportRequestorValue()}</option>
                            </g:each>
                        </optgroup>
                    </select>
                    <hr/>
                    <label><g:message code="app.label.gantt.item.dependsOn" default="Depends on"/></label>
                    <div class="dependson"></div>
                    <div>
                        <button type="button" id="dropDependence" class="btn btn-primary" data-dismiss="modal"><g:message code="app.label.gantt.item.depends.drop" default="Drop dependence"/></button>
                        <button type="button" id="changeDependence" class="btn btn-primary" data-dismiss="modal"><g:message code="app.label.gantt.item.depends.change" default="Change dependence"/></button>
                    </div>
                </div>
                <div class="modal-footer">
                    <a class="btn btn-primary link"><g:message code="default.button.open.label" default="Open"/></a>
                    <button type="submit" class="btn btn-primary"><g:message code="default.button.update.label"/></button>
                    <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="default.button.close.label"/></button>
                </div>
            </form>

        </div>
    </div>
</div>