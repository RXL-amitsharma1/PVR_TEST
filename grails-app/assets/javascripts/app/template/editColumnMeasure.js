$(function () {
  // For create page
  if ($(".columnMeasureSet").length == 1) {
    addColumnMeasureDiv(0);
    $(".columnMeasureSet").find(".removeColumnMeasure").hide();
    columnSetList.push([]);
    $("#columnMeasureContainer").width(
      $("#columnMeasureContainer").width() - 400
    ); // for scroll
  } else {
    var contentWidth = getColMeasTotalWidth();
    $("#columnMeasureContainer").width(contentWidth + 150);
  }

  $("#addColumnMeasure").on("click", function () {
    if ($(".columnMeasureSet").length <= 9) {
      addColumnMeasureDiv(numColMeas);
      initMeasureSelect2(numColMeas);
      columnSetList.push([]);
      numColMeas++;
      $(".removeColumnMeasure").show();
    }
  });

  $(document).on("click", ".removeColumnMeasure", function () {
    var container = $(this).closest(".columnMeasureSet");
    var index = $(container).attr("sequence");
    measureIndexList[index] = -1;
    columnSetList[index] = [];
    measureList[index] = [];

    var containerWidth = $(container.parentElement).width();
    containerWidth -= $(container).width();
    $(container.parentElement).width(containerWidth);

    $(container).remove();

    if ($(".columnMeasureSet").length == 2) {
      // including the hidden gsp template
      $(".columnMeasureSet").find(".removeColumnMeasure").hide();
    }
    updateColorCellConditionsAfterDelete();
    updateAllTimeframesCheckbox();
  });
});

function addColumnMeasureDiv(index) {
  var container = $("#columnMeasureContainer");

  // clone template for colMeas; change id and sequence
  var cloned = $("#colMeas_template").clone();
  cloned.removeAttr("id");
  cloned.attr("sequence", index).attr("id", "colMeas" + index + "_template");

  cloned.find("#selectMeasure").attr("id", "selectMeasure" + index);

  cloned
    .find("#colMeas-validMeasureIndex")
    .attr("id", "colMeas" + index + "-validMeasureIndex")
    .attr("name", "colMeas" + index + "-validMeasureIndex");

  cloned
    .find("#columns")
    .attr("id", "columns" + index)
    .attr("name", "columns" + index);

  cloned
    .find("#showTotalIntervalCases")
    .attr("id", "showTotalIntervalCases" + index)
    .attr("name", "showTotalIntervalCases" + index);

  cloned
    .find("#showTotalIntervalCasesLabel")
    .attr("id", "showTotalIntervalCasesLabel" + index)
    .attr("for", "showTotalIntervalCases" + index);

  cloned
    .find("#showTotalCumulativeCases")
    .attr("id", "showTotalCumulativeCases" + index)
    .attr("name", "showTotalCumulativeCases" + index);

  cloned
    .find("#showTotalCumulativeCasesLabel")
    .attr("id", "showTotalCumulativeCasesLabel" + index)
    .attr("for", "showTotalCumulativeCases" + index);

  var containerWidth = $(container).width();
  container.append(cloned);
  containerWidth += $(cloned).width();
  $(container).width(containerWidth + 50);
}

function getColMeasTotalWidth() {
  var totalWidth = 0;
  $.each($("#columnMeasureContainer").find(".columnMeasureSet"), function () {
    totalWidth += $(this).width() + 30;
  });
  return totalWidth;
}
