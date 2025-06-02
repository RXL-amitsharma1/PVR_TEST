<%@ page import="com.rxlogix.enums.StatusEnum" %>
<div class="pv-caselist">
    <table id="rxTableQualityReports" class="table table-striped display order-column list-table pv-list-table dataTable no-footer">
        <thead>
        <tr>
            <th style="padding-left: 10px !important; min-width: 30px;"> <div class="checkbox checkbox-primary" style="padding-left: 0;">
                <g:checkBox name="selectAll" checked="false"/>
                <label for="selectAll"></label>
            </div></th>
            <g:each in="${moduleColumnList.fieldName}" var="columnName">
                <th class="qualityColumn col-min-100" name="${columnName}">${moduleColumnList.find{it.fieldName==columnName}?.fieldLabel?:message(code:"app.reportField."+columnName)}
            </g:each>
            <th class="qualityColumn" name="errorType"><g:message code="app.label.errorType"/></th>
            <th class="qualityColumn"><g:message code="quality.capa.issueType.label"/></th>
            <th class="qualityColumn"><g:message code="quality.capa.rootCause.label"/> </th>
            <th class="qualityColumn"><g:message code="quality.capa.respParty.label"/></th>
            <th class="qualityColumn"><g:message code="app.label.dateCreated"/></th>
            <th class="qualityColumn"><g:message code="app.label.action.item.priority"/></th>
            <th class="qualityColumn dueIn"><g:message code="app.label.workflow.rule.due.in"/></th>
            <th class="qualityColumn " ><g:message code="app.label.assignedToGroup"/></th>
            <th class="qualityColumn " ><g:message code="app.label.assignedToUser"/></th>
            <th class="qualityColumn "><g:message code="app.label.state"/></th>
            <th class="qualityColumn "></th>
            <th><div ><i class="fa fa-comment-o " title="${message(code:'comment.textData.label')}"></i></div><div class='pv-stacked-row'><i class="fa fa-ticket" title="${message(code:'app.label.quality.issue')}"></i></div></th>
            <th style="width: 30px"><g:message code="app.label.action"/></th>
        </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>
<g:render template="includes/assignedToModal"/>
<g:render template="includes/editPriorityModal"/>
<g:render template="includes/assignOwner"/>
<g:render template="includes/createIssue"/>