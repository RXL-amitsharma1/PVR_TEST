$(function () {
  $(".pv-switch input").each((ind, item) => {
    $(item).prop("checked", $(item).attr("data-value") === "true");
  });
  var dashboardTable = $("#rxTableUserGroupDasboard").DataTable({
    //"sPaginationType": "bootstrap",
    stateSave: true,
    stateDuration: -1,
    initComplete: function () {
      $(".dt-empty").attr("colspan", "4");
    },
  });

  $("#availableDashboard").select2();
  var allSelectedDashboards = fetchAllRows(dashboardTable);

  $(document).on("click", ".removeDashboard", function (e) {
    var idToRemove = $(this).data("id");
    allSelectedDashboards = allSelectedDashboards.filter(function (ele) {
      return $(ele).val() != idToRemove;
    });
    dashboardTable.row($(this).closest("tr")).remove().draw();
  });

  $(document).on("click", ".add-dashboard", function (e) {
    var id = $("#availableDashboard").val();
    var isNew = true;
    for (var i = 0; i < allSelectedDashboards.length; i++) {
      if ($(allSelectedDashboards[i]).val() == id) {
        isNew = false;
        break;
      }
    }
    if (isNew)
      $.ajax({
        url: dashboardDetailURL + "?id=" + $("#availableDashboard").val(),
        dataType: "json",
      }).done(function (data) {
        dashboardTable.row
          .add([
            data.label +
              '<input type="hidden" name="dashboardId" value="' +
              data.id +
              '">',
            data.owner,
            data.dashboardType,
            '<div class="btn-group dropdown " align="center">\n' +
              '<a href="' +
              dashboardURL +
              "?id=" +
              data.id +
              '" class="btn btn-success btn-xs ">' +
              $.i18n._("view") +
              "</a>\n" +
              '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">\n' +
              '<span class="caret"></span>\n' +
              '<span class="sr-only">Toggle Dropdown</span>\n' +
              "</button>\n" +
              '<ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">\n' +
              '<li role="presentation"><a href="javascript:void(0)" role="menuitem" class="listMenuOptions removeDashboard"\n' +
              'data-id="' +
              data.id +
              '">' +
              $.i18n._("delete") +
              "</a></li>\n" +
              "</ul>\n" +
              "</div>",
          ])
          .draw(false);
        // push in selectedDashboards
        allSelectedDashboards.push(
          '<input type="hidden" name="dashboardId" value="' + data.id + '" />'
        );
      });
  });

  // non-visible input field are not passed in form (only visible page (not all in paginated screen), therefore done manually
  $("form").submit(function (e) {
    e.preventDefault();
    var $form = $(this);
    $form.append(allSelectedDashboards); // append in the form
    $form.get(0).submit(); // submit
  });

  if ($("#rxTableUserGroup").is(":visible")) {
    var table = $("#rxTableUserGroup")
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
        customProcessing: true, //handled using processing.dt event
        stateSave: true,
        stateDuration: -1,
        serverSide: true,
        ajax: {
          url: USERGROUP.listUrl,
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
        rowId: "userGroupUniqueId",
        aaSorting: [],
        order: [[3, "desc"]],

        aLengthMenu: [
          [50, 100, 200, 500],
          [50, 100, 200, 500],
        ],
        pagination: true,
        iDisplayLength: 50,

        drawCallback: function (settings) {
          pageDictionary(
            $("#rxTableUserGroup_wrapper")[0],
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
            mData: "fieldProfile",
            bSortable: false,
            mRender: function (data, type, row) {
              var text =
                data == null
                    ? $.i18n._("all.fields")
                  : data
                      .replace(/&/g, "&amp;")
                      .replace(/</g, "&lt;")
                      .replace(/>/g, "&gt;");
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
            sClass: "dt-center",
            bSortable: false,
            aTargets: ["id"],
            mRender: function (data, type, full) {
              var actionButton =
                '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs" href="' +
                USERGROUP.viewUrl +
                "/" +
                data["id"] +
                '">' +
                $.i18n._("view") +
                '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;"> \
                            <li role="presentation"><a role="menuitem" href="' +
                USERGROUP.editUrl +
                "/" +
                data["id"] +
                '">' +
                $.i18n._("edit") +
                '</a></li> \
                            <li role="presentation"><a role="menuitem" href="#" data-toggle="modal" \
                                data-target="#deleteModal" data-instancetype="' +
                $.i18n._("userGroup") +
                '" data-instanceid="' +
                data["id"] +
                '" data-instancename="' +
                replaceBracketsAndQuotes(data["name"]) +
                '">' +
                $.i18n._("delete") +
                "</a></li> \
                        </ul> \
                    </div>";
              return actionButton;
            },
          },
        ],
      })
      .on("draw.dt", function () {
        setTimeout(function () {
          $("#rxTableUserGroup tbody tr").each(function () {
            $(this).find("td:eq(5)").attr("nowrap", "nowrap");
            $(this).find("td:eq(6)").attr("nowrap", "nowrap");
          });
        }, 100);
        $(".comment").shorten({
          showChars: 70,
        });
        addReadMoreButton(".comment", 70);
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      });
    actionButton("#rxTableUserGroup");
    loadTableOption("#rxTableUserGroup");
  }
  $("#fieldProfile")
    .select2({
      placeholder: $.i18n._("all.fields"),
      minimumInputLength: 3,
      multiple: false,
      allowClear: true,
      ajax: {
        delay: 100,
        dataType: "json",
        url: USERGROUP.ajaxProfileSearchUrl + "/?",
        data: function (params) {
          return {
            term: params.term,
            max: params.page * 10,
          };
        },
        processResults: function (data, params) {
          var more = params.page * 10 <= data.length;
          return {
            results: data,
            pagination: {
              more: more,
            },
          };
        },
      },
      initSelection: function (element, callback) {
        var value = $(element).attr("data-value") ?? $(element).val();
        if (value) {
          if ($(element).val() !== value) {
            $(element)
                .append(new Option($("#fieldProfileVal").val() ?? value, value, true, true))
                .val(value)
                .trigger("change");
          }
          setTimeout(() => callback({ id: value, text: $("#fieldProfileVal").val() }), 0);
        } else {
          //workaround for placeholder displaying
          setTimeout(() => callback([]), 0);
        }
      },
    })
    .on("select2:select", function (eventData) {
      var selectedObj = eventData.params.data;
      $("#fieldProfileVal").val(selectedObj.text);
      if (selectedObj != null && selectedObj != undefined) {
        $("#fieldProfile").val(selectedObj.id).trigger("change").attr("data-value", selectedObj.id);
      }
    });
  bindSelect2WithUrl(
    $("#dataProtectionQuery"),
    querySearchUrl,
    queryNameUrl,
    true
  );
});

function fetchAllRows(table) {
  var allSelected = table
    .rows()
    .data()
    .toArray()
    .map(function (row) {
      var rowString = $(row).get(0);
      // parse input tags only
      rowString = rowString.substring(rowString.indexOf("<input"));
      return $(rowString);
    });
  return allSelected;
}
