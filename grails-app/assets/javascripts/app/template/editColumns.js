var SORT_ASCENDING = "asc";
var SORT_DESCENDING = "desc";
var SORT_DISABLED = "disabled";

var newColumnList = [];
var newGroupingList = [];
var newRowColList = [];
var newRowList = [];
var numOfReassessListedness = 0;
var undoTagList = [];
var removeLastTag = 0;

var columnSetList = []; // for data tabulation templates
var sequence_col_dt = [];
var iggnoreDrilldownWarning = false;
var iggnoreWorldMapWarning = false;

function getTemplateReportFieldOptions(data) {
  var html = "";
  html =
    html +
    $("<option>", { value: "" }).text(
      $.i18n._("dataTabulation.select.field")
    )[0].outerHTML;
  $.each(data, function (index, fieldGroup) {
    var group = $("<optgroup>", { label: fieldGroup.group });
    $.each(fieldGroup.children, function (index, field) {
      group.append(
        $("<option>", {
          argusName: field.argusName,
          reportFieldName: field.reportFieldName,
          description: encodeToHTML(field.description),
          legend: field.legend,
          customExpression: encodeToHTML(field.customExpression),
          customFieldId: field.customFieldId,
          fromBlindedList: field.fromBlindedList,
          fromProtectedList: field.fromProtectedList,
          clobType: field.clobType,
          dateType: field.dateType,
          fieldLabelJ: encodeToHTML(field.fieldLabelJ),
          fieldLabelE: encodeToHTML(field.fieldLabelE),
          value: field.value,
        }).text(encodeToHTML(field.displayText))
      );
    });
    html = html + group[0].outerHTML;
  });
  return html;
}

