<div id="customSQLValueContainer" class="customSQLValueContainer ${!qbeForm?"row":"col-xs-4"}  queryBlankContainer">
    <div class="errorMessageOperator"><g:message code="app.query.value.invalid.number" /></div>
<g:if test="${!qbeForm}">
    <div class="col-xs-2 sqlKeyContainer">
        <p class="inputSQLKey"></p>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <g:textField name="inputSQLValue" class="form-control inputSQLValue" placeholder="Value"/>
    </div>
</g:if>
<g:else>
    <div class="col-xs-12 expressionsNoPad sqlKeyContainer">
        <label class="inputSQLKey"></label>
        <g:textField name="inputSQLValue" class="form-control inputSQLValue" placeholder="Value"/>
    </div>
</g:else>
    <div class="col-xs-1" hidden="hidden">
        <input class="qevValue" value="${qev?.value}" />
        <input name="inputSQLKey" class="qevKey" value="${qev?.key}" />
    </div>
</div>