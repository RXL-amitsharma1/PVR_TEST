<%@ page import="com.rxlogix.config.ReportConfiguration; com.rxlogix.config.ExecutedPublisherSource; com.rxlogix.config.Configuration; com.rxlogix.config.publisher.PublisherConfigurationSection; com.rxlogix.config.ExecutedIcsrReportConfiguration; com.rxlogix.config.BaseConfiguration; com.rxlogix.config.IcsrReportConfiguration;com.rxlogix.config.publisher.PublisherTemplateParameter; com.rxlogix.config.UnitConfiguration; grails.converters.JSON; com.rxlogix.config.publisher.PublisherTemplate; com.rxlogix.config.ApplicationSettings; com.rxlogix.config.CaseSeries; com.rxlogix.user.User; com.rxlogix.user.UserGroup; com.rxlogix.Constants; com.rxlogix.util.ViewHelper; com.rxlogix.util.RelativeDateConverter; java.text.SimpleDateFormat; com.rxlogix.enums.ReportFormatEnum; org.hibernate.validator.constraints.Email; com.rxlogix.util.DateUtil;com.rxlogix.config.publisher.Gantt" %>
<g:set var="userService" bean="userService"/>
<g:set var="currentUser" value="${userService.currentUser}"/>
<g:set var="forPvq" value="${configurationInstance   && (configurationInstance instanceof ReportConfiguration)&&configurationInstance.pvqType}"/>
<asset:javascript src="app/emailAttachmentSplit.js"/>
<asset:javascript src="app/publisher/PublisherTemplateConfiguration.js"/>
<script>
    var fetchParametersUrl="${createLink(controller: 'publisherTemplate', action: 'fetchParameters')}";
    var publisherTemplateParametersUrl="${createLink(controller: 'publisherTemplate', action: 'getTemplateParameters')}";
    var testScriptUrl="${createLink(controller: 'publisherTemplate', action: 'testScript')}";
    var PVPTemplateSearchUrl="${createLink(controller: 'publisherTemplate', action: 'getPublisherTemplateList')}";
    var PVPTemplateNameUrl="${createLink(controller: 'publisherTemplate', action: 'getTemplateNameDescription')}";
    var PVPTaskTemplateSearchUrl="${createLink(controller: 'taskTemplate', action: 'ajaxGetPublisherSectionTemplates')}";
    var PVPTaskTemplateNameUrl="${createLink(controller: 'taskTemplate', action: 'ajaxGetPublisherSectionTemplatesName')}";
    var publisherContributorsUrl = "${createLink(controller: 'userRest', action: 'getPublisherContributors')}";
    var userValuesUrl = "${createLink(controller: 'userRest', action: 'userListValue')}";
</script>
<g:if test="${params.fromTemplate}">
    <script>
        $(function () {
            $(".timezone-container").hide();
        });
    </script>
