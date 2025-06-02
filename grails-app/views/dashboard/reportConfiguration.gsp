<%@ page import="com.rxlogix.util.ViewHelper" %>

<form id="chartWidgetRefreshForm">
    <input type="hidden" name="id" value="${templateQueryInstance.usedConfiguration.id}">
    <input type="hidden" name="sectionId" value="${templateQueryInstance.id}">
    <div class="container " id="templateQueryList">
        <g:set var="configurationService" bean="configurationService"/>

        <div data-counter="1" class="col-md-12 templateQuery-div queryWrapperRow" id="templateQuery0">
            <div class="row granularityDiv" style="display: ${templateQueryInstance?.granularity ? 'block' : 'none'}">
                <div class="col-md-12">
                    <label><g:message code="app.label.granularity"/></label>

                    <g:select name="templateQueries[0].granularity" optionKey="name" optionValue="display" disabled="${!templateQueryInstance?.granularity}"
                              from="${com.rxlogix.util.ViewHelper.granularity}" value="${templateQueryInstance?.granularity}"
                              class="form-control select2-box granularitySelect"/>
                </div>
            </div>
            <div class="row">
                %{--Date Range--}%
                <div class="col-md-12 sectionDateRange">
                    <g:set var="i" value="0"/>
                    <g:render template="/configuration/dateRange"
                              model="[templateQueryInstance: templateQueryInstance]"/>
                </div>
                %{--Query--}%
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-12 ">
                            <label><g:message code="app.label.chooseAQuery"/></label>
                        </div>
                    </div>

                    <div class="row queryContainer" style="margin: 0">
                        <div>
                            <i class="fa fa-refresh fa-spin loading"></i>
                        </div>
                        <div>

                            <div class="doneLoading" style="padding-bottom: 5px;">
                                <div>
                                    <div>
                                        <g:select name="templateQueries[0].query"
                                                  from="${[]}"
                                                  value="${templateQueryInstance?.query?.id}"
                                                  data-value="${templateQueryInstance?.query?.id}"
                                                  class="form-control selectQuery"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="row">
                %{--Blank values--}%
                <div class="col-md-12">
                    <div class="templateSQLValues">
                        <g:if test="${templateQueryInstance?.templateValueLists?.size() > 0}">
                            <g:each var="tvl" in="${templateQueryInstance.templateValueLists}">
                                <g:each var="tv" in="${tvl.parameterValues}" status="j">
                                    <g:render template='/query/customSQLValue' model="['qev': tv, 'i': 0, 'j': j]"/>
                                </g:each>
                            </g:each>
                        </g:if>
                        <g:if test="${templateQueryInstance?.getPOIInputsKeysValues()}">
                            <g:each in="${templateQueryInstance.getPOIInputsKeysValues()}" var="inputPOIKeyValue">
                                <g:render template='/query/poiInputValue' model="[key: inputPOIKeyValue.key, value: inputPOIKeyValue.value]"/>
                            </g:each>
                        </g:if>
                    </div>

                    <div class="queryExpressionValues">
                        <g:if test="${templateQueryInstance?.queryValueLists?.size() > 0}">
                            <g:each var="qvl" in="${templateQueryInstance.queryValueLists}">
                                <g:each var="qev" in="${qvl.parameterValues}" status="j">
                                    <g:if test="${qev?.hasProperty('reportField')}">
                                        <g:render template='/query/toAddContainerQEV' model="['qev': qev, 'i': 0, 'j': j]"/>
                                    </g:if>
                                    <g:else>
                                        <g:render template='/query/customSQLValue' model="['qev': qev, 'i': 0, 'j': j]"/>
                                    </g:else>
                                </g:each>
                            </g:each>
                        </g:if>
                    </div>
                    <g:hiddenField class="validQueries" name="templateQueries[0].validQueries"
                                   value="${configurationService.getQueriesId(templateQueryInstance)}"/>
                </div>
            </div>
        </div>

    </div>
    <g:if test="${templateQueryInstance.getUsedConfiguration().instanceOf(com.rxlogix.config.PeriodicReportConfiguration) && ((templateQueryInstance.getUsedConfiguration()?.numOfExecutions > 0) || templateQueryInstance.getUsedConfiguration()?.isEnabled)}">
        <label for="reportJustification" class="forReport"><g:message code="app.report.justification"/></label>
        <input id="reportJustification" name="auditLogJustification" class="form-control"/>
    </g:if>
</form>

<script>
    var dashboardWidget = true
</script>
<asset:javascript src="app/configuration/templateQueries.js"/>
<g:if test="${!reportConfiguration}">
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
</g:if>
<asset:javascript src="app/query/queryValueSelect2.js"/>
<asset:javascript src="app/configuration/blankParameters.js"/>
<asset:javascript src="app/configuration/dateRange.js"/>


