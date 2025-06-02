<%@ page import="com.rxlogix.config.Configuration" %>
<g:form name="${formName}" action="transferOwnershipOfAssets" method="post">
    <input type="hidden" name="previousOwner" value="${userInstance.id}" />
    <input type="hidden" name="newOwnerValue"  />
    <input type="hidden" name="sharedWithValue"  />
    <input type="hidden" name="transferTypeValue"  />
%{--Configurations--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.adhoc.configurations.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["configuration"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">

            <g:if test="${items.configurations}">
                <g:each in="${items.configurations}" var="report" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="configuration" id='report_${report.id}' type="checkbox" checked class="form-control" value="${report.id}">
                            <label for="configuration_${report.id}">
                                ${report.reportName}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--PeriodicConfigurations--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.aggregate.configurations.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["periodicConfiguration"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">

            <g:if test="${items.periodicConfigurations}">
                <g:each in="${items.periodicConfigurations}" var="report" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="periodicConfiguration" id='periodicConfiguration_${report.id}' type="checkbox" checked class="form-control" value="${report.id}">
                            <label for="periodicConfiguration_${report.id}">
                                ${report.reportName}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>


%{--ExecutedConfigurations--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.adhoc.report.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["executedConfiguration"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking ">

            <g:if test="${items.executedConfigurations}">
                <g:each in="${items.executedConfigurations}" var="report" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="executedConfiguration" id='executedConfiguration_${report.id}' type="checkbox" checked class="form-control" value="${report.id}">
                            <label for="executedConfiguration_${report.id}">
                                ${report.reportName}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--ExecutedPeriodicConfigurations--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.aggregate.report.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["executedPeriodicConfiguration"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">
            <g:if test="${items.executedPeriodicConfigurations}">
                <g:each in="${items.executedPeriodicConfigurations}" var="report" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="executedPeriodicConfiguration" id='executedPeriodicConfiguration_${report.id}' type="checkbox" checked class="form-control" value="${report.id}">
                            <label for="executedPeriodicConfiguration_${report.id}">
                                ${report.reportName}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--CaseSeries--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.caseSeries.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["caseSeries"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">
            <g:if test="${items.caseSeries}">
                <g:each in="${items.caseSeries}" var="item" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="caseSeries" id='caseSeries_${item.id}' type="checkbox" checked class="form-control" value="${item.id}">
                            <label for="caseSeries_${item.id}">
                                ${item.seriesName}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--ExecutedCaseSeries--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.executedCaseSeries.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["executedCaseSeries"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">
            <g:if test="${items.executedCaseSeries}">
                <g:each in="${items.executedCaseSeries}" var="item" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="executedCaseSeries" id='executedCaseSeries_${item.id}' type="checkbox" checked class="form-control" value="${item.id}">
                            <label for="caseSeries_${item.id}">
                                ${item.seriesName}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--Queries--}%
<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.queries.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["query"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">
            <g:if test="${items.queries}">
                <g:each in="${items.queries}" var="query" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="query" id='query_${query.id}' type="checkbox" checked class="form-control" value="${query.id}">
                            <label for="query_${query.id}">
                                ${query.name}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--Templates--}%

<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.templates.label"/> <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["template"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>

        <div class="fourColumnsSnaking">

            <g:if test="${items.templates}">
                <g:each in="${items.templates}" var="template" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="template" id='template_${template.id}' type="checkbox" checked class="form-control" value="${template.id}">
                            <label for="template_${template.id}">
                                ${template.name}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--ActionItems--}%

<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.actionitem.label"/>  <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["action"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>
        <label class="shareItems" style="display: none"><g:message code="ownership.assignedto.label"/></label>
        <div class="fourColumnsSnaking">
            <g:if test="${items.actionItems}">
                <g:each in="${items.actionItems}" var="action" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="actionItem" id='action_${action.id}' type="checkbox" checked class="form-control" value="${action.id}">
                            <label for="template_${action.id}">
                                ${action.description}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

%{--ReportRequest--}%

<div class="row">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.reportRequest.label"/><a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["reportRequest"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>
        <label class="shareItems" style="display: none"><g:message code="ownership.assignedto.label"/></label>
        <div class="fourColumnsSnaking">
            <g:if test="${items.reportRequests}">
                <g:each in="${items.reportRequests}" var="reportRequest" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="reportRequest" id='reportRequest_${reportRequest.id}' type="checkbox" checked class="form-control" value="${reportRequest.id}">
                            <label for="reportRequest_${reportRequest.id}">
                                ${reportRequest.description}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>
    </div>
</div>

<div class="row shareItems" style="display: none">
    <div class="col-md-12">
        <h3 class="sectionHeader userOwnershipTitle"><g:message code="ownership.reportRequest.label"/>  <a class="btn btn-xs btn-primary" href="#" data-evt-clk='{"method": "toggleCheck", "params": ["requesterRequest"]}'><g:message code="ownership.check.uncheck.label"/></a></h3>
        <label><g:message code="ownership.requestor.label"/></label>
        <div class="fourColumnsSnaking">
            <g:if test="${items.requestedReportRequests}">
                <g:each in="${items.requestedReportRequests}" var="reportRequest" status="i">
                    <div class="forceLineWrap">
                        <div class="checkbox checkbox-primary checkbox-inline">
                            <input name="requesterRequest" id='requesterRequest_${reportRequest.id}' type="checkbox" checked class="form-control" value="${reportRequest.id}">
                            <label for="requesterRequest_${reportRequest.id}">
                                ${reportRequest.description}
                            </label>
                        </div>
                    </div>
                </g:each>
            </g:if>
            <g:else>
                <g:message code="app.label.none.parends"/>
            </g:else>
        </div>


    </div>
</div>
</g:form>
