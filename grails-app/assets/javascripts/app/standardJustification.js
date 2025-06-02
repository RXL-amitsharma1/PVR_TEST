$(function() {
    $('#standard-justifications').select2();

    $(document).on("loadStandardJustifications", function (event, type) {
        resetStandardJustifications();
        $('#standard-justifications-box').show();

        if (pvr.common_util.isJapaneseLocal()) {
            $('#justification-ja-box').show();
            $('#justification-label-postfix').show();
            $('#req-indicator-standard-justification-ja').show();
            $('#req-indicator-standard-justification').hide();
        } else {
            $('#req-indicator-standard-justification-ja').hide();
            $('#req-indicator-standard-justification').show();
        }

        showLoader();
        $.ajax({
            url: standardJustificationsUrl,
            dataType: 'json',
            data: {actionName: type}
        }).done(function (result) {
            hideLoader();
            const $standardJustifications = $('#standard-justifications');
            $standardJustifications.empty().trigger('change');
            if (_.isEmpty(result)) {
                return;
            }
            const textFieldName = (userLocale === JAPANESE_LOCALE) ? 'justificationJ' : 'justification'
            const data = _.map(result, function (item) {
                return {id: item.id, text: item[textFieldName], justification: item.justification, justificationJ: item.justificationJ};
            });
            $standardJustifications.select2({
                allowClear: true,
                placeholder: '',
                data: data
            }).on('select2:clear', function (eventData) {
                if (!pvr.common_util.isJapaneseLocal()) {
                    $('.justification-control-ja').val('').trigger('change');
                }
            }).on('select2:select', function (eventData) {
                const data = $(this).select2('data') || [];
                $('.justification-control-ja').val(data[0].justificationJ).trigger('change');
                $('.justification-control').val(data[0].justification).trigger('change');
            }).val(null).trigger('change');
        });
    });

    $(document).on("hideStandardJustifications", function () {
        if (!pvr.common_util.isJapaneseLocal()) {
            $('#justification-ja-box').hide();
            $('#justification-label-postfix').hide();
        }
        $('.justification-control-ja').val('').trigger('change');
        $('.justification-control').val('').trigger('change');
        $('#standard-justifications-box').hide();
    });

    $(document).on("resetStandardJustifications", function () {
        resetStandardJustifications();
    });

    $(document).on('keyup paste cut change', 'textarea.textarea-countable', function () {
        const $textarea = $(this);
        $textarea.next().find('.textarea-counter-value').text($textarea.val().length);
    });

    function resetStandardJustifications() {
        $('.justification-control-ja').val('').trigger('change');
        $('.justification-control').val('').trigger('change');
        $('#standard-justifications').val(null).trigger('change');
    }
});