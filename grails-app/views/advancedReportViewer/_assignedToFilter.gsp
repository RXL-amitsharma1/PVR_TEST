<style>
.glyphicon-filter {
    padding: 3px 9px;
}

.glyphicon-filter:hover {
    background: rgba(255, 255, 255, 0.3);
}

.filter-input {
    margin-bottom: 5px;
}

.datepicker .input-group {
    margin-bottom: 5px;
}

.pv-caselist .pv-list-table tbody tr .no-padding {
    padding: 2px 0px !important;
}
</style>
<%@ page import="com.rxlogix.Constants;com.rxlogix.config.ExecutionStatus; com.rxlogix.user.User; com.rxlogix.user.Preference;grails.plugin.springsecurity.SpringSecurityUtils" %>
<g:set var="userService" bean="userService"/>
<script>
    assignedToFilterListUrl = "${createLink(controller: 'userRest', action: 'assignedToFilterList',params: [type: 'pvc'])}";
    assignedToFilterValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
    updateSimilarCasesCheckUrl = "${createLink(controller: 'preference', action: 'updateRODSimilarCasesCheck')}";

    function initSharedWithFilter(tableId, table, width) {
        var searchDiv = $('#' + tableId + '_filter').parent(),
            sharedWith = $('#assignedToFilter');
        searchDiv.find('input').attr('placeholder', $.i18n._("fieldprofile.search.label"));
        searchDiv.find('label').css('margin-bottom', '2px');
        searchDiv.find('label').css('margin-left', '5px');
        searchDiv.attr("class", "pull-right mb-10 col-xs-6");
        searchDiv.parent().addClass('searchToolbar col-xs-6 pull-right');
        width = width ? width : '120px';
        let doNotAllowToClear = ${SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA")};
        bindShareWith(sharedWith, assignedToFilterListUrl, assignedToFilterValuesUrl, width, true, $('body'),$.i18n._('app.advancedFilter.assigned'),!doNotAllowToClear).on("change", function (e) {
            table.draw();
        });
    }

    $(function (event) {
        $('#similarCasesCheckbox').on('click', function () {
            showLoader();
            $.ajax({
                url: updateSimilarCasesCheckUrl,
                data: {isChecked: $(this).prop("checked")},
                dataType: 'json'
            })
                .done(function (data) {
                    hideLoader();
                })
                .fail(function (data) {
                    hideLoader();
                    if ($(this).is(":checked")) {
                        $(this).prop("checked", false).trigger("change");
                    } else if (!$(this).is(":checked")) {
                        $(this).prop("checked", true).trigger("change");
                    }
                    console.log("Error", data);
                });
        });
    });
</script>