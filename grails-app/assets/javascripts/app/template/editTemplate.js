/**
 * Created by gologuzov on 08.09.18.
 */
$(function () {
  enableDisableFixedTemplateButtons();
  $("#browseFixedTemplate").on("click", function (e) {
    $("#fixedTemplateFile").trigger("click");
  });

  $("#fixedTemplateFile").on("change", function (e) {
    if (e.target.files.length > 0) {
      var fileName = e.target.files[0].name;
      $("#fixedTemplateName").val(fileName);
    } else {
      $("#fixedTemplateName").val(null);
    }
    enableDisableFixedTemplateButtons();
  });

  $("#deleteFixedTemplate").on("click", function (e) {
    $("#fixedTemplateFile").val(null);
    $("#fixedTemplateFile").trigger("change");
  });

  function showHideFixedTemplateButtons() {
    if ($("#useFixedTemplate").is(":checked")) {
      $(".fixedTemplateButton").show();
    } else {
      $(".fixedTemplateButton").hide();
    }
  }

  $("#useFixedTemplate").on("change", function () {
    showHideFixedTemplateButtons();
  });

  showHideFixedTemplateButtons();
});

function enableDisableFixedTemplateButtons() {
  if ($("#fixedTemplateName").val() == "") {
    $("#deleteFixedTemplate").addClass("disabled");
    $(".downloadFixedTemplate").addClass("disabled");
  } else {
    $("#deleteFixedTemplate").removeClass("disabled");
    $(".downloadFixedTemplate").removeClass("disabled");
  }
}
