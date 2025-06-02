var sharedWithModalShow = false;
var emailToModalShow = false;
var emailModalInited = false;
$(function () {
    // For shared with modal
    $('#shareWith').select2().on("change", function (e) {
        $('#shareWith').parent().removeClass('has-error');
    });
    $('#sharedWithModal').on('show.bs.modal', function (e) {
        var executedConfigId = e.relatedTarget.id;
        $('#executedConfigId').val(executedConfigId);
        $('#sharedWith').val(null).trigger('change');
        $('#sharedWith').parent().removeClass('has-error');
        $('#share-with-required-alert').hide();

        $.ajax({
            cache: false,
            type: 'GET',
            url: getSharedWith + '?id=' + executedConfigId,
            dataType: 'json'
        })
            .done(function (result) {
                var users = '';
                $.each(result.users, function () {
                    users += this.fullName + ' (' + this.username + ') <br />'
                });
                $('#sharedWithUserList').html(users);
                var groups = '';
                $.each(result.groups, function () {
                    groups += this.name + ' <br />'
                });
                $('#sharedWithGroupList').html(groups);
            });

        sharedWithModalShow = true;

    }).on('hide.bs.modal', function (e) {
        sharedWithModalShow = false;
    });
    // For email to modal
//    $('#emailUsers').select2().on("change", function (e) {
//        $('#emailUsers').parent().removeClass('has-error');
//    });
    $('#emailToModal').on('show.bs.modal submissionModalOpen', function (e, id) {
        var executedConfigId = id ? id : e.relatedTarget.id;
        $('#executedConfigId').val(executedConfigId);
        $('#emailUsers').val(null).trigger('change');
        $('#emailUsers').parent().removeClass('has-error');
        $('#formatError').hide();
        $('#email-to-required-alert').hide();

        // clear checkbox for attachemnt formats
        $('.emailOption').prop("checked", false);
        if ($("#pvpSectionsSelect").length > 0) {
            var initSelect = function (selector, url) {
                $(selector).select2({
                    multiple: true,
                    ajax: {
                        url: url + "?id=" + executedConfigId,
                        dataType: "json",
                        type: "GET",
                        delay: 50,
                        data: function (params) {
                            return {
                                term: params.term
                            };
                        },
                        processResults: function (data) {
                            return {
                                results: data
                            }
                        }
                    }
                });
            }
            initSelect("#pvpSectionsSelect", pvpSectionsUrl)
            initSelect("#pvpFullDocumentSelect", pvpFullDocumentUrl)
        }
        emailToModalShow = true;
        if (emailToModalShow) {
            $.ajax({
                url: addEmailConfiguration,
                data: {id: executedConfigId},
                dataType: 'json'
            })
                .done(function (result) {
                    emailConfig(!emailModalInited, result);
                    emailModalInited = true;
                    $(".showEmailConfigurationLoading").hide();
                    $(".showEmailConfiguration").show();
                });
        }

    }).on('hide.bs.modal', function (e) {
        emailToModalShow = false;
    }).on("shown.bs.modal", function () {
        setSelect2InputWidth($(this).find("#emailUsers"));
    });

    $("[data-evt-sbt]").on('submit', function() {
        const eventData = JSON.parse($(this).attr("data-evt-sbt"));
        const methodName = eventData.method;
        const params = eventData.params;
        // Call the method from the eventHandlers object with the params
        if (methodName == 'submitForm') {
            return submitForm();
        }
    });
});

function submitForm() {
    var validInfo = true;
    if (sharedWithModalShow) {
        if ($('#sharedWith').select2('val') == '') {
            validInfo = false;
            $('#sharedWith').parent().addClass('has-error');
            $('#share-with-required-alert').show();
        }
    }
    if (emailToModalShow) {
        var email = $("#emailUsers").val();
        if (!email) {
            validInfo = false;
            $('#email-to-required-alert').show();
        }
    }
    return validInfo;
}