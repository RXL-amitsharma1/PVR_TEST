<%@ page import="com.rxlogix.config.CustomSQLTemplate; com.rxlogix.enums.TemplateTypeEnum" %>
<g:if test="${editable}">
    <div class="expandingArea">
        <pre><span></span><br></pre>
        <g:textArea class="sqlBox form-control" name="customSQLTemplateSelectFrom" maxlength="${CustomSQLTemplate.constrainedProperties.customSQLTemplateSelectFrom.maxSize}" placeholder='${message(code:("app.template.customSQL.exampleText1"))}' value="${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateSelectFrom : ''}"
                    readonly="${ciomsITemplate}"/>
    </div>
    <pre class="textSegment">where exists (select 1 from GENERATED_QUERY_RESULT caseList where cm.case_id = caseList.case_id and cm.version_num = caseList.version_num and cm.tenant_id = caseList.tenant_id)</pre>
    <div class="expandingArea">
        <pre><span></span><br></pre>
        <g:textArea class="sqlBox form-control" name="customSQLTemplateWhere" maxlength="${CustomSQLTemplate.constrainedProperties.customSQLTemplateWhere.maxSize}" placeholder='${message(code:("app.template.customSQL.exampleText2"))}' value="${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateWhere : ''}" readonly="${ciomsITemplate}"/>
    </div>

    <div class="bs-callout bs-callout-info">
        <h5><g:message code="app.label.note" />:</h5>
        <div><g:message code="app.template.parameterizedSQL.lateValidation" /></div>
        <div><g:message code="app.template.parameterizedSQL.copyCaseNumber" /></div>
    </div>

    <div class="bs-callout bs-callout-info">
        <h5><g:message code="example" />:</h5>
        <div class="text-muted"><pre>select case_num "Case Number" from V_C_IDENTIFICATION cm</pre></div>
    </div>
    <div class="row">
        <div class="col-xs-5">
            <label><g:message code="app.label.drillDownTemplate" /></label>
            <g:select name="drillDownTemplate" from="${[]}" noSelection="['': message(code: 'select.operator')]" class="form-control drillDownTemplate" data-value="${template?.drillDownTemplateId}"/>
        </div>

        <div class="col-xs-1">
            <label>&nbsp;</label><div>
            <a href="${template?.drillDownTemplateId ?createLink(controller: 'template' , action: 'view', id: template?.drillDownTemplateId):'#'}"
               title="${message(code: 'app.label.viewTemplate')}" target="_blank" class="pv-ic templateQueryIcon templateViewButton glyphicon glyphicon-info-sign ${template?.drillDownTemplateId ? '' : 'hide'}"></a>
        </div>
        </div>

        <div class="col-xs-3">
            <label><g:message code="app.label.drillDownField" /></label>
            <input name="drillDownField" class="form-control " value="${template?.drillDownField}" maxlength="${CustomSQLTemplate.constrainedProperties.drillDownField.maxSize}"/>
        </div>
        <div class="col-xs-3">
            <label><g:message code="app.label.drillDownFilterColumns" /></label>
            <input name="drillDownFilerColumns" class="form-control " value="${template?.drillDownFilerColumns}" maxlength="${CustomSQLTemplate.constrainedProperties.drillDownFilerColumns.maxSize}"/>
        </div>
    </div>
    <g:render template="includes/reportFooter" model="[readonly: ciomsITemplate]"/>
    <asset:javascript src="app/template/editCustomSQL.js"/>
</g:if>
<g:else>
    <pre>${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateSelectFrom : ''}</pre>
    <pre>where exists (select 1 from GENERATED_QUERY_RESULT caseList where cm.case_id = caseList.case_id and cm.version_num = caseList.version_num and cm.tenant_id = caseList.tenant_id)</pre>
    <pre>${template?.templateType == TemplateTypeEnum.CUSTOM_SQL ? template.customSQLTemplateWhere : ''}</pre>
    <div style="padding-bottom: 10px;"></div>

    <label><g:message code="app.label.drillDownTemplate"/>:</label>
    <g:if test="${template?.drillDownTemplateId}">
        <g:if test="${isExecuted}">
            <a href="${template?.drillDownTemplateId ? createLink(controller: 'template', action: 'viewExecutedTemplate', id: template?.drillDownTemplateId) : '#'}"
               title="${message(code: 'app.label.viewTemplate')}"
               target="_blank">${template?.drillDownTemplate?.name}</a> <br>
        </g:if>
        <g:else>
            <a href="${template?.drillDownTemplateId ? createLink(controller: 'template', action: 'view', id: template?.drillDownTemplateId) : '#'}"
               title="${message(code: 'app.label.viewTemplate')}"
               target="_blank">${template?.drillDownTemplate?.name}</a> <br>
        </g:else>
    </g:if>
    <g:else>
        <g:message code="app.label.none"/> <br>
    </g:else>

    <label><g:message code="app.label.drillDownField"/>:</label>
    <g:if test="${template?.drillDownField}">
        ${template?.drillDownField} <br>
    </g:if>
    <g:else>
        <g:message code="app.label.none"/> <br>
    </g:else>

    <label><g:message code="app.label.drillDownFilterColumns"/>:</label>
    <g:if test="${template?.drillDownFilerColumns}">
        ${template?.drillDownFilerColumns}
    </g:if>
    <g:else>
        <g:message code="app.label.none"/>
    </g:else>

    <g:render template="includes/reportFooter" model="[readonly: !editable]"/>
    <div style="padding-bottom: 20px;"></div>
</g:else>
