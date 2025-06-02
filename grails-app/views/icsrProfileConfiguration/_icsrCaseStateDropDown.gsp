<%@ page import="com.rxlogix.util.ViewHelper" %>
<style>
.nopadding {
    padding: 0px 0px 0px 0px !important;
}
.dataTables_wrapper .dataTables_filter input {
    margin-left: 0em !important;
}
#select2-icsrCaseStateControl-container {
    text-align: center !important;
    padding-right: 1em !important;
}
</style>
<div style="display: none">
    <div id="icsrCaseStateDropDown" style="position: absolute; right: 200px; display: inline-block; margin-right: 5px;" class="nopadding">
        <div class="nopadding" style="width: 380px;">
            <label style="width: 38%; text-align: right;"><g:message code="app.label.status.dropdown"/></label>
            <g:select name="icsrCaseStateFilter" from="${ViewHelper.getIcsrCaseStateEnumI18n()}"
                      optionKey="id" optionValue="name" value="${status ?: 'GENERATED'}"
                      class="form-control" style="text-align-last: center; width: 80%"
                      id="icsrCaseStateControl"/>
        </div>
    </div>
</div>
<script>
    function initIcsrCaseStateDropDown(tableId, table) {
        var tableDtEnd = $('#' + tableId + '_wrapper .dt-layout-cell.dt-end');
        tableDtEnd.find('.dt-search').before($('#icsrCaseStateDropDown'));
        if (${status != null && status != ""}) {
            sessionStorage.setItem("icsrCaseStatus", $('select[name="icsrCaseStateFilter"]').val());
        }
    }
</script>