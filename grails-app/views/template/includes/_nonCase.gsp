<%@ page import="com.rxlogix.config.NonCaseSQLTemplate; com.rxlogix.enums.TemplateTypeEnum" %>
<asset:javascript src="app/template/nonCase.js"/>
<style>
.text-muted {
    background:white;
    -webkit-transition:background 1s;
    -moz-transition:background 1s;
    -o-transition:background 1s;
    transition:background 1s
}
</style>
<div class="row">
    <div class="col-md-3">

        <div class="checkbox checkbox-primary checkbox-inline">
            <g:checkBox name="showChartSheet" value="${template?.showChartSheet}" />
            <label for="showChartSheet">
                <g:message code="show.chartSheet"/>
            </label>
        </div>
    </div>
    <sec:ifAnyGranted roles="ROLE_CHART_TEMPLATE_EDITOR">
        <div class="col-md-9">
            <div class="checkbox checkbox-primary checkbox-inline">
                <g:checkBox name="usePvrDB" value="${template?.usePvrDB}" />
                <label for="usePvrDB">
                    <g:message code="app.template.nonCase.execute.on.pvr"/>
                </label>
            </div>
        </div>
    </sec:ifAnyGranted>

</div>

<input type="hidden" id="specialChartSettings" name="specialChartSettings" value="${template?.specialChartSettings}">
<div class="row">
    <div class="col-xs-12">
    <g:if test="${editable}" >
        <div class="expandingArea">
            <pre><span></span><br></pre>
            <g:textArea class="sqlBox form-control" name="nonCaseSql"
                        placeholder='${message(code:("app.template.nonCase.exampleText1"))}'
                        value="${template?.templateType == TemplateTypeEnum.NON_CASE ? template.nonCaseSql : ''}"/>
        </div>
        <div class="row specialChartSettingsDiv" style="${template?.showChartSheet ? '' : 'display:none'}">
            <b><g:message code="app.label.specialChartSettings"/></b>
        </div>

        <div class="bs-callout bs-callout-info">
            <h5><g:message code="app.label.notes" />:</h5>
            <div><g:message code="app.template.parameterizedSQL.lateValidation" /></div>
            <div><g:message code="app.template.parameterizedSQL.chartColumns" /></div>
            <div><g:message code="app.template.parameterizedSQL.chartColumnsPercentage" /></div>
        </div>

        <div class="bs-callout bs-callout-info">
            <h5><g:message code="examples" />:</h5>
            <div class="text-muted"><pre>select case_num "Case Number" from V_C_IDENTIFICATION cm where rownum < 15</pre></div>
            <div class="text-muted"><pre>select occured_country_desc, Count(case_num) CHART_COLUMN_TotalCases from V_C_IDENTIFICATION cm where rownum < 15 group by occured_country_desc</pre></div>
            <div class="text-muted"><pre>select * from (select occured_country_desc, Count(case_num) "CHART_COLUMN_Total Cases",
            round(100*(count(*) / sum(count(*)) over ()),2) "CHART_COLUMN_P_Percentage"
            from V_C_IDENTIFICATION cm  group by occured_country_desc order by 3 desc) where rownum <= 15</pre></div>
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
                <input name="drillDownField" class="form-control " value="${template?.drillDownField}" maxlength="${NonCaseSQLTemplate.constrainedProperties.drillDownField.maxSize}"/>
            </div>
            <div class="col-xs-3">
                <label><g:message code="app.label.drillDownFilterColumns" /></label>
                <input name="drillDownFilerColumns" class="form-control " value="${template?.drillDownFilerColumns}" maxlength="${NonCaseSQLTemplate.constrainedProperties.drillDownFilerColumns.maxSize}"/>
            </div>
        </div>
        <g:render template="includes/reportFooter" />
        <asset:javascript src="app/template/editCustomSQL.js"/>
    </g:if>
    <g:else>
        <pre>${template?.templateType == TemplateTypeEnum.NON_CASE ? template.nonCaseSql : ''}</pre>
        <div style="padding-bottom: 10px;"></div>
        <div class="row specialChartSettingsDiv" style="${template?.showChartSheet ? '' : 'display:none'}">
            <b><g:message code="app.label.specialChartSettings"/></b>
        </div>

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
        <div style="padding-bottom: 20px;"></div>
        <g:render template="includes/reportFooter" model="[readonly: !editable]"/>
    </g:else>
    </div>
</div>

<div class="row d-t-border chartSettingsHeader" style="margin-top: 5px;display: none">
    <div class="rxmain-container-header"><a class="theme-color chartSettings" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg click fa-caret-down"></i> <g:message code="app.label.chartSettings"/> </a></div>
    <span style="margin-left: 5px"><g:message code="app.label.maxChartPoints" default="Max Chart Points"/></span>
    <input name="maxChartPoints" id="maxChartPoints" class="form-control" type="number" min="1" max="999" value="${template?.maxChartPoints?:50}" style="width: 50px; display: inline; margin-top: 2px">

    <div class="checkbox checkbox-primary" style="display: inline">
        <g:checkBox name="chartExportAsImage" value="${template?.chartExportAsImage}"/>
        <label style="margin-left: 10px; margin-top: 5px;" class="no-bold add-cursor chartExportAsImageDiv" for="chartExportAsImage">
            <g:message code="show.chartExportAsImage"/>

        </label>
        <i class="glyphicon glyphicon-info-sign"  title="${message(code: 'app.data.tabulation.measures.imageAsChart.info')}"></i>
    </div>
    <div class="chartSettings-section row p-10">
        <div id="easychart-preview" hidden="hidden"></div>
    </div>
</div>

<g:chartDefaultOptionsHidden name="chartDefaultOptions" />
<g:hiddenField name="chartCustomOptions" value="${template?.chartCustomOptions}"/>
<div class="specialSettingsToClone row" style="display: none; padding: 2px; padding-left: 10px">
    <div style="float: left"><g:message code="app.label.nonCase.specialSettings.text1"/> <span class="chartColumnName" style="font-weight: bold"></span> <g:message code="app.label.nonCase.specialSettings.text2"/></div>
    <div style="float: left;margin-left:4px; width: 150px">
        <select name="valuesChartType" style="  height: 20px" class="valuesChartType form-control" ${editable?"":"disabled"}>
            <option value=""><g:message code="app.label.default"/></option>
            <option value="line"><g:message code="app.label.line"/></option>
            <option value="spline"><g:message code="app.label.spline"/></option>
            <option value="column"><g:message code="app.label.column"/></option>
            <option value="area"><g:message code="app.label.area"/></option>
        </select></div>
    <div style="float: left; margin-left:4px;"><g:message code="app.label.nonCase.specialSettings.text3"/></div>
    <div style="float: left;margin-left:4px;"><input name="chartColumnLabel" style=" height: 20px" value="" ${editable?"":"disabled"} class="chartColumnLabel form-control">
    </div>
</div>

<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.data.tabulation.measures.imageAsChart.warningModal'), warningModalId:'chartExportAsImageWarning', queryType: ' ' ]"/>