$(function () {
  /*  This is set affirmatively to false in view.gsp only by a hidden field, so the expression below will eval to true.
     In create/edit, it's not set as a hidden field, so the expression below will eval to false - morett
     */
  viewOnly = $("#editable").val() === "false";
  var templateType = $("#templateType").val();

  var colSelected;
  var sequence_col = 0;
  var sortLevels = 5;
  var stackId = 1;
  var stackedColumnsList = {};
  var sequence_row = 0;
  var sequence_group = 0;
  var sequence_rowColumn = 0;

  var sortObjList = [];
  var sortLevel = 1;
  var MAX_SORT_LEVELS = 5;

  var COLUMN = "column";
  var ROW = "row";
  var GROUP = "group";
  var ROW_COL = "rowCol";
  var RE_ASSESS_LISTEDNESS = "Re-assess Listedness";
  var RE_ASSESS_LISTEDNESS_J_LABEL = "Re-assess Listedness (J)";
  var RE_ASSESS_LISTEDNESS_E_LABEL = "再評価された一覧表示 (E)";
  var RE_ASSESS_LISTEDNESS_J = "再評価された一覧表示";

  var TYPE_CASE_LINE = "CASE_LINE";
  var TYPE_DATA_TAB = "DATA_TAB";
  var TYPE_CUSTOM_SQL = "CUSTOM_SQL";
  var TYPE_NON_CASE = "NON_CASE";
  var REPORT_TEMPLATE_TYPE = {
    CLL: "CLL",
    DT_ROW: "DT_ROW",
    DT_COLUMN: "DT_COLUMN",
  };

  var addToDT = COLUMN;

  $("#templtCustomDateSelector").datepicker({
    allowPastDates: true,
    date: $("[name='templtReassessDate']").val(),
    momentConfig: {
      culture: userLocale,
      format: DEFAULT_DATE_DISPLAY_FORMAT,
    },
  });

  $("[name='reassessListedness']").on("change", function () {
    if ($(this).val() === "CUSTOM_START_DATE") {
      $("#templtCustomDateSelector").show();
    } else $("#templtCustomDateSelector").hide();
  });

  function loadTemplateReportFields(templateType, url, cache, sourceId) {
    if (templateType === TYPE_CASE_LINE) {
      $.ajax({
        type: "GET",
        url: url,
        cache: !!cache,
        data: {
          sourceId: sourceId ? sourceId : "",
          selectionType: REPORT_TEMPLATE_TYPE.CLL,
        },
        dataType: "json",
        beforeSend: function () {
          $("#cLLReportFieldDropDownLoading").show();
          $("#cLLReportFieldDropDown").hide();
        },
      })
        .always(function () {
          $("#cLLReportFieldDropDownLoading").hide();
          $("#cLLReportFieldDropDown").show();
        })
        .fail(function (err) {
          console.log(err);
        })
        .done(function (result) {
          if (result && result.data) {
            var html = getTemplateReportFieldOptions(result.data);
            $("#cLLReportFieldDropDown select").html(html);
            initSelectField_lineListing();
          }
        });
    } else if (templateType === TYPE_DATA_TAB) {
      $("#dTTReportFieldDropDownLoading").show();
      $("#dTTReportFieldDropDown").hide();
      var rowReportFields = $.ajax({
        type: "GET",
        url: url,
        cache: !!cache,
        data: {
          sourceId: sourceId ? sourceId : "",
          selectionType: REPORT_TEMPLATE_TYPE.DT_ROW,
        },
      });

      var columnReportFields = $.ajax({
        type: "GET",
        url: url,
        cache: !!cache,
        data: {
          sourceId: sourceId ? sourceId : "",
          selectionType: REPORT_TEMPLATE_TYPE.DT_COLUMN,
        },
      });

      $.when(rowReportFields, columnReportFields)
        .done(function () {
          $("#dTTReportFieldDropDownLoading").hide();
          $("#dTTReportFieldDropDown").show();
          if (
            rowReportFields.status === 200 &&
            columnReportFields.status === 200
          ) {
            if (
              rowReportFields.responseJSON &&
              rowReportFields.responseJSON.data
            ) {
              $("#dTTReportFieldDropDown #dtRowSelect2 select").html(
                getTemplateReportFieldOptions(rowReportFields.responseJSON.data)
              );
            }
            if (
              columnReportFields.responseJSON &&
              columnReportFields.responseJSON.data
            ) {
              $("#dTTReportFieldDropDown #dtColumnSelect2 select").html(
                getTemplateReportFieldOptions(
                  columnReportFields.responseJSON.data
                )
              );
            }
            initSelectField_dataTabulation();
          } else {
            console.log(rowReportFields);
            console.log(columnReportFields);
          }
        })
        .fail(function () {
          console.log(rowReportFields);
          console.log(columnReportFields);
          $("#dTTReportFieldDropDownLoading").hide();
          $("#dTTReportFieldDropDown").show();
        });
    }
  }

  function initSelectField_lineListing() {
    getSelect2TreeView($("#selectField_lineListing"))
      .off()
      .on("select2:select", function (e) {
        const element = e.params.data.element
        const added = JSON.parse(JSON.stringify(e.params.data));
        if (added.text == "Select Field") {
          $("#selectField_lineListing").parent().addClass("has-error");
        } else {
          $("#selectField_lineListing").parent().removeClass("has-error");
          var container = $($(".selectedContainerBorder")[0]).find(
            ".containerToBeSelected"
          )[0];
          var comparedToContainers = getComparedContainers(this, container);
          var tagArray = [added, container];
          if (
            !isDuplicate(
              element.attributes.reportFieldName.value,
              comparedToContainers
            )
          ) {
            added.argusName = element.attributes.argusName.value;
            added.reportFieldName = element.attributes.reportFieldName.value;
            added.description = element.attributes.description.value;
            added.fromBlindedList =
              element.attributes.fromBlindedList.value == "true";
            added.fromProtectedList =
              element.attributes.fromProtectedList.value == "true";
            added.blindedValue =
              element.attributes.fromBlindedList.value == "true";
            added.redactedValue =
              element.attributes.fromProtectedList.value == "true";
            added.dateType = element.attributes.dateType.value;
            added.legend = element.attributes.legend.value;
            added.customExpression =
              element.attributes.customExpression.value;
            added.customExpression = unescapeHTML(
              element.attributes.customExpression.value
            );
            if (added.redactedValue && !added.blindedValue) {
              added.customExpression =
                "decode (cm.EXC_FRM_PRIVACY_LOCATION_ID,1," + added.argusName + ",'Redacted')";
            }
            added.customFieldId = element.attributes.customFieldId.value;
            added.clobType = element.attributes.clobType.value;
            added.fieldLabelJ = element.attributes.fieldLabelJ.value;
            added.fieldLabelE = element.attributes.fieldLabelE.value;
            delete added.renameValue;
            undoTagList.push(tagArray);
            if (undoTagList.length > 0) {
              $("#resetTagId").show();
            }
            if ($(container).hasClass("groupingContainer")) {
              addFieldToCLLGrouping(added, container);
            } else if ($(container).hasClass("rowColumnsContainer")) {
              addFieldToCLLRowCols(added, container);
            } else {
              addFieldToColumns(added, container);
            }
          }
        }
      })
      .on("select2:open", function () {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
          searchField[0].focus();
        }
        $("#selectField_lineListing").val("Select Field").trigger("change");
        $(document).on("keyup keydown", function (event) {
          shifted = event.shiftKey;
        });
      })
      .on("select2:close", function (e) {
        if (shifted) {
          $("#selectField_lineListing").select2("open");
        }
      });
  }

  function initSelectField_dataTabulation() {
    getSelect2TreeView($("#selectField_dataTabulation_" + COLUMN))
      .off()
      .on("select2:select", function (e) {
        dtOnChange(e, this, COLUMN);
      })
      .on("select2:open", function () {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
          searchField[0].focus();
        }
        $("#selectField_dataTabulation_" + COLUMN).val(null).trigger('change');
        $(document).on("keyup keydown", function (event) {
          shifted = event.shiftKey;
        });
      })
      .on("select2:close", function (e) {
        if (shifted) {
          $("#selectField_dataTabulation_" + COLUMN).select2("open");
        }
      });

    getSelect2TreeView($("#selectField_dataTabulation_" + ROW))
      .off()
      .on("select2:select", function (e) {
        dtOnChange(e, this, ROW);
      })
      .on("select2:open", function () {
        var searchField = $('.select2-dropdown .select2-search__field');
        if (searchField.length) {
          searchField[0].focus();
        }
        $("#selectField_dataTabulation_" + ROW)
          .val("Select Field")
          .trigger("change");
        $(document).on("keyup keydown", function (event) {
          shifted = event.shiftKey;
        });
      })
      .on("select2:close", function (e) {
        if (shifted) {
          $("#selectField_dataTabulation_" + ROW).select2("open");
        }
      });
  }

  function dtOnChange(e, select2, type) {
    const added = e.params.data;
    if (added.text == "Select Field") {
      $("#selectField_dataTabulation_" + type)
        .parent()
        .addClass("has-error");
    } else {
      $("#selectField_dataTabulation_" + type)
        .parent()
        .removeClass("has-error");
      var container = $($(".selectedContainerBorder")[0]).find(
        ".containerToBeSelected"
      )[0];
      var index = $(container).closest(".columnMeasureSet").attr("sequence");

      var comparedToContainers = getComparedContainers(select2, container);
      if (
        !isDuplicate(
          added.element.attributes.reportFieldName.value,
          comparedToContainers
        )
      ) {
        added.argusName = added.element.attributes.argusName.value;
        added.reportFieldName = added.element.attributes.reportFieldName.value;
        added.description = added.element.attributes.description.value;
        added.fromBlindedList =
          added.element.attributes.fromBlindedList.value == "true";
        added.fromProtectedList =
          added.element.attributes.fromProtectedList.value == "true";
        added.blindedValue =
          added.element.attributes.fromBlindedList.value == "true";
        added.redactedValue =
          added.element.attributes.fromProtectedList.value == "true";
        added.dateType = added.element.attributes.dateType.value;
        added.legend = added.element.attributes.legend.value;
        added.customExpression =
          added.element.attributes.customExpression.value;
        if (added.redactedValue && !added.blindedValue) {
          added.customExpression =
            "decode (cm.EXC_FRM_PRIVACY_LOCATION_ID,1," + added.argusName + ",'Redacted')";
        }
        added.customFieldId = added.element.attributes.customFieldId.value;
        added.fieldLabelJ = added.element.attributes.fieldLabelJ.value;
        added.fieldLabelE = added.element.attributes.fieldLabelE.value;
        if (type == ROW) {
          if ($(container).hasClass("groupingContainer")) {
            addFieldToCLLGrouping(added, container);
          } else {
            addFieldToDTRows(added, container);
          }
        } else {
          added.clobType = added.element.attributes.clobType.value;
          addFieldToColumns(added, container, index);
        }
        updateAllTimeframesCheckbox();
      }
    }
  }

  // ------------------------------------------------------------------------------------------------------ END EVENTS

  // Helper methods --------------------------------------------------------------------------------------------------

  function getComparedContainers(obj, currentContainer) {
    var containersList = $(".rowsAndColumnsContainer");
    var comparedToContainers = [];
    // Only columns in CLL allow duplicates
    if (
      !(
        $(currentContainer).hasClass("columnsContainer") &&
        templateType == TYPE_CASE_LINE
      )
    ) {
      comparedToContainers.push(currentContainer);
    }
    $.each(containersList, function () {
      if (!$(this).hasClass("selectedContainerBorder")) {
        if (
          !(
            $(currentContainer).hasClass("columnsContainer") &&
            $(this).hasClass("columnsContainer")
          )
        ) {
          comparedToContainers.push(this);
        }
      }
    });
    return comparedToContainers;
  }

  function isDuplicate(selectedFieldId, comparedToContainers) {
    var isDuplicate = false;
    var totalFields = [];
    $.each(comparedToContainers, function () {
      $.each($(this).find(".fieldInfo"), function () {
        totalFields.push(this);
      });
    });

    $.each(totalFields, function () {
      this.parentElement.style.border = "";
      if ($(this).attr("fieldId") == selectedFieldId) {
        isDuplicate = true;
        $("#selectField_dataTabulation_" + addToDT)
          .parent()
          .addClass("has-error");
        this.parentElement.style.border = "solid 1px red";
      }
    });
    return isDuplicate;
  }

  function isFirst(stackId) {
    var isFirstInStack = true;
    if (stackId > 0) {
      var columns = $("[id^=columnsContainer]").find(".fieldInfo");
      $.each(columns, function () {
        if ($(this).attr("stackId") == stackId) {
          isFirstInStack = false;
        }
      });
    }
    return isFirstInStack;
  }

  function addCLLSet(index) {
    var cnt = $(".row.columnsContainer").first().clone();
    cnt.find(".containerToBeSelected").attr("id", "columnsContainer" + index);
    cnt.find(".containerToBeSelected").attr("data-setid", index);
    cnt.find(".containerToBeSelected").empty();
    $(".selectedContainerBorder").removeClass("selectedContainerBorder");
    cnt.addClass("selectedContainerBorder");

    $("#addCLLSet").before(
      $(
        "<i id='removeCLLSet" +
          index +
          "' class='fa fa-times removeCLLSet' data-setid='" +
          index +
          "'></i>"
      )
    );
    $("#addCLLSet").before(cnt);
  }

  function addFieldToColumns(reportField, containerToAppend, dtIndex) {
    var seq = sequence_col;
    if (dtIndex > -1) {
      if (sequence_col_dt[dtIndex]) {
        seq = sequence_col_dt[dtIndex];
      } else {
        seq = 0;
      }
    }
    var reportFieldInfo = reportField;
    reportFieldInfo.type = COLUMN;
    reportFieldInfo.sequence = seq;
    if (templateType == TYPE_DATA_TAB) {
      reportFieldInfo.colMeasIndex = $(containerToAppend)
        .closest(".columnMeasureSet")
        .attr("sequence");
    }
    if (templateType == TYPE_CASE_LINE) {
      reportFieldInfo.setId = parseInt(
        $(containerToAppend).closest(".columnsContainer").attr("data-setid")
      );
    }

    var baseCell = document.getElementById("toAddColumn");
    var inStack = !isFirst(reportField.stackId);

    var clonedNodes = getClonedNodes(baseCell, inStack);
    var cellToAdd = clonedNodes[0];
    var fieldInfo = clonedNodes[1];

    addFieldInfo(reportField, fieldInfo, seq);
    addIcons(cellToAdd, fieldInfo, reportFieldInfo, false);
    if (templateType === TYPE_DATA_TAB) {
      hideEmptyContent(fieldInfo);
    } else {
      addToStackList(reportField, sequence_col);
    }

    if (inStack) {
      var lastIndex = $("[stackId=" + reportField.stackId + "]").length - 1;
      var lastInStack = $("[stackId=" + reportField.stackId + "]")[lastIndex];
      lastInStack.parentElement.insertBefore(
        cellToAdd,
        lastInStack.nextSibling
      );
    } else if (templateType === TYPE_DATA_TAB) {
      // Add data tabulation columns
      insertVerticalField(cellToAdd, containerToAppend);
    } else {
      insertHorizontalField(cellToAdd, containerToAppend);
    }

    addListenersForRowCol(cellToAdd);

    if (dtIndex > -1) {
      if (sequence_col_dt[dtIndex]) {
        sequence_col_dt[dtIndex]++;
      } else {
        sequence_col_dt[dtIndex] = 1;
      }
      columnSetList[dtIndex].push(reportFieldInfo);
    } else {
      sequence_col++;
      newColumnList.push(reportFieldInfo);
    }
    if (templateType === TYPE_DATA_TAB) {
      sequence_col++;
      newColumnList.push(reportFieldInfo);
    }
    $('[data-toggle="popover"]').popover();
  }

  function addFieldToCLLGrouping(reportField, containerToAppend) {
    var reportFieldInfo = reportField;
    reportFieldInfo.type = GROUP;
    reportFieldInfo.sequence = sequence_group;

    var baseCell = document.getElementById("toAddColumn");

    var clonedNodes = getClonedNodes(baseCell, false);
    var cellToAdd = clonedNodes[0];
    var fieldInfo = clonedNodes[1];

    addFieldInfo(reportField, fieldInfo, sequence_group);
    addIcons(
      cellToAdd,
      fieldInfo,
      reportFieldInfo,
      templateType == TYPE_DATA_TAB
    );
    hideEmptyContent(fieldInfo);
    insertHorizontalField(cellToAdd, containerToAppend);
    addListeners(cellToAdd);

    sequence_group++;
    newGroupingList.push(reportFieldInfo);
    $('[data-toggle="popover"]').popover();
  }

  function addFieldToCLLRowCols(reportField, containerToAppend) {
    var reportFieldInfo = reportField;
    reportFieldInfo.type = ROW_COL;
    reportFieldInfo.sequence = sequence_rowColumn;

    var baseCell = document.getElementById("toAddColumn");

    var clonedNodes = getClonedNodes(baseCell, false);
    var cellToAdd = clonedNodes[0];
    var fieldInfo = clonedNodes[1];

    addFieldInfo(reportField, fieldInfo, sequence_rowColumn);
    addIcons(cellToAdd, fieldInfo, reportFieldInfo, true);
    hideEmptyContent(fieldInfo);
    insertVerticalField(cellToAdd, containerToAppend);
    addListeners(cellToAdd);

    sequence_rowColumn++;
    newRowColList.push(reportFieldInfo);
    $('[data-toggle="popover"]').popover();
  }

  function addFieldToDTRows(reportField, containerToAppend) {
    var reportFieldInfo = reportField;
    reportFieldInfo.type = ROW;
    reportFieldInfo.sequence = sequence_row;

    var baseCell = document.getElementById("toAddRow");

    var clonedNodes = getClonedNodes(baseCell, false);
    var cellToAdd = clonedNodes[0];
    var fieldInfo = clonedNodes[1];

    addFieldInfo(reportField, fieldInfo, sequence_row);
    addIcons(cellToAdd, fieldInfo, reportFieldInfo, true);

    containerToAppend.insertBefore(cellToAdd, containerToAppend.firstChild);
    var rowContainerWidth = $(containerToAppend).width();
    var rowContentWidth = getRowContentWidth(containerToAppend);
    if (rowContentWidth > rowContainerWidth) {
      rowContainerWidth += rowContentWidth - rowContainerWidth + 5;
      $(containerToAppend).width(rowContainerWidth);
    }

    addListeners(cellToAdd);

    sequence_row++;
    newRowList.push(reportFieldInfo);
    $('[data-toggle="popover"]').popover();
  }

  function getClonedNodes(baseCell, inStack) {
    var cellToAdd = baseCell.cloneNode(true);
    var fieldInfo = $(cellToAdd).find(".fieldInfo")[0];

    if (inStack) {
      cellToAdd = $(baseCell).find(".fieldInfo")[0].cloneNode(true);
      fieldInfo = cellToAdd;
    }
    $(cellToAdd).removeAttr("id");

    return [cellToAdd, fieldInfo];
  }

  function addFieldInfo(reportField, fieldInfo, sequence) {
    var reportFieldText = reportField.text;
    if (
      reportFieldText === RE_ASSESS_LISTEDNESS ||
      reportFieldText === RE_ASSESS_LISTEDNESS_J ||
      reportFieldText === RE_ASSESS_LISTEDNESS_E_LABEL ||
      reportFieldText === RE_ASSESS_LISTEDNESS_J_LABEL
    ) {
      numOfReassessListedness++;
      $(".reassessListedness").show();
    }

    $(fieldInfo).attr("sequence", sequence);
    $(fieldInfo).attr("fieldId", reportField.reportFieldName);
    $(fieldInfo).attr(
      "fieldName",
      decodeFromHTML(reportField.text || reportField.reportFieldName)
    );
    $(fieldInfo).attr("argusName", reportField.argusName);
    $(fieldInfo).attr("stackId", reportField.stackId);
    $(fieldInfo).attr("clobType", reportField.clobType);
    $(fieldInfo).attr("dateType", reportField.dateType);
    $(fieldInfo).attr("fromProtectedList", reportField.fromProtectedList);
    $(fieldInfo).attr("fromBlindedList", reportField.fromBlindedList);
    $(fieldInfo).attr("redactedValue", reportField.redactedValue);
    $(fieldInfo).attr("blindedValue", reportField.blindedValue);
    $(fieldInfo).attr("description", decodeFromHTML(reportField.description));
    $(fieldInfo).attr("legend", reportField.legend);
    $(fieldInfo).attr("fieldLabelJ", decodeFromHTML(reportField.fieldLabelJ));
    $(fieldInfo).attr("fieldLabelE", decodeFromHTML(reportField.fieldLabelE));
    if (reportField.renameValue) {
      if (reportField.renameValue != reportField.text) {
        $(fieldInfo).attr("renamedTo", reportField.renameValue);
      }
    }
    if (reportField.newLegendValue) {
      if (reportField.newLegendValue != reportField.legend) {
        $(fieldInfo).attr("renamedLegendTo", reportField.newLegendValue);
      }
    }
    $(fieldInfo).attr("columnWidth", reportField.columnWidth);
    $(fieldInfo).attr("drillDownTemplate", reportField.drillDownTemplate);
    $(fieldInfo).attr(
      "drillDownFilerColumns",
      reportField.drillDownFilerColumns
    );
    $(fieldInfo).attr("colorConditions", reportField.colorConditions);
  }

  function addToStackList(reportField, sequence) {
    if (reportField.stackId > 0) {
      if (!stackedColumnsList.hasOwnProperty(reportField.stackId)) {
        stackedColumnsList[reportField.stackId] = [];
        if (stackId <= reportField.stackId) {
          stackId = reportField.stackId + 1;
        }
      }
      stackedColumnsList[reportField.stackId].push(sequence);
    }
  }

  function hideEmptyContent(fieldInfo) {
    $.each(
      $(fieldInfo.parentElement.parentElement).find(".emptyColumnContent"),
      function () {
        $(this).hide();
      }
    );
  }

  function appendField(cellName, fieldInfo, fieldLabel) {
    fieldLabel = fieldLabel || cellName;
    var desc = $(fieldInfo).attr("description");

    if (desc)
      $(fieldInfo).append(
        '<div class="columnName"><i data-toggle="popover" class="iPopover" data-trigger="hover"  data-container="body" data-content="' +
          replaceBracketsAndQuotes(desc) +
          '"><span class="fa fa-info-circle" style="font-size: 16px; cursor: pointer"></span></i> ' +
          encodeToHTML(fieldLabel) +
          "</div>"
      );
    else
      $(fieldInfo).append(
        '<div class="columnName"><i data-toggle="popover" class="iPopover" data-trigger="hover"  data-container="body" data-content="' +
          replaceBracketsAndQuotes(cellName) +
          '"><span class="fa fa-info-circle" style="font-size: 16px; cursor: pointer"></span></i> ' +
          encodeToHTML(fieldLabel) +
          "</div>"
      );
  }

  $(document).on("mouseover", ".iPopover", function () {
    var popoverContainer = $(".popover-content");
    popoverContainer.css("word-break", "break-word");
    var content = popoverContainer.html();
    if (content && content.length > 0) {
      content = decodeFromHTML(content);
      popoverContainer.html(content);
    }
  });

  function addIcons(
    cellToAdd,
    fieldInfo,
    reportFieldInfo,
    addToRowColumns,
    colMeasureIndex
  ) {
    if (viewOnly) {
      var dragColumn = $(cellToAdd).find(".middleColumn")[0];
      $(dragColumn).removeAttr("draggable");
      $(fieldInfo).append('<div class="sortOrderSeq"></div>');
    } else if (addToRowColumns) {
      $(fieldInfo).append('<i class="fa fa-times removeColumn"></i>');
    } else {
      $(fieldInfo).append(
        '<i class="fa fa-times removeColumn"></i><div class="sortOrderSeq"></div>'
      );
    }

    var cellName = $(fieldInfo).attr("fieldName");
    var fieldLabelJ = $(fieldInfo).attr("fieldLabelJ");
    var fieldLabelE = $(fieldInfo).attr("fieldLabelE");

    if ($(fieldInfo).attr("renamedTo")) {
      if ($(fieldInfo).attr("renamedTo") != cellName) {
        cellName = $(fieldInfo).attr("renamedTo");
        appendField(cellName, fieldInfo);
      }
    } else {
      if (cellName.indexOf("(J)") > -1) {
        appendField(cellName, fieldInfo, fieldLabelJ);
      } else if (cellName.indexOf("(E)") > -1) {
        appendField(cellName, fieldInfo, fieldLabelE);
      } else {
        appendField(cellName, fieldInfo);
      }
    }

    if (!addToRowColumns) {
      $(fieldInfo).append(
        '<i class="sortIcon fa fa-sort fa-lg sortDisabled"></i>'
      );
    }

    // Add sort info
    if (!addToRowColumns && reportFieldInfo.sort) {
      var sortClassList = $(fieldInfo).find(".sortIcon")[0].classList;
      if (reportFieldInfo.sort == SORT_ASCENDING) {
        removeSortIcon(sortClassList);
        addAscIcon(sortClassList);
      } else if (reportFieldInfo.sort == SORT_DESCENDING) {
        removeSortIcon(sortClassList);
        addDescIcon(sortClassList);
      }
      $($(fieldInfo).find(".sortOrderSeq")).text(reportFieldInfo.sortLevel);

      sortObjList.push(reportFieldInfo);
      sortObjList = sortObjList.sort(function (a, b) {
        return a.sortLevel - b.sortLevel;
      });
      sortLevel++;
    }
  }
  function insertHorizontalField(cellToAdd, containerToAppend) {
    var containerWidth = $(containerToAppend).width();
    $(containerToAppend).append(cellToAdd);
    containerWidth += $(cellToAdd).width();
    $(containerToAppend).width(containerWidth);
  }
  function insertVerticalField(cellToAdd, containerToAppend) {
    cellToAdd.classList.remove("floatLeft");
    $.each($(cellToAdd).find(".add-cursor"), function () {
      this.classList.remove("floatLeft");
    });
    var elements;
    if (templateType === TYPE_DATA_TAB) {
      // Add to data tabulation columns
      elements = $(containerToAppend).find(".wholeColumn");
    } else {
      elements = $("#rowColumnsContainer").find(".wholeColumn");
    }
    var length = elements.length;

    if (length > 0) {
      var lastElement = elements[length - 1];
      lastElement.parentElement.insertBefore(
        cellToAdd,
        lastElement.nextSibling
      );
      if ($(lastElement).width() > $(cellToAdd).width()) {
        $(cellToAdd).width($(lastElement).width());
      }
    } else {
      $(containerToAppend).append(cellToAdd);
    }
    var maxWidth = $(cellToAdd.querySelector(".middleColumn")).width();
    $(cellToAdd.querySelectorAll(".addHiddenColumnHeader")).width(maxWidth);
    $(cellToAdd.querySelectorAll(".emptyFutureColumn")).width(maxWidth);
  }

  function addListeners(cellToAdd) {
    if (!viewOnly) {
      $.each($(cellToAdd).find(".middleColumn"), function () {
        addDragListeners(this);
      });

      $.each($(cellToAdd).find(".emptyFutureColumn"), function () {
        addDragListenersToEmptyColumn(this);
      });
    }
  }

  function addListenersForRowCol(cellToAdd) {
    if (!viewOnly) {
      $.each($(cellToAdd).find(".middleColumn"), function () {
        this.removeAttribute("draggable");
        addDragListenersForMid(this);
      });
      $.each($(cellToAdd).find(".fieldInfo"), function () {
        this.setAttribute("draggable", "true");
        addDragListenersRow(this);
      });
      $.each($(cellToAdd).find(".emptyFutureColumn"), function () {
        addDragListenersToEmptyRowColumn(this);
      });

      //if in editable case
      var childEle = $(cellToAdd);
      if (childEle.hasClass("fieldInfo")) {
        childEle[0].setAttribute("draggable", "true");
        addDragListenersRow(childEle[0]);
      }
    }
  }

  function getRowContentWidth(containerToAppend) {
    var totalWidth = 0;
    $.each($(containerToAppend).find(".wholeColumn"), function () {
      totalWidth += $(this).width();
    });
    return totalWidth;
  }

  // ---------------------------------------------------------------------------------------------- END HELPER METHODS

  // Update ----------------------------------------------------------------------------------------------------------

  function updateColumnListAndSortOrder() {
    updateRFList(newColumnList, $("[id^=columnsContainer]").find(".fieldInfo"));
    updateRFList(newGroupingList, $("#groupingContainer").find(".fieldInfo"));
    updateRFList(newRowColList, $("#rowColumnsContainer").find(".fieldInfo"));
    updateRFList(
      newRowList,
      $("#rowsContainer").find(".fieldInfo").get().reverse()
    );

    // update column set
    $.each(columnSetList, function (index, columns) {
      updateRFList(
        columns,
        $($(".columnMeasureSet")[index]).find(".fieldInfo")
      );
    });

    updateSortOrderText();
  }

  function updateSortOrderText() {
    $.each(sortObjList, function (index, obj) {
      var sequence = obj.sequence;
      $.each($(".fieldInfo"), function () {
        var containerType = COLUMN;
        if (
          $(this)
            .closest(".rowsAndColumnsContainer")
            .hasClass("groupingContainer")
        ) {
          containerType = GROUP;
        } else if (
          $(this)
            .closest(".rowsAndColumnsContainer")
            .hasClass("rowColumnsContainer")
        ) {
          containerType = ROW_COL;
        } else if (
          $(this).closest(".rowsAndColumnsContainer").hasClass("rowsContainer")
        ) {
          containerType = ROW;
        }

        if (
          obj.type == containerType &&
          parseInt($(this).attr("sequence")) == sequence
        ) {
          if (templateType != TYPE_DATA_TAB) {
            $(this).find(".sortOrderSeq").text(obj.sortLevel);
          }
        }
      });
    });
  }

  function updateRFList(list, elements) {
    var newSequence = 0;
    $.each(elements, function () {
      if ($(this).attr("sequence")) {
        $(this).attr("preSeq", $(this).attr("sequence"));
        $(this).attr("sequence", newSequence);
        newSequence++;
      }
    });
    $.each(list, function (index) {
      var oldSequence = list[index].sequence;
      var el = findByAttributeValue(elements, "preSeq", oldSequence);
      list[index].sequence = $(el).attr("sequence");
    });

    // sort the list by sequence ascending
    list = list.sort(function (a, b) {
      return a.sequence - b.sequence;
    });
  }

  function findByAttributeValue(elements, attribute, value) {
    var el;
    $.each(elements, function () {
      if ($(this).attr(attribute) == value) {
        el = this;
      }
    });
    return el;
  }

  // ------------------------------------------------------------------------------------------------------ END UPDATE

  // Drag and Drop ---------------------------------------------------------------------------------------------------

  var dragSrcEl = null;

  function inSameContainer(sourceEl, targetEl) {
    var isSame = false;
    var $sourceContainer = $(sourceEl).closest(".rowsAndColumnsContainer");
    var $targetContainer = $(targetEl).closest(".rowsAndColumnsContainer");

    if (
      $sourceContainer.hasClass("groupingContainer") &&
      $targetContainer.hasClass("groupingContainer")
    ) {
      isSame = true;
    }
    if (
      $sourceContainer.hasClass("rowColumnsContainer") &&
      $targetContainer.hasClass("rowColumnsContainer")
    ) {
      isSame = true;
    }
    if (
      $sourceContainer.hasClass("rowsContainer") &&
      $targetContainer.hasClass("rowsContainer")
    ) {
      isSame = true;
    }
    if (
      $sourceContainer.hasClass("columnsContainer") &&
      $targetContainer.hasClass("columnsContainer")
    ) {
      isSame = true;
    }

    return isSame;
  }

  function handleDragStart(e) {
    this.style.opacity = "0.7";
    $(".columnSelected").removeClass("columnSelected");
    var textHeader = $(this).find(".addColumnHeader")[0];
    if (textHeader == undefined) $(this).addClass("columnSelected");
    else textHeader.classList.add("columnSelected");
    dragSrcEl = this;
  }

  function handleDragOver(e) {
    if (e.preventDefault) {
      e.preventDefault(); // Necessary. Allows us to drop.
    }
    var isGroup = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("groupingContainer");

    if (inSameContainer(dragSrcEl, this) && dragSrcEl != this) {
      if (!(isGroup || templateType == TYPE_DATA_TAB)) {
        $($(this.parentElement).find(".addColumnHeader")).removeClass(
          "defaultHeaderBackground"
        );
        $($(this.parentElement).find(".addColumnHeader")).addClass(
          "overHeaderBackground"
        );
      }
      $($(this.parentElement).find(".emptyFutureColumn")).addClass("over");
      $($(this.parentElement).find(".emptyFutureColumn")).removeAttr("hidden");
    }
    e.stopPropagation();

    return false;
  }

  function handleDragEnter(e) {
    e.stopPropagation();

    if (inSameContainer(dragSrcEl, this) && dragSrcEl != this) {
      $($(this.parentElement).find(".emptyFutureColumn")).addClass("over");
      $($(this.parentElement).find(".emptyFutureColumn")).removeAttr("hidden");
    }
  }

  function handleDragLeave(e) {
    e.stopPropagation();
  }

  function handleDrop(e) {
    if (e.stopPropagation) {
      e.stopPropagation(); // stops the browser from redirecting.
    }

    var preSelected = colSelected;
    colSelected = dragSrcEl;
    var list = getRelatedList(); // Actually stack is only for CLL columns

    var isGroup = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("groupingContainer");
    if (
      inSameContainer(dragSrcEl, this) &&
      dragSrcEl != this &&
      !(isGroup || templateType == TYPE_DATA_TAB)
    ) {
      // stack here
      var target = this;
      $.each($(dragSrcEl).find(".fieldInfo"), function () {
        var sourceHeader = this.cloneNode(true);
        colSelected = sourceHeader;

        var lastIndex = $(target).find(".addColumnHeader").length - 1;
        var targetHeader = $(target).find(".addColumnHeader")[lastIndex];
        target.insertBefore(sourceHeader, targetHeader.nextSibling);

        var targetStackId = $(targetHeader).attr("stackId");
        if (targetStackId > 0) {
          $(sourceHeader).attr("stackId", targetStackId);
          list[$(sourceHeader).attr("sequence")].stackId = targetStackId;
        } else {
          $(targetHeader).attr("stackId", stackId);
          $(sourceHeader).attr("stackId", stackId);
          list[$(targetHeader).attr("sequence")].stackId = stackId;
          list[$(sourceHeader).attr("sequence")].stackId = stackId;
          stackId++;
        }
      });
      $(dragSrcEl.parentElement).remove();
      updateColumnListAndSortOrder();
    }

    //*** remove unwanted divs ***//
    $.each($(".wholeColumn"), function () {
      var ele = $(this).find("div.fieldInfo").length;
      if (ele < 1) {
        $(this).remove();
      }
    });
    $.each($(".middleColumn"), function () {
      var ele = $(this).find("div.fieldInfo").length;
      if (ele < 1) {
        $(this).remove();
      }
    });
    //** end of remove functionality **//

    colSelected = preSelected;
    $($(this.parentElement).find(".emptyFutureColumn")).attr(
      "hidden",
      "hidden"
    );
    return false;
  }

  function handleDragEnd(e) {
    this.style.opacity = "1";

    [].forEach.call($(".add-cursor"), function (col) {
      col.classList.remove("over");
      col.classList.remove("overEmpty");
    });

    [].forEach.call($(".columnSelected"), function (col) {
      col.classList.remove("columnSelected");
      col.classList.add("addColumnHeader");
    });

    [].forEach.call($(".addColumnHeader"), function (col) {
      col.classList.remove("overHeaderBackground");
      col.classList.add("defaultHeaderBackground");
    });

    [].forEach.call($(".emptyFutureColumn"), function (col) {
      $(col).attr("hidden", "hidden");
    });

    //*** remove unwanted divs ***//
    $.each($(".wholeColumn"), function () {
      var ele = $(this).find("div.fieldInfo").length;
      if (ele < 1) {
        $(this).remove();
      }
    });
    $.each($(".middleColumn"), function () {
      var ele = $(this).find("div.fieldInfo").length;
      if (ele < 1) {
        $(this).remove();
      }
    });
    //** end of remove functionality **//
    updateColumnListAndSortOrder();
  }

  function handleDragOverEmpty(e) {
    if (e.preventDefault) {
      e.preventDefault(); // Necessary. Allows us to drop.
    }

    if (dragSrcEl.parentElement != this.parentElement) {
      $(this).addClass("overEmpty");
      $($(this.parentElement).find(".emptyFutureColumn")).removeAttr("hidden");
    }

    e.stopPropagation();
    return false;
  }

  function handleDragEnterEmpty(e) {
    e.stopPropagation();

    if (dragSrcEl.parentElement != this.parentElement) {
      $(this).addClass("overEmpty");
      $($(this).find(".emptyFutureColumn")).removeAttr("hidden");
      $($(this.parentElement).find(".addColumnHeader")).removeClass(
        "overHeaderBackground"
      );
      $($(this.parentElement).find(".addColumnHeader")).addClass(
        "defaultHeaderBackground"
      );
    }
  }

  function handleDragLeaveEmpty(e) {
    e.stopPropagation();

    $(this).removeClass("overEmpty");
    $($(this.parentElement).find(".emptyFutureColumn")).attr(
      "hidden",
      "hidden"
    );
  }

  function handleDropEmpty(e) {
    if (e.stopPropagation) {
      e.stopPropagation(); // stops the browser from redirecting.
    }

    var isRow = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("rowsContainer");

    if (isRow) {
      if ($(this).hasClass("addLeft")) {
        this.parentElement.parentElement.insertBefore(
          dragSrcEl.parentElement,
          this.parentElement.nextSibling
        );
      }
      if ($(this).hasClass("addRight")) {
        this.parentElement.parentElement.insertBefore(
          dragSrcEl.parentElement,
          this.parentElement
        );
      }
    } else {
      if ($(this).hasClass("addLeft")) {
        this.parentElement.parentElement.insertBefore(
          dragSrcEl.parentElement,
          this.parentElement
        );
      }
      if ($(this).hasClass("addRight")) {
        this.parentElement.parentElement.insertBefore(
          dragSrcEl.parentElement,
          this.parentElement.nextSibling
        );
      }
    }

    $($(this.parentElement).find(".emptyFutureColumn")).attr(
      "hidden",
      "hidden"
    );

    return false;
  }

  /**** Methods for RowColoums in Cll ****/

  function handleDragStartRowCol(e) {
    this.style.opacity = "0.7";
    $(".rowcolumnSelected").removeClass("rowcolumnSelected");
    var textHeader = this;
    if (textHeader == undefined) $(this).addClass("rowcolumnSelected");
    else textHeader.classList.add("rowcolumnSelected");
    dragSrcEl = this;
  }

  function handleDragOverRowCol(e) {
    if (e.preventDefault) {
      e.preventDefault(); // Necessary. Allows us to drop.
    }
    var isGroup = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("groupingContainer");
    if (inSameContainer(dragSrcEl, this) && dragSrcEl != this) {
      if (!(isGroup || templateType == TYPE_DATA_TAB)) {
        $($(this.parentElement).find(".addColumnHeader")).removeClass(
          "defaultHeaderBackground"
        );
        $($(this.parentElement).find(".addColumnHeader")).addClass(
          "overHeaderBackground"
        );
      }
      $($(this.parentElement).find(".emptyFutureColumn")).addClass("over");
      $($(this.parentElement).find(".emptyFutureColumn")).removeAttr("hidden");
    }
    e.stopPropagation();

    return false;
  }

  function handleDragEnterRowCol(e) {
    e.stopPropagation();

    if (inSameContainer(dragSrcEl, this) && dragSrcEl != this) {
      $($(this.parentElement).find(".emptyFutureColumn")).addClass("over");
      $($(this.parentElement).find(".emptyFutureColumn")).removeAttr("hidden");
    }
  }

  function handleDropRowCol(e) {
    if (e.stopPropagation) {
      e.stopPropagation(); // stops the browser from redirecting.
    }

    var preSelected = colSelected;
    colSelected = dragSrcEl;
    var list = getRelatedList(); // Actually stack is only for CLL columns
    var isGroup = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("groupingContainer");
    if (inSameContainer(dragSrcEl, this)) {
      var target = this;
      var sourceHeader = $(".rowcolumnSelected")[0].cloneNode(true);

      $(sourceHeader).removeClass("rowcolumnSelected");
      colSelected = sourceHeader;

      var lastIndex = $(target).find(".addColumnHeader").length - 1;
      var targetHeader = $(target).find(".addColumnHeader")[lastIndex];
      $(sourceHeader).attr("draggable", "true");
      $(sourceHeader).addClass("addParent");
      addDragListenersRow(sourceHeader);
      target.insertBefore(sourceHeader, targetHeader.nextSibling);

      var targetStackId = $(targetHeader).attr("stackId");
      if (targetStackId > 0) {
        $(sourceHeader).attr("stackId", targetStackId);
        list[$(sourceHeader).attr("sequence")].stackId = targetStackId;
      } else {
        $(targetHeader).attr("stackId", stackId);
        $(sourceHeader).attr("stackId", stackId);
        list[$(targetHeader).attr("sequence")].stackId = stackId;
        list[$(sourceHeader).attr("sequence")].stackId = stackId;
        stackId++;
      }
      $(".rowcolumnSelected")[0].remove();
      var sequenceInc = 0;
      $.each($(".middleColumn "), function (i, mainDivelm) {
        $.each($(mainDivelm).find(".fieldInfo"), function (j, elm) {
          $(elm).addClass("defaultHeaderBackground");
          $(elm).removeClass("overHeaderBackground");
          $(elm).css("opacity", "1");
          sequenceInc++;
        });
      });
    }

    $('[data-toggle="popover"]').popover();
    //*** removed unwanted divs ***//
    $.each($(".wholeColumn"), function () {
      var ele = $(this).find("div.fieldInfo").length;
      if (ele < 1) {
        $(this).remove();
      }
    });
    $.each($(".middleColumn"), function () {
      var ele = $(this).find("div.fieldInfo").length;
      if (ele < 1) {
        $(this).remove();
      }
    });
    //** end of remove functionality **//

    colSelected = preSelected;
    $($(this.parentElement).find(".emptyFutureColumn")).attr(
      "hidden",
      "hidden"
    );
    updateColumnListAndSortOrder();
    return false;
  }

  function handleDragOverEmptyRowCol(e) {
    if (e.preventDefault) {
      e.preventDefault(); // Necessary. Allows us to drop.
    }

    if (dragSrcEl.parentElement != this.parentElement) {
      $(this).addClass("overEmpty");
      $($(this.parentElement).find(".emptyFutureColumn")).removeAttr("hidden");
    }

    e.stopPropagation();
    return false;
  }

  function handleDragEnterEmptyRowCol(e) {
    e.stopPropagation();

    if (dragSrcEl.parentElement != this.parentElement) {
      $(this).addClass("overEmpty");
      $($(this).find(".emptyFutureColumn")).removeAttr("hidden");
      $($(this.parentElement).find(".addColumnHeader")).removeClass(
        "overHeaderBackground"
      );
      $($(this.parentElement).find(".addColumnHeader")).addClass(
        "defaultHeaderBackground"
      );
    }
  }

  function handleDragLeaveEmptyRowCol(e) {
    e.stopPropagation();

    $(this).removeClass("overEmpty");
    $($(this.parentElement).find(".emptyFutureColumn")).attr(
      "hidden",
      "hidden"
    );
  }

  function handleDropEmptyRowCol(e) {
    if (e.stopPropagation) {
      e.stopPropagation(); // stops the browser from redirecting.
    }
    var $parentDiv = $(this).closest(".containerToBeSelected");
    if ($parentDiv != undefined) {
      changeSet($parentDiv);
    }
    var $sourceContainer = $(dragSrcEl).closest(".rowsAndColumnsContainer");
    if ($sourceContainer.hasClass("columnsContainer")) {
      var mainDiv = document.createElement("div");
      mainDiv.className = "floatLeft middleColumn add-cursor";
      var seqInt = $(dragSrcEl)[0].getAttribute("sequence");
      delete newColumnList[parseInt(seqInt)]["stackId"];
      $(dragSrcEl)[0].setAttribute("stackId", -1);
      mainDiv.appendChild($(dragSrcEl)[0]);
      var emptyColumnContent = document.createElement("div");
      emptyColumnContent.className = "emptyColumnContent";
      mainDiv.appendChild(emptyColumnContent);
      var masterMain = document.createElement("div");
      masterMain.className = "floatLeft wholeColumn";
      var addLeftDiv = document.createElement("div");
      addLeftDiv.className = "floatLeft emptyFutureColumn addLeft add-cursor";
      var childDiv1 = document.createElement("div");
      childDiv1.className = "addHiddenColumnHeader";
      addLeftDiv.appendChild(childDiv1);
      var childDiv2 = document.createElement("div");
      childDiv2.className = "emptyColumnContent";
      addLeftDiv.appendChild(childDiv2);
      addDragListenersToEmptyRowColumn(addLeftDiv);
      masterMain.appendChild(addLeftDiv);
      masterMain.appendChild(mainDiv);
      var addRightDiv = document.createElement("div");
      addRightDiv.className = "floatLeft emptyFutureColumn addRight add-cursor";
      var childDiv3 = document.createElement("div");
      childDiv3.className = "addHiddenColumnHeader";
      addRightDiv.appendChild(childDiv3);
      var childDiv4 = document.createElement("div");
      childDiv4.className = "emptyColumnContent";
      addRightDiv.appendChild(childDiv4);
      addDragListenersToEmptyRowColumn(addRightDiv);
      masterMain.appendChild(addRightDiv);
      $('[data-toggle="popover"]').popover();
      if ($(this).hasClass("addLeft")) {
        this.parentElement.parentElement.insertBefore(
          masterMain,
          this.parentElement
        );
      }
      if ($(this).hasClass("addRight")) {
        this.parentElement.parentElement.insertBefore(
          masterMain,
          this.parentElement.nextSibling
        );
      }
      if (templateType == TYPE_DATA_TAB) {
        masterMain.classList.remove("floatLeft");
        $.each($(masterMain).find(".add-cursor"), function () {
          this.classList.remove("floatLeft");
        });
        hideEmptyContent(masterMain);
      }
      addDragListenersForMid(mainDiv);
      addDragListenersRow($(dragSrcEl)[0]);
    } else {
      if ($(this).hasClass("addLeft")) {
        this.parentElement.parentElement.insertBefore(
          dragSrcEl.parentElement,
          this.parentElement
        );
      }
      if ($(this).hasClass("addRight")) {
        this.parentElement.parentElement.insertBefore(
          dragSrcEl.parentElement,
          this.parentElement.nextSibling
        );
      }
    }
    $($(this.parentElement).find(".emptyFutureColumn")).attr(
      "hidden",
      "hidden"
    );

    return false;
  }

  /**** End for RowColoums in cll ****/

  function addDragListeners(element) {
    element.addEventListener("dragstart", handleDragStart, false);
    element.addEventListener("dragover", handleDragOver, true);
    element.addEventListener("dragenter", handleDragEnter, false);
    element.addEventListener("dragleave", handleDragLeave, false);
    element.addEventListener("drop", handleDrop, false);
    element.addEventListener("dragend", handleDragEnd, false);
  }

  function addDragListenersForMid(element) {
    //element.addEventListener('dragstart', handleDragStart, false);
    element.addEventListener("dragover", handleDragOverRowCol, true);
    element.addEventListener("dragenter", handleDragEnterRowCol, false);
    element.addEventListener("dragleave", handleDragLeave, false);
    element.addEventListener("drop", handleDropRowCol, false);
    element.addEventListener("dragend", handleDragEnd, false);
  }

  function addDragListenersRow(element) {
    element.addEventListener("dragstart", handleDragStartRowCol, false);
    element.addEventListener("dragover", handleDragOverRowCol, true);
    element.addEventListener("dragenter", handleDragEnterRowCol, false);
    element.addEventListener("dragleave", handleDragLeave, false);
    element.addEventListener("drop", handleDropRowCol, false);
    element.addEventListener("dragend", handleDragEnd, false);
  }

  function addDragListenersToEmptyRowColumn(element) {
    element.addEventListener("dragover", handleDragOverEmptyRowCol, false);
    element.addEventListener("dragenter", handleDragEnterEmptyRowCol, false);
    element.addEventListener("dragleave", handleDragLeaveEmptyRowCol, false);
    element.addEventListener("drop", handleDropEmptyRowCol, false);
    element.addEventListener("dragend", handleDragEnd, false);
  }

  function addDragListenersToEmptyColumn(element) {
    element.addEventListener("dragover", handleDragOverEmpty, false);
    element.addEventListener("dragenter", handleDragEnterEmpty, false);
    element.addEventListener("dragleave", handleDragLeaveEmpty, false);
    element.addEventListener("drop", handleDropEmpty, false);
    element.addEventListener("dragend", handleDragEnd, false);
  }

  // ----------------------------------------------------------------------------------------------- END DRAG AND DROP

  function changeSet($parentDiv) {
    var setId = $parentDiv.attr("data-setid");
    var fieldId = $(dragSrcEl)[0].getAttribute("fieldid");
    if (setId && fieldId) {
      for (var j = 0; j < newColumnList.length; j++) {
        if (newColumnList[j].reportFieldName == fieldId)
          newColumnList[j].setId = setId;
      }
    }
  }

  function removeSet(ind) {
    newColumnList = newColumnList.filter(function (el) {
      return el.setId != ind;
    });

    $("#removeCLLSet" + ind).remove();
    $("#columnsContainer" + ind)
      .parent()
      .parent()
      .remove();
    var sets = $(".columnsContainer .containerToBeSelected");
    var max = 0;
    for (var i = 0; i < sets.length; i++) {
      var cur = parseInt($(sets[i]).attr("data-setid"));
      if (cur > max) max = cur;
    }
    if (max > ind) {
      for (var i = ind + 1; i <= max; i++) {
        for (var j = 0; j < newColumnList.length; j++) {
          if (newColumnList[j].setId == i) newColumnList[j].setId = i - 1;
        }
        $("#columnsContainer" + i).attr("data-setid", i - 1);
        $("#columnsContainer" + i).attr("id", "columnsContainer" + (i - 1));
        $("#removeCLLSet" + i).attr("data-setid", i - 1);
        $("#removeCLLSet" + i).attr("id", "removeCLLSet" + (i - 1));
      }
    }
    updateColumnListAndSortOrder();
  }

  function isEmptySet() {
    var sets = $(".columnsContainer .containerToBeSelected");
    for (var i = 0; i < sets.length; i++) {
      if ($(sets[i]).find(".floatLeft").length == 0) return true;
    }
    return false;
  }

  function clearEmptySets() {
    var sets = $(".columnsContainer .containerToBeSelected");
    var max = 0;
    for (var i = 0; i < sets.length; i++) {
      var cur = parseInt($(sets[i]).attr("data-setid"));
      if (cur > max) max = cur;
    }
    for (var i = max; i >= 0; i--) {
      if ($("#columnsContainer" + i).find(".floatLeft").length == 0)
        removeSet(i);
    }
  }

  function submitForm() {
    if (TYPE_CASE_LINE == templateType && isEmptySet()) {
      $("#warningModal").modal("show");
      $("#warningModal")
        .find("#warningButton")
        .off("click")
        .on("click", function () {
          clearEmptySets();
          $("#warningModal").modal("hide");
          return processSubmitForm();
        });
    } else if (TYPE_DATA_TAB == templateType) {
      if (!iggnoreDrilldownWarning) {
        var failDrillDownTemplates = validateDrilldownTemplates();
        if (failDrillDownTemplates && failDrillDownTemplates.length > 0) {
          $("#drillDownWarning").modal("show");
          $("#drillDownWarning")
            .find(".extramessage")
            .html(
              "<br>" +
                _.map(failDrillDownTemplates, function (e) {
                  return e.name;
                }).join("<br>")
            );
          $("#warningButton")
            .off("click")
            .on("click", function () {
              $("#warningModal").modal("hide");
              iggnoreDrilldownWarning = true;
              processSubmitForm();
            });
          return false;
        }
      }
      if (!iggnoreWorldMapWarning) {
        if ($("#worldMap").is(":checked")) {
          $("#showWorldMapWarning").modal("show");
          $("#showWorldMapWarning")
            .find("#warningButton")
            .off("click")
            .click(function () {
              $("#showWorldMapWarning").modal("hide");
              iggnoreWorldMapWarning = true;
              processSubmitForm();
            });
          return false;
        }
      }
      var chartEnabled = $("#showChartSheet").is(":checked");
      return processSubmitForm();
    } else {
      return processSubmitForm();
    }
    return false;
  }

  function validateDrilldownTemplates() {
    var fields = [];
    var drillDowns = [];
    $(".fieldInfo:visible").each(function () {
      fields.push($(this).attr("fieldId"));
    });
    $(".drilldownTemplate").each(function () {
      var v = $(this).val();
      if (!_.isEmpty(v)) drillDowns.push(v);
    });
    var result;
    $.ajax({
      type: "GET",
      url: checkDrillDownUrl,
      data: {
        fields: fields,
        drillDowns: drillDowns,
      },
      contentType: "application/json",
      async: false,
      dataType: "json",
    })
      .done(function (responce) {
        result = responce;
      })
      .fail(function () {
        console.log("Error retrieving custom SQL parameters");
      });
    return result;
  }

  function processSubmitForm() {
    if (newColumnList.length == 0) {
      $("#columns").val("");
    } else {
      $("#columns").val(JSON.stringify(newColumnList));
    }

    if (newGroupingList.length == 0) {
      $("#grouping").val("");
    } else {
      $("#grouping").val(JSON.stringify(newGroupingList));
    }

    if (newRowColList.length == 0) {
      $("#rowCols").val("");
    } else {
      $("#rowCols").val(JSON.stringify(newRowColList));
    }

    if (newRowList.length == 0) {
      $("#rows").val("");
    } else {
      $("#rows").val(JSON.stringify(newRowList));
    }

    if (numOfReassessListedness == 0) {
      $("#reassessListedness").val(null).trigger("change");
    }

    setValidMeasureIndexList();

    //    console.log('number of colMeas:', numColMeas);
    //    $('#numColMeas').val(numColMeas);

    $.each(columnSetList, function (index, columns) {
      if (columns.length == 0) {
        $("#columns" + index).val("");
      } else {
        $("#columns" + index).val(JSON.stringify(columns));
      }
    });

    setCLLIdsForTemplateSet();

    //For Advanced Custom Expression
    if (
      $("#templateType").val() === "CASE_LINE" ||
      $("#templateType").val() === "DATA_TAB"
    ) {
      var checkedNumber = checkAllNumberFields();
      if (!checkedNumber) {
        return false;
      }

      sendReassessListedness();

      removeEmptyGroups(builderAll);

      printToJSON();
    }

    return true;
  }

  function showRenameArea(open, target) {
    if (open) {
      $(".measureOptions").hide();
      $($(".columnRenameArea")[0]).slideDown();
      var columnWidthSelect = $($(".columnRenameArea .selectedColumnWidth")[0]);
      if ($(target).parents(".columnMeasureSet").length > 0) {
        columnWidthSelect.prop("disabled", true);
      } else {
        columnWidthSelect.prop("disabled", false);
      }
    } else {
      $($(".columnRenameArea")[0]).hide();
    }
  }

  function showCustomExpression(customExpression) {
    var renameArea = $(".columnRenameArea")[0];
    if (customExpression) {
      $($(renameArea).find(".customExpressionArea")[0]).removeAttr("hidden");
      $($(renameArea).find(".customExpressionValue")[0]).val(
        unescapeHTML(customExpression)
      );
    } else {
      $($(renameArea).find(".customExpressionArea")[0]).attr(
        "hidden",
        "hidden"
      );
      $($(renameArea).find(".customExpressionValue")[0]).val("");
    }
  }

  function showAdvancedSorting(advancedSorting) {
    var renameArea = $(".columnRenameArea")[0];
    if (advancedSorting) {
      $($(renameArea).find(".advancedSortingArea")[0]).removeAttr("hidden");
      $($(renameArea).find(".advancedSortingValue")[0]).val(advancedSorting);
    } else {
      $($(renameArea).find(".advancedSortingArea")[0]).attr("hidden", "hidden");
      $($(renameArea).find(".advancedSortingValue")[0]).val("");
    }
  }

  function getRelatedList() {
    var isGroup = $(colSelected)
      .closest(".rowsAndColumnsContainer")
      .hasClass("groupingContainer");
    var isRowColumn = $(colSelected)
      .closest(".rowsAndColumnsContainer")
      .hasClass("rowColumnsContainer");
    var isRow = $(colSelected)
      .closest(".rowsAndColumnsContainer")
      .hasClass("rowsContainer");
    var list = [];

    // get related list for selected field
    if (isGroup) {
      list = newGroupingList;
    } else if (isRowColumn) {
      list = newRowColList;
    } else if (isRow) {
      list = newRowList;
    } else if (templateType == TYPE_DATA_TAB) {
      var index = $(colSelected).closest(".columnMeasureSet").attr("sequence");
      list = columnSetList[parseInt(index)];
    } else {
      list = newColumnList;
    }
    return list;
  }

  $("#sourceProfile").select2();

  $("#templateSourceProfileDiv").on("change", "#sourceProfile", function () {
    loadTemplateReportFields(
      templateType,
      tmpltReportFieldsOptsBySource,
      false,
      $(this).val()
    );
  });

  if (!viewOnly) {
    if (typeof tmpltDefaultReportFieldsOpts !== "undefined") {
      loadTemplateReportFields(
        templateType,
        tmpltDefaultReportFieldsOpts,
        true
      );
    }
  }

  var $input = $("#templateFooter");
    $input.autocomplete({
    classes: { "ui-autocomplete": "template-footer-autocomplete" },
    source: $input.data("select"),
    minLength: 0,
  open: function () {
            $(".template-footer-autocomplete").css("max-width", $input.outerWidth());
        }
    });

  // Startup ---------------------------------------------------------------------------------------------------------
  // For edit/view page and validation round trip
  if ($("#columns").val()) {
    $.each(JSON.parse($("#columns").val()), function () {
      var ind = 0;
      if (this.setId && parseInt(this.setId) > 0) {
        ind = parseInt(this.setId);
        if (!document.getElementById("columnsContainer" + ind)) addCLLSet(ind);
      }
      addFieldToColumns(
        this,
        document.getElementById("columnsContainer" + ind)
      );
    });
  }

  if ($("#grouping").val()) {
    $.each(JSON.parse($("#grouping").val()), function () {
      addFieldToCLLGrouping(this, document.getElementById("groupingContainer"));
    });
  }

  if ($("#rowCols").val()) {
    $.each(JSON.parse($("#rowCols").val()), function () {
      addFieldToCLLRowCols(
        this,
        document.getElementById("rowColumnsContainer")
      );
    });
  }

  if ($("#rows").val()) {
    $.each(JSON.parse($("#rows").val()), function () {
      addFieldToDTRows(this, document.getElementById("rowsContainer"));
    });
  }

  numColMeas = parseInt($("#numColMeas").val());
  for (var index = 0; index < numColMeas; index++) {
    columnSetList.push([]);
    if ($("#columns" + index).val()) {
      $.each(JSON.parse($("#columns" + index).val()), function () {
        addFieldToColumns(
          this,
          document.getElementById("columnsContainer" + index),
          index
        );
      });
    }
  }

  function updateHideTotalRowCountCheckbox() {
    var isTotalaChecked =
      $("#columnShowTotal").is(":checked") ||
      $("#columnShowSubTotal").is(":checked");
    if (isTotalaChecked) {
      $("#hideTotalRowCount").attr("disabled", false);
    } else {
      $("#hideTotalRowCount").attr("disabled", true);
      $("#hideTotalRowCount").prop("checked", false);
    }
  }

  updateHideTotalRowCountCheckbox();
  //TODO: GSP should handle this instead of JS. Controller needs to let GSP know if it is create, edit, or view
  if (viewOnly) {
    if (templateType == TYPE_CUSTOM_SQL || templateType == TYPE_NON_CASE) {
      $.each($(".sqlBox"), function () {
        this.disabled = true;
      });
      $("#usePvrDB").parent().removeClass("add-cursor");
      $("#usePvrDB").attr("disabled", true);
      $("#chartExportAsImage").attr("disabled", true);
      $("#worldMap").attr("disabled", true);
      $("#maxChartPoints").attr("disabled", true);
    } else if (
      templateType == TYPE_CASE_LINE ||
      templateType == TYPE_DATA_TAB
    ) {
      var previewArea;
      $("#pageBreakByGroup").attr("disabled", true);
      $("#pageBreakByGroup").parent().removeClass("add-cursor");
      if (templateType == TYPE_CASE_LINE) {
        $("#columnShowTotal").attr("disabled", true);
        $("#columnShowSubTotal").attr("disabled", true);
        $("#hideTotalRowCount").attr("disabled", true);
        $("#columnShowDistinct").attr("disabled", true);
        $("#columnShowDistinct").parent().removeClass("add-cursor");
        $("#columnShowTotal").parent().removeClass("add-cursor");
        $("#columnShowSubTotal").parent().removeClass("add-cursor");
        $("#hideTotalRowCount").parent().removeClass("add-cursor");
        $(".removeCLLSet").remove();
        $("#addCLLSet").remove();
        previewArea = $(".row.columnsContainer")[0].parentElement;
        $("#showAdvancedSorting")
          .attr("disabled", true)
          .removeClass("add-cursor")
          .css({ cursor: "not-allowed" })
          .on("click", function (e) {
            return false;
          });
        $("#advancedSortingValue").attr("disabled", true);
        $("#templateFooter").attr("disabled", true);
      } else {
        $("#addColumnMeasure").hide();
        $(".removeColumnMeasure").hide();
        $(".selectMeasure").hide();
        $(".showTotalIntervalCases").attr("disabled", true);
        $(".showTotalIntervalCases").parent().removeClass("add-cursor");
        $(".showTotalCumulativeCases").attr("disabled", true);
        $(".showTotalCumulativeCases").parent().removeClass("add-cursor");
        $("#showChartSheet").attr("disabled", true);
        $("#chartExportAsImage").attr("disabled", true);
        $("#worldMap").attr("disabled", true);
        $("#maxChartPoints").attr("disabled", true);
        $("#supressHeaders").attr("disabled", true);
        $("#supressRepeatingExcel").attr("disabled", true);
        $("#drillDownToCaseList").attr("disabled", true);
        /*$("#transposeOutput").attr('disabled', true);*/
        $("#positiveCountOnly").attr("disabled", true);
        $("#allTimeframes").attr("disabled", true);

        $("#showChartSheet").parent().removeClass("add-cursor");
        $("#supressHeaders").parent().removeClass("add-cursor");
        $("#supressRepeatingExcel").parent().removeClass("add-cursor");
        $("#drillDownToCaseList").parent().removeClass("add-cursor");
        /*$("#transposeOutput").parent().removeClass('add-cursor');*/
        $("#positiveCountOnly").parent().removeClass("add-cursor");
        $("#allTimeframes").parent().removeClass("add-cursor");
        previewArea = $("#dataTabulation").find(".previewLabel")[0];
      }
      previewArea.classList.remove("col-xs-9");
      previewArea.classList.add("col-xs-12");

      $.each($(".rowsAndColumnsContainer"), function () {
        this.classList.remove("selectedContainerBorder");
      });

      $("#reassessListedness").prop("disabled", true);
      $("#reassessForProduct").prop("disabled", true);
    }
  }

  //TODO: Instead of finding the container onchange, store the currently selected container in a var and update that var when the selected container changes
  var shifted = false;
  $("#selectedTemplate_lineListing").select2();
  $("#selectedTemplate_dataTabulation").select2();
  bindSelect2WithUrl(
    $(".drillDownTemplateCll"),
    specificCllTemplateSearchUrl,
    templateNameUrl,
    true
  ).on("select2:open", function (e) {
    var searchField = $('.select2-dropdown .select2-search__field');
    if (searchField.length) {
      searchField[0].focus();
    }
  });
  $(".drillDownFilerColumns").select2({
    placeholder: $.i18n._("selectOne"),
    multiple: true,
    query: function (query) {
      var data = { results: [] },
        i,
        j,
        s;
      const selector = $(".drillDownFilerColumns");
      $(".fieldInfo").each(function () {
        var fieldId = $(this).attr("fieldId");
        var fieldLabele = $(this).attr("fieldLabele");
        if (fieldId) {
          data.results.push({
            id: fieldId,
            text: fieldLabele ? fieldLabele : fieldId,
          });
          if (!selector.find(`option[value="${fieldId}"]`).length) {
            selector
              .append(
                new Option(
                  fieldLabele ? fieldLabele : fieldId,
                  fieldId,
                  false,
                  false
                )
              )
              .trigger("change");
          }
        }
      });
      query.callback(data);
    },
    initSelection: function (element, callback) {
      var data = [];
      $(".fieldInfo").each(function () {
        var fieldId = $(this).attr("fieldId");
        var fieldLabele = $(this).attr("fieldLabele");
        if (fieldId) {
          var fields =
            (_.isEmpty($(element).val())
              ? $(".drillDownFilerColumns")?.val()?.split?.(",")
              : element.val().split?.(",")) ?? [];
          for (var i = 0; i < fields.length; i++) {
            if (fields[i] == fieldId) {
              data.push({
                id: fieldId,
                text: fieldLabele ? fieldLabele : fieldId,
              });
              break;
            }
          }
        }
      });

      callback(data);
    },
  });

  //show or hide Primary Datasheet checkbox based on selected datasheet
  function updateOnPrimaryDatasheetCheckbox() {
      const selectedData = $("#selectDatasheet").select2('data');
      const hasChildrenFlag = _.isEmpty(selectedData) ? false : selectedData[0].hasChildrenFlag
    if (!hasChildrenFlag) {
      $(".onPrimaryDatasheet").show();
    } else {
      $(".onPrimaryDatasheet").hide();
      $("#onPrimaryDatasheet").prop("checked", false);
    }
  }

  function updateDatasheetSelection(colSeq, list) {
      var datasheet = list[colSeq].datasheet;
      if (datasheet) {
          $("#selectDatasheet").val(datasheet).trigger("change"); // TODO: show default datasheet
      } else {
          var defaultDatasheet = $("#selectDatasheet").val(); // Select first value from list if none is selected
          list[colSeq].datasheet = defaultDatasheet ? defaultDatasheet : "";
      }
      $("#onPrimaryDatasheet").prop(
          "checked",
          !!list[colSeq].onPrimaryDatasheet
      );
      updateOnPrimaryDatasheetCheckbox();
  }

  $("#reassessListedness").select2();
  $("#selectDatasheet")
    .select2()
    .on("select2:select", function (e) {
      var seq = $(colSelected.parentElement).attr("sequence");
      var list = getRelatedList();
      list[seq].datasheet = $(this).val();
      updateOnPrimaryDatasheetCheckbox();
      list[seq].onPrimaryDatasheet = $(this)
        .parent()
        .find("#onPrimaryDatasheet")
        .is(":checked");
    });
  $("#onPrimaryDatasheet").on("change", function (e) {
    var seq = $(colSelected.parentElement).attr("sequence");
    var list = getRelatedList();
    list[seq].onPrimaryDatasheet = $(this)
      .parent()
      .find("#onPrimaryDatasheet")
      .is(":checked");
  });

  // ----------------------------------------------------------------------------------------------------- END STARTUP

  // Events ----------------------------------------------------------------------------------------------------------

  $(document).on("change", "#columnShowTotal,#columnShowSubTotal", function () {
    updateHideTotalRowCountCheckbox();
  });
  $(document).on("click", ".rowsAndColumnsContainer", function () {
    if (!viewOnly) {
      $.each($(".selectedContainerBorder"), function () {
        this.classList.remove("selectedContainerBorder");
      });
      if (
        $(this).hasClass("rowsContainer") ||
        $(this).hasClass("groupingContainer")
      ) {
        addToDT = ROW;
        $("#dtRowSelect2").show();
        $("#dtColumnSelect2").hide();
      } else {
        if (templateType == TYPE_DATA_TAB) {
          addToDT = COLUMN;
          $("#dtColumnSelect2").show();
          $("#dtRowSelect2").hide();
        }
      }
      this.classList.add("selectedContainerBorder");
    }
  });

  $(document).on("click", ".columnName", function () {
    $.each($(".columnSelected"), function () {
      this.classList.remove("columnSelected");
    });
    var fieldInfo = this.parentElement;
    fieldInfo.classList.add("columnSelected");
    colSelected = this;

    var list = getRelatedList();
    var colSeq = $(fieldInfo).attr("sequence");
    var isDatasheetOption = false;
    var datasheetFieldsArray = [
      RE_ASSESS_LISTEDNESS,
      RE_ASSESS_LISTEDNESS_J,
      RE_ASSESS_LISTEDNESS_E_LABEL,
      RE_ASSESS_LISTEDNESS_J_LABEL,
    ];

    // show custom expression value
    showCustomExpression(list[colSeq].customExpression);
    showAdvancedSorting(list[colSeq].advancedSorting);
    showRenameArea(true, fieldInfo);

    var renameArea = $(".columnRenameArea")[0];
    // show rename value
    var colName = $(fieldInfo).attr("fieldName");
    var fieldLabelJ = $(fieldInfo).attr("fieldLabelJ");
    var fieldLabelE = $(fieldInfo).attr("fieldLabelE");
    if (list[colSeq].renameValue) {
      if ($.inArray(colName, datasheetFieldsArray) !== -1)
        isDatasheetOption = true;

      colName = list[colSeq].renameValue;
    }
    if (colName.indexOf("(J)") > -1) {
      $($(renameArea).find(".selectedColumnName")[0]).val(fieldLabelJ);
    } else if (colName.indexOf("(E)") > -1) {
      $($(renameArea).find(".selectedColumnName")[0]).val(fieldLabelE);
    } else {
      $($(renameArea).find(".selectedColumnName")[0]).val(colName);
    }

    // show rename description value
    var legendValue = $(fieldInfo).attr("legend");
    if (list[colSeq].newLegendValue) {
      legendValue = list[colSeq].newLegendValue;
    }
    var renameArea = $(".columnRenameArea")[0];
    $($(renameArea).find(".selectedColumnLegend")[0]).val(legendValue);

    // show column width
    var columnWidthValue = $(fieldInfo).attr("columnWidth");
    if (list[colSeq].columnWidth) {
      columnWidthValue = list[colSeq].columnWidth;
    }
    $($(renameArea).find(".selectedColumnWidth")[0]).val(columnWidthValue);

    // show color conditions
    if (templateType === "CASE_LINE") {
      if (
        $("#columnsContainerContainer .columnSelected").length > 0 ||
        $("#rowColumnsContainer .columnSelected").length > 0
      ) {
        var colorConditionsValue = $(fieldInfo).attr("colorConditions");
        if (list[colSeq].colorConditions) {
          colorConditionsValue = list[colSeq].colorConditions;
        }
        $($(renameArea).find(".colorConditionsJson")[0]).val(
          colorConditionsValue
        );
        $(".colorConditionsCantainer").show();
        createTablesForSavedColorConditions();
      } else {
        $($(renameArea).find(".colorConditionsJson")[0]).val("");
        $(".colorConditionsCantainer").hide();
      }
    }

    // show drilldown
    var drillDownTemplateValue = $(fieldInfo).attr("drillDownTemplate");
    if (list[colSeq].drillDownTemplate) {
      drillDownTemplateValue = list[colSeq].drillDownTemplate;
    }
    $($(renameArea).find(".drillDownTemplateCll")[0])
      .val(drillDownTemplateValue)
      .trigger("change");
    if (drillDownTemplateValue != "" && drillDownTemplateValue != null) {
      var isExecuted = $("#isExecuted").val();
      if (isExecuted == "true") {
        $(".templateViewButton").attr(
          "href",
          executedtmpltViewUrl + "/" + drillDownTemplateValue
        );
      } else {
        $(".templateViewButton").attr(
          "href",
          templateViewUrl + "/" + drillDownTemplateValue
        );
      }
      $(".templateViewButton").removeClass("hide");
    } else {
      $(".templateViewButton").addClass("hide");
    }
    var drillDownFilerColumnsValue = $(fieldInfo).attr("drillDownFilerColumns");
    if (list[colSeq].drillDownFilerColumns) {
      drillDownFilerColumnsValue = list[colSeq].drillDownFilerColumns;
    }
    $($(renameArea).find(".drillDownFilerColumns")[0])
      .attr("data-value", drillDownFilerColumnsValue)
      .select2("open")
      .select2("close") // workaround to trigger query function and append options, so value will be visible
      .val(drillDownFilerColumnsValue?.split(",") ?? [])
      .trigger("change");

    var hideSubtotal = $(".hideSubtotal");
    if (list[colSeq].type == "row") {
      hideSubtotal.parent().show();
      hideSubtotal = hideSubtotal[0];
      if (colSeq == 0) {
        hideSubtotal.checked = false;
        hideSubtotal.disabled = true;
      } else {
        hideSubtotal.disabled = false;
        if (list[colSeq].hideSubtotal) {
          hideSubtotal.checked = true;
        } else {
          hideSubtotal.checked = false;
        }
      }
    } else {
      hideSubtotal.parent().hide();
    }
    // show blinded/redacted values
    var blindedCheckbox = $(".blindedValues")[0];
    var redactedCheckbox = $(".redactedValue")[0];
    if (list[colSeq].blindedValue) {
      blindedCheckbox.checked = true;
      blindedCheckbox.disabled = true;
      $($(renameArea).find(".customExpressionArea")[0]).attr("hidden", "hidden");
      redactedCheckbox.checked = false;
      redactedCheckbox.disabled = true;
    } else if (list[colSeq].redactedValue) {
      redactedCheckbox.checked = true;
      redactedCheckbox.disabled = true;
      $($(renameArea).find(".customExpressionArea")[0]).attr("hidden", "hidden");
      blindedCheckbox.checked = false;
      blindedCheckbox.disabled = true;
    } else if (list[colSeq].fromProtectedList || list[colSeq].fromBlindedList) {
      redactedCheckbox.checked = !!list[colSeq].fromProtectedList;
      redactedCheckbox.disabled = true;
      blindedCheckbox.checked = !!list[colSeq].fromBlindedList;
      blindedCheckbox.disabled = true;
    } else {
      blindedCheckbox.disabled = false;
      blindedCheckbox.checked = false;
      redactedCheckbox.disabled = false;
      redactedCheckbox.checked = false;
    }

    if (list[colSeq].fromBlindedList || list[colSeq].fromProtectedList)
      $(".showCustomExpression").hide();
    else $(".showCustomExpression").show();
    if (templateType == TYPE_CASE_LINE) {
      // show comma-separated value
      if (list[colSeq].commaSeparatedValue) {
        $(".commaSeparated")[0].checked = true;
      } else {
        $(".commaSeparated")[0].checked = false;
      }

      // show suppress repeating values
      if (list[colSeq].suppressRepeatingValues) {
        $(".suppressRepeating")[0].checked = true;
      } else {
        $(".suppressRepeating")[0].checked = false;
      }

      // show suppress label
      if (list[colSeq].suppressLabel) {
        $(".suppressLabel")[0].checked = true;
      } else {
        $(".suppressLabel")[0].checked = false;
      }

      //Advanced Sorting
      if (list[colSeq].sortLevel) {
        $($(renameArea).find(".showAdvancedSorting")[0]).removeAttr("hidden");
      } else {
        $($(renameArea).find(".showAdvancedSorting")[0]).attr(
          "hidden",
          "hidden"
        );
      }
    }

    // show datasheet selection
    if ($.inArray(colName, datasheetFieldsArray) !== -1 || isDatasheetOption) {
      if($('#selectDatasheet').find('option').length === 0){
        $.ajax({
            url: datasheetUrl,
            type: "GET",
            dataType: "json",
            beforeSend: function () {
                $("#datasheetDropDownLoading").show();
                $("#datasheetDropDown").hide();
            },
        })
        .always(function () {
            $("#datasheetDropDownLoading").hide();
            $("#datasheetDropDown").show();
        })
        .done(function (result) {
            const dropdownData = _.isEmpty(result) ? [] : $.map(result, function (val) {
                return {id: val.sheetName, text: val.sheetName, hasChildrenFlag: val.hasChildrenFlag ? val.hasChildrenFlag : ''};
            });
            $('#selectDatasheet').select2({allowClear: true, placeholder: '', data: dropdownData});
            updateDatasheetSelection(colSeq, list);
        });
      } else {
        updateDatasheetSelection(colSeq, list);
      }
      $(".datasheetOption").show();
    } else {
      $(".datasheetOption").hide();
    }

    if (viewOnly) {
      $("#mainContent input").prop("disabled", true);
      $("#mainContent button").prop("disabled", true);
      $("#mainContent textarea").prop("disabled", true);
      $("#mainContent select").prop("disabled", true);
      $("#selectDatasheet").prop("disabled", true);
      $.each($(":checkbox"), function () {
        this.parentElement.classList.remove("add-cursor");
      });
      $(
        ".showColorConditionModal, .addParameter, .sectionRemove,.colorConditionUp,.colorConditionDown,.iconHelp,.removeOneCondition"
      ).addClass("hidden");
    }
    if ($(".viewButtons").attr("disabled")) {
      $(".viewButtons").removeAttr("disabled", "disabled");
    }
    $(".fieldSectionHeader").text(
      $(".selectedColumnName").val() + $.i18n._("settings")
    );
  });

  // Accordion method for open and close section on click method.

  var openCloseTab = function (section, strip) {
    //note- section=> panel which open after click, strip=> where  we click to open the section.
    section.toggle();
    if (strip.find(".openCloseIcon").hasClass("fa-caret-right")) {
      strip
        .find(".openCloseIcon")
        .removeClass("fa-caret-right")
        .addClass("fa-caret-down");
    } else {
      strip
        .find(".openCloseIcon")
        .removeClass("fa-caret-down")
        .addClass("fa-caret-right");
    }
  };

  $(document).on("click", ".showHideAdvancedSettings", function () {
    openCloseTab(
      $(this).closest(".d-t-border").find(".advancedSettings"),
      $(this)
    );
  });
  $(document).on("click", ".case-count-strip", function () {
    openCloseTab(
      $(this).closest(".d-t-border").find(".case-count-section"),
      $(this)
    );
  });
  $(document).on("click", ".conditional-strip", function () {
    openCloseTab(
      $(this).closest(".d-t-border").find(".color-section"),
      $(this)
    );
  });
  $(document).on("click", ".chartSettings", function () {
    openCloseTab($(".chartSettings-section"), $(this));
  });

  $(document).on("change", "#drillDownToCaseList", function () {
    var isChecked = $(this).prop("checked");
    if (!viewOnly && isChecked) {
      $(".drilldownTemplate").val("").trigger("change");
      $(".templateQueryIcon").addClass("hide");
      $(".drilldownTemplate").attr("disabled", true);
      $(".drilldownTemplate").parent().removeClass("add-cursor");
    } else {
      $(".drilldownTemplate").removeAttr("disabled", true);
      $(".drilldownTemplate").parent().addClass("add-cursor");
    }
  });

  $(document).on("click", ".closeRenameArea", function () {
    showRenameArea(false);
    colSelected.parentElement.classList.remove("columnSelected");
    colSelected = null;
  });
  $(document).on("click", "#addCLLSet", function () {
    var ind = -1;
    $.each($(".containerToBeSelected"), function () {
      var i = parseInt($(this).attr("data-setid"));
      if (i > ind) ind = i;
    });
    ind++;
    addCLLSet(ind);
  });
  $(document).on("click", ".removeCLLSet", function () {
    if ($("[id^=columnsContainer]").length > 2) {
      var ind = parseInt($(this).attr("data-setid"));
      removeSet(ind);
    }
  });

  $(document).on("click", ".commaSeparated", function () {
    var fieldInfo = colSelected.parentElement;
    var sequence = $(fieldInfo).attr("sequence");
    var list = getRelatedList();

    if (this.checked) {
      list[sequence].commaSeparatedValue = true;
    } else {
      list[sequence].commaSeparatedValue = false;
    }
  });

  $(document).on("click", ".suppressRepeating", function () {
    var fieldInfo = colSelected.parentElement;
    var sequence = $(fieldInfo).attr("sequence");
    var list = getRelatedList();

    if (this.checked) {
      list[sequence].suppressRepeatingValues = true;
    } else {
      list[sequence].suppressRepeatingValues = false;
    }
  });

  $(document).on("click", ".suppressLabel", function () {
    var fieldInfo = colSelected.parentElement;
    var sequence = $(fieldInfo).attr("sequence");
    var list = getRelatedList();

    if (this.checked) {
      list[sequence].suppressLabel = true;
    } else {
      list[sequence].suppressLabel = false;
    }
  });

  $(".cLLCheckBoxes").on("click", ".hideSubtotal", function () {
    var fieldInfo = colSelected.parentElement;
    var sequence = $(fieldInfo).attr("sequence");
    var list = getRelatedList();
    list[sequence].hideSubtotal = !!this.checked;
  });

  $(".cLLCheckBoxes").on("click", ".redactedValue", function () {
    var fieldInfo = colSelected.parentElement;
    var sequence = $(fieldInfo).attr("sequence");
    var list = getRelatedList();

    var customExpression = $($(".columnRenameArea")[0]).find(
      ".customExpressionArea"
    )[0];

    if (this.checked) {
      list[sequence].redactedValue = true;
      $(".cLLCheckBoxes").find(".blindedValues").prop("checked", false);
      list[sequence].blindedValue = false;
      $(".customExpressionValue").val(
        "decode (1,1,'Redacted', " + $(fieldInfo).attr("argusName") + ")"
      );
    } else {
      list[sequence].redactedValue = false;
      if (list[sequence].fromBlindedList || list[sequence].fromProtectedList) {
        list[sequence].blindedValue = true;
        $(".cLLCheckBoxes").find(".blindedValues").prop("checked", true);
      } else {
        $(customExpression).attr("hidden", "hidden");
      }
      $(".customExpressionValue").val($(fieldInfo).attr("argusName"));
    }
    $(".customExpressionValue").trigger("change");
  });

  $(".cLLCheckBoxes").on("click", ".blindedValues", function () {
    var fieldInfo = colSelected.parentElement;
    var sequence = $(fieldInfo).attr("sequence");
    var list = getRelatedList();

    var customExpression = $($(".columnRenameArea")[0]).find(
      ".customExpressionArea"
    )[0];

    if (this.checked) {
      list[sequence].blindedValue = true;
      $(".cLLCheckBoxes").find(".redactedValue").prop("checked", false);
      list[sequence].redactedValue = false;
      $(".customExpressionValue").val($(fieldInfo).attr("argusName"));
    } else {
      list[sequence].blindedValue = false;
      if (list[sequence].fromBlindedList || list[sequence].fromProtectedList) {
        $(".cLLCheckBoxes").find(".redactedValue").prop("checked", true);
        list[sequence].redactedValue = true;
        $(".customExpressionValue").val(
          "decode (1,1,'Redacted', " + $(fieldInfo).attr("argusName") + ")"
        );
      } else {
        $(".customExpressionValue").val($(fieldInfo).attr("argusName"));
        $(customExpression).attr("hidden", "hidden");
      }
    }
    $(".customExpressionValue").trigger("change");
  });

  $(document).on("click", ".showCustomExpression", function () {
    var sequence = $(colSelected.parentElement).attr("sequence");
    var list = getRelatedList();
    var customExpression = $($(".columnRenameArea")[0]).find(
      ".customExpressionArea"
    )[0];
    if (customExpression.hasAttribute("hidden")) {
      $(customExpression).removeAttr("hidden");
      var value = $(colSelected.parentElement).attr("argusName");
      if (list[sequence].customExpression) {
        value = list[sequence].customExpression;
      }
      $($(".columnRenameArea")[0]).find(".customExpressionValue")[0].value =
        value;
    } else {
      $(customExpression).attr("hidden", "hidden");
    }
  });

  $(document).on("click", ".showAdvancedSorting", function () {
    var sequence = $(colSelected.parentElement).attr("sequence");
    var list = getRelatedList();
    var advancedSorting = $($(".columnRenameArea")[0]).find(
      ".advancedSortingArea"
    )[0];
    if ($(advancedSorting).attr("hidden")) {
      $(advancedSorting).removeAttr("hidden");
      var value = $(colSelected.parentElement).attr("argusName");
      if (list[sequence].advancedSorting) {
        value = list[sequence].advancedSorting;
      }
      $($(".columnRenameArea")[0]).find(".advancedSortingValue")[0].value =
        value;
    } else {
      $(advancedSorting).attr("hidden", "hidden");
    }
  });

  $(document).on("change", ".customExpressionValue", function () {
    var seq = $(colSelected.parentElement).attr("sequence");
    var renameArea = $(".columnRenameArea")[0];
    var customExpressionValue = $(
      $(renameArea).find(".customExpressionValue")[0]
    ).val();
    if (
      customExpressionValue.trim() ===
        $(colSelected.parentElement).attr("argusName") ||
      customExpressionValue.trim() === ""
    ) {
      customExpressionValue = "";
    }

    var list = getRelatedList();
    list[seq].customExpression = decodeFromHTML(customExpressionValue);
  });

  $(document).on("change", ".advancedSortingValue", function () {
    var seq = $(colSelected.parentElement).attr("sequence");
    var renameArea = $(".columnRenameArea")[0];
    var advancedSortingValue = $($(renameArea).find(".advancedSortingValue")[0])
      .val()
      .trim();
    if (
      advancedSortingValue == $(colSelected.parentElement).attr("argusName") ||
      advancedSortingValue == ""
    ) {
      advancedSortingValue == "";
    }

    var list = getRelatedList();
    list[seq].advancedSorting = advancedSortingValue;
  });

  $(document).on("change", ".selectedColumnName", function () {
    var fieldInfo = colSelected.parentElement;
    var renameSeq = $(fieldInfo).attr("sequence");
    var renameArea = $(".columnRenameArea")[0];
    // may need trim() on newName
    var newName = $($(renameArea).find(".selectedColumnName")).val();
    $(".fieldSectionHeader").text(newName + $.i18n._("settings"));
    var list = getRelatedList();

    if (newName != $(fieldInfo).attr("fieldName")) {
      list[renameSeq].renameValue = newName;
      $(fieldInfo).attr("renamedTo", newName);
    } else {
      list[renameSeq].renameValue = null;
      $(fieldInfo).removeAttr("renamedTo");
    }

    $(colSelected).text(newName);

    var desc = $(fieldInfo).attr("description");
    if (desc)
      $(colSelected).html(
        '<i data-toggle="popover" class="iPopover" data-trigger="hover"  data-container="body" data-content="' +
          replaceBracketsAndQuotes(desc) +
          '"><span class="fa fa-info-circle" style="font-size: 16px; cursor: pointer"></span></i> ' +
          encodeToHTML(newName)
      );
    else $(colSelected).text(newName);
    $('[data-toggle="popover"]').popover();
  });

  $(document).on("change", ".selectedColumnLegend", function () {
    var fieldInfo = colSelected.parentElement;
    var renameSeq = $(fieldInfo).attr("sequence");
    var renameArea = $(".columnRenameArea")[0];
    var newLegend = $($(renameArea).find(".selectedColumnLegend")).val().trim();

    var list = getRelatedList();

    if (newLegend != $(fieldInfo).attr("legend")) {
      list[renameSeq].newLegendValue = newLegend;
      $(fieldInfo).attr("renamedLegendTo", newLegend);
    } else {
      list[renameSeq].renameValue = null;
      $(fieldInfo).removeAttr("renamedLegendTo");
    }
  });

  $(document).on("click", ".resetThisColLegend", function () {
    var fieldInfo = colSelected.parentElement;
    var originalValue = $(fieldInfo).attr("legend");
    var renameArea = $(".columnRenameArea")[0];
    $($(renameArea).find(".selectedColumnLegend")[0]).val(originalValue);
    $(fieldInfo).removeAttr("renamedLegendTo");
    var list = getRelatedList();
    list[$(fieldInfo).attr("sequence")].newLegendValue = null;
  });

  $(document).on("click", ".resetThisCol", function () {
    var fieldInfo = colSelected.parentElement;
    var originalName = $(fieldInfo).attr("fieldName");
    var renameArea = $(".columnRenameArea")[0];
    $($(renameArea).find(".selectedColumnName")[0]).val(originalName);
    var desc = $(fieldInfo).attr("description");
    if (desc)
      $(colSelected).html(
        '<i data-toggle="popover" class="iPopover" data-trigger="hover"  data-container="body" data-content="' +
          replaceBracketsAndQuotes(desc) +
          '"><span class="fa fa-info-circle" style="font-size: 16px; cursor: pointer"></span></i> ' +
          encodeToHTML(originalName)
      );
    else $(colSelected).text(originalName);

    $('[data-toggle="popover"]').popover();
    $(fieldInfo).removeAttr("renamedTo");
    var list = getRelatedList();
    list[$(fieldInfo).attr("sequence")].renameValue = null;
    $(".fieldSectionHeader").text(originalName + $.i18n._("settings"));
  });

  $(document).on("change", ".selectedColumnWidth", function () {
    var fieldInfo = colSelected.parentElement;
    var renameSeq = $(fieldInfo).attr("sequence");
    var renameArea = $(".columnRenameArea")[0];
    // may need trim() on newName
    var newColumnWidth = $($(renameArea).find(".selectedColumnWidth")).val();

    var list = getRelatedList();

    if (newColumnWidth != $(fieldInfo).attr("columnWidth")) {
      list[renameSeq].columnWidth = newColumnWidth;
    } else {
      list[renameSeq].columnWidth = null;
    }
  });

  $(document).on(
    "select2:select select2:unselect",
    ".drilldownTemplate, .drillDownFilerColumns",
    function () {
      var drillDownTemplateValue = $(this).attr("data-value");
      if (drillDownTemplateValue != "" && drillDownTemplateValue != null) {
        var drillDownTemplateVal = $(this)
          .closest(".row")
          .find(".templateQueryIcon");
        drillDownTemplateVal.attr(
          "href",
          templateViewUrl + "/" + drillDownTemplateValue
        );
        drillDownTemplateVal.removeClass("hide");
      } else {
        var drillDownTemplateVal = $(this)
          .closest(".row")
          .find(".templateQueryIcon");
        drillDownTemplateVal.addClass("hide");
      }
    }
  );

  function selectTemplateOnChange(templateDropdownWidget, obj) {
    if (templateDropdownWidget != "" && templateDropdownWidget != null) {
      $(".templateViewButton").attr(
        "href",
        templateViewUrl + "/" + templateDropdownWidget
      );
      $(".templateViewButton").removeClass("hide");
    } else {
      $(".templateViewButton").addClass("hide");
    }
  }

  $(document).on("change", ".colorConditionsJson", function () {
    if (colSelected) {
      var fieldInfo = colSelected.parentElement;
      var seq = $(fieldInfo).attr("sequence");
      var list = getRelatedList();
      list[seq].colorConditions = $(this).val();
    }
  });

  $(document).on("change", "#worldMap", function () {
    if ($(this).is(":checked")) {
      $("#chartExportAsImage").prop("checked", true);
      $("#chartExportAsImage").prop("disabled", true);
      $(".chartSettings-section").hide();
      $(".worldMapConfigDiv").show();
    } else {
      $("#chartExportAsImage").prop("disabled", false);
      $(".chartSettings-section").show();
      $(".worldMapConfigDiv").hide();
    }
  });

  $("#worldMap").trigger("change");

  $(document).on(
    "select2:select select2:unselect",
    ".drillDownTemplateCll, .drillDownFilerColumns",
    function () {
      var fieldInfo = colSelected.parentElement;
      var renameSeq = $(fieldInfo).attr("sequence");
      var renameArea = $(".columnRenameArea")[0];

      var drillDownTemplateValue = $(
        $(renameArea).find(".drillDownTemplateCll")
      ).val();
      var drillDownFilerColumnsValue = $(
        $(renameArea).find(".drillDownFilerColumns")
      ).val();

      var list = getRelatedList();

      list[renameSeq].drillDownTemplate = drillDownTemplateValue;
      list[renameSeq].drillDownFilerColumns = drillDownFilerColumnsValue;

      selectTemplateOnChange(drillDownTemplateValue);
    }
  );

  $(document).on("click", ".resetThisColWidth", function () {
    var fieldInfo = colSelected.parentElement;
    var originalValue = $(fieldInfo).attr("columnWidth");
    var renameArea = $(".columnRenameArea")[0];
    $($(renameArea).find(".selectedColumnWidth")[0]).val(originalValue);
    var list = getRelatedList();
    list[$(fieldInfo).attr("sequence")].columnWidth = null;
  });

  $(document).on("click", ".sortIcon", function () {
    if (!viewOnly) {
      $.each($(".columnSelected"), function () {
        this.classList.remove("columnSelected");
      });
      var renameArea = $(".columnRenameArea")[0];
      var fieldInfo = this.parentElement;
      fieldInfo.classList.add("columnSelected");
      var seq = $(fieldInfo).attr("sequence");
      var preSelected = colSelected;
      colSelected = this;
      var list = getRelatedList();

      if (
        sortLevels > 0 ||
        (sortLevels <= 0 && this.classList.contains("sortEnabled"))
      ) {
        if (this.classList.contains("sortDisabled")) {
          removeSortIcon(this.classList);
          addAscIcon(this.classList);
          this.style.opacity = 1.0;

          list[seq].sortLevel = sortLevel;
          list[seq].sort = SORT_ASCENDING;
          sortObjList.push(list[seq]);

          if (templateType != TYPE_DATA_TAB) {
            $($(fieldInfo).find(".sortOrderSeq")).text(sortLevel);
            if (list[seq].sortLevel) {
              $($(renameArea).find(".showAdvancedSorting")[0]).removeAttr(
                "hidden"
              );
            }
          }

          sortLevel++;
        } else if (this.classList.contains("sortAscending")) {
          removeAscIcon(this.classList);
          addDescIcon(this.classList);
          this.style.opacity = 1.0;

          list[seq].sort = SORT_DESCENDING;

          if (templateType != TYPE_DATA_TAB) {
            $($(fieldInfo).find(".sortOrderSeq")).text(sortLevel - 1);
          }
        } else if (this.classList.contains("sortDescending")) {
          removeDescIcon(this.classList);
          addSortIcon(this.classList);
          this.style.opacity = 0.3;

          list[seq].sortLevel = 0;
          list[seq].sort = SORT_DISABLED;

          // update list
          var index = sortObjList.indexOf(list[seq]);
          sortObjList.splice(index, 1);
          $.each(sortObjList, function (index, obj) {
            obj.sortLevel = index + 1;
          });

          if (templateType != TYPE_DATA_TAB) {
            $($(fieldInfo).find(".sortOrderSeq")).text("");
            $($(renameArea).find(".showAdvancedSorting")[0]).attr(
              "hidden",
              "hidden"
            );
          }
          sortLevel--;
        }
      }
      updateSortOrderText();
      colSelected = preSelected;
    }
  });

  $(document).on("click", ".undoTagButton", function () {
    var reportField;
    var containerToDelete;
    var isGroup;
    var isRowColumn;
    var isRow;

    if (undoTagList.length > 0) {
      reportField = undoTagList[undoTagList.length - 1][0];
      containerToDelete = undoTagList[undoTagList.length - 1][1];
      $(
        $('div[fieldid="' + reportField.id + '"]').closest(".wholeColumn")
      ).remove();
      undoTagList = undoTagList.splice(0, undoTagList.length - 1);
      if (undoTagList.length == 0) $("#resetTagId").hide();
      removeLastTag++;
    } else {
      $("#resetTagId").hide();
    }
  });

  $(document).on("click", ".removeColumn", function () {
    var isGroup = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("groupingContainer");
    var isRowColumn = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("rowColumnsContainer");
    var isRow = $(this)
      .closest(".rowsAndColumnsContainer")
      .hasClass("rowsContainer");

    var colMeasIndex = $(this).closest(".columnMeasureSet").attr("sequence");

    if (!viewOnly) {
      var fieldInfo = this.parentElement;
      var currentSequence = $(fieldInfo).attr("sequence");
      var colNameSelected = $(fieldInfo).attr("fieldName");

      var preSelected = colSelected;
      colSelected = this;
      var list = getRelatedList();
      var removeSortLevel = list[currentSequence].sortLevel;
      showRenameArea(false);

      if (
        colNameSelected === RE_ASSESS_LISTEDNESS ||
        colNameSelected === RE_ASSESS_LISTEDNESS_J ||
        colNameSelected === RE_ASSESS_LISTEDNESS_E_LABEL ||
        colNameSelected === RE_ASSESS_LISTEDNESS_J_LABEL
      ) {
        numOfReassessListedness--;
        if (numOfReassessListedness === 0) {
          $("#reassessListedness").val(null).trigger("change");
          $("[name='templtReassessDate']").val("");
          $("#templtCustomDateSelector").hide();
          $(".reassessListedness").hide();
        }
      }

      var wholeColumn = fieldInfo.parentElement.parentElement;
      if (list[currentSequence].stackId > 0) {
        $(fieldInfo).remove();
        var colsLeft = $(wholeColumn).find(".fieldInfo");
        if (colsLeft.length == 1) {
          list[currentSequence].stackId = 0;
        }
      } else if (isRow) {
        var containerWidth = $(wholeColumn.parentElement).width();
        var displayWidth = $(wholeColumn.parentElement.parentElement).width();
        if (containerWidth > displayWidth) {
          containerWidth -= $(wholeColumn).width();
          if (containerWidth < displayWidth) {
            containerWidth = displayWidth;
          }
          $(wholeColumn.parentElement).width(containerWidth);
        }
        $(wholeColumn).remove();
      } else if (isGroup && templateType === TYPE_DATA_TAB) {
        var containerWidth = $(wholeColumn.parentElement).width();
        containerWidth -= $(wholeColumn).width();
        $(wholeColumn.parentElement).width(containerWidth);
        $(wholeColumn).remove();
      } else if (isRowColumn || templateType === TYPE_DATA_TAB) {
        $(wholeColumn).remove();
      } else {
        var containerWidth = $(wholeColumn.parentElement).width();
        containerWidth -= $(wholeColumn).width();
        $(wholeColumn.parentElement).width(containerWidth);
        $(wholeColumn).remove();
      }

      if (removeSortLevel > 0) {
        // update list
        var index = sortObjList.indexOf(list[currentSequence]);
        sortObjList.splice(index, 1);
        $.each(sortObjList, function (index, obj) {
          obj.sortLevel = index + 1;
        });
        sortLevel--;
      }

      list.splice(currentSequence, 1);
      if (isRow) {
        sequence_row--;
      } else if (isGroup) {
        sequence_group--;
      } else if (isRowColumn) {
        sequence_rowColumn--;
      } else if (templateType == TYPE_DATA_TAB) {
        sequence_col_dt[colMeasIndex]--;
      } else {
        sequence_col--;
      }

      updateColumnListAndSortOrder();
      colSelected = preSelected;
      updateColorCellConditionsAfterDelete();
    }
    updateAllTimeframesCheckbox();
  });

  $("#templateForm").on("submit", function (event) {
    return submitForm();
  });

  updateAllTimeframesCheckbox();
});

