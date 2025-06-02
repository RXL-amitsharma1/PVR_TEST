<div id="toAddContainerSet" class="toAddContainerSet">
    <div class="col-xs-4 expressionsNoPadFirst expressionQueryContainer">
        %{--Value would be given using Jquery setJson value logic--}%
        <g:select type="hidden" name="selectQuery" class="form-control expressionQuery" from="${[]}" value=""></g:select>
    </div>
    <g:if test="${!editable}">
        <div class="col-xs-1" style="margin-top: 12px">
            <g:if test="${isExecuted}">
                <g:link controller="query" action="viewExecutedQuery" target="_blank" name="viewLink"><g:message
                        code="app.label.view"/></g:link>
            </g:if>
            <g:else>
                <g:link controller="query" action="view" target="_blank" name="viewLink"><g:message
                        code="app.label.view"/></g:link>
            </g:else>
        </div>
    </g:if>
</div>