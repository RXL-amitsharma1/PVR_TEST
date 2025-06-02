<%@ page import="com.rxlogix.util.MiscUtil; com.rxlogix.enums.ReportThemeEnum; grails.util.Holders; com.rxlogix.config.SourceProfile; com.rxlogix.enums.EvaluateCaseDateEnum; com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper; com.rxlogix.IcsrScheduleService" %>
<g:set var="icsrScheduleService" bean="icsrScheduleService"/>
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

            </div>


            %{--Evaluate Case Date On--}%

            <div class="col-md-6">
                <div class="row">
                    <div class="col-md-4 global-criteria-checkbox">
                        <div class="row">
                            <div class="col-md-12 ${params.fromTemplate ? "hidden" : ""}">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="includeProductObligation" class=""
                                                value="${configurationInstance?.includeProductObligation}"
                                                checked="${configurationInstance?.includeProductObligation}"/>
                                    <label for="includeProductObligation">
                                        <g:message code="reportCriteria.include.product.obligation.cases"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <div id="excludeNonValidCases" class="checkbox checkbox-primary">
                                    <g:checkBox id="excludeNonValidCases"
                                                name="excludeNonValidCases"
                                                value="${configurationInstance?.excludeNonValidCases}"
                                                checked="${configurationInstance?.excludeNonValidCases}"/>
                                    <label for="excludeNonValidCasesDisabled">
                                        <g:message code="reportCriteria.exclude.non.valid.cases"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12 ${params.fromTemplate ? "hidden" : ""}">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="includeStudyObligation" class=""
                                                value="${configurationInstance?.includeStudyObligation}"
                                                checked="${configurationInstance?.includeStudyObligation}"/>
                                    <label for="includeStudyObligation">
                                        <g:message code="reportCriteria.include.study.obligation.cases"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12 ${params.fromTemplate ? "hidden" : ""}">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="includeNonReportable" class=""
                                                value="${configurationInstance?.includeNonReportable}"
                                                checked="${configurationInstance?.includeNonReportable}"/>
                                    <label for="includeNonReportable">
                                        <g:message code="reportCriteria.include.non.reportable.cases"/>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    %{--Inclusion Options--}%
                    <div class="col-md-4">
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.label.icsr.profile.conf.authorizationType" />
                                    <span class="required-indicator" id="authorizationSpan">*</span></label>
                                <div class="row">
                                <div class="col-xs-12" style="padding-bottom: 5px;">
                                <g:select name="authorizationTypeId"
                                          id="authorizationTypeId"
                                          from="${icsrScheduleService.getAuthType()}"
                                          noSelection="${['': message(code: 'select.one')]}"
                                          class="form-control authorizationTypeId " optionKey="id" optionValue="name"
                                          value="${configurationInstance?.authorizationTypes*.toInteger()}" multiple="true"/>

                                </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-12">
                                <label><g:message code="app.label.downgradeQuery"/></label>

                                <div class="row queryContainer">

                                    <div class="col-xs-12">
                                        <div class="doneLoading" style="padding-bottom: 5px;">
                                            <g:select name="globalQuery" readonly="${params.fromTemplate}"
                                                      from="${[]}" noSelection="['': message(code: 'select.operator')]"
                                                      data-value="${configurationInstance?.globalQuery?.id}"
                                                      class="form-control selectQuery"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

                <div class="row">
                    <div class="globalQueryWrapper col-md-12" style="float: right">
                        <div class="queryExpressionValues">
                            <g:if test="${configurationInstance?.globalQueryValueLists?.size() > 0}">
                                <g:each var="qvl" in="${configurationInstance.globalQueryValueLists}">
                                %{--<div>${qvl.query.name}:${qvl.id}</div>--}%
                                    <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                        <g:if test="${qev?.hasProperty('reportField')}">
                                            <g:render template='/query/toAddContainerQEV'
                                                      model="['qev': qev, 'i': i, 'j': j]"/>
                                        </g:if>
                                        <g:else>
                                            <g:render template='/query/customSQLValue'
                                                      model="['qev': qev, 'i': i, 'j': j]"/>
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

