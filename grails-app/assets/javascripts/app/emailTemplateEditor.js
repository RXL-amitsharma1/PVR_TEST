var updateTemplateButton, deleteTemplateButton, templateContent = null, currentTemplate;

$(function () {
    $(document).on('focusin', function (e) {
        if ($(e.target).closest(".mce-window").length) {
            e.stopImmediatePropagation();
        }
    });
    if ($("textarea[name=body]").length > 0)
        tinymce.init({
            selector: 'textarea[name=body]',
            height: 500,
            branding: false,
            plugins: 'table image link code',
            menubar: 'edit insert format table tools',
            forced_root_block: 'div',
            promotion: false,
        });
    else
        tinymce.init({
            selector: '.richEditor',
            height: 300,
            branding: false,
            plugins: 'table image link code',
            promotion: false,
            menu: {
                edit: {
                    title: $.i18n._('email.editor.button.edit'),
                    items: 'undo redo | cut copy paste pastetext | selectall'
                },
                insert: {title: $.i18n._('email.editor.button.insert'), items: 'image link media | template hr'},
                view: {title: $.i18n._('email.editor.button.view'), items: 'visualaid'},
                format: {
                    title: $.i18n._('email.editor.button.format'),
                    items: 'bold italic underline strikethrough superscript subscript | formats | removeformat'
                },
                table: {
                    title: $.i18n._('email.editor.button.table'),
                    items: 'inserttable tableprops deletetable | cell row column'
                },
                tools: {title: $.i18n._('email.editor.button.tools'), items: 'spellchecker code'},
                templatemenu: {
                    title: $.i18n._('email.editor.button.templates'),
                    items: 'common users | updatetemplate savetemplate deletetemplate'
                }
            },
            menubar: 'templatemenu edit insert format table tools',
            // content_css: EDITOR_CSS,
            forced_root_block: 'div',
            setup: function (editor) {
                editor.ui.registry.addMenuItem('common', {
                    text: $.i18n._('email.editor.button.templates.load'),
                    context: 'templatemenu',
                    onAction: function () {
                        $("#isUserSpecificTemplate").val(false);
                        loadTemplateList(false, "");
                    }
                });
                editor.ui.registry.addMenuItem('users', {
                    text: $.i18n._('email.editor.button.templates.loadmy'),
                    context: 'templatemenu',
                    onAction: function () {
                        $("#isUserSpecificTemplate").val(true);
                        loadTemplateList(true, "");
                    }
                });
                editor.ui.registry.addMenuItem('updatetemplate', {
                    text: $.i18n._('email.editor.button.templates.update'),
                    context: 'templatemenu',
                    onAction: function () {
                        if (isBodyNotEmpty()) {
                            updateTemplate()
                        }
                    },
                    onSetup: function (api) {
                        if (typeof updateTemplateButton === 'undefined') {
                            updateTemplateButton = api;
                            updateTemplateButton.setEnabled(false);
                        }
                    }
                });
                editor.ui.registry.addMenuItem('savetemplate', {
                    text: $.i18n._('email.editor.button.templates.save'),
                    context: 'templatemenu',
                    onAction: function () {
                        if (isBodyNotEmpty()) {
                            $("#emailTemplateSave").modal("show");
                        }
                    }
                });
                editor.ui.registry.addMenuItem('deletetemplate', {
                    text: $.i18n._('email.editor.button.templates.delete'),
                    context: 'templatemenu',
                    onAction: function () {
                        removeTemplate();
                    },
                    onSetup: function (api) {
                        if (typeof deleteTemplateButton === 'undefined') {
                            deleteTemplateButton = api;
                            deleteTemplateButton.setEnabled(false);
                        }
                    }
                });
                editor.on('OpenWindow', function(e){
                    if (e.win.features.title === 'Insert/edit image') {
                        var widthInput = e.win.$el.find("input")[2];
                        var heightInput =  e.win.$el.find("input")[3];
                        restrictInputToNumbers(widthInput);
                        restrictInputToNumbers(heightInput);
                    }
                });
            }
        });

    function loadTemplateList(isUserSpecificTemplate, searchTerm) {
        var params = {
            isUserSpecificTemplate : isUserSpecificTemplate,
            searchTerm : searchTerm
        };
        $.ajax({
            url: listTemplateUrl,
            type: 'get',
            data: params,
        })
            .fail(function (err) {
                alert((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : ""));
            })
            .done(function (data) {
                templateContent = data.data;
                var html = "";
                for (var i = 0; i < templateContent.length; i++)
                    html += "<div><a class='setTemplateContent click' data-id=\"" + templateContent[i].id + "\">" + templateContent[i].name + "</a></div>\n" +
                        "                <div class=\"small\">" + templateContent[i].description + "</div>\n" +
                        "                  <br>";
                $("#templateListContent").html(html);
                $("#emailTemplateList").modal("show");
            });
    }

    $(document).on("click", ".setTemplateContent", function () {
        setTemplateContent(parseInt($(this).attr("data-id")));
    });
    $(document).on("click", "#saveEmailTemplate", function () {
        var emailTemplateName = $("#emailTemplateName"),
            emailTemplateDescription = $("#emailTemplateDescription");
        if (emailTemplateName.val() === "") {
            emailTemplateName.css("border-color", "red");
            setTimeout(function () {
                emailTemplateName.css("border-color", "#cccccc");
            }, 1000);
            return
        }
        if (emailTemplateDescription.val() === "") {
            emailTemplateDescription.css("border-color", "red");
            setTimeout(function () {
                emailTemplateDescription.css("border-color", "#cccccc");
            }, 1000);
            return
        }
        $.ajax({
            url: saveTemplateUrl,
            type: 'post',
            dataType: 'json',
            data: {
                body: tinyMCE.activeEditor.getContent(),
                name: emailTemplateName.val(),
                description: emailTemplateDescription.val(),
                to: JSON.stringify($("#emailConfiguration\\.to").select2("val")),
                cc: JSON.stringify($("#emailConfiguration\\.cc").select2("val"))
            }
        })
            .fail(function (errorResponse) {
                showConfigurationErrorModal(errorResponse);
            })
            .done(function (data) {
                currentTemplate = data.data;
                updateTemplateButton.setEnabled(true);
                deleteTemplateButton.setEnabled(true);
                $("#emailTemplateSave").modal("hide");
            });

    })
    ;

    $("#emailSearch").on('keyup', function () {
        var searchTerm = $("#emailSearch").val();
        var isUserSpecificTemplate = $("#isUserSpecificTemplate").val();
        loadTemplateList((isUserSpecificTemplate == 'true'), searchTerm);
    });

});

function showConfigurationErrorModal(errorResponse) {
    const errorMessage = (errorResponse.responseJSON.message ? errorResponse.responseJSON.message : "") +
        (errorResponse.responseJSON.stackTrace ? "\n" + errorResponse.responseJSON.stackTrace : "");
    const $emailConfigurationErrorModal = $('#emailConfigurationErrorModal');
    $emailConfigurationErrorModal.find('.modal-body .description').text(errorMessage);
    $emailConfigurationErrorModal.find('#warningModalLabel').text($.i18n._("Error"));
    $emailConfigurationErrorModal.modal('show');
}

function setTemplateContent(id, excludeTo) {
    for (var i = 0; i < templateContent.length; i++) {
        if (templateContent[i].id === id) {
            tinyMCE.activeEditor.setContent(templateContent[i].body);
            if (templateContent[i].description) {
                $("#emailConfiguration\\.subject").val(templateContent[i].description)
            }
            if (templateContent[i].cc)
                $("#emailConfiguration\\.cc").select2("val", [].concat($("#emailConfiguration\\.cc").select2("val")).concat(templateContent[i].cc.split(",")))
            if (templateContent[i].to)
                $("select[name=emailConfiguration\\.to]").select2("val", [].concat($("select[name=emailConfiguration\\.to]").select2("val")).concat(templateContent[i].to.split(",")))
            if (templateContent[i].type.name === "USER") {
                currentTemplate = id;
                updateTemplateButton.setEnabled(true);
                deleteTemplateButton.setEnabled(true);
            } else {
                currentTemplate = null;
                if (updateTemplateButton) updateTemplateButton.setEnabled(false);
                if (deleteTemplateButton) deleteTemplateButton.setEnabled(false);
            }
            break;
        }
    }
    $("#emailTemplateList").modal("hide");
}

function isBodyNotEmpty() {
    if (tinyMCE.activeEditor.getContent() && tinyMCE.activeEditor.getContent().length > 0)
        return true
    var confirmationModal = $("#confirmationModal");
    confirmationModal.modal("show");
    confirmationModal.find('.modalHeader').html($.i18n._('email.editor.confirm.error'));
    confirmationModal.find('.confirmationMessage').html($.i18n._('email.editor.confirm.error.message'));
    confirmationModal.find('.okButton').html($.i18n._('ok'));
    confirmationModal.find('.okButton').off().on('click', function () {
        $("#confirmationModal").modal("hide");
    });
    return false
}

function removeTemplate() {
    var confirmationModal = $("#confirmationModal");
    confirmationModal.modal("show");
    confirmationModal.find('.modalHeader').html($.i18n._('delete.confirm'));
    confirmationModal.find('.okButton').html($.i18n._('yes'));
    confirmationModal.find('.confirmationMessage').html($.i18n._('email.editor.confirm.delete'));
    confirmationModal.find('.okButton').off().on('click', function () {
        $.ajax({
            url: deleteTemplateUrl,
            type: 'get',
            dataType: 'json',
            data: {
                id: currentTemplate
            }
        })
            .fail(function (err) {
                alert((err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace ? "\n" + err.responseJSON.stackTrace : ""));
            })
            .done(function (data) {
                tinyMCE.activeEditor.setContent("");
                updateTemplateButton.setEnabled(false);
                deleteTemplateButton.setEnabled(false);
            });
    });
}

function updateTemplate() {
    $.ajax({
        url: saveTemplateUrl,
        type: 'post',
        data: {
            id: currentTemplate,
            body: tinyMCE.activeEditor.getContent()
        },
        dataType: 'html'
    })
        .fail(function (errorResponse) {
            showConfigurationErrorModal(errorResponse);
        })
        .done(function (data) {
            var confirmationModal = $("#confirmationModal");
            confirmationModal.modal("show");
            confirmationModal.find('.modalHeader').html($.i18n._('email.editor.confirm.success'));
            confirmationModal.find('.confirmationMessage').html($.i18n._('email.editor.confirm.update.success'));
            confirmationModal.find('.okButton').html($.i18n._('ok'));
            confirmationModal.find('.okButton').off().on('click', function () {
                $("#confirmationModal").modal("hide");
            });
        });

}

function restrictInputToNumbers(input) {
    $(input).on('keypress', function (event) {
        var charCode = event.which ? event.which : event.keyCode;
        if (charCode < 48 || charCode > 57) {
            event.preventDefault();
        }
    });
}

