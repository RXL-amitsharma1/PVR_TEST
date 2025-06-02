$(function () {
  addReadMoreButton(".fieldLevel", 200);
  $(".resizable").resizable();
  $(window).on("resize", function () {
    setIframeHeight();
  });
    $(document).on("mousedown", '.caseContentTable .ui-resizable-handle', function (e) {
        $("#ifremeHover").show();
    });
    $(document).on("mouseup", function (e) {
        $("#ifremeHover").hide();
    });
  var attachmentSize = document
    .getElementById("files")
    .getAttribute("data-attachment-size");
  if (attachmentSize > 0) {
    $("#files").removeAttr("disabled");
    $(".showDocument").removeAttr("disabled");
    showHideAttachment(true);
  } else {
    showHideAttachment(false);
  }

  function showHideAttachment(show) {
    if (show) {
      $(".attachmentContent").parent().show();
    } else {
      $(".attachmentContent").parent().hide();
    }
    setIframeHeight(true);
  }

  function setIframeHeight(reset) {
    var screenHeight = parseInt($(window).height());
    var bottomTableHeight = 0;

    var selectedHeight = parseInt($(".caseContent").parent().height());
    var height =
      screenHeight - parseInt($("iframe").offset().top) - bottomTableHeight;
    if (!reset) height = selectedHeight - 100;
    $("iframe").css({ height: height + "px" });

    height =
      screenHeight -
      parseInt($(".caseContent").offset().top) -
      bottomTableHeight;
    if (!reset) height = selectedHeight - 10;
    $(".caseContent").css({ height: height + "px" });
    $(".caseContent")
      .parent()
      .css({ "max-height": height + "px" });
  }

  setIframeHeight(true);
  $(document).on("click", ".showDocument", function () {
    var file = $("#files");
    $("iframe").attr(
      "src",
      downloadUrl +
        "?id=" +
        encodeURIComponent(file.val()) +
        "&filename=" +
        file.find(":selected").attr("data-filename") +
        "&caseNumber=" +
        caseNumber +
        "&versionNumber=" +
        versionNumber
    );
  });

  $(document).on("click", ".showHideEmptyFields", function () {
    $("label[data-value='']").toggle();
  });

  $(document).on("click", ".showHideAttachments", function () {
    if ($(".attachmentContent").is(":visible")) showHideAttachment(false);
    else showHideAttachment(true);
  });
});
