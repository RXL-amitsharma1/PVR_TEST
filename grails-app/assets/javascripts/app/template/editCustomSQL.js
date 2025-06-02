$(function () {
    bindSelect2WithUrl($('.drillDownTemplate'), customSQLTemplateSearchUrl, templateNameUrl, true).on("select2:open", function (e) {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
            searchField[0].focus();
        }
    });
    $(document).on("change", ".drillDownTemplate", function () {
        var drillDownTemplateValue = $(this).val();
        if (drillDownTemplateValue != '' && drillDownTemplateValue != null) {
            $('.templateViewButton').attr('href', templateViewUrl + '/' + drillDownTemplateValue);
            $('.templateViewButton').removeClass('hide');
        } else {
            $('.templateViewButton').addClass('hide');
        }
    });

});

