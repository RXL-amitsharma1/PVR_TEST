<%@ page import="com.rxlogix.Constants; com.rxlogix.enums.UnitTypeEnum; com.rxlogix.enums.IcsrProfileSubmissionDateOptionEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.IcsrProfileDueDateAdjustmentEnum; com.rxlogix.enums.IcsrProfileDueDateOptionsEnum; com.rxlogix.config.SourceProfile; com.rxlogix.user.User; com.rxlogix.config.IcsrOrganizationType; com.rxlogix.config.Configuration; com.rxlogix.config.UnitConfiguration" %>

<div class="rxmain-container rxmain-container-top">
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.getUser()}"/>
    <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: currentUser.id}"/>

    <div class="rxmain-container-row rxmain-container-header">
        <label class="rxmain-container-header-label">
            <g:message code="app.label.icsr.profile.conf.basic.information"/>
        </label>
    </div>

    <div class="rxmain-container-content rxmain-container-show">
        <g:set var="unitConfigurationList" value="${UnitConfiguration.findAllByUnitRetired(false)}"/>
        <g:set var="icsrOrganizationTypeList" value="${IcsrOrganizationType.getAll()}"/>
        <g:set var="userList" value="${User.findAllByEnabled(true, [sort: 'username', order: 'asc'])}"/>
        <div class="row form-group">
            <div class="col-md-2">
                <label for="reportName" style="font-size: 13px;"><g:message
                        code="app.label.icsr.profile.conf.icsrConfigurationName"/><span
                        class="required-indicator">*</span></label>
                <g:textField name="reportName" id="reportName" value="${configurationInstance?.reportName}"
                             maxlength="${configurationInstance.constrainedProperties.reportName.maxSize}" class="form-control "/>
            </div>

            <div class="col-md-2">
                <label for="recipientOrganization.id"><g:message
                        code="app.label.icsr.profile.conf.recipientOrganization"/> <span
                        class="required-indicator">*</span></label>
                <g:select name="recipientOrganization.id"
                          from="${unitConfigurationList.findAll {
                              it.unitType in [UnitTypeEnum.RECIPIENT, UnitTypeEnum.BOTH]
                          }}"
                          optionKey="id" optionValue="unitName"
                          noSelection="['': '']"
                          value="${configurationInstance?.recipientOrganization?.id}"
                          class="form-control select2-box recipientOrg"/>

            </div>

            <div class="col-md-2">
                <label for="recipientType"><g:message code="app.label.icsr.profile.conf.recipientType"/></label>
                <g:textField name="recipientType" id="recipientType" class="form-control" disabled=""/>
            </div>

            <div class="col-md-2">
                <label for="recipientCountry"><g:message code="app.label.icsr.profile.conf.recipientCountry"/></label>
                <g:textField name="recipientCountry" id="recipientCountry" class="form-control" disabled="disabled"/>
            </div>

            <div class="col-md-2">
                <label for="senderOrganization.id"><g:message
                        code="app.label.icsr.profile.conf.senderOrganization"/><span class="required-indicator">*</span>
                </label>
                <g:select name="senderOrganization.id"
                          from="${unitConfigurationList.findAll {
                              it.unitType in [UnitTypeEnum.SENDER, UnitTypeEnum.BOTH]
                          }}" optionKey="id"
                          optionValue="unitName"
                          noSelection="['': '']"
                          value="${configurationInstance?.senderOrganization?.id}"
                          class="form-control select2-box senderOrg"/>
            </div>

            <div class="col-md-2">
                <label for="senderType"><g:message code="app.label.icsr.profile.conf.senderType"/></label>
                <g:textField name="senderType" class="form-control" disabled=""/>

            </div>
        </div>

        <div class="row form-group">
            <div class="col-md-2">
                <div class="hidden">
                    <g:if test="${sourceProfiles.size() > 1}">
                        <div class="${params.fromTemplate ? "hidden" : ""}">
                            <label><g:message code="userGroup.source.profiles.label"/></label>
                            <g:select name="sourceProfile.id" id="sourceProfile"
                                      from="${sourceProfiles}"
                                      optionValue="sourceName" optionKey="id"
                                      value="${configurationInstance?.sourceProfile?.id ?: SourceProfile?.central?.id}"
                                      class="form-control"/>
                        </div>
                    </g:if>
                    <g:else>
                        <g:hiddenField name="sourceProfile.id"
                                       value="${configurationInstance?.sourceProfile?.id ?: sourceProfiles.first()?.id}"/>
                    </g:else>
                </div>
            </div>
            <div class="col-md-2">
                <div class="hidden">
                    <label><g:message code="userGroup.field.profile.label"/></label>
                    <g:select name="fieldProfile.id" id="fieldProfile"
                              from="${fieldProfiles}" noSelection="['': '']"
                              optionValue="name" optionKey="id"
                              value="${configurationInstance?.fieldProfile?.id}"
                              class="form-control select2-box"/>
                </div>
            </div>
        </div>
        <div class="row form-group">
            <div class="col-md-2">

                <div class="checkbox checkbox-primary">
                    <g:checkBox name="autoScheduling" class=""
                                value="${configurationInstance?.autoScheduling}"/>
                    <label for="autoScheduling">
                        <g:message code="app.label.icsr.profile.conf.autoScheduling"/>
                    </label>
                </div>
            </div>
            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="autoGenerate" id="autoGenerate" class=""
                                value="${configurationInstance?.autoGenerate}"/>
                    <label for="autoGenerate">
                        <g:message code="app.label.icsr.profile.conf.autoGenerate"/>
                    </label>
                </div>
            </div>
            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="autoTransmit" class=""
                                value="${configurationInstance?.autoTransmit}"/>
                    <label for="autoTransmit">
                        <g:message code="app.label.icsr.profile.conf.autoTransmit"/>
                    </label>
                </div>
            </div>
            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="autoSubmit" id="autoSubmit" class=""
                                value="${configurationInstance?.autoSubmit}"/>
                    <label for="autoSubmit">
                        <g:message code="app.label.icsr.profile.conf.autoSubmit"/>
                    </label>
                </div>
            </div>
            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="localCpRequired" id="localCpRequired" class="" disabled="${configurationInstance?.autoGenerate}"
                                value="${configurationInstance?.localCpRequired}"/>
                    <label for="localCpRequired">
                        <g:message code="app.label.icsr.profile.conf.localCpRequired"/>
                    </label>
                </div>
            </div>
            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="manualScheduling" id="manualScheduling" class=""
                                value="${configurationInstance?.manualScheduling}"/>
                    <label for="manualScheduling">
                        <g:message code="app.label.icsr.profile.conf.manualScheduling"/>
                    </label>
                </div>
            </div>
        </div>
        <div class="row form-group">

            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="autoScheduleFUPReport" id="autoScheduleFUPReport" class=""
                                value="${configurationInstance?.autoScheduleFUPReport}"/>
                    <label for="autoScheduleFUPReport">
                        <g:message code="app.label.icsr.profile.conf.autoScheduleFUPReport"/>
                    </label>
                </div>
            </div>
            %{--<div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="deviceReportable" id="deviceReportable" class=""
                                value="${configurationInstance?.deviceReportable}"/>
                    <label for="deviceReportable">
                        <g:message code="app.label.icsr.profile.conf.deviceReportable"/>
                    </label>
                </div>
            </div>--}%

            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="adjustDueDate" id="adjustDueDate" class=""
                                value="${configurationInstance?.adjustDueDate}"/>
                    <label for="adjustDueDate">
                        <g:message code="app.label.icsr.profile.adjustDueDate"/>
                    </label>
                </div>
            </div>

            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="isJapanProfile" id="isJapanProfile" class=""
                                value="${configurationInstance?.isJapanProfile}"/>
                    <label for="isJapanProfile">
                        <g:message code="app.label.icsr.profile.conf.japan.profile"/>
                    </label>
                </div>
            </div>

            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="awareDate" id="awareDate" class="" disabled="${configurationInstance?.isJapanProfile ? false : true}"
                                value="${configurationInstance?.awareDate}"/>
                    <label for="awareDate">
                        <g:message code="app.label.icsr.profile.conf.japanAwareDate"/>
                    </label>
                </div>
            </div>

            %{--<div class="col-md-2" style="">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="multipleReport" id="multipleReport" class=""
                                value="${configurationInstance?.multipleReport}"/>
                    <label for="multipleReport">
                        <g:message code="app.label.icsr.profile.conf.multipleReporting"/>
                    </label>
                </div>
            </div>--}%

            <div class="col-md-2">
                <div class="checkbox checkbox-primary">
                    <g:checkBox name="includeOpenCases" id="includeOpenCases" class="" disabled="${configurationInstance?.autoGenerate}"
                                value="${configurationInstance?.includeOpenCases}"/>
                    <label for="includeOpenCases">
                    <g:message code="app.label.icsr.profile.conf.includeOpenCases"/>
                    </label>
                </div>
            </div>

            <div class="col-md-2">

                <div class="checkbox checkbox-primary">
                    <g:checkBox name="isDisabled" class=""
                                value="${configurationInstance?.isDisabled}"/>
                    <label for="isDisabled">
                        <g:message code="app.label.icsr.profile.conf.disabled"/>
                    </label>
                </div>
            </div>
        </div>

        <div class="row form-group">
            <div id="ruleEvaluationDiv" class="col-md-2">
                <label><g:message code="app.label.icsr.profile.conf.iscr.product.evaluation"/></label>
                <g:select name="ruleEvaluation" id="ruleEvaluation"
                          from="${[]}" data-value="${configurationInstance?.ruleEvaluation}"
                          noSelection="['': message(code: 'select.one')]"
                          class="form-control select2-box"></g:select>
            </div>
            <div id="submissionDateFromDiv1" class="col-md-2" style="display: none">
                <div  style="display: block">
                    <label><g:message code="app.label.icsr.profile.conf.iscr.submission.date"/></label>
                    <g:select name="submissionDateFrom1" id="submissionDateFrom1"
                              from="${IcsrProfileSubmissionDateOptionEnum.asList}" noSelection="['': '']"
                              optionValue="name" optionKey="id"
                              data-evt-change='{"method": "updateHiddenField", "params": []}'
                              class="form-control select2-box"/>
                </div>
            </div>

            <div id="submissionDateFromDiv2" class="col-md-2" style="display: none">
                <div  style="display: block">
                    <label><g:message code="app.label.icsr.profile.conf.iscr.submission.date"/></label>
                    <g:select name="submissionDateFrom2" id="submissionDateFrom2"
                              from="${IcsrProfileSubmissionDateOptionEnum.ACKRecord}" noSelection="['': '']"
                              optionValue="name" optionKey="id"
                              data-evt-change='{"method": "updateHiddenField", "params": []}'
                              class="form-control select2-box"/>
                </div>
            </div>

            <g:hiddenField name="submissionDateFrom" id="submissionDateFrom" value="${configurationInstance?.submissionDateFrom}" data-original-value="${configurationInstance?.submissionDateFrom}" />

            <div class="col-md-2">
                <div id="calendarName" style="display: none">

                    <label class="add-margin-bottom"><g:message
                            code="app.label.icsr.profile.calendar"/></label>

                    <select class="calenderNameControl form-control" id="calenderNameControl" name="calenderNameControl" data-value="${calendarValue}" value="${calendarValue ? 'dummy': ''}" style="min-width: 100px;" ></select>
                </div>
            </div>


            <div class="col-md-2">
                <div id="dueDateOptionsDiv" style="display: none">

                    <label><g:message code="app.label.due.Date.Options"/></label>
                    <g:select name="dueDateOptionsEnum" id="dueDateOptionsEnum"
                              from="${IcsrProfileDueDateOptionsEnum.asList}" noSelection="['': '']"
                              optionValue="name" optionKey="id"
                              value="${configurationInstance?.dueDateOptionsEnum}"
                              class="form-control select2-box"/>
                </div>
            </div>
            <div class="col-md-2">
                <div id="dueDateAdjustmentDiv" style="display: none">

                    <label><g:message code="app.label.due.Date.Adjustment"/></label>
                    <g:select name="dueDateAdjustmentEnum" id="dueDateAdjustmentEnum"
                              from="${IcsrProfileDueDateAdjustmentEnum.asList}" noSelection="['': '']"
                              optionValue="name" optionKey="id"
                              value="${configurationInstance?.dueDateAdjustmentEnum}"
                              class="form-control select2-box"/>
                </div>
            </div>
            <div class="col-md-2">
                <div>
                    <g:render template="/includes/widgets/tenantDropDownTemplate"
                              model="[configurationInstance: configurationInstance]"/>
                </div>
            </div>
        </div>
    </div>
</div>
