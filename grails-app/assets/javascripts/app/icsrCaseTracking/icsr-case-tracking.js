var IcsrCaseStateEnum = Object.freeze({
    "ERROR": 'ERROR',
    "COMMIT_REJECTED": 'COMMIT_REJECTED',
    "PARSER_REJECTED": 'PARSER_REJECTED',
    "ACKNOWLEDGED": 'ACKNOWLEDGED',
    "COMMIT_RECEIVED": 'COMMIT_RECEIVED',
    "COMMIT_ACCEPTED": 'COMMIT_ACCEPTED',
    "SCHEDULED": 'SCHEDULED',
    "MANUAL": 'MANUAL',
    "SUBMISSION_NOT_REQUIRED": 'SUBMISSION_NOT_REQUIRED',
    "SUBMISSION_NOT_REQUIRED_FINAL": 'SUBMISSION_NOT_REQUIRED_FINAL',
    "TRANSMITTED": 'TRANSMITTED',
    "TRANSMITTING_ATTACHMENT": 'TRANSMITTING_ATTACHMENT',
    "TRANSMITTED_ATTACHMENT": 'TRANSMITTED_ATTACHMENT',
    "SUBMITTED": 'SUBMITTED',
    "GENERATION_ERROR":'GENERATION_ERROR',
    "GENERATED":'GENERATED',
    "TRANSMISSION_ERROR":'TRANSMISSION_ERROR'
});

const actionDropdownTemplates= {
    "generateReport": (row) => {
        if (row.flagLocalCpRequired || row.flagAutoGenerate) return "";
        return `<li role="presentation">
            <a role="menuitem" class="localCp" 
               data-url="${saveLocalCp}?caseId=${row.caseId}&flagLocalCp=2&versionNumber=${row.versionNumber}&caseNumber=${row.caseNumber}&prodHashCode=${row.prodHashCode}&profileId=${row.profileId}&processReportId=${row.processedReportId}&exTempQueryId=${row.exIcsrTemplateQueryId}">
               ${$.i18n._("generate.report")}
            </a>
        </li>`;
    },

    "localCPCompleted": (row) => {
        if (!row.flagLocalCpRequired) return "";
        return `<li role="presentation">
            <a role="menuitem" class="localCp" 
               data-url="${saveLocalCp}?caseId=${row.caseId}&flagLocalCp=1&versionNumber=${row.versionNumber}&caseNumber=${row.caseNumber}&prodHashCode=${row.prodHashCode}&profileId=${row.profileId}&processReportId=${row.processedReportId}&exTempQueryId=${row.exIcsrTemplateQueryId}">
               ${$.i18n._("local.cp")}
            </a>
        </li>`;
    },

    "submissionNotRequired": (row) =>
        `<li role="presentation">
            <a role="menuitem" href="#" data-toggle="modal" data-target="#reportSubmissionModal" 
               data-url="${markAsSubmittedUrl}?noSubmisson=true&icsrTempQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}">
               ${$.i18n._('labelNotSubmit')}
            </a>
        </li>`,

    "delete": (row) =>
        `<li role="presentation">
            <a role="menuitem" href="#" data-toggle="modal" data-target="#deleteCaseModal"
               data-case-number="${row.caseNumber}" data-version-number="${row.versionNumber}" 
               data-profile-name="${row.profileName}" data-followup-number="${row.followupNumber}" 
               data-url="${deleteCaseUrl}?exTempQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}">
               ${$.i18n._("labelDelete")}
            </a>
        </li>`,

    "Re-Generate": (row) =>
        `<li role="presentation">
            <a role="menuitem" href="#" data-toggle="modal" data-target="#regenerateCaseModal"
               data-url="${regenerateCaseUrl}?exTempQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}">
               ${$.i18n._('regenerateCase')}
            </a>
        </li>`,

    "transmit": (row) => {
        var transmitUrl = `${transmitCase}?icsrTempQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}&profileName=${row.profileName}`;
        return `<li role="presentation">
            <a role="menuitem" href="#" class="checkPreviousVersion" data-template-id="${row.templateId}" 
               data-profile-name="${row.profileName}" data-recipient="${row.recipient}" 
               data-case-number="${row.caseNumber}" data-version-number="${row.versionNumber}" 
               data-url="${transmitUrl}">
               ${$.i18n._("transmitCase")}
            </a>
        </li>`;
    },

    "submit": (row) =>
        `<li role="presentation">
            <a role="menuitem" href="#" data-toggle="modal" data-target="#reportSubmissionModal"
               data-url="${markAsSubmittedUrl}?icsrTempQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}">
               ${$.i18n._("labelSubmit")}
            </a>
        </li>`,

    "download": (row) =>
        `<li role="presentation">
            <a href="${downloadReportUrl}?exIcsrTemplateQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}&processReportId=${row.processedReportId}&prodHashCode=${row.prodHashCode}&fromIcsr=true">
               ${$.i18n._("labelDownload")}
            </a>
        </li>`,

    "emailTo": (row) =>
        `<li role="presentation">
            <a role="menuitem" href="#" data-toggle="modal" data-target="#emailToModal"
               data-ex-icsr-template-query-id="${row.exIcsrTemplateQueryId}" 
               data-case-number="${row.caseNumber}" data-version-number="${row.versionNumber}">
               ${$.i18n._("labelEmailTo")}
            </a>
        </li>`,

    "markNullification": (row) => {
        if (!row.allowNullification) return "";
        return `<li role="presentation">
            <a role="menuitem" href="#" data-toggle="modal" data-target="#nullificationModal"
               data-url="${markAsNullifiedUrl}?icsrTempQueryId=${row.exIcsrTemplateQueryId}&caseNumber=${row.caseNumber}&versionNumber=${row.versionNumber}&dueInDays=${row.dueInDays}&prodHashCode=${row.prodHashCode}">
               ${$.i18n._("labelNullify")}
            </a>
        </li>`;
    }
};

var bulkIds = [];
var initialSubmissionDate = ''
var initialSubmissionDateNew = ''
var initialTimeZone = ''
const icsrTrackingTablePageInfo = {};

window.parent.postMessage("ICSRPageStart", "*");

