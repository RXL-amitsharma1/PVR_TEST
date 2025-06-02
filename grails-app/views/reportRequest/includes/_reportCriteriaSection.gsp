<%@ page import="com.rxlogix.util.MiscUtil; com.rxlogix.enums.ReportThemeEnum; com.rxlogix.config.UserDictionary; grails.util.Holders; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            <div class="col-md-7">
                <div class="row">
                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom">
                            <g:message code="app.label.productSelection"/>
                        </label>

                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection reportRequest with-placeholder" placeholder="${message(code:'app.label.productSelection')}"></div>

                            <div class="iconSearch  reportRequest">
                                <div id="productSearchIconLoading" class="loading"><asset:image src="select2-spinner.gif" height="16" width="16"/></div>
                                <i id="productSearchIcon" class="fa fa-search" data-toggle="modal" data-target="#productModal" style="display: none" data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"></i>
                            </div>
                        </div>
                        <g:hiddenField name="productSelection" value="${reportRequestInstance?.productSelection}"/>
                        <g:textField name="productGroupSelection" value="${reportRequestInstance?.productGroupSelection}" hidden="hidden"/>
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <g:checkBox name="suspectProduct" value="${reportRequestInstance?.suspectProduct}"
                                        class="reportRequest" checked="${reportRequestInstance?.suspectProduct}"/>
                            <label for="suspectProduct" class="add-margin-bottom">
                                <g:message code="app.label.SuspectProduct"/>
                            </label>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom ">
                            <g:message code="app.label.studySelection"/>
                        </label>

                        <div class="wrapper">
                            <div id="showStudySelection" class="showDictionarySelection reportRequest with-placeholder" placeholder="${message(code:'app.label.studySelection')}"></div>

                            <div class="iconSearch reportRequest">
                                <i class="fa fa-search" data-toggle="modal" data-target="#studyModal"></i>
                            </div>
                        </div>
                        <g:hiddenField name="studySelection" value="${reportRequestInstance?.studySelection}"/>

                    </div>
                    %{--Event Selection--}%
                    <div class="col-md-4">
                        <label><g:message code="app.label.eventSelection"/></label>

                        <div class="wrapper">
                            <div id="showEventSelection" class="showDictionarySelection reportRequest with-placeholder" placeholder="${message(code:'app.label.eventSelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search reportRequest" id="searchEvents" data-toggle="modal" data-target="#eventModal" data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"></i>
                            </div>
                        </div>
                        <g:textField name="eventSelection" value="${reportRequestInstance?.eventSelection}" hidden="hidden"/>
                        <g:textField name="eventGroupSelection" value="${reportRequestInstance?.eventGroupSelection}" hidden="hidden"/>
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <g:checkBox name="limitPrimaryPath" value="${reportRequestInstance?.limitPrimaryPath}"
                                        checked="${reportRequestInstance?.limitPrimaryPath}"/>
                            <label for="limitPrimaryPath" class="add-margin-bottom">
                                <g:message code="app.label.eventSelection.limit.primary.path"/>
                            </label>
                        </div>
                    </div>

                    <div class="clearfix"></div>
                </div>

            </div>
            %{--Date Range Type--}%
            <div class="col-md-2 ">
                <label><g:message code="app.label.DateRangeType"/></label>
            <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer" data-toggle="modal"
                  data-target="#dateRangeTypeHelp"></span>
            <g:select name="dateRangeType.id" id="dateRangeType"
                          from="${ViewHelper.getDateRangeTypeI18n()}"
                          optionValue="display" optionKey="name"
                          value="${reportRequestInstance?.dateRangeType?.id}"
                          class="form-control reportRequest"/>


                %{--Evaluate Case Date On--}%
                <div style="margin-top: 10px;">
                    <label><g:message code="app.label.EvaluateCaseDateOn" /></label>
                    <div id="evaluateDateAsDiv">
                        <g:select name="evaluateDateAsNonSubmission"
                                  from="${ViewHelper.getEvaluateCaseDateI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${reportRequestInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs reportRequest"/>

                    </div>
                    <div id="evaluateDateAsSubmissionDateDiv">
                        <g:select name="evaluateDateAsSubmissionDate"
                                  from="${ViewHelper.getEvaluateCaseDateSubmissionI18n()}"
                                  optionValue="display" optionKey="name"
                                  value="${reportRequestInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs"/>

                    </div>
                    <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden" value="${reportRequestInstance?.evaluateDateAs}"/>
                %{--Date Picker--}%
                <div style="margin-top: 10px">
                    <div class="fuelux ${hasErrors(bean: reportRequestInstance, field: 'dueDate', 'has-error')}">
                       <div>

                           <div class="datepicker" id="asOfVersionDatePicker">
                            <div class="input-group">
                                <g:textField name="asOfVersionDate"
                                             value="${renderShortFormattedDate(date: reportRequestInstance.asOfVersionDate)}"
                                             placeholder="${message(code:"select.version")}"
                                             class="form-control fuelux reportRequest"/>

                                <g:render id="asOfVersionDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                        </div>
                    </div>
                </div>
                </div>
                %{--Report Start Date and Report End Date--}%
                <div style="margin-top: 10px" class="adhocReportInformation">
                    <label><g:message default="Report Start Date" code="select.report.start.date"/></label>
                    <div class="fuelux">
                        <div class="datepicker reportSelectionDate toolbarInline" id="startDateDiv">
                            <div class="input-group">
                                <g:textField id="startDate" placeholder="${message(code:"placeholder.starteDate.label" )}" class="form-control fuelux date reportRequest" name="startDate" value="${renderShortFormattedDate(date: reportRequestInstance?.startDate)}"/>
                                <g:render id="startDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div style="margin-top: 10px" class="adhocReportInformation">
                    <label><g:message default="Report End Date" code="select.report.end.date"/></label>
                    <div class="fuelux">
                        <div class="datepicker reportSelectionDate toolbarInline" id="endDateDiv">
                            <div class="input-group">
                                <g:textField id="endDate" placeholder="${message(code:"placeholder.endDate.label" )}" class="form-control fuelux date reportRequest" name="endDate" value="${renderShortFormattedDate(date: reportRequestInstance?.endDate)}"/>
                                <g:render id="endDateIn" class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            %{--Inclusion Options--}%
            <div class="col-md-3">

                <label><g:message code="app.label.inclusionOptions"/></label>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeFollowUp" class="reportRequest"
                                name="excludeFollowUp"
                                value="${reportRequestInstance?.excludeFollowUp}"
                                checked="${reportRequestInstance?.excludeFollowUp}"/>
                    <label for="excludeFollowUp">
                        <g:message code="reportCriteria.exclude.follow.up"/>
                    </label>
                </div>

                <div id="lockedVersionOnly" class="checkbox checkbox-primary">
                    <g:checkBox id="includeLockedVersion" class="reportRequest"
                                name="includeLockedVersion"
                                value="${reportRequestInstance?.includeLockedVersion}"
                                checked="${reportRequestInstance?.includeLockedVersion}"/>
                    <label for="includeLockedVersion">
                        <g:message code="reportCriteria.include.locked.versions.only"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="includeAllStudyDrugsCases"
                                name="includeAllStudyDrugsCases"
                                value="${reportRequestInstance?.includeAllStudyDrugsCases}"
                                checked="${reportRequestInstance?.includeAllStudyDrugsCases}"/>
                    <label for="includeAllStudyDrugsCases">
                        <g:message code="reportCriteria.include.all.study.drugs.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeNonValidCases" class="reportRequest"
                                name="excludeNonValidCases"
                                value="${reportRequestInstance?.excludeNonValidCases}"
                                checked="${reportRequestInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeDeletedCases" class="reportRequest"
                            name="excludeDeletedCases"
                            value="${reportRequestInstance?.excludeDeletedCases}"
                            checked="${reportRequestInstance?.excludeDeletedCases}"/>
                    <label for="excludeDeletedCases">
                        <g:message code="reportCriteria.exclude.deleted.cases"/>
                    </label>
                </div>

               <div class="checkbox checkbox-primary checkbox-inline">
                <g:checkBox name="includeMedicallyConfirmedCases" value="${reportRequestInstance?.includeMedicallyConfirmedCases}"
                            checked="${reportRequestInstance?.includeMedicallyConfirmedCases}"/>
                <label for="includeMedicallyConfirmedCases" class="add-margin-bottom">
                    <g:message code="reportCriteria.include.medically.confirm.cases"/>
                </label>
            </div>

            </div>
        </div>
    </div>
</div>

<g:set var="userService" bean="userService"/>
<g:set var="currentUserTheme" value="${ReportThemeEnum.searchByName(userService.getUser().preference.theme)}"/>
<g:set var="backgroundColor" value="${MiscUtil.colorToHex(currentUserTheme.columnHeaderBackgroundColor)}"/>
<g:set var="textColor" value="${MiscUtil.colorToHex(currentUserTheme.columnHeaderForegroundColor)}"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"
          model="[multiIngredientValue: reportRequestInstance?.isMultiIngredient, includeWHODrugValue: reportRequestInstance?.includeWHODrugs, backgroundColor: backgroundColor, textColor: textColor]"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>
<g:render template="/query/dateRangeTypeHelp"/>

