<%@ page import="com.rxlogix.Constants; com.rxlogix.ExcelExportService; com.rxlogix.config.DataTabulationTemplate;com.rxlogix.enums.ReasonOfDelayAppEnum;  groovy.json.JsonOutput;com.rxlogix.config.TemplateQuery; com.rxlogix.config.ExecutedTemplateQuery; com.rxlogix.user.UserGroup; com.rxlogix.config.Query; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.config.Configuration; com.rxlogix.config.TemplateQuery; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.config.ReportTemplate; com.rxlogix.config.ReportFooter; org.springframework.context.MessageSource; com.rxlogix.util.ViewHelper;" %>
<g:set var="icsrScheduleService" bean="icsrScheduleService"/>
<g:set var="configurationService" bean="configurationService"/>

<div id="templateQuery${i}" class="templateQuery-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name="templateQueries[${i}].version" value="${templateQueryInstance?.version}"/>
    <g:hiddenField name='templateQueries[${i}].id' value="${templateQueryInstance?.id}"/>
    <g:hiddenField name='templateQueries[${i}].dynamicFormEntryDeleted' value='false'/>
    <g:hiddenField name='templateQueries[${i}].new' value="${templateQueryInstance?.id == null ? 'true' : 'false'}"/>
    <g:if test="${templateQueryIndex}">
        <g:hiddenField class="templateQueryFieldToUpdate" name="templateQueryFieldToUpdate" type-name='${templateQueryIndex.type}' value="${templateQueryIndex.index}"/>
    </g:if>
    <div style="position: relative; height: 0">
        <g:if test="${!params.fromTemplate && !qbeForm}">
            <div>
                <span class="btn-group " style="position: absolute;top: -7px;right: -7px;border-bottom: 1px solid #ccc;height: 20px;border-left: 1px solid #ccc; z-index: 10;   box-shadow: -1px 1px 3px #ccc;">
                    <i class="fa fa-arrows add-cursor grab-icon" title="${message(code: 'tip.dragSection')}" style="font-size: 12px;margin-left: 5px;"></i>
                    <i class="fa fa-times add-cursor  templateQueryDeleteButton" style="font-size: 14px;margin-left: 9px;margin-right: 5px;" id="templateQueries[${i}].deleteButton" data-toggle="tooltips" title="${message(code: 'tip.removeSection')}" data-id="${i}"></i>
                </span>
            </div>
        </g:if>
        <span class="btn-group " style="top: -7px;left: -7px;border-bottom: 1px solid #ccc;border-right: 1px solid #ccc;  z-index: 10;  box-shadow: 1px 1px 3px #ccc;min-width: 15px;">
            <span class="sectionCounterNumber" style="margin-left: 2px;margin-right: 2px;"></span>
        </span>
    </div>
    <div class="row templateContainer" id="templateQueries">

        %{--Template--}%
        <div class="col-md-4 templateWrapperRow">
            <div class="row">
                <div class="col-md-12"> %{--To Keep Format--}%
                    <div class="row">
                        <div class="col-md-12" style=" margin-left: 20px;">
                            <label><g:message code="app.label.chooseAReportTemplate"/><span class="required-indicator">*</span></label>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-11">
                            <div style="height: 40px">
                                <g:select hidden="hidden" from="${[]}" name="templateQueries[${i}].template" readonly="${params.fromTemplate}"
                                               data-value="${templateQueryInstance?.template?.id}"
                                               class="form-control selectTemplate"/>

                            </div>
                        </div>
                        <div><a href="${templateQueryInstance?.template?.id ?createLink(controller: 'template' , action: 'view', id: templateQueryInstance?.template?.id):'#'}"
                                title="${message(code: 'app.label.viewTemplate')}" target="_blank" class="pv-ic templateQueryIcon templateViewButton glyphicon glyphicon-info-sign ${templateQueryInstance?.template?.id ? '' : 'hide'}"></a>

                            <div class="btn-group newTemplateDiv ${templateQueryInstance?.template?.id ? 'hide' : ''}">
                                <span class="glyphicon glyphicon-plus-sign dropdown-toggle newTemplate" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"></span>
                                <ul class="dropdown-menu">
                                    <g:if test="${isForIcsrReport || isForIcsrProfile}">
                                        <li><a data-url='${createLink(action: 'createTemplate')}?templateType=${TemplateTypeEnum.ICSR_XML.key}'
                                               data-message='${message(code: "app.template.query.create.warning")}'
                                               class="createTemplateQueryButton">${message(code: "app.templateType.XML")}</a>
                                        </li>
                                    </g:if>
                                    <g:else>
                                        <li><a data-url='${createLink(action: 'createTemplate')}?templateType=${TemplateTypeEnum.CASE_LINE.key}'
                                               data-message='${message(code: "app.template.query.create.warning")}'
                                               class="createTemplateQueryButton">${message(code: "app.templateType.CASE_LINE")}</a>
                                        </li>
                                        <li><a data-url='${createLink(action: 'createTemplate')}?templateType=${TemplateTypeEnum.DATA_TAB.key}'
                                               data-message='${message(code: "app.template.query.create.warning")}'
                                               class="createTemplateQueryButton">${message(code: "app.templateType.DATA_TAB")}</a>
                                        </li>
                                        <li><a data-url='${createLink(action: 'createTemplate')}?templateType=${TemplateTypeEnum.CUSTOM_SQL.key}'
                                               data-message='${message(code: "app.template.query.create.warning")}'
                                               class="createTemplateQueryButton">${message(code: "app.templateType.CUSTOM_SQL")}</a>
                                        </li>
                                        <li><a data-url='${createLink(action: 'createTemplate')}?templateType=${TemplateTypeEnum.NON_CASE.key}'
                                               data-message='${message(code: "app.template.query.create.warning")}'
                                               class="createTemplateQueryButton">${message(code: "app.templateType.NON_CASE")}</a>
                                        </li>
                                        <li><a data-url='${createLink(action: 'createTemplate')}?templateType=${TemplateTypeEnum.TEMPLATE_SET.key}'
                                               data-message='${message(code: "app.template.query.create.warning")}'
                                               class="createTemplateQueryButton">${message(code: "app.templateType.TEMPLATE_SET")}</a>
                                        </li>
                                    </g:else>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row granularityDiv" style="display: ${templateQueryInstance?.granularity ? 'block' : 'none'}">
                <div class="col-md-3">
                    <label><g:message code="app.label.granularity"/></label>
                </div>
                <div class="col-md-8">
                    <g:select name="templateQueries[${i}].granularity" optionKey="name" optionValue="display" disabled="${!templateQueryInstance?.granularity}"
                              from="${ViewHelper.granularity}" value="${templateQueryInstance?.granularity ?: com.rxlogix.enums.GranularityEnum.MONTHLY}"
                              class="form-control select2-box granularitySelect"/>
                </div>
            </div>
            <div class="row templtReassessDateDiv" style="display: ${templateQueryInstance?.showReassessDateDiv() ? 'block' : 'none'}">
                <div class="col-md-5">
                    <label><g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/></label>
                </div>
                <div class="col-md-6 fuelux datepicker input-group templtDatePicker">
                    <g:textField name="templateQueries[${i}].templtReassessDate" class="form-control templtReassessDate" value="${renderShortFormattedDate(date: templateQueryInstance?.templtReassessDate ?: (templateQueryInstance?.showReassessDateDiv() ? new Date() : null))}"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="row ${(params.fromTemplate || isForIcsrReport ) ? "hidden" : ""}">
                <g:if test="${isForPeriodicReport}">
                    <div class="col-md-12 pvpOnly" style="display:none;  padding-bottom: 10px;">
                        <g:if test="${templateQueryInstance?.userGroup?.id}">
                            <span class="queryTemplateUserGroupLabel">Visible For ${UserGroup.get(templateQueryInstance?.userGroup?.id).name} Only</span> <span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span>
                        </g:if>
                        <g:else>
                            <span class="queryTemplateUserGroupLabel"><g:message code="app.label.PublisherTemplate.visibleForAnyUserGroup"/></span> <span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span>
                        </g:else>
                        <input type="hidden" id="templateQueries[${i}].userGroup" name="templateQueries[${i}].userGroup" value="${templateQueryInstance?.userGroup?.id}">
                    </div>
                </g:if>
                <div class="col-md-7">
                    <a class="add-cursor showHeaderFooterArea"><g:message code="${isForIcsrProfile ? "add.description.label" : "add.header.title.and.footer"}" /></a>
                </div>
                <g:if test="${isForPeriodicReport}">
                    <div class="col-md-5 ">
                        <label class="no-bold add-cursor">
                            <g:checkBox id="templateQueries[${i}].draftOnly"
                                        name="templateQueries[${i}].draftOnly"
                                        value="${templateQueryInstance?.draftOnly}"
                                        checked="${templateQueryInstance?.draftOnly}"/>
                            <span style="margin-left: 5px"><g:message code="app.label.draftOnly"/> </span>
                        </label>
                    </div>

                </g:if>
                <div class="clearfix"></div>
            </div>

            <g:if test="${!isForIcsrProfile }">
            <div class="headerFooterArea" hidden="hidden">
                <div class="row">
                    <div class="col-md-12">
                        <g:textField name="templateQueries[${i}].header"
                                     maxlength="${TemplateQuery.constrainedProperties.header.maxSize}"
                                     value="${templateQueryInstance?.header}"
                                     placeholder="${message(code: "placeholder.templateQuery.header")}"
                                     class="form-control"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <g:textField name="templateQueries[${i}].title"
                                     maxlength="${Configuration.constrainedProperties.reportName.maxSize}"
                                     value="${templateQueryInstance?.title}"
                                     placeholder="${message(code: "placeholder.templateQuery.title")}"
                                     class="form-control"/>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <g:footerSelect name="templateQueries[${i}].footer" value="${templateQueryInstance?.footer}" class="form-control footerSelect"/>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].headerProductSelection"
                                        name="templateQueries[${i}].headerProductSelection"
                                        value="${templateQueryInstance?.headerProductSelection}"
                                        checked="${templateQueryInstance?.headerProductSelection}"/>
                            <label for="templateQueries[${i}].headerProductSelection">
                                <g:message code="templateQuery.headerProductSelection.label" />
                            </label>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].headerDateRange"
                                        name="templateQueries[${i}].headerDateRange"
                                        value="${templateQueryInstance?.headerDateRange}"
                                        checked="${templateQueryInstance?.headerDateRange}"/>
                            <label for="templateQueries[${i}].headerDateRange">
                                <g:message code="templateQuery.headerDateRange.label" />
                            </label>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].displayMedDraVersionNumber"
                                        name="templateQueries[${i}].displayMedDraVersionNumber"
                                        value="${templateQueryInstance?.displayMedDraVersionNumber}"
                                        checked="${templateQueryInstance?.displayMedDraVersionNumber}"/>
                            <label for="templateQueries[${i}].displayMedDraVersionNumber">
                                <g:message code="templateQuery.displayMedDraVersionNumber.label"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            </g:if>
            <g:elseif test="${isForIcsrProfile}">
            <div class="headerFooterArea" hidden="hidden">
                <div class="row">
                    <div class="col-md-12">
                        <g:textField name="templateQueries[${i}].title"
                                     maxlength="${Configuration.constrainedProperties.reportName.maxSize}"
                                    value="${templateQueryInstance?.title}"
                                    placeholder="${message(code: "placeholder.templateQuery.description")}"
                                    class="form-control"/>
                    </div>
                </div>
            </div>
            </g:elseif>


            <div class="ciomsProtectedArea ${params.fromTemplate ? "hidden" : ""}" ${(isForIcsrProfile || isForIcsrReport || templateQueryInstance?.template?.id == ReportTemplate.cioms1Id() || templateQueryInstance?.template?.id == ReportTemplate.medWatchId()) ? "" : "hidden"}>
                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].privacyProtected"
                                        name="templateQueries[${i}].privacyProtected"
                                        value="${templateQueryInstance?.privacyProtected}"
                                        checked="${templateQueryInstance?.privacyProtected}"/>
                            <label for=templateQueries[${i}].privacyProtected>
                                <g:message code="templateQuery.privacyProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox id="templateQueries[${i}].blindProtected"
                                        name="templateQueries[${i}].blindProtected"
                                        value="${templateQueryInstance?.blindProtected}"
                                        checked="${templateQueryInstance?.blindProtected}"/>
                            <label for=templateQueries[${i}].blindProtected>
                                <g:message code="templateQuery.blindProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-${qbeForm?12:8} queryWrapperRow">
            <div class="row">
                %{--Query--}%
                <div class="col-md-6">
                    <div class="row">
                        <div class="col-md-12 ${hasErrors(bean: error, field: 'query', 'has-error')}">
                            <label><g:message code="app.label.chooseAQuery"/></label>
                        </div>
                    </div>

                    <div class="row queryContainer">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>
                        <div class="col-md-12">
                            <div class="doneLoading" style="padding-bottom: 5px;">
                                <div>
                                    <div class="col-md-11"><g:select hidden="hidden" from="${[]}" name="templateQueries[${i}].query"
                                                                          readonly="${params.fromTemplate}"
                                                                          data-value="${templateQueryInstance?.query?.id}"
                                                                          class="form-control selectQuery"/></div>
                                    <div><a href="${templateQueryInstance?.query?.id ?createLink(controller: 'query' , action: 'view', id: templateQueryInstance?.query?.id):'#'}"
                                            title="${message(code: 'app.label.viewQuery')}" target="_blank" class="templateQueryIcon pv-ic queryViewButton glyphicon glyphicon-info-sign ${templateQueryInstance?.query?.id ?'':'hide'}"></a>
                                        <g:selectController isForPeriodicReport="${isForPeriodicReport}" isForIcsrReport="${isForIcsrReport}" isForIcsrProfile="${isForIcsrProfile}"
                                                             fromTemplate="${params.fromTemplate ? "hidden" : ""}" templateQueryInstance="${templateQueryInstance?.query?.id ? 'hide' : ''}"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <g:if test="${isForIcsrProfile}">
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.to" value="${templateQueryInstance?.emailConfiguration?.to}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.cc" value="${templateQueryInstance?.emailConfiguration?.cc}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.body" value="${templateQueryInstance?.emailConfiguration?.body}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.subject" value="${templateQueryInstance?.emailConfiguration?.subject}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration.deliveryReceipt" value="${templateQueryInstance?.emailConfiguration?.deliveryReceipt}"/>
                    <g:hiddenField name="templateQueries[${i}].emailConfiguration" id="emailConfiguration-${i}" value="${templateQueryInstance?.emailConfiguration?.id}"/>
                %{--Authorization Type--}%

                %{--Due In Days--}%
                    <div class="col-md-1">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.label.icsr.profile.conf.dueInDays"/><span class="required-indicator">*</span></label>

                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div>
                                    <g:textField type="number" min="1" data-evt-onkeyup='{"method": "checkDecimal", "params": []}' required="true"
                                                 name='templateQueries[${i}].dueInDays' maxlength="9"
                                                 value="${templateQueryInstance?.dueInDays}" class="form-control dueInDays"/>
                                </div>
                            </div>
                        </div>
                    </div>

                <div class="col-md-7">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="col-md-2">
                                <div class="row">
                                    <div class="col-md-12">
                                        <label>
                                            <g:message code="app.label.icsr.profile.conf.expedited"/>
                                        </label>

                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-12">
                                        <div>
                                            <g:checkBox name="templateQueries[${i}].isExpedited"
                                                        value="${templateQueryInstance?.isExpedited}"
                                                        checked="${templateQueryInstance?.isExpedited}"/>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        %{--Message Type--}%
                            <div class="col-md-5">
                                <div class="row">
                                    <div class="col-md-12">
                                        <label><g:message code="app.label.icsr.profile.conf.messageType"/><span class="required-indicator">*</span></label>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-12">
                                        <div>
                                            <g:select name="templateQueries[${i}].msgType"
                                                      from="${icsrScheduleService.getMsgType()}"
                                                      class="form-control msgType" optionKey="id" optionValue="name"
                                                      value="${templateQueryInstance?.icsrMsgType}"/>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        %{--Distribution Channel Type--}%
                            <div class="col-md-5">
                                <div class="row">
                                    <div class="col-md-12">
                                        <label><g:message code="app.label.icsr.profile.conf.distributionChannel"/><span class="required-indicator">*</span></label>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-md-12">
                                                    <div>
                                            <g:select name="templateQueries[${i}].distributionChannelName"
                                                      from="${ViewHelper.getDistributionChannelEnumI18n()}"
                                                      data-idx="${i}"
                                                      data-evt-change='{"method": "distributionChannelChanged", "params": []}'
                                                      class="form-control distributionChannelName" optionKey="name" optionValue="display"
                                                      value="${templateQueryInstance?.distributionChannelName}"/>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12">
                                        <div>
                                            <span data-idx="${i}" data-evt-clk='{"method": "showEmailConfiguration", "params": []}' id="showEmailConfiguration-${i}" <g:if test="${templateQueryInstance?.distributionChannelName.toString() != 'EMAIL'}"> style="cursor: pointer; display:none;" </g:if> >
                                            <g:if test="${templateQueryInstance?.distributionChannelName.toString() == 'EMAIL' && templateQueryInstance?.emailConfiguration?.to}">
                                                <asset:image src="icons/email-secure.png" title="${message(code: 'default.button.addEmailConfigurationEdited.label')}"/></span>
                                            </g:if>
                                            <g:else>
                                                <asset:image src="icons/email.png" title="${message(code: 'default.button.addEmailConfiguration.label')}"/></span>
                                            </g:else>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                </g:if>
                <g:elseif test="${isForIcsrReport}">
                %{--Query Level--}%
                    <div class="col-md-2">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.label.queryLevel"/></label>
                                <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                                      data-toggle="modal"
                                      data-target="#queryLevelHelpModal"></span>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div>
                                    <g:select name="templateQueries[${i}].queryLevel" readonly="${params.fromTemplate}"
                                              from="${ViewHelper.getQueryLevels()}"
                                              value="${templateQueryInstance?.queryLevel}"
                                              valueMessagePrefix="app.queryLevel"
                                              class="form-control selectQueryLevel"/>
                                </div>
                            </div>
                        </div>
                    </div>

                %{--Date Range--}%
                    <div class="col-md-2 sectionDateRange">
                        <g:render template="/configuration/dateRange"
                                  model="[templateQueryInstance: templateQueryInstance, isForPeriodicReport: isForPeriodicReport]"/>
                    </div>
                    <div class="col-md-2">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.label.icsr.profile.conf.messageType"/><span class="required-indicator">*</span></label>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div>
                                        <g:select name="templateQueries[${i}].msgType"
                                              from="${icsrScheduleService.getMsgType()}"
                                              class="form-control msgType" optionKey="id" optionValue="name"
                                              value="templateQueryInstance?.icsrMsgType"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:elseif>
                <g:else>
                %{--Query Level--}%
                    <div class="col-md-3">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.label.queryLevel"/></label>
                                <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                                      data-toggle="modal"
                                      data-target="#queryLevelHelpModal"></span>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div>
                                    <g:select name="templateQueries[${i}].queryLevel" readonly="${params.fromTemplate}"
                                              from="${ViewHelper.getQueryLevels()}"
                                              value="${templateQueryInstance?.queryLevel}"
                                              valueMessagePrefix="app.queryLevel"
                                              class="form-control selectQueryLevel"/>
                                </div>
                            </div>
                        </div>
                    </div>

                %{--Date Range--}%
                    <div class="col-md-3 sectionDateRange">
                        <g:render template="/configuration/dateRange"
                                  model="[templateQueryInstance: templateQueryInstance, isForPeriodicReport: isForPeriodicReport]"/>
                    </div>
                </g:else>


            </div>

            <div class="row">
                %{--Blank values--}%
                <div class="col-md-12">
                    <div class="templateSQLValues">
                        <g:if test="${templateQueryInstance?.templateValueLists?.size() > 0}">
                            <g:each var="tvl" in="${templateQueryInstance.templateValueLists}">
                                <g:each var="tv" in="${tvl.parameterValues}" status="j">
                                    <g:render template='/query/customSQLValue' model="['qev': tv, 'i':i, 'j':j]"/>
                                </g:each>
                            </g:each>
                        </g:if>
                        <g:if test="${templateQueryInstance?.getPOIInputsKeysValues()}">
                            <g:each in="${templateQueryInstance.getPOIInputsKeysValues()}" var="inputPOIKeyValue">
                                <g:render template='/query/poiInputValue' model="[key:inputPOIKeyValue.key,value:inputPOIKeyValue.value]"/>
                            </g:each>
                        </g:if>
                    </div>

                    <div class="queryExpressionValues">
                        <g:if test="${templateQueryInstance?.queryValueLists?.size() > 0}">
                            <g:if test="${qbeForm}">
                                <div  class="queryStructure col-xs-8" >
                                    <label>Query:</label>
                                    <div style="border: #cccccc solid 1px;padding: 2px; border-radius: 5px;background: #EfEfEf">
                                        ${(templateQueryInstance.query instanceof com.rxlogix.config.QuerySet)?ExcelExportService.getQuerySetStructure(templateQueryInstance.query):templateQueryInstance.query.name}
                                    </div>
                                </div>
                            </g:if>

                            <g:each var="qvl" in="${templateQueryInstance.queryValueLists}">
                                <g:if test="${(templateQueryInstance.queryValueLists.size()>1) ||qbeForm}">
                                    <g:if test="${qbeForm}">
                                    <div>
                                    <div class="row rxmain-container-header " style="height: 30px; margin-top: 5px"><i class="fa fa-caret-right fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i> <label>${qvl.query.name}</label></div>
                                    <div class="row rxmain-container-content rxmain-container-hide">
                                    </g:if>
                                    <g:else>
                                        <g:if test="${(templateQueryInstance.query instanceof com.rxlogix.config.QuerySet)}">
                                            <g:if test="${qvl.parameterValues?.size()>0}">
                                                <label>${qvl.query.name}</label>
                                            </g:if>
                                        </g:if>
                                        <g:else>
                                        <label>${qvl.query.name}</label>
                                        </g:else>
                                    </g:else>
                                </g:if>
                                <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                    <g:if test="${qev?.hasProperty('reportField')}">
                                        <g:render template='/query/toAddContainerQEV' model="['qev': qev, 'i':i, 'j':j, qbeForm:qbeForm]"/>
                                    </g:if>
                                    <g:else>
                                        <g:render template='/query/customSQLValue' model="['qev': qev, 'i':i, 'j':j, qbeForm:qbeForm]"/>
                                    </g:else>
                                </g:each>
                                <g:if test="${qbeForm && (templateQueryInstance.queryValueLists.size()>0)}">
                                    </div>
                                    </div>
                                </g:if>
                            </g:each>
                        </g:if>
                    </div>
                <div class="col-md-12 showCustomReassess" style="display: ${templateQueryInstance?.showQueryReassessDateDiv() ? 'block' : 'none'}; padding-left: 30px !important;">
                    <div class="col-md-4">
                        <label><g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/></label>
                    </div>
                    <div class="col-md-3 fuelux datepicker input-group customDatePicker">
                        <g:textField name="templateQueries[${i}].reassessListednessDate" class="form-control reassessDate" value="${renderShortFormattedDate(date: templateQueryInstance?.reassessListednessDate ?: new Date())}"
                        />
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
                    <g:hiddenField class="validQueries" name="templateQueries[${i}].validQueries"
                                   value="${configurationService.getQueriesId(templateQueryInstance)}" />
                </div>
            </div>
        </div>
    </div>
    <g:if test="${theInstance?.pvqType}">
        <div class="rcaSection">
        <div class="row ">

            <div class="col-md-2"><label><g:message code="rod.fieldType.Issue_Type"/></label>
                <input type="hidden" name="templateQueries[${i}].issueType" id="templateQueries[${i}].issueType" class="issueTypeValue" value="${templateQueryInstance?.issueType}">
                <select class="form-control issueType">
                    <option value=""></option>
                </select>
            </div>
            <div class="col-md-2">
                <label><g:message code="rod.fieldType.Root_Cause"/></label>
                <input type="hidden" name="templateQueries[${i}].rootCause" id="templateQueries[${i}].rootCause" class="rootCauseValue" value="${templateQueryInstance?.rootCause}">
                <select class="form-control rootCause">
                    <option value=""></option>
                </select>
            </div>
            <div class="col-md-2">
                <label><g:message code="rod.fieldType.Resp_Party"/></label>
                <input type="hidden" name="templateQueries[${i}].responsibleParty" id="templateQueries[${i}].responsibleParty" class="responsiblePartyValue" value="${templateQueryInstance?.responsibleParty}">
                <select class="form-control responsibleParty">
                    <option value=""></option>
                </select>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.label.assignedToGroup"/></label>
                <select name="templateQueries[${i}].assignedToGroup" id="templateQueries[${i}].assignedToGroup"
                        class='form-control assignedToGroupValue'
                        data-value="${templateQueryInstance?.assignedToGroup?Constants.USER_GROUP_TOKEN + templateQueryInstance?.assignedToGroupId:""}"></select>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.label.assignedToUser"/></label>
                <select name="templateQueries[${i}].assignedToUser" id="templateQueries[${i}].assignedToUser"
                        class='form-control assignedToUserValue'
                        data-value="${templateQueryInstance?.assignedToUser?Constants.USER_TOKEN+templateQueryInstance?.assignedToUserId:""}"></select>
            </div>
            <div class="col-md-2">
                <label><g:message code="app.label.quality.priority"/></label>
                <select class="form-control priority" name="templateQueries[${i}].priority" id="templateQueries[${i}].priority">
                    <g:each var="p" in="${grails.util.Holders.config.qualityModule.pvqPriorityList ?: []}">
                        <option value="${p.name}" ${templateQueryInstance?.priority == p.name ? "selected" : ""}>${p.name}</option>
                    </g:each>
                </select>
            </div>
        </div>
            <div class="row">
            <div class="col-md-4">
                <label><g:message code="app.pvc.investigation"/> <i style="cursor:pointer;" class="fa fa-edit editSql"></i></label>
                <textarea style="height: 73px;" class="multiline-text form-control textInput ${templateQueryInstance?.investigationSql?"hidden":""}"  name="templateQueries[${i}].investigation" id="templateQueries[${i}].investigation" maxlength="${TemplateQuery.constrainedProperties.investigation.maxSize}">${templateQueryInstance?.investigation}</textarea>
                <textarea style="height: 73px;" class="multiline-text form-control sqlInput ${templateQueryInstance?.investigationSql?"":"hidden"}"  readonly name="templateQueries[${i}].investigationSql" id="templateQueries[${i}].investigationSql" maxlength="${TemplateQuery.constrainedProperties.investigationSql.maxSize}">${templateQueryInstance?.investigationSql}</textarea>
                <span class="removeSql fa fa-times ${templateQueryInstance?.investigationSql?"":"hidden"}" style="position: absolute;right: 6px;top: 25px;cursor: pointer;"></span>
            </div>
            <div class="col-md-4">
                <label><g:message code="app.pvc.summary"/> <i style="cursor:pointer;" class="fa fa-edit editSql"></i></label>
                <textarea style="height: 73px;" class="multiline-text form-control textInput ${templateQueryInstance?.summarySql?"hidden":""}"  name="templateQueries[${i}].summary" id="templateQueries[${i}].summary" maxlength="${TemplateQuery.constrainedProperties.summary.maxSize}">${templateQueryInstance?.summary}</textarea>
                <textarea style="height: 73px;" class="multiline-text form-control sqlInput ${templateQueryInstance?.summarySql?"":"hidden"}"  readonly name="templateQueries[${i}].summarySql" id="templateQueries[${i}].summarySql" maxlength="${TemplateQuery.constrainedProperties.summarySql.maxSize}">${templateQueryInstance?.summarySql}</textarea>
                <span class="removeSql fa fa-times ${templateQueryInstance?.summarySql?"":"hidden"}" style="position: absolute;right: 6px;top: 25px;cursor: pointer;"></span>
            </div>
            <div class="col-md-4">
                <label><g:message code="quality.actions.label"/> <i style="cursor:pointer;" class="fa fa-edit editSql"></i></label>
                <textarea style="height: 73px;" class="multiline-text form-control textInput ${templateQueryInstance?.actionsSql?"hidden":""}"  name="templateQueries[${i}].actions" id="templateQueries[${i}].actions" maxlength="${TemplateQuery.constrainedProperties.actions.maxSize}">${templateQueryInstance?.actions}</textarea>
                <textarea style="height: 73px;" class="multiline-text form-control sqlInput ${templateQueryInstance?.actionsSql?"":"hidden"}"  readonly name="templateQueries[${i}].actionsSql" id="templateQueries[${i}].actionsSql" maxlength="${TemplateQuery.constrainedProperties.actionsSql.maxSize}">${templateQueryInstance?.actionsSql}</textarea>
                <span class="removeSql fa fa-times ${templateQueryInstance?.actionsSql?"":"hidden"}" style="position: absolute;right: 6px;top: 25px;cursor: pointer;"></span>
            </div>
        </div>
        </div>
    </g:if>
</div>
<g:render template="/query/queryLevelHelp"/>
