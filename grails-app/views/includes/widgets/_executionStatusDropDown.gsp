<%@ page import="com.rxlogix.util.ViewHelper" %>
<div id="configTypeDiv" style="float: right">
    <div id="exStatusConfigType" style="float: right" >
            <label><g:message code="app.label.type"/></label>
            <g:select name="exStatusConfigTypeFilter" from="${ViewHelper.getExecutionStatusConfigTypeEnumI18n()}"
                      optionKey="name" optionValue="display" value="REPORTS"
                      class="form-control" id="exStatusConfigTypeControl"/>
    </div>
</div>
<script>
    function initConfigurationTypeDropDown(tableId, container) {
        if (tableId == 'rxTableInboundExecutionStatus'){
            var topControls = $(".topControls");
            $('#'+tableId+'_wrapper').find('.dt-search').before(topControls);
            topControls.show();
        }
    }

    function initExecutionStatusDropDown(selectedValueFromTable){
        if (selectedValueFromTable) {
            $("#exStatusConfigTypeControl").val(selectedValueFromTable);
        }
        var statusDropDown = $("#exStatusConfigTypeControl");
        statusDropDown.select2();
        $("#configTypeDiv").css('display','block');
        statusDropDown.on('change', function(){
            var selectedValue = statusDropDown.find(":selected").val();
            switch (selectedValue) {
                case 'INBOUND_COMPLIANCE':
                    window.location.href = inboundExStatus;
                    break;
                case 'ICSR_PROFILE':
                    window.location.href = reportsExStatus + "?isICSRProfile=true"
                    break;
                case 'REPORTS':
                default :
                    window.location.href = reportsExStatus + "?isICSRProfile=false";
            }
        });
    }

</script>
