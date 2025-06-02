var formattedData = [];
var CORRECTIVE_IDENTIFIER = "corrective";
var PREVENTATIVE_IDENTIFIER = "preventative";
$(function () {
  var mappingTable;
  initRodSelectionButtons();
  var capaTable = $("#capaTable");
  var init_mapping_table = function (tableIdentifier) {
    if ($.fn.DataTable.isDataTable("#capaTable")) {
      capaTable.DataTable().destroy();
    }
    var tableDataUrl;
    var deleteLabel;
    var deleteAction;
    if (tableIdentifier === CORRECTIVE_IDENTIFIER) {
      tableDataUrl = getCorrectiveDataList;
      deleteLabel = "app.corrective.action.label";
      deleteAction = "deleteCorrective";
    } else if (tableIdentifier === PREVENTATIVE_IDENTIFIER) {
      tableDataUrl = getPreventativeDataList;
      deleteLabel = "app.preventative.action.label";
      deleteAction = "deletePreventative";
    }
    mappingTable = capaTable.DataTable({
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
      initComplete: function () {
        actionButton("#capaTable");
      },

      ajax: {
        url: tableDataUrl,
        dataSrc: function (data) {
          console.log(data);
          if (tableIdentifier === CORRECTIVE_IDENTIFIER) {
            transformCorrectiveActionJson(data);
          } else if (tableIdentifier === PREVENTATIVE_IDENTIFIER) {
            transformPreventativeActionJson(data);
          }
          return formattedData;
        },
      },

      aaSorting: [[0, "desc"]],
      order: [[1, "asc"]],
      bLengthChange: true,
      iDisplayLength: 10,
      aoColumns: [
        {
          mData: "id",
          visible: false,
          mRender: function (data, type, row) {
            return '<span id="lateId">' + row.id + "</span>";
          },
        },
        {
          mData: "name",
          mRender: function (data, type, row) {
            return encodeToHTML(data);
          },
        },
        {
          mData: "ownerApp",
          mRender: function (data, type, row) {
            return encodeToHTML(data);
          },
        },
        {
          mData: null,
          bSortable: false,
          width: "8%",
          mRender: function (data, type, row) {
            var actionButton =
              '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs capa-show" role="menuitem" href="javascript:void(0)" data-id="' +
              row.id +
              '" data-label="' +
              encodeToHTML(row.name) +
              '" data-toggle="modal" data-target="#capaModal" data-app-type=' +
              row.ownerApp +
              ">" +
              $.i18n._("view") +
              '</a> \
                            <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                               <span class="caret"></span> \
                               <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="capa-edit" role="menuitem" href="javascript:void(0)" data-id="' +
              row.id +
              '" data-label="' +
              encodeToHTML(row.name) +
              '" data-toggle="modal" data-target="#capaModal" data-app-type=' +
              row.ownerApp +
              ">" +
              $.i18n._("edit") +
              '</a></li> \
                                <li role="presentation"><a class="work-flow-edit hide-delete" role="menuitem" href="#" data-toggle="modal" data-action=' +
              deleteAction +
              ' \
                                    data-target="#deleteModal" data-instancetype="' +
              $.i18n._(deleteLabel) +
              '" data-instanceid="' +
              row.id +
              '" data-instancename="' +
              replaceBracketsAndQuotes(encodeToHTML(row.name)) +
              '" data-app-type = ' +
              row.ownerApp +
              ">" +
              $.i18n._("delete") +
              "</a></li> \
                                </ul> \
                        </div>";
            return actionButton;
          },
        },
      ],
    });
    return mappingTable;
  };

  init_mapping_table(CORRECTIVE_IDENTIFIER);

  function transformCorrectiveActionJson(data) {
    formattedData = [];

    for (var i in data.correctiveActionJson) {
      var correctiveActionObj = JSON.parse(data.correctiveActionJson[i]);
      var row = {};
      row.id = correctiveActionObj.id;
      row.name = correctiveActionObj.textDesc;
      row.ownerApp = correctiveActionObj.ownerApp;
      formattedData.push(row);
    }
  }

  function transformPreventativeActionJson(data) {
    formattedData = [];

    for (var i in data.preventativeActionJson) {
      var preventativeActionObj = JSON.parse(data.preventativeActionJson[i]);
      var row = {};
      row.id = preventativeActionObj.id;
      row.name = preventativeActionObj.textDesc;
      row.ownerApp = preventativeActionObj.ownerApp;
      formattedData.push(row);
    }
  }

  $("#createButton").on("click", function (e) {
    $("#warningNote").show();
    $("#objectId").val(null);
    $("#label").val("");
    $("#ownerApp").attr("disabled", false);
    $("#submitButton").text($.i18n._("save"));
    $("#ownerApp").attr("disabled", false);
    $("#label").prop("readonly", false);
    var dataKey;
    $("#cancelButtonForView").hide($.i18n._("update"));
    $("#cancelButtonForView").attr("hidden", true);
    $("#submitButton").show($.i18n._("Save"));
    $("#cancelButton").show($.i18n._("update"));
    if ($("#showCorrective").hasClass("active")) {
      $("#capaModal")
        .find(".rxmain-container-header-label")
        .first()
        .text($.i18n._("app.corrective.action.create"));
    } else {
      $("#capaModal")
        .find(".rxmain-container-header-label")
        .first()
        .text($.i18n._("app.preventative.action.create"));
    }
  });

  $(document).on("click", ".capa-edit", function (e) {
    $("#warningNote").show();
    var labelElement = $("#label");
    labelElement.val($(this).data("label"));
    labelElement.prop("readonly", false);
    var linkId = $(this).data("id");
    $("#objectId").val(linkId);
    var ownerAppElement = $("#ownerApp");
    ownerAppElement.attr("disabled", false);
    ownerAppElement.val($(this).data("appType")).attr("disabled", true);
    var submitButtonElement = $("#submitButton");
    submitButtonElement.text($.i18n._("update"));
    submitButtonElement.show($.i18n._("update"));
    $("#cancelButton").show($.i18n._("update"));
    var cancelButtonViewElement = $("#cancelButtonForView");
    cancelButtonViewElement.hide($.i18n._("update"));
    cancelButtonViewElement.attr("hidden", true);
    if ($("#showCorrective").hasClass("active")) {
      $("#capaModal")
        .find(".rxmain-container-header-label")
        .first()
        .text($.i18n._("app.corrective.action.edit"));
    } else {
      $("#capaModal")
        .find(".rxmain-container-header-label")
        .first()
        .text($.i18n._("app.preventative.action.edit"));
    }
  });

  $(document).on("click", "#cancelButton", function (e) {
    $(".modalError").hide();
  });

  $(document).on("click", ".capa-show", function (e) {
    var labelElement = $("#label");
    labelElement.val($(this).data("label"));
    labelElement.prop("readonly", true);
    var linkId = $(this).data("id");
    $("#objectId").val(linkId);
    var ownerAppElement = $("#ownerApp");
    ownerAppElement.attr("disabled", true);
    ownerAppElement.val($(this).data("appType")).trigger("change");
    var submitButtonElement = $("#submitButton");
    submitButtonElement.hide($.i18n._("update"));
    submitButtonElement.attr("hidden", true);
    var cancelButtonElement = $("#cancelButton");
    cancelButtonElement.hide($.i18n._("update"));
    cancelButtonElement.attr("hidden", true);
    $("#warningNote").hide();
    $("#cancelButtonForView").show($.i18n._("Close"));
    if ($("#showCorrective").hasClass("active")) {
      $("#capaModal")
        .find(".rxmain-container-header-label")
        .first()
        .text($.i18n._("app.corrective.action.label"));
    } else if ($("#showRootCause").hasClass("active")) {
      $("#capaModal")
        .find(".rxmain-container-header-label")
        .first()
        .text($.i18n._("app.preventative.action.label"));
    }
  });

  $("#submitButton").on("click", function (e) {
    var dataObj = {};
    dataObj.textDesc = $("#label").val();
    if (dataObj.textDesc == "" || /;|,|#|<|>|'|"/.test(dataObj.textDesc)) {
      $(".modalError").show();
      return;
    }
    dataObj.id = $("#objectId").val();
    dataObj.ownerApp = $("#ownerApp").val();
    if ($("#showCorrective").hasClass("active")) {
      dataObj.capaType = 0;
    } else if ($("#showPreventative").hasClass("active")) {
      dataObj.capaType = 1;
    }
    $.ajax({
      url: saveCAPA,
      data: dataObj,
      method: "POST",
    }).done(function (result) {
      window.location.reload();
    });
  });

  function initRodSelectionButtons() {
    var buttonHtml =
      '<div class="rxmain-container-row">' +
      '<div class="col-lg-12">' +
      '<ul class="nav nav-tabs" role="tablist" style="height: 52px;">' +
      '<li role="presentation" id="showCorrective" class="active"><a href="javascript:void(0)" class="tab-ref" aria-controls="overviewTab" role="tab" data-toggle="tab" aria-expanded="true">' +
      $.i18n._("app.corrective.action.label") +
      "</a>" +
      "</li>" +
      '<li role="presentation" id="showPreventative"><a href="javascript:void(0)" class="tab-ref" aria-controls="sectionsTab" role="tab" data-toggle="tab" aria-expanded="false">' +
      $.i18n._("app.preventative.action.label") +
      "</a>" +
      "</li>" +
      "</ul></div>" +
      "</div>";
    $("div.case-quality-datatable-toolbar")
      .css("width", "100%")
      .html(buttonHtml);
  }

  $(document).on("click", "#showCorrective", function (e) {
    $(".active").removeClass("active");
    $("#showCorrective").addClass("active");
    $(".tab-ref").attr("aria-expanded", false);
    $("#showCorrective a").attr("aria-expanded", true);
    init_mapping_table(CORRECTIVE_IDENTIFIER);
  });

  $(document).on("click", "#showPreventative", function (e) {
    $(".active").removeClass("active");
    $("#showPreventative").addClass("active");
    $(".tab-ref").attr("aria-expanded", false);
    $("#showPreventative a").attr("aria-expanded", true);
    init_mapping_table(PREVENTATIVE_IDENTIFIER);
  });
});
