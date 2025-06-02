<%@ page import="com.rxlogix.config.UserDictionary" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.reportRequestType.title"/></title>
    <asset:javascript src="app/reportRequestTypeList.js"/>
    <asset:javascript src="app/reportRequestType.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
%{--    <asset:stylesheet src="user-group.css"/>--}%
<style>
.dt-layout-row:first-child {
    padding-right:0px;
    margin-top:15px;
}
.dt-layout-row:last-child {
    margin-top:10px;
}
</style>
</head>
<body>
<div class="content">
    <div class="container ">
        <div>
            <rx:container title="${message(code:"app.label.reportRequest.settings")}">
    <div class="body">
        <div id="action-list-conainter" class="list pv-caselist">

            <g:render template="/includes/layout/flashErrorsDivs" bean="${reportRequestTypeInstance}" var="theInstance"/>
            <!-- Nav tabs -->
            <ul class="nav nav-tabs" role="tablist">
                <li role="presentation" class="${(!params.type || params.type == 'type') ? 'active' : ''}"><a href="#typeTab" aria-controls="typeTab" role="tab" data-toggle="tab"><g:message code="app.label.reportRequestType.appName"/></a>
                </li>
                <li role="presentation" class="${params.type == 'priority' ? 'active' : ''}"><a href="#priority" aria-controls="priority" role="tab" data-toggle="tab"><g:message code="app.label.reportRequestPriority.appName"/></a>
                </li>
                <li role="presentation" class="${params.type == 'link' ? 'active' : ''}"><a href="#link" aria-controls="link" role="tab" data-toggle="tab"><g:message code="app.label.reportRequestLinkType.appName"/></a>
                </li>
                <li role="presentation" class="${params.type == 'field' ? 'active' : ''}"><a href="#field" aria-controls="field" role="tab" data-toggle="tab"><g:message code="app.label.reportRequestField.appName"/></a>
                </li>
                <li role="presentation" class="${params.type == UserDictionary.UserDictionaryType.PSR_TYPE_FILE.name() ? 'active' : ''}"><a href="#psrTypeFile" aria-controls="psrTypeFile" role="tab" data-toggle="tab"><g:message code="app.label.reportRequest.psrTypeFile"/></a>
                </li>
                <li role="presentation" class="${params.type == UserDictionary.UserDictionaryType.INN.name() ? 'active' : ''}"><a href="#inn" aria-controls="inn" role="tab" data-toggle="tab"><g:message code="app.label.reportRequest.inn"/></a>
                </li>
                <li role="presentation" class="${params.type == UserDictionary.UserDictionaryType.DRUG.name() ? 'active' : ''}"><a href="#drugCode" aria-controls="drugCode" role="tab" data-toggle="tab"><g:message code="app.label.reportRequest.drugCode"/></a>
                </li>
            </ul>

            <!-- Tab panes -->
            <div class="tab-content">
                <div role="tabpanel" class="tab-pane ${(!params.type || params.type == 'type') ? 'active' : ''}" id="typeTab">
                    <div class="navScaffold">
                        <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=type">
                            <span class="glyphicon glyphicon-plus icon-white"></span>
                            <g:message code="default.button.create.label"/>
                        </a>
                    </div>

            <div class="pv-caselist">
                <table id="reportRequestTypeList" class="table table-striped pv-list-table dataTable no-footer"  style="width: 100%">
                    <thead>
                    <tr>
                        <th></th>
                        <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                        <th><g:message default="Description" code="app.label.reportRequestType.description"/></th>
                        <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                        <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                        <th><g:message default="Action" code="app.label.action"/></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>

                <div role="tabpanel" class="tab-pane ${params.type == 'priority' ? 'active' : ''}" id="priority">

            <div id="priorityContent" class="pv-caselist">
            <div class="navScaffold">
                <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=priority" >
                    <span class="glyphicon glyphicon-plus icon-white"></span>
                    <g:message code="default.button.create.label" />
                </a>
            </div>

            <table id="reportRequestPriorityList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                <thead>
                <tr>
                    <th></th>
                    <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                    <th><g:message default="Description" code="app.label.reportRequestType.description"/></th>
                    <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                    <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                    <th><g:message default="Action" code="app.label.action"/></th>
                </tr>
                </thead>
            </table>
        </div>
        </div>
        <div role="tabpanel" class="tab-pane ${params.type=='link'?'active':''}" id="link">

                <div id="linkContent" class="pv-caselist">
                    <div class="navScaffold">
                        <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=link" >
                            <span class="glyphicon glyphicon-plus icon-white"></span>
                            <g:message code="default.button.create.label" />
                        </a>
                    </div>

                        <table id="reportRequestLinkList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                                <th><g:message default="Description" code="app.label.reportRequestType.description"/></th>
                                <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                                <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                                <th><g:message default="Action" code="app.label.action"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane ${params.type == 'field' ? 'active' : ''}" id="field">

                    <div id="fieldContent">
                        <div class="navScaffold">
                            <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=field">
                                <span class="glyphicon glyphicon-plus icon-white"></span>
                                <g:message code="default.button.create.label"/>
                            </a>
                        </div>

                        <table id="reportRequestFieldList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                                <th><g:message default="Label" code="app.label.reportRequestField.label"/></th>
                                <th><g:message default="Field Type" code="app.label.reportRequestField.index"/></th>
                                <th><g:message default="For Report Request Type" code="app.label.reportRequestField.type"/></th>
                                <th><g:message default="Section" code="app.label.reportRequestField.section"/></th>
                                <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                                <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                                <th><g:message default="Action" code="app.label.action"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>

                <div role="tabpanel" class="tab-pane ${params.type == 'INN' ? 'active' : ''}" id="inn">

                    <div id="innContent">
                        <div class="navScaffold">
                            <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=${UserDictionary.UserDictionaryType.INN.name()}">
                                <span class="glyphicon glyphicon-plus icon-white"></span>
                                <g:message code="default.button.create.label"/>
                            </a>
                        </div>

                        <table id="innList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                                <th><g:message default="Description" code="app.label.reportRequestType.description"/></th>
                                <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                                <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                                <th><g:message default="Action" code="app.label.action"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane ${params.type == 'PSR_TYPE_FILE' ? 'active' : ''}" id="psrTypeFile">

                    <div id="psrTypeFileContent">
                        <div class="navScaffold">
                            <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=${UserDictionary.UserDictionaryType.PSR_TYPE_FILE.name()}">
                                <span class="glyphicon glyphicon-plus icon-white"></span>
                                <g:message code="default.button.create.label"/>
                            </a>
                        </div>

                        <table id="psrTypeFileList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                                <th><g:message default="Description" code="app.label.reportRequestType.description"/></th>
                                <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                                <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                                <th><g:message default="Action" code="app.label.action"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
                <div role="tabpanel" class="tab-pane ${params.type == 'DRUG' ? 'active' : ''}" id="drugCode">

                    <div id="drugCodeContent">
                        <div class="navScaffold">
                            <a class="btn btn-primary" href="${createLink(controller: 'reportRequestType', action: 'create')}?type=${UserDictionary.UserDictionaryType.DRUG.name()}">
                                <span class="glyphicon glyphicon-plus icon-white"></span>
                                <g:message code="default.button.create.label"/>
                            </a>
                        </div>

                        <table id="drugCodeList" class="table table-striped pv-list-table dataTable no-footer" style="width: 100%">
                            <thead>
                            <tr>
                                <th></th>
                                <th><g:message default="Name" code="app.label.reportRequestType.name"/></th>
                                <th><g:message default="Description" code="app.label.reportRequestType.description"/></th>
                                <th><g:message default="Last Updated" code="app.label.reportRequestType.lastUpdated"/></th>
                                <th><g:message default="Modified By" code="app.label.reportRequestType.modifiedBy"/></th>
                                <th><g:message default="Action" code="app.label.action"/></th>
                            </tr>
                            </thead>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</rx:container>
<g:form controller="${controller}" method="delete">
    <input type="hidden" name="type" id="type">
    <g:render template="/includes/widgets/deleteRecord"/>
</g:form>
</body>
</html>