// Sort Icon -------------------------------------------------------------------------------------------------------
function removeSortIcon(classList) {
  classList.remove("fa-sort");
  classList.remove("sortDisabled");
}

function removeAscIcon(classList) {
  classList.remove("fa-sort-asc");
  classList.remove("sortAscending");
  classList.remove("sortEnabled");
}

function removeDescIcon(classList) {
  classList.remove("fa-sort-desc");
  classList.remove("sortDescending");
  classList.remove("sortEnabled");
}

function addSortIcon(classList) {
  classList.add("fa-sort");
  classList.add("sortDisabled");
}

function addAscIcon(classList) {
  classList.add("fa-sort-asc");
  classList.add("sortAscending");
  classList.add("sortEnabled");
}

function addDescIcon(classList) {
  classList.add("fa-sort-desc");
  classList.add("sortDescending");
  classList.add("sortEnabled");
}

//show or hide "All Timeframes" checkbox depends on selected columns
function updateAllTimeframesCheckbox() {
  if ($("#templateType").val() == "DATA_TAB") {
    var dateFields = $(".fieldInfo[dateType=1]");
    var input = $("input[name=allTimeframes]");
    if (dateFields.length == 0) {
      input[0].checked = false;
      $("#allTimeframesWraper").hide();
    } else {
      $("#allTimeframesWraper").show();
    }
  }
}
function updateAllTimeframesCheckbox() {
  if ($("#templateType").val() == "DATA_TAB") {
    var dateFields = $(".fieldInfo[dateType=1]");
    var input = $("input[name=allTimeframes]");
    if (dateFields.length == 0) {
      input[0].checked = false;
      $("#allTimeframesWraper").hide();
    } else {
      $("#allTimeframesWraper").show();
    }
  }
}

// --------------------------------------------------------------------------------------------------- END SORT ICON
