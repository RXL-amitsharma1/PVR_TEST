function showStudyDrugsWidget() {
    var studyDrugsObj = bindSelect2WithUrl($("#studyDrugs"), productsListUrl, null, true);
    studyDrugsObj.on("select2:select", function (e) {
        var selectedItems = $("#studyDrugs").select2('val');
        $("#studyDrugsInput").val(selectedItems).trigger('focusout');
        $("#studyCompounds").val(null).trigger('change');
        $("#studyCompoundsInput").val('');
    });

    var select2Obj = studyDrugsObj.data('select2');
    select2Obj.opts.placeholder = $.i18n._('placeholder.studyDrugs');
    select2Obj.setPlaceholder();
}

function bindImpCheckboxEvent() {
    $('#study_imp').on('click', function (e) {
        var studyDrugsInputObj = $("#studyDrugsInput");
        if (studyDrugsInputObj.val()) {
            studyDrugsInputObj.trigger('focusout');
        }
    });
}