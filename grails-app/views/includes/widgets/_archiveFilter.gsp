    <div id="archivedFilter" style="float: right; margin-right: 3px; margin-left: 3px;">
        <div class="checkbox checkbox-primary" style="padding-top: 2px; text-align: center;">
            <g:checkBox id="includeArchived" name="includeArchived"/>
            <label for="includeArchived" style="font-weight: bold"><g:message code="app.label.showArchived"/></label>
        </div>
    </div>
<script>
    var sessionStoragearchivedFilterVariableName = window.location.pathname.replace(/\//g, "") + ".archivedFilter";
    var filterOn = sessionStorage.getItem(sessionStoragearchivedFilterVariableName)
    $("#includeArchived").prop('checked', (filterOn == "true"));

    function initArchiveFilter(table) {
        $("#includeArchived").on("change", function (e) {
            sessionStorage.setItem(sessionStoragearchivedFilterVariableName, $("#includeArchived").is(":checked"));
            table.draw();
        });
    }
</script>