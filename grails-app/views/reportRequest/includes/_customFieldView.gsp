<%@ page import="grails.converters.JSON; com.rxlogix.config.ReportRequestField" %>
<div class="row">
<g:set var="customValues" value="${reportRequestInstance?.customValues ? JSON.parse(reportRequestInstance?.customValues)?.collectEntries{ k, v->[(k):v]} : [:]}"/>
<g:each var="field" in="${ReportRequestField.findAllByIsDeletedAndSection(false, section)?.sort { it.index }}" status="i">
<g:if test="${!field.reportRequestType || (field.reportRequestType==reportRequestInstance.reportRequestType)}">
    <div class="col-md-3 ${field.masterPlanningRequest ? "masterPlanningRequest" : ""}">
        <g:if test="${field.fieldType == ReportRequestField.Type.BOOLEAN}">
            <label>${field.label}</label>
            <g:formatBoolean boolean="${customValues?.get(field.name)}"
                             true="${message(code: "default.button.yes.label")}"
                             false="${message(code: "default.button.no.label")}"/>
        </g:if>
        <g:else>
            <label>${field.label}</label><br>
            ${customValues?.get(field.name)}
        </g:else>

    </div>
    <g:if test="${field.fieldType == ReportRequestField.Type.CASCADE}">
        <label>${field.secondaryLabel}</label><br>
        <g:set var="spl" value="${customValues?.get("secondary"+field.name)?.split("~")}"/>
        ${spl?.size()>1?spl[1]:""}
    </g:if>
</g:if>
</g:each>
</div>