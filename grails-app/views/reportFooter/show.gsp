<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: "app.label.reportFooter.appName")}"/>
    <title><g:message code="default.show.title" args="[entityName]"/></title>
</head>

<body>

<rx:container title="${message(code: 'app.label.viewReportFooter')}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${reportFooterInstance}" var="theInstance"/>

    <div class="container-fluid">
        <div class="row">
            <div class="col-xs-12">
                <div class="row">
                    <div class="col-xs-6">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.reportFooter.footer"/></label>

                                <div>${reportFooterInstance.footer}</div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.reportRequestType.description"/></label>

                                <div>${reportFooterInstance.description}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div class="pull-right">
                    <g:link controller="reportFooter" action="edit" id="${reportFooterInstance.id}"
                            class="btn btn-primary"><g:message
                            code="default.button.edit.label"/></g:link>
                    <g:link url="#" data-toggle="modal" data-target="#deleteModal" data-instancetype="reportFooter"
                            data-instanceid="${reportFooterInstance.id}"
                            data-instancename="${reportFooterInstance.footer}"
                            class="btn pv-btn-grey"><g:message code="default.button.delete.label"/></g:link>
                </div>
            </div> 
        </div>
    </div>
    <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${reportFooterInstance}"
              var="theInstance"/>

    <g:form controller="${controller}" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>

</rx:container>

</body>
</html>