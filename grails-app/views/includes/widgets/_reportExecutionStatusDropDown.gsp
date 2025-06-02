<%@ page import="com.rxlogix.util.ViewHelper" %>

    <div id="reportExecutionStatusDropDown" style="float: right; margin-left: 10px">
        <label><g:message code="app.label.action.item.status" /></label>
        <g:select name="submissionFilter" from="${isInboundCompliance ? ViewHelper.getReportExecutionStatusEnumForICI18n() : ViewHelper.getReportExecutionStatusEnumI18n()}"
                  optionKey="name" optionValue="display" value="GENERATING"
                  class="form-control" style="text-align-last: center;"
                  id="reportExecutionStatusControl" />
    </div>
</div>
<script>
    function initReportExeuctionStatusDropDown(tableId,container) {
        var searchDiv = container? container:$('#'+tableId+ '_wrapper').find('.dt-search').parent(),
        statusDropDown = $('#reportExecutionStatusControl')
        // searchDiv.find('input').css('height', '40');
        // searchDiv.find('label').css('margin-bottom', '-2px');
        // searchDiv.attr("class", "col-xs-4");
        searchDiv.parent().addClass('searchToolbar top');
        searchDiv.parent().addClass('report-Execution-searchbar');
        $('#' + tableId + '_wrapper').find('.dt-search').after($('.topControls'));
        $('.topControls').show();
        $('#' + tableId + '_wrapper').find('.dt-search').after($('#reportExecutionStatusDropDown'));
    }
</script>
