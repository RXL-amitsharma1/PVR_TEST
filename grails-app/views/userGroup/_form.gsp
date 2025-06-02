<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.user.UserGroup; com.rxlogix.config.SourceProfile;com.rxlogix.Constants"%>
<script>
    $(function () {
        initializeSelect2("#sourceProfiles");
        initializeSelect2("#dateRangeTypes");
    });
    var GROUP_MANAGER="${Constants.GROUP_MANAGER}"
</script>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-xs-4">
                    <div class="${hasErrors(bean: userGroupInstance, field: 'name', 'has-error')}">
                        <label><g:message code="userGroup.name.label"/><span class="required-indicator">*</span></label>
                        <g:textField name="name" maxlength="${UserGroup.constrainedProperties.name.maxSize}"
                                     value="${userGroupInstance?.name}"
                                     placeholder="${message(code: 'userGroup.name.label')}"
                                     class="form-control"/>
                    </div>
                    <div>&nbsp;</div>
                    %{--Description--}%
                    <label for="description"><g:message code="userGroup.description.label"/></label>
                    <div>
                        <g:textArea name="description" value="${userGroupInstance?.description}"
                                     style="height: 70px;" maxlength="${UserGroup.constrainedProperties.description.maxSize}"
                                    placeholder="${message(code: 'userGroup.description.label')}"
                                    class="form-control"/>
                    </div>
                </div>
                <div class="col-xs-4">
                    <div>
                        <label><g:message code="userGroup.field.profile.label"/></label>
                        <g:select name="fieldProfile" from="${[]}"
                                     data-value="${userGroupInstance?.fieldProfile?.id}"
                                  class="form-control"></g:select>
                        <g:hiddenField name="fieldProfileVal" value="${userGroupInstance?.fieldProfile?.name}"/>
                    </div>
                    <div>
                        <label for="sourceProfiles"><g:message code="userGroup.source.profiles.label"/></label>
                        <g:select id="sourceProfiles" name="sourceProfiles"
                                  from="${sourceProfileList}"
                                  optionValue="sourceName" optionKey="id"
                                  value="${userGroupInstance?.sourceProfiles?.id?:SourceProfile.central.id}"
                                  multiple="true"
                                  class="form-control"/>
                    </div>
                    <div>
                        <label for="dataProtectionQuery"><g:message code="userGroup.field.dataProtectionQuery.label"/></label>
                        <g:select type="hidden" name="dataProtectionQuery" from="${[]}" data-value="${userGroupInstance?.dataProtectionQuery?.id}"
                                  class="form-control selectQuery"></g:select>
                    </div>
                    <div style="margin-top:7px; display: flex; align-items: center ">
                        <label class="pv-switch">
                            <input type="checkbox"
                                   name="defaultRRAssignTo"
                                   id="defaultRRAssignTo"
                                   class="workflowStateField checkbox form-element"
                                   data-value="${userGroupInstance?.defaultRRAssignTo}"
                                   data-on="${message(code: "default.button.yes.label")}"
                                   data-off="${message(code: "default.button.no.label")}"
                            />
                            <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                        </label>
                        <label for="defaultRRAssignTo"><g:message code="userGroup.field.defaultAssignTo.label"/></label>
                    </div>
                    <g:if test="${!canUpdateDefaultRRAssignedTo}">
                        <span style="">(<g:message code="com.rxlogix.config.UserGroup.defaultRRAssignTo.unique"/>)</span>
                        <script>
                            $(document).find("#defaultRRAssignTo").attr("disabled", "true");
                        </script>
                    </g:if>
                </div>
                <div class="col-xs-4">
                    <div>
                        <label for="dateRangeTypes"><g:message code="userGroup.date.range.type.label"/></label>
                        <g:select id="dateRangeTypes" name="dateRangeTypes"
                                  from="${ViewHelper.getDateRangeTypeI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${userGroupInstance?.dateRangeTypes?.id}"
                                  multiple="true"
                                  class="form-control"/>
                    </div>
                </div>
                <div class="clearfix"></div>

                <div style="margin-top: 20px">

                    <!-- Nav tabs -->
                    <ul class="nav nav-tabs" role="tablist">
                        <li role="presentation" class="active"><a href="#roles" aria-controls="roles" role="tab" data-toggle="tab"><g:message code="roles.label" /></a></li>
                        <li role="presentation"><a href="#users" aria-controls="users" role="tab" data-toggle="tab"><g:message code="userGroup.users.label" /></a></li>
                        <li role="presentation"><a href="#dasboards" aria-controls="dasboards" role="tab" data-toggle="tab"><g:message code="app.label.dashboard.dashboards" /></a></li>
                    </ul>

                    <!-- Tab panes -->
                    <div class="tab-content">
                        <div role="tabpanel" class="tab-pane active" id="roles">
                        <div class="row">
                            <div class="col-lg-6">
                        %{--Create Mode--}%
                            <g:if test="${actionName == 'create' || actionName == 'save'}">
                                <g:each var="role" in="${roleList.sort { it.authority }}" status="i">
                                    <div class="row">
                                        <div class="col-sm-6">
                                            <g:message code="app.role.${role.authority}" default="${role.authority}"/>
                                        </div>

                                        <div class="col-sm-6">
                                            <label class="pv-switch">
                                                <input
                                                    type="checkbox"
                                                        name="${role.authority}"
                                                        id="${role.authority}"
                                                        class="form-element"
                                                        data-value="${userGroupRoleList?.contains(role.authority)}"
                                                        data-on="${message(code: "default.button.yes.label")}"
                                                        data-off="${message(code: "default.button.no.label")}"
                                                />
                                                <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                                            </label>
                                        </div>
                                    </div>
                                </g:each>

                            </g:if>

                        %{--Edit Mode--}%
                            <g:if test="${actionName == 'edit' || actionName == 'update'}">
                                <g:each var="entry" in="${roleMap}">
                                    <div class="row" >
                                        <div class="col-sm-6">
                                            <g:message code="app.role.${entry.key.authority}" default="${entry.key.authority}"/>
                                        </div>

                                        <div class="col-sm-6">
                                            <label class="pv-switch">
                                                <input  type="checkbox"
                                                        name="${entry.key.authority}"
                                                        id="${entry.key.authority}"
                                                        class="form-element"
                                                        data-value="${entry.value}"
                                                        data-on="${message(code: "default.button.yes.label")}"
                                                        data-off="${message(code: "default.button.no.label")}"
                                                />
                                                <span class="switch-slider" data-on="${message(code: "default.button.yes.label")}" data-off="${message(code: "default.button.no.label")}"></span>
                                            </label>
                                        </div>
                                    </div>
                                </g:each>
                            </g:if>
                            </div>
                            <div class="col-lg-6"> </div>
                        </div>
                        </div>

                        <div role="tabpanel" class="tab-pane" id="users">
                            <div style="width: 100%">
                                <table id="rxUserTable" class=" table userTable table table-striped pv-list-table dataTable no-footer" width="100%">
                                    <thead>
                                    <tr class="userTableHeader">
                                        <th><g:message code="user.fullName.label"/></th>
                                        <th><g:message code="user.groupManager.label"/></th>
                                    </tr>
                                    </thead>
                                </table>
                            </div>
                            <a href="#" class="btn btn-primary add-remove-user" data-evt-clk='{"method": "modalShow", "params": ["#addRemoveUserModal"]}'>
                                <span class=" icon-white"></span><g:message code="userGroup.add.remove.users.label"/></a>
                        </div>
                        <div role="tabpanel" class="tab-pane" id="dasboards">
                            <div class="row">
                                <div class="col-md-5">
                                    <g:select name="availableDashboard"
                                              from="${availableDashboards}"
                                              optionValue="name" optionKey="id"
                                              class="form-control "/>
                                </div>
                                <div class="col-md-1">
                                    <a href="javascript:void(0)" class="btn btn-primary add-dashboard" >
                                        <g:message code="default.button.add.label" /></a>
                                </div>
                                <div class="col-md-6">

                                </div>

                            </div>
                            <div class="pv-caselist">
                            <table id="rxTableUserGroupDasboard" class="table table-striped pv-list-table dataTable no-footer" width="100%">
                                <thead>
                                <tr>
                                    <th><g:message default="Label" code="app.label.dashboardDictionary.label"/></th>
                                    <th><g:message default="Owner" code="app.label.dashboardDictionary.owner"/></th>
                                    <th><g:message default="Type" code="app.label.dashboardDictionary.type"/></th>
                                    <th><g:message default="Action" code="app.label.action"/></th>
                                </tr>
                                </thead>
                                <g:each in="${dashboardList}" var="dashboard">
                                    <tr>
                                        <td>
                                            ${dashboard.label}
                                            <input type="hidden" name="dashboardId" value="${dashboard.id}">
                                        </td>
                                        <td>
                                            ${dashboard.owner}
                                        </td>
                                        <td>
                                            ${message(code: dashboard.dashboardType.getI18nKey())}
                                        </td>
                                        <td>
                                            <div class="btn-group dropdown " align="center">
                                                <a href="${g.createLink(controller: 'dashboard',action:'index', params:[id:dashboard.id]  )}" class="btn btn-success btn-xs "><g:message code="app.label.view"/></a>
                                                <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                                                    <span class="caret"></span>
                                                    <span class="sr-only">Toggle Dropdown</span>
                                                </button>
                                                <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                                                    <li role="presentation"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions removeDashboard"
                                                                               data-id="${dashboard.id}"><g:message
                                                                code="default.button.remove.label"/></a></li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                </g:each>
                            </table>
                            </div>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </div>
</div>