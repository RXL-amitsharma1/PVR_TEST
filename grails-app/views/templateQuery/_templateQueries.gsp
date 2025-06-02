<%@ page import="com.rxlogix.config.IcsrProfileConfiguration; com.rxlogix.config.IcsrReportConfiguration; com.rxlogix.user.UserGroup; com.rxlogix.config.TemplateQuery;com.rxlogix.config.BasicPublisherSource;com.rxlogix.config.TemplateQuery" %>
<g:set var="qbeForm" value="${true && theInstance?.qbeForm}"/>
<sec:ifAnyGranted roles="ROLE_BQA_EDITOR">
    <g:set var="qbeForm" value="${true && theInstance?.qbeForm && !(configurationInstance?.errors?.errorCount>0)}"/>
</sec:ifAnyGranted>
<script>
    var qbeForm=${qbeForm};
</script>
<div id="templateQueryList" data-counter="${theInstance.templateQueries?.size()}">
    <g:if test="${theInstance.templateQueries?.size() > 0}">
        <g:each var="templateQuery" in="${theInstance?.templateQueries}" status="i">
            <g:render template='/templateQuery/templateQuery'
                      model="['templateQueryInstance': templateQuery, 'i': i, 'hidden': false, isForPeriodicReport: isForPeriodicReport,qbeForm:qbeForm]"/>
        </g:each>
    </g:if>
</div>

%{--Add Template Query button--}%
<div class="row ${(params.fromTemplate || isForIcsrReport || qbeForm) ? "hidden" : ""}">
    <div class="col-md-12 text-right">
        <input type="button"
               class="btn btn-primary copyTemplateQueryLineItemButton"
               value="${message(code: "button.copy.section.label")}"/>
        <input type="button"
               class="btn btn-primary addTemplateQueryLineItemButton"
               value="${message(code: "button.add.section.label")}"/>
    </div>
</div>

<div id="userGroupEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
    <g:select name="queryTemplateUserGroupSelect" from="${UserGroup.findAllByIsDeleted(false).sort {it.name.toLowerCase()}}" class="form-control queryTemplateUserGroupSelect"
              optionKey="id" optionValue="name" noSelection="['0': 'Any']"/>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>
<div id="userEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
    <g:set var="users" value="${com.rxlogix.user.User.findAllByEnabled(true).sort{it.fullName}}"/>
    <select name="userSelect" class="form-control userSelectSelect" >
        <g:each in="${users}" var="user">
            <option value="${user.id}" data-blinded="${user.isBlinded}">${user.getReportRequestorValue()} </option>
        </g:each>
    </select>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button type="button" class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button type="button" class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>