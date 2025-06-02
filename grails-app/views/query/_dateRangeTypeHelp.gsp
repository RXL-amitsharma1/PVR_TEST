<%@ page import="com.rxlogix.config.DateRangeType;com.rxlogix.util.ViewHelper"%>
<!-- Modal for queryLevelHelp -->
<div class="modal fade dateRangeTypeHelp" id="dateRangeTypeHelp" tabindex="-1" role="dialog" aria-labelledby="Date Range Type Help">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "modalHide", "params": ["#dateRangeTypeHelp"]}' aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <span><b><g:message code="app.date.range.type.label" /></b></span>
            </div>
            <div class="modal-body container-fluid">
                <g:each in="${ViewHelper.getDateRangeTypeKeyDescriptionI18n()}" var="dateRangeType">
                <div><span><b><g:message code="${dateRangeType.display}" /></b></span>: <g:message code="${dateRangeType.description}" /></div><br>
                </g:each>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey cancel" data-evt-clk='{"method": "modalHide", "params": ["#dateRangeTypeHelp"]}'><g:message code="default.button.ok.label" /></button>
            </div>
        </div>
    </div>
</div>