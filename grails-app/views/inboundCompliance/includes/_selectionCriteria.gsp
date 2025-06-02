<%@ page import="com.rxlogix.util.MiscUtil; com.rxlogix.enums.ReportThemeEnum; grails.util.Holders; com.rxlogix.config.DateRangeType; com.rxlogix.util.ViewHelper; com.rxlogix.config.SourceProfile" %>
<div class="rxmain-container ">
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.getUser()}"/>
    <g:hiddenField name="owner" id="owner" value="${configurationInstance?.owner?.id ?: currentUser.id}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header report-header-collapse">
            <label class="rxmain-container-header-label report-lable-collapse">
                <g:message code="app.label.selectionCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content report-content">
            <div class="row">
                <div class="col-md-7">
                    <div class="row p-t-0">
                        <div class="col-md-6">
                            <label class="labelBold add-margin-bottom" style=" width: 115px">
                                <g:message code="app.label.productSelection"/>
                            </label>
                            <div class="checkbox checkbox-primary checkbox-inline" style=" width: 190px">
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

                        <div class="col-md-6">
                            <label class="labelBold add-margin-bottom " style=" width: 115px">
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
                            <a href="http://jsonlint.com/" target="_blank" rel="noopener noreferrer"><g:message code="prettify.my.json.here"/></a>
                        </div>
                    </div>

                </div>
                <div class="col-md-3">
                    <div class="col-md-12">
                        <label><g:message code="app.label.DateRangeType"/></label>
                        <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                              data-toggle="modal"
                              data-target="#dateRangeTypeHelp"></span>
                        <g:select name="dateRangeType.id" id="dateRangeType"
                                  from="${[]}" data-value="${configurationInstance?.dateRangeType?.id}"
                                  class="form-control"/>
                    </div>
                    <div class="col-md-12">
                        <div class="${params.fromTemplate ? "" : "m-t-10"}">
                            <div class="fuelux">
                                <g:render template="includes/globalDateRange"
                                          model="[configurationInstance: configurationInstance]"/>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-12">
                        %{--Data Source--}%
                        <g:if test="${sourceProfiles.size() > 1}">
                            <div class="m-t-10">
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
                %{--Inclusion Options--}%
                <div class="col-md-2 ${params.fromTemplate ? "hidden" : ""}">

                    <label><g:message code="app.label.inclusionOptions"/></label>

                    <div class="checkbox checkbox-primary">
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

                    <div class="checkbox checkbox-primary">
                        <g:checkBox id="isDisabled"
                                    name="isDisabled"
                                    value="${configurationInstance?.isDisabled}"
                                    checked="${configurationInstance?.isDisabled}"/>
                        <label for="isDisabled">
                            <g:message code="app.label.disabled"/>
                        </label>
                    </div>
                </div>
            </div>
        </div>
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
