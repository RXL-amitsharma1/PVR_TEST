<%@ page import="com.rxlogix.user.User" %>
<div class="modal fade" id="addRemoveFieldModal" data-backdrop="static" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header dropdown">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title"><g:message code="add.remove.reporting.fields"/></h4>
            </div>
            <div class="modal-body action-item-modal-body">
                <div id="allowedFieldData" style="display: none;" data-evt-clk='{"method": "enableDisableAddAllBtn", "params": []}'>

                </div>
                <div class="form-group row loading" id="allowedFieldSpinner" style="display:none;text-align:center;">
                    <i class="fa fa-refresh fa-spin"></i>
                </div>
            </div>
            <div class="modal-footer">
                <div class="buttons creationButtons">
                    <input id="addRemoveFieldButton" type="button" class="btn btn-primary" value="${g.message(code: 'app.label.select')}" data-evt-clk='{"method": "updateFields", "params": []}'>
                    <button type="button" data-dismiss="modal" class="btn pv-btn-grey" aria-label="Close"><g:message code="app.button.close"/></button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var init = function () {
            $('#addRemoveFieldModal #allowedFields').pickList();
        };
        init();

        $(document).on('click', '[data-evt-clk]', function () {
            const eventData = JSON.parse($(this).attr("data-evt-clk"));
            const methodName = eventData.method;
            if (methodName == 'enableDisableAddAllBtn') {
                var f = $(".fieldNameFilter").val();
                if (_.isEmpty(f)) {
                    $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
                } else {
                    $(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
                }
            } else if (methodName == "removeParentNode") {
                // Call the method from the eventHandlers object with the params
                $(this).parents('.demo-chip').remove()
            } else if (methodName == "updateFields") {
                updateFields();
            }
        });
    });
    function updateFields() {
        var groupDiv = $("div.groupName-" + selectedGroupId + " div.panel-collapse .selectedFields")
        var itemIds = $("#addRemoveFieldModal #allowedFields").val();
        var addedItems = [];
        $(groupDiv.find(".demo-chip")).each(function (index, item) {
            var label = $(item).find(".forceLineWrap").text().split("(")[0].trim();
            var blinded = $(item).find("input.blindedReportFields").is(":checked");
            var redacted = $(item).find("input.protectedReportFields").is(":checked");
            var hidden = $(item).find("input.hiddenReportFields").is(":checked");
            addedItems.push({label: label, blinded: blinded, redacted: redacted, hidden: hidden});
        });
        groupDiv.html("");

        if (!itemIds) {
            $('#addRemoveFieldModal').modal('hide');
            return;
        }

        const createRadioBtn = (type, value, index, checked = false) => {
            const checkedAttr = checked ? " checked" : "";
            return '<input type="radio" value="' + value + '" class="' + type + '" name="fieldRestrictions-' + selectedGroupId + '-' + index + '" ' + checkedAttr + '/>';
        };

        $('ul.pickList_targetList li').each( function (index) {
            var addedEntry = addedItems.find(entry => entry.label == $(this).attr("label").split("(")[0].trim())
            var value = $(this).attr("data-value")
            var blinded = false
            var redacted = false
            var hidden = true

            if (addedEntry) {
                blinded = addedEntry.blinded;
                redacted = addedEntry.redacted;
                hidden = addedEntry.hidden;
            }

            var blindedCheckbox = createRadioBtn("blindedReportFields", value, index, blinded);
            var redactedCheckbox = createRadioBtn("protectedReportFields", value, index, redacted);
            var hiddenCheckbox = createRadioBtn("hiddenReportFields", value, index, hidden);

            groupDiv.append('<span class="snippet-demo-container demo-chip demo-chip__basic"><span class="mdl-chip mdl-chip-wrapped"><span class="mdl-chip__text forceLineWrap">'
                + $(this).attr("label").trim() +
                '&nbsp;<span style="display: inline-block;">( ' + blindedCheckbox + ' ${g.message(code:"app.template.blinded")})</span>' + '&nbsp;<span style="display: inline-block;">( ' + redactedCheckbox + ' ${g.message(code:"app.template.protected")})</span>' + '&nbsp;<span style="display: inline-block;">( ' + hiddenCheckbox + ' ${g.message(code:"app.template.hidden")})</span>' +
                '<span class="closebtn" data-evt-clk=\'{\"method\": \"removeParentNode\", \"params\": []}\' >&times;</span><input type="hidden" name="selectedReportFields" value="' + value + '"/></span></span></span>')
        });
        $('#addRemoveFieldModal').modal('hide');
    }
</script>
<style type="text/css">
.pickList {
    margin-left: 155px;
}

.pickList_list {
    width: 250px;
    height: 280px;
}
</style>