$(function () {
  var tableFilter = {};
  var advancedFilter = false;
  var searchString = {};

  bindBulkCheckboxEvent();

  if ($("#rxTableIcsrCaseTracking").is(":visible")) {
    var table = $("#rxTableIcsrCaseTracking")
      .ExtendedDataTable({

          stateSaving: {
              isEnabled: true,
              stateDataKey: 'icsrCaseTrackingTableStateKey',
              permanentAdvancedFilters: ['recipient', 'profileId'],
              permanentInlineFilters: ['recipient', 'profileName']
          },

        customProcessing: true, //handled using processing.dt event
        serverSide: true,

          autoWidth: false,

          fixedHeader: {
              isEnabled: true
          },

          colResize: {
              isEnabled: true,
              //hoverClass: 'dt-colresizable-hover',
              isResizable: function (column) {
                  if (column.idx === 0) {
                      return false;
                  }
                  return true;
              }
          },

          inlineFilterConfig: {
              callback: function () {
                  resetBulkIds();
              }
          },
          advancedFilterConfig: {
              panelWidth: 467,
              //columnCount: 2,
              //filterGroup: [{key: "standard", label: "Standard"}, {key: "additional", label: "Additional"}],
              //containerId: 'config-filter-panel',
              callback: function () {
                  resetBulkIds();
              },
              extraFilters: [
                  // {
                  //     name: 'advancedFilterExtraConfig1',
                  //     type: 'number',
                  //     label: 'Extra Config 1',
                  // },
                  // {
                  //     name: 'advancedFilterExtraConfig2',
                  //     type: 'date-range',
                  //     group: [
                  //         {
                  //             label: 'Extra Config 2 From',
                  //         },
                  //         {
                  //             label: 'Extra Config 2 To',
                  //         }
                  //     ]
                  // }
              ]
          },

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
        ajax: {
          url: icsrCaseTrackingListUrl,
          dataSrc: "data",
          type: "POST",
          data: function (d) {
            var isManuaSchedule = new URLSearchParams(window.location.href).get(
              "isManualSchedule"
            );
            if (isManuaSchedule && isManuaSchedule != "") {
              d.tableFilter = {};
              d.advancedFilter = false;
              d.search.value = "";
              d.searchData = "";
              $(".dataTables_filter input[type='search']").val("");
            } else {
              d.searchData = JSON.stringify(searchString);
            }
            d.state = $('select[name="icsrCaseStateFilter"]').val();
          },
        },
        preDrawCallback: function () {
          disableSearchBox();
        },
        initComplete: function () {
            updateIcsrTrackingTablePageInfo(table);
          submission_modal_after_load();
          delete_case_modal_after_load();
          regenerate_case_modal_after_load();
          resetBulkIds();

          //workaround due to search.dt triggered by sort applying
          $('#rxTableIcsrCaseTracking_wrapper .dt-search input[type="search"]').on('keyup paste cut', function (e) {
              if (e.type !== 'keyup' || (typeof e.which === 'undefined' ||
                  (typeof e.which === 'number' && e.which > 0 && !e.ctrlKey && !e.metaKey && !e.altKey && ![37,38,39,40].includes(e.which)))) {
                  if (bulkIds.length > 0) {
                      resetBulkIds();
                  }
              }
          });

          initIcsrCaseStateDropDown("rxTableIcsrCaseTracking", table);
          var icsrCaseStateDropDown = $("#icsrCaseStateControl");
          icsrCaseStateDropDown.select2();
          icsrCaseStateDropDown.on("select2:open", function (e) {
              var searchField = $('.select2-dropdown .select2-search__field');
              if (searchField.length) {
                  searchField[0].focus();
              }
          }).on("change", function () {
            sessionStorage.setItem("icsrCaseStatus", $(this).val());
            resetBulkIds();
            $("#rxTableIcsrCaseTracking").DataTable().draw();
          });
          if (
            typeof sessionStorage.getItem("icsrCaseStatus") != "undefined" &&
            sessionStorage.getItem("icsrCaseStatus") != null
          ) {
            icsrCaseStateDropDown
              .val(sessionStorage.getItem("icsrCaseStatus"))
              .trigger("change");
          }
          setTimeout(function () {
            if (
              typeof sessionStorage.getItem("icsrCaseStatus") != "undefined" &&
              sessionStorage.getItem("icsrCaseStatus") != null
            )
              $("#rxTableIcsrCaseTracking").DataTable().draw();
          }, 100);$("div.dataTables_filter input").off();
            var timer = null;
            $("div.dataTables_filter input").keyup( function (e) {
                var searchString = this.value;
                if(timer) {
                    clearTimeout(timer);
                }
                timer = setTimeout(function (){
                    table.search(searchString).draw();
                }, 800);
            });
          $("div.dataTables_filter input").off();
          var timer = null;
          $("div.dataTables_filter input").on("keyup", function (e) {
            var searchString = this.value;
            if (timer) {
              clearTimeout(timer);
            }
            timer = setTimeout(function () {
              table.search(searchString).draw();
            }, 500);
          });
        },
        aLengthMenu: [
          [50, 100, 200, 500],
          [50, 100, 200, 500],
        ],
        rowId: "icsrCaseTrackingUniqueId",
          order: {
              data: 'dueDate',
              dir: 'desc'
          },
        bLengthChange: true,
        iDisplayLength: 50,
        columnDefs: [
            {"visible": false, "targets": [4,5,6,7,8,12,13,14,15,20,21]},
            {"orderable": false, "targets": [10, 22, 23]},
            {width: '100px', "targets": [5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22]}
        ],
        pagination: true,

        drawCallback: function (settings) {
          pageDictionary(
            $("#rxTableIcsrCaseTracking_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );
          var searchString = settings.json.caseNumber;
          if (searchString != "" && searchString != undefined) {
            $("div.dataTables_filter input").val(settings.json.caseNumber);
            table.search(settings.json.caseNumber);
          }
          colEllipsis();
          updateIcsrProfileSelectAllState();
        },
        aoColumns: [
          //Don't Change mData labels as we are using it for our sorting parameter name for sorting data should be property name
          /*{
                    "mData": null,
                    mRender: function (data, type, row) {
                        return '';
                    }
                },*/
          {
            mData: null,
              width: '33px',
              orderable: false,
              className: "select-checkbox",
            mRender: function (data, type, row) {
              var icsrRowId = row.id ? row.id : -1;
              var checked = "";
              $.each(bulkIds, function (i, obj) {
                if (obj.currentIcsrRowId == icsrRowId) checked = "checked";
              });
              var recipient = row.recipient ? row.recipient : null;
              return (
                '<input class="bulkTransmitSubmitChk" type="checkbox" data-icsrRowId="' +
                icsrRowId +
                '"' +
                checked +
                ' data-template-id="' +
                row.templateId +
                '" data-profile-name="' +
                row.profileName +
                '" data-recipient="' +
                row.recipient +
                '" data-case-number="' +
                row.caseNumber +
                '" data-version-number="' +
                row.versionNumber +
                '"data-preferred-time-zone="' +
                row.preferredTimeZone +
                '" data-id=' +
                row.e2BStatus +
                " value=" +
                row.exIcsrTemplateQueryId +
                "_" +
                row.caseNumber +
                "_" +
                row.versionNumber +
                ' style="cursor: pointer"/>'
              );
            },
          },
          {
            mData: "caseNumber",
              advancedFilter: true,
              inlineFilter: {
                  type: 'multi-value-text'
              },
            sClass: "col-min-110",
              width: '110px',
            mRender: function (data, type, row) {
              var link = "";
              if (row.showReportLink == true) {
                link =
                  '<a href="' +
                  generateCioms +
                  "?caseNumber=" +
                  row.caseNumber +
                  "&versionNumber=" +
                  row.versionNumber +
                  '&fromIcsr=true" target="_blank" >' +
                  encodeToHTML(data) +
                  "</a>";
              } else {
                link = encodeToHTML(data);
              }
              if (
                row.e2BStatus == "TRANSMISSION_ERROR" ||
                row.e2BStatus == "ERROR"
              ) {
                link =
                  link +
                  ' <span class="fa fa-exclamation-circle es-error fa-sm" style="color: red"></span>';
              }
              return link;
            },
          },
          {
            mData: "versionNumber",
              advancedFilter: {
                  //label: 'V#',
                  type: 'number',
                  name: "versionNumber"
              },
              inlineFilter: {
                  type: 'number'
              },
              width: '60px',
            sClass: "dt-center col-min-60",
            mRender: function (data, type, row) {
              var link = "";
              if (row.showReportLink == true) {
                link =
                  '<a href="#" class="caseDetail" data-toggle="modal" data-case-number="' +
                  row.caseNumber +
                  '" data-version-number="' +
                  row.versionNumber +
                  '"' +
                  ">" +
                  encodeToHTML(data) +
                  "</a>";
              } else {
                link = encodeToHTML(data);
              }
              return link;
            },
          },
          {
              "mData": "recipient",
              inlineFilter: {
                  type: 'multi-value-text'
              },
              width: '100px',
            mRender: function (data, type, row) {
                var recipient = encodeToHTML(data);
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + recipient + '">' + recipient + '</div>';
            },
          },
          {
            mData: "followupInfo",
              inlineFilter: {
                  type: 'multi-value-text',
                  name: 'icsrTrackingFollowupInfo'
              },
              stackId: 1,
              width: '80px',
              sClass: 'dt-center',
            mRender: function (data, type, row) {
              if(data != null)
                 data = $.i18n._("app.followupInfo." + data)
              var followupData =
                '<div class="followupInfo edt-stacked-render-data" style="white-space: nowrap;">' +
                encodeToHTML(data) +
                "</div>";
              return followupData;
            },
          },
          {
              "mData": "followupNumber",
              width: '80px',
              inlineFilter: {
                  type: 'number'
              },
              stackId: 1,
              "sClass":"dt-center",
            mRender: function (data, type, row) {
                return encodeToHTML(data);
            },
          },
          {
              "mData": "caseReceiptDate",
              width: '110px',
              stackId: 3,
              inlineFilter: {
                  type: 'date-range'
              },
              advancedFilter: {
                  type: 'date-range',
                  group: [
                      {
                          label: $.i18n._("app.advancedFilter.caseReceiptDateStart")
                      },
                      {
                          label: $.i18n._("app.advancedFilter.caseReceiptDateEnd")
                      }
                  ]
              },
              sClass: "dt-center col-min-110",
            mRender: function (data, type, row) {
                const caseReceiptDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + caseReceiptDate + '">' + caseReceiptDate + '</div>';
            },
          },
          {
              "mData": "safetyReceiptDate",
              inlineFilter: {
                  type: 'date-range'
              },
              width: '110px',
              sClass: "dt-center col-min-110",
              advancedFilter: {
                  type: 'date-range',
                  group: [
                      {
                          label: $.i18n._("app.advancedFilter.safetyReceiptDateStart")
                      },
                      {
                          label: $.i18n._("app.advancedFilter.safetyReceiptDateEnd")
                      }
                  ]
              },
            mRender: function (data, type, row) {
                const safetyReceiptDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + safetyReceiptDate + '">' + safetyReceiptDate + '</div>';
            },
          },
          {
              "mData": "productName",
              width: '110px',
              inlineFilter: {
                  maxlength: 16000
              },
              advancedFilter: {
                  maxlength: 16000
              },
            mRender: function (data, type, row) {
                var productName = encodeToHTML(data);
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + productName + '">' + productName + '</div>';
            },
          },
            {
                mData: "profileName",
                stackId: 2,
                inlineFilter: {
                    type: 'multi-value-text'
                },
                width: '110px',
                advancedFilter: {
                    type: 'select2-multi-value',
                    valueType: 'number',
                    name: 'profileId',
                    ajax: {
                        url: listProfiles,
                        async: true,
                        valueField: 'id',
                        displayField: 'reportName'
                    }
                },
                sClass:"col-min-120",
                mRender: function (data, type, row) {
                    var profileName = "";
                    var title = "";
                    if (row.showReportLink == true) {
                        profileName =
                            '<a href="' +
                            icsrProfileViewUrl +
                            "?id=" +
                            row.exIcsrProfileId +
                            '" >' +
                            encodeToHTML(data) +
                            "</a>";
                    } else {
                        profileName = encodeToHTML(data);
                    }
                    title = encodeToHTML(data);
                    if (row.manualFlag) {
                        profileName = profileName + " (" + $.i18n._('app.manual.schedule.label') + ")";
                        title = title + " (" + $.i18n._('app.manual.schedule.label') + ")";
                    } else if (row.queryName) {
                        profileName = profileName + " (" + row.queryName + ")";
                        title = title + " (" + row.queryName + ")";
                    }
                    return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + title + '">' + profileName + '</div>';
                },
            },
            {
                mData: "reportForm",
                stackId: 2,
                sClass:"col-min-100",
                width: '110px',
                mRender: function (data, type, row) {
                    var link = "";
                    if (row.showReportLink == true && row.isGenerated == true) {
                        if (~[IcsrCaseStateEnum.SCHEDULED, IcsrCaseStateEnum.MANUAL].indexOf(row.e2BStatus)) {
                            link = '<a href="' + showReportUrl + '?isInDraftMode=true&exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' +row.processedReportId +'&prodHashCode=' + row.prodHashCode + '&reportLang=' + userLocale + '&fromIcsr=true" >' + encodeToHTML(data) + '</a>';
                        } else {
                            link = '<a href="' + showReportUrl + '?exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' +row.processedReportId +'&prodHashCode=' + row.prodHashCode +'&reportLang=' + userLocale + '&fromIcsr=true" >' + encodeToHTML(data) + '</a>';
                        }
                    } else {
                        link = row.submissionFormDesc ? encodeToHTML(row.submissionFormDesc) : encodeToHTML(data);
                    }
                    var allData = data ? data :"";
                    return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + allData + '">' + link + '</div>';
                }
            },
            {
                "mData": "awareDate",
                inlineFilter: {
                    type: 'date-range'
                },
                width: '110px',
                stackId: 3,
                sClass: "dt-center col-min-110",
                mRender: function (data, type, row) {
                    const awareDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                    return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + awareDate + '">' + awareDate + '</div>';
                }
            },
            {
                "mData": "authorizationType",
                "sClass":"col-min-100",
                stackId: 6,
                width: '110px',
                inlineFilter: {
                    maxlength: 1000,
                    name: 'icsrTrackingAuthorizationType'
                },
                advancedFilter: {
                    type: 'select2',
                    name: 'icsrTrackingAuthorizationType',
                    ajax: {
                        url: listAuthorizationType,
                        async: true,
                        valueField: 'authroizationType',
                        displayField: 'authroizationType'
                    }
                },
                mRender: function (data, type, row) {
                    let authorizationType = (data !== null) ? $.i18n._("app.authorizationType." + data.toLowerCase().replace(/ /g, "_")) : '';
                    authorizationType = encodeToHTML(authorizationType);
                    return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + authorizationType + '">' + authorizationType + '</div>';
                }
            },
            {
                data: 'approvalNumber',
                stackId: 6,
                width: '110px',
                inlineFilter: {
                    maxlength: 1000
                },
                advancedFilter: {
                    maxlength: 1000
                },
                mRender: function (data, type, row) {
                    const approvalNumber = encodeToHTML(data);
                    return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + approvalNumber + '">' + approvalNumber + '</div>';
                },
            },
          {
            mData: "dueInDays",
                inlineFilter: {
                  type: 'number'
              },
              sClass: 'dt-center',
            mRender: function (data, type, row) {
              var dueInDays = encodeToHTML(data) + $.i18n._("days");
              return dueInDays;
            },
          },
          {
                mData: "scheduledDate",
              stackId: 4,
              inlineFilter: {
                  type: 'date-range'
              },
              advancedFilter: {
                  type: 'date-range',
                  group: [
                      {
                          label: $.i18n._("app.advancedFilter.scheduleDateStart")
                      },
                      {
                          label: $.i18n._("app.advancedFilter.scheduleDateEnd")
                      }
                  ]
              },
              sClass: "dt-center col-min-110",
              mRender: function (data, type, row) {
                  var scheduledDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                  return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + scheduledDate + '">' + scheduledDate + '</div>';
              }
            },
            {
                mData: "dueDate",
                inlineFilter: {
                    type: 'date-range'
                },
                stackId: 4,
                sClass: "dt-center col-min-110",
                mRender: function (data, type, row) {
                    var clazz = "";
                    if (row.indicator == "red") {
                        clazz =
                            'class="label-danger edt-stacked-render-data" style="padding: 2px; font-size:12px;"';
                    } else if (row.indicator == "yellow") {
                        clazz =
                            'class="label-primary edt-stacked-render-data" style="padding: 2px; font-size:12px;"';
                    } else {
                        clazz =
                            'class="label-normal edt-stacked-render-data" style="padding: 2px; font-size:12px;"';
                    }
                    if (data) {
                        return '<span ' + clazz + '>' + moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT) + "</span>";
                    }
                    return "<span class='edt-stacked-render-data'></span>";
                },
            },
            {
            mData: "generationDate",
                stackId: 5,
                inlineFilter: {
                    type: 'date-range'
                },
                advancedFilter: {
                    type: 'date-range',
                    group: [
                        {
                            label: $.i18n._("app.advancedFilter.generationDateStart")
                        },
                        {
                            label: $.i18n._("app.advancedFilter.generationDateEnd")
                        }
                    ]
                },
                sClass: "dt-center col-min-110",
            mRender: function (data, type, row) {
                var generationDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + generationDate + '">' + generationDate + '</div>';
            },
          },
          {
            mData: "transmissionDate",
              inlineFilter: {
                  type: 'date-range'
              },
              stackId: 5,
              sClass: "dt-center col-min-110",
            mRender: function (data, type, row) {
                var transmissionDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + transmissionDate + '">' + transmissionDate + '</div>';
            },
          },
          {
            mData: "submissionDate",
              inlineFilter: {
                  type: 'date-range'
              },
              advancedFilter: {
                  type: 'date-range',
                  group: [
                      {
                          label: $.i18n._("app.advancedFilter.submissionDateStart")
                      },
                      {
                          label: $.i18n._("app.advancedFilter.submissionDateEnd")
                      }
                  ]
              },
              sClass: "dt-center col-min-110",
            mRender: function (data, type, row) {
                var submissionDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + submissionDate + '">' + submissionDate + '</div>';
            },
          },
          {
            mData: "preferredDateTime",
              inlineFilter: {
                  type: 'date-range'
              },
            sClass: "col-min-110",
            mRender: function (data, type, row) {
                var preferredDateTime = data ? moment.utc(data).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) + ' (' + row.recipientTimeZone + ')' : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + preferredDateTime + '">' + preferredDateTime + '</div>';
            },
          },
          {
            mData: "modifiedDate",
              inlineFilter: {
                  type: 'date-range'
              },
              sClass: "dt-center col-min-110",
            mRender: function (data, type, row) {
                var modifiedDate = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                return '<div class="one-row-dot-overflow edt-stacked-render-data" title="' + modifiedDate + '">' + modifiedDate + '</div>';
            },
          },
          {
            mData: "e2BStatus",
            sClass: "dt-center",
            mRender: function (data, type, row) {
              var dataShow = "";
              if (data) {
                var msg = data;
                if (data == IcsrCaseStateEnum.COMMIT_RECEIVED) {
                  dataShow =
                    ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' +
                    $.i18n._("icsr.case.tracking.pending.attachment") +
                    '">' +
                    $.i18n._("icsr.case.tracking.status." + data) +
                    ' <i class="fa fa-clock-o" aria-hidden="true"></i></button>';
                } else if (data == IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT) {
                  dataShow =
                    ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' +
                    $.i18n._("icsr.case.tracking.transmitting.attachment") +
                    '">' +
                    $.i18n._("icsr.case.tracking.status." + data) +
                    ' <i class="fa fa-paperclip" aria-hidden="true"></i> <i class="fa fa-spinner" aria-hidden="true"></i></button>';
                } else if (data == IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT) {
                  dataShow =
                    ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' +
                    $.i18n._("icsr.case.tracking.transmitted.attachment") +
                    '">' +
                    $.i18n._("icsr.case.tracking.status." + data) +
                    ' <i class="fa fa-paperclip" aria-hidden="true"></i></button>';
                }  else if (row.regenerateFlag && (~[IcsrCaseStateEnum.GENERATED].indexOf(row.e2BStatus)|| ~[IcsrCaseStateEnum.COMMIT_REJECTED].indexOf(row.e2BStatus) || ~[IcsrCaseStateEnum.GENERATION_ERROR].indexOf(row.e2BStatus)  || ~[IcsrCaseStateEnum.PARSER_REJECTED].indexOf(row.e2BStatus) ||  ~[IcsrCaseStateEnum.TRANSMISSION_ERROR].indexOf(row.e2BStatus))) {
                            dataShow = '<div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' + $.i18n._('icsr.case.tracking.status.RE-GENERATION_IN_PROGRESS') + '">' +'*' +  $.i18n._('icsr.case.tracking.status.' + data)+'</button>';
                }else {
                  dataShow =
                    ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' +
                    $.i18n._("icsr.case.tracking.status." + msg) +
                    '">' +
                    $.i18n._("icsr.case.tracking.status." + data) +
                    "</button>";
                }
                if (
                  ~[
                    IcsrCaseStateEnum.ERROR,
                    IcsrCaseStateEnum.COMMIT_REJECTED,
                    IcsrCaseStateEnum.PARSER_REJECTED,
                  ].indexOf(data)
                ) {
                  dataShow =
                    dataShow +
                    ' <a class="errorInfo" style="cursor: pointer; margin-right: -15px;">' +
                    '<i class="fa fa-exclamation-circle es-error fa-sm"></i> </a>';
                        }
                if (~[IcsrCaseStateEnum.GENERATION_ERROR].indexOf(data)) {
                    dataShow = dataShow +
                        ' <a class="errorInfoUponHover" style="cursor: pointer; margin-right: -15px;">' +
                        '<i class="fa fa-exclamation-circle es-error fa-sm" title="' + $.i18n._('icsr.case.tracking.status.GENERATION_ERROR_MESSAGE') + '"></i> </a>';
                }
                if (~[IcsrCaseStateEnum.SCHEDULED].indexOf(data) && row.showPrequalifiedError == true) {
                    dataShow = dataShow +
                        ' <a class="errorInfoUponHover" style="cursor: pointer; margin-right: -15px;">' +
                        '<i class="fa fa-exclamation-circle es-error fa-sm" title="' + $.i18n._('icsr.case.tracking.status.PRE_QUALIFIED_ERROR_MESSAGE') + '"></i> </a>';
                }
                dataShow = dataShow + "</div>";
              }
              return dataShow;
            },
          },
            {
                mData: null,
                sClass: "dataTableColumnCenter",
                aTargets: ["id"],
                mRender: function(data, type, row) {
                    if (!row.showReportLink) return "";
                    var actionButton = "";

                    // Action Button view/preview
                    var baseParams = "?exIcsrTemplateQueryId=" + row.exIcsrTemplateQueryId + "&caseNumber=" + row.caseNumber + "&versionNumber=" + row.versionNumber
                    var link = showReportUrl + baseParams + "&processReportId=" + row.processedReportId + "&prodHashCode=" + row.prodHashCode + "&reportLang=" + userLocale + "&fromIcsr=true";
                    var label = "view";
                    if ([IcsrCaseStateEnum.SCHEDULED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL].includes(row.e2BStatus)) {
                        if (row.report == true || row.isGenerated == true) {
                            link += "&isInDraftMode=true";
                        } else if (row.isGenerated == false) {
                            link = (row.e2BStatus===IcsrCaseStateEnum.SCHEDULED ? generatedCaseDataScheduled : generateCaseData) + baseParams + "&status=" + row.e2BStatus + "&processedReportId=" + row.processedReportId;
                        }
                        label = "generateCaseData";
                    }
                    actionButton += '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success dt-action-btn btn-xs ' + (row.e2BStatus===IcsrCaseStateEnum.GENERATION_ERROR ? 'disabled-link' : '') + '" target="_blank" href="' + link + '">' + $.i18n._(label) + '</a>'

                    // Dropdown Actions
                    if(!isICSRDist) {
                        if(actionDropdownMap[row.e2BStatus]?.includes("download")) {
                            actionButton += '<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                                                <span class="caret"></span> \
                                                <span class="sr-only">Toggle Dropdown</span> \
                                             </button> \
                                             <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
                            actionButton += actionDropdownTemplates["download"](row);
                            actionButton += "</ul>";
                        }
                        actionButton += "</div>";
                        return actionButton
                    }

                    actionButton += getDropdownActions(row) + "</div>";
                    return actionButton;
                }
            },
        ],
        buttons: [],
        select: {
          info: false,
          style: "multi",
          selector: "td:first-child",
        },
      })
      .on("draw.dt", function () {
        enableSearchBox();
      }).on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      }).on('column-visibility.dt', function (e, settings, column, state) {
        colEllipsis();
        }).on('page.dt', function () {
            updateIcsrTrackingTablePageInfo(table);
        }).on('length.dt', function () {
            updateIcsrTrackingTablePageInfo(table);
            updateIcsrTrackingTablePageInfoSelectAll(false);
        });

    actionButton("#rxTableIcsrCaseTracking");

    loadTableOption("#rxTableIcsrCaseTracking");
    $("#rxTableIcsrCaseTracking tbody")
      .on("click", "tr td button.stateHistory", function () {
        if(isICSRDistAdmin == true)
            openSubmissionHistoryFormModal(table.row($(this).closest("tr")).data());
        else
            openStateHistoryModal(table.row($(this).closest("tr")).data());
      })
      .on("click", "tr td a.errorInfo", function () {
        openErrorDetailsModal(table.row($(this).closest("tr")).data());
      })
      .on("click", "tr td a.caseDetail", function () {
        openCaseDetailsModal(table.row($(this).closest("tr")).data());
      });

    $(".dropdown-menu").find("#icsrProfileSelectAll").attr("disabled", true);
  }

  //preventing autofilling from browser password manager
  $(document).on("keyup paste cut", ".password-input", function () {
    var $this = $(this);
    if ($this.val() === "") {
      $this.attr("type", "text");
      var container = $("#password-input-div");
      container.html(
        '<input name="' +
          new Date().getTime() +
          '" id="password-input" class="form-control password-input" type="text">'
      );
      $(".password-input").focus();
    } else $this.attr("type", "password");
  });

  $(document).on(
    "keyup paste cut",
    ".bulk-transmission-password-input",
    function () {
      var $this = $(this);
      if ($this.val() === "") {
        $this.attr("type", "text");
        var container = $("#bulk-transmission-password-input-div");
        container.html(
          '<input name="' +
            new Date().getTime() +
            '" id="bulk-transmission-password-input" class="form-control bulk-transmission-password-input" type="text">'
        );
        $(".bulk-transmission-password-input").focus();
      } else $this.attr("type", "password");
    }
  );

  $(document).on(
    "keyup paste cut",
    ".bulk-submission-password-input",
    function () {
      var $this = $(this);
      if ($this.val() === "") {
        $this.attr("type", "text");
        var container = $("#password-input-div");
        container.html(
          '<input name="' +
            new Date().getTime() +
            '" id="bulk-submission-password-input" class="form-control bulk-submission-password-input" type="text">'
        );
        $(".bulk-submission-password-input").focus();
      } else $this.attr("type", "password");
    }
  );

  $(document).on("click", ".checkPreviousVersion", function () {
    var $this = $(this);
    var transmitUrl = $this.data("url");
    var templateId = $this.data("templateId");
    var profileName = $this.data("profileName");
    var recipient = $this.data("recipient");
    var caseNumber = $this.data("caseNumber");
    var versionNumber = $this.data("versionNumber");
    showLoader();
    $.ajax({
      url: checkPreviousVersionUrl,
      type: "post",
      data: {
        templateId: templateId,
        profileName: profileName,
        recipient: recipient,
        caseNumber: caseNumber,
        versionNumber: versionNumber,
      },
      dataType: "json",
    })
      .done(function (data) {
        hideLoader();
        transmission_modal_after_load(transmitUrl);
      })
      .fail(function (err) {
        hideLoader();
        var confirmationModal = $("#confirmationModal");
        confirmationModal.modal("show");
        confirmationModal
          .find(".modalHeader")
          .html($.i18n._("app.case.transmit.confirmation.title"));
        confirmationModal
          .find(".confirmationMessage")
          .html($.i18n._("app.case.transmit.confirmation.message"));
        confirmationModal.find(".okButton").html($.i18n._("Continue"));
        confirmationModal
          .find(".okButton")
          .removeClass("btn btn-danger")
          .addClass("btn btn-primary");
        confirmationModal
          .find(".okButton")
          .off()
          .on("click", function () {
            transmission_modal_after_load(transmitUrl);
          });
      });
  });

  $(document).on("click", ".checkPreviousVersionForAllCases", function () {
    showLoader();
    $.ajax({
      url: checkPreviousVersionForAllCasesUrl,
      type: "post",
      data: "checkIds=" + JSON.stringify(bulkIds),
      dataType: "json",
    })
      .done(function (data) {
        hideLoader();
        bulkTransmitConfirmationModal();
      })
      .fail(function (err) {
        hideLoader();
        var confirmationModal = $("#confirmationModal");
        confirmationModal.modal("show");
        confirmationModal
          .find(".modalHeader")
          .html($.i18n._("app.case.transmit.confirmation.title"));
        confirmationModal
          .find(".confirmationMessage")
          .html($.i18n._(
              "app.case.transmit.bulk.confirmation.message",
              encodeToHTML(bulkIds.length)
          ));
        confirmationModal.find(".okButton").html($.i18n._("Continue"));
        confirmationModal
          .find(".okButton")
          .removeClass("btn btn-danger")
          .addClass("btn btn-primary");
        confirmationModal
          .find(".okButton")
          .off()
          .on("click", function () {
            bulk_transmission_modal_after_load();
          });
      });
  });

});

