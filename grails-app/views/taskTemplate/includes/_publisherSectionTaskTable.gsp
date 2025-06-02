<%@ page import="com.rxlogix.config.PublisherSectionTask; com.rxlogix.config.ReportTask; com.rxlogix.config.ActionItemCategory; com.rxlogix.user.UserGroup; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PriorityEnum; com.rxlogix.user.User;" %>
<table class="table m-t-10 dataTable" id="taskTable">
    <tr>
        <th width="30">
            <g:if test="${addButton}">
            <div class="btn-group">
                <span class="table-add md md-plus dropdown-toggle pv-cross" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"></span>
                <ul class="dropdown-menu">
                    <li><a class="click table-add addTaskTable" ><g:message code="app.label.task.template.addTasks" default="Add"/></a></li>
                    <li><a class="click task-template-show"><g:message code="app.label.add.task.templates"/></a></li>
                </ul>
            </div>
            </g:if>
        </th>
        <th  width="150" style="display: none"><g:message default="Action Category" code="app.label.action.item.action.category"/></th>
        <th width="*"><g:message code="app.label.description"/></th>
        <th width="100"><g:message default="Priority" code="app.label.action.item.priority"/></th>
        <th width="200"><g:message default="Assigned To" code="app.label.action.item.assigned.to"/></th>
        <th width="285"><g:message default="Due Date" code="app.label.action.item.due.date"/></th>



    </tr>

    <tr  class="hide">
        <g:if test="${mode != 'show'}">
            <td style="vertical-align: middle"><span class="table-remove md md-close pv-cross removeTaskTable"></span></td>
        </g:if>
        <g:else>
            <td></td>
        </g:else>
        <td style="vertical-align: middle;display: none"> <g:select id="aiActionCategory" name="aiActionCategory" disabled="true"
                                                      from="${ViewHelper.actionItemCategoryEnumPvr()}"
                                                      optionKey="name" optionValue="display"
                                                      value="${ActionItemCategory.findByKey('PERIODIC_REPORT')?.key}"
                                                      noSelection="['': message(code: 'select.category')]"
                                                      class="form-control taskTemplateField select2-box"/></td>
        <td style="vertical-align: middle"><textarea style="height: 60px;" name="aiDescription" class="form-control taskTemplateField"></textarea></td>
        <td style="vertical-align: middle"><g:select name="aiPriority"
                                                     optionKey="name" optionValue="display"
                                                     from="${ViewHelper.priorityEnum}"
                                                     class="form-control select2-box taskTemplateField"/></td>
        <td style="vertical-align: middle"> <g:set var="users" value="${User.findAllByEnabled(true).sort{it.username}}"/>
        <g:set var="userGroups" value="${UserGroup.findAllByIsDeleted(false).sort{it.name}}"/>
            <select name="aiAssignedTo" class="form-control select2-box taskTemplateField" >
                <option value=""  data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToOwner')}</option>
                <option value="${ PublisherSectionTask.AssignToType.CONTRIBUTOR.name()}"  data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToContributor')}</option>
                <option value="${ PublisherSectionTask.AssignToType.AUTHOR.name()}"  data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToAuthor')}</option>
                <option value="${ PublisherSectionTask.AssignToType.APPROVER.name()}"  data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToApprover')}</option>
                <option value="${ PublisherSectionTask.AssignToType.REVIEWER.name()}"  data-blinded="false">${message(code: 'com.rxlogix.config.TaskTemplate.assignToReviewer')}</option>
                <g:if test="${userGroups}">
                    <optgroup label="${g.message(code: 'user.group.label')}"  data-blinded="false">
                        <g:each in="${userGroups}" var="userGroup">
                            <option value="${userGroup.getReportRequestorKey()}" data-blinded="${userGroup.isBlinded}">${userGroup.getReportRequestorValue()} </option>
                        </g:each>
                    </optgroup>
                </g:if>
                <optgroup label="${g.message(code: 'user.label')}"  data-blinded="false">
                    <g:each in="${users}" var="user">
                        <option value="${user.getReportRequestorKey()}" data-blinded="${user.isBlinded}">${user.getReportRequestorValue()} </option>
                    </g:each>
                </optgroup>
            </select></td>
        <td style="vertical-align: middle">

                <g:select style="display: inline; width: 175px" name='baseDate' id='baseDate' value="${PublisherSectionTask.BaseDate.CREATION_DATE.name()}"
                          from="${PublisherSectionTask.BaseDate.i18List}" optionKey="name" optionValue="display" class="form-control taskTemplateField"/>


                <g:select style="display: inline;width: 50px" name='sign' id='sign' from="${['+','-']}" class="form-control taskTemplateField"/>

            <input style=" display: inline; width: 50px" class="form-control taskTemplateField" min="1" type="number" name="aiDueDateShift" value="1" ></td>
    </tr>
</table>