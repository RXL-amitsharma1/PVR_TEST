<%@ page import="grails.util.Holders; com.rxlogix.user.UserGroupUser; com.rxlogix.user.UserRole; com.rxlogix.user.Role;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'user.label')}"/>
    <title><g:message code="app.userManagement.show.user"/></title>
</head>


<body>
<div class="content">
    <div class="container">
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<rx:container title="${message(code: "app.label.userManagement")}">
    <div class="row breadcrumbs">
        <div class="col-md-6">
            <g:link action="index"><< <g:message code="users.list"/></g:link>
        </div>
    </div>




    <g:render template="/includes/layout/flashErrorsDivs" bean="${userInstance}" var="theInstance"/>

    <g:set var="whatIsBeingDeleted"
           value="${userInstance.fullName + (userInstance?.email ? ' (' + userInstance.email + ')' : '')}"/>
    <g:render template="/includes/widgets/buttonBarCRUD" bean="${userInstance}" var="theInstance"
              model="[showDeleteButton: false]"/>

%{--User Details--}%
    <h3 class="sectionHeader"><g:message code="user.details"/></h3>

    <div class="row">

        <div class="col-md-6">

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.username.label"/></label></div>

                <div class="col-md-${column2Width}">${userInstance.username}</div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.fullName.label"/></label></div>

                <div class="col-md-${column2Width}">${userInstance.fullName}</div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.email.label"/></label></div>

                <div class="col-md-${column2Width}">${userInstance.email}</div>
            </div>

            <g:if test="${Holders.config.get('pvreports.multiTenancy.enabled')}">
                <div class="row">
                    <div class="col-md-${column1Width}"><label><g:message code="user.tenants.label"/></label></div>

                    <div class="col-md-${column2Width}">${userInstance.tenants*.displayName.join(', ')}</div>
                </div>
            </g:if>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.lastLogin.label"/></label></div>

                <div class="col-md-${column2Width}">
                    <g:renderLastLoginDateForUser user="${userInstance}"/>
                </div>
            </div>
        </div>

        <div class="col-md-6">

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.enabled.label"/></label></div>

                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.enabled}" false="${message(code: "default.button.no.label")}"
                                                                     true="${message(code: "default.button.yes.label")}"/></div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.accountLocked.label"/></label></div>

                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.accountLocked}" false="${message(code: "default.button.no.label")}"
                                                                     true="${message(code: "default.button.yes.label")}"/></div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.badPasswordAttempts.label"/></label>
                </div>

                <div class="col-md-${column2Width}">${userInstance.badPasswordAttempts}</div>
            </div>

            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="user.accountExpired.label"/></label></div>

                <div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.accountExpired}" false="${message(code: "default.button.no.label")}"
                                                                     true="${message(code: "default.button.yes.label")}"/></div>
            </div>

            %{--<div class="row">--}%
            %{--<div class="col-md-${column1Width}"><label><g:message code="user.passwordExpired.label"/></label></div>--}%

            %{--<div class="col-md-${column2Width}"><g:formatBoolean boolean="${userInstance.passwordExpired}"--}%
            %{--false="No" true="Yes"/></div>--}%
            %{--</div>--}%
        </div>

    </div>

    <div style="margin-top: 40px"></div>

%{--Roles--}%
    <h3 class="sectionHeader"><g:message code="roles.label"/></h3>

    <div class="row">
        <g:if test="${roles}">
            <g:each in="${roles}" var="userRoleInstance" >
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

%{--User Group--}%
    <h3 class="sectionHeader"><g:message code="user.assigned.group.label"/></h3>

    <div class="row">

        <g:if test="${usergroups}">
            <g:each in="${usergroups}" var="userGroupInstance" >
                <div class="row">
                    <div class="col-md-6">
                        <div class="col-md-${column2Width}">
                            ${userGroupInstance}
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

%{--Preferences--}%
    <h3 class="sectionHeader"><g:message code="app.label.preference"/></h3>

    <div class="row">
        <div class="col-md-6">
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.language"/></label></div>

                <div class="col-md-${column2Width}">${userInstance.preference.locale.displayName}</div>
            </div>
        </div>

        <div class="col-md-6">
            <div class="row">
                <div class="col-md-${column1Width}"><label><g:message code="app.label.timezone"/></label></div>

                <div class="col-md-${column2Width}">${userInstance.preference.timeZone}</div>
            </div>
        </div>
    </div>

%{--API Token--}%
    <h3 class="sectionHeader"><g:message code="app.label.api.token"/></h3>

    <div class="row">
        <div class="col-md-12">
            <input type="text" class="col-md-12" readonly value="${userInstance.apiToken}"/>
        </div>
    </div>

%{--Ownership--}%
    <h3 class="sectionHeader"><g:message code="app.ownership.label"/> / <g:message code="app.label.shared"/></h3>

    <div class="row">
        <div class="col-md-12">
            <g:render template="/ownership/index" model="[userInstance: userInstance, owned: owned, shared: shared]"/>
        </div>
    </div>



    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${userInstance}" var="theInstance"/>

</rx:container>
    </div>
</div>
</body>
</html>


