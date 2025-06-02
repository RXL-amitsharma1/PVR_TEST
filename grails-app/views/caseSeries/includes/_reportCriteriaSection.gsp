<%@ page import="com.rxlogix.util.MiscUtil; com.rxlogix.enums.ReportThemeEnum; grails.util.Holders; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>

<div class="row">
    <div class="col-md-12">
        <div class="row">

            %{--Product Selection/Study Selection--}%
            <div class="col-md-8">

                <div class=" row p-t-0">
                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom" style="width: 130px">
                            <g:message code="app.label.productSelection"/>
                        </label>

                        <div class="checkbox checkbox-primary checkbox-inline">
                            <g:checkBox name="suspectProduct" data-value="${seriesInstance?.suspectProduct}"
                                        data-edit="${editMode}" value="${seriesInstance?.suspectProduct}"
                                        checked="${seriesInstance?.suspectProduct}"/>
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
                        <g:hiddenField name="productSelection" value="${seriesInstance?.productSelection}"/>
                        <g:textField name="productGroupSelection" value="${seriesInstance?.productGroupSelection}"
                                     hidden="hidden"/>
                    </div>

                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom " style="width: 115px">
                            <g:message code="app.label.studySelection"/>
                        </label>
                        <div style="position:relative; display: inline-block; width: 190px" >&nbsp;</div>
                        <div class="wrapper">
                            <div id="showStudySelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.studySelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search" data-toggle="modal" data-target="#studyModal"></i>
                            </div>
                        </div>
                        <g:hiddenField name="studySelection" value="${seriesInstance?.studySelection}"/>

                    </div>


                    <div class="col-md-4">
                        <label class="labelBold add-margin-bottom " style=" width: 115px"><g:message
                                code="app.label.eventSelection"/></label>
                        <div style="position:relative; display: inline-block; width: 190px" >&nbsp;</div>
                        <div class="wrapper">
                            <div id="showEventSelection" class="showDictionarySelection with-placeholder" placeholder="${message(code:'app.label.eventSelection')}"></div>

                            <div class="iconSearch">
                                <i class="fa fa-search" id="searchEvents" data-toggle="modal" data-target="#eventModal"
                                   data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"></i>
                            </div>
                        </div>
                        <g:textField name="eventSelection" value="${seriesInstance?.eventSelection}" hidden="hidden"/>
                        <g:textField name="eventGroupSelection" value="${seriesInstance?.eventGroupSelection}"
                                     hidden="hidden"/>

                    </div>

                    <div class="clearfix"></div>
                </div>


                <div class="row" hidden="hidden">
                    <div class="col-md-12">
                        <!-- TODO: For debugging use only; type="hidden" for this input field after we are done needing to see it -->
                        <input name="JSONExpressionValues" id="JSONExpressionValues" value=""/>
                        <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer"><g:message
                                code="prettify.my.json.here"/></a>
                    </div>
                </div>

                %{--Choose Global Query--}%
                <div style="margin-top: 10px;" class="globalqueryWrapperRow">
                    <label>
                        <g:message code="app.label.chooseAQuery"/>
                        <g:hiddenField name="globalQueryBlanks" id="globalQueryBlanks" value="${globalQueryBlanks}"/>
                    </label>

                    <div class="row queryContainer p-t-0">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>

                        <div class="col-xs-8">
                            <div class="doneLoading" style="padding-bottom: 5px;">
                                <g:select type="hidden" name="globalQuery" from="${[]}" noSelection="['': message(code: 'select.operator')]"
                                               data-value="${seriesInstance?.globalQuery?.id}"
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
                <div class="globalQueryWrapper col-md-10" style="float: left">
                    <div class="queryExpressionValues">
                        <g:if test="${seriesInstance?.globalQueryValueLists?.size()}">
                            <g:each var="qvl" in="${seriesInstance.globalQueryValueLists}">
                                <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                    <g:if test="${qev?.hasProperty('reportField')}">
                                        <g:render template='/query/toAddContainerQEV' model="['qev': qev, 'i':i, 'j':j]"/>
                                    </g:if>
                                    <g:else>
                                        <g:render template='/query/customSQLValue' model="['qev': qev, 'i':i, 'j':j]"/>
                                    </g:else>
                                </g:each>
                            </g:each>
                        </g:if>

                    </div>
                    <g:hiddenField class="validQueries" name="validQueries"
                                   value="${seriesInstance?.queriesIdsAsString}" />
                </div>
            </div>

            </div>

            <div class="col-md-2">
            %{--Evaluate Case Date On--}%
            <label><g:message code="app.label.EvaluateCaseDateOn"/></label>

            <div id="evaluateDateAsDiv">
                <g:select name="evaluateDateAsNonSubmission"
                          from="${ViewHelper.getEvaluateCaseDateI18n()}"
                          optionValue="display" optionKey="name"
                          value="${seriesInstance?.evaluateDateAs}"
                          class="form-control evaluateDateAs"/>

            </div>

            <div id="evaluateDateAsSubmissionDateDiv">

                <g:select name="evaluateDateAsSubmissionDate"
                          from="${ViewHelper.getEvaluateCaseDateSubmissionI18n()}"
                          optionValue="display" optionKey="name"
                          value="${seriesInstance?.evaluateDateAs}"
                          class="form-control evaluateDateAs"/>

            </div>
            <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden"
                   value="${seriesInstance?.evaluateDateAs}"/>

            %{--Date Picker--}%
            <div style="margin-top: 10px">

                <div class="fuelux">
                    <div class="datepicker" id="asOfVersionDatePicker">
                        <div class="input-group">
                            <g:textField name="asOfVersionDate" placeholder="${message(code: "select.version")}"
                                         class="form-control"
                                         value="${renderShortFormattedDate(date: seriesInstance?.asOfVersionDate)}"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>

                </div>
            </div>

            %{--Date Range Type--}%
                <label><g:message code="app.label.DateRangeType"/></label>
                <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer" data-toggle="modal"
                      data-target="#dateRangeTypeHelp"></span>
                <g:select name="dateRangeType.id" id="dateRangeType"
                          from="${ViewHelper.getDateRangeTypeI18n()}"
                          optionValue="display" optionKey="name"
                          value="${seriesInstance?.dateRangeType?.id}"
                          class="form-control"/>


                %{--Date Picker--}%
                <div style="margin-top: 10px;">
                    <div class="fuelux">
                        <g:render template="includes/globalDateRange"
                                  model="[dateRangeInformation: seriesInstance.caseSeriesDateRangeInformation]"/>
                    </div>
                </div>
            </div>

            %{--Inclusion Options--}%
            <div class="col-md-2">
                <label><g:message code="app.label.inclusionOptions"/></label>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeFollowUp"
                                name="excludeFollowUp"
                                value="${seriesInstance?.excludeFollowUp}"
                                checked="${seriesInstance?.excludeFollowUp}"/>
                    <label for="excludeFollowUp">
                        <g:message code="reportCriteria.exclude.follow.up"/>
                    </label>
                </div>

                <div id="lockedVersionOnly" class="checkbox checkbox-primary">
                    <g:checkBox id="includeLockedVersion"
                                name="includeLockedVersion"
                                value="${seriesInstance?.includeLockedVersion}"
                                checked="${seriesInstance?.includeLockedVersion}"/>
                    <label for="includeLockedVersion">
                        <g:message code="reportCriteria.include.locked.versions.only"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="includeAllStudyDrugsCases"
                                name="includeAllStudyDrugsCases"
                                value="${seriesInstance?.includeAllStudyDrugsCases}"
                                checked="${seriesInstance?.includeAllStudyDrugsCases}"/>
                    <label for="includeAllStudyDrugsCases">
                        <g:message code="reportCriteria.include.all.study.drugs.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeNonValidCases"
                                name="excludeNonValidCases"
                                value="${seriesInstance?.excludeNonValidCases}"
                                checked="${seriesInstance?.excludeNonValidCases}"/>
                    <label for="excludeNonValidCases">
                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
                    </label>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox id="excludeDeletedCases"
                            name="excludeDeletedCases"
                            value="${seriesInstance?.excludeDeletedCases}"
                            checked="${seriesInstance?.excludeDeletedCases}"/>
                    <label for="excludeDeletedCases">
                    <g:message code="reportCriteria.exclude.deleted.cases"/>
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
          model="[multiIngredientValue: seriesInstance?.isMultiIngredient, includeWHODrugValue: seriesInstance?.includeWHODrugs, backgroundColor: backgroundColor, textColor: textColor]"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>
<g:render template="/query/dateRangeTypeHelp"/>