$(function () {
    if( typeof localizationDataUrl !== 'undefined')  bindSelect2WithUrl($("#localizationId"), localizationDataUrl, localizationTextUrl);
    tinymce.init({
        selector: '.richEditor',
        height: 300,
        branding: false,
        plugins: 'table image link code media',
        forced_root_block: 'div',
        promotion: false,
        setup: function (editor) {
        }
    });

    $('#releaseNumber').on('keypress', function (event) {
        var regex = new RegExp("^[0-9|\.]+$");
        var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
        if (!regex.test(key)) {
            event.preventDefault();
            return false;
        }
    });

    $(document).on("change", ".releaseNotesItemTitle", function () {
        $(this).val($(this).val().trim());
    });

    $("#userGroups").select2({
        placeholder: $.i18n._('All'),
        allowClear: true
    });
});