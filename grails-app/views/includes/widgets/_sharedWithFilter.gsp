<style>
.glyphicon-filter{padding: 3px 9px;}
.glyphicon-filter:hover{background: rgba(255, 255, 255, 0.3);}
.filter-input{margin-bottom: 5px;}
.datepicker .input-group{margin-bottom: 5px;}
.pv-caselist .pv-list-table tbody tr .no-padding { padding: 2px 0px !important;}
</style>
<%@ page import="com.rxlogix.Constants;com.rxlogix.config.ExecutionStatus" %>
<div style="display: none">
    <div id="sharedWithFilter">
        <!--<label><g:message code="app.label.shared"/></label>-->
        <select class="sharedWithControl form-control" id="sharedWithFilterControl" name="sharedWithFilterControl" value="" style="min-width: 100px;"></select>
    </div>
</div>
<script>
    if("${clazz}" == "${ExecutionStatus.name}"){
        sharedWithFilterListUrl = "${createLink(controller: 'userRest', action: 'sharedWithUserList')}";
    } else {
        sharedWithFilterListUrl = "${createLink(controller: 'userRest', action: 'sharedWithFilterList', params: [clazz:clazz])}";
    }
    sharedWithFilterValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
    var sessionStorageSharedWithVariableName = window.location.pathname.replace(/\//g,"")+".sharedWith";
    bindShareWith($('#sharedWithFilterControl'), sharedWithFilterListUrl, sharedWithFilterValuesUrl, '250px');
    initSharedWithFilterControl();
    function initSharedWithFilter(tableId,table,width) {
        var searchDiv = $('#'+tableId+ '_wrapper').find('.dt-search').parent(),
            sharedWith = $('#sharedWithFilterControl');
        //searchDiv.find('input').css('height', '30');
        searchDiv.find('input').attr('placeholder', $.i18n._("fieldprofile.search.label"));
        searchDiv.find('label').css('margin-bottom', '-2px');
        searchDiv.attr("class", "col-xs-8 pull-right mb-10");
        searchDiv.find(".dt-search").addClass("pull-right");
        searchDiv.parent().addClass('searchToolbar top ');
        // $('#' + tableId + '_wrapper').find('.dt-search').after($('.typeFilterDiv'));

        $('#' + tableId + '_wrapper').find('.dt-search').after($('.topControls'));
        $('.topControls').show();
        $('#' + tableId + '_wrapper').find('.dt-search').after($('#sharedWithFilter'));

        width = width?width:'250px';
        bindShareWith(sharedWith, sharedWithFilterListUrl, sharedWithFilterValuesUrl, width).on("change", function (e) {
            sessionStorage.setItem(sessionStorageSharedWithVariableName, $('#sharedWithFilterControl').val());
            table.draw();
        });
    }
          function initSharedWithFilterControl() {
                   var storageValue = sessionStorage.getItem(sessionStorageSharedWithVariableName);
                    if (!_.isEmpty(storageValue)) {
                            storageValue = storageValue.split(",");
                    }
              $('#sharedWithFilterControl').val(_.isEmpty(storageValue) ? '' : storageValue).trigger("change");
           }
</script>