</g:if>
<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <g:if test="${!(configurationInstance instanceof CaseSeries)}">
                <i class="fa fa-caret-${(isForPeriodicReport||forPvq) ? 'down' : 'right'} fa-lg click"
                   data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:if test="${!forPvq}">
                    <g:message code="app.label.reportDeliveryOptions"/> &
                    </g:if>
                    <g:message code="app.label.scheduler"/>
                </label>
            </g:if>
            <g:else>
                <i class="fa fa-caret-down' fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.caseDeliveryOptions"/>
                </label>

            </g:else>
        </div>

        <div class="rxmain-container-content rxmain-container-${isForPeriodicReport || forPvq || isForIcsrReport || (configurationInstance instanceof CaseSeries) ? 'show' : 'hide'}">

            <div class="row">
                <div class="col-xs-4">

                    %{--Share With--}%
                    <div class="row">
                        <div class="col-xs-12 editableBy">
                            <script>
                                sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                                sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                                $(function () {
                                    bindShareWithEditable($('#cfgSharedWith'), sharedWithListUrl, sharedWithValuesUrl, "100%")
                                });
                            </script>
                            <label><g:message code="shared.with"/><span class="required-indicator">*</span></label>
                            <g:set var="sharedWithValue" value="${(configurationInstance?.shareWithGroups?.collect {
                                Constants.USER_GROUP_TOKEN + it.id
                            } + configurationInstance?.shareWithUsers?.collect {
                                Constants.USER_TOKEN + it.id
                            })?.join(";")}"/>
                            <g:if test="${!sharedWithValue}">
                                <g:set var="sharedWithValue"
                                       value="${([Constants.USER_TOKEN + currentUser.id]).join(";")}"/>
                            </g:if>
                            <select class="sharedWithControl form-control" id="cfgSharedWith" name="sharedWith"
                                    data-value="${sharedWithValue}"></select>
                            <g:if test="${configurationInstance instanceof ReportConfiguration}">
                            <g:set var="executableByValue" value="${(configurationInstance?.executableByGroup?.collect {
                                Constants.USER_GROUP_TOKEN + it.id
                            } + configurationInstance?.executableByUser?.collect {
                                Constants.USER_TOKEN + it.id
                            })?.join(";")}"/>
                            <g:if test="${!executableByValue}">
                                <g:set var="executableByValue"
                                       value="${([Constants.USER_TOKEN + currentUser.id]).join(";")}"/>
                            </g:if>
                            <input class="executableByControl form-control" id="executableBy" name="executableBy"
                                   type="hidden" value="${executableByValue}">
                            </g:if>
                        </div>
                    </div>
                    %{--Email to--}%
            <g:if test="${!isForIcsrReport}">
                    <div class="row">
                        <div class="col-xs-12 m-t-5">
                            <label><g:message code="app.label.emailTo"/></label>
                            <span class="mdi mdi-email showEmailConfiguration" style="cursor: pointer;" data-toggle="modal"
                                  data-target="#emailConfiguration">
                                <asset:image src="icons/email.png" title="${message(code: 'default.button.addEmailConfiguration.label')}"/>
                            </span>
                        </div>
                    </div>

                    %{--Email to--}%
                        <div class="row">
                            <div class="col-xs-12" style="padding-right: 27px;">
                                <g:renderClosableInlineAlert id="email-invalid-alert" type="danger" />
                                <g:select id="emailUsers"
                                          name="deliveryOption.emailToUsers"
                                          from="${[]}"
                                          data-value="${configurationInstance?.deliveryOption?.emailToUsers?.join(",")}"
                                          class="form-control emailUsers" multiple="true"
                                          data-options-url="${createLink(controller: 'email', action: 'allEmails', params: [id: configurationInstance?.id])}"/><i
                                    class="fa fa-pencil-square-o copyPasteEmailButton"></i>
                            </div>
                        </div>

                    %{--Attachments--}%
                        <div id="attachmentCheckboxes">
                            <div class="row form-inline">
                                <div class="radio radio-primary">
                                    <g:radio value="" id="emailAttachmentCheckboxAll" disabled="true"
                                             class="emailAttachmentCheckbox" name="emailAttachmentCheckbox"
                                             checked="${configurationInstance?.deliveryOption?.additionalAttachments ? false : true}"/>
                                    <label for="emailAttachmentCheckboxAll">
                                        <g:message code="app.label.additionalAttachments.allSections"/>
                                    </label>
                                </div>
                                <g:if test="${!(configurationInstance instanceof CaseSeries)}">
                                    <div class="radio radio-primary">
                                        <g:radio value="" id="emailAttachmentCheckboxSplit" disabled="true"
                                                 class="emailAttachmentCheckbox" name="emailAttachmentCheckbox"
                                                 checked="${configurationInstance?.deliveryOption?.additionalAttachments ? true : false}"/>
                                        <label for="emailAttachmentCheckboxSplit">
                                            <g:message code="app.label.additionalAttachments.splitSections"/>
                                        </label>
                                    </div>
                                </g:if>
                                <g:if test="${isForPeriodicReport || !isForIcsrReport}">
                                    <div class="row">
                                        <div class="col-xs-12">
                                            <div id="emailAttachmentAll">
                                                <g:each var="reportFormatEnum" in="${ReportFormatEnum.emailShareOptions}">
                                                    <div class="checkbox checkbox-primary checkbox-inline">
                                                        <g:checkBox id="deliveryOption${reportFormatEnum}"
                                                                    class="emailOption"
                                                                    name="deliveryOption.attachmentFormats"
                                                                    value="${reportFormatEnum}"
                                                                    checked="${(configurationInstance?.deliveryOption?.attachmentFormats && reportFormatEnum in configurationInstance.deliveryOption.attachmentFormats)}"/>
                                                        <label for="deliveryOption${reportFormatEnum}">
                                                            ${message(code: reportFormatEnum.i18nKey)}
                                                        </label>
                                                    </div>
                                                </g:each>
                                            </div>

                                            <div id="emailAttachmentSplit" style="display: none">

                                                <div class="alert alert-warning alert-dismissible fade in additionalAttachmentsWarning"
                                                     role="alert" style="display: none;">
                                                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span
                                                            aria-hidden="true">&times;</span></button>
                                                    <strong><g:message code="app.label.warning"/></strong><g:message
                                                        code="app.label.additionalAttachmentsWarning"/></div>

                                                <input type="hidden" id="emailAttachmentsConfig" name="deliveryOption.additionalAttachments"
                                                       value="${configurationInstance?.deliveryOption?.additionalAttachments}">
                                                <table id="emailAttachmentsTable" class="emailAttachmentsTable" width="100%">
                                                    <tr>
                                                        <th width="50px"><span class="glyphicon glyphicon-plus showAttachmentModalButton"
                                                                               style="color: #070; cursor: pointer;"></span></th>
                                                        <th align="center" width="200px"><g:message code="file.format"/></th>
                                                        <th align="center"><g:message code="app.label.reportSections"/></th>
                                                    </tr>
                                                </table>

                                            </div>
                                        </div>
                                    </div>
                                </g:if>
                            </div>

                        </div>
                    </g:if>

                    <g:if test="${isForIcsrReport}">
                        <div class="row">
                            <div class="col-xs-12">
                                <label><g:message code="app.label.icsr.report.send.emailNotification.to"/></label>
                                <span class="showEmailConfiguration" style="cursor: pointer;" data-toggle="modal"
                                      data-target="#emailConfiguration"><asset:image
                                        src="icons/email.png"
                                        title="${message(code: 'default.button.addEmailConfiguration.label')}"/></span>
                            </div>
                        </div>

                    %{--Email to--}%
                        <div class="row">
                            <div class="col-xs-12" style="padding-right: 27px;">
                                <g:select id="emailUsers"
                                          name="deliveryOption.emailToUsers"
                                          from="${[]}"
                                          data-value="${configurationInstance?.deliveryOption?.emailToUsers?.join(",")}"
                                          class="form-control emailUsers" multiple="true"
                                          data-options-url="${createLink(controller: 'email', action: 'allEmails', params: [id: configurationInstance?.id])}"/><i
                                    class="fa fa-pencil-square-o copyPasteEmailButton"></i>
                                %{--  Added to handle delieveryOption nullpointer handling--}%
                                <input type="hidden" id="emailAttachmentsConfig" name="deliveryOption.additionalAttachments"
                                       value="">

                            </div>
                        </div>

                    %{--Attachments--}%

                    </g:if>
                </div>




            %{--Scheduler--}%
                    <div class="col-xs-4">
                        <div class="row">
                            <div class="col-xs-12">
                                %{--
                                    This has to be checked if this can be reused.
                                    Right now this can only be accessed from the configuration page because
                                    the page is not open to all the view pages.
                                --}%
                                %{--The Markup code--}%
                                <g:render template="/configuration/schedulerTemplate" model="[adhoc:(configurationInstance instanceof com.rxlogix.config.Configuration)]"/>
                                <g:hiddenField name="isEnabled" id="isEnabled"
                                               value="${configurationInstance?.isEnabled}"/>
                                <g:hiddenField name="schedulerTime"
                                               value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(currentUser)}"/>
                                <g:scheduleDateJSON value="${configurationInstance?.scheduleDateJSON ?: null}" name="scheduleDateJSON"/>
                                <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone"
                                       value="${ViewHelper.getUserTimeZoneForConfig(configurationInstance, currentUser)}"/>
                                <input type="hidden" id="timezoneFromServer" name="timezone"
                                       value="${DateUtil.getTimezone(currentUser)}"/>
                                <g:if test="${(configurationInstance instanceof com.rxlogix.config.Configuration)}" >
                                    <div class="checkbox checkbox-primary m-t-5 ${params.fromTemplate ? "hidden" : ""}" >
                                        <g:checkBox name="removeOldVersion" id="removeOldVersion"
                                                    value="${configurationInstance?.removeOldVersion}"
                                                    checked="${configurationInstance?.removeOldVersion}"/>
                                        <label for="removeOldVersion">
                                            <g:message code="app.label.removeOldVersion" default="Remove Old Versions"/>
                                        </label>
                                    </div>
                                </g:if>
                            </div>

                            <div class="clearfix"></div>
                        </div>

                        %{--For Edit only--}%
                        <div class="row">
                            <g:if test="${configurationInstance?.isEnabled && configurationInstance?.nextRunDate}">
                                <div class="col-xs-8 ${hasErrors(bean: configurationInstance, field: 'nextRunDate', 'has-error')}">
                                    <label><g:message code="app.label.nextScheduledRunDate"/></label>

                                    <div>
                                        <g:render template="/includes/widgets/dateDisplayWithTimezone"
                                                  model="[date: configurationInstance?.nextRunDate]"/>
                                    </div>
                                </div>
                            </g:if>
                            <div class="clearfix"></div>
                        </div>

                    </div>


                <div class="col-xs-4  ${forPvq?"hidden":""}" >
                    <div class="row">
                        <div class="col-xs-12">
                            <g:if test="${isForPeriodicReport || isForIcsrReport}">
                                <g:if test="${!isForIcsrReport}">
                                    <div class="row ">
                                        <div class="col-xs-4 set-margin-dueIn">
                                            <g:if test="${grailsApplication.config.pv.app.pvpublisher.gantt.enabled}">
                                            <div class="radio radio-inline pvpOnly " style="display: none">
                                                <input type="radio" id="dueInPlanSwitcher1" name="dueInPlanSwitcher" ${configurationInstance?.gantt ? "" : "checked"}/>
                                                <label for="dueInPlanSwitcher1"><g:message code="app.label.dueInDaysPastDLP"/></label>
                                            </div>
                                            </g:if>
                                            <g:else>
                                                <div class="pvpOnly" style="display: none">
                                                    <label for="dueInDays"><g:message code="app.label.dueInDaysPastDLP"/></label>
                                                </div>
                                            </g:else>
                                            <div class="notPvpOnly">
                                                <label for="dueInDays"><g:message code="app.label.dueInDaysPastDLP"/></label>
                                            </div>
                                            <div>
                                                <input type="number" name="dueInDays" id="dueInDays"
                                                       value="${configurationInstance.dueInDays}" class="form-control dueInDays" max="365" min="0"/>
                                            </div>
                                        </div>
                                        <g:if test="${grailsApplication.config.pv.app.pvpublisher.gantt.enabled}">
                                        <div class="col-xs-8 set-margin-dueIn pvpOnly " style="display: none">
                                            <div class="radio radio-inline">
                                                <input type="radio" id="dueInPlanSwitcher2" name="dueInPlanSwitcher" ${configurationInstance?.gantt ? "checked" : ""}/>
                                                <label for="dueInPlanSwitcher2"><g:message code="app.label.gantt.appName"/></label>
                                            </div>
                                            <div>
                                                <g:select name="gantt" from="${Gantt.findAllByIsDeletedAndIsTemplate(false, true)}" value="${configurationInstance?.ganttId}"
                                                          class="select2 form-control ganttSelect" optionKey="id" optionValue="name" noSelection="['': '']"/>
                                            </div>
                                        </div>
                                        </g:if>
                                    </div>
                                    <div class="row">
                                        <div class="col-xs-12 margin-left-38">
                                            <label class="add-margin-bottom"><g:message
                                                    code="app.label.reportingDestinations"/><span
                                                    class="required-indicator">*</span></label>

                                            <div class="destinations">
                                                <g:hiddenField name="primaryReportingDestination"
                                                               value="${configurationInstance.primaryReportingDestination}"/>
                                                <g:select name="reportingDestinations"
                                                          from="${[]}"
                                                          data-value="${configurationInstance.allReportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                                          value="${configurationInstance.allReportingDestinations?.join(Constants.MULTIPLE_AJAX_SEPARATOR)}"
                                                          class="form-control" multiple="multiple"/>
                                            </div>
                                        </div>
                                    </div>
                                </g:if>
                            </g:if>
                            <g:if test="${isForIcsrReport}">
                                <div class="row">
                                    <div class="col-xs-6">
                                        <label><g:message
                                                code="app.label.icsr.profile.conf.senderOrganization"/></label><span
                                            class="required-indicator">*</span>

                                        <div class="destinations">
                                            <g:select name="senderOrganization.id"
                                                      from="${UnitConfiguration.findAllByUnitRetired(false)}"
                                                      noSelection="['': '']"
                                                      optionKey="id" optionValue="unitName"
                                                      class="form-control senderOrg"
                                                      value="${configurationInstance.senderOrganization?.id}"/>
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-xs-6">
                                        <label><g:message
                                                code="app.label.icsr.profile.conf.recipientOrganization"/></label><span
                                            class="required-indicator">*</span>

                                        <div class="destinations">
                                            <g:select name="recipientOrganization.id"
                                                      from="${UnitConfiguration.findAllByUnitRetired(false)}"
                                                      noSelection="['': '']"
                                                      optionKey="id" optionValue="unitName"
                                                      class="form-control recipientOrg"
                                                      value="${configurationInstance.recipientOrganization?.id}"/>
                                        </div>
                                    </div>
                                </div>
                            </g:if>
                            <g:if test="${(configurationInstance instanceof BaseConfiguration) && !(isForIcsrReport)}">
                                <g:showIfDmsServiceActive>
                                    <div class="row" style="margin-top: 5px">
                                        <div class="col-xs-12">
                                            <div id="sendToDmsCheckbox"
                                                 class="checkbox checkbox-primary checkbox-inline">
                                                <g:checkBox id="dmsEnabled"
                                                            class="dmsEnabled"
                                                            name="dmsEnabled"
                                                            checked="${configurationInstance?.dmsConfiguration && !configurationInstance.dmsConfiguration.isDeleted && configurationInstance.dmsConfiguration.format != null}"/>
                                                <label for="dmsEnabled">
                                                    ${message(code: "app.dms.upload.label")}
                                                </label> <span class="glyphicon glyphicon-edit showDmsConfigurationDlg"
                                                               style="cursor: pointer;font-size: 20px;"></span>
                                            </div>

                                            <br>
                                            <g:each var="reportFormatEnum" in="${ReportFormatEnum.emailShareOptions}"
                                                    status="i"
                                                    style="margin-top: 10px;">
                                                <div class="radio radio-inline">
                                                    <g:radio name="dmsConfiguration.format" value="${reportFormatEnum}"
                                                             id="format${reportFormatEnum}"
                                                             checked="${((configurationInstance?.dmsConfiguration?.format == reportFormatEnum) || (!(configurationInstance?.dmsConfiguration?.format) && i == 0))}"/>
                                                    <label for="format${reportFormatEnum}">
                                                        ${message(code: reportFormatEnum.i18nKey)}
                                                    </label>
                                                </div>
                                            </g:each>
                                        </div>
                                    </div>
                                </g:showIfDmsServiceActive>
                                <g:if test="${grailsApplication.config.oneDrive.enabled}">
                                    <div class="col-md-12"  style="border-radius: 5px; border: 1px #cccccc solid; margin-top: 10px; padding-top: 5px;">
                                        <div>
                                            <g:message code="app.label.oneDrive.uploadToOneDrive"/>
                                            <input type="hidden" id="oneDriveFolderId" name="deliveryOption.oneDriveFolderId" value="${configurationInstance?.deliveryOption?.oneDriveFolderId}">
                                            <input type="hidden" id="oneDriveSiteId" name="deliveryOption.oneDriveSiteId" value="${configurationInstance?.deliveryOption?.oneDriveSiteId}">
                                            <input type="hidden" id="oneDriveUserSettings" name="deliveryOption.oneDriveUserSettings" value="${configurationInstance?.deliveryOption?.oneDriveUserSettings?.id}">
                                            <div class="input-group">
                                                <input id="oneDriveFolderName" class="form-control" readonly name="deliveryOption.oneDriveFolderName" value="${configurationInstance?.deliveryOption?.oneDriveFolderName}">
                                                <span class="input-group-btn">
                                                    <button class="btn btn-primary selectOneDrive" type="button"><g:message code="scheduler.select"/></button>
                                                </span>
                                            </div>
                                        </div>

                                        <g:each var="reportFormatEnum" in="${ReportFormatEnum.emailShareOptions}">
                                            <div class="checkbox checkbox-primary checkbox-inline">
                                                <g:checkBox id="oneDriveOption${reportFormatEnum}"
                                                            class="oneDriveOption"
                                                            name="deliveryOption.oneDriveFormats"
                                                            value="${reportFormatEnum}"
                                                            checked="${(configurationInstance?.deliveryOption?.oneDriveFormats && reportFormatEnum in configurationInstance.deliveryOption.oneDriveFormats)}"/>
                                                <label for="oneDriveOption${reportFormatEnum}">
                                                    ${message(code: reportFormatEnum.i18nKey)}
                                                </label>
                                            </div>
                                        </g:each>
                                    </div>
                                </g:if>
                            </g:if>
                        </div>

                    </div>
                </div>

            </div>
        </div>
    </div>
