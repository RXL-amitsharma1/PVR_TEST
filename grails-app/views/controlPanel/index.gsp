<%@ page import="grails.util.Holders; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.ReportFieldService;" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'controlPanel.label')}"/>
    <g:set var="userService" bean="userService"/>
    <title><g:message code="app.controlPanel.title"/></title>
    <script>
        var downloadPdfUrl = "${createLink(controller: 'controlPanel', action: 'downloadPdfFromHtml')}";
        var medDraUsageCheckUrl = "${createLink(controller: 'controlPanel', action: 'medDraUsage')}";
        var medDraAllUsageCheckUrl = "${createLink(controller: 'controlPanel', action: 'medDraAllUsage')}";
        var medDraUpdateUrl = "${createLink(controller: 'controlPanel', action: 'updateMedDra')}";
        var importExcel = "${createLink(controller: 'controlPanel', action: 'importExcel')}";
        var queryLink = "${createLink(controller: 'query', action: 'view', absolute: true)}/";
        var templateLink = "${createLink(controller: 'template', action: 'view', absolute: true)}/";
        var adhocLink = "${createLink(controller: 'configuration', action: 'view', absolute: true)}/";
        var aggregateLink = "${createLink(controller: 'periodicReport', action: 'view', absolute: true)}/";
        var caseSeriesLink = "${createLink(controller: 'caseSeries', action: 'show', absolute: true)}/";
        var saveDefaultUiUrl = "${createLink(action:'saveDefaultUi')}";
        var removeDefaultUiUrl = "${createLink(action:'removeDefaultUi')}";
        var exportToExcelUrl = "${createLink(action:'exportToExcel')}";
        var getExportFileUrl = "${createLink(action:'getExportFile')}";
        var saveDmsSettingsUrl = "${createLink(action:'saveDmsSettings')}";
        var testDmsSettingsUrl = "${createLink(action:'testDmsSettings')}";
        var importQueriesJson = "${createLink(controller: 'controlPanel',action: 'importQueriesJson')}";
        var importTemplatesJson = "${createLink(controller: 'controlPanel',action: 'importTemplatesJson')}";
        var importDashboardsJson= "${createLink(controller: 'controlPanel',action: 'importDashboardsJson')}";
        var importConfigurationsJson = "${createLink(controller: 'controlPanel',action: 'importConfigurationsJson')}";
        var specificCllTemplateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateSetCLL', params: [templateTypeEnum: TemplateTypeEnum.CASE_LINE.name()])}";
        var tmpltDefaultReportFieldsOpts="${createLink(controller: 'template', action: 'userDefaultReportFieldsOpts', params: [lastModified: ViewHelper.getCacheLastModified(userService.getUser(),session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'])])}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var  pvadminURL = "${Holders.config.pvadmin.api.url}"
    </script>
    <asset:stylesheet src="controlPanel.css"/>
    <asset:javascript src="app/controlPanel.js"/>
    <asset:javascript src="vendorUi/tableexport/xlsx.core.min.js"/>
    <asset:javascript src="vendorUi/tableexport/FileSaver.js"/>
    <asset:javascript src="vendorUi/tableexport/tableexport.js"/>
    <asset:stylesheet src="select2-treeview.css" />
    <asset:stylesheet src="select2-treeview.css" />
    <asset:javascript src="vendorUi/select2/select2-treeview.js"/>
</head>

