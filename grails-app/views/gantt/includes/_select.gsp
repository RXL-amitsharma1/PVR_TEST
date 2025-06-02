<select name="${name}" style="${itemState ? "" : "display:none"}"
        multiple="multiple" class="multipleSelect2 form-control ${name} conditionField">
    <g:each var="it" in="${list}">
        <option value="${it.name}" ${it.name.toString() in itemState?.split(",") ? "selected" : ""}>${it.display}</option>
    </g:each>
</select>