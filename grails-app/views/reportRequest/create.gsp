<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<head>
    <g:javascript>
        var extensionCheck = "${grailsApplication.config.reportRequest.fileType}";
        var findTaskUrl = "${createLink(controller: 'reportRequest', action: 'findTasks')}";
        var saveUrl = "${createLink(controller: "reportRequest", action: "save")}?${_csrf.parameterName}=${_csrf.token}"
        var reportRequestDropdownURL = "${createLink(controller: "reportRequestRest", action: "reportRequestDropdownList")}"
        var reportingDestinationsUrl="${createLink(controller: 'queryRest', action: 'getReportingDestinations')}";
        var reportRedirectURL = "${createLink(controller: 'report', action: 'showFirstSection')}";
        var configurationRedirectURL = "${createLink(controller: 'configurationRest', action: 'redirectViewConfiguration')}";
        var configurationsListUrl = "${createLink(controller: 'configurationRest', action: 'getConfigurationsList')}";
        var reportsListUrl = "${createLink(controller: 'reportResultRest', action: 'getReportsList')}";
        var reportRequestURL = "${createLink(controller: 'reportRequest', action: 'show')}";
        var addDictionaryValueUrl = "${createLink(controller: 'configurationRest', action: 'addUserDictionaryValue')}";
        var publisherContributorsUrl = "${createLink(controller: 'userRest', action: 'getPublisherContributors')}";
        var userValuesUrl = "${createLink(controller: 'userRest', action: 'userListValue')}";
        var AttachmentSizeLimit = ${grailsApplication.config.grails.controllers.attachment.maxFilSize};
    </g:javascript>

    <style>
        .form-horizontal .form-group {
            margin-left: 0;
            margin-right: 0;
        }
    </style>

    <meta name="layout" content="main">

    <asset:javascript src="app/reportRequest.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js" />
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>

    <asset:javascript src="app/configuration/configurationCommon.js"/>
    <asset:javascript src="app/configuration/dateRange.js"/>
    <asset:javascript src="app/disableAutocomplete.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>

    <asset:javascript src="app/reportRequestActionItems.js"/>
    <asset:javascript src="app/actionItem/actionItemModal.js"/>

    <g:set var="entityName" value="Request Report" />
    <title><g:message code="app.task.reportRequest.create.title"/></title>

    <script>

        var viewActionItemUrl = "${createLink(controller: "actionItem", action: "view")}"

    </script>
</head>

<body>
<div class="content ">
    <div class="container ">
        <div>
            <div class="col-sm-12">
                <div class="page-title-box">
                    <div class="fixed-page-head">
                        <div class="page-head-lt">
                            <div class="col-md-12">
                                <h5>${message(code: "app.report.request.title")} </h5>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="mt-30">


<g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestInstance}" var="theInstance"/>
            <div class="alert alert-success alert-dismissible reportRequestAlert m-t-4" role="alert" hidden="hidden">
                <button type="button" class="close">
                    <span aria-hidden="true">&times;</span>
                    <span class="sr-only"><g:message code="default.button.close.label"/></span>
                </button>
                <div id="successMessage"></div>
            </div>

<g:form method="POST" enctype="multipart/form-data" name="configurationForm" class="form-horizontal" autocomplete="off" data-evt-sbt='{"method": "onFormSubmit", "params": []}'>

    <g:render template="includes/form"
              model="['mode': 'create', users: users, actionItems  : actionItems, comments: comments, reportRequestInstance: reportRequestInstance, taskTemplates: taskTemplates, reportRequestTypes: reportRequestTypes]"/>

    <div class="buttonBar m-b-10" style="text-align: right">
        <button type="button" class="btn btn-primary update_button"> ${message(code: 'default.button.save.label')}</button>
        <button type="button" class="btn pv-btn-grey" data-evt-clk='{"method": "goToUrl", "params": ["reportRequest", "index"]}'
                id="cancelButton">${message(code: "default.button.cancel.label")}</button>
    </div>
</g:form>
        </div>
    </div>
</div>
<g:render template="includes/reportRequestComment"/>
<g:render template="/actionItem/includes/actionItemModal"/>
<g:render template="/includes/widgets/warningTemplate"/>
</body>
</html>