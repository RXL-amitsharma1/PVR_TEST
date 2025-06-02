var selectedGroupId;
$(function () {
  if ($("#rxTableFieldProfile").is(":visible")) {
    var table = $("#rxTableFieldProfile")
      .DataTable({
        layout: {
          topStart: null,
          topEnd: { search: { placeholder: "Search" } },
          bottomStart: [
            "pageLength",
            "info",
            {
              paging: {
                type: "full_numbers",
              },
            },
          ],
          bottomEnd: null,
        },
        language: { search: ''},
        //"sPaginationType": "bootstrap",
        stateSave: true,
        stateDuration: -1,
        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        ajax: {
          url: FIELDPROFILE.listUrl,
          dataSrc: "data",
          data: function (d) {
            d.searchString = d.search.value;
            if (d.order.length > 0) {
              d.direction = d.order[0].dir;
              //Column header mData value extracting
              d.sort = d.columns[d.order[0].column].data;
            }
          },
        },
        rowId: "fieldProfileUniqueId",
        aaSorting: [],
        order: [[2, "desc"]],
        bLengthChange: true,

        aLengthMenu: [
          [10, 25, 50, 100],
          [10, 25, 50, 100],
        ],
        pagination: true,
        iDisplayLength: 50,

        drawCallback: function (settings) {
          pageDictionary(
            $("#rxTableFieldProfile_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );
        },

        aoColumns: [
          //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
          {
            mData: "name",
            mRender: function (data, type, row) {
              return encodeToHTML(data);
            },
          },
          {
            mData: "description",
            mRender: function (data, type, row) {
              var text = data == null ? "" : encodeToHTML(data);
              return '<div class="comment">' + text + "</div>";
            },
          },
          {
            mData: "lastUpdated",
            aTargets: ["lastUpdated"],
            sClass: "dataTableColumnCenter forceLineWrapDate",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
            },
          },
          {
            mData: "dateCreated",
            aTargets: ["dateCreated"],
            sClass: "dataTableColumnCenter forceLineWrapDate",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
            },
          },
          {
            mData: "createdBy",
            mRender: function (data, type, row) {
              return data == null
                ? ""
                : data
                    .replace(/&/g, "&amp;")
                    .replace(/</g, "&lt;")
                    .replace(/>/g, "&gt;");
            },
          },
          {
            mData: null,
            bSortable: false,
            aTargets: ["id"],
            mRender: function (data, type, full) {
              var actionButton =
                '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs" href="' +
                FIELDPROFILE.viewUrl +
                "/" +
                data["id"] +
                '">' +
                $.i18n._("view");
              if (!(data["name"] === privacyProfileName)) {
                actionButton += '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' +
                FIELDPROFILE.editUrl +
                "/" +
                data["id"] +
                '">' +
                $.i18n._("edit") +
                '</a></li> \
                            <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                data-target="#deleteModal" data-instancetype="' +
                $.i18n._("FIELDPROFILE") +
                '" data-instanceid="' +
                data["id"] +
                '" data-instancename="' +
                replaceBracketsAndQuotes(data["name"]) +
                '">' +
                $.i18n._("delete") +
                "</a></li> \
                        </ul> \
                    </div>";
              }
              return actionButton;
            },
          },
        ],
      })
      .on("draw.dt", function () {
        setTimeout(function () {
          $("#rxTableFieldProfile tbody tr").each(function () {
            $(this).find("td:eq(2)").attr("nowrap", "nowrap");
            $(this).find("td:eq(3)").attr("nowrap", "nowrap");
          });
        }, 100);
        addReadMoreButton(".comment", 70);
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });
    actionButton("#rxTableFieldProfile");
    loadTableOption("#rxTableFieldProfile");
  }

  if (typeof document != "undefined") {
    $('.panel-collapse.in').each(function () {
      loadAccordionContent($(this));
    });
  }

  $(".groupNameKey").on("click", function () {
    var groupId = $(this).data("value");
    selectedGroupId = $(this).attr("id");
    $("#addRemoveFieldModal #allowedFieldData").html("");
    $("#addRemoveFieldModal").modal("show");
    $("#allowedFieldSpinner").show();
    $.ajax({
      url:
        FIELDPROFILE.ajaxReportFieldByGroupUrl +
        "?name=" +
        groupId +
        "&id=" +
        jQuery("form#fieldProfileForm #id").val(),
      dataType: "html",
    })
      .done(function (data) {
        var contentDiv = $("div." + groupId).find(".unSelectedFields");
        if (data == "") {
          contentDiv
            .html("<div>" + $.i18n._("fieldprofile.nofild.label") + "</div>")
            .addClass("data");
        } else {
          contentDiv.html(data).addClass("data");
        }
        if (groupId == "groupName-" + selectedGroupId) {
          $("#allowedFieldSpinner").hide();
          if (data == "") {
            contentDiv
              .html("<div>" + $.i18n._("fieldprofile.nofild.label") + "</div>")
              .addClass("data");
            $("#allowedFieldData").html(contentDiv.html()).show();
          } else {
            contentDiv.html(data).addClass("data");
            $("#allowedFieldData").html(contentDiv.html()).show();
            var selectedValues = $(
                "div." + groupId + ' input.blindedReportFields, ' +
                "div." + groupId + ' input.protectedReportFields, ' +
                "div." + groupId + ' input.hiddenReportFields'
            )
              .map(function () {
                return "" + $(this).val();
              })
              .get();
            var selectBox = $("#addRemoveFieldModal #allowedFields");
            if (selectBox && selectedValues != undefined) {
              selectBox.val(selectedValues);
            }
            $("#addRemoveFieldModal #allowedFields").pickList();
            $(".pickList_list.pickList_targetList").css("height", "305px");
            $(".pickList_listLabel.pickList_sourceListLabel").text($.i18n._("picklist.available"));
            $(".pickList_listLabel.pickList_targetListLabel").text($.i18n._("picklist.selected"));
            $(".pickList_listLabel.pickList_sourceListLabel").html(
              $(".pickList_listLabel.pickList_sourceListLabel").html() +
                '<br> <input class="fieldNameFilter" style="width:100%;" placeholder="' +
                $.i18n._("fieldprofile.search.label") +
                '" >'
            );
          }
        }
      })
      .fail(function (error) {
        $("#allowedFieldSpinner").hide();
        $("#addRemoveFieldModal").modal("hide");
        alert("Some error occurred");
      });

    var existingData = [];
    $.each(
      $(this).parent().parent().find("div.selectedFields .demo-chip"),
      function () {
        existingData.push($(this).text().slice(0, -1).trim());
      });
    if (existingData.length > 0) {
      $.each($("#allowedFieldData .pickList_sourceList li"), function () {
        if ($.inArray($(this).text().trim(), existingData) != -1) {
          $("div#allowedFieldData .pickList_targetList").append(
            $(this).clone());
          $(this).remove();
        }
      });
    }
  });
  $(document).on("keyup", ".fieldNameFilter", function () {
    var f = $(this).val().toLowerCase();
      if(_.isEmpty(f)) {
          $(this).parents(".pickList").find(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", false);
      }else {
          $(this).parents(".pickList").find(".pickList_controlsContainer").find(".pickList_addAll").attr("disabled", true);
      }
      $(".pickList_sourceList li").each(function () {
      var elem = $(this);
      if (elem.html().toLowerCase().indexOf(f) > -1) elem.show();
      else elem.hide();
    });
  });

  $("#addRemoveFieldModal").on("shown.bs.modal", function() {
      $('.pickList_list').attr("tabindex", 0);
  });

  $("#saveButton").on("click", function () {
    var form = $('form[name="fieldProfileForm"]');

    var blindedFields = []
    $('input.blindedReportFields[type="radio"]:checked').each(function() {
      blindedFields.push($(this).val());
    });

    var protectedFields = []
    $('input.protectedReportFields[type="radio"]:checked').each(function() {
      protectedFields.push($(this).val());
    });

    var hiddenFields = []
    $('input.hiddenReportFields[type="radio"]:checked').each(function() {
      hiddenFields.push($(this).val());
    });

    $(document).find("#blindedFields").val(blindedFields.join(","));
    $(document).find("#protectedFields").val(protectedFields.join(","));
    $(document).find("#hiddenFields").val(hiddenFields.join(","));

    form.find('input[type="radio"]').detach();

    form.submit();
  })
});

function loadAccordionContent(panel) {
  var chipGrid = panel.find(".chip-grid");
  if (chipGrid.contents().length > 0 || method == 'create') {
    return;
  }

  // showLoader();
  var groupName = panel.prev('.panel-heading').find('[data-toggle="collapse"]').data('groupname');
  var loaderSelector = "#fieldGroup-" + groupName + "-loader";

  $.ajax({
    url: loadGroupDataUrl,
    method: 'GET',
    data: { id: $('.fieldProfileId').val(), groupName: groupName },
    dataType: 'json',
    beforeSend: function () {
      $(loaderSelector).css("display", "block");
    },
    success: function (response) {
      $(loaderSelector).css("display", "none");
      var reportFields = response.reportFields;
      var fieldNames = response.fieldNameMap;
      var chipHtml = '';

      reportFields.forEach(function (field, fieldIndex) {
        var fieldId = field.id;
        var fieldName = fieldNames[field.id];

        var isBlinded = field.isBlinded ? "checked" : "";
        var isProtected = field.isProtected ? "checked" : "";
        var isHidden = field.isHidden ? "checked" : "";
        var isDisabled = disabled ? "disabled" : "";

        chipHtml += '<span class="snippet-demo-container demo-chip demo-chip__basic">' +
            '<span class="mdl-chip mdl-chip-wrapped">' +
            '<span class="mdl-chip__text forceLineWrap">' + fieldName + '&nbsp;' +
            '<span style="display: inline-block;">' +
            '( <input type="radio" value="' + fieldId + '" name="fieldRestrictions-' + groupName + '-' + fieldIndex + '" class="blindedReportFields" ' + isBlinded + '> ' + blindedLabel + ')&nbsp;' +
            '</span>' +
            '<span style="display: inline-block;">' +
            '( <input type="radio" value="' + fieldId + '" name="fieldRestrictions-' + groupName + '-' + fieldIndex + '" class="protectedReportFields" ' + isProtected + '> ' + protectedLabel + ')&nbsp;' +
            '</span>' +
            '<span style="display: inline-block;">' +
            '( <input type="radio" value="' + fieldId + '" name="fieldRestrictions-' + groupName + '-' + fieldIndex + '" class="hiddenReportFields" ' + isHidden + '> ' + hiddenLabel + ')&nbsp;' +
            '</span>';
        if (!isDisabled) {
          chipHtml += '<span class="closebtn" data-evt-clk=\'{"method": "removeParentNode", "params": []}\'>&times;</span>'
        }
        chipHtml += '</span>' +
            '</span>' +
            '</span>';
      });

      chipGrid.append(chipHtml);
    },
    error: function () {
      chipGrid.html('Failed to load data.');
    }
  });
}

$(document).on('shown.bs.collapse', '.panel-collapse', function () {
  loadAccordionContent($(this));
});

