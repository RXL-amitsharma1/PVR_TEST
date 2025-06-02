<%@ page import="com.rxlogix.user.UserRole" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'role.label')}"/>
    <title><g:message code="app.roleManagement.title"/></title>


</head>

<body>
<div class="content">
    <div class="container ">
        <div>
            <rx:container title="${message(code: 'app.label.roleManagement')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${roleInstance}" var="theInstance"/>

    <div class="curvedBox pv-caselist">
        <table class="table table-striped table-curved table-hover pv-list-table dataTable no-footer" >
            <thead>
            <tr>
                <g:sortableColumn property="authority" title="${message(code: 'role.authority.label')}"/>
                <g:sortableColumn property="description" title="${message(code: 'role.description.label')}"/>
                <th>${message(code: 'role.numberOfUsers.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:if test="${roleInstanceTotal > 0}">
                <g:each in="${roleInstanceList.sort()}" status="i" var="roleInstance">
                    <tr>
                        <td><g:link action="show" id="${roleInstance.id}"><g:message code="app.role.${roleInstance.authority}" default="${roleInstance.authority}"/></g:link></td>
                        <td>${roleInstance.i18nDescription}</td>
                        <td>${UserRole.countByRole(roleInstance)}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="2">None</td>
                </tr>
            </g:else>
            </tbody>
        </table>
    </div>

    <g:render template="/includes/widgets/pagination" bean="${roleInstanceTotal}" var="theInstanceTotal"/>

</rx:container>
        </div>
    </div>
</div>
</body>
</html>
