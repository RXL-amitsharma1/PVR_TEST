<%@ page import="com.rxlogix.config.DateRangeType; com.rxlogix.util.ViewHelper;" %>
<div class="rxmain-container ">
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.getUser()}"/>
    <g:hiddenField name="owner" id="owner" value="${autoReasonOfDelayInstance?.owner?.id ?: currentUser.id}"/>

    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header report-header-collapse">
            <label class="rxmain-container-header-label report-lable-collapse">
                <g:message code="app.label.selectionCriteria"/>
            </label>
        </div>

        <div class="rxmain-container-content report-content">
            <div class="row">
                <div class="col-md-2">
                    <div class="m-t-10">
                        <label><g:message code="app.label.DateRangeType"/></label>
                        <span class="glyphicon glyphicon-question-sign modal-link" style="cursor:pointer"
                              data-toggle="modal"
                              data-target="#dateRangeTypeHelp"></span>
                        <g:select name="dateRangeType.id" id="dateRangeType"
                                  from="${[]}" data-value="${autoReasonOfDelayInstance?.dateRangeType?.id}"
                                  class="form-control"/>
                    </div>
                </div>
                <div class="col-md-2">
                    <div class="m-t-10">
                    %{--Evaluate Case Date On--}%
                        <label><g:message code="app.label.EvaluateCaseDateOn"/></label>
                        <div id="evaluateDateAsDiv">
                            <g:select name="evaluateDateAsNonSubmission"
                                      from="${[]}"
                                      data-value="${autoReasonOfDelayInstance?.evaluateDateAs}"
                                      class="form-control evaluateDateAs" />

                        </div>
                        <div id="evaluateDateAsSubmissionDateDiv">

                            <g:select name="evaluateDateAsSubmissionDate"
                                      from="${[]}"
                                      data-value="${autoReasonOfDelayInstance?.evaluateDateAs}"
                                      class="form-control evaluateDateAs" />

                        </div>
                        <input name="evaluateDateAs" id="evaluateDateAs" type="text" hidden="hidden" value="${autoReasonOfDelayInstance?.evaluateDateAs}"/>

                    </div>
                </div>
                <div class="col-md-2">
                    <div class="m-t-10">
                        <div class="fuelux">
                            <g:render template="/autoReasonOfDelay/includes/globalDateRange"
                                      model="[autoReasonOfDelayInstance: autoReasonOfDelayInstance]"/>
                        </div>
                    </div>
                </div>
                <div class="col-md-2">

                %{--Data Source--}%
                    <g:if test="${sourceProfiles.size() > 1}">
                        <div class="m-t-10">
                            <label><g:message code="userGroup.source.profiles.label"/></label>
                            <g:select name="sourceProfile.id" id="sourceProfile"
                                      from="${sourceProfiles}"
                                      optionValue="sourceName" optionKey="id"
                                      value="${autoReasonOfDelayInstance?.sourceProfile?.id ?: SourceProfile?.central?.id}"
                                      class="form-control"/>
                        </div>
                    </g:if>
                    <g:else>
                        <g:hiddenField name="sourceProfile.id"
                                       value="${autoReasonOfDelayInstance?.sourceProfile?.id ?: sourceProfiles.first()?.id}"/>
                    </g:else>
                </div>
            </div>
        </div>
    </div>
</div>

<g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>
<g:render template="/query/dateRangeTypeHelp"/>