var bulkTransmitConfirmationModal = function () {
    var confirmationModal = $("#confirmationModal");
    confirmationModal.modal("show");
    confirmationModal
        .find(".modalHeader")
        .html($.i18n._("app.case.transmit.confirmation.title"));
    confirmationModal
        .find(".confirmationMessage")
        .html($.i18n._(
                "app.case.bulk.transmit.confirmation.message",
                encodeToHTML(bulkIds.length)
        ));
    confirmationModal.find(".okButton").html($.i18n._("Continue"));
    confirmationModal
        .find(".okButton")
        .removeClass("btn btn-danger")
        .addClass("btn btn-primary");
    confirmationModal
        .find(".okButton")
        .off()
        .on("click", function () {
            bulk_transmission_modal_after_load();
        });
};


$(document).on("click", ".bulkSubmitConfirmation", function () {
    var confirmationModal = $("#confirmationModal");
    confirmationModal.modal("show");
    confirmationModal
        .find(".modalHeader")
        .html($.i18n._("app.case.submit.confirmation.title"));
    confirmationModal
        .find(".confirmationMessage")
        .html($.i18n._(
            "app.case.bulk.submit.confirmation.message",
            encodeToHTML(bulkIds.length)
        ));
    confirmationModal.find(".okButton").html($.i18n._("Continue"));
    confirmationModal
        .find(".okButton")
        .removeClass("btn btn-danger")
        .addClass("btn btn-primary");
    confirmationModal
        .find(".okButton")
        .off()
        .on("click", function () {
            bulk_submission_modal_after_load();
        });
});

$(document).on('click', '[data-evt-clk]', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-clk"));
    const methodName = eventData.method;
    const params = eventData.params;
    var elem = $(this);
    if(methodName == 'openStateHistoryModal') {
        openStateHistoryModal(elem);
    }else if(methodName == 'hideAlert') {
        hideAlert(elem)
    }
});

var delete_case_modal_after_load = function () {
  var deleteCaseModal = $("#deleteCaseModal");
  deleteCaseModal
    .on("shown.bs.modal", function (e) {
      var rowObj = $(e.relatedTarget);
      var deleteCaseUrl = $(e.relatedTarget).data("url");
        var caseNumber = rowObj.data('case-number');
        var versionNumber=rowObj.data('version-number');
        var profileName=rowObj.data('profile-name');
        var followUpNumber=rowObj.data('followup-number');
      //showModalLoader(deleteCaseModal);
      var instanceType = $.i18n._($.i18n._("app.configurationType.CASE"));
      deleteCaseModal.find(".btn").removeAttr("disabled", "disabled");

      deleteCaseModal.find("#deleteCaseModalLabel").text("");
      deleteCaseModal
        .find("#deleteCaseModalLabel")
        .text($.i18n._("modal.delete.title", instanceType));

      var nameToDeleteLabel = $.i18n._("deleteThis", instanceType);
      deleteCaseModal.find("#nameToDelete").text("");
      deleteCaseModal.find("#nameToDelete").text(nameToDeleteLabel);

      deleteCaseModal.find(".caseNumberValue").text(caseNumber);
      deleteCaseModal.find(".versionNumberValue").text(versionNumber);
      deleteCaseModal.find(".profileNameValue").text(profileName);
      deleteCaseModal.find(".followUpNumberValue").text(followUpNumber);
      deleteCaseModal
        .find("#deleteCaseButton")
        .off()
        .on("click", function () {
          const confirmation = $("#deleteCaseJustification").val();
          const confirmationJ = $("#deleteCaseJustificationJa").val() || '';
          const justificationId = $('#standard-justifications:visible').val() || ''
          const requiredConfirmation = pvr.common_util.isJapaneseLocal() ? confirmationJ : confirmation;
          if (requiredConfirmation !== '' && requiredConfirmation.trim().length > 0) {
            showLoader();
            $.ajax({
              type: "GET",
              url: deleteCaseUrl,
              data: { justificationId: justificationId, justification: confirmation, justificationJ: confirmationJ },
              dataType: "json",
            })
              .done(function (result) {
                successNotificationForIcsrCaseTracking(result.message);
                var dataTable = $("#rxTableIcsrCaseTracking").DataTable();
                dataTable.ajax.reload(function () {
                  dataTable.row(rowObj).remove();
                });
                deleteCaseModal.modal("hide");
                hideLoader();
              })
              .fail(function (err) {
                errorNotificationForIcsrCaseTracking(
                  (err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace
                      ? "<br>" + err.responseJSON.stackTrace
                      : "")
                );
                hideLoader();
                deleteCaseModal.modal("hide");
                window.scrollTo(0, 0);
              });
          } else {
            $("#deleteCaseDlgErrorDiv").show();
          }
        });

        $(document).trigger("loadStandardJustifications", ['ICSRMarkReportDeletion']);
    })
    .on("hidden.bs.modal", function () {
      $("#deleteCaseDlgErrorDiv").hide();
      $("#deleteCaseJustification").val("");
    });
};

var disableSearchBox = function () {
  var searchBox = $("div.dataTables_filter input");
  searchBox.attr("disabled", "disabled");
  $("#rxTableIcsrCaseTracking thead tr:eq(1) th input").each(function (i) {
    $(this).attr("disabled", "disabled");
  });
};

var enableSearchBox = function () {
  var searchBox = $("div.dataTables_filter input");
  searchBox.removeAttr("disabled");
  $("#rxTableIcsrCaseTracking thead tr:eq(1) th input").each(function (i) {
    if ($(this).data("type") != "disabled") {
      $(this).removeAttr("disabled");
    }
  });
};

var regenerate_case_modal_after_load = function () {
    var regenerateCaseModal = $("#regenerateCaseModal");
    regenerateCaseModal.on('shown.bs.modal', function (e) {
        var regenerateCaseUrl = $(e.relatedTarget).data('url');
        var rowId = $(e.relatedTarget).parents('tr').attr('id');
        regenerateCaseModal.find('#regenerateCaseModalLabel').text("");
        regenerateCaseModal.find('#regenerateCaseModalLabel').text($.i18n._('modal.regenerate.title'));
        regenerateCaseModal.find("#regenerateButton").off().on("click", function () {
            var confirmation= $("#regenerateComments").val();
            if (confirmation != "" && confirmation.trim().length > 0) {
                $.ajax({
                    type: "POST",
                    url: regenerateCaseUrl,
                    data: {regenerateComment: confirmation},
                    success: function (result) {
                        regenerateCaseModal.modal("hide");
                        reloadData(rowId);
                        successNotificationForIcsrCaseTracking(result.message);
                    }, error: function (err) {
                        errorNotificationForIcsrCaseTracking((err.responseJSON.message ? err.responseJSON.message : "") +
                            (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                        regenerateCaseModal.modal("hide");
                    },
                    beforeSend: function () {
                        regenerateCaseModal.find("#regenerateButton").attr('disabled', 'disabled');
                        showModalLoader(regenerateCaseModal);
                    },
                    complete: function () {
                        regenerateCaseModal.find("#regenerateButton").removeAttr('disabled');
                        hideModalLoader(regenerateCaseModal);
                    }
                });
            }else {
                $('#regenerateErrorMessageDiv').show();
            }
        });
        regenerateCaseModal.find('.regenerateErrorMessageDivClose').off().on('click', function () {
            $('#regenerateErrorMessageDiv').hide();
        });

    }).on("hidden.bs.modal", function () {
        $('#regenerateErrorMessageDiv').hide();
        regenerateCaseModal.find("#regenerateComments").val('');
        regenerateCaseModal.find('#regenerateButton').off();
    });
};

function transmission_modal_after_load(transmitUrl) {
  var transmissionModal = $("#transmitJustification");
  transmissionModal.data("url", transmitUrl);
  transmissionModal.modal("show");
}

function bulk_transmission_modal_after_load() {
  var bulkTransmissionModal = $("#bulkTransmitJustification");
  bulkTransmissionModal.modal("show");
}

function bulk_submission_modal_after_load() {
    var bulkReportSubmissionModal = $("#bulkReportSubmissionModal");
    bulkReportSubmissionModal.modal("show");
}

var submission_modal_after_load = function () {
  var reportSubmissionModal = $("#reportSubmissionModal");
  reportSubmissionModal
    .on("shown.bs.modal", function (e) {
      var rowObj = $(e.relatedTarget);
      var rowId = rowObj.closest("tr").attr("id");
      showModalLoader(reportSubmissionModal);
      reportSubmissionModal
        .find(".modal-content:first")
        .load(rowObj.data("url"), function () {
          hideModalLoader(reportSubmissionModal);
          // buildReportingDestinationsSelectBox(reportSubmissionModal.find("[name=reportingDestinations]"), periodicReportConfig.reportingDestinationsUrl, reportSubmissionModal.find("input[name='primaryReportingDestination']"), false);
          var submissionDate = reportSubmissionModal
            .find('.datepicker input[name="submissionDate"]')
            .val();
          $("#submissionDateDiv").datepicker({
            allowPastDates: true,
            date: submissionDate,
            twoDigitYearProtection: true,
            culture: userLocale,
            momentConfig: {
              format: DEFAULT_DATE_DISPLAY_FORMAT,
            },
          });
          var dueDate = reportSubmissionModal
            .find('.datepicker input[name="dueDate"]')
            .val();
          $("#dueDateDiv").datepicker({
            allowPastDates: true,
            date: dueDate,
            twoDigitYearProtection: true,
            culture: userLocale,
            momentConfig: {
              format: DEFAULT_DATE_DISPLAY_FORMAT,
            },
          });
          reportSubmissionModal
            .find("button.submit-draft")
            .off()
            .on("click", function () {

              const $submFormValidationAlert = $('#icsrTrackingSubmFormValidationAlert');
              $submFormValidationAlert.hide();
              if (pvr.common_util.isJapaneseLocal() && _.isEmpty($("#caseSubmissionCommentsJa").val())
                  || !pvr.common_util.isJapaneseLocal() && _.isEmpty($("#caseSubmissionComments").val())) {
                  $submFormValidationAlert.show();
                  return;
              }

              var data = new FormData();
              var form_data = reportSubmissionModal
                .find("form")
                .serializeArray();
              $.each(form_data, function (key, input) {
                if (input.name !== "dueDate" && input.name !== "submissionDate") {
                    data.append(input.name, input.value);
                }
              });
              data.append(
                "password",
                reportSubmissionModal.find("#password-input").val()
              );
              var file_data = $("#file_input").get(0).files;
              for (var i = 0; i < file_data.length; i++) {
                data.append("file", file_data[i]);
              }
              if (file_data.length > 0) {
                $("#file_name").val($("#file_input").get(0).files[0].name);
                data.append("filename", $("#file_name").val());
              }
              data.append(
                "approvalDate",
                reportSubmissionModal.find("#approvalDate").val()
              );
              data.append(
                  "dueDate",
                  formatJapaneseDate($('#dueDate').val())
              );
              data.append(
                  "submissionDate",
                  formatJapaneseDate($('#submissionDate').val())
              );
              data.append(
                  "justificationId",
                  $('#standard-justifications:visible').val() || ''
              );
              $.ajax({
                url: caseSubmitUrl,
                method: "POST",
                data: data,
                processData: false,
                contentType: false,
                mimeType: "multipart/form-data",
                dataType: "json",
              })
                .done(function (result) {
                  reportSubmissionModal.modal("hide");
                  reloadData(rowId);
                  successNotificationForIcsrCaseTracking(result.message);
                })
                .fail(function (err) {
                  var responseText = err.responseText;
                  var responseTextObj = JSON.parse(responseText);
                  if (responseTextObj.message != undefined) {
                    $("#submitErrorMessage").parent().removeClass("hide");
                    $("#submitErrorMessage").html(
                      responseTextObj.message.replace(/,/g, "<br/>")
                    );
                  } else {
                    $("#submitErrorMessage").parent().removeClass("hide");
                    $("#submitErrorMessage").html("Failed due to some reason!");
                  }
                });
            });
        });
    })
    .on("hidden.bs.modal", function () {
      reportSubmissionModal.find(".modal-content:first").html("");
      $("#submitErrorMessage").parent().addClass("hide");
      $("#submitErrorMessage").html("");
    });
  var transmissionModal = $("#transmitJustification");
  transmissionModal
    .on("shown.bs.modal", function (e) {
      var transmitUrl = transmissionModal.data("url");
      var rowId = $(e.relatedTarget).parents("tr").attr("id");
      transmissionModal
        .find("#transmitButton")
        .off()
        .on("click", function () {
          var parameters = transmissionModal
            .find("#justificationForm")
            .serialize();
          parameters +=
            "&password=" +
            encodeURIComponent(transmissionModal.find("#password-input").val());
          parameters +=
            "&approvalDate=" + transmissionModal.find("#approvalDate").val();
          $.ajax({
            url: transmitUrl,
            type: "post",
            data: parameters,
            dataType: "json",
            beforeSend: function () {
              transmissionModal
                .find("#transmitButton")
                .attr("disabled", "disabled");
              showModalLoader(transmissionModal);
            },
          })
            .done(function (data) {
              transmissionModal.modal("hide");
              reloadData(rowId);
              successNotificationForIcsrCaseTracking(data.message);
            })
            .fail(function (err) {
              var responseText = err.responseText;
              var responseTextObj = JSON.parse(responseText);
              if (responseTextObj.message != undefined) {
                $("#transmitErrorMessage").parent().removeClass("hide");
                $("#transmitErrorMessage").html(responseTextObj.message);
              } else {
                $("#transmitErrorMessage").parent().removeClass("hide");
                $("#transmitErrorMessage").html("Failed due to some reason!");
              }
            })
            .always(function () {
              transmissionModal.find("#transmitButton").removeAttr("disabled");
              hideModalLoader(transmissionModal);
            });
          transmissionModal
            .find(".transmitErrorMessageDivClose")
            .off()
            .on("click", function () {
              $("#transmitErrorMessage").parent().addClass("hide");
            });
        });
    })
    .on("hidden.bs.modal", function () {
      transmissionModal.find("#password-input").val("");
      $("#transmitErrorMessage").parent().addClass("hide");
      $("#transmitErrorMessage").html("");
      transmissionModal.find("#transmissionComments").val("");
      transmissionModal.find("#transmitButton").off();
    });

  var nullificationModal = $("#nullificationModal");
  nullificationModal
    .on("shown.bs.modal", function (e) {
      var nullificationUrl = $(e.relatedTarget).data("url");
      nullificationModal
        .find("#nullifyButton")
        .off()
        .on("click", function () {
          var confirmation = $("#notificationComments").val();
          if (confirmation != "" && confirmation.trim().length > 0) {
            showLoader();
            $.ajax({
              type: "GET",
              url: nullificationUrl,
              data: { justification: confirmation },
              dataType: "json",
            })
              .done(function (result) {
                successNotificationForIcsrCaseTracking(result.message);
                var dataTable = $("#rxTableIcsrCaseTracking").DataTable();
                dataTable.ajax.reload(function (data) {}, false).draw();
                nullificationModal.modal("hide");
                hideLoader();
              })
              .fail(function (err) {
                errorNotificationForIcsrCaseTracking(
                  (err.responseJSON.message ? err.responseJSON.message : "") +
                    (err.responseJSON.stackTrace
                      ? "<br>" + err.responseJSON.stackTrace
                      : "")
                );
                hideLoader();
                nullificationModal.modal("hide");
                window.scrollTo(0, 0);
              });
          } else {
            $("#nullificationErrorDiv").show();
          }
        });
      nullificationModal
        .find(".nullificationErrorDivclose")
        .off()
        .on("click", function () {
          $("#nullificationErrorDiv").hide();
        });
    })
    .on("hidden.bs.modal", function () {
      $("#nullificationErrorDiv").hide();
      $("#notificationComments").val("");
    });

  $(document).on("click", ".warningJustificationforTransmit", function () {
    $("#WarningDiv").show();
    $("#WarningDiv p").text($.i18n._("transmit.report"));
    $(".WarningDivclose").on("click", function () {
      $("#WarningDiv").hide();
    });
  });

  $(document).on("click", ".warningJustificationforSubmit", function () {
    $("#WarningDiv").show();
    $("#WarningDiv p").text($.i18n._("submit.report"));
    $(".WarningDivclose").on("click", function () {
      $("#WarningDiv").hide();
    });
  });

  $(document).on("click", ".localCp", function () {
    var confirmationModal = $("#confirmationModal");
    var url = $(this).attr("data-url");
    confirmationModal.modal("show");
    confirmationModal.find(".modalHeader").html($.i18n._("icsr.confirmation"));
    confirmationModal
      .find(".confirmationMessage")
      .html($.i18n._("icsr.confirmation.msg"));
    confirmationModal.find(".okButton").html($.i18n._("continue"));
    confirmationModal
      .find(".okButton")
      .removeClass("btn btn-danger")
      .addClass("btn btn-primary");
    confirmationModal
      .find(".okButton")
      .off()
      .on("click", function () {
        $.ajax({
          type: "POST",
          url: url,
          async: true,
          dataType: "json",
        })
          .done(function (response) {
              $("#confirmationModal").modal("hide");
              successNotificationForIcsrCaseTracking(response.message);
              var dataTable = $("#rxTableIcsrCaseTracking").DataTable();
              dataTable.ajax.reload(function (data) {}, false).draw();
              hideLoader();
          })
          .fail(function (err) {
            errorNotificationForIcsrCaseTracking(
              (err.responseJSON.message ? err.responseJSON.message : "") +
                (err.responseJSON.stackTrace
                  ? "<br>" + err.responseJSON.stackTrace
                  : "")
            );
          });
      });
  });

  $("#emailToModal")
    .on("show.bs.modal", function (e) {
      var rowObj = $(e.relatedTarget);
      $('form[name="emailForm"] #exIcsrTemplateQueryId').val(
        rowObj.data("ex-icsr-template-query-id")
      );
      $('form[name="emailForm"] #caseNumber').val(rowObj.data("case-number"));
      $('form[name="emailForm"] #versionNumber').val(
        rowObj.data("version-number")
      );
      $("#emailUsers").val(null).trigger("change");
      $("#emailUsers").parent().removeClass("has-error");
      $("#formatError").hide();
      $("#emailUsers")
        .parents("div.modal-body")
        .find(".alert-danger")
        .addClass("hide");

      // clear checkbox for attachemnt formats
      $(".emailOption").prop("checked", false);
      emailToModalShow = true;
    })
    .on("hide.bs.modal", function (e) {
      emailToModalShow = false;
      $('form[name="emailForm"] #exIcsrTemplateQueryId').val("");
      $('form[name="emailForm"] #caseNumber').val("");
      $('form[name="emailForm"] #versionNumber').val("");
    });
};

$(document).on("change", "#file_input", function () {
  $("#file_name").val($("#file_input").get(0).files[0].name);
});

function openStateHistoryModal(rowData) {
  var caseNumber = rowData.caseNumber;
  var versionNumber = rowData.versionNumber;
  var profileName = rowData.profileName;
  var exIcsrTemplateQueryId = rowData.exIcsrTemplateQueryId;
  var recipientName = rowData.recipient;
  var followupNumber = rowData.followupNumber;
  var localReportMessage = rowData.localReportMessage;
  var queryName = rowData.queryName;
  var manualFlag = rowData.manualFlag;
  var displayName = manualFlag ? profileName : (queryName ? profileName + " (" + queryName + ")" : profileName);
  $.ajax({
    type: "GET",
    url: submissionHistoryCase,
    data: {
      caseNumber: caseNumber,
      versionNumber: versionNumber,
      profileName: profileName,
      exIcsrTemplateQueryId: exIcsrTemplateQueryId,
    },
    dataType: "json",
  })
    .done(function (result) {
      $("#caseSubmissionData").html("");
      $("#submissionHistoryCase").modal("show");
      $("#submissionHistoryCase span#caseNumber").html(caseNumber);
      $("#submissionHistoryCase span#versionNumber").html(versionNumber);
      $("#submissionHistoryCase span#profileName").html(displayName);
      $("#submissionHistoryCase span#recipientName").html(recipientName);
      $("#submissionHistoryCase span#followupNumber").html(followupNumber);
      if (localReportMessage == null) {
        $("#submissionHistoryCase span#localReportMessage").html("N/A");
      } else {
        $("#submissionHistoryCase span#localReportMessage").html(
          localReportMessage
        );
      }
      if ($.isEmptyObject(result)) {
        var tr =
          '<tr><td colspan="4" style="text-align: center" class="padding">No data found.</td></tr>';
        $("#submissionHistoryCaseTable").append(tr);
        $(".alert-danger").hide(tr);
      } else {
        $.each(result, function (v, k) {
          var tr
            tr += handleAttachmentStatus(k, caseNumber, versionNumber, exIcsrTemplateQueryId)
          $("#submissionHistoryCaseTable").append(tr);
          $(".alert-danger").hide(tr);
        });
      }
    })
    .fail(function (err) {
      var responseText = err.responseText;
      var responseTextObj = JSON.parse(responseText);
      if (responseTextObj.message) {
        return;
      }
    });
}

function removeAbsolutePath(filename) {
    var modifiedFileName;
    if ( filename && filename.indexOf('/incoming') !== -1) {
        modifiedFileName = "..." + filename.substring(filename.indexOf('/incoming'));
    }
    else{
        modifiedFileName = filename;
    }
    return modifiedFileName;
}

function handleAttachmentStatus(v, caseNumber, versionNumber, exIcsrTemplateQueryId) {
    var tr = "<tr>";
    tr = tr + '<td class="padding">';
    if (v.attachmentAckFileName != null) {
        tr =
            tr +
            "&nbsp;" +
            '<a id="checkFileExist" data-ackFileName="' +
            v.attachmentAckFileName +
            '"><i class="fa fa-file fa-sm" title="' +
            removeAbsolutePath(v.attachmentAckFileName) +
            '"></i></a>';
    } else if (v.ackFileName != null) {
        tr =
            tr +
            "&nbsp;" +
            '<a id="checkFileExist" data-ackFileName="' +
            v.ackFileName +
            '"><i class="fa fa-file fa-sm" title="' +
            removeAbsolutePath(v.ackFileName) +
            '"></i></a>';
    }
    if (
        v.submissionDocument == true
    ) {
        tr =
            tr +
            '<a id="downloadDocFile" data-e2bProcessId="' +
            v.e2bProcessId +
            '"><i class="fa fa-paperclip fa-flip-horizontal" data-placement="left" style="cursor: pointer"></i></a>';
    }

    if (v.e2BStatus == IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT) {
        tr =
            tr +
            "&nbsp;" +
            '<a id="downloadMergedPdf" data-exIcsrTemplateQueryId="' +
            exIcsrTemplateQueryId +
            '" data-caseNumber="' +
            caseNumber +
            '" data-versionNumber="' +
            versionNumber +
            '" style="cursor: pointer" title="' +
            $.i18n._("icsr.case.linked.attachment") +
            '"><i class="fa fa-file fa-sm" ></i></a>';
        tr =
            tr +
            " " +
            $.i18n._("icsr.case.tracking.status." + v.e2BStatus) +
            "&nbsp;" +
            '<i class="fa fa-paperclip fa-sm" title="' +
            $.i18n._("icsr.case.tracking.transmitting.attachment") +
            '"></i>' +
            "&nbsp;" +
            '<i class="fa fa-spinner fa-sm" title="' +
            $.i18n._("icsr.case.tracking.transmitting.attachment") +
            '"></i></td>';
    } else if (v.e2BStatus == IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT) {
        tr =
            tr +
            " " +
            $.i18n._("icsr.case.tracking.status." + v.e2BStatus) +
            "&nbsp;" +
            '<i class="fa fa-paperclip fa-sm" title="' +
            $.i18n._("icsr.case.tracking.transmitted.attachment") +
            '"></i></td>';
    } else if (v.attachmentAckFileName != null) {
        tr =
            tr +
            " " +
            $.i18n._("icsr.case.tracking.status." + v.e2BStatus) +
            "&nbsp;" +
            '<i class="fa fa-paperclip fa-sm" title="' +
            $.i18n._("icsr.case.tracking.status." + v.e2BStatus) +
            '"></i></td>';
    } else {
        tr =
            tr +
            " " +
            $.i18n._("icsr.case.tracking.status." + v.e2BStatus) +
            "</td>";
    }
    if (v.statusDate != null) {
        tr =
            tr +
            '<td class="padding" style="white-space: normal;">' +
            moment
                .utc(v.statusDate)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) +
            " (" +
            v.userTimeZone +
            ")" +
            "</td>";
    } else {
        tr = tr + '<td class="padding"></td>';
    }
    if (v.preferredTimeZoneDate != null) {
        tr =
            tr +
            '<td class="padding" style="white-space: normal;">' +
            moment
                .utc(v.preferredTimeZoneDate)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) +
            " (" +
            v.preferredTimeZone +
            ")" +
            "</td>";
    } else {
        tr = tr + '<td class="padding"></td>';
    }
    if (v.lastUpdatedBy != null) {
        tr = tr + '<td class="padding">' + v.lastUpdatedBy + "</td>";
    } else {
        tr = tr + '<td class="padding"></td>';
    }
    var comments = "N/A";
    if (userLocale === JAPANESE_LOCALE && v.commentsJ) {
        comments = v.commentsJ;
    } else if (v.comments != null) {
        comments = v.comments;
    }
    tr =
        tr +
        '<td class="padding">' +
        (comments
            ? comments
                .replace(/"/gi, "&quot;")
                .replace(/</gi, "&lt;")
                .replace(/>/gi, "&gt;")
                .replace(/\n/gi, "<br>")
            : "") +
        "</td>";
    tr = tr + "</tr>";
    return tr;
}

$(document).on("click", "#downloadDocFile", function () {
  var e2bProcessId = $(this).attr("data-e2bProcessId");
  window.location.href = downloadDocFileUrl + "?e2bProcessId=" + e2bProcessId;
});

$(document).on("click", "#downloadMergedPdf", function () {
  var exIcsrTemplateQueryId = $(this).attr("data-exIcsrTemplateQueryId");
  var caseNumber = $(this).attr("data-caseNumber");
  var versionNumber = $(this).attr("data-versionNumber");
  window.location.href =
    downloadMergedPdfUrl +
    "?exIcsrTemplateQueryId=" +
    exIcsrTemplateQueryId +
    "&caseNumber=" +
    caseNumber +
    "&versionNumber=" +
    versionNumber;
});

$(document).on("click", "#checkFileExist", function () {
  var fileName = $(this).attr("data-ackFileName");
  $.ajax({
    url: checkFileExistUrl + "?ackFileName=" + fileName,
    async: true,
    dataType: "json",
  })
    .done(function (data) {
      window.location.href = downloadAckFileUrl + "?ackFileName=" + fileName;
    })
    .fail(function (err) {
      errorNotification(
        err.responseJSON.message ? err.responseJSON.message : ""
      );
    });
});

function errorNotification(message) {
  $(".alert-danger").alert("close");
  if (message != undefined && message != "")
    $(".modal-body").prepend(
      '<div class="alert alert-danger alert-dismissable">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    );
}

function checkSelectionAbility() {
    const maxSelectedCount = 1000;

    if (_.isEmpty(bulkIds)) {
        return true;
    }
    if (bulkIds.length >= maxSelectedCount) {
        $("#bulkUpdateMaxRowsWarning").modal("show");
        return false;
    }
    return true;
}

function updateIcsrTrackingTablePageInfo(table) {
    icsrTrackingTablePageInfo.page = table.page.info().page;
    icsrTrackingTablePageInfo.length = table.page.info().length;
}

function updateIcsrTrackingTablePageInfoSelectAll(selectAllChecked) {
    if (selectAllChecked) {
        icsrTrackingTablePageInfo.pageSelectAllApplied = icsrTrackingTablePageInfo.page;
    } else {
        icsrTrackingTablePageInfo.pageSelectAllApplied = null;
    }
}

function findRowInBulkIds(rowId) {
    return bulkIds.find(function (value) {
        return value.currentIcsrRowId === rowId;
    });
}

var resetBulkIds = function () {
  bulkIds = [];
  $("#rxTableIcsrCaseTracking").trigger('tableSelectedRowsCountChanged', [0]);
  $(".bulkTransmitSubmitChk").prop("checked", false);
  $('thead th #icsrProfileSelectAll').prop("checked", false);
  updateIcsrTrackingTablePageInfoSelectAll(false);
  enableDisableBulkTransmitButton(bulkIds);
  enableDisableBulkSubmitButton(bulkIds);
  enableDisableBulkDownloadButton(bulkIds);
    enableDisableBulkRegenerateButton(bulkIds);
};

var reloadData = function (rowId, resetPagination) {
  if (resetPagination != true) {
    resetPagination = false;
  }
  var dataTable = $("#rxTableIcsrCaseTracking").DataTable();
  dataTable.ajax.reload(function () {
    highlightRow(rowId);
  }, resetPagination);
};

var highlightRow = function (rowId) {
  if (rowId != undefined && rowId != "") {
    var dataTable = $("#rxTableIcsrCaseTracking").DataTable();
    dataTable
      .row("#" + rowId)
      .nodes()
      .to$()
      .addClass("flash-row");
  }
};

function openErrorDetailsModal(rowData) {
  var caseNumber = rowData.caseNumber;
  var versionNumber = rowData.versionNumber;
  var profileName = rowData.profileName;
  var exIcsrTemplateQueryId = rowData.exIcsrTemplateQueryId;
  var ackFileName = rowData.ackFileName;
  var e2BStatus = rowData.e2BStatus;
    var modifiedAckFileName;
    if ( ackFileName && ackFileName.indexOf('/incoming') !== -1) {
        modifiedAckFileName = "..." + ackFileName.substring(ackFileName.indexOf('/incoming'));
    }
    else{
        modifiedAckFileName = ackFileName;
    }
  $.ajax({
    type: "POST",
    url: caseErrorDetails,
    data: {
      caseNumber: caseNumber,
      versionNumber: versionNumber,
      profileName: profileName,
      exIcsrTemplateQueryId: exIcsrTemplateQueryId,
      status: e2BStatus,
    },
    dataType: "html",
  })
    .done(function (result) {
      $("#errorDetailsData").html("");
      $("#errorDetails").modal("show");
      $("#errorDetailCaseNumber").text(caseNumber);
      $("#errorDetailVersionNumber").text(versionNumber);
        $("#errorAckFileName").text(modifiedAckFileName);
      if ($.isEmptyObject(result)) {
        var tr =
          '<tr><td colspan="5" style="text-align: center" class="padding">No data found.</td></tr>';
        $("#errorDetailsTable").append(tr);
      } else {
        var tr = "<tr>";
        if (result != null) {
          tr = tr + '<td class="padding">' + result + "</td>";
        } else {
          tr = tr + '<td class="padding">No data found.</td>';
        }
        $("#errorDetailsTable").append(tr);
      }
    })
    .fail(function (err) {
      var responseText = err.responseText;
      var responseTextObj = JSON.parse(responseText);
      if (responseTextObj.message) {
        return;
      }
    });
}

function openCaseDetailsModal(rowData) {
  var caseNumber = rowData.caseNumber;
  var versionNumber = rowData.versionNumber;
  var profileName = rowData.profileName;
  $.ajax({
    type: "GET",
    url: caseHistoryData,
    data: { caseNumber: caseNumber, versionNumber: versionNumber },
    dataType: "json",
  })
    .done(function (result) {
      $("#caseHistoryData").html("");
      $("#caseHistoryDetails").modal("show");
      $("#caseDetailsCaseNumber").text(caseNumber);
      $("#caseDetailsVersionNumber").text(versionNumber);
      if ($.isEmptyObject(result)) {
        var tr =
          '<tr><td colspan="5" style="text-align: center" class="padding">No data found.</td></tr>';
        $("#caseHistoryTable").append(tr);
      } else {
        $.map(result, function (v, k) {
          var tr = "<tr>";
          if (v.reportDestination != null) {
            tr = tr + '<td class="padding">' + v.reportDestination + "</td>";
          } else {
            tr = tr + '<td class="padding"></td>';
          }
          tr =
            tr +
            '<td class="padding">' +
            $.i18n._("icsr.case.tracking.status." + v.e2BStatus);
          if (v.ackFileName != null) {
            tr =
              tr +
              ' <i class="fa fa-file fa-sm" title="' +
              removeAbsolutePath(v.ackFileName) +
              '"></i>';
          }
          tr = tr + "</td>";
          if (v.statusDate != null) {
            tr =
              tr +
              '<td class="padding">' +
              moment
                .utc(v.statusDate)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) +
              "</td>";
          } else {
            tr = tr + '<td class="padding"></td>';
          }
          if (v.lastUpdatedBy != null) {
            tr = tr + '<td class="padding">' + v.lastUpdatedBy + "</td>";
          } else {
            tr = tr + '<td class="padding"></td>';
          }
          if (v.lastUpdateDate != null) {
            tr =
              tr +
              '<td class="padding">' +
              moment
                .utc(v.lastUpdateDate)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) +
              "</td>";
          } else {
            tr = tr + '<td class="padding"></td>';
          }
          tr = tr + "</tr>";
          $("#caseHistoryTable").append(tr);
        });
      }
    })
    .fail(function (err) {
      var responseText = err.responseText;
      var responseTextObj = JSON.parse(responseText);
      if (responseTextObj.message) {
        return;
      }
    });
}

function downloadCasesReport(table, url, reportSpec) {
  var rows = table.rows({ selected: true }).data();
  var caseNumbers = [];
  for (var i = 0; i < rows.length; i++) {
    caseNumbers.push(rows[i].exIcsrTemplateQueryId + ";" + rows[i].caseNumber);
  }
  var params = { caseNumber: caseNumbers, reportSpec: reportSpec };
  pvr.common_util.downloadFile(url + "?" + $.param(params));
}

function bindBulkCheckboxEvent() {
  $(document).on("change", ".bulkTransmitSubmitChk", function () {
    var value = $(this).val();
    var status = $(this).attr("data-id");
    var templateId = $(this).data("templateId");
    var profileName = $(this).data("profileName");
    var caseNumber = $(this).data("caseNumber");
    var versionNumber = $(this).data("versionNumber");
    var recipient = $(this).data("recipient");
    var preferredTimeZone = $(this).data("preferredTimeZone");
    var currentIcsrRowId = $(this).attr("data-icsrRowId");
    if ($(this).is(":checked")) {
        if (checkSelectionAbility()) {
            bulkIds.push({
                id: value,
                status: status,
                recipient: recipient,
                templateId: templateId,
                profileName: profileName,
                caseNumber: caseNumber,
                versionNumber: versionNumber,
                preferredTimeZone: preferredTimeZone,
                currentIcsrRowId: currentIcsrRowId
            });
        } else {
            $(this).prop('checked', false);
        }
    } else {
      var index = _.findIndex(bulkIds, function (item) {
        return item.id == value;
      });
      if (index > -1) bulkIds.splice(index, 1);
    }

    $(this).trigger('tableSelectedRowsCountChanged', [(bulkIds ? bulkIds.length : 0)]);

    enableDisableBulkTransmitButton(bulkIds);
    enableDisableBulkSubmitButton(bulkIds);
    enableDisableBulkDownloadButton(bulkIds);
    enableDisableBulkRegenerateButton(bulkIds);
    updateIcsrProfileSelectAllState();
  });

  var bulkTransmissionModal = $("#bulkTransmitJustification");
  bulkTransmissionModal
    .on("shown.bs.modal", function (e) {
      var transmitUrl = bulkTransmissionUrl;
      bulkTransmissionModal
        .find("#bulkTransmitConfirmButton")
        .off()
        .on("click", function () {
          var parameters = bulkTransmissionModal
            .find("#bulkJustificationForm")
            .serialize();
          parameters +=
            "&password=" +
            encodeURIComponent(
              bulkTransmissionModal
                .find("#bulk-transmission-password-input")
                .val()
            );
          parameters +=
            "&approvalDate=" +
            bulkTransmissionModal.find("#approvalDate").val();
          parameters += "&transmitIds=" + JSON.stringify(bulkIds);
          var confirmation = $("#bulkTransmissionComments").val();
          $.ajax({
            url: transmitUrl,
            type: "post",
            data: parameters,
            dataType: "json",
            beforeSend: function () {
              bulkTransmissionModal
                .find("#bulkTransmitConfirmButton")
                .attr("disabled", "disabled");
              showModalLoader(bulkTransmissionModal);
            },
          })
            .done(function (data) {
              bulkTransmissionModal.modal("hide");
              reloadData();
              resetBulkIds();
              successNotificationForIcsrCaseTracking(data.message);
            })
            .fail(function (err) {
              var responseText = err.responseText;
              var responseTextObj = JSON.parse(responseText);
              if (responseTextObj.message != undefined) {
                $("#bulkTransmitErrorMessage").parent().removeClass("hide");
                $("#bulkTransmitErrorMessage").html(responseTextObj.message);
              } else {
                $("#bulkTransmitErrorMessage").parent().removeClass("hide");
                $("#bulkTransmitErrorMessage").html(
                  "Failed due to some reason!"
                );
              }
            })
            .always(function () {
              bulkTransmissionModal
                .find("#bulkTransmitConfirmButton")
                .removeAttr("disabled");
              hideModalLoader(bulkTransmissionModal);
            });
        });
    })
    .on("hidden.bs.modal", function () {
      bulkTransmissionModal.find("#bulk-transmission-password-input").val("");
      $("#bulkTransmitErrorMessage").parent().addClass("hide");
      $("#bulkTransmitErrorMessage").html("");
      bulkTransmissionModal.find("#bulkTransmissionComments").val("");
      bulkTransmissionModal.find("#bulkTransmitConfirmButton").off();
      $("#bulkTransmissionErrorDiv").hide();
    });

  var bulkReportSubmissionModal = $("#bulkReportSubmissionModal");
  bulkReportSubmissionModal
    .on("shown.bs.modal", function (e) {
      showModalLoader(bulkReportSubmissionModal);
      var url = bulkMarkAsSubmittedURL;
      var ids = [];
      var recipient = bulkIds[0].recipient;
      var preferredTimeZone = bulkIds[0].preferredTimeZone;
      _.each(bulkIds, function (item) {
        ids.push(item.id);
      });
      url +=
        "?bulkIds=" +
        ids +
        "&recipient=" +
        encodeURI(recipient) +
        "&preferredTimeZone=" +
        encodeURI(preferredTimeZone);
      bulkReportSubmissionModal
        .find(".modal-content:first")
        .load(url, function () {
          hideModalLoader(bulkReportSubmissionModal);
          // buildReportingDestinationsSelectBox(reportSubmissionModal.find("[name=reportingDestinations]"), periodicReportConfig.reportingDestinationsUrl, reportSubmissionModal.find("input[name='primaryReportingDestination']"), false);
          var submissionDate = bulkReportSubmissionModal
            .find('.datepicker input[name="submissionDate"]')
            .val();
          $("#submissionDateDiv").datepicker({
            allowPastDates: true,
            date: submissionDate,
            twoDigitYearProtection: true,
            culture: userLocale,
            momentConfig: {
              format: DEFAULT_DATE_DISPLAY_FORMAT,
            },
          });
          bulkReportSubmissionModal
            .find("button.submit-draft")
            .off()
            .on("click", function () {
              const $submFormValidationAlert = $('#icsrTrackingBulkSubmFormValidationAlert');
              $submFormValidationAlert.hide();
              if (pvr.common_util.isJapaneseLocal() && _.isEmpty($("#bulkCaseSubmissionCommentsJa").val())
                  || !pvr.common_util.isJapaneseLocal() && _.isEmpty($("#bulkCaseSubmissionComments").val())) {
                  $submFormValidationAlert.show();
                  return;
              }

              var data = new FormData();
              var form_data = bulkReportSubmissionModal
                .find("form")
                .serializeArray();
              $.each(form_data, function (key, input) {
                if (input.name !== "submissionDate") {
                    data.append(input.name, input.value);
                }
              });
              data.append(
                "password",
                bulkReportSubmissionModal
                  .find("#bulk-submission-password-input")
                  .val()
              );
              var file_data = $("#bulk_submission_file_input").get(0).files;
              for (var i = 0; i < file_data.length; i++) {
                data.append("file", file_data[i]);
              }
              if (file_data.length > 0) {
                $("#file_name").val($("#file_input").get(0).files[0].name);
                data.append("filename", $("#file_name").val());
              }
              data.append(
                "approvalDate",
                bulkReportSubmissionModal.find("#approvalDate").val()
              );
              data.append(
                  "submissionDate",
                  formatJapaneseDate($('#submissionDate').val())
              );
              data.append(
                  "justificationId",
                  $('#standard-justifications:visible').val() || ''
              );
              $.ajax({
                url: bulkCaseSubmitUrl,
                method: "POST",
                data: data,
                processData: false,
                contentType: false,
                mimeType: "multipart/form-data",
                dataType: "json",
              })
                .done(function (result) {
                  bulkReportSubmissionModal.modal("hide");
                  reloadData();
                  resetBulkIds();
                  successNotificationForIcsrCaseTracking(result.message);
                })
                .fail(function (err) {
                  var responseText = err.responseText;
                  var responseTextObj = JSON.parse(responseText);
                  if (responseTextObj.message != undefined) {
                    $("#bulkSubmitErrorMessage").parent().removeClass("hide");
                    $("#bulkSubmitErrorMessage").html(
                      responseTextObj.message.replace(/,/g, "<br/>")
                    );
                  } else {
                    $("#bulkSubmitErrorMessage").parent().removeClass("hide");
                    $("#bulkSubmitErrorMessage").html(
                      "Failed due to some reason!"
                    );
                  }
                });
            });
        });
    })
    .on("hidden.bs.modal", function () {
      bulkReportSubmissionModal.find("#bulk-submission-password-input").val("");
      bulkReportSubmissionModal.find(".modal-content:first").html("");
      $("#bulkSubmitErrorMessage").parent().addClass("hide");
      $("#bulkSubmitErrorMessage").html("");
    });

    var bulkRegenerateCaseModal = $("#bulkRegenerateCaseModal");
    bulkRegenerateCaseModal.on('shown.bs.modal', function (e) {
        bulkRegenerateCaseModal.find('#bulkRegenerateCaseModalLabel').text("");
        bulkRegenerateCaseModal.find('#bulkRegenerateCaseModalLabel').text($.i18n._('modal.regenerate.title'));
        bulkRegenerateCaseModal.find("#bulkRegenerateButton").off().on("click", function () {
            var confirmation = $("#bulkRegenerateComments").val();
            var regenerateIds = JSON.stringify(bulkIds);
            if (confirmation != "" && confirmation.trim().length > 0) {
                $.ajax({
                    type: "POST",
                    url: bulkRegenerateCaseUrl,
                    data: {bulkRegenerateComment: confirmation ,regenerateIds: regenerateIds},
                    success: function (result) {
                        bulkRegenerateCaseModal.modal("hide");
                        reloadData();
                        resetBulkIds();
                        successNotificationForIcsrCaseTracking(result.message);
                    }, error: function (err) {
                        var responseText = err.responseText;
                        var responseTextObj = JSON.parse(responseText);
                        if (responseTextObj.message != undefined) {
                            $("#bulkRegenerateErrorMessageDiv").parent().removeClass('hide');
                            $("#bulkRegenerateErrorMessageDiv").html(responseTextObj.message.replace(/,/g, "<br/>"));
                        } else {
                            $("#bulkRegenerateErrorMessageDiv").parent().removeClass('hide');
                            $("#bulkRegenerateErrorMessageDiv").html("Failed due to some reason!");
                        }
                    },
                    beforeSend: function () {
                        bulkRegenerateCaseModal.find("#bulkRegenerateButton").attr('disabled', 'disabled');
                        showModalLoader(bulkRegenerateCaseModal);
                    },
                    complete: function () {
                        bulkRegenerateCaseModal.find("#bulkRegenerateButton").removeAttr('disabled');
                        hideModalLoader(bulkRegenerateCaseModal);
                    }
                });
            }else {
                $('#bulkRegenerateErrorMessageDiv').show();
            }
        });
        bulkRegenerateCaseModal.find('.bulkRegenerateErrorMessageDivClose').off().on('click', function () {
            $('#bulkRegenerateErrorMessageDiv').hide();
        });

    }).on("hidden.bs.modal", function () {
        $('#bulkRegenerateErrorMessageDiv').hide();
        bulkRegenerateCaseModal.find("#bulkRegenerateComments").val('');
        bulkRegenerateCaseModal.find('#bulkRegenerateButton').off();
    });

}

$(document).on("click", ".bulkDownloadButton", function () {
  var bulkDownloadConfirmationModal = $("#bulkDownloadConfirmationModal");
  bulkDownloadConfirmationModal
    .find(".modalHeader")
    .html($.i18n._("app.bulk.download.confirmation.title"));
  bulkDownloadConfirmationModal.find(".okButton").html($.i18n._("yes"));
  bulkDownloadConfirmationModal
    .find(".confirmationMessage")
    .html(
      $.i18n._(
        "app.bulk.download.confirmation.message",
        encodeToHTML(bulkIds.length)
      )
    );
  var parameters =
    "&data=" +
    JSON.stringify(
      bulkIds.map(function (item) {
        return item.id;
      })
    );
  bulkDownloadConfirmationModal
    .find(".okButton")
    .off()
    .on("click", function () {
      $.ajax({
        url: bulkDownloadReportsUrl,
        type: "post",
        data: parameters,
        dataType: "json",
      })
        .done(function (result) {
          bulkDownloadConfirmationModal.modal("hide");
          reloadData();
          resetBulkIds();
          successNotificationForIcsrCaseTracking(result.message);
        })
        .fail(function (err) {
          bulkDownloadConfirmationModal.modal("hide");
          reloadData();
          resetBulkIds();
          errorNotificationForIcsrCaseTracking(
            (err.responseJSON.message ? err.responseJSON.message : "") +
              (err.responseJSON.stackTrace
                ? "<br>" + err.responseJSON.stackTrace
                : "")
          );
        });
    });
});

function enableDisableBulkTransmitButton(bulkIds) {
  var eligibleForTransmit = true;
  if (bulkIds.length == 0) {
    eligibleForTransmit = false;
  } else {
    $.each(bulkIds, function (i, obj) {
      if (obj.status !== "GENERATED") {
        eligibleForTransmit = false;
        return false;
      }
    });
  }
  if (eligibleForTransmit == true) {
    $(".bulkTransmitButton").prop("disabled", false);
    $(".bulkTransmitButton").removeClass("custom-cursor");
  } else {
    $(".bulkTransmitButton").prop("disabled", true);
    $(".bulkTransmitButton").addClass("custom-cursor");
  }
}

function enableDisableBulkSubmitButton(bulkIds) {
  var eligibleForSubmission = true;
  if (bulkIds.length == 0) {
    eligibleForSubmission = false;
  } else {
    $.each(bulkIds, function (i, obj) {
      if (
        obj.status !== "TRANSMITTED" ||
        obj.recipient !== bulkIds[0].recipient ||
        obj.preferredTimeZone !== bulkIds[0].preferredTimeZone
      ) {
        eligibleForSubmission = false;
        return false;
      }
    });
  }
  if (eligibleForSubmission == true) {
    $(".bulkSubmitButton").prop("disabled", false);
    $(".bulkSubmitButton").removeClass("custom-cursor");
  } else {
    $(".bulkSubmitButton").prop("disabled", true);
    $(".bulkSubmitButton").addClass("custom-cursor");
  }
}

function enableDisableBulkDownloadButton(bulkIds) {
  var eligibleForDownload = true;
  if (bulkIds.length == 0) {
    eligibleForDownload = false;
  } else {
    $.each(bulkIds, function (i, obj) {
        if (obj.status == "SCHEDULED" || obj.status == "ERROR" || obj.status == "TRANSMISSION_ERROR" || obj.status == "GENERATION_ERROR" || obj.status == "PARSER_REJECTED") {
        eligibleForDownload = false;
        return false;
      }
    });
  }
  if (eligibleForDownload == true) {
    $(".bulkDownloadButton").prop("disabled", false);
    $(".bulkDownloadButton").removeClass("custom-cursor");
  } else {
    $(".bulkDownloadButton").prop("disabled", true);
    $(".bulkDownloadButton").addClass("custom-cursor");
  }
}

function enableDisableBulkRegenerateButton(bulkIds) {
    var eligibleForRegenerate = true;
    if (bulkIds.length == 0) {
        eligibleForRegenerate = false;
    } else {
        $.each(bulkIds, function (i, obj) {
            if (obj.status !== IcsrCaseStateEnum.GENERATED && obj.status !== IcsrCaseStateEnum.GENERATION_ERROR && obj.status !== IcsrCaseStateEnum.COMMIT_REJECTED && obj.status !== IcsrCaseStateEnum.PARSER_REJECTED && obj.status !== IcsrCaseStateEnum.TRANSMISSION_ERROR) {
                eligibleForRegenerate = false;
                return false;
            }
        });
    }
    if (eligibleForRegenerate == true) {
        $(".bulkRegenerateButton").prop('disabled', false);
        $(".bulkRegenerateButton").removeClass('custom-cursor');
    } else {
        $(".bulkRegenerateButton").prop('disabled', true);
        $(".bulkRegenerateButton").addClass('custom-cursor');
    }

}

async function openSubmissionHistoryFormModal(rowData) {
    if (!rowData.showReportLink) {
        return;
    }
    var reportSubmissionModal = $("#reportSubmissionHistoryModal");
    reportSubmissionModal.modal("show");
    reportSubmissionModal
        .one("shown.bs.modal", async function (e) {
            var rowId = rowData.id;
            var icsrTempQueryId = rowData.exIcsrTemplateQueryId;
            var caseNumber = rowData.caseNumber;
            var versionNumber = rowData.versionNumber;
            var url = markAsSubmittedUrlNew + '?icsrTempQueryId=' + icsrTempQueryId + '&caseNumber=' + caseNumber + '&versionNumber=' + versionNumber + '&noSubmisson=false';
            showModalLoader(reportSubmissionModal);
            try {
                const html = await $.get(url);
                reportSubmissionModal.find(".modal-content:first").html(html);

                var currentState = reportSubmissionModal
                    .find('select[name="icsrCaseStateChanged"]')
                    .val();
                toggleSubmissionDateField(currentState);
                toggleMyStartTimeField(currentState);
                toggleTimezoneField(currentState);

                var submissionDate = reportSubmissionModal
                    .find('.datepicker input[name="submissionDate"]')
                    .val();
                $("#submissionDateDiv").datepicker({
                    allowPastDates: true,
                    date: submissionDate,
                    twoDigitYearProtection: true,
                    culture: userLocale,
                    momentConfig: {
                        format: DEFAULT_DATE_DISPLAY_FORMAT,
                    },
                });

                var dueDate = reportSubmissionModal
                    .find('.datepicker input[name="dueDate"]')
                    .val();
                $("#dueDateDiv").datepicker({
                    allowPastDates: true,
                    date: dueDate,
                    twoDigitYearProtection: true,
                    culture: userLocale,
                    momentConfig: {
                        format: DEFAULT_DATE_DISPLAY_FORMAT,
                    },
                });
                initialSubmissionDate = $('#submissionDate').val();
                initialSubmissionDateNew = $('#submissionDateNew').val();
                // $('[name=icsrCaseStateChanged]').trigger('change');
                reportSubmissionModal
                    .find("button.new-submit-draft")
                    .off("click")
                    .on("click", function () {
                        var data = new FormData();
                        var form_data = reportSubmissionModal
                            .find("form")
                            .serializeArray();

                        // const $submFormValidationAlert = $('#icsrTrackingSubmFormValidationAlert');
                        // $submFormValidationAlert.hide();
                        // if (pvr.common_util.isJapaneseLocal() && _.isEmpty($("#caseSubmissionCommentsJa").val())
                        //     || !pvr.common_util.isJapaneseLocal() && _.isEmpty($("#caseSubmissionComments").val())) {
                        //     $submFormValidationAlert.show();
                        //     return;
                        // }
                        var comment = $("#caseSubmissionComments").val().trim();
                        if (!comment) {
                            var $errorAlert = $(".commentAlert.alert-danger");
                            $errorAlert.removeClass("hide");
                            $("#submitErrorMessage").html($.i18n._("app.label.comment.required"));
                            return;
                        }
                        $(".commentAlert.alert-danger").addClass("hide");
                        $("#submitErrorMessage").html("");

                        $.each(form_data, function (key, input) {
                            if (input.name !== "dueDate" && input.name !== "submissionDate") {
                                data.append(input.name, input.value);
                            }
                        });
                        data.append(
                            "password",
                            reportSubmissionModal.find("#password-input").val()
                        );
                        var file_data = $("#file_input").get(0).files;
                        for (var i = 0; i < file_data.length; i++) {
                            data.append("file", file_data[i]);
                        }
                        if (file_data.length > 0) {
                            $("#file_name").val($("#file_input").get(0).files[0].name);
                            data.append("filename", $("#file_name").val());
                        }
                        data.append(
                            "approvalDate",
                            reportSubmissionModal.find("#approvalDate").val()
                        );
                        data.append(
                            "dueDate",
                            formatJapaneseDate($('#dueDate').val())
                        );
                        data.append(
                            "submissionDate",
                            formatJapaneseDate($('#submissionDate').val())
                        );
                        // data.append(
                        //     "justificationId",
                        //     $('#standard-justifications:visible').val() || ''
                        // );
                        data.append(
                            "justificationId",
                            null
                        );
                        $.ajax({
                            url: statusSubmitUrl,
                            method: "POST",
                            data: data,
                            processData: false,
                            contentType: false,
                            mimeType: "multipart/form-data",
                            dataType: "json",
                        })
                            .done(function (result) {
                                reportSubmissionModal.modal("hide");
                                reloadData();
                                successNotificationForIcsrCaseTracking(result.message);
                            })
                            .fail(function (err) {
                                var responseText = err.responseText;
                                var responseTextObj = JSON.parse(responseText);
                                if (responseTextObj.message != undefined) {
                                    $("#submitErrorMessage").parent().removeClass("hide");
                                    $("#submitErrorMessage").html(
                                        responseTextObj.message.replace(/,/g, "<br/>")
                                    );
                                } else {
                                    $("#submitErrorMessage").parent().removeClass("hide");
                                    $("#submitErrorMessage").html("Failed due to some reason!");
                                }
                            });
                    });
                await openStateHistorySubmissionModal(rowData);
            } catch (err) {
                console.error("Failed to load modal content or state history:", err);
            } finally {
                hideModalLoader(reportSubmissionModal);
            }
        });
    reportSubmissionModal
        .one("hidden.bs.modal", function () {
            reportSubmissionModal.find(".modal-content:first").html(""); // Clear content
            $("#submitErrorMessage").parent().addClass("hide");
            $("#submitErrorMessage").html("");
            reportSubmissionModal.off("shown.bs.modal");
        });
}

function openStateHistorySubmissionModal(rowData) {
    var caseNumber = rowData.caseNumber;
    var versionNumber = rowData.versionNumber;
    var profileName = rowData.profileName;
    var exIcsrTemplateQueryId = rowData.exIcsrTemplateQueryId;
    var recipientName = rowData.recipient;
    var followupNumber = rowData.followupNumber;
    var localReportMessage = rowData.localReportMessage;
    var queryName = rowData.queryName;
    var manualFlag = rowData.manualFlag;
    var displayName = manualFlag ? profileName : (queryName ? profileName + " (" + queryName + ")" : profileName);
    return $.ajax({
        type: "GET",
        url: submissionHistoryCase,
        data: {
            caseNumber: caseNumber,
            versionNumber: versionNumber,
            profileName: profileName,
            exIcsrTemplateQueryId: exIcsrTemplateQueryId,
        },
        dataType: "json",
    })
        .done(function (result) {
            $("#newCaseSubmissionData").html("");
            // $("#newSubmissionHistoryCase").modal("show");
            $("#newSubmissionHistoryCase span#caseNumber").html(caseNumber);
            $("#newSubmissionHistoryCase span#versionNumber").html(versionNumber);
            $("#newSubmissionHistoryCase span#profileName").html(displayName);
            $("#newSubmissionHistoryCase span#recipientName").html(recipientName);
            $("#newSubmissionHistoryCase span#followupNumber").html(followupNumber);
            if (localReportMessage == null) {
                $("#newSubmissionHistoryCase span#localReportMessage").html("N/A");
            } else {
                $("#newSubmissionHistoryCase span#localReportMessage").html(
                    localReportMessage
                );
            }
            if ($.isEmptyObject(result)) {
                var tr =
                    '<tr><td colspan="4" style="text-align: center" class="padding">No data found.</td></tr>';
                $("#newSubmissionHistoryCaseTable").append(tr);
                $(".alert-danger").hide(tr);
            } else {
                $.each(result, function (v, k) {
                    var tr
                    tr += handleAttachmentStatus(k, caseNumber, versionNumber, exIcsrTemplateQueryId)
                    $("#newSubmissionHistoryCaseTable").append(tr);
                    $(".alert-danger").hide(tr);
                });
            }
            retryDataLoad(rowData);
        })
        .fail(function (err) {
            var responseText = err.responseText;
            var responseTextObj = JSON.parse(responseText);
            if (responseTextObj.message) {
                return;
            }
        });
}

function retryDataLoad(rowData) {
    // Check if the table is populated with rows
    var tableContent = $("#newSubmissionHistoryCaseTable").children().length;

    if (tableContent === 0) {
        // No rows, retry after 1 second (1000ms)
        setTimeout(function() {
            openStateHistorySubmissionModal(rowData);
        }, 1000);
    }
}

// Function to enable or disable the submissionDate field based on currentState
function toggleSubmissionDateField(currentState) {
    var $submissionDateInput = $("input[name='submissionDate']");
    var $calendarButton = $submissionDateInput
        .closest(".input-group")
        .find("button[data-toggle='dropdown']");

    if (currentState === "SUBMITTED") {
        // Enable the field and calendar button
        $submissionDateInput.prop("readonly", false); // Allow typing
        $submissionDateInput.css("pointer-events", "auto"); // Allow clicks
        $submissionDateInput.removeAttr("tabindex"); // Allow tab focus
        $calendarButton.prop("disabled", false);     // Allow calendar popup
    } else {
        // Disable the field and calendar button
        $submissionDateInput.prop("readonly", true);  // Prevent typing
        $submissionDateInput.css("pointer-events", "none"); // Block all clicks
        $submissionDateInput.attr("tabindex", "-1"); // Prevent tab focus
        $calendarButton.prop("disabled", true);      // Disable calendar popup
    }
}

function toggleMyStartTimeField(currentState) {
    var $myStartTimeInput = $("input[name='myStartTime']");
    var $dropdownButton = $myStartTimeInput
        .closest(".input-group")
        .find("button[data-toggle='dropdown']");

    if (currentState === "SUBMITTED") {
        // Enable the field and dropdown button
        $myStartTimeInput.prop("readonly", false); // Allow typing
        $myStartTimeInput.css("pointer-events", "auto"); // Allow clicks
        $myStartTimeInput.removeAttr("tabindex"); // Allow tab focus
        $dropdownButton.prop("disabled", false); // Allow dropdown popup
    } else {
        // Disable the field and dropdown button
        $myStartTimeInput.prop("readonly", true); // Prevent typing
        $myStartTimeInput.css("pointer-events", "none"); // Block all clicks
        $myStartTimeInput.attr("tabindex", "-1"); // Prevent tab focus
        $dropdownButton.prop("disabled", true); // Disable dropdown popup
    }
}

function toggleTimezoneField(currentState) {
    var $timezoneButton = $(".timezone-button");
    var $timezoneInput = $("#timezoneSelect");

    if (currentState === "SUBMITTED") {
        // Enable the timezone dropdown
        $timezoneButton.prop("disabled", false); // Enable the dropdown button
        $timezoneButton.css("pointer-events", "auto"); // Allow clicking
        $timezoneInput.prop("readonly", false); // Enable the hidden input
    } else {
        // Disable the timezone dropdown
        $timezoneButton.prop("disabled", true); // Disable the dropdown button
        $timezoneButton.css("pointer-events", "none"); // Block clicking
        $timezoneInput.prop("readonly", true); // Make the hidden input readonly
    }
}

function toogleDateFieldData(currentState) {
    $('#submissionDate').val(initialSubmissionDate)
    $('#submissionDateNew').val(initialSubmissionDateNew)
    initialTimeZone = $('#myScheduler').scheduler('value').timeZone;
    var formattedTime
    if(initialSubmissionDateNew == null)
        formattedTime = null
    else
        formattedTime = moment(initialSubmissionDateNew, "YYYY-MM-DD HH:mm:ss").format("hh:mm A");
    if(formattedTime)
        $('#myStartTime').val(formattedTime);
    if(currentState === "SUBMITTED"){
        var submissionDate = $('#submissionDate').val();
        var tzString = $("#timezoneFromServer").val();
        if (tzString) {
            var timeZoneData = tzString.split(",");
            var name = timeZoneData[0].split(":")[1].trim();
        }
        submissionDate = formatJapaneseDate(submissionDate);
        var startTime = $('#myStartTime').val();
        var formattedDateTime = moment.tz(submissionDate + " " + startTime, "DD-MMM-YYYY hh:mm A", name)
            .format("YYYY-MM-DDTHH:mmZ");
        var schedulerInfo = $('#myScheduler').scheduler('value');
        schedulerInfo.startDateTime = formattedDateTime;
        schedulerInfo.timeZone = initialTimeZone
        scheduleDateJSONDefault = JSON.stringify(schedulerInfo);
        $('#scheduleDateJSON').val(scheduleDateJSONDefault);
    } else{
        var schedulerInfo = $('#myScheduler').scheduler('value');
        schedulerInfo.startDateTime = null;
        schedulerInfo.timeZone = null;
        var scheduleDateJSONDefault = JSON.stringify(schedulerInfo);
        $('#scheduleDateJSON').val(scheduleDateJSONDefault);
        $('#submissionDate').val('');
        $('#myStartTime').val('');
        $("#timezoneFromServer").val('');
    }
}

$(document).on('change', '.icsrCaseStateChanged', function (){
    var currentState = $(this).val();
    $('#icsrCaseState').val(currentState);
    toggleSubmissionDateField(currentState);
    toggleMyStartTimeField(currentState);
    toggleTimezoneField(currentState);
    toogleDateFieldData(currentState)
});

$(document).on('change', '[name=icsrCaseStateChanged]', function (){
    const state = ($(this).val() || '').toUpperCase();
    if (state === 'SUBMISSION_NOT_REQUIRED' || state === 'SUBMISSION_NOT_REQUIRED_FINAL') {
        $(document).trigger('loadStandardJustifications', ['ICSRSubmissionNotRequired']);
    } else if (state === 'SUBMITTED') {
        $(document).trigger('loadStandardJustifications', ['ICSRSubmit']);
    } else {
        $(document).trigger('hideStandardJustifications');
    }
});

$(document).on("change", "#icsrProfileSelectAll", function () {
  var checkboxes = $("#rxTableIcsrCaseTracking .bulkTransmitSubmitChk").get();
  const checked = $(this)[0].checked;
  if (checked) {
    bulkIds = [];
    $(".bulkTransmitSubmitChk").prop("checked", true);
    for (var i = 0; i < checkboxes.length; i++) {
      var value = checkboxes[i].value;
      var status = checkboxes[i].getAttribute("data-id");
      var templateId = checkboxes[i].getAttribute("data-template-id");
      var profileName = checkboxes[i].getAttribute("data-profile-name");
      var caseNumber = checkboxes[i].getAttribute("data-case-number");
      var versionNumber = checkboxes[i].getAttribute("data-version-number");
      var recipient = checkboxes[i].getAttribute("data-recipient");
      var preferredTimeZone = checkboxes[i].getAttribute(
        "data-preferred-time-zone"
      );
      var currentIcsrRowId = checkboxes[i].getAttribute("data-icsrrowid");
      bulkIds.push({
        id: value,
        status: status,
        recipient: recipient,
        templateId: templateId,
        profileName: profileName,
        caseNumber: caseNumber,
        versionNumber: versionNumber,
        preferredTimeZone: preferredTimeZone,
        currentIcsrRowId: currentIcsrRowId,
      });
    }
  } else {
    resetBulkIds();
  }

  $(this).trigger('tableSelectedRowsCountChanged', [(bulkIds ? bulkIds.length : 0)]);

  updateIcsrTrackingTablePageInfoSelectAll(checked);

  enableDisableBulkTransmitButton(bulkIds);
  enableDisableBulkSubmitButton(bulkIds);
  enableDisableBulkDownloadButton(bulkIds);
    enableDisableBulkRegenerateButton(bulkIds);
});

function updateIcsrProfileSelectAllState() {
  var checkboxes = $("#rxTableIcsrCaseTracking .bulkTransmitSubmitChk").get();
  var allRowsSelected = true;
  for (var i = 0; i < checkboxes.length; i++) {
    var checkboxCurrentIcsrRowId = checkboxes[i].getAttribute("data-icsrrowid");

    if (!findRowInBulkIds(checkboxCurrentIcsrRowId)) {
      allRowsSelected = false;
      break;
    }
  }
    const $icsrProfileSelectAll = $('thead th #icsrProfileSelectAll');
    const pageSelectAllApplied = icsrTrackingTablePageInfo.pageSelectAllApplied;

    $icsrProfileSelectAll.prop("checked", allRowsSelected && icsrTrackingTablePageInfo.page === pageSelectAllApplied);

    if (!allRowsSelected && icsrTrackingTablePageInfo.page === pageSelectAllApplied) {
        updateIcsrTrackingTablePageInfoSelectAll(false);
    }

    if ((pageSelectAllApplied || pageSelectAllApplied === 0)
        && icsrTrackingTablePageInfo.page !== pageSelectAllApplied) {
        $icsrProfileSelectAll.attr('disabled', true);
    } else {
        $icsrProfileSelectAll.attr('disabled', false);
    }
}

function hideAlert(element) {
    $(element).closest(".commentAlert").addClass("hide");
}

function getDropdownActions(row) {
    var actions = actionDropdownMap[row.e2BStatus]?.map(action => actionDropdownTemplates[action]?.(row)).filter(Boolean) || [];
    if(actions.length){
        return `<button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown">
                    <span class="caret"></span>
                    <span class="sr-only">Toggle Dropdown</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">
                    ${actions.join("")}
                </ul>`;
    }
    return '';
}
