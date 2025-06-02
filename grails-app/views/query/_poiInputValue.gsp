<div id="poiInputValueContainer" class="poiInputValueContainer row queryBlankContainer">
    <div class="errorMessageOperator"><g:message code="app.query.value.invalid.number" /></div>

    <div class="col-xs-2 poiKeyContainer">
        <p class="inputPOIKeyText"></p>
        <g:hiddenField name="inputPOIKEY" class="inputPOIKey"/>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <g:textField name="inputPOIValue" class="form-control inputPOIValue" placeholder="Value"/>
    </div>

    <div class="col-xs-1" hidden="hidden">
        <input class="poiValue" value="${value}" />
        <input name="poiKey" class="poiKey" value="${key}" />
    </div>
</div>