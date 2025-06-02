$(function () {
    var select = $("#reportField");
    getSelect2TreeView(select);
    select.on("select2:select", function (e) {
        const added = e.params.data
        if (added.text != 'Select Field') {
            var defaultExpression = $("#defaultExpression");
            defaultExpression.val(added.element.attributes.argusName.value);
            $("#customName").val(added.text);
            $("#customDescription").val(added.element.attributes.description.value);
            $('#templateDTColumnSelectable').prop('checked', added.element.attributes.templateDTColumnSelectable.value);
            $('#templateDTRowSelectable').prop('checked', added.element.attributes.templateDTRowSelectable.value);
            $('#templateCLLSelectable').prop('checked', added.element.attributes.templateCLLSelectable.value);
            $('#fieldGroupId').val(added.element.attributes.fieldGroup.value).trigger("change");
        }
    });
    $("#fieldGroupId").select2();
});

