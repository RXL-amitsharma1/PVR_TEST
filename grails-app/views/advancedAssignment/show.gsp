<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="Advanced Assignment"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
    <asset:javascript src="app/advancedAssignment.js"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5 class="page-header-settings"><g:message code="default.show.label" args="[entityName]"/></h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div  class="settings-content">

<rx:container title="${message(code: 'app.label.advanced.assignment.appName')}">

        <g:render template="includes/form" model="[mode: 'show', advancedAssignmentInstance: advancedAssignmentInstance,
                                                   ownerUsername: ownerUsername, ownerUserId: ownerUserId]"/>

<div class="row">
    <div class="col-md-12">
        <div class="buttonBar">
            <div class="pull-right">
                <g:link controller="advancedAssignment" action="edit" id="${advancedAssignmentInstance.id}"
                        class="btn btn-primary"><g:message code="default.button.edit.label"/></g:link>
                <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["advancedAssignment", "index"]}'
                        id="cancelButton">${message(code: "default.button.cancel.label")}</button>
            </div>
        </div>
    </div>
</div>


</rx:container>
        </div>
    </div>
</div>

</body>
</html>