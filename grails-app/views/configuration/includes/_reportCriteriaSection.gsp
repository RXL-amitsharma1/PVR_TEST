<%@ page import="com.rxlogix.util.MiscUtil; com.rxlogix.enums.ReportThemeEnum; grails.util.Holders;com.rxlogix.enums.DateRangeEnum; com.rxlogix.config.SourceProfile; com.rxlogix.config.ExecutedCaseSeries; com.rxlogix.config.DateRangeType; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper;" %>
<g:if test="${sourceProfiles.size() > 1}">
    <script>
        $("#sourceProfile").select2();
    </script>
</g:if>
<style>
    .float-left{
        float:left;
        margin-right: 20px !important;
    }
</style>
<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study Selection--}%
            <div class="col-md-${configurationInstance?.pvqType?"12":"7"}">
                <div class="row p-t-0 ${configurationInstance?.pvqType?"hidden":""} ">
                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom" style=" width: 130px">
                            <g:message code="app.label.productSelection"/>
                        </label>
                        <div class="checkbox checkbox-primary checkbox-inline" style=" width: 205px">
                            <g:checkBox name="suspectProduct" data-value="${configurationInstance?.suspectProduct}" data-edit="${editMode}" value="${configurationInstance?.suspectProduct}"
                                        checked="${configurationInstance?.suspectProduct}"/>
                            <label for="suspectProduct" class="add-margin-bottom">
                                <g:message code="app.label.SuspectProduct"/>
                            </label>
                        </div>
                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.productSelection')}"></div>

                            <div class="iconSearch">
                                <div id="productSearchIconLoading" class="loading"><asset:image src="select2-spinner.gif" height="16" width="16"/></div>
                                <i id="productSearchIcon" class="fa fa-search" data-toggle="modal" data-target="#productModal" style="display: none" data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"></i>
                            </div>
                        </div>
                        <g:hiddenField name="productSelection" value="${configurationInstance?.productSelection}"/>
                        <g:textField name="productGroupSelection" value="${configurationInstance?.productGroupSelection}" hidden="hidden"/>
                    </div>

                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom " style=" width: 115px">
                            <g:message code="app.label.studySelection"/>
                        </label>
                        <div style="position:relative; display: inline-block; width: 205px" >&nbsp;</div>
                        <div class="wrapper">
                            <div id="showStudySelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.studySelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search" data-toggle="modal" data-target="#studyModal"></i>
                            </div>
                        </div>
                        <g:hiddenField name="studySelection" value="${configurationInstance?.studySelection}"/>

                    </div>
                    %{--Event Selection--}%
                    <div class="col-md-4">
                        <label  class="labelBold add-margin-bottom " style=" width: 115px"><g:message code="app.label.eventSelection"/></label>
                    <div class="checkbox checkbox-primary checkbox-inline " style=" width: 210px;${params.fromTemplate ? "visibility: hidden;" : ""} ">
                        <g:checkBox name="limitPrimaryPath" value="${configurationInstance?.limitPrimaryPath}"
                                    checked="${configurationInstance?.limitPrimaryPath}"/>
                        <label for="limitPrimaryPath" class="add-margin-bottom">
                            <g:message code="app.label.eventSelection.limit.primary.path"/>
                        </label>
                    </div>
                        <div class="wrapper">
                            <div id="showEventSelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.eventSelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search" id="searchEvents" data-toggle="modal" data-target="#eventModal" data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"></i>
                            </div>
                        </div>
                        <g:textField name="eventSelection" value="${configurationInstance?.eventSelection}" hidden="hidden"/>
                        <g:textField name="eventGroupSelection" value="${configurationInstance?.eventGroupSelection}" hidden="hidden"/>

                    </div>

                    <div class="clearfix"></div>
                </div>

                <div class="row" hidden="hidden">
                    <div class="col-md-12">
                        <!-- TODO: For debugging use only; type="hidden" for this input field after we are done needing to see it -->
                        <input name="JSONExpressionValues" id="JSONExpressionValues" value=""/>
                        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer"><g:message code="prettify.my.json.here"/></a>
                    </div>
                </div>
                <div class="row" id = "globalQueryInfo">
                    <div class="col-md-${configurationInstance?.pvqType?"12":"7"}">
                        <div style="margin-top: 6px;" class="globalqueryWrapperRow">
                            <label><g:message code="app.label.chooseAQuery"/></label>

                            <div class="row queryContainer p-t-0">
                                <div>
                                    <i class="fa fa-refresh fa-spin loading"></i>
                                </div>

                                <div class="globalQueryDiv col-xs-${configurationInstance?.pvqType?"9":"11"}">
                                    <div class="doneLoading" style="padding-bottom: 5px;">
                                        <g:select hidden="hidden" name="globalQuery" from="${[]}" noSelection="['': message(code: 'select.operator')]"
                                                       data-value="${configurationInstance?.globalQuery?.id}"
                                                       class="form-control selectQuery"/></div>

                                </div>

                                <div>
                                    <div class="pull-left"><a
                                            href="${configurationInstance?.globalQuery?.id ? createLink(controller: 'query', action: 'view', id: configurationInstance?.globalQuery?.id) : '#'}"
                                            title="${message(code: 'app.label.viewQuery')}" target="_blank"
                                            class="templateQueryIcon pv-ic queryViewButton glyphicon glyphicon-info-sign ${configurationInstance?.globalQuery?.id ? '' : 'hide'}"></a>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                    <div class="col-md-5">
                        <div style="margin-top: 6px" class="${params.fromTemplate || configurationInstance?.pvqType ? "hidden" : ""}">
                            <label><g:message code="app.label.useCaseSeries"/></label>
                            <div>
                                <div style="float: left; width: 90%">
                                    <select name="useCaseSeries" id="useCaseSeries" data-value="${configurationInstance?.useCaseSeries?.id}" class="form-control"></select>
                                </div>
                                <div class="viewCaseLink">
                                    <i class="md md-lg md-launch"></i>
                                </div>
                                <div class="clearfix"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="globalQueryWrapper col-md-12" id="globalQueryWrapper">
                        <div class="queryExpressionValues">
                            <g:if test="${configurationInstance?.globalQueryValueLists?.size() > 0}">
                                <g:each var="qvl" in="${configurationInstance?.globalQueryValueLists}">
                                %{--<div>${qvl.query.name}:${qvl.id}</div>--}%
                                    <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                        <g:if test="${qev?.hasProperty('reportField')}">
                                            <g:render template='/query/toAddContainerQEV' model="['qev': qev, 'i': i, 'j': j]"/>
                                        </g:if>
                                        <g:else>
                                            <g:render template='/query/customSQLValue' model="['qev': qev, 'i': i, 'j': j]"/>
                                        </g:else>
                                    </g:each>
                                </g:each>
                            </g:if>

                        </div>
                        <g:hiddenField class="validQueries" name="validQueries"
                                       value="${configurationInstance.queriesIdsAsString}"/>
                    </div>
                </div>
            </div>



            <div class="col-md-${configurationInstance?.pvqType?"12":"2"}">
                %{--Evaluate Case Date On--}%
                <div class="${configurationInstance?.pvqType?"col-md-3 m-t-10":""}">
                    <label><g:message code="app.label.EvaluateCaseDateOn"/></label>
                    <div id="evaluateDateAsDiv">
                        <g:select name="evaluateDateAsNonSubmission"
                                  from="${[]}"
                                  data-value="${configurationInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs"
                                  disabled="${params.fromTemplate}"/>

                    </div>
                    <div id="evaluateDateAsSubmissionDateDiv">

                        <g:select name="evaluateDateAsSubmissionDate"
                                  from="${[]}"
                                  data-value="${configurationInstance?.evaluateDateAs}"
                                  class="form-control evaluateDateAs"
                                  disabled="${params.fromTemplate}"/>

                    </div>
                    <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden" value="${configurationInstance?.evaluateDateAs}"/>

                    %{--Date Picker--}%
                    <div style="margin-top: 10px" class="fuelux-date-picker-box">

                        <div class="fuelux">
                            <div class="datepicker" id="asOfVersionDatePicker">
                                <div class="input-group">
                                    <g:textField name="asOfVersionDate"
                                                 value="${renderShortFormattedDate(date: configurationInstance?.asOfVersionDate)}"
                                                 placeholder="${message(code: "select.version")}"
                                                 class="form-control"/>

                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                %{--Date Range Type--}%
                <div class="${params.fromTemplate ? "hidden" : "m-t-10"} ${configurationInstance?.pvqType?"col-md-3":""}">
                    <label><g:message code="app.label.DateRangeType"/></label>
                    <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                          data-toggle="modal"
                          data-target="#dateRangeTypeHelp"></span>
                    <g:select name="dateRangeType.id" id="dateRangeType"
                              from="${[]}" data-value="${configurationInstance?.dateRangeType?.id}"
                              class="form-control"/>
                </div>

                <div class="${params.fromTemplate ? "" : "m-t-10"} ${configurationInstance?.pvqType?"col-md-3":""}">
                    <div class="fuelux">
                        <g:render template="/periodicReport/includes/globalDateRange"
                                  model="[configurationInstance: configurationInstance]"/>
                    </div>
                </div>

            %{--Data Source--}%
                <g:if test="${sourceProfiles.size() > 1}">
                    <div class="${params.fromTemplate ? "hidden" : "m-t-10"} ${configurationInstance?.pvqType?"col-md-3":""}">
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

            %{--Inclusion Options--}%
            <div class="col-md-${configurationInstance?.pvqType? "12" : "3"} ${params.fromTemplate? "hidden" : ""}">

                <label class="${configurationInstance?.pvqType? "hidden" : ""}"><g:message code="app.label.inclusionOptions"/></label>

                <div class="checkbox checkbox-primary ${configurationInstance?.pvqType? "float-left" : ""}">
                    <g:checkBox id="excludeFollowUp"
                                name="excludeFollowUp"
                                value="${configurationInstance?.excludeFollowUp}"
                                checked="${configurationInstance?.excludeFollowUp}"
                                data-evt-change='{"method": "showHideIncludeNonSignificantFollowUpCheckBox", "params": []}'/>
                    <label for="excludeFollowUp">
                        <g:message code="reportCriteria.exclude.follow.up"/>
                    </label>
                </div>

                <div id="lockedVersionOnly" class="checkbox checkbox-primary ${configurationInstance?.pvqType? "float-left" : ""}">
                    <g:checkBox id="includeLockedVersion"
                                name="includeLockedVersion"
                                value="${configurationInstance?.includeLockedVersion}"
                                checked="${configurationInstance?.includeLockedVersion}"/>
                    <label for="includeLockedVersion">
                        <g:message code="reportCriteria.include.locked.versions.only"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary ${configurationInstance?.pvqType? "float-left" : ""}">
                    <g:checkBox id="includeAllStudyDrugsCases"
                                name="includeAllStudyDrugsCases"
                                value="${configurationInstance?.includeAllStudyDrugsCases}"
                                checked="${configurationInstance?.includeAllStudyDrugsCases}"/>
                    <label for="includeAllStudyDrugsCases">
                        <g:message code="reportCriteria.include.all.study.drugs.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary ${configurationInstance?.pvqType? "float-left" : ""}">
                    <g:checkBox id="excludeNonValidCases"
                                name="excludeNonValidCases"
                                value="${configurationInstance?.excludeNonValidCases}"
                                checked="${configurationInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeDeletedCases"
                                name="excludeDeletedCases"
                                value="${configurationInstance?.excludeDeletedCases}"
                                checked="${configurationInstance?.excludeDeletedCases}"/>
                    <label for="excludeDeletedCases">
                        <g:message code="reportCriteria.exclude.deleted.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary ${configurationInstance?.pvqType? "float-left" : ""}">
                    <g:checkBox id="includeMedicallyConfirmedCases"
                                name="includeMedicallyConfirmedCases"
                                value="${configurationInstance?.includeMedicallyConfirmedCases}"
                                checked="${configurationInstance?.includeMedicallyConfirmedCases}"/>
                    <label for="includeMedicallyConfirmedCases">
                        <g:message code="reportCriteria.include.medically.confirm.cases"/>
                    </label>
                </div>

            <div class="checkbox checkbox-primary ${configurationInstance?.pvqType? "float-left" : ""}">
                <g:checkBox id="includeNonSignificantFollowUp"
                            name="includeNonSignificantFollowUp"
                            value="${configurationInstance?.includeNonSignificantFollowUp}"
                            checked="${configurationInstance?.includeNonSignificantFollowUp}"
                            data-evt-change='{"method": "showHideExcludeFollowUpCheckBox", "params": []}'/>
                <label for="includeNonSignificantFollowUp">
                    <g:message code="reportCriteria.include.non.significant.followup.cases"/>
                </label>
            </div>
            </div>
        </div>

        <g:if test="${actionName == 'edit'}">
            <div class="row" >
                <div class="col-md-8">
                    <div id="fillInQuery" hidden="hidden">
                        <div id="showBlankExpressions" class="queryExpressionValues"></div>

                        <div hidden="hidden"><g:render template="/query/toAddContainer"
                                                       model="[reportConfiguration: true, currentUser: currentUser]"/></div>
                    </div>
                </div>
            </div>
        </g:if>

    </div>
</div>

<g:set var="currentUserTheme" value="${ReportThemeEnum.searchByName(currentUser.preference.theme)}"/>
<g:set var="backgroundColor" value="${MiscUtil.colorToHex(currentUserTheme.columnHeaderBackgroundColor)}"/>
<g:set var="textColor" value="${MiscUtil.colorToHex(currentUserTheme.columnHeaderForegroundColor)}"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"
          model="[multiIngredientValue: configurationInstance?.isMultiIngredient, includeWHODrugValue: configurationInstance?.includeWHODrugs, backgroundColor: backgroundColor, textColor: textColor]"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>

<g:render template="/query/dateRangeTypeHelp"/>

