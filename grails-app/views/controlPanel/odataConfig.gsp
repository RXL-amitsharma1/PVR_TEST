<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'controlPanel.label')}"/>
    <title><g:message code="app.odataConfig.title"/></title>
    <script>
        var getDsTableFields = "${createLink(action:'getDsTableFields')}";
    </script>
    <asset:stylesheet src="controlPanel.css"/>
    <asset:javascript src="app/controlPanel.js"/>
</head>

<body>

<rx:container title="${message(code: "controlPanel.label")}">

    <g:render template="/includes/layout/flashErrorsDivs" bean="${dbSource}" var="theInstance"/>

    <h3><g:message code="app.odataConfig.title"/></h3>

    <div class="horizontalRuleFull"></div>

    <div class="margin20Top">
    <g:form action="saveOdataConfig" >

        <div class="row form-group">
            <div class="col-lg-6">
                <label for="dsName"><g:message code="app.label.odataSource.name" /><span class="required-indicator">*</span></label>
                <g:textField name="dsName" value="${dbSource?.dsName}"  class="form-control "/>
            </div>
        </div>
        <div class="row form-group">
            <div class="col-lg-6">
                <label for="dsUrl"><g:message code="app.label.odataSource.url" /><span class="required-indicator">*</span></label>
                <g:textField name="dsUrl" value="${dbSource?.dsUrl}" placeholder="HOST:PORT/SID"  class="form-control "/>
            </div>
        </div>
        <div class="row form-group">
            <div class="col-lg-6">
                <label for="dsLogin"><g:message code="app.label.odataSource.login" /><span class="required-indicator">*</span></label>
                <g:textField name="dsLogin" value="${dbSource?.dsLogin}"  class="form-control "/>
            </div>
        </div>
        <div class="row form-group">
            <div class="col-lg-6">
                <label for="dsPassword">
                    <g:if test="${dbSource?.id > 0}">
                        <g:message code="app.label.odataSource.password"/>
                    </g:if>
                    <g:else>
                        <g:message code="user.password.label"/><span class="required-indicator">*</span>
                    </g:else>
                </label>
                <g:passwordField name="dsPassword" value=""  class="form-control "/>
            </div>
        </div>
        <input name="id" id="id" type="hidden" value="${dbSource?.id}">
        <input name="settings" id="settings" type="hidden" value="${dbSource?.settings?:"{}"}">
        <g:if test="${dbSource?.id >0 }">
        <div class="row">
            <div class="col-md-8">

                <div id="pvatable" class="table-editable">

                    <table class="table">
                        <tr class="pvaheader">
                            <th style="width: 50px"><span class="pvatable-add glyphicon glyphicon-plus"></span></th>
                            <th style="width: 200px"><g:message code="app.odataConfig.entity.name"/></th>
                            <th style="width: 38%"><g:message code="app.odataConfig.table"/></th>
                            <th style="width: 38%"><g:message code="app.odataConfig.description"/></th>
                            <th style="width: 80px;text-align: center"><g:message code="app.label.action"/></th>

                        </tr>
                        <tr>
                            <td>
                                <span class="pvatable-remove glyphicon glyphicon-remove"></span>
                            </td>
                            <td></td>
                            <td ></td>
                            <td ></td>
                            <td><button type="button" class="btn btn-success btn-xs editPvaTable"><g:message code="app.edit.button.label"/></button></td>

                        </tr>
                        <!-- This is our clonable table line -->
                        <tr class="hide">
                        <td>
                            <span class="pvatable-remove glyphicon glyphicon-remove"></span>
                        </td>
                        <td></td>
                        <td ></td>
                        <td ></td>
                        <td><button type="button" class="btn btn-success btn-xs editPvaTable"><g:message code="app.edit.button.label"/></button></td>
                    </tr>
                    </table>
                </div>

                <div id="successDiv" class="success" style="display: none"><g:message code="app.odataConfig.success"/></div>

                <div id="errorDiv" class="alert alert-danger" style="display: none"></div>


            </div>

        </div>
        <br>
        </g:if>
        <button id="updatePvaRest" class="btn btn-primary">
            <g:message code="${dbSource?.id?'app.update.button.label':'app.save.button.label'}"/>
        </button>
        <a href="${createLink(action:'odataSources')}" class="btn btn-primary">
            <g:message code="app.label.odataSource.back"/>
        </a>

    </g:form>
    </div>

    <div class="margin20Top text-muted" style="text-align: right">
        <rx:pageInfo timeZone="${session."user.preference.timeZone"}"/>
    </div>
</rx:container>

<g:render template="/includes/widgets/spinner"
          model="[id: 'spinnerMessage', message: message(code: 'app.label.performingOperation')]"/>
<g:render template="/includes/widgets/infoTemplate"
          model="${[messageBody: message(code: 'app.uiSettings.success.label')]}"/>

<div class="modal fade" tabindex="-1" id="pvaEntityModal" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.odataConfig.entity.config"/></h4>
            </div>

            <div class="modal-body" id="usageContent">
                <div class="form-group">
                    <label for="pvaTableName"><g:message code="app.odataConfig.table"/></label>
                    <select class="form-control" id="pvaTableName" >
                        <g:each var="table" in="${pvaTables}">
                            <option value="${table}">${table}</option>
                        </g:each>
                    </select>
                </div>
                <div class="form-group">
                    <label for="pvaEntityName"><g:message code="app.odataConfig.entity.name"/></label>
                    <input class="form-control" id="pvaEntityName" >
                </div>
                <div class="form-group">
                    <label for="pvaDescription"><g:message code="app.odataConfig.description"/></label>
                    <input class="form-control" id="pvaDescription" >
                </div>
                <div class="form-group">
                    <label for="limitQuery"><g:message code="app.odataConfig.limit.query"/></label>
                    <input class="form-control" id="limitQuery" >
                </div>

                <div class="form-group">
                    <label for="fields"><g:message code="app.odataConfig.operations"/></label>
                    <div class="row">
                        <div class="col-lg-3"><div class="checkbox checkbox-primary">
                            <input type="checkbox" id="readEntity" checked disabled=autocomplete="off">
                            <label for="readEntity"><g:message code="app.odataConfig.read"/></label>
                        </div></div>
                        <div class="col-lg-3">
                            <div class="checkbox checkbox-primary">
                                <input type="checkbox" id="createEntity" autocomplete="off">
                                <label for="createEntity"><g:message code="app.odataConfig.create"/></label>
                            </div>
                        </div>
                        <div class="col-lg-3">
                            <div class="checkbox checkbox-primary">
                                <input type="checkbox" id="updateEntity" autocomplete="off">
                                <label for="updateEntity"><g:message code="app.odataConfig.update"/></label>
                            </div>
                        </div>
                        <div class="col-lg-3">
                            <div class="checkbox checkbox-primary">
                                <input type="checkbox" id="deleteEntity" autocomplete="off">
                                <label for="deleteEntity"><g:message code="app.odataConfig.delete"/></label>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="fields"><g:message code="app.odataConfig.fields"/></label>
                    <textarea class="form-control" rows="10" id="fields"></textarea>
                </div>
                <div class="form-group">
                    <button type="button" id="getDsTableFields" class="btn btn-default "><g:message code="app.odataConfig.genarate"/></button>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey updateEntity"><g:message code="default.button.save.label"/></button>
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="app.button.close"/></button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