</div>

<g:if test="${!forPvq && (configurationInstance instanceof com.rxlogix.config.Configuration) || (configurationInstance instanceof com.rxlogix.config.PeriodicReportConfiguration)}">
    <script>
        var listTaskTemplateUrl = "${createLink(controller: "taskTemplate", action: "ajaxGetReportTemplates")}";
        var getTaskForTemplateUrl = "${createLink(controller: "taskTemplate", action: "ajaxGetReportTasksForTemplate")}";
        var aggregateReportViewTaskMode = false;
        var isForPeriodicReport = "${isForPeriodicReport}"
        var pageType = "${actionName}"
    </script>
    <asset:javascript src="app/reportTask.js"/>
    <div class="rxmain-container rxmain-container-top">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-${isForPeriodicReport ? 'down' : 'right'} fa-lg click taskContainerIcon" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.deliveryOptions.task.header"
                               default="Action Items To Be Created On Report Execution"/>
                </label>
            </div>

            <div class="rxmain-container-content rxmain-container-${isForPeriodicReport || isForIcsrReport || (configurationInstance instanceof CaseSeries) ? 'show' : 'hide'}"">

                <div class="row">
                    <g:render template="/taskTemplate/includes/reportTaskTable" model="[addButton:true, isForPeriodicReport:isForPeriodicReport]"/>
                    <g:hiddenField id="tasks"  name="tasks" value="${configurationInstance?.getReportTasksAsJson()}" />
                </div>
            </div>
        </div>
    </div>
    <g:set var="qualityService" bean="qualityService"/>
    <g:if test="${isForPeriodicReport}">
    <div class="rxmain-container rxmain-container-top pvpOnly" style="display: none;">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.PublisherTemplate.appName" default="Publisher Template"/>
                    <span class="fa fa-question-circle-o modal-link" style="cursor:pointer" data-toggle="modal" data-target="#publisherHelpModal"></span>
                </label>
            </div>
            <div class="rxmain-container-content rxmain-container-show">
            <g:if test="${showSectionAttachmentWarning}">
                <div class="alert alert-warning alert-dismissible forceLineWrap" role="alert">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <i class="fa fa-warning"></i>
                    <g:message code="app.reportRequest.attachment.warning"/>
                </div>
            </g:if>
                <div class="alert alert-danger alert-dismissible forceLineWrap templateError" role="alert" style="display: none">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <g:message code="app.publisher.section.error"/>
                </div>
            <table width="100%" class="table dataTable templateTable" >
                <thead>
                <tr>
                    <th width="50px">
                        <div class="btn-group" style="margin-left: -15px">


                        <span class="table-add md md-plus dropdown-toggle pv-cross" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true"></span>
                        <ul class="dropdown-menu">
                            <li><a class="click publisherSectionAddTemlate" ><g:message code="app.label.PublisherTemplate.addPublisherTemplateFromLibrary"/></a></li>
                            <li><a class="click publisherSectionAddFile"><g:message code="app.label.PublisherTemplate.attachPublisherTemplate"/></a></li>
                        </ul>
                        </div>
                    </th>
                    <th width="100px"><label><g:message code="app.label.name"/></label></th>
                    <th width="100px"><label><g:message code="app.label.dueInDays"/></label></th>
                    <th width="100px"><label><g:message code="app.label.task.template.appName"/></label></th>
                    <th width="150px"><label><g:message code="app.label.action.item.assigned.to"/></label></th>
                    <th width="150px"><label><g:message code="app.label.reportSubmission.destinations"/></label></th>
                    <th width="*"><label><g:message code="app.label.template"/></label></th>
                </tr>
                </thead>
                <tbody class="publisherSectionsTable">
                <tr class="publisherSectionRowTemplate" style="display: none">
                    <td>
                        <span class='table-remove md md-close publisherSectionRemove pv-cross'></span>
                        <span class='table-add md md-arrow-up publisherSectionUp pv-cross'></span>
                        <span class='table-add md md-arrow-down publisherSectionDown pv-cross'></span>
                        <input class="form-control" name="publisherSectionId" type="hidden" value="0">
                    </td>
                    <td><input class="form-control" name="publisherSectionName" maxlength="${com.rxlogix.config.publisher.PublisherConfigurationSection.constrainedProperties.name.maxSize}" ></td>
                    <td><input class="form-control" type="number" name="pubDueInDays" min="0" max="9999" data-evt-input='{"method": "validateInput", "params": []}'></td>
                    <td><input class="form-control" name="publisherSectionTaskTemplate"></td>
                    <td>
                        <div>
                        <span class="queryTemplateUserGroupLabel"><g:message code="app.label.PublisherTemplate.visibleForAnyUserGroup"/></span> <span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span>
                        <input type="hidden" name="publisherSectionUserGroup" >

                        </div>
                        <div>

                        <g:message code="app.label.PublisherTemplate.author"/>: <span class="authorLabel"></span> <span class="fa fa-edit author" style="cursor: pointer"></span>
                        <input type="hidden" name="author" value="">
                        </div>
                        <div>
                        <g:message code="app.label.PublisherTemplate.reviewer"/>: <span class="reviewerLabel"></span> <span class="fa fa-edit reviewer" style="cursor: pointer"></span>
                        <input type="hidden" name="reviewer" value="">
                        </div>
                        <div>
                        <g:message code="app.label.PublisherTemplate.approver"/>: <span class="approverLabel"></span> <span class="fa fa-edit approver" style="cursor: pointer"></span>
                        <input type="hidden" name="approver" value="">
                        </div>
                    </td>
                    <td>
                        <input name="publisherReportingDestinations" value="" class="form-control publisherReportingDestinations" multiple="multiple">
                    </td>
                    <td>
                        <g:render template="/configuration/includes/publisherParameterValue" model="[publisherParameterName:'publisherSectionTemplate']"/>
                    </td>
                </tr>
                <tr class="publisherSectionRowFile" style="display: none">
                    <td>
                        <span class='table-remove md-close publisherSectionRemove pv-cross' ></span>
                        <span class='table-add md md-arrow-up pv-cross publisherSectionUp' ></span>
                        <span class='table-add md md-arrow-down pv-cross publisherSectionDown' ></span>
                        <input class="form-control" name="publisherSectionId" type="hidden" value="0">
                    </td>
                    <td><input class="form-control" name="publisherSectionName" maxlength="${com.rxlogix.config.publisher.PublisherConfigurationSection.constrainedProperties.name.maxSize}"></td>
                    <td><input class="form-control" type="number" name="pubDueInDays"  min="0" max="9999" data-evt-input='{"method": "validateInput", "params": []}'></td>
                    <td><input class="form-control" name="publisherSectionTaskTemplate"></td>
                    <td>
                        <div>
                        <span class="queryTemplateUserGroupLabel"><g:message code="app.label.PublisherTemplate.visibleForAnyUserGroup"/></span> <span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span>
                        <input type="hidden" name="publisherSectionUserGroup" >
                        </div>
                        <div>

                        <g:message code="app.label.PublisherTemplate.author"/>: <span class="authorLabel"></span> <span class="fa fa-edit author" style="cursor: pointer"></span>
                        <input type="hidden" name="author" value="">
                        </div>
                        <div>
                        <g:message code="app.label.PublisherTemplate.reviewer"/>: <span class="reviewerLabel"></span> <span class="fa fa-edit reviewer" style="cursor: pointer"></span>
                        <input type="hidden" name="reviewer" value="">
                        </div>
                        <div>
                        <g:message code="app.label.PublisherTemplate.approver"/>: <span class="approverLabel"></span> <span class="fa fa-edit approver" style="cursor: pointer"></span>
                        <input type="hidden" name="approver" value="">
                        </div>
                    </td>
                    <td>
                        <input name="publisherReportingDestinations" value="" class="form-control publisherReportingDestinations" multiple="multiple">
                    </td>
                    <td>
                        <g:render template="/configuration/includes/publisherFileSection" model="[publisherParameterName:'publisherSectionTemplate']"/>
                    </td>
                </tr>
                <g:if test="${isForPeriodicReport}">
                <g:each in="${configurationInstance?.publisherConfigurationSections?.sort{it.sortNumber}}" var="section">
                    <tr class="templateTableRow">
                    <td>
                        <span class='table-remove md md-close publisherSectionRemove pv-cross' ></span>
                        <span class='table-add md md-arrow-up pv-cross publisherSectionUp' ></span>
                        <span class='table-add md md-arrow-down pv-cross publisherSectionDown' ></span>
                        <input name="publisherSectionId" value="${section.id}" type="hidden">
                    </td>
                    <td><input class="form-control" name="publisherSectionName" value="${section.name}" maxlength="${com.rxlogix.config.publisher.PublisherConfigurationSection.constrainedProperties.name.maxSize}"></td>
                    <td><input class="form-control" type="number" name="pubDueInDays"  min="0" max="9999" value="${section.dueInDays}" data-evt-input='{"method": "validateInput", "params": []}'></td>
                    <td><input class="form-control" name="publisherSectionTaskTemplate"  value="${section.taskTemplateId}"></td>
                    <td>
                        <div>
                        <g:if test="${section?.assignedToGroup?.id}">
                            <span class="queryTemplateUserGroupLabel"><g:message code="app.label.PublisherTemplate.visibleFor" /> ${UserGroup.get(section?.assignedToGroup?.id).name} <g:message code="app.label.PublisherTemplate.only" /></span> <span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span>
                        </g:if>
                        <g:else>
                            <span class="queryTemplateUserGroupLabel"><g:message code="app.label.PublisherTemplate.visibleForAnyUserGroup"/></span> <span class="fa fa-edit updateQueryTemplateUserGroup" style="cursor: pointer"></span>
                        </g:else>
                            <input type="hidden" name="publisherSectionUserGroup" value="${section?.assignedToGroup?.id}">
                        </div>
                        <div>
                            <g:message code="app.label.PublisherTemplate.author"/>: <span class="authorLabel">${section?.author?.fullName}</span>
                            <span class="fa fa-edit author" style="cursor: pointer"></span>
                            <input type="hidden" name="author" value="${section?.author?.id}">
                        </div>

                        <div>
                            <g:message code="app.label.PublisherTemplate.reviewer"/>: <span class="reviewerLabel">${section?.reviewer?.fullName}</span>
                            <span class="fa fa-edit reviewer" style="cursor: pointer"></span>
                            <input type="hidden" name="reviewer" value="${section?.reviewer?.id}">
                        </div>

                        <div>
                            <g:message code="app.label.PublisherTemplate.approver"/>: <span class="approverLabel">${section?.approver?.fullName}</span>
                            <span class="fa fa-edit approver" style="cursor: pointer"></span>
                            <input type="hidden" name="approver" value="${section?.approver?.id}">
                        </div>
                    </td>
                        <td>
                            <input name="publisherReportingDestinations" value="${section?.destination}" class="form-control publisherReportingDestinations" multiple="multiple">
                        </td>
                    <td>
                        <g:if test="${!section.publisherTemplate}">
                            <g:render template="/configuration/includes/publisherFileSection" model="[publisherParameterName:'publisherSectionTemplate', parameters:section?.parameterValues, filename:section?.filename]"/>
                        </g:if>
                        <g:else>
                            <g:render template="/configuration/includes/publisherParameterValue" model="[publisherParameterName:'publisherSectionTemplate',publisherTemplate:section?.publisherTemplate, parameterValues:section?.parameterValues]"/>
                        </g:else>

                    </td>
                    </tr>
                </g:each>
                </g:if>
                </tbody>
            </table>
            </div>
        </div>
    </div>

    <div class="rxmain-container rxmain-container-top pvpOnly" style="display: none;">
        <div class="rxmain-container-inner">
            <div class="rxmain-container-row rxmain-container-header">
                <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
                <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                    <g:message code="app.label.PublisherTemplate.additionalSources"
                               default="Publisher additional sources"/>
                </label>
            </div>
            <div class="rxmain-container-content rxmain-container-show">
                <g:if test="${isForPeriodicReport}">
                    <g:render template="/periodicReport/includes/configurationAttchment" model="[attachments: configurationInstance?.attachments, showAttachmentWarning: showAttachmentWarning]"/>
                </g:if>
            </div>
        </div>
    </div>
    </g:if>
