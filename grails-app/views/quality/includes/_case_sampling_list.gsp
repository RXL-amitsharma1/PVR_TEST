<div class="pv-caselist">
    <table id="case-sampling-list" class="table table-striped display order-column list-table pv-list-table dataTable no-footer" >
        <thead>
        <tr>
            <th style="padding-left: 10px !important; min-width: 30px;"> <div class="checkbox checkbox-primary" style="padding-left: 0;">
                <g:checkBox name="selectAll" checked="false"/>
                <label for="selectAll"></label>
            </div></th>
            <g:each in="${columnNameList}" var="columnName">
            %{--errorType check, as it has separate message code, not a report field--}%
                <g:if test="${columnName == grailsApplication.config.qualityModule.extraColumnList[0]}">
                    <th class="qualityColumn" name="${columnName}"><g:message code="app.label.${columnName}"/></th>
                </g:if>
                <g:else>
                    <th class="qualityColumn" name="${columnName}">${moduleColumnList.find{it.fieldName==columnName}?.fieldLabel?:message(code:"app.reportField."+columnName)}
                </g:else>
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
    <g:render template="includes/createIssue"/>
</div>
<g:render template="includes/assignedToModal"/>