<body>
<div class="content">
    <div class="container">
        <div>
            <rx:container title="${message(code: "controlPanel.label")}">
                <g:if test="${inboundInitialConfiguration}">
                    <g:render template="/includes/layout/flashErrorsDivs" bean="${inboundInitialConfiguration}" var="theInstance"/>
                </g:if>
                <g:else>
                    <g:render template="/includes/layout/flashErrorsDivs" bean="${applicationSettingsInstance}" var="theInstance"/>
                </g:else>
                <div class="panel-group" id="accordion">
                <!-------------------------------------------------------accordian 1 has started-------------------------------------------------->
                            <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h4 class="panel-title">
                                            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapse1">
                                             <g:message code="controlPanel.ldap.label"/>
                                            </a>
                                        </h4>
                                    </div>
                                    <div id="collapse1" class="panel-collapse collapse in p-10">
                                        <div class="horizontalRuleFull"></div>
                                        <h4 class="m-t-0"><g:message code="controlPanel.ldap.mirror"/></h4>
                                        <div>
                                            <g:message code="controlPanel.ldap.info.message"/>
                                        </div>

                                        <small class="text-muted">
                                            <g:message code="controlPanel.ldap.done.automatically"/>
                                        </small>

                                        <div class="margin20Top m-t-5">
                                            <g:link elementId="mirrorLdapValues" class="btn btn-primary" action="mirrorLdapValues">
                                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                                <g:message code="controlPanel.ldap.mirror"/>
                                            </g:link>
                                        </div>
                                    </div>
                            </div>
                <!-------------------------------------------------------accordian 1 has closed---------------------------------------------------->

                <!--------------------------------------------------------accordian 2 has started-------------------------------------------------->
                            <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h4 class="panel-title">
                                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse2">
                                                <g:message code="controlPanel.reportField.label"/>
                                            </a>
                                        </h4>
                                    </div>
                                    <div id="collapse2" class="panel-collapse collapse p-10">
                                        <div class="horizontalRuleFull"></div>
                                        <small class="text-muted">
                                            <g:message code="controlPanel.reportField.info.message"/>
                                        </small>

                                        <div class="margin20Top">
                                            <g:link elementId="refreshCache" class="btn btn-primary m-t-5" action="refreshCaches">
                                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                                <g:message code="controlPanel.reportField.label"/>
                                            </g:link>
                                        </div>
                                    </div>
                            </div>

                <!-------------------------------------------------------accordian 2 has closed---------------------------------------------------->


                <!-------------------------------------------------------accordian 3 has started--------------------------------------------------->

                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse3">
                                <g:message code="controlPanel.delete.cached.report.files.label"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse3" class="panel-collapse collapse open p-10">
                        <div class="horizontalRuleFull"></div>
                        <small class="text-muted">
                            <g:message code="controlPanel.delete.cached.report.info.message"/>
                        </small>

                        <div class="margin20Top">
                            <g:link elementId="deleteReportFiles" class="btn btn-primary m-t-5" action="deleteReportFiles">
                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                <g:message code="controlPanel.delete.cached.report.files.label"/>
                            </g:link>
                        </div>
                    </div>
                </div>


                <!-------------------------------------------------------accordian 3 has closed----------------------------------------------------------->

                <!-------------------------------------------------------accordian 4 has started--------------------------------------------------->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse4">
                                <g:message code="controlPanel.encrypt.decrypt.reportData.label"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse4" class="panel-collapse collapse open p-10">
                        <div class="margin20Top">
                            <g:link elementId="encryptReportData" class="btn btn-primary" action="encryptAllReportData">
                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                <g:message code="controlPanel.encrypt.reportData.button"/>
                            </g:link> &nbsp;
                            <g:link elementId="decryptReportData" class="btn btn-danger" action="decryptAllReportData">
                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                <g:message code="controlPanel.decrypt.reportData.button"/>
                            </g:link>
                        </div>
                    </div>
                </div>
                <!-------------------------------------------------------accordian 4 has closed--------------------------------------------------------->

                <!-------------------------------------------------------accordian 5 has started--------------------------------------------------->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse5">
                                <g:message code="app.odataConfig.title"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse5" class="panel-collapse collapse open p-10">
                        <div class="margin20Top">
                            <g:link  class="btn btn-primary" action="odataSources">
                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                <g:message code="app.odataConfig.configure"/>
                            </g:link>
                        </div>
                    </div>
                </div>

                <!-------------------------------------------------------accordian 5 has closed---------------------------------------------------->

                <!-------------------------------------------------------accordian 6 has started--------------------------------------------------->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse6">
                                <g:message code="controlPanel.reset.autoROD.label"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse6" class="panel-collapse collapse open p-10">
                        <div class="margin20Top">
                            <g:link class="btn btn-primary" action="resetAutoRODJobIfInProgress">
                                <span class="glyphicon glyphicon-resize-horizontal"></span>
                                <g:message code="controlPanel.reset.label"/>
                            </g:link>
                        </div>
                    </div>
                </div>
                <!-------------------------------------------------------accordian 6 has closed---------------------------------------------------->

                <!-------------------------------------------------------accordian 7 has started--------------------------------------------------->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse7">
                                <g:message code="app.uiSettings.title.label"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse7" class="panel-collapse collapse open p-10">
                        <div class="margin20Top">
                            <div class="checkbox checkbox-primary">
                                <input type="checkbox"
                                       id="hideVersion" ${applicationSettingsInstance.defaultUiSettings ? "checked=\"checked\"" : ""}
                                       autocomplete="off">
                                <label for="hideVersion">
                                    <g:message code="app.uiSettings.checkbox.label"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
                <!-------------------------------------------------------accordian 7 has closed---------------------------------------------------->

                <!-------------------------------------------------------accordian 12 has started--------------------------------------------------->
                                <div class="panel panel-default">
                                    <div class="panel-heading">
                                        <h4 class="panel-title">
                                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse12">
                                                <g:message code="app.label.sender.initialization.configuration"/>
                                            </a>
                                        </h4>
                                    </div>
                                    <div id="collapse12" class="panel-collapse collapse open p-10">
                                        <div class="horizontalRuleFull"></div>
                                             <g:form name="inbound-initial-conf-form" id="inbound-initial-conf-form">
                                                <div class="row">
                                                    <input type="hidden" id="editable" value="true"/>
                                                    <input type="hidden" id="templateType" value="CASE_LINE"/>
                                                    <div class="margin20Top">
                                                        <div class="col-xs-3">
                                                            <label><g:message code="app.label.select.field"/><span class="required-indicator">*</span></label>
                                                            <select name="reportFieldId" id="reportField" class="form-control selectField">
                                                                <option default><g:message code="dataTabulation.select.field" /></option>
                                                                <g:each in="${fields}" var="group">
                                                                    <optgroup label="${g.message(code: "app.reportFieldGroup.${group.text}")}">
                                                                        <g:each in="${group.children}" var="field">
                                                                            <g:set var="sourceColumn" value="${field.getSourceColumn(selectedLocale)}"/>
                                                                            <option argusName="${sourceColumn?.tableName?.tableAlias}.${sourceColumn?.columnName}"
                                                                                    reportFieldName="${field.name}" templateCLLSelectable="${field.templateCLLSelectable}"
                                                                                    templateDTRowSelectable="${field.templateDTRowSelectable}" templateDTColumnSelectable="${field.templateDTColumnSelectable}" fieldGroup="${field.fieldGroup?.name}"
                                                                                    description='${message(code: ("app.reportField."+field.name+".label.description"), default: "")}'
                                                                                    value="${field.id}" ${inboundInitialConfiguration?.reportField?.name==field.name?"selected":""}><g:message code="app.reportField.${field.name}"/></option>
                                                                        </g:each>
                                                                    </optgroup>
                                                                </g:each>
                                                            </select>
                                                        </div>
                                                        <g:if test="${inboundInitialConfiguration && inboundInitialConfiguration.reportField}">
                                                            <div class="col-xs-3">
                                                                <label><g:message code="app.label.startDate"/></label>
                                                                <div class="fuelux">
                                                                    <div>
                                                                        <div class="datepicker toolbarInline" id="startDateDiv">
                                                                            <div class="input-group">
                                                                                <g:textField id="startDate" placeholder="${message(code:"placeholder.starteDate.label" )}" class="form-control fuelux needsActionItemRole" name="startDate" value="${renderShortFormattedDate(date: inboundInitialConfiguration?.startDate)}"/>
                                                                                <g:render id="startDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>


                                                            <div class="col-xs-2 m-t-25" style="margin-left: 10px;">
                                                                <div class="checkbox checkbox-primary">
                                                                    <g:checkBox id="caseDateLogic" checked="${inboundInitialConfiguration?inboundInitialConfiguration.caseDateLogicValue:1}" name="caseDateLogic"/>
                                                                    <label for="caseDateLogic">
                                                                        <g:message code="controlPanel.caseDateLogic.label"/>
                                                                    </label>
                                                                    <i data-toggle="popover" data-trigger="hover" data-container="body"
                                                                       data-content="<g:message code="controlPanel.inboundCompliance.caseDateLogic.help"/>" class="fa fa-info-circle p-l-5 font-20"></i>
                                                                </div>
                                                                <input type="hidden" id="caseDateLogicValue" name="caseDateLogicValue" value="${inboundInitialConfiguration?inboundInitialConfiguration.caseDateLogicValue:1}"/>
                                                            </div>

                                                        </g:if>

                                                        <div class="col-xs-3 m-t-20">
                                                            <g:if test="${inboundInitialConfiguration && inboundInitialConfiguration.reportField}">
                                                                <g:actionSubmit class="btn btn-primary" action="saveInboundIntialConf" value="${message(code: 'controlPanel.sender.initialize.label')}"></g:actionSubmit>
                                                            </g:if>
                                                            <g:else>
                                                                <g:actionSubmit class="btn btn-primary" action="saveInboundIntialConf" value="${message(code:'default.button.save.label')}"></g:actionSubmit>
                                                            </g:else>
                                                        </div>
                                                    </div>
                                                </div>
                                            </g:form>
                                    </div>
                                </div>
                <!-------------------------------------------------------accordian 12 has closed---------------------------------------------------->

                <!-------------------------------------------------------accordian 8 has started--------------------------------------------------->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse8">
                                <g:message code="controlPanel.exportToExcel.label"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse8" class="panel-collapse collapse open p-10">
                        <small class="text-muted">
                            <g:message code="controlPanel.exportToExcel.text"/>
                        </small>

                        <div class="margin20Top">
                            <div class="checkbox checkbox-primary">
                                <input type="checkbox" id="exportQced" checked autocomplete="off">
                                <label for="exportQced">
                                    <g:message code="controlPanel.exportToExcel.qcedOnly"/>
                                </label>
                            </div>
                            <button type="button" id="exportToExcel" class="btn btn-primary m-t-10">
                                <span class="glyphicon glyphicon-export"></span>
                                <g:message code="controlPanel.exportToExcel.button"/>
                            </button>

                            <div id="exportStatusLoading" style="display: none"><asset:image src="loading.gif" width="50px" height="50px"/></div>

                            <div id="exportStatus" style="display: none"></div>
                        </div>
                    </div>
                </div>
                <!-------------------------------------------------------accordian 8 has closed----------------------------------------------------------->

                <!-------------------------------------------------------accordian 9 has started---------------------------------------------------------->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse9">
                                <g:message code="controlPanel.medDra.update.label"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse9" class="panel-collapse collapse open p-10">
                        <small class="text-muted">
                            <g:message code="controlPanel.medDra.info.message"/>

                        </small><br>
                        <small class="text-danger">
                            <g:message code="controlPanel.medDra.info.warning"/>
                        </small>

                        <div class="margin20Top">

                            %{--<textarea name="changes" id="changes" rows="4" style="width: 50%" placeholder="controlPanel.medDra.placeholder"></textarea>--}%
                            <div class="row">
                                <div class="col-md-8">

                                    <div id="table" class="table-editable">

                                        <table class="table">
                                            <tr>
                                                <th style="width: 50px"><span class="table-add glyphicon glyphicon-plus"></span></th>
                                                <th style="width: 25%"><g:message code="controlPanel.medDra.level.label"/></th>
                                                <th style="width: 38%"><g:message code="controlPanel.medDra.oldValue.label"/></th>
                                                <th style="width: 38%"><g:message code="controlPanel.medDra.newValue.label"/></th>
                                                <th style="width: 80px;text-align: center"><g:message code="app.label.action"/></th>

                                            </tr>
                                            <tr>
                                                <td>
                                                    <span class="table-remove glyphicon glyphicon-remove"></span>
                                                </td>
                                                <td>
                                                    <select class="form-control" style="min-width: 80px">
                                                        <option value="SOC" data-id = "1"><g:message code="app.eventDictionary.soc"/></option>
                                                        <option value="HLGT" data-id = "2"><g:message code="app.eventDictionary.hlgt"/></option>
                                                        <option value="HLT" data-id = "3"><g:message code="app.eventDictionary.hlt"/></option>
                                                        <option value="PT" data-id = "4"><g:message code="app.eventDictionary.pt"/></option>
                                                        <option value="LLT" data-id = "5"><g:message code="app.eventDictionary.llt"/></option>
                                                        <option value="Synonyms" data-id = "6"><g:message code="app.eventDictionary.synonyms"/></option>
                                                        <option value="SMQ Broad" data-id = "7"><g:message code="app.eventDictionary.smqb"/></option>
                                                        <option value="SMQ Narrow" data-id = "8"><g:message code="app.eventDictionary.smqn"/></option>
                                                    </select>
                                                </td>
                                                <td contenteditable="true" class="editable" placeholder="?"></td>
                                                <td contenteditable="true" class="editable" placeholder="?"></td>
                                                <td><button type="button" class="btn btn-success btn-xs checkUsage"><g:message
                                                        code="controlPanel.medDra.checkUsage"/></button></td>

                                            </tr>
                                            <!-- This is our clonable table line -->
                                            <tr class="hide">
                                                <td>
                                                    <span class="table-remove glyphicon glyphicon-remove"></span>
                                                </td>

                                                <td>
                                                    <select class="form-control" style="min-width: 80px">
                                                        <option value="SOC" data-id = "1"><g:message code="app.eventDictionary.soc"/></option>
                                                        <option value="HLGT" data-id = "2"><g:message code="app.eventDictionary.hlgt"/></option>
                                                        <option value="HLT" data-id = "3"><g:message code="app.eventDictionary.hlt"/></option>
                                                        <option value="PT" data-id = "4"><g:message code="app.eventDictionary.pt"/></option>
                                                        <option value="LLT" data-id = "5"><g:message code="app.eventDictionary.llt"/></option>
                                                        <option value="Synonyms" data-id = "6"><g:message code="app.eventDictionary.synonyms"/></option>
                                                        <option value="SMQ Broad" data-id = "7"><g:message code="app.eventDictionary.smqb"/></option>
                                                        <option value="SMQ Narrow" data-id = "8"><g:message code="app.eventDictionary.smqn"/></option>
                                                    </select>
                                                </td>
                                                <td contenteditable="true" class="editable" placeholder="?"></td>
                                                <td contenteditable="true" class="editable" placeholder="?"></td>
                                                <td><button type="button" class="btn btn-success btn-xs checkUsage"><g:message
                                                        code="controlPanel.medDra.checkUsage"/></button></td>
                                            </tr>
                                        </table>
                                    </div>

                                    <div id="noDataInExcel" class="warning" style="display: none"></div>

                                    <div id="errorDiv" class="alert alert-danger" style="display: none"></div>

                                    <div class="input-group" style="width: 50%; float: left">
                                        <input type="text" class="form-control" id="file_name" readonly>
                                        <label class="input-group-btn">
                                            <span class="btn btn-primary browse-button">
                                                <g:message code="controlPanel.medDra.excel.label"/> <input type="file" id="file_input"
                                                                                                           accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                                                                                           style="display: none;">
                                            </span>
                                        </label>
                                    </div>
                                    <i data-toggle="popover" data-trigger="hover" data-container="body"
                                       data-content="<g:message code="controlPanel.medDra.excel.help"/> " class="fa fa-info-circle p-l-5 font-20"></i>
                                </div>

                            </div>
                            <br>
                            <button id="updateMedDra" class="btn btn-primary">
                                <span class="glyphicon glyphicon-retweet"></span>
                                <g:message code="controlPanel.medDra.update.label"/>
                            </button>
                            <button id="checkAllUsages" class="btn btn-primary checkAllUsages">
                                <span class="glyphicon glyphicon-question-sign"></span>
                                <g:message code="controlPanel.medDra.checkUsage"/>
                            </button>

                        </div>
                    </div>
                </div>

                <!-------------------------------------------------------accordian 9 has closed----------------------------------------------------->

                <!-------------------------------------------------------accordian 10 has started--------------------------------------------------->



                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse10">
                                <g:message code="controlPanel.upload.json.files"/>
                            </a>
                        </h4>
                    </div>
                    <div id="collapse10" class="panel-collapse collapse open p-10">
                        <div class="margin20Top">
                            <div id="successDivJsonUpload" style="display: none"></div>
                            <div id="errorDivJsonUpload" style="display: none"></div>

                            <div class="input-group" style="width: 50%; float: left">
                                <input type="text" class="form-control" id="queries_json" readonly>
                                <label class="input-group-btn">
                                    <span class="btn btn-primary browse-button">
                                        <g:message code="controlPanel.queries.json.load.file"/> <input type="file" id="queries_json_file_input"
                                                                                                       accept="application/json"
                                                                                                       style="display: none;">
                                    </span>
                                </label>
                            </div> <br>
                            <div style="margin-top: 30px;"></div>
                            <div class="input-group" style="width: 50%; float: left">
                                <input type="text" class="form-control" id="templates_json" readonly>
                                <label class="input-group-btn">
                                    <span class="btn btn-primary browse-button">
                                        <g:message code="controlPanel.templates.json.load.file"/> <input type="file"
                                                                                                         id="templates_json_file_input"
                                                                                                         accept="application/json"
                                                                                                         style="display: none;">
                                    </span>
                                </label>
                            </div> <br>
                            <div style="margin-top: 30px;"></div>

                            <div class="input-group" style="width: 50%; float: left">
                                <input type="text" class="form-control" id="configurations_json" readonly>
                                <label class="input-group-btn">
                                    <span class="btn btn-primary browse-button">
                                        <g:message code="controlPanel.configurations.json.load.file"/> <input type="file"
                                                                                                              id="configurations_json_file_input"
                                                                                                              accept="application/json"
                                                                                                              style="display: none;">
                                    </span>
                                </label>
                            </div><br>
                            <div style="margin-top: 30px;"></div>

                            <div class="input-group" style="width: 50%; float: left">
                                <input type="text" class="form-control" id="dashboards_json" readonly>
                                <label class="input-group-btn">
                                    <span class="btn btn-primary browse-button">
                                        <g:message code="controlPanel.dashboards.json.load.file"/> <input type="file"
                                                                                                          id="dashboards_json_file_input"
                                                                                                          accept="application/json"
                                                                                                          style="display: none;">
                                    </span>
                                </label>
                            </div>
                            <br>
                        </div>
                    </div>
                </div>

                <!-------------------------------------------------------accordian 10 has closed--------------------------------------------------------->

                <!-------------------------------------------------------accordian 11 has started--------------------------------------------------->

                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse11">
                <g:message code="app.dms.title.label"/>
                </a>
            </h4>
        </div>
        <div id="collapse11" class="panel-collapse collapse open p-10">
            <div class="margin20Top">
                <div class="margin20Top">
                    <div class="row">
                        <div class="col-md-4">
                            <g:textArea name="dmsSettings" class="dmsSettings" value="${applicationSettingsInstance.dmsIntegration}" style="resize: both;width: 100%;height: 270px;"/>
                        </div>
                        <div class="col-md-4">
                            <b><g:message code="example"/></b>
                            <pre style="height: 250px;">
                                ${defaultDmsSettings}
                            </pre>
                        </div>
                        <div class="col-md-4">

                        </div>
                    </div>
                    <div id="errorDmsDiv" style="display: none"></div>
                    <br>
                    <button id="saveDmsSettings" class="btn btn-primary saveDmsSettings" disabled=""><g:message code="app.save.button.label" /></button>
                    <button id="testDmsSettings" class="btn btn-primary testDmsSettings"><g:message code="app.label.dms.test" /></button>
                </div>



        </div>

        </div>

        </div>

  <!-------------------------------------------------------accordian 11 has closed--------------------------------------------------------->


  <!-------------------------------------------------------accordian 13 has Started--------------------------------------------------------->
         <g:if test="${Holders.config.grails.mail.oAuth.enabled}">
             <div class="panel panel-default">
                 <div class="panel-heading">
                     <h4 class="panel-title">
                         <a class="accordion-toggle collapsed" data-toggle="collapse"
                            data-parent="#accordion"
                            href="#collapse15">
                             <g:message code="app.oauth.header.title"/>
                         </a>
                     </h4>
                 </div>

                 <div id="collapse15" class="panel-collapse collapse open p-10">
                     <div class="margin20Top">

                         <div class="margin20Top">
                             <g:form controller="mailOAuth" action="sendTestMail">
                                 <div class="form-group row">
                                     <label for="email" class="col-md-1 col-form-label"><g:message
                                             code="app.label.email.email"/></label>

                                     <div class="col-md-2">
                                         <g:textField name="email" class="form-control"
                                                      value="${sec.loggedInUserInfo(field: "email")?.decodeHTML()}"/>
                                     </div>

                                     <div class="col-md-2">
                                         <g:submitButton class="btn btn-info" name="testEmail"
                                                         value="${message(code: 'mail.auth.test.email.btn.label')}"/> &nbsp;
                                         <g:link controller="mailOAuth" action="generate"
                                                 class="btn btn-primary"><g:message
                                                 code="mail.auth.generate.token.btn.label"/></g:link>
                                     </div>
                                 </div>
                             </g:form>
                         </div>

                     </div>
                 </div>
             </div>
            </g:if>
                     <!-- Commenting as not needed for now ---->
                     %{--<g:if test="${Holders.config.pvadmin.api.url}">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h4 class="panel-title">
                                    <a class="accordion-toggle collapsed" data-toggle="collapse"
                                       data-parent="#accordion"
                                       href="#collapse13">
                                        <g:message code="app.label.config.management"
                                                   default="Configuration Management"/>
                                    </a>
                                </h4>
                            </div>
                            <div id="collapse13" class="panel-collapse collapse open p-10">
                                <div class="margin20Top">
                                    <div>
                                        <g:link controller="configManagement" action="index" class="btn btn-primary">
                                            <span class="glyphicon glyphicon-resize-horizontal"></span>
                                            <g:message code="app.label.config.management"
                                                       default="Configuration Management"/>
                                        </g:link>
                                    </div>
                                </div>

                            </div>
                        </div>
                        <!-------------------------------------------------------accordian 13 has closed--------------------------------------------------------->

                    </g:if>--}%

                <!-------------------------------------------------------accordian 14 has Started--------------------------------------------------------->

                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion" href="#collapse14">
                                    <g:message code="controlPanel.user.addUsers"/>
                                </a>
                            </h4>
                        </div>
                        <div id="collapse14" class="panel-collapse collapse open p-10">
                            <g:form controller="controlPanel" action="addUsers" enctype="multipart/form-data">
                                <div class="input-group" style="width: 40%; float: left">
                                    <input type="text" class="form-control" id="excel-filename" readonly>
                                    <label class="input-group-btn">
                                        <span class="btn btn-primary">
                                            <g:message code="controlPanel.user.addUsers"/>
                                            <input type="file"  name="excelFile" accept=".xlsx" style="display: none;">
                                        </span>
                                    </label>
                                </div>
                                <br>
                                <div>
                                    <br>
                                    <g:submitButton class='btn btn-primary' name="uploadbutton" id="uploadButton" value="Upload"/>
                                    <g:actionSubmit class="btn btn-primary" value="Download User Template" action="downloadUserTemplate"/>
                                    <g:actionSubmit class="btn btn-primary" value="Download Users In Applicaiton" action="downloadAllUsers"/>
                                </div>
                            </g:form>
                            <hr>
                        </div>
                    </div>
                    <!-------------------------------------------------------accordian 14 has Closed--------------------------------------------------------->
                </div>
                <div class="margin20Top text-muted" style="text-align: right">
                    <rx:pageInfo timeZone="${session."user.preference.timeZone"}"/>
                </div>
            </rx:container>

            <g:render template="/includes/widgets/spinner"
                      model="[id: 'spinnerMessage', message: message(code: 'app.label.performingOperation')]"/>
            <g:render template="/includes/widgets/infoTemplate"
                      model="${[messageBody: message(code: 'app.uiSettings.success.label')]}"/>

            <div class="modal fade" tabindex="-1" id="usageModal" role="dialog">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                    aria-hidden="true">&times;</span></button>
                            <h4 class="modal-title"><g:message code="app.label.usage"/></h4>
                        </div>

                        <div class="modal-body" id="usageContent">

                        </div>

                        <div class="modal-footer">
                            <input type="hidden" id="jsondata">
                            <button type="button" class="btn btn-default toExcel"><g:message code="save.as.excel"/></button>
                            <button type="button" class="btn btn-default toPdf"><g:message code="save.as.pdf"/></button>
                            <button type="button" class="btn btn-default" data-dismiss="modal"><g:message
                                    code="app.button.close"/></button>
                        </div>
                    </div>
                </div>
            </div>

            <div id="editor"></div>

            <div id="tableWrap" style="display: none">
                <table id="export">
                    <tr><td></td><td></td></tr>
                </table>
            </div>

        </div>
    </div>
</div>
</body>
</html>
