<%@ page import="com.rxlogix.util.MiscUtil; com.rxlogix.enums.ReportThemeEnum; grails.util.Holders; com.rxlogix.config.SourceProfile; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study Selection--}%
            <div class="col-md-6">

                <div class=" row">
                    <div class="col-md-6">
                        <label class="labelBold add-margin-bottom">
                            <g:message code="app.label.productSelection"/>
                        </label>
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <g:checkBox name="suspectProduct" data-value="${configurationInstance?.suspectProduct}"
                                        data-edit="${editMode}" value="${configurationInstance?.suspectProduct}"
                                        checked="${configurationInstance?.suspectProduct}"/>
                            <label for="suspectProduct">
                                <g:message code="app.label.SuspectProduct"/>
                            </label>
                        </div>

                        <div class="wrapper">
                            <div id="showProductSelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.productSelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search" data-toggle="modal" data-target="#productModal"
                                   data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"></i>
                            </div>
                        </div>
                        <g:hiddenField name="productSelection" value="${configurationInstance?.productSelection}"/>
                        <g:hiddenField name="productGroupSelection"
                                       value="${configurationInstance?.productGroupSelection}"/>
                    </div>

                    <div class="col-md-6">
                        <label class="labelBold add-margin-bottom ">
                            <g:message code="app.label.studySelection"/>
                        </label>
                        <div style="position:relative; display: inline-block; width: 190px" >&nbsp;</div>
                        <div class="wrapper">
                            <div id="showStudySelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.studySelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search" data-toggle="modal" data-target="#studyModal"></i>
                            </div>
                        </div>
                        <g:hiddenField name="studySelection" value="${configurationInstance?.studySelection}"/>

                    </div>

                    <div class="clearfix"></div>
                </div>


                <div class="row" hidden="hidden">
                    <div class="col-md-12">
                        <!-- TODO: For debugging use only; type="hidden" for this input field after we are done needing to see it -->
                        <input name="JSONExpressionValues" id="JSONExpressionValues" value=""/>
                        <a href="http://jsonlint.com/" target="_blank"><g:message code="prettify.my.json.here"/></a>
                    </div>
                </div>

            %{--Choose Global Query--}%
            <div style="margin-top: 10px;" class="globalqueryWrapperRow">
                <label><g:message code="app.label.chooseAQuery"/></label>

                <div class="row queryContainer">

                    <div class="col-xs-10">
                        <div class="doneLoading" style="padding-bottom: 5px;">
                            <g:select name="globalQuery" readonly="${params.fromTemplate}"
                                           from="${[]}" noSelection="['': message(code: 'select.operator')]"
                                           value="${configurationInstance?.globalQuery?.id}"
                                           data-value="${configurationInstance?.globalQuery?.id}"
                                           class="form-control selectQuery"/>
                        </div>
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

            <div class="row">
                <div class="globalQueryWrapper col-md-12" style="float: left">
                    <div class="queryExpressionValues">
                        <g:if test="${configurationInstance?.globalQueryValueLists?.size() > 0}">
                            <g:each var="qvl" in="${configurationInstance.globalQueryValueLists}">
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


            %{--Evaluate Case Date On--}%

            <div class="col-md-2">
                <div class="${params.fromTemplate ? "hidden" : ""}">
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
                    <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden"
                           value="${configurationInstance?.evaluateDateAs}"/>

                    %{--Date Picker--}%
                    <div style="margin-top: 10px">

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

                <!--<div style="margin-top: 10px;">
                    <label><g:message code="app.icsrReport.referenceProfile.label"/></label>

                    <div>
                        <g:hiddenField name="referenceProfile" value="${configurationInstance?.referenceProfile?.id}"
                                       class="form-control"/>
                    </div>
                </div>-->
            </div>


            %{--Date Range Type--}%
            <div class="col-md-2 ">
                <div class="${params.fromTemplate ? "hidden" : ""}">
                    <label><g:message code="app.label.DateRangeType"/></label>
                    <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                          data-toggle="modal"
                          data-target="#dateRangeTypeHelp"></span>
                    <g:select name="dateRangeType.id" id="dateRangeType"
                              from="${ViewHelper.getDateRangeTypeI18n()}"
                              optionValue="display" optionKey="name"
                              data-value="${configurationInstance?.dateRangeType?.id}"
                              class="form-control"/>
                </div>

                <div>
                    <g:if test="${sourceProfiles.size() > 1}">
                        <div class="${params.fromTemplate ? "hidden" : "m-t-10"}">
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

                %{--Date Picker--}%
                <div style="margin-top: 10px;">
                    <div class="fuelux">
                        <g:render template="/icsrReport/includes/globalDateRange"
                                  model="[configurationInstance: configurationInstance]"/>
                    </div>
                </div>
            </div>

            %{--Inclusion Options--}%
            <div class="col-md-2 ${params.fromTemplate ? "hidden" : ""}">
                <label><g:message code="app.label.inclusionOptions"/></label>

                <div class="checkbox checkbox-primary">
                    <g:checkBox name="includePreviousMissingCases"
                                value="${configurationInstance?.includePreviousMissingCases}"
                                checked="${configurationInstance?.includePreviousMissingCases}"/>
                    <label for="includePreviousMissingCases">
                        <g:message code="reportCriteria.include.previous.missing.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="includeOpenCasesInDraft"
                                name="includeOpenCasesInDraft"
                                value="${configurationInstance?.includeOpenCasesInDraft}"
                                checked="${configurationInstance?.includeOpenCasesInDraft}"/>
                    <label for="includeOpenCasesInDraft">
                        <g:message code="reportCriteria.include.open.cases.in.draft"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeFollowUp"
                                name="excludeFollowUp"
                                value="${configurationInstance?.excludeFollowUp}"
                                checked="${configurationInstance?.excludeFollowUp}"/>
                    <label for="excludeFollowUp">
                        <g:message code="reportCriteria.exclude.follow.up"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="includeAllStudyDrugsCases"
                                name="includeAllStudyDrugsCases"
                                value="${configurationInstance?.includeAllStudyDrugsCases}"
                                checked="${configurationInstance?.includeAllStudyDrugsCases}"/>
                    <label for="includeAllStudyDrugsCases">
                        <g:message code="reportCriteria.include.all.study.drugs.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeNonValidCases"
                                name="excludeNonValidCases"
                                value="${configurationInstance?.excludeNonValidCases}"
                                checked="${configurationInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
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
          model="[multiIngredientValue: configurationInstance?.isMultiIngredient, includeWHODrugValue: configurationInstance?.includeWHODrugs, backgroundColor: backgroundColor, textColor: textColor]"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>
<g:render template="/query/dateRangeTypeHelp"/>
