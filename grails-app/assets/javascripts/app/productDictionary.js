$.each($("input.dictionary-select:hidden"), function (i, element) {
    var additionalDicSelectObj = bindSelect2WithUrl($(element), $(element).attr("data-url"), null, true);
    if ($(element).is('.additionalCriteria, .productSectorSelect') && additionalDicSelectObj) {
        additionalDicSelectObj.on("change", function (e) {
            if ($(this).val()) {
                $("#productsNameDicInput.searchProducts").val(WILD_SEARCH_CHARACTER).trigger('focusout');
            }
        });
    }
});
