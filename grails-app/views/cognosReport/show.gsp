<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'cognosReport.label')}"/>
    <title><g:message code="cognosReport.label"/></title>
</head>

<body>
<div class="content">
    <div class="container">
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<div class="col-md-12 p-t-10">
    <rx:container title="${message(code: "app.label.cognosReport")}">
        <div class="row breadcrumbs">
                  <div class="col-md-6">
        <g:link action="index"><< Cognos Report List</g:link>
        </div>
        </div>
        <p/>

        <g:render template="/includes/layout/flashErrorsDivs" bean="${cognosReportInstance}" var="theInstance"/>

        <sec:ifAnyGranted roles="ROLE_COGNOS_CRUD">
            <g:render template="/includes/widgets/buttonBarCRUD" bean="${cognosReportInstance}" var="theInstance"
                      model="[whatIsBeingDeleted: cognosReportInstance.name]"/>
        </sec:ifAnyGranted>

        <div class="row">
            <div class="col-md-12">

                <h3 class="sectionHeader">Cognos Report Details</h3>

                <div class="col-md-12">

                    <div class="row">
                        <div class="col-md-${column1Width}"><label><g:message code="cognosReport.name.label"/></label></div>

                        <div class="col-md-${column2Width}">${cognosReportInstance.name}</div>
                    </div>

                    <div class="row">
                        <div class="col-md-${column1Width}"><label><g:message code="cognosReport.url.label"/></label></div>

                        <g:if test="${grailsApplication.config.cognosReport.view.enabled}">
                            <div class="col-md-${column2Width}"><g:link url="${cognosReportInstance?.url?:'/'}"
                                                                        target="_blank">View Report</g:link></div>
                        </g:if>
                    </div>

                    <div class="row">
                        <div class="col-md-${column1Width}"><label><g:message
                                code="cognosReport.description.label"/></label></div>

                        <div class="forceLineWrap col-md-${column2Width}">${cognosReportInstance?.description}</div>
                    </div>
                </div>
            </div>
        </div>


        <g:render template="/includes/widgets/dateCreatedLastUpdated" bean="${cognosReportInstance}" var="theInstance"/>

    </rx:container>
</div>

    </div>
</div>
</body>
</html>
