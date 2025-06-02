function showStudyCompoundsWidget() {

    var studyCompoundsObj = bindSelect2WithUrl($("#studyCompounds"), compoundsListUrl, null, true);
    studyCompoundsObj.on("select2:select", function (e) {
        var selectedItems = $("#studyCompounds").select2('val');
        $("#studyCompoundsInput").val(selectedItems).trigger('focusout');
        $("#studyDrugs").val(null).trigger('change');
        $("#studyDrugsInput").val('');
        $("#study_imp").prop("checked", false);
    });

    var select2Obj = studyCompoundsObj.data('select2');
    select2Obj.opts.placeholder = $.i18n._('placeholder.studyCompounds');
    select2Obj.setPlaceholder();
}