</g:if>
<g:render template="/configuration/includes/emailConfiguration" model="[emailConfiguration: configurationInstance?.emailConfiguration]"/>
<g:render template="/email/includes/copyPasteEmailModal"/>
<g:render template="/oneDrive/downloadModal" model="[select:true]"/>
<g:render template="/includes/widgets/errorTemplate" model="[messageBody:g.message(code:'app.label.PublisherTemplate.error.field')]"/>
<g:render template="/publisherTemplate/includes/publisherHelp" />
<g:render template="/configuration/includes/publisherQuestModal"/>
<g:showIfDmsServiceActive>
    <g:if test="${configurationInstance instanceof com.rxlogix.config.BaseConfiguration}">
        <g:render template="/includes/widgets/errorTemplate" model="[messageBody:message(code: 'app.dms.config.error'), errorModalId:'dmsErrorModal']"/>
        <g:render template="/configuration/includes/dmsConfiguration"
                  model="[configurationInstance: configurationInstance]"/>
        <div id="dmsConfigContainer">
        </div>
    </g:if>
</g:showIfDmsServiceActive>

<div class="modal fade" id="taskTemplateList" data-keyboard="false" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.deliveryOptions.task.template.list"/></h4>
            </div>

            <div class="modal-body">
                <div style="overflow: auto; height: 200px" id="taskTemplateListContent">
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal" aria-label="Close">
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div></div>
    </div>
</div>

<div class="modal fade" id="addAttachmentModal" data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message
                        code="app.label.additionalAttachments.addAttachment"/></h4>
            </div>

            <div class="modal-body">
                <div class="alert alert-danger alert-dismissible fade in additionalAttachmentsDialogError"
                     id="attachmentModalDialogError" role="alert" style="display: none;">
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <strong><g:message code="app.label.warning"/></strong><g:message
                        code="app.label.additionalAttachmentsDialogError"/></div>

                <g:each var="reportFormatEnum" in="${ReportFormatEnum.emailShareOptions}">
                    <div class="checkbox checkbox-primary checkbox-inline">
                        <input type="checkbox" class="attachmentFormat attachmentFormatModal"
                               id="attachmentFormat${reportFormatEnum}" value="${reportFormatEnum}"/>
                        <label for="attachmentFormat${reportFormatEnum}">
                            ${message(code: reportFormatEnum.i18nKey)}
                        </label>
                    </div>
                </g:each>
                <div id="attachmentModalContent">

                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="saveAttachmentButton" class="btn btn-success">
                    <g:message code="default.button.confirm.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey cancel" id="cancelAttachmentButton" data-dismiss="modal">
                    <g:message code="default.button.cancel.label"/>
                </button>
            </div>
        </div>
    </div>
</div>
<g:render template="/periodicReport/includes/publisherWarningModal"/>
<g:render template="/pvp/includes/composer"/>