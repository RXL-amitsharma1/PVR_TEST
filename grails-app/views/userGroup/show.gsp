<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.user.User; com.rxlogix.user.UserRole" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'userGroup.label')}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
%{--    <asset:stylesheet href="user-group.css"/>--}%
</head>
<body>
<div class="content">
    <div class="container">
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<rx:container title="${message(code: "user.group.view.label")}">
    <g:render template="/includes/layout/flashErrorsDivs" bean="${userGroupInstance}" var="theInstance"/>
    <div class="row">
        <div class="col-md-12">
            <h3 class="sectionHeader"><g:message code="user.group.details.label" /></h3>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.name.label"/></label></div>
                <div class="col-md-${column2Width}">${userGroupInstance?.name}</div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.description.label"/></label></div>
                <div class="col-md-${column2Width}">${userGroupInstance?.description}</div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.template.blinded"/></label></div>
                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userGroupInstance?.isBlinded}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" /></div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.field.profile.label"/></label></div>
                <div class="col-md-${column2Width}">${userGroupInstance?.fieldProfile?.name ?:'All Fields'}</div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.source.profiles.label"/></label></div>
                <div class="col-md-${column2Width}">${userGroupInstance?.sourceProfiles?.sourceName?.join(", ")}</div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.date.range.type.label"/></label></div>
                <div class="col-md-${column2Width}">${userGroupInstance?.dateRangeTypes?.each{ViewHelper.getDateRangeTypeI18nMessage(it.id)}?.join(", ")}</div>
            </div>
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.field.dataProtectionQuery.label"/></label></div>
                <div class="col-md-${column2Width}">${userGroupInstance?.dataProtectionQuery?.nameWithDescription}</div>
            </div>
            <div class="row"><div class="col-md-${column2Width}"></div></div>

    <g:if test="${userGroupInstance?.defaultRRAssignTo}">
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="userGroup.field.defaultAssignTo.label"/></label></div>
                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userGroupInstance?.defaultRRAssignTo}" true="${message(code: "default.button.yes.label")}" false="${message(code: "default.button.no.label")}" /></div>
            </div>
    </g:if>
        </div>
    </div>
    <div style="margin-top: 40px"></div>
    <h3 class="sectionHeader"><g:message code="roles.label"/></h3>
    <div class="row">
        <g:if test="${userGroupAuthority}">
            <g:each in="${userGroupAuthority}" var="userRoleInstance" >
                <div class="row">
                    <div class="col-md-6">
                        <div class="col-md-${column2Width}">
                            ${userRoleInstance}
                        </div>
                    </div>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <div class="row">
                <div class="col-md-6">
                    <div class="col-md-${column2Width}">
                        <g:message code="app.label.none.parends"/>
                    </div>
                </div>
            </div>
        </g:else>

    </div>
    <h3 class="sectionHeader"><g:message code="userGroup.users.label" /></h3>
    <div class="row">
        <g:if test="${userGroupUser}">
            <g:each in="${userGroupUser}" var="userInstance" status="i">
                <div class="row">
                    <div class="col-md-6">
                        <div class="col-md-${column2Width}">
                            ${i+1}. ${userInstance}
                        </div>
                    </div>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <div class="row">
                <div class="col-md-6">
                    <div class="col-md-${column2Width}">
                        <g:message code="app.label.none.parends"/>
                    </div>
                </div>
            </div>
        </g:else>
    </div>
    <h3 class="sectionHeader"><g:message code="app.label.dashboard.dashboards" /></h3>
    <div class="row">
        <g:if test="${dashboardList}">
            <g:each in="${dashboardList}" var="dashboard" status="i">
                <div class="row">
                    <div class="col-md-6">
                        <div class="col-md-${column2Width}">
                            ${i+1}. ${dashboard}
                        </div>
                    </div>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <div class="row">
                <div class="col-md-6">
                    <div class="col-md-${column2Width}">
                        <g:message code="app.label.none.parends"/>
                    </div>
                </div>
            </div>
        </g:else>
    </div>
   <div class="margin20Top"></div>

    <div class="row">
        <div class="col-md-9">
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${userGroupInstance}" var="theInstance"/>
        </div>
    <div class="col-md-3">
    <div class="buttonBar floatR">
        <div class="pull-right">
            <g:link action="edit" id="${userGroupInstance?.id ?:params.id}" class="btn btn-primary updateButton"><g:message code='default.button.edit.label'/></g:link>
            <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["userGroup", "index"]}'
                    id="cancelButton">${message(code: "default.button.cancel.label")}</button>
        </div>
    </div>
    </div>
    </div>
</rx:container>
    </div>
</div>
</body>
</html>
