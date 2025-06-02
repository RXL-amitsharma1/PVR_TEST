<%@ page import="com.rxlogix.config.QueryRCA" %>

<div id="queryRCAList" data-counter="${theInstance?.queriesRCA?.size()}">
    <g:if test="${theInstance?.queriesRCA?.size() > 0}">
        <g:each var="queryRCA" in="${theInstance?.queriesRCA}" status="i">
            <g:render template='/autoReasonOfDelay/includes/queryRCA'
                      model="['queryRCAInstance': queryRCA, 'i': i, 'hidden': false]"/>
        </g:each>
    </g:if>
</div>

%{--Add Template Query button--}%
<div class="row">
    <div class="col-md-12 text-right">
        <input type="button"
               class="btn btn-primary copyQueryRCALineItemButton"
               value="${message(code: "button.copy.section.label")}"/>
        <input type="button"
               class="btn btn-primary addQueryRCALineItemButton"
               value="${message(code: "button.add.section.label")}"/>
    </div>
</div>