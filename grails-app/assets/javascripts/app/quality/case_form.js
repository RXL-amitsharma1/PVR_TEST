var optionsremovecoldatacol = [];
var selectAll = false;
var selectAll2 = false;
var selectedIds = [];
var selectedIds2 = [];
var table;
var table2;
var tableFilter = {};
var advancedFilter = false;
var totalFilteredRecord;
var selectedCases = [];
var issueTypesArray = [];
var rootCasuesArray = [];
var responsiblePartiesArray = [];
var errorTypesArray = [];
var fieldNamesArray = [];
var fieldLocationsArray = [];
var firstTime = true;
var ifAttatchemntsLoaded = 0;
var pageDataTable;
$(function () {
  addReadMoreButton(".fieldLevel", 200);
  $( ".resizable" ).resizable({
        stop: function (event, context) {
            var element = context.element;
            if (element && element.hasClass('issueListContentDiv')) {
                element.find('.dt-layout-table').find('.dt-scroll-body').scrollLeft(0);
                scrollToResizableElementBottom(element);
            }
        }
  });
  $(document).on('resize', '.issueListContentDiv', function () {
      var contentBodyHeight = $(this).height() - $(this).find('.dt-scroll-head').height();
      var scrollBody = $(this).find('.dt-scroll-body');
      scrollBody.css('max-height', contentBodyHeight + 'px');
      scrollBody.css('height', contentBodyHeight + 'px');
      scrollBody.height(contentBodyHeight);
      if (scrollBody.scrollTop() > 0) {
          scrollBody.scrollTop(0);
      }
  });
  populateIssueTypeList();
  populateRootCauseList();
  populateResponsiblePartyList();
  populatePriorityList();
  $(document).on("mousedown", '.caseContentTable .ui-resizable-handle', function (e) {
        $("#ifremeHover").show();
    });
    $(document).on("mouseup", function (e) {
        $("#ifremeHover").hide();
    });
    $(document).on("click", "#Excel, #allExcel", function (e) {
    exportToExcelCaseForm($(this).attr("id"));
  });

  $(document).on("click", "#closeSuccessButton", function (e) {
    $("#successNotification").parent().hide();
  });

  $(document).on(
    "classAdded",
    "#Excel, #allExcel, #caseFilter, #caseDelete",
    function (e) {
      $("#caseFilter").addClass("rxmain-dropdown-settings");
      if ($(this).hasClass("fa-caret-right")) {
        $(this).removeClass("fa-caret-right");
      } else if ($(this).hasClass("fa-caret-down")) {
        $(this).removeClass("fa-caret-down");
      }
    }
  );

  $(document).on("savedAI", ".creationButton", function () {
    table.ajax.reload();
  });

  $(document).on("updatedAI", ".update-action-item", function () {
    table.ajax.reload();
  });

  $(".saveReasonsOfDelay").on("click", function () {
    reloadQualityIssues();
  });

  $(".allQualityIssuesList").one("click", function () {
    table2.ajax.reload();
  });

  var attachmentSize = document
    .getElementById("files")
    .getAttribute("data-attachment-size");
  if (attachmentSize > 0) {
    $("#files").removeAttr("disabled");
    $(".showDocument").removeAttr("disabled");
      showHideAttachment(true, true);
  } else {
      showHideAttachment(false, true);
  }

  function populateIssueTypeList() {
    var issueJson = JSON.parse(issuesList);
    for (var j = 0; j < issueJson.length; j++) {
      var d = {};
      d["key"] = $.trim(issueJson[j].id);
      d["value"] = $.trim(issueJson[j].textDesc);
      issueTypesArray.push(d);
    }
  }

  function populateRootCauseList() {
    var rootCauseJson = JSON.parse(rootCauseList);
    for (var j = 0; j < rootCauseJson.length; j++) {
      var d = {};
      d["key"] = $.trim(rootCauseJson[j].textDesc);
      d["value"] = $.trim(rootCauseJson[j].textDesc);
      rootCasuesArray.push(d);
    }
  }

  function populateResponsiblePartyList() {
    var responsiblePartyJson = JSON.parse(responsiblePartyList);
    for (var j = 0; j < responsiblePartyJson.length; j++) {
      var d = {};
      d["key"] = $.trim(responsiblePartyJson[j].textDesc);
      d["value"] = $.trim(responsiblePartyJson[j].textDesc);
      responsiblePartiesArray.push(d);
    }
  }

  function populateAll(issuesList) {
    var uniqueErrorTypes = [];
    var uniqueFieldNames = [];
    var uniqueFieldLocations = [];
    for (var j = 0; j < issuesList.length; j++) {
      if (
        issuesList[j].errorType &&
        !uniqueErrorTypes[$.trim(issuesList[j].errorType)]
      ) {
        var errorType = {};
        errorType["key"] = $.trim(issuesList[j].errorType);
        errorType["value"] = $.trim(issuesList[j].errorType);
        errorTypesArray.push(errorType);
        uniqueErrorTypes[$.trim(issuesList[j].errorType)] = 1;
      }
      if (
        issuesList[j].fieldName &&
        !uniqueFieldNames[$.trim(issuesList[j].fieldName)]
      ) {
        var fieldName = {};
        fieldName["key"] = $.trim(issuesList[j].fieldName);
        fieldName["value"] = $.trim(issuesList[j].fieldName);
        fieldNamesArray.push(fieldName);
        uniqueFieldNames[$.trim(issuesList[j].fieldName)] = 1;
      }
      if (
        issuesList[j].fieldLocation &&
        !uniqueFieldLocations[$.trim(issuesList[j].fieldLocation)]
      ) {
        var fieldLocation = {};
        fieldLocation["key"] = $.trim(issuesList[j].fieldLocation);
        fieldLocation["value"] = $.trim(issuesList[j].fieldLocation);
        fieldLocationsArray.push(fieldLocation);
        uniqueFieldLocations[$.trim(issuesList[j].fieldLocation)] = 1;
      }
    }
  }

  function populatePriorityList() {
    $.ajax({
      url: qualityPriorityTagsUrl,
      dataType: "json",
    })
      .done(function (data) {
        var issuePrioritySelect = $("#issuePriority");
        var prioritySelectElements = "";
        for (var optionIter in data) {
          prioritySelectElements =
            prioritySelectElements +
            "<option value='" +
            data[optionIter] +
            "'>" +
            data[optionIter] +
            "</option>";
        }
        issuePrioritySelect.html(prioritySelectElements);
      })
      .fail(function (data) {
        console.log("Error fetching priority tags", data);
      });
  }

  function closeDropDownOnScroll() {
    $(".dt-scroll-body").on("scroll", function () {
      $(".pv-grp-btn .dropdown-menu").hide();
    });
  }

  function showHideAttachment(show, init) {
    const $attachmentsResizable = $('.attachmentContent').closest('.resizable');

    if (show) {
        $attachmentsResizable.show();
    } else {
        $attachmentsResizable.hide();
    }

      if (init) {
          const vw = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
          $attachmentsResizable.height(300);
          $attachmentsResizable.width(Math.round(vw/2));
      }
      $attachmentsResizable.trigger('resize');
  }

  $(document).on(
    "click",
    ".issueListDiv:not('.qualityIssuesList')",
    function () {
      const $caseListElement = $(this).closest('.pv-caselist');
      hideShowContent(this);
      setIssueListDivTableHeight($caseListElement);
      scrollToResizableElementBottom($caseListElement.find('.issueListContentDiv'));
    }
  );

  $(document).on("click", ".issueListDiv.qualityIssuesList", function () {
    const $caseListElement = $(this).closest('.pv-caselist');
    hideShowContent(this);
    table.columns.adjust();
    setIssueListDivTableHeight($caseListElement);
    scrollToResizableElementBottom($caseListElement.find('.issueListContentDiv'));
  });

  function scrollToResizableElementBottom($resizableElement) {
      if ($resizableElement.length === 0) {
          return;
      }
      const vh = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
      if (($resizableElement.offset().top + $resizableElement.outerHeight()) > vh && ($resizableElement.offset().top - 120 > 0)) {
          $(document).scrollTop($(document).scrollTop() + $resizableElement.outerHeight() + 10);
      }
  }

  function setIssueListDivTableHeight($container) {
      const $issueListContentDiv = $container.find('.issueListContentDiv');
      if (!$issueListContentDiv.is(':visible')) {
          return;
      }
      const $tableScrollHead = $issueListContentDiv.find('.dt-scroll-head');
      const $issuesTable = $issueListContentDiv.find('table[id=issuesTable]');
      const $allIssuesTable = $issueListContentDiv.find('table[id=allIssuesTable]');
      const $dataTable = $issuesTable.length > 0 ? $issuesTable : $allIssuesTable;
      const initialBodyHeight = 250;
      let initialListDivHeight;
      if ($dataTable.height() > initialBodyHeight) {
          initialListDivHeight = ($tableScrollHead.height() ?? 0) + initialBodyHeight;
      } else {
          initialListDivHeight = ($tableScrollHead.height() ?? 0) + $dataTable.height();
      }

      const platformInfo = window.navigator?.userAgentData?.platform || window.navigator.userAgent;
      //specific additionalHeight for OS Windows with horizontal scroll displayed
      const additionalHeight = !_.isEmpty(platformInfo) && platformInfo.indexOf('Win') !== -1 ? 35 : 10;
      $issueListContentDiv.height(initialListDivHeight + additionalHeight);
      $issueListContentDiv.trigger('resize');
  }

    var caseContentResizable = $('.caseContent').closest('.resizable');
    //resizable border compensation
    caseContentResizable.css({'height': (caseContentResizable.height() + 10) + 'px'});
  $(document).on("click", ".showDocument", function () {
    var file = $("#files");
    var versionNum = file.find(":selected").attr("data-version");
    var isRedacted = file.find(":selected").attr("data-isRedacted");
    if (!versionNum || versionNum == "null") {
      versionNum = versionNumber;
    }
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
        versionNum +
        "&isRedacted=" +
        isRedacted
    );
  });

  $(document).on("click", ".fieldLabel", function () {
    $("#fieldValueLabel").val($(this).attr("data-value"));
    $("#fieldValue").val($(this).attr("data-value"));
    $("#fieldName").val($(this).attr("data-name"));
    $("#fieldNameLabel").val($(this).attr("data-name"));
    $("#fieldLocation").val($(this).attr("data-location"));
    $("#fieldLocationLabel").val($(this).attr("data-location"));
    $("#entryType").val($("#entryType").attr("data-default"));
    $("#issuePriority").val($(this).attr("data-priority"));
    $("#errorTypeTextArea").val("");
    $("#errorId").val("");
    $("#dataType").attr("disabled", false);
    $("#errorCommentSelect").attr("disabled", false);
    $("#errorTypeTextArea").attr("readonly", false);
    $("#errorTypeTextArea").trigger("change");
    if ($(this).attr("data-isReviewable") == "Y") {
      $("#qualityIssueModal").modal("show");
    }
  });
  $(document).on("click", ".editError", function (evt) {
    evt.preventDefault();
    var thiz = $(this);
    $("#qualityIssueModal").modal("show");
    $("#fieldValueLabel").val(thiz.attr("data-value"));
    $("#fieldValue").val(thiz.attr("data-value"));
    $("#fieldName").val(thiz.attr("data-name"));
    $("#fieldNameLabel").val(thiz.attr("data-name"));
    $("#fieldLocation").val(thiz.attr("data-location"));
    $("#fieldLocationLabel").val(thiz.attr("data-location"));
    $("#mandatoryType").val(thiz.attr("data-mandatoryType"));
    $("#dataType").val(thiz.attr("data-dataType"));
    $("#dataType").attr("disabled", true);
    $("#qualityIssueType").val(thiz.attr("data-qualityIssueTypeId"));
    $("#qualityIssueTypeId").val("" + thiz.attr("data-qualityIssueTypeId"));
    $("#errorTypeTextArea").val(thiz.attr("data-errorType"));
    $("#entryType").val(thiz.attr("data-entryType"));
    $("#errorId").val(thiz.attr("data-id"));
    $("#issuePriority").val(thiz.attr("data-priority"));
    if ($("#entryType").val() == "A") {
      $("#errorCommentSelect").attr("disabled", true);
      $("#errorTypeTextArea").attr("readonly", true);
    } else {
      $("#errorCommentSelect").attr("disabled", false);
      $("#errorTypeTextArea").attr("readonly", false);
    }
    $("#errorTypeTextArea").trigger("change");
  });
  $(document).on("change", "#errorCommentSelect", function () {
    $("#errorTypeTextArea").val($("#errorCommentSelect").val());
    $("#errorTypeTextArea").trigger("change");
  });

    initCaseFormAssignedTo();

  $(document).on("keyup change", "#errorTypeTextArea", function () {
    if ($("#errorTypeTextArea").val() != "") {
      $(".qualityIssueSave").attr("disabled", false);
    } else {
      $(".qualityIssueSave").attr("disabled", true);
    }
  });

    table = $("#issuesTable").DataTable({

        //"sPaginationType": "bootstrap",
        initComplete: function (settings) {
            $("label[data-value='']").toggle();
            initComment();
            initActionItems();
            initExpandHandler(table);
            initCreateIssue();
            initViewCriteria();
            initViewCriteriaForManualError();
            closeDropDownOnScroll();
            initReasonOfDelay();
            reloadQualityIssues();
            if (!firstTime) {
                init_filter();
            }
        },
        scrollX: true,
        scrollY: '100px',
        drawCallback: function (settings) {
            if (selectedIds) {
                for (var i = 0; i < selectedIds.length; i++) {
                    $(".selectCheckbox[_id=" + selectedIds[i] + "]").attr(
                        "checked",
                        true
                    );
                }
            }
            updateTitleForThreeRowDotElements();
        },
        headerCallback: function (thead) {
            // Update header when table is rendered
            $(thead).find('.commentIssueHeader').html(
                '<div><i class="fa fa-comment-o" title="' + $.i18n._("lateProcessing.comments.label") + '"></i></div>' +
                '<div class="pv-stacked-row"><i class="fa fa-ticket" title="' + $.i18n._("qualityModule.issue.label") + '"></i></div>'
            );
        },
        ajax: {
            url: issueListUrl,
            type: "POST",
            dataSrc: function (res) {
                populateAll(res);
                return res;
            },
            data: function (data) {
                data.tableFilter = JSON.stringify(tableFilter);
                data.advancedFilter = advancedFilter;
                prepareIssueListSortData(data)
            },
            beforeSend: function () {
                $("#totalQualityIssuesSpinner").show();
            },
            complete: function (responseData) {
                initExistingErrors(responseData["responseJSON"] || []);
                $("#totalQualityIssuesSpinner").hide();
                if (firstTime) {
                    init_filter();
                }
                firstTime = false;
            },
        },
        customProcessing: true, //handled using processing.dt event
        serverSide: true,
        deferLoading: 0,
        searching: false,
        paging: false,
        info: false,
        order: [1, "asc"],
        columns: [
            {
                data: null,
                name: "select",
                bSortable: false,
                width: "25px",
                mRender: function (data, type, row, meta) {
                    var strPrm = "<div class='checkbox checkbox-primary' style='margin-top: -20px;'>";
                    strPrm = strPrm + "<input type='checkbox' class='selectCheckbox' style='margin-left: 10px;' name='selected" + meta.row + "' _caseNumber='" + data.caseNumber + "' _caseVersion='" + data.caseVersion + "' _errorType='" + data.errorType + "' _id='" + data.id + "' ";
                    if (findInArray(selectedIds, data.id) || selectAll == true) {
                        strPrm += "checked=true ";
                    }
                    strPrm = strPrm + "/>";
                    strPrm = strPrm + "<label class= 'selected' for='selected" + meta.row + "'></label>"
                    strPrm = strPrm + "</div>";
                    return strPrm;
                },
            },
            {
                mData: "qualityMonitoringType",
                sClass: "text-center"
            },
            {

                mData: "errorType",
                sClass: "text-center",
                mRender: function (data, type, row) {
                    return (
                        '<div class="three-row-dot-overflow"><a href="#" class="editError"' +
                        'data-errorType="' +
                        row.errorType +
                        '"' +
                        'data-mandatoryType="' +
                        row.typeCode +
                        '"' +
                        'data-dataType="' +
                        row.dataType +
                        '"' +
                        'data-name="' +
                        row.fieldName +
                        '"' +
                        'data-value="' +
                        row.value +
                        '"' +
                        'data-location="' +
                        row.fieldLocation +
                        '"' +
                        'data-entryType="' +
                        row.entryType +
                        '"' +
                        'data-qualityIssueType="' +
                        row.qualityIssueType +
                        '"' +
                        'data-qualityIssueTypeId="' +
                        row.qualityIssueTypeId +
                        '"' +
                        'data-id="' +
                        row.id +
                        '"' +
                        'data-priority="' +
                        row.priority +
                        '"' +
                        '>' +
                        encodeToHTML(data) +
                        '</a></div>');
                }
            }, {
                mData: "fieldName",
                sClass: "text-center",
                mRender: function (data, type, row) {
                    var content = (data == null) ? '' : encodeToHTML(data);
                    return "<div class='three-row-dot-overflow'>" + content + "</div>";
                }
            },
            {
                mData: "value",
                sClass: "text-center",
                mRender: function (data, type, row) {
                    var content = (data == null) ? '' : encodeToHTML(data);
                    return "<div class='three-row-dot-overflow'>" + content + "</div>";
                }
            }, {
                mData: "fieldLocation",
                sClass: "text-center",
                mRender: function (data, type, row) {
                    var content = (data == null) ? '' : encodeToHTML(data);
                    return "<div class='three-row-dot-overflow'>" + content + "</div>";
                }
            }, {

                mData: "entryType",
                sClass: "text-center"
            }
            , {
                visible:false,
                mData: "dueIn",
                sClass: "dataTableColumnCenter",
                mRender: function (data, type, row) {
                    return moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT);
                }
            }, {
                mData: "priority",
                sClass: "dataTableColumnCenter",
                sWidth: "100px",
                mRender: priorityRender
            },
            {
                mData: "issueTypeDetail",
                bSortable: false,
                sClass: "text-center",
                sWidth: "100px",
                mRender: function (data, type, row) {
                    var result = [];
                    result.push(row.issueTypeDetail.qualityIssueType);
                    result.push(row.issueTypeDetail.rootCause);
                    result.push(row.issueTypeDetail.responsibleParty);
                    return "<div style='display: inline-flex;'><div class='one-row-dot-overflow'>" + result.join("</div></div><div class='pv-stacked-row'><div class='one-row-dot-overflow'>") + "</div></div>";
                }
            }, {
                mData: 'actionItemStatus',
                sWidth: "60px",
                sClass: "dataTableColumnCenter",
                bSortable: true,
                mRender: actionItemStatusRender
            }, {
                "data": 'comm',
                width: '50px',
                "sClass": "text-center",
                "bSortable": false,
                "visible": true,
                "mRender": function (data, type, row) {
                    issWidth = "style=\"width: 34px;margin-top: 2px\"";
                    var issue = $.i18n._('no');
                    if (row['hasIssues'] === 'true') {
                        issue = $.i18n._('yes')
                        issWidth = ""
                    }

                    issue = '<a href="#" id="createIssue" class="btn lbl-badge-default btn-xs" style="margin-top: 2px"  data-id="' + row.id + '" >' + issue + '</a>';

                    const commentContentPrefix = '<a href="#" data-content="' + escapeHTML(row.comment) + '" data-caseNumber="' + row.caseNumber + '" data-errorType="' + row.errorType + '" data-id="' + row.id + '" data-viewmode="' + row.isFinalState + '" class="addComment  btn lbl-badge-default btn-xs">';
                    let commentContentPostfix = '<i class=" fa fa-comment-o" data-content="' + escapeHTML(row.comment) + '" data-placement="left"></i></a>';
                    if (row.comment !== null) {
                        commentContentPostfix = '<i class=" fa fa-commenting-o commentPopoverMessage " data-content="' + escapeHTML(row.comment) + '" data-placement="left" style="" data-original-title="' + $.i18n._('app.caseList.comment') + '"></i></a>';
                    }

                    return '<div>' + commentContentPrefix + commentContentPostfix + '</div><div class=\'pv-stacked-row\'>' + issue + '</i></div>';
                }
            }, {
                mData: null,
                sWidth: "60px",
                bSortable: false,
                sClass: "pv-col-md",
                mRender: buttonGroupRender,
            },
        ],
    }).on('draw.dt', function () {
        initComment();
    });
    pageDataTable = table;
    loadTableOption("#issuesTable");
    removeOptionColumn();

  table2 = $("#allIssuesTable").DataTable({
    //"sPaginationType": "bootstrap",
    ajax: {
      url: allIssueListUrl,
      type: "POST",
      dataSrc: "",
        "data": function (data) {
            prepareIssueListSortData(data)
        },
      complete: function () {},
    },
    customProcessing: true, //handled using processing.dt event
    serverSide: true,
    deferLoading: 0,
    searching: false,
    paging: false,
    info: false,
    order: [ 1, "asc" ],
    scrollX: true,
    scrollY: "calc(25vh)",
    drawCallback: function (settings) {
        updateTitleForThreeRowDotElements();
    },
    columns: [
      {
        mData: "",
        bSortable: false,
        sClass: "text-center",
        mRender: function (data, type, row, meta) {
          var strPrm = "<div class='checkbox checkbox-primary'>";
          strPrm =
            strPrm +
            "<input type='checkbox' class='selectCheckbox2' style='margin-left: -18px;'  name='selected" +
            meta.row +
            "' _id='" +
            row.id +
            "' _dataType='" +
            row.dataType +
            "' ";

          if (
            findInArray(selectedIds2, row.id + ":" + row.dataType) ||
            selectAll2 === true
          ) {
            strPrm += "checked=true ";
          }

          strPrm = strPrm + "/>";
          strPrm =
            strPrm +
            "<label class= 'selected' for='selected" +
            meta.row +
            "'></label>";
          strPrm = strPrm + "</div>";
          return strPrm;
        },
      },
      {
        mData: "qualityMonitoringType",
          sClass: "text-center",
        mRender: function (data, type, row) {
          var content = (data == null) ? '' : encodeToHTML(data);
          return "<div class='three-row-dot-overflow'>" + content + "</div>";
        }
      },
      {
        mData: "errorType",
          sClass: "text-center",
        mRender: function (data, type, row) {
            var content = (data == null) ? '' : encodeToHTML(data);
            return "<div class='three-row-dot-overflow'>" + content + "</div>";
        },
      },
      {
        mData: "fieldName",
          sClass: "text-center",
        mRender: function (data, type, row) {
          var content = (data == null) ? '' : encodeToHTML(data);
          return "<div class='three-row-dot-overflow'>" + content + "</div>";
        }
      },
      {
        mData: "value",
          sClass: "text-center",
        mRender: function (data, type, row) {
            var content = (data == null) ? '' : encodeToHTML(data);
            return "<div class='three-row-dot-overflow'>" + content + "</div>";
        },
      },
      {
        mData: "fieldLocation",
          sClass: "text-center",
        mRender: function (data, type, row) {
          var content = (data == null) ? '' : encodeToHTML(data);
          return "<div class='three-row-dot-overflow'>" + content + "</div>";
        }
      },
      {
        mData: "entryType",
          sClass: "text-center"
      },
      {
        visible: false,
        mData: "dueIn",
        sClass: "dataTableColumnCenter",
        mRender: function (data, type, row) {
          return moment
            .utc(data)
            .tz(userTimeZone)
            .format(DEFAULT_DATE_DISPLAY_FORMAT);
        },
      },
      {
        mData: "priority",
        sClass: "dataTableColumnCenter",
        sWidth: "100px",
        mRender: function (data, type, row) {
          return encodeToHTML(data.replace("-1", " "));
        },
      },
      {
        mData: "issueTypeDetail",
        sClass: "text-center",
        sWidth: "100px",
        mRender: function (data, type, row) {
          var result = [];
          result.push(row.issueTypeDetail.qualityIssueType);
          result.push(row.issueTypeDetail.rootCause);
          result.push(row.issueTypeDetail.responsibleParty);
          return "<div style='display: inline-flex;'><div class='one-row-dot-overflow'>" + result.join("</div></div><div class='pv-stacked-row'><div class='one-row-dot-overflow'>") + "</div></div>";
        },
      },
    ],
  }).one('draw.dt', function () {
      setIssueListDivTableHeight($('#allIssuesTable_wrapper').closest('.rxmain-container'));
  });

  loadTableOption("#allIssuesTable");
  $("#tableColumns2").find("tbody tr:first").remove();

  $(document).on("click", "#selectAll2", function (evt) {
    if ($(this).is(":checked")) {
      selectAll2 = true;
      $(".selectCheckbox2").prop("checked", true).trigger("change");
    } else {
      selectAll2 = false;
      $(".selectCheckbox2").prop("checked", false).trigger("change");
    }
  });

  table2.on("change", "input.selectCheckbox2", function () {
    var isChecked = $(this).prop("checked");
    var id = $(this).attr("_id");
    var dataType = $(this).attr("_dataType");
    var idType = id + ":" + dataType;
    var idx = findIdx(selectedIds2, idType);
    if (isChecked) {
      if (idx < 0) {
        if (idType) {
          selectedIds2.push(idType);
        }
      }
    } else {
      if (idx >= 0) {
        selectedIds2.splice(idx, 1);
      }
      if (selectAll2 === true) {
        $("#selectAll2").prop("checked", false);
        selectAll2 = false;
      }
    }
    $(this).trigger('tableSelectedRowsCountChanged', [(selectedIds2 ? selectedIds2.length : 0)]);
  });

  function prepareIssueListSortData(data) {
        if (data.order.length > 0) {
            data.direction = data.order[0].dir;
            data.sort = data.columns[data.order[0].column].data;
            if (data.sort === 'actionItemStatus') {
                data.sortMap = {}
                Object.keys(ACTION_ITEM_GROUP_STATE_ENUM).forEach(function(key) {
                    var state = ACTION_ITEM_GROUP_STATE_ENUM[key];
                    data.sortMap[state] = $.i18n._('app.actionItemGroupState.' + state);
                });
            }
        }
    }function findIdx(arItems, searchValue) {
    var found = false;
    var return_idx = -1;
    for (var i = 0; i < arItems.length && !found; i++) {
      if (arItems[i] === searchValue) {
        found = true;
      }
    }
    if (found) {
      return_idx = i - 1;
    } else {
      return_idx = -1;
    }
    return return_idx;
  }

  function findInArray(arItems, searchValue) {
    var found = false;
    for (var i = 0; i < arItems.length && !found; i++) {
      if (arItems[i] === searchValue) {
        found = true;
      }
    }
    return found;
  }

  function exportToExcelCaseForm(excelId) {
    var data = createParametersExcelEmailCaseForm(excelId);
    if (
      (excelId === "Excel" && selectedIds.length === 0) ||
      (excelId === "allExcel" && selectedIds2.length === 0)
    ) {
      $("#errorExportModal")
        .find(".messageBody")
        .html($.i18n._("app.quality.empty.list.selected"));
      $("#errorExportModal").modal("show");
      return false;
    }
    $("#excelData").val(JSON.stringify(data));
    $("#excelExport").trigger("submit");
  }

  function createParametersExcelEmailCaseForm(excelId) {
    var data = {};
    if (excelId === "allExcel") {
      data["table"] = "allIssuesTable";
      if (selectAll2 === true) {
        data["selectAll"] = "true";
      } else {
        data["selectAll"] = "false";
        data["selectedIds"] = selectedIds2;
      }
    } else if (excelId === "Excel") {
      data["table"] = "issuesTable";
      if (selectAll === true) {
        data["selectAll"] = "true";
        data["advanceFilter"] = advancedFilter;
        if (advancedFilter) {
          data["tableFilter"] = JSON.stringify(tableFilter);
        }
      } else {
        data["selectAll"] = "false";
        data["selectedIds"] = selectedIds;
      }
    }
    return data;
  }

  $(document).on("click", ".errorIcon", function () {
    var id = $(this).attr("data-id");
    $(".editError[data-id='" + id + "']").trigger("click");
  });
  $(document).on("click", ".qualityIssueSave", function () {
    var errorTypeValue = $("#errorTypeTextArea").val();
      if (/;|#|<|>|'|"/.test(errorTypeValue)) {
      //Single or double quotes not allowed in qualityIssueDescription
      $("#qualityIssueModal").find(".alert-danger").removeClass("hide");
      $("#qualityIssueModal")
        .find(".errorMessageSpan")
        .html($.i18n._("qualityModule.qualityIssueDescription.validation"));
      return;
    }
    showLoader();
    $.ajax({
      type: "POST", // value can be very large causing - request header too large error
      url: updateFieldErrorURL,
      data: {
        caseNumber: caseNumber,
        versionNumber: versionNumber,
        value: $("#fieldValue").val(),
        fieldName: $("#fieldName").val(),
        fieldLocation: $("#fieldLocation").val(),
        mandatoryType: $("#mandatoryType").val(),
        dataType: $("#dataType").val(),
        errorType: $("#errorTypeTextArea").val(),
        submissionIdentifier: $("#submissionIdentifier").val(),
        qualityIssueType: $("#qualityIssueType").val(),
        qualityIssueTypeId: $("#qualityIssueTypeId").val(),
        entryType: $("#entryType").val(),
        priority: $("#issuePriority").val(),
        id: $("#errorId").val(),
        selectedId: $("#selectedId").val(),
      },
    })
      .done(function (result) {
        $("#qualityIssueModal").modal("hide");
        location.reload();
        hideLoader();
      })
      .fail(function () {
        console.log("Error retrieving POI parameters");
      });
  });

  function initExistingErrors(aoData) {
    var totalFieldLabel = 0;
    var notError = 0;
    for (var i = 0; i < aoData.length; i++) {
      var row = aoData[i]._aData ? aoData[i]._aData : aoData[i];
      if (row.fieldLocation) {
        totalFieldLabel++;
        var label = $("label[data-location='" + row.fieldLocation + "']");
        label.parent().find("span.errorIcon").remove();
        label.removeClass("fieldLabel");
        label.addClass("errorIcon");
        label.attr("data-id", row.id);
        label.before(
          '<span class="fa fa-warning errorIcon" data-id=\'' +
            row.id +
            '\' style="color:red;cursor: pointer; font-size: 10px;position: relative;top: -2px;"></span> '
        );
      }
      if (row.qualityIssueNotError) {
        notError++;
      } else if (!row.qualityIssueTypeId || row.qualityIssueType === EMPTY_LABEL) {
        notError++;
      }
    }
    $("#totalLabel").html(aoData.length - notError); //Subtracting not error issue count from total count
    //        $("#totalLabel").html(aoData.length)
    $("#totalFieldLabel").html(aoData.length - totalFieldLabel);
  }

  $(document).on("click", ".showHideEmptyFields", function () {
    $("label[data-value='']").toggle();
  });

  $(document).on("click", ".showHideAttachments", function () {
    var showHideAttachmentsSpinner = $("#showHideAttachmentsSpinner");
    var isAttachmentsLoading = showHideAttachmentsSpinner.is(":visible");
    if (!isAttachmentsLoading) {
      if ($(".attachmentContent").is(":visible")) {
        showHideAttachment(false);
      } else {
        if (ifAttatchemntsLoaded == 0) {
          showHideAttachmentsSpinner.show();
          $.ajax({
            url: getAttachmentsByCaseNoUrl,
            success: function (data) {
              showHideAttachmentsSpinner.hide();
              var s = "";
              for (var i = 0; i < data.length; i++) {
                if (data[i].notes != undefined && data[i].notes != "") {
                  s +=
                    '<option value="' +
                    data[i].id +
                    '" data-filename="' +
                    data[i].fileName +
                    '" data-version="' +
                    data[i].version +
                    '" data-isRedacted="' +
                    data[i].isRedacted +
                    '">' +
                    data[i].fileName +
                    " (" +
                    data[i].notes +
                    ")</option>";
                } else {
                  s +=
                    '<option value="' +
                    data[i].id +
                    '" data-filename="' +
                    data[i].fileName +
                    '" data-version="' +
                    data[i].version +
                    '" data-isRedacted="' +
                    data[i].isRedacted +
                    '">' +
                    data[i].fileName +
                    "</option>";
                }
              }
              if (data.length > 0) {
                $("#files").html(s);
                $("#files").removeAttr("disabled");
                $(".showDocument").removeAttr("disabled");
              }
              ifAttatchemntsLoaded = 1;
              showHideAttachment(true);
            },
            error: function (err) {
              showHideAttachmentsSpinner.hide();
              showHideAttachment(true);
              $(".errorContent").html($.i18n._("app.ajax.unrecognized.error"));
              $(".errorDiv").show();
              setTimeout(function () {
                $(".errorContent").html("");
                $(".errorDiv").hide();
                showHideAttachment(false);
              }, 4000);
            },
          });
        } else {
          showHideAttachment(true);
        }
      }
    }
  });

  $('[name="errorsCountCloseButton"]').on("click", function () {
    $(".errorContent").html("");
    $(".errorDiv").hide();
  });

  var init_filter = function () {
    var filter_data = [
      {
        label: $.i18n._("app.advancedFilter.qualityIssueDescription"),
        type: "select2-manual",
        name: "errorType",
        data: errorTypesArray,
        filter_group: "standard",
      },
      {
        label: $.i18n._("app.advancedFilter.fieldName"),
        type: "select2-manual",
        name: "fieldName",
        data: fieldNamesArray,
        filter_group: "standard",
      },
      {
        label: $.i18n._("app.advancedFilter.value"),
        type: "text",
        name: "value",
      },
      {
        label: $.i18n._("app.advancedFilter.fieldLocation"),
        type: "select2-manual",
        name: "fieldLocation",
        data: fieldLocationsArray,
        filter_group: "standard",
      },
      {
        label: $.i18n._("app.advancedFilter.source"),
        type: "select2-manual",
        name: "entryType",
        data: [
          { key: "A", value: "A" },
          { key: "M", value: "M" },
        ],
      },
      {
        label: $.i18n._("app.advancedFilter.priority"),
        type: "select2",
        name: "priority",
        ajax: {
          url: qualityPriorityTagsUrl,
          data_handler: function (data) {
            var priorities = [];
            for (var optionIter in data) {
              var d = {};
              d["val"] = $.trim(data[optionIter]);
              priorities.push(d);
            }
            return pvr.filter_util.build_options(
              priorities,
              "val",
              "val",
              true
            );
          },
          error_handler: function (data) {
            console.log(data);
          },
        },
        filter_group: "standard",
      },
      {
        label: $.i18n._("app.fixed.template.issueType"),
        type: "select2-manual",
        name: "qualityIssueType",
        data: issueTypesArray,
        filter_group: "standard",
      },
      {
        label: $.i18n._("app.fixed.template.rootCause"),
        type: "select2-manual",
        name: "rootCause",
        data: rootCasuesArray,
        filter_group: "standard",
      },
      {
        label: $.i18n._("app.fixed.template.responsibleParty"),
        type: "select2-manual",
        name: "responsibleParty",
        data: responsiblePartiesArray,
        filter_group: "standard",
      },
    ];

    pvr.filter_util.construct_right_filter_panel({
      table_id: "#issuesTable",
      container_id: "config-filter-panel",
      filter_defs: filter_data,
      column_count: 1,
      panel_width: 450,
      filter_group: [
        { key: "standard", label: "" },
        { key: "additional", label: "" },
      ],
      done_func: function (filter) {
        tableFilter = filter;
        advancedFilter = true;
        table = $("#issuesTable").DataTable();
        table.draw();
      },
    });
  };

  function reloadQualityIssues() {
    var totalQualityIssuesSpinner = $("#totalQualityIssuesSpinner");
    totalQualityIssuesSpinner.show();

    table.ajax.reload(function (data) {
      initExistingErrors(data);
      totalQualityIssuesSpinner.hide();
    });
  }

  var appendDropdownOutside = (function () {
    var dropdownMenu;
    $(".dropdown-outside").on({
      "show.bs.dropdown": function (e) {
        e.stopPropagation();
        dropdownMenu = $(e.target).find(".dropdown-menu");
        var eOffset = $(e.target).offset();
        $("body").append(
          dropdownMenu
            .css({
              display: "block",
              top: eOffset.top + $(e.target).outerHeight(),
              left: eOffset.left - 100,
              "max-width": "200px",
            })
            .detach()
        );
      },
      "hidden.bs.dropdown": function (e) {
        e.stopPropagation();
        $(e.target).append(dropdownMenu.detach());
        dropdownMenu.hide();
        $(".pv-grp-btn").removeClass("active");
      },
    });
  })();
});

function initCaseFormAssignedTo(){
    let userSelect =  $("#case-form-assignedToUser")
    let groupSelect =  $("#case-form-assignedToUserGroup")
    if(userSelect.hasClass('select2-hidden-accessible')) {
        userSelect.select2("destroy");
    }
    if(groupSelect.hasClass('select2-hidden-accessible')) {
        groupSelect.select2("destroy");
    }
    bindShareWith(userSelect,sharedWithUserListUrl,  sharedWithValuesUrl,"60%",true, $('body'),  "placeholder.selectUsers"
    ).on("change", function () {
        changeAssignedEvent($(this), "User_");
        groupSelect.attr("data-extraParam",JSON.stringify({user:$(this).val()}));
        groupSelect.data('select2').results.clear()
    });
    bindShareWith(groupSelect,sharedWithGroupListUrl,  sharedWithValuesUrl,"60%",true, $('body'),  "placeholder.selectGroup"
    ).on("change", function () {
        changeAssignedEvent($(this),"UserGroup_");
        userSelect.attr("data-extraParam",JSON.stringify({userGroup:$(this).val()}));
        userSelect.data('select2').results.clear()
    });

    userSelect.attr("data-extraParam",JSON.stringify({userGroup:groupSelect.attr("data-value")}));
    groupSelect.attr("data-extraParam",JSON.stringify({user:userSelect.attr("data-value")}));
    let changeAssignedEvent = function($this, field){
        showLoader();
        $.ajax({
            url: updateAssignedOwnerUrl,
            data: {
                caseNumber: $this.data("case-number"),
                errorType: $this.data("error-type"),
                submissionIdentifier: $this.attr("data-submissionIdentifier"),
                value:$this.val(),
                dataType: $this.attr("data-dataType"),
                version: $this.data("version-number"),
                field: field,
                selectedCases:
                    selectedCases && selectedCases.length > 0
                        ? JSON.stringify(selectedCases)
                        : "",
            }
        })
            .done(function (data) {
                hideLoader();
            })
            .fail(function (data) {
                hideLoader();
                alert($.i18n._("Error") + " : " + data.responseText);
            });
    }
}