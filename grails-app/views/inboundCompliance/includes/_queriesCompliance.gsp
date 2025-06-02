<%@ page import="com.rxlogix.config.QueryRCA" %>

<div id="queryComplianceList" data-counter="${theInstance?.queriesCompliance?.size()}">
    <g:if test="${theInstance?.queriesCompliance?.size() > 0}">
        <g:each var="queryCompliance" in="${theInstance?.queriesCompliance}" status="i">
            <g:render template='includes/queryCompliance'
                      model="['queryComplianceInstance': queryCompliance, 'i': i, 'hidden': false]"/>
        </g:each>
    </g:if>
</div>

%{--Add Template Query button--}%
<div class="row">
    <div class="col-md-12 text-right">
        <input type="button"
               class="btn btn-primary copyQueryComplianceLineItemButton"
               value="${message(code: "button.copy.section.label")}"/>
        <input type="button"
               class="btn btn-primary addQueryComplianceLineItemButton"
               value="${message(code: "button.add.section.label")}"/>
    </div>
</div>