ActionItemFilterEnum = {
  MY_OPEN: "My Open Action Items",
  MY_ALL: "My All Action Items",
  ALL: "All Action Items",
};

var actionItem = actionItem || {};

actionItem.actionItemList = (function () {
  //Action item table.
  var action_item_table;
  var allTableParams;
  var sessionStorageAssignedToVariableName =
    window.location.pathname.replace(/\//g, "") + ".assignedTo";
  var tableFilter = {};
  var advancedFilter = false;

  //The function for initializing the action item data tables.
  var init_action_item_table = function (url) {
    $("#assignedToFilterAIControl").val(
      sessionStorage.getItem(sessionStorageAssignedToVariableName)
        ? sessionStorage.getItem(sessionStorageAssignedToVariableName)
        : ""
    );

    //Initialize the datatable
    action_item_table = $("#actionItemList")
      .DataTable({
        layout: {
          topStart: null,
          topEnd: { search: { placeholder: $.i18n._("fieldprofile.search.label") } },
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
        headerCallback: function () {
          $("#actionItemList_length").hide();
          $(".dataTables_length").css("margin-top", "10px");
        },
        drawCallback: function (oSettings) {
          //Click event bind to the action item view option.
          $(".action-item-view").on("click", function () {
            var actionItemId = $(this).attr("data-value");
            actionItem.actionItemModal.view_action_item(actionItemId);
          });

          //Click event bind to the delete icon.
          $(".action-item-delete").on("click", function () {
            var actionItemId = $(this).attr("data-value");
            actionItem.actionItemModal.delete_action_item(
              actionItemId,
              true,
              null
            );
          });

          //Click event bind to the edit icon.
          $(".action-item-edit").on("click", function () {
            var actionItemId = $(this).attr("data-value");
            actionItem.actionItemModal.edit_action_item(
              hasAccessOnActionItem,
              actionItemId,
              true,
              null,
              null
            );
          });

          //Click event bind to the export icon.
          $(".action-item-export").on("click", function () {
            var actionItemId = $(this).attr("data-value");
            $("#excelData").val(actionItemId);
            $("#excelExport").trigger("submit");
          });

          pageDictionary(
            $("#actionItemList_wrapper")[0],
            oSettings.aLengthMenu[0][0],
            oSettings.json.recordsFiltered
          );
        },

        initComplete: function() {

                //Toggle the action buttons on the action item list.
                actionButton( '#actionItemList' );

                var $divToolbar = $('<div class="toolbarDiv col-xs-8" style="margin-bottom: 10px;max-width: 65%"></div>');
                var $rowDiv = $('<div style="margin-left: 150px"></div>');
                $divToolbar.append($rowDiv);
                $('#actionItemList_wrapper').attr("class", "row");

                $($('#actionItemList_wrapper').children()[0]).attr("class", "col-xs-4");

                $("#actionItemList_wrapper").prepend($divToolbar);

                getFilterRadios($rowDiv);
                prepareActionItemAssignedTo();

                //Bind the click event on the create action item button.
                $("#createActionItem").on('click', function() {
                    actionItem.actionItemModal.init_action_item_modal(true, null);
                });

                var actionItemModalObj = $('#actionItemModal');

                //Bind the click event on update button click
                actionItemModalObj.find('.update-action-item').on('click', function() {
                    actionItem.actionItemModal.update_action_item(true, actionItemModalObj, null);
                });

                //Bind the click event on update button click
                actionItemModalObj.find('.edit-action-item').on('click', function() {

                    //Toggle the modal window buttons.
                    actionItemModalObj.find('#excelSingleAI').addClass('hide');
                    actionItemModalObj.find('#creationScreenButton').addClass('hide');
                    actionItemModalObj.find('#editScreenButton').removeClass('hide');
                    actionItemModalObj.find('#viewScreenButton').addClass('hide');

                    //Enable all the elements.
                    actionItem.actionItemModal.toggle_element_disable(actionItemModalObj, false);

                    //Enable the datepicker
                    actionItemModalObj.find("#dueDate").siblings("div.input-group-btn").find(".btn.dropdown-toggle").removeAttr('disabled');
                    actionItemModalObj.find("#completionDate").siblings("div.input-group-btn").find(".btn.dropdown-toggle").removeAttr('disabled');

                    //Temporarily saved dueDateValue and completionDateValue
                    var dueDateValue = actionItemModalObj.find("#dueDate").val();
                    var completionDateValue = actionItemModalObj.find("#completionDate").val();

                    initializePastDatesNotAllowedDatePicker();

                    //Re-assign original values
                    dueDateValue ? actionItemModalObj.find("#dueDateDiv").datepicker('setDate', dueDateValue) : actionItemModalObj.find("#dueDate").val('');
                    completionDateValue ? actionItemModalObj.find("#completionDateDiv").datepicker('setDate', completionDateValue) : actionItemModalObj.find("#completionDate").val('');

                });
        },
        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        ajax: {
          url: actionItemUrl,
          type: "POST",
          dataSrc: "data",
          data: function (d) {
            d.searchString = d.search.value;
            if (d.order.length > 0) {
              d.direction = d.order[0].dir;
              //Column header mData value extracting
              d.sort = d.columns[d.order[0].column].data;
              d.tableFilter = JSON.stringify(tableFilter);
              d.advancedFilter = advancedFilter;
              d.sharedwith = $("#assignedToFilterAIControl").val();
            }
            if ($('input[name="relatedReports"]').length > 0) {
              d.filterType = $('input[name="relatedReports"]:checked').val();
            }
            var type = $("#newTabContent .active").attr("value");
            if (type) {
              d.filterType = type;
            }

            allTableParams = d;
          },
        },

        aaSorting: [[4, "desc"]],
        bLengthChange: true,
        aLengthMenu: [
          [10, 25, 50, 100],
          [10, 25, 50, 100],
        ],
        pagination: true,
        iDisplayLength: 10,

        // drawCallback: function (settings) {
        //     showTotalPage(settings.json.recordsFiltered);
        //     pageDictionary($('#actionItemList_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
        // },
        aoColumns: [
          {
            mData: "actionItemId",
            visible: false,
            mRender: function (data, type, row) {
              return '<span id="actionItemId">' + row.actionItemId + "</span>";
            },
          },
          {
            mData: "actionCategory",
            mRender: function (data, type, row) {
              return "<span>" + row.actionItemCategory + "</span>";
            },
          },
          {
            mData: "assignedTo",
            bSortable: false,
            mRender: $.fn.dataTable.render.text(),
          },
          {
            mData: "description",
            mRender: function (data, type, row) {
              return (
                "<div class='three-row-dot-overflow'>" +
                encodeToHTML(row.description) +
                "</div>"
              );
            },
          },
          {
            mData: "dueDate",
            aTargets: ["dueDate"],
            sClass: "dataTableColumnCenter",
            mRender: function (data, type, full) {
              return data
                ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT)
                : "";
            },
          },
          {
            mData: "completionDate",
            aTargets: ["completionDate"],
            sClass: "dataTableColumnCenter",
            mRender: function (data, type, full) {
              return data
                ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT)
                : "";
            },
          },
          {
            mData: "priority",
            mRender: function (data, type, full) {
              return data ? $.i18n._("priority." + data) : "";
            }
          },
          {
            mData: "status",
            aTargets: ["status"],
            mRender: function (data, type, full) {
              return data ? $.i18n._("status_enum." + data) : "";
            },
          },
          {
            mData: "appType",
            mRender: function (data, type, row) {
              if (row.appType) {
                return "<span>" + row.appType + "</span>";
              } else {
                return '<span style="margin-left:30%">-</span>';
              }
            },
          },
          {
            mData: null,
            bSortable: false,
            mRender: function (data, type, row) {
              var actionButton =
                '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                            <a class="btn btn-success btn-xs btn-left-round action-item-view" href="#" data-value="' +
                row.actionItemId +
                '">' +
                $.i18n._("view") +
                '</a> \
                            <button type="button" class="btn btn-default btn-xs btn-right-round dropdown-toggle" data-toggle="dropdown"> \
                                <span class="caret"></span> \
                                <span class="sr-only">Toggle Dropdown</span> \
                            </button> \
                            <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                                <li role="presentation"><a class="action-item-edit" role="menuitem" href="#" data-value="' +
                row.actionItemId +
                '">' +
                $.i18n._("edit") +
                '</a></li>\
                                <li role="presentation"><a class="action-item-export" role="menuitem" href="#" data-value="' +
                row.actionItemId +
                '">' +
                $.i18n._("app.action.item.export") +
                '</a></li>\
                                <li role="presentation"><a class="action-item-delete" role="menuitem" href="#" data-value="' +
                row.actionItemId +
                '">' +
                $.i18n._("delete") +
                "</a></li>\
                            </ul></div>";
              return actionButton;
            },
          },
        ],
      })
      .on("draw.dt", function () {
        updateTitleForThreeRowDotElements();
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });
    loadTableOption("#actionItemList");
    init_table_filter();

    return action_item_table;
  };

  $(document).on("click", "#excelAllAI", function () {
    var form = $("#excelExportForm");
    form.find("input").detach();
    for (var x in allTableParams) {
      form.append(
        "<input type='hidden' name='" +
          x +
          "' value='" +
          allTableParams[x] +
          "'>"
      );
    }
    form.submit();
  });

  //Method to get the filter radios.
  var getFilterRadios = function (toolbar) {
    var radios = '<div class="col-xs-12" id="newTabContent">';
    radios =
      radios +
      '<a class="add-cursor active" value="' +
      ActionItemFilterEnum.MY_OPEN +
      '" data-evt-clk=\'{\"method\": \"checkFilterType\", \"params\": []}\' > ' +
      $.i18n._("my.open.action.items") +
      "</a>";
    radios =
      radios +
      '<a class="add-cursor" value="' +
      ActionItemFilterEnum.MY_ALL +
      '" data-evt-clk=\'{\"method\": \"checkFilterType\", \"params\": []}\'> ' +
      $.i18n._("my.all.action.items") +
      "</a>";
    radios =
      radios +
      '<a class="add-cursor" value="' +
      ActionItemFilterEnum.ALL +
      '" data-evt-clk=\'{\"method\": \"checkFilterType\", \"params\": []}\'> ' +
      $.i18n._("all.action.items") +
      "</a>";
    radios = $(radios);
    $(toolbar).append(radios);
    initEvtClk();
  };

  let initEvtClk = (function () {
    $("[data-evt-clk]").on('click', function(e) {
      e.preventDefault();
      const eventData = JSON.parse($(this).attr("data-evt-clk"));
      const methodName = eventData.method;
      const params = eventData.params;

      if(methodName == 'checkFilterType') {
        var elem = $(this);
        checkFilterType(elem);
      }
    });
  });

  var prepareActionItemAssignedTo = function () {
    var assignedToFilterDiv =
      '<select class="assignedToFilterAIControl form-control"  id="assignedToFilterAIControl" placeholder="' +
      $.i18n._("app.advancedFilter.action.item.assigned.to") +
      '" name="assignedToFilterAIControl" data-value="" style="text-align:left;min-width: 200px;margin-right: 5px !important;"></select>';
    assignedToFilterDiv = $(assignedToFilterDiv);
    $('#actionItemList_wrapper').find('.dt-search').before(assignedToFilterDiv);
    var sharedWith = $(".assignedToFilterAIControl");
    bindShareWith(
      sharedWith,
      sharedWithListUrl,
      sharedWithValuesUrl,
      "40%",
      true,
      $('body'),
      "app.advancedFilter.action.item.assigned.to"
    ).on("change", function (e) {
      sessionStorage.setItem(
        sessionStorageAssignedToVariableName,
        sharedWith.val()
      );
      action_item_table.draw();
    });
  };

  var init_table_filter = function () {
    var filter_data = [
      {
        label: $.i18n._("app.advancedFilter.action.item.action.category"),
        type: "select2-id",
        name: "actionCategory",
        ajax: {
          url: actionItemCategoryListUrl,
          data_handler: function (data) {
            return pvr.filter_util.build_options(data, "key", "value", true);
          },
          error_handler: function (data) {
            console.log(data);
          },
        },
      },
      {
        label: $.i18n._("app.advancedFilter.action.item.description"),
        type: "text",
        name: "description",
        maxlength: 4000,
      },
      {
        label: $.i18n._("app.advancedFilter.action.item.due.date.start"),
        type: "date-range",
        group: "dueDate",
        group_order: 1,
      },
      {
        label: $.i18n._("app.advancedFilter.action.item.due.date.end"),
        type: "date-range",
        group: "dueDate",
        group_order: 2,
      },

      {
        label: $.i18n._("app.advancedFilter.action.item.completion.date.start"),
        type: "date-range",
        group: "completionDate",
        group_order: 1,
      },
      {
        label: $.i18n._("app.advancedFilter.action.item.completion.date.end"),
        type: "date-range",
        group: "completionDate",
        group_order: 2,
      },
      {
        label: $.i18n._("app.advancedFilter.action.item.priority"),
        type: "select2",
        name: "priority",
        ajax: {
          url: priorityUrl,
          data_handler: function (data) {
            return pvr.filter_util.build_options(data, "key", "name", true);
          },
          error_handler: function (data) {
            console.log(data);
          },
        },
      },
      {
        label: $.i18n._("app.advancedFilter.status"),
        type: "select2-enum",
        name: "status",
        data_type: "StatusEnum",
        data: statusTypes,
      },
      {
        label: $.i18n._("app.advancedFilter.application"),
        type: "select2-enum",
        name: "appType",
        data_type: "AppTypeEnum",
        data: appType,
      },
    ];

    pvr.filter_util.construct_right_filter_panel({
      table_id: "#actionItemList",
      container_id: "config-filter-panel",
      filter_defs: filter_data,
      column_count: 1,
      panel_width: 420,
      done_func: function (filter) {
        tableFilter = filter;
        advancedFilter = true;
        var dataTable = $("#actionItemList").DataTable();
        dataTable.ajax.reload(function (data) {}, false).draw();
      },
    });
  };

  //Funtion to refresh the table.
  var reload_action_item_table = function () {
    action_item_table.ajax.reload();
  };

  return {
    init_action_item_table: init_action_item_table,
    reload_action_item_table: reload_action_item_table,
  };
})();

function checkFilterType(elem) {
  $("#newTabContent a").removeClass("active");
  $(elem).addClass("active");
  $("#actionItemList")
    .DataTable()
    .order([[4, "desc"]])
    .draw();
}
