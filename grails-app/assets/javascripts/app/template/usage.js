$(function () {
    var table = $('#rxCheckTemplateUsage').DataTable({
        //"sPaginationType": "bootstrap",
        "layout": {
            topStart: null,
            topEnd: {search: {placeholder: 'Search'}},
            bottomStart: ['pageLength','info', {
                paging: {
                    type: 'full_numbers'
                }
            }],
            bottomEnd: null,
        },
        language: { search: ''},
        "aaSorting": [[ 0, "asc" ]],
        "bLengthChange": true,
        "aLengthMenu": [ [50, 100, 200, 1000, -1], [50, 100, 200, 1000, "All"] ],
        "pagination": true,
        "iDisplayLength": 50,

        drawCallback: function (settings) {
            var sizeOfUsage = $("#sizeOfUsage").val();
            showTotalPage(sizeOfUsage);
            pageDictionary($('#rxCheckTemplateUsage_wrapper')[0], settings.aLengthMenu[0][0], sizeOfUsage);
        },
    });

    $.each($('.dateInTable'), function () {
        $(this).text(moment.utc($(this).text()).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT));
    });

    loadTableOption('#rxCheckTemplateUsage');
});