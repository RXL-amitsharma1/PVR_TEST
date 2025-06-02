var delimiter = null;

$(function () {
    var selectedField;
    $("input.searchProducts, input.searchStudies, input.searchEvents").on("focusin", function () {
        showPencilIcon($(this));
        selectedField = $(this);
    }).on("focusout", function (event) {
        var self = $(this);
        setTimeout(function () {
            hidePencilIcon(self);
        }, 500);
    });
    $('#copyAndPasteDicModal').on('show.bs.modal', function () {
        resetMultiSearchModal();
        $("#productModal").css('z-index', 1000);
        $("#eventModal").css('z-index', 1000);
        $("#studyModal").css('z-index', 1000);
    });

    $('#copyAndPasteDicModal').on('hidden.bs.modal', function () {
        $("#productModal").css('z-index', 1050);
        $("#eventModal").css('z-index', 1050);
        $("#studyModal").css('z-index', 1050);
    });
    function showPencilIcon(elem) {
        elem.prev().find(".fa-pencil-square-o").removeClass("hidden");
    }

    function hidePencilIcon(elem) {
        elem.prev().find(".fa-pencil-square-o").addClass("hidden");
    }

    $(document).on('click', '.confirm-paste-dic-values', function (evt) {
        evt.preventDefault();

        var container = $("#copyAndPasteDicModal");

        delimiter = getCopyAndPasteDelimiter(container);
        var pasteContent = container.find('.copyPasteContent').val();
        var level = selectedField.attr("level");

        if (delimiter != null) {
            if (delimiter == '|') {
                delimiter = '\\|';
            }

            if (delimiter == '\\n') {
                delimiter = '\\\\n+';
                pasteContent = pasteContent.replace(/\n\r?/g, '\\n');
            }
        }

        if (pasteContent != null && !_.isEmpty(pasteContent)) {
            selectedField.val(pasteContent);
            selectedField.trigger('focusout');
        }
    });

    function getCopyAndPasteDelimiter(container) {
        var selectedValue = $(container).find('input:radio[name^=delimiter]:checked').val();
        if (selectedValue === 'none') {
            return null;
        } else if (selectedValue === 'others') {
            var value = $(container).find('.c_n_p_other_delimiter').val();
            if (_.isEmpty(value)) {
                return null;
            }
            else {
                return value;
            }
        } else {
            return selectedValue;
        }
    }
});