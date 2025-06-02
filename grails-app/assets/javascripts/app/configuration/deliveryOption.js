$(function () {
  $(".selectlist.timezone.timezone-select-div  .dropdown-menu").css(
    "width",
    "334"
  );

  var create = " ";

  initSelect2ForEmailUsers(".emailUsers");
  $(document).on("click", ".addEmail", function () {
    var id = $(this).data("id").replace(".", "\\.");
    var select = $("#" + id);
    var email = $("#newEmail" + id).val();
    var description = $("#newEmailDescription" + id).val();
    if (!validateNewEmailData(email, description)) {
      return;
    }
    $.ajax({
      url: emailAddUrl,
      type: "post",
      data: { email: email, description: description },
      dataType: "json",
    })
        .done(function (response) {
          if (response.status) {
            $(".emailUsers").each(function (index, elem) {
              if (elem.tagName === "SELECT")
                $(
                    "<option value='" +
                    email +
                    "'>" +
                    encodeToHTML(description) +
                    " - " +
                    email +
                    "</option>"
                ).appendTo($(elem));
            });
            var selectedItems = select.select2("val");
            selectedItems.push(email);
            select.val(selectedItems);
            select.select2("close");
          } else {
            showAddEmailValidationAlert(response.message);
          }
        })
        .fail(function (XMLHttpRequest, textStatus, errorThrown) {
          if (XMLHttpRequest.readyState === 4) {
            // HTTP error (can be checked by XMLHttpRequest.status and XMLHttpRequest.statusText)
            if (XMLHttpRequest.status === 401) {
              alert($.i18n._("app.session.expired.error"));
            } else {
              console.log(XMLHttpRequest.status);
              console.log(XMLHttpRequest.statusText);
            }
          } else if (XMLHttpRequest.readyState === 0) {
            // Network error (i.e. connection refused, access denied due to CORS, etc.)
            alert($.i18n._("app.network.error"));
          } else {
            // something weird is happening
            alert($.i18n._("app.ajax.unrecognized.error"));
          }
        });
  });

  $("#saveEmailConfiguration").on("click", function () {
    updateEmailAttachmentTypesEditable($("#emailUsers").val());
  });

  $("#emailUsers").on("select2:select select2:unselect change", function () {
    updateEmailAttachmentTypesEditable($(this).val());
  });

  function updateEmailAttachmentTypesEditable(selectedEmailUsers) {
    if (selectedEmailUsers == undefined || !selectedEmailUsers.length) {
      $("#attachmentCheckboxes").hide();
      $("#emailAttachmentAll input[type=checkbox]").prop("checked", false);
    } else {
      $("#attachmentCheckboxes").show();
    }
  }

  $(document).on("change", ".dmsEnabled", function (event) {
    if (this.checked) {
      $("input[name$=format]").removeAttr("disabled");
    } else {
      $("input[name$=format]").attr("disabled", "disabled");
    }
  });

  $(".dmsEnabled").trigger("change");
});

function initSelect2ForEmailUsers(selector) {
  var promises = [];
  $(selector).not('.select2-container').each(function () {
    var selectorObj = $(this);
    var ajaxPromise = $.ajax({
      url: selectorObj.data("options-url"),
      dataType: "json",
    })
      .done(function (data) {
        var selectedValues = selectorObj.data("value");
        $.map(data, function (x) {
          selectorObj.append(
            $("<option></option>").attr("value", x.key).text(x.value)
          );
          return {
            id: x.key,
            text: x.value,
          };
        });
        selectorObj.select2({
            placeholder: $(selectorObj).attr("placeholder")
              ? $(selectorObj).attr("placeholder")
              : $.i18n._("placeholder.emailAddress"),
            allowClear: true,
            width: $(selectorObj).data("width") ?? "95%",
          language: {
            noResults: function () {
              var term = selectorObj.parent().find(".select2-search__field").val();
              var id = $(selectorObj).attr("id");
              var emailInvalidAlert = $("#email-invalid-alert");
              if (emailInvalidAlert.length > 0)
                emailInvalidAlert[0].classList.remove("hide");
              emailInvalidAlert.hide();
              create =
                "<div style='background: #eeeeee;padding: 5px'>" +
                (emailInvalidAlert[0] ? emailInvalidAlert[0].outerHTML : "") +
                "<input class='form-control newEmail' placeholder='" +
                $.i18n._("email.email.email") +
                "' id='newEmail" +
                id +
                "' value='" +
                term +
                "' maxlength='200'>" +
                "<input style='margin-top: 3px;margin-bottom: 3px;' class='form-control newEmail' id='newEmailDescription" +
                id +
                "' placeholder='" +
                $.i18n._("email.description") +
                "' maxlength='1000'>" +
                "<button  data-id='" +
                id +
                "' class='btn btn-success addEmail'>" +
                $.i18n._("email.add") +
                "</button></div>";
              return create;
            }
          },
          escapeMarkup: function (markup) {
            return markup;
          }
        })
          .on("click", ".inline-alert-close", function () {
            //because formatNoMatches generates html dynamically, the common inline-alert-close listener will not work
            $(this).closest(".closable-inner-alert").hide();
          });
        $("#emailUsers").parent().find(".select2-search__field").attr("maxlength", 200);

        if (selectedValues) {
          selectorObj.val(selectedValues.split(",")).trigger("change");
        }
      })
      .fail(function (jqXHR, exception) {
        var msg = "";
        if (jqXHR.status === 0) {
          msg = "Not connect.\n Verify Network.";
        } else if (jqXHR.status == 404) {
          msg = "Requested page not found. [404]";
        } else if (jqXHR.status == 500) {
          msg = "Internal Server Error [500].";
        } else if (exception === "parsererror") {
          msg = "Requested JSON parse failed.";
        } else if (exception === "timeout") {
          msg = "Time out error.";
        } else {
          msg = "Uncaught Error.\n" + jqXHR.responseText;
        }
        alert(msg);
      });
    promises.push(ajaxPromise);
  });
  $.when.apply($, promises).done(function () {
    $(document).on("keyup", selector, function (event) {
      if (event.keyCode == 13) {
        $(".addEmail").trigger("click");
      }
    });
    $(selector).trigger("change");
  });
}

function validateNewEmailData(email, description) {
  var validationMessage = "";
  if (!email) {
    validationMessage += $.i18n._("email.email.required") + "<br>";
  } else if (!isEmail(email)) {
    validationMessage += $.i18n._("email.format.invalid") + "<br>";
  }
  if (!description) {
    validationMessage += $.i18n._("email.description.required") + "<br>";
  }
  if (validationMessage) {
    showAddEmailValidationAlert(validationMessage);
    return false;
  }
  return true;
}
function showAddEmailValidationAlert(message) {
  if (!message) {
    return;
  }
  var emailInvalidAlert = $(".select2-results #email-invalid-alert");
  if (emailInvalidAlert.length > 0) {
    emailInvalidAlert.hide();
    emailInvalidAlert.find(".alert-text").html(message);
    emailInvalidAlert.show();
  } else {
    alert(message);
  }
}

function isEmail(email) {
  var regex = /\S+@\S+\.\S+/;
  return regex.test(email);
}
