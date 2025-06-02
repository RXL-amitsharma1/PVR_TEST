$(function () {
    $("#locale").select2();
    $("#timeZone").select2();
    bindMultipleSelect2WithUrl($("#tenantsSelectBox"), TenantSearch.availableTenantsUrl, true, false, '', TenantSearch.selectedValues);
    $('.pv-switch input').each((ind, item) => {
        $(item).prop('checked', $(item).attr('data-value') === 'true')
    })
    $('#token-gen-bt').on('click', function (evt) {
        $.ajax({
            url: '/reports/userRest/generateAPIToken',
            method: 'GET',
            dataType: 'json'
        })
            .done(function (data) {
                $('#api-token-field').val(data.token);
            })
            .fail(function (err) {
                console.log(err);
            });

        return false;
    });

    $("#username").select2({
        placeholder: $.i18n._("username.label"),
        minimumInputLength: 3,
        multiple: false,
        ajax: {
            delay: 100,
            dataType: "json",
            url: LDAPSEARCH.ajaxLdapSearchUrl,
            data: function (params) {
                return {
                    term: isValidPattern(params.term) ? params.term : "",
                    max: params.page * 10
                };
            },
            processResults: function (data, params) {
                var more = (params.page * 10) <= data.length;
                return {
                    results: data,
                    pagination: {
                        more: more
                    }
                };
            },
            cache: true
        }
    }).on('select2:select', function (eventData) {
        var data = eventData.params.data
        $('#username').val(data.id);
    });

});

