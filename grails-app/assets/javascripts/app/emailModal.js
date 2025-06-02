$(function () {
    var select;
    $(document).on('click', '.copyPasteEmailButton', function (evt) {
        $("#emailCopyPasteWarn").hide();
        $('.confirm-paste-email-values').prop('disabled', false);
        $copyAndPasteEmailModal = $("#copyAndPasteEmailModal");
        select=$(this).parent().find("select");
        var emails = select.select2("val");
        if (emails && emails.length > 0) {
            $copyAndPasteEmailModal.find('.copyPasteContent').val(emails.join(";"));
            $("#semicolon-delimiter").prop('checked', true);
        }else{
            $copyAndPasteEmailModal.find('.copyPasteContent').val("");
        }
        $copyAndPasteEmailModal.modal("show");
    });

    $(document).on('input propertychange', '.copyPasteContent', function (evt) {
        $('.confirm-paste-email-values').prop('disabled', false);
        $("#emailCopyPasteWarn").hide();
    });

    $(document).on('input propertychange', '.c_n_p_other_delimiter', function (evt) {
        $('.confirm-paste-email-values').prop('disabled', false);
        $("#emailCopyPasteWarn").hide();
    });

    $(document).on('change', 'input:radio[name^=delimiter]', function (evt) {
        $('.confirm-paste-email-values').prop('disabled', false);
        $("#emailCopyPasteWarn").hide();
    });

    $(document).on('click', '.confirm-paste-email-values', function (evt) {
        evt.preventDefault();

        var container = $("#copyAndPasteEmailModal");

        delimiter = getCopyAndPasteDelimiter(container);
        var pasteContent = container.find('.copyPasteContent').val();
        if (delimiter !== null) {
            if (delimiter === '|') {
                delimiter = '\\|'
            }
            if (delimiter === '\\n') {
                delimiter = '\n'
            }
        }

        var emails = _.without(_.uniq(_.map(pasteContent.split(delimiter), function (e) {
            return e.trim()
        })), "");
        var isValid = validateEmails(emails);
        var allExist = isValid && checkExistence(emails);//check existence only if all emails are valid
        if (allExist) {
            select.val(emails).trigger("change");
            container.modal('hide');
            $("#emailCopyPasteWarn").hide();
        }
    });

    $(document).on('click', '#closeErrorJustificaton', function (evt) {
        $("#emailCopyPasteError").hide();
    });

    function checkExistence(emails) {
        var emailCopyPasteWarn = $("#emailCopyPasteWarn"),
            emailCopyPasteWarnContent = $("#emailCopyPasteWarnContent"),
            existEmails = [];
        emailCopyPasteWarnContent.html("");
        $("#emailUsers").find("option").each(function () {
            existEmails.push($(this).val());
        });
        var difference = _.difference(emails, existEmails);
        if (difference && difference.length > 0) {
            _.each(difference, function (e) {
                if (emailCopyPasteWarnContent.html() === "")
                    emailCopyPasteWarnContent.html($.i18n._('email.not.exist') + "<br><div class='row'>" +
                        "<div class='col-sm-6'><label>" + $.i18n._('email.description.field') + "</label></div>" +
                        "<div  class='col-sm-6'><label>" + $.i18n._('email') + "</label></div>" +
                        "</div>" + createRow(e));
                else
                    emailCopyPasteWarnContent.html(emailCopyPasteWarnContent.html() + createRow(e));

            });
            emailCopyPasteWarnContent.html(emailCopyPasteWarnContent.html() +
                "<div class='row'><div class='col-sm-5'></div><div class='col-sm-2' id='emailCopyPasteSaveLoading'>&nbsp;</div><div class='col-sm-5'  style='text-align: right'><input type='button' value='" +
                $.i18n._('save') + "' class='btn btn-success emailCopyPasteSave'> </div></div>");
            emailCopyPasteWarn.show();
            $('.confirm-paste-email-values').prop('disabled', true);
            return false;
        }
        return true;
    }

    $(document).on('click', '.emailCopyPasteSave', function (evt) {
        var emailsToAdd = [];
        var emailsToAddInput = $(".emailsToAdd");
        for (var i = 0; i < emailsToAddInput.length; i++) {
            var $emailsToAddInput = $(emailsToAddInput[i]);
            var description = $emailsToAddInput.val();
            if (description === "") {
                $emailsToAddInput.css("border-color", "#a94442");

                setTimeout(function () {
                    $emailsToAddInput.css("border-color", "#ccc");
                }, 2000);
                return
            } else {
                emailsToAdd.push({email: encodeToHTML($emailsToAddInput.attr("data-email")), description: description});
            }
        }

        $('.confirm-paste-email-values').prop('disabled', true);
        $('.emailCopyPasteSave').prop('disabled', true);
        $("#emailCopyPasteSaveLoading").addClass("small-loading");

        $.ajax({
            url: emailAddAllUrl,
            type: 'POST',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify(emailsToAdd)
        })
            .done(function (message) {
                $(".emailUsers").each(function (index, elem) {
                    if (elem.tagName == "SELECT")
                        _.each(emailsToAdd, function (e) {
                            $("<option value='" + e.email + "'>" + e.description + " - " + e.email + "</option>").appendTo($(elem));
                        });
                });
                $("#emailCopyPasteWarnContent").html("<b>" + $.i18n._('email.successfully.added') + "</b>");
                $('.confirm-paste-email-values').prop('disabled', false);
            })
            .fail(function (err) {
                errorNotification((err.responseJSON.message ? err.responseJSON.message : ""));
            });
    });

    function createRow(email) {
        return "<div class='row'>" +
            "<div class='col-sm-6'><input class='form-control emailsToAdd' data-email='" + email + "' value='" + email + "' maxlength='1000'></div>" +
            "<div  class='col-sm-6'><input class='form-control ' disabled value='" + email + "'></div>" +
            "</div>"
    }

    function validateEmails(emails) {
        var emailCopyPasteError = $("#emailCopyPasteError"),
            emailCopyPasteErrorContent = $("#emailCopyPasteErrorContent");

        emailCopyPasteError.hide();
        emailCopyPasteErrorContent.html("");

        _.each(emails, function (e) {
            if (!isEmail(e)) {
                if (emailCopyPasteErrorContent.html() === "")
                    emailCopyPasteErrorContent.html($.i18n._('email.not.valid.error') + " " + encodeToHTML(e));
                else
                    emailCopyPasteErrorContent.html(emailCopyPasteErrorContent.html() + ", " + e)
            }
        });
        if (emailCopyPasteErrorContent.html() !== "") {
            emailCopyPasteError.show();
            return false
        }
        return true
    }

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

    $("#cancelBtn").on('click', function () {
       $(".alert-danger").addClass('hide');
    });

    $(document).on('click', '#errorJustifictaion', function (evt) {
        $(".emailModal").addClass('hide');
    });

});