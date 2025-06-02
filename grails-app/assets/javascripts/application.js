// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.

//= require vendorUi/bower_components/webcomponentsjs/webcomponents-lite.js
//= require grid
//= require common-application
//= require vendorUi/select2/select2.full.min
//= require spring-websocket
//= require app/rxTitleOptions

//= require vendorUi/perfect-scrollbar/perfect-scrollbar.min
//= require_self

var DEFAULT_DATE_TIME_DISPLAY_FORMAT = "DD-MMM-YYYY hh:mm&#160;A";
var DEFAULT_DATE_DISPLAY_FORMAT = "DD-MMM-YYYY";
var DATEPICKER_FORMAT_AM_PM = "DD-MMM-YYYY hh:mm:ss A";
var MULTILINE_DATE_TIME_DISPLAY = "DD-MMM-YYYY</br>hh:mm:ss A";
var SESSION_TIME_OUT = "sessionTimeOut";
var UNAUTHORIZED = "Unauthorized";
var MULTIPLE_AJAX_SEPARATOR = "@!";
var JAPANESE_LOCALE = "ja";
var WILD_SEARCH_CHARACTER = "%";

var PR_Calendar = "PR_Calendar";
var REPOERT_REQUEST = "REPOERT_REQUEST";
var PERIODIC_REPORT = "PERIODIC_REPORT";
var ADHOC_REPORT = "ADHOC_REPORT";
var DRILLDOWN_RECORD = "DRILLDOWN_RECORD";
var IN_DRILLDOWN_RECORD = "IN_DRILLDOWN_RECORD";
var ACTION_PLAN = "ACTION_PLAN";
var QUALITY_MODULE = "QUALITY_MODULE";
var QUALITY_MODULE_CAPA = "QUALITY_MODULE_CAPA";
var PV_CENTRAL_CAPA = "PV_CENTRAL_CAPA";
const RELOADING_DEFERRED_ALERTS_DATA_KEY = 'reloading_deferred_alerts_data';

var STATUS_ENUM = {
  OPEN: "OPEN",
  IN_PROGRESS: "IN_PROGRESS",
  NEED_CLARIFICATION: "NEED_CLARIFICATION",
  CLOSED: "CLOSED",
};

var EXECUTION_STATUS_ENUM = {
  SCHEDULED: "Scheduled",
  BACKLOG: "Backlog",
  GENERATING: "Generating",
  DELIVERING: "Delivering",
  COMPLETED: "Completed",
  ERROR: "Error",
  WARN: "Warn",
};

var EXECUTION_STATUS_DROP_DOWN_ENUM = {
  SCHEDULED: "SCHEDULED",
};

var ACTION_ITEM_CATEGORY_ENUM = {
  REPORT_REQUEST: "REPORT_REQUEST",
  REQUEST_MISSING_INFORMATION: "REQUEST_MISSING_INFORMATION",
  PROCESS_CASE: "PROCESS_CASE",
  PERIODIC_REPORT: "PERIODIC_REPORT",
  CONFIGURE_REPORT: "CONFIGURE_REPORT",
  ADHOC_REPORT: "ADHOC_REPORT",
  QUALITY_MODULE: "QUALITY_MODULE",
  QUALITY_MODULE_CORRECTIVE: "QUALITY_MODULE_CORRECTIVE",
  QUALITY_MODULE_PREVENTIVE: "QUALITY_MODULE_PREVENTIVE",
  DRILLDOWN_RECORD: "DRILLDOWN_RECORD",
  ACTION_PLAN: "ACTION_PLAN",
  IN_DRILLDOWN_RECORD: "IN_DRILLDOWN_RECORD",
};

var UI_THEME = {
  gradient_blue: "Gradient Blue",
  solid_orange: "Solid Orange",
  solid_blue: "Solid Blue",
  solid_golden_grey: "Solid Golden Grey",
};

var LOGIN_PAGE = "login/auth";

var loaderStack = [];

let chartOptions = [];

if (typeof jQuery !== "undefined") {
  (function ($) {
    $("#spinner")
      .ajaxStart(function () {
        $(this).fadeIn();
      })
      .ajaxStop(function () {
        $(this).fadeOut();
      });
  })(jQuery);
}

$.fn.select2.defaults.set("width", null);

DataTable.defaults.column.orderSequence = ['asc', 'desc'];

$(window).on("load", function () {
  showReloadingDeferredAlerts();
  //The following will prevent all future AJAX requests from being cached, regardless of which jQuery method you use ($.get, $.ajax, etc.)
  $.ajaxSetup({ cache: false });
  $.fn.dataTable.datetime(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
  checkUserNavigation();
  clearSessionStorage();

  //Following is called when Ajax requests goes into error state
  $(document).ajaxError(function (event, request, settings, thrownError) {
    checkIfSessionTimeOutThenReload(event, thrownError);
  });
});

// clear the sessionStorage once the user logs out
function clearSessionStorage() {
  // if ((!document.referrer == '') && (document.referrer.toString().indexOf('reports') == -1)) {
  //     window.location.href = "/reports"
  // }
  if (location.href.indexOf(LOGIN_PAGE) != -1) {
    sessionStorage.clear();
  }
}

function showReloadingDeferredAlerts() {
  const data = sessionStorage.getItem(RELOADING_DEFERRED_ALERTS_DATA_KEY);
  if (!_.isEmpty(data)) {
    let alertsData;
    try {
      alertsData = JSON.parse(data);
    } catch (e) {
      alertsData = {};
    }

    $.each(alertsData['alerts'] || [], function (index, alert) {
      showInlineAlert(alert.type, alert.message, alertsData.rootSelector);
    });
    sessionStorage.removeItem(RELOADING_DEFERRED_ALERTS_DATA_KEY);
  }
}

function showInlineAlert(type, message, rootSelector) {
  const selectorPrefix = rootSelector ? (rootSelector + ' ') : '';
  const alertElement = $(selectorPrefix + '#inline-alert-' + type);
  alertElement.hide();
  alertElement.find('.alert-text').text(message);
  alertElement.show();
}

function showResponseAlerts(response, rootSelector) {
  //todo: add message code/multiple same type messages support
  $.each(response['alerts'] || [], function (index, alert) {
    showInlineAlert(alert.type, alert.message, rootSelector);
  });
}

function reloadPageAndShowResponseAlerts(response, rootSelector, reloadFn) {
  sessionStorage.setItem(
      RELOADING_DEFERRED_ALERTS_DATA_KEY, JSON.stringify({
        alerts: response['alerts'],
        rootSelector: rootSelector
      })
  );
  if ($.isFunction(reloadFn)) {
    reloadFn();
  } else {
    location.reload();
  }
}

function updateTitleForThreeRowDotElements() {
  $(".three-row-dot-overflow").each(function () {
    if ($(this).height() > 50) $(this).attr("title", extractTextFromElement($(this)));
  });
  $(".two-row-dot-overflow").each(function () {
        if ($(this).height() > 35) $(this).attr("title", extractTextFromElement($(this)));
    });
  $(".five-row-dot-overflow").each(function () {
    if ($(this)[0].scrollHeight > $(this).outerHeight()) {
      $(this).attr("title", extractTextFromElement($(this)));
    }
  });
  $(".one-row-dot-overflow").each(function () {
    $(this).attr("title", extractTextFromElement($(this)));
  });
}

function clearFormInputsChangeFlag(formSelector) {
  if ($(formSelector).find(".changed-input").length) {
    $.each($(formSelector).find(".changed-input"), function () {
      $(this).removeClass("changed-input");
    });
    return true;
  }
  return false;
}

function checkUserNavigation() {
  $("form").on("submit", function () {
    clearFormInputsChangeFlag($("form"));
  });

  $("#mainContent form")
    .not("#userSearchForm")
    .not("#auditLogSearchForm")
    .on(
      "change keyup keydown keypress",
      "input, textarea, select, checkbox, radio",
      function (e) {
        if (!$(this).hasClass("changed-input")) {
          $(this).addClass("changed-input");
        }
      }
    );

  $(window).on("beforeunload", function (event) {
    if (
      $("form .changed-input:visible").length ||
      ($(".studyRadio").length &&
        $(".productRadio").length &&
        $("form .changed-input").length)
    ) {
      return $.i18n._("navigateAwayErrorMessage");
    }
  });
}

function showSpinnerMessage(id) {
  if (id) {
    $("#" + id).css("display", "inline");
  } else {
    $("#spinnerMessage").css("display", "inline");
  }
}

function clearSpinnerMessage() {
  $("#spinnerMessage").css("display", "none");
}

//Client side sorting of the entries of a dropdown
$.fn.sort_select_box = function () {
  // Get options from select box
  var my_options = $("#" + this.attr("id") + " option");
  // sort alphabetically
  my_options.sort(function (a, b) {
    if (a.text > b.text) return 1;
    else if (a.text < b.text) return -1;
    else return 0;
  });
  //replace with sorted my_options;
  $(this).empty().append(my_options);

  // clearing any selections
  $("#" + this.attr("id") + " option").attr("selected", false);
};

$(function () {
  var token = $("meta[name='_csrf']").attr("content");
  var header = $("meta[name='_csrf_header']").attr("content");
  var parameter = $("meta[name='_csrf_parameter']").attr("content");

  if (header && token) {
    $(document).ajaxSend(function (e, xhr, options) {
      if (options.type != "GET") {
        xhr.setRequestHeader(header, token);
      }
    });
  }

  if (parameter && token) {
    $("form").on("submit", function () {
      var hiddenField = $("<input>")
        .attr("type", "hidden")
        .attr("name", parameter)
        .val(token);
      $(hiddenField).appendTo(this);
    });
  }
});

function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

function resetForm(container) {
  container
    .find("input:text, input:password, input:file, select, textarea")
    .val("");
  container
    .find("input:radio, input:checkbox")
    .prop("checked", false)
    .prop("selected", false);
}

function closeAllCopyPasteModals() {
  $("#emailCopyPasteError").hide();
  $("div[id^='copyAndPaste']").modal("hide");
  resetForm($("#importValueSection"));
  $("#validate-values").attr("disabled", "disabled");
  $("#fileFormatError").hide();
    $('#copyAndPasteDicModal').on('hidden.bs.modal',function(){
        if($('#productModal:visible,#eventModal:visible,#studyModal:visible').length){
            $('body').addClass('modal-open');
        }
    });
}

function closeAllImportValueModal() {
  var importModal = $("div[id^='importValueModal'].in");
  importModal.modal("hide");
  var importValueSection = importModal
    .parent()
    .prev(".copyAndPasteModal")
    .find("#importValueSection");
  resetForm(importValueSection);
  importValueSection.find("#validate-values").attr("disabled", "disabled");
}

function successNotification(message, singleFlag) {
    var selectedContainers = singleFlag ? $("div.rxmain-container:visible").first() : $("div.rxmain-container");
    $(".alert-success").alert("close");
    if (message != undefined && message != "")
        selectedContainers.before(
            '<div class="alert alert-success alert-dismissable">' +
            '<button type="button" class="close" ' +
            'data-dismiss="alert" aria-hidden="true">' +
            "&times;" +
            "</button>" +
            message +
            "</div>"
        );
}

function successNotificationForIcsrCaseTracking(message) {
  $(".alert-danger").alert("close");
  $(".alert-success").alert("close");
  if (message != undefined && message != "")
    $(".rxmain-container").prepend(
      '<div class="alert alert-success alert-dismissable">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    );
}

function errorNotification(message) {
  $(".alert-danger").alert("close");
  if (message != undefined && message != "")
    $("div.rxmain-container").before(
      '<div class="alert alert-danger alert-dismissable">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    );
}

function showInlineNotification(
  type,
  message,
  contextSelector,
  targetSelector
) {
  var contextPrefix = contextSelector ? contextSelector + " " : "";
  $(contextPrefix + ".alert-" + type).alert("close");
  if (message) {
    $(
      contextPrefix +
        (targetSelector ? targetSelector : "div.rxmain-container-content")
    )
      .first()
      .prepend(
        '<div class="alert alert-' +
          type +
          ' alert-dismissable">' +
          '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">' +
          "&times;" +
          "</button>" +
          message +
          "</div>"
      );
  }
}

function errorNotificationForQuality(message) {
  $(".alert-danger").alert("close");
  if (message != undefined && message != "")
    $($("div.rxmain-container")[1]).before(
      '<div class="alert alert-danger alert-dismissable">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    );
}

function errorNotificationForIcsrCaseTracking(message) {
  $(".alert-success").alert("close");
  $(".alert-danger").alert("close");
  if (message != undefined && message != "")
    $("div.rxmain-container").prepend(
      '<div class="alert alert-danger alert-dismissable">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    );
}

function warningNotification(message) {
  $(".alert-warning").alert("close");
  if (message != undefined && message != "")
    $("div.rxmain-container").before(
      '<div class="alert alert-warning alert-dismissable">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    );
}

function formatLabel(label) {
  if (!label.qced && !label.isFavorite) {
    return encodeToHTML(label.text);
  }
  var qcedIcon =
    '<i class="fa fa-check-circle-o"  title="' +
    $.i18n._("app.qced.help") +
    '"></i> ';
  var favoriteIcon =
    '<i class="fa fa-star" title="' +
    $.i18n._("app.favorite.label") +
    '"></i> ';
  var $state = $(
    "<span>" +
      (label.qced ? qcedIcon : "") +
      (label.isFavorite ? favoriteIcon : "") +
      encodeToHTML(label.text) +
      "</span>"
  );
  return $state;
}

function formatSelection(label) {
  if (!label.qced && !label.isFavorite) {
    return (
      '<span title="' +
      replaceBracketsAndQuotes(label.text) +
      '">' +
      replaceBracketsAndQuotes(label.text) +
      "</span>"
    );
  }
  var qcedIcon =
    '<i class="fa fa-check-circle-o"  title="' +
    $.i18n._("app.qced.help") +
    '"></i> ';
  var favoriteIcon =
    '<i class="fa fa-star" title="' +
    $.i18n._("app.favorite.label") +
    '"></i> ';
  var $state = $(
    "<span>" +
      (label.qced ? qcedIcon : "") +
      (label.isFavorite ? favoriteIcon : "") +
      '<span title="' +
      replaceBracketsAndQuotes(label.text) +
      '">' +
      replaceBracketsAndQuotes(label.text) +
      "</span></span>"
  );
  return $state;
}

function bindSelect2WithUrl(
  selector,
  optionsDataUrl,
  optionTextUrl,
  allowClear,
  favoritList,
  minimumInputLength = 0
) {
  var oldSelectedValue;
  try {
    oldSelectedValue = selector.val();
  } catch (e) {
    oldSelectedValue = "";
  }
  const modalParent = $(selector).parents(".modal[role=dialog]") || null;
  return selector.select2({
    placeholder: $.i18n._("selectOne"),
    minimumInputLength,
    allowClear: allowClear,
    dropdownParent: modalParent?.[0],
    templateResult: formatLabel,
    templateSelection: formatSelection,
    language: {
      searching: function () {
        return  $.i18n._("searching.label"); // Replace with your desired text
      },
    },
    ajax: {
      url: optionsDataUrl,
      dataType: "json",
      delay: 250,
      data: function (params) {
        // page is the one-based page number tracked by Select2
        return {
          term: params.term, //search term
          page: params.page, // page number
          max: 30,
          oldSelectedValue: oldSelectedValue,
        };
      },
      processResults: function (data, params) {
        checkIfSessionTimeOutThenReload(null, data);
        var page = params.page || 1;
        var more = page * 30 < data.total_count; // whether or not there are more results available

        data.items.forEach(function (item) {
          if (favoritList)
            for (var k = 0; k < favoritList.length; k++) {
              if (item.text == favoritList[k]) {
                item.isFavorite = true;
                break;
              }
            }
          item.text = encodeToHTML(item.text);
          item.text = unescapeHTML(item.text);
        });
        // notice we return the value of more so Select2 knows if more results can be loaded
        return {
          results: data.items,
          pagination: {
            more: more,
          },
        };
      },
    },
    escapeMarkup: function (m) {
      return m;
    }, // we do not want to escape markup since we are displaying html in results
    initSelection: function (element, callback) {
      var value = $(element).attr("data-value") ?? $(element).val();
      if (value && optionTextUrl && !Number.isNaN(value)) {
        $.get(optionTextUrl + "?id=" + value, function (data) {
          data.text = encodeToHTML(data.text);
          data.text = unescapeHTML(data.text);
          let selectedData = {
            id: value,
            text: data.text ?? value,
            qced: data.qced,
            isFavorite: data.isFavorite,
            configureAttachments: data.configureAttachments,
            groupingColumns: data.groupingColumns,
          }
          selector.trigger("initCompete");
          if (selector.val() !== value) {
            selector
              .append(new Option(data.text ?? value, value, true, true))
              .val(value)
              .trigger("change");
          }
          setTimeout(() =>  callback(selectedData),0);
        });

        var link = $(element).parent().next().find('[name="viewLink"]');
        if (link.length) {
          link.attr("href", link.attr("href") + "/" + value);
        }
        return;
      }
      /*
                placeholder is not displayed without callback
                we can't directly call callback in case we are setting data-value (i.e. copy section)
                pushing callback to the end of callstack, it will make initSelection run few times
                which will get data-value and initialize dropdown
            */
      setTimeout(() => callback([]), 0);
    },
  });
}

function bindMultipleSelect2WithUrl(
  selector,
  optionsDataUrl,
  allowClear,
  placeholder,
  type,
  selectedJSONValue,
  optionTextUrl
) {
  function formatSelection(label) {
    const selector = $(this);
    setTimeout(function () {
      selector.trigger("optionRendered");
    }, 0);
    return `${
      label.text
    } <i class="select2label" data-id="${label.id}"></i>`;
  }

  return selector.select2({
    placeholder: placeholder,
    minimumInputLength: 0,
    allowClear: allowClear,
    multiple: true,
    separator: MULTIPLE_AJAX_SEPARATOR,
    closeOnSelect: false,
    templateSelection: formatSelection.bind(selector),
    ajax: {
      url: optionsDataUrl,
      dataType: "json",
      delay: 250,
      data: function (params) {
        // page is the one-based page number tracked by Select2
        return {
          term: params.term, //search term
          page: params.page, // page number
          max: 30, //max
          type: type,
        };
      },
      processResults: function (data, params) {
        checkIfSessionTimeOutThenReload(null, data);
        var page = params.page || 1;
        var more = page * 30 < data.total_count; // whether or not there are more results available

        $.each(data.items, function (index, item) {
          item.text = encodeToHTML(item.text);
        });

        // notice we return the value of more so Select2 knows if more results can be loaded
        return {
          results: data.items,
          pagination: {
            more: more,
          },
        };
      },
    },
    escapeMarkup: function (m) {
      return m;
    }, // we do not want to escape markup since we are displaying html in results
    initSelection: function (element, callback) {
      let value = $(element).attr("data-value");
      if (!value) {
        value = $(element).val();
      }
      var selectedValues = [];
      if (value && optionTextUrl) {
        $.get(optionTextUrl + "?ids=" + value, function (data) {
          $.each(data, function (index, value) {
            selectedValues.push({
              id: value.id,
              text: encodeToHTML(value.text),
            });
          });
          callback(selectedValues);
          selector.trigger("initCompete");
        });
        return;
      }
      if (selectedJSONValue) {
        $.each(JSON.parse(selectedJSONValue), function (index, value) {
          selectedValues.push({ id: value.id, text: value.name });
          if (!selector.find(`option[value="${value.id}"]`).length) {
            selector
                .append(new Option(value.name, value.id, false, true))
                .trigger("change");
          }
        });
        callback(selectedValues);
        return;
      }

      if (value && value.length > 0 && !(Array.isArray(value) && (value.length === 0))) {
        const allValues = value.split(MULTIPLE_AJAX_SEPARATOR).flatMap((destination) => {
          if (destination.startsWith("[") && destination.endsWith("]")) {
            const innerContent = destination.substring(1, destination.length - 1);
            if (innerContent.includes(",")) {
              return innerContent.split(",");
            } else {
              return [destination];
            }
          } else {
            return [destination];
          }
        });
        $.each(allValues, function (index, value) {
          selectedValues.push({ id: value, text: value });
          if (!element.find(`option[value="${value}"]`).length && value != "") {
            selector
              .append(new Option(value, value, false, true))
              .val(allValues)
              .trigger("change");
          }
        });
        callback(selectedValues);
        return;
      }
      callback([]);
    },
    language: {
      noResults: function () {
        var create = "";
        var term = selector.parent().find(".select2-search__field").val();
        if (type == "A") {
          create =
              "<span id='addNewCaseLevelTag' class='btn btn-success'>" +
              $.i18n._("create") +
              "</span>";
          return (
              "<input readonly='readonly' class='form-control' id='newCaseLevelTag' value='" +
              term +
              "'>" + create
          );
        } else if (type == "G") {
          create =
              "<span id='addNewGlobalLevelTag' class='btn btn-success'>" +
              $.i18n._("create") +
              "</span>";
          return (
              "<input readonly='readonly' class='form-control' id='newGlobalLevelTag' value='" +
              term +
              "'>" + create
          );
        } else {
          return "No matches found";
        }
      }
    }
  });
}

function bindShareWithEditable(selector, sharedWithListUrl, sharedWithValuesUrl, width, single) {
    var select2 = bindShareWith(selector, sharedWithListUrl, sharedWithValuesUrl, width, single);
    var editableBy = select2.closest(".editableBy").find("#executableBy");

    function removeFromEditable(user) {
        var users = editableBy.val()?.split(";");
        editableBy.val(_.filter(users, function (e) {
            return e != user
        }).join(";"))
    }

    function addToEditable(user) {
        var users = editableBy.val()?.split(";");
        if (!_.find(users, function (e) {
            return e == user
        })) editableBy.val(editableBy.val() + ";" + user);
    }

    select2.on('select2:opening', function(e) {
        // we can't get the target of opening event, so we check window event and making sure it was called by indicator clicking
            const event = window.event;
            if (event?.type === 'click' && ($(event?.target)?.hasClass('indicator') || $(event?.target)?.hasClass('select2-label'))) {
                e.stopPropagation();
                e.preventDefault();
            }
        })
        .data('select2').$container.on("click", "li.select2-selection__choice", function (event) {
        var ind = $(this).find(".indicator");
        if (ind.hasClass("primary")) {
            ind.removeClass("primary");
            removeFromEditable(ind.attr("data-id"));
        } else {
            addToEditable(ind.attr("data-id"));
            ind.addClass("primary");
        }
    });
    select2.on("select2:unselecting", function (e) {
        if (!$(this).select2("val") || $(this).select2("val").length == 1) e.preventDefault();
    }).on("select2:unselect", function (e) {
        removeFromEditable(e.params.data.id);
    }).on("change, initDataLoaded", function (e) {
        var users = editableBy.val()?.split(";");
        $(this).find("li.select2-selection__choice .indicator").removeClass('primary');
        $(this).find("li.select2-selection__choice").each(function () {
            var ind = $(this).find(".indicator");
            if (_.find(users, function (e) {
                return e == ind.attr("data-id")
            })) {
                ind.addClass("primary");
            } else {
                ind.removeClass("primary");
            }
        });
    })
    .trigger("change");
  return select2;
}

//TODO: Check if we can use the existing bindSelect2WithUrl() method
function bindShareWith(
    selector,
    sharedWithListUrl,
    sharedWithValuesUrl,
    width,
    single,
    parent = $('body'),
    placeholder = "sharedWith",
    allowClear = true
) {

  let loadSelection = function (select, value, callback) {
    if (sharedWithValuesUrl) {
      let ids = value;
      if (Array.isArray(value)) ids = value.join(";")
      $.get(sharedWithValuesUrl + "?ids=" + ids, function (data) {
        if (callback) {
          if (single) {
            callback(data?.[0] ?? data);
          } else {
            callback(data);
          }
        }
        select.trigger("initDataLoaded");
        if (!data || !value || (Array.isArray(value) && (value.length === 0))) {
          return;
        }
        data.forEach(item => {
          let option = select.find(`option[value="${item.id}"]`)
          if (option.length === 0) {
            select.append(new Option(item.text, item.id, true, true));
          }
        })

      });
    }
  };

  var select2 = selector
      .select2({
        placeholder: $.i18n._(placeholder),
        minimumInputLength: 0,
        language: {
          searching: function () {
            return $.i18n._("searching.label")
          }
        },
        multiple: !single,
        width: width ?? null,
        separator: ";",
        allowClear: allowClear,
        templateResult: formatBlindedUserResult,
        templateSelection: formatBlindedUserSelection.bind(selector),
        dropdownParent: parent,
        ajax: {
          url: sharedWithListUrl,
          dataType: "json",
          delay: 250,
          data: function (params) {
            let data ={
              term: params.term, //search term
              page: params.page, // page number
              max: 30,
            };
            if(selector.attr("data-extraParam")){
              let obj = JSON.parse(selector.attr("data-extraParam"));
              data ={...data, ...obj};
            }
            return data;
          },
          processResults: function (data, params) {
            checkIfSessionTimeOutThenReload(null, data);
            var page = params.page || 1;
            var more = page * 30 < data.total_count; // whether or not there are more results available

            // notice we return the value of more so Select2 knows if more results can be loaded
            return {
              results: data.items,
              pagination: {
                more: more,
              },
            };
          },
        },
        escapeMarkup: function (m) {
          return m;
        },
        initSelection: function (element, callback) {
          if (sharedWithValuesUrl) {
            let value = $(element).attr("data-value");
            if (!value) {
              value = $(element).val();
            }
            loadSelection($(element), value, callback)
          }
        },
      })
      .on("select2-loaded", function (e) {
        $(".select2-results__option").each(function (index) {
          var $this = $(this);
          if ($this.html() === "<span></span>" || $this.html() === "")
            $(this).detach();
        });
      });

  select2.each(function (index) {
    let thiz = $(this);
    thiz.data('custom_select_val', function (val) {
      var v = thiz.val();
      if (!v || Array.isArray(val)) {
        loadSelection(thiz, val, null)
      }
      return thiz;
    });
  });
  return select2;
}


$.fn.old_val = $.fn.val;
$.fn.val = function () {
  if (arguments.length === 0 || !arguments[0]) return this.old_val.apply(this, arguments);
  var custom_select_val = this.data('custom_select_val');
  if (custom_select_val) {
    if (arguments[0].indexOf(";") > -1) arguments[0] = arguments[0].split(";");
    this.old_val.apply(this, arguments);
    console.log("custom_select_val!")
    return custom_select_val.apply(this, arguments);
  } else {
    return this.old_val.apply(this, arguments);
  }
};

function checkIfSessionTimeOutThenReload(event, result) {
  if (result && (result === UNAUTHORIZED || result[SESSION_TIME_OUT])) {
    event ? event.stopPropagation() : "";
    alert($.i18n._("sessionTimeOut"));
    window.location.reload();
    return false;
  }
}

/**
 * Determines whether a value is a positive integer or not
 * @param n
 * @returns {boolean}
 */
function isPositiveInteger(n) {
  return n % 1 === 0 && n > 0;
}

function isEmpty(str) {
  return !str || 0 === str.length;
}

function isBlank(str) {
  return !str || /^\s*$/.test(str);
}

function getReloader(toolbar, tableName) {
  var reloader =
    '<span title="Refresh" class="glyphicon reloaderBtn glyphicon-refresh"></span>';
  reloader = $(reloader);
  $(toolbar).append(reloader);
  if (tableName != undefined) {
    $(".reloaderBtn").on("click", function () {
      $(".reloaderBtn").addClass("glyphicon-refresh-animate");
      $(tableName).DataTable().draw();
    });
  }
}

function buildReportingDestinationsSelectBox(
  destinationsSelectBox,
  url,
  primaryDestinationField,
  isPrimarySelectable,
  userValuesUrl
) {
  var selectReportings = bindMultipleSelect2WithUrl(
    destinationsSelectBox,
    url,
    true,
    null,
    null,
    null,
    userValuesUrl
  );
  selectReportings
    .on("select2:opening", function (e) {
      // we can't get the target of opening event, so we check window event and making sure it was called by indicator clicking
      const event = window.event;
      if (
        event?.type === "click" &&
        $(event?.target)?.hasClass("primary-indicator")
      ) {
        e.stopPropagation();
        e.preventDefault();
      }
    })
    .siblings(".select2")
    .on(
      "click",
      "li.select2-selection__choice span.primary-indicator",
      function (event) {
        if (isPrimarySelectable != false) {
          $(this)
            .parent()
            .parent()
            .find("li.select2-selection__choice span.primary-indicator")
            .removeClass("primary");
          $(this).addClass("primary");
          primaryDestinationField.val(
            $(this).parent().find(".select2label").attr("data-id")
          );
        }
      }
    );
  selectReportings
    .on("select2:unselect", function (e) {
      if (isPrimarySelectable != false) {
        var primaryDestination = primaryDestinationField.val();
        if (e.params.data.id == primaryDestination) {
          var first = $(this)
            .parent()
            .parent()
            .find("li.select2-selection__choice")[0];
          if (first) {
            primaryDestinationField.val(
              $(first).find(".select2label").attr("data-id")
            );
          } else primaryDestinationField.val("");
        }
      }
    })
    .on("change optionRendered initCompete", function (e) {
      var primaryDestination = primaryDestinationField.val();
      var choices = $(this)
        .parent()
        .parent()
        .find("li.select2-selection__choice");

      // Assign first option as primary if no primaryDestination is set
      if (isPrimarySelectable && !primaryDestination) {
        var first = choices.first();
        if (first.length > 0) {
          primaryDestinationField.val(first.find(".select2label").attr("data-id"));
          primaryDestination = primaryDestinationField.val();
        } else {
          // Clear primaryDestination if no items are selected
          primaryDestinationField.val("");
          primaryDestination = null;
        }
      }

      // Remove existing "primary" classes
      choices.find(".primary-indicator").removeClass("primary");

      // Add primary-indicator spans and highlight the primary
      choices.each(function () {
        if ($(this).find(".primary-indicator").length === 0) {
          $(this).prepend("<span class='primary-indicator'>P</span>");
        }
        if (primaryDestination && $(this).find(".select2label").attr("data-id").trim() === primaryDestination.trim()) {
          $(this).find(".primary-indicator").addClass("primary");
        }
      });
    })
    .trigger("change");
  return selectReportings;
}

function getReportFieldOptions(data) {
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
          value: encodeToHTML(field.name),
          "data-dictionary": field.dictionary,
          "data-level": field.level,
          "data-validatable": field.validatable,
          "data-isAutocomplete": field.isAutocomplete,
          "data-isNonCacheSelectable": field.isNonCacheSelectable,
          "data-dataType": field.dataType,
          "data-isText": field.isText,
          "data-description": encodeToHTML(field.description),
        }).text(encodeToHTML(field.displayText))
      );
    });
    html = html + group[0].outerHTML;
  });
  return html;
}

function loadReportFieldOptions(
  url,
  cache,
  sourceId,
  inputs,
  beforeSendCallback,
  completeCallback,
  successCallback
) {
  $.ajax({
    type: "GET",
    url: url,
    cache: cache,
    dataType: "json",
    data: {
      sourceId: sourceId ? sourceId : "",
    },
    beforeSend: function () {
      if (beforeSendCallback) {
        beforeSendCallback();
      } else {
        showLoader();
      }
    },
  })
    .done(function (result) {
      if (result && result.data) {
        var html = getReportFieldOptions(result.data);
        $.each(inputs, function (index, input) {
          input.html(html);
          if (input.data("prefix")) {
            var prefix = input.data("prefix");
            $.each(input.find("option"), function (index, option1) {
              if ($(option1).val()) $(option1).val(prefix + $(option1).val());
            });
          }
        });
      }
      if (successCallback) successCallback();
    })
    .fail(function (err) {
      console.log(err);
    })
    .always(function () {
      if (completeCallback) {
        completeCallback();
      } else {
        hideLoader();
      }
    });
}

//Email configuration for Adhoc/Periodic Report create and edit page and also for Dashboard and Generated Adhoc Reports page.
var emailToUserSelect = $("select[name=emailToUsers]");
$(document).on("show.bs.modal", "#emailConfiguration", function (e) {
  emailToUserSelect = getEmailToUsersSelect(e);
  $("select[name=emailConfiguration\\.to]")
    .val(emailToUserSelect.select2("val"))
    .trigger("change");
});

function getEmailToUsersSelect(e) {
  //madal can be called from different places on one page.
  if (e) {
    var userSelect = $(e.relatedTarget)
      .closest(".rxmain-container-content")
      .find("#emailUsers");
    if (userSelect.length == 1) {
      return userSelect;
    }
  }
  var emailToUsers = $("select[name=emailToUsers]");
  if (emailToUsers.length == 1) return $("select[name=emailToUsers]");
  if (
    e &&
    $(e.relatedTarget).closest(".modal").attr("id") == "reportSubmissionModal"
  )
    return $(emailToUsers[1]);
  else return $(emailToUsers[0]);
}

$(document).on('shown.bs.modal', '#emailConfiguration', function (event) {
    var emailConfigModal = $(event.target);
    emailConfigModal.find('div.alert').hide();
    emailConfigModal.find('.field-validation-error').removeClass('field-validation-error');
    emailConfigModal.find('.has-error').removeClass('has-error');
    setSelect2InputWidth($(emailConfigModal).find("select[id='emailConfiguration.to']"));
    setSelect2InputWidth($(emailConfigModal).find("select[id='emailConfiguration.cc']"));
});

function emailConfig(init, data) {
  var emailConfiguration = $("#emailConfiguration");
  var noEmailOnNoData = $("input[name=emailConfiguration\\.noEmailOnNoData]");
  var subject = $("input[name=emailConfiguration\\.subject]");
  var cc = $("select[name=emailConfiguration\\.cc]");
  var to = $("select[name=emailConfiguration\\.to]");
  var body = $("textarea[name=emailConfiguration\\.body]");
  var pageOrientation = $("select[name=emailConfiguration\\.pageOrientation]");
  var paperSize = $("select[name=emailConfiguration\\.paperSize]");
  var sensitivityLabel = $(
    "select[name=emailConfiguration\\.sensitivityLabel]"
  );
  var showPageNumbering = $(
    "input[name=emailConfiguration\\.showPageNumbering]"
  );
  var excludeCriteriaSheet = $(
    "input[name=emailConfiguration\\.excludeCriteriaSheet]"
  );
  var excludeAppendix = $("input[name=emailConfiguration\\.excludeAppendix]");
  var excludeComments = $("input[name=emailConfiguration\\.excludeComments]");
  var excludeLegend = $("input[name=emailConfiguration\\.excludeLegend]");
  var showCompanyLogo = $("input[name=emailConfiguration\\.showCompanyLogo]");

  var noEmailOnNoDataValue = $("input[name=noEmailOnNoDataValue]");
  var subjectValue = $("input[name=subjectValue]");
  var ccValue = $("input[name=ccValue]");
  var toValue = $("input[name=toValue]");
  var bodyValue = $("input[name=bodyValue]");
  var pageOrientationValue = $("input[name=pageOrientationValue]");
  var paperSizeValue = $("input[name=paperSizeValue]");
  var sensitivityLabelValue = $("input[name=sensitivityLabelValue]");
  var showPageNumberingValue = $("input[name=showPageNumberingValue]");
  var excludeCriteriaSheetValue = $("input[name=excludeCriteriaSheetValue]");
  var excludeAppendixValue = $("input[name=excludeAppendixValue]");
  var excludeCommentsValue = $("input[name=excludeCommentsValue]");
  var excludeLegendValue = $("input[name=excludeLegendValue]");
  var showCompanyLogoValue = $("input[name=showCompanyLogoValue]");
  if (data) {
    if (data.length == 0) resetValues(true);
    else {
      toValue.val(data.to);
      ccValue.val(data.cc);
      subjectValue.val(data.subject);
      bodyValue.val(data.body);
      noEmailOnNoDataValue.val(data.noEmailOnNoData);
      pageOrientationValue.val(data.pageOrientation.name);
      paperSizeValue.val(data.paperSize.name);
      sensitivityLabelValue.val(data.sensitivityLabel.name);
      showPageNumberingValue.val(data.showPageNumbering);
      excludeCriteriaSheetValue.val(data.excludeCriteriaSheet);
      excludeAppendixValue.val(data.excludeAppendix);
      excludeCommentsValue.val(data.excludeComments);
      excludeLegendValue.val(data.excludeLegend);
      showCompanyLogoValue.val(data.showCompanyLogo);
      setSavedValues();
    }
  }
  if (hasChanges()) {
    $(".showEmailConfiguration img").attr({
      src: "/reports/assets/icons/email-secure.png",
      title: $.i18n._("app.configuration.addEmailConfigurationEdited.label"),
    });
    $(".showEmailConfiguration img").attr("top", "13px");
  } else {
    $(".showEmailConfiguration img").attr({
      src: "/reports/assets/icons/email.png",
      title: $.i18n._("app.configuration.addEmailConfiguration.label"),
    });
  }
  if (init) {
    $("#cancelEmailConfiguration").on("click", function (e) {
      setSavedValues();
      $(this).closest(".modal").modal("hide");
    });

    $(emailConfiguration)
      .find("#resetEmailConfiguration")
      .on("click", function (e) {
        resetValues();
      });

    $(emailConfiguration)
      .find("#closeError")
      .on("click", function (e) {
        $("#emailConfiguration div.alert").hide();
      });

    $(emailConfiguration)
      .find("#saveEmailConfiguration")
      .on("click", function (e) {
        body.val(tinyMCE.activeEditor.getContent());
        if (!validateModalOnSubmit()) {
          return false;
        }
        ccValue.val(cc.val());
        toValue.val(to.val());
        emailToUserSelect.val(to.val()).trigger('change');
        subjectValue.val(subject.val());
        bodyValue.val(tinyMCE.activeEditor.getContent());

        pageOrientationValue.val(pageOrientation.val());
        paperSizeValue.val(paperSize.val());
        sensitivityLabelValue.val(sensitivityLabel.val());

        noEmailOnNoDataValue.val(noEmailOnNoData.is(":checked"));
        showPageNumberingValue.val(showPageNumbering.is(":checked"));
        excludeCriteriaSheetValue.val(excludeCriteriaSheet.is(":checked"));
        excludeAppendixValue.val(excludeAppendix.is(":checked"));
        excludeCommentsValue.val(excludeComments.is(":checked"));
        excludeLegendValue.val(excludeLegend.is(":checked"));
        showCompanyLogoValue.val(showCompanyLogo.is(":checked"));

        if (hasChanges()) {
          $(".showEmailConfiguration img").attr({
            src: "/reports/assets/icons/email-secure.png",
            title: $.i18n._(
              "app.configuration.addEmailConfigurationEdited.label"
            ),
          });
        } else {
          $(".showEmailConfiguration img").attr({
            src: "/reports/assets/icons/email.png",
            title: $.i18n._("app.configuration.addEmailConfiguration.label"),
          });
        }

        $(this).attr("data-dismiss", "modal");
      });
  }

  function resetValues(initMode) {
        var toValue = initMode === true ? emailToUserSelect.select2("val") : "";
    body.val("");
    if (tinyMCE.activeEditor) tinyMCE.activeEditor.setContent("");
    subject.val("");
    cc.val("").trigger("change");
    to.val(toValue).trigger("change");
    pageOrientation.val(DEFAULT_EMAIL_OPTIONS.PAGE_ORIENTATION);
    paperSize.val(DEFAULT_EMAIL_OPTIONS.PAPER_SIZE);
    sensitivityLabel.val(DEFAULT_EMAIL_OPTIONS.SENSITIVITY_LABEL);
    noEmailOnNoData.prop("checked", false);
    showPageNumbering.prop(
      "checked",
      DEFAULT_EMAIL_OPTIONS.SHOW_PAGE_NUMBERING
    );
    excludeCriteriaSheet.prop(
      "checked",
      DEFAULT_EMAIL_OPTIONS.EXCLUDE_CRITERIA_SHEET
    );
    excludeAppendix.prop("checked", DEFAULT_EMAIL_OPTIONS.EXCLUDE_APPENDIX);
    excludeComments.prop("checked", DEFAULT_EMAIL_OPTIONS.EXCLUDE_COMMENTS);
    excludeLegend.prop("checked", DEFAULT_EMAIL_OPTIONS.EXCLUDE_LEGEND);
    showCompanyLogo.prop("checked", DEFAULT_EMAIL_OPTIONS.SHOW_COMPANY_LOGO);
  }

  function setSavedValues() {
    cc.val(ccValue.val().split(",")).trigger("change");
    to.val(emailToUserSelect.select2("val")).trigger("change");
    subject.val(subjectValue.val());
    body.val(bodyValue.val());
    if (tinyMCE && tinyMCE.activeEditor)
      tinyMCE.activeEditor.setContent(bodyValue.val());
    pageOrientation.val(pageOrientationValue.val());
    paperSize.val(paperSizeValue.val());
    sensitivityLabel.val(sensitivityLabelValue.val());
    noEmailOnNoData.prop("checked", noEmailOnNoDataValue.val() == "true");
    showPageNumbering.prop("checked", showPageNumberingValue.val() == "true");
    excludeCriteriaSheet.prop(
      "checked",
      excludeCriteriaSheetValue.val() == "true"
    );
    excludeAppendix.prop("checked", excludeAppendixValue.val() == "true");
    excludeComments.prop("checked", excludeCommentsValue.val() == "true");
    excludeLegend.prop("checked", excludeLegendValue.val() == "true");
    showCompanyLogo.prop("checked", showCompanyLogoValue.val() == "true");
  }

  function hasChanges() {
    return (
      subject.val() != "" ||
      body.val() != "" ||
      cc.val().toString() != "" ||
      to.val().toString() != "" ||
      noEmailOnNoData.is(":checked") ||
      pageOrientation.val() != DEFAULT_EMAIL_OPTIONS.PAGE_ORIENTATION ||
      paperSize.val() != DEFAULT_EMAIL_OPTIONS.PAPER_SIZE ||
      sensitivityLabel.val() != DEFAULT_EMAIL_OPTIONS.SENSITIVITY_LABEL ||
      showPageNumbering.is(":checked") !=
        DEFAULT_EMAIL_OPTIONS.SHOW_PAGE_NUMBERING ||
      excludeCriteriaSheet.is(":checked") !=
        DEFAULT_EMAIL_OPTIONS.EXCLUDE_CRITERIA_SHEET ||
      excludeAppendix.is(":checked") !=
        DEFAULT_EMAIL_OPTIONS.EXCLUDE_APPENDIX ||
      excludeComments.is(":checked") !=
        DEFAULT_EMAIL_OPTIONS.EXCLUDE_COMMENTS ||
      excludeLegend.is(":checked") != DEFAULT_EMAIL_OPTIONS.EXCLUDE_LEGEND ||
      showCompanyLogo.is(":checked") !=
        DEFAULT_EMAIL_OPTIONS.SHOW_COMPANY_LOGO ||
      (tinyMCE.activeEditor && tinyMCE.activeEditor.getContent().trim() !== "")
    );
  }

  function hasEmptyRequiredField() {
        return !subject.val() || !subject.val().trim() || !body.val() || !body.val().trim();
    }function validateModalOnSubmit() {
    var validate;

    if(!hasEmptyRequiredField() && (!hasChanges() || (subject.val().trim() && tinyMCE.activeEditor && tinyMCE.activeEditor.getContent().trim()))) {
      validate = true;
      $("#emailConfiguration div.alert").hide();
      noEmailOnNoDataValue.parent().removeClass("has-error");
      subject.parent().removeClass("has-error");
      body.parent().removeClass("field-validation-error");
    } else {
      if (!subject.val().trim()) {
        subject.parent().addClass("has-error");
          $("#emailConfiguration div.alert").show();
      } else {
        subject.parent().removeClass("has-error");
      }
      if (!tinyMCE.activeEditor.getContent().trim()) {
        body.parent().addClass("field-validation-error");
        $("#emailConfiguration div.alert").show();
      } else {
        body.parent().removeClass("field-validation-error");
      }
      validate = false;
    }

    return validate;
  }
}

function initializePastDatesNotAllowedDatePicker() {
  $(".pastDateNotAllowed").datepicker({
    allowPastDates: true,
    date: moment(
      moment().tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT),
      DEFAULT_DATE_DISPLAY_FORMAT
    ),
    restricted: [
      {
        from: -Infinity,
        to: moment(
          moment()
            .tz(userTimeZone)
            .subtract(1, "days")
            .format(DEFAULT_DATE_DISPLAY_FORMAT),
          DEFAULT_DATE_DISPLAY_FORMAT
        ),
      },
    ],
    momentConfig: {
      culture: userLocale,
      format: DEFAULT_DATE_DISPLAY_FORMAT,
    },
  });
}

$(document).on("preInit.dt", function(event){
    var table = $(event.target).DataTable();
    var searchInputTimeout = 700;
    table.searchInputTimer = null;
    $(event.target).closest(".dt-container")
        .find(".dt-search input[type='search']").off().keyup( function () {
        var searchString = this.value;
        if(table.searchInputTimer) {
            clearTimeout(table.searchInputTimer);
        }
        table.searchInputTimer = setTimeout(function (){
            table.search(searchString).draw();
        }, searchInputTimeout);
    });
});

function changeFavoriteState(id, state, icon) {
    $.ajax({
        url: toFavorite + "?id=" + id + "&state=" + state,
        dataType: "html",
    })
        .done(function (result) {
            if (icon.hasClass("glyphicon-star")) {
                icon.removeClass("glyphicon-star");
                icon.addClass("glyphicon-star-empty");
            } else {
                icon.removeClass("glyphicon-star-empty");
                icon.addClass("glyphicon-star");
            }
        })
        .fail(function (err) {
            errorNotification(
                (err.responseJSON.message ? err.responseJSON.message : "") +
                (err.responseJSON.stackTrace
                    ? "<br>" + err.responseJSON.stackTrace
                    : "")
            );
        });
}

function renderFavoriteIcon(data, type, row) {
  return row.isFavorite
    ? '<span class="glyphicon glyphicon-star favorite" data-exconfig-id="' +
        row.id +
        '" title="' +
        $.i18n._("app.favorite.label") +
        '"></span> '
    : '<span class="glyphicon glyphicon-star-empty favorite" data-exconfig-id="' +
        row.id +
        '" title="' +
        $.i18n._("app.favorite.label") +
        '"></span>';
}

function formatBlindedUserResult(label) {
  if (!label.blinded) {
    return "<span class='select2-label'>" + encodeToHTML(label.text) + "</span>";
  }
  return (
    '<span style="margin-left: -18px;"><i class="fa fa-eye-slash"  title="' +
    $.i18n._("app.template.blinded") +
    '"></i> ' +
    encodeToHTML(label.text) +
    "</span>"
  );
}

function canEditIcon(label) {
  const editableBy = $(this).closest(".editableBy").find("#executableBy");
  const isInEditable = !!editableBy
    .val()
    ?.split(";")
    .find((el) => el === label.id);
  return `<span data-id="${label.id}" class="${
    isInEditable ? "primary" : ""
  } indicator fa fa-edit" title="${$.i18n._(
    "app.sharedWith.canEdit"
  )}"></span>`;
}

function formatBlindedUserSelection(label) {
  if (!label.blinded) {
    return (
      canEditIcon.call(this, label) +
      "<span class='select2-label'>" +
      encodeToHTML(label.text) +
      "</span>"
    );
  }
  return (
    "" +
    canEditIcon(label) +
    '<span><i class="fa fa-eye-slash"  title="' +
    $.i18n._("app.template.blinded") +
    '"></i> ' +
    encodeToHTML(label.text) +
    "</span>"
  );
}

function goToUrl(controller, action, params) {
  var href = "";
  if (params == null) {
    href = "/reports/" + controller + "/" + action;
  } else if (params != null) {
    href = "/reports/" + controller + "/" + action;
    var size = $.map(params, function (n, i) {
      return i;
    }).length;
    var len = size;
    $.each(params, function (key, value) {
      if (key === "id" && size === 1) {
        href += "/" + value;
      } else {
        if (size === len) {
          href += "?" + key + "=" + value + "&";
        } else {
          href += key + "=" + value + "&";
        }
        size--;
      }
    });

    if (href.substr(href.length - 1) === "&") {
      href = href.slice(0, -1);
    }
  }

  window.location.href = href;
}

function showLoaderMultiple() {
  loaderStack.push(1);
  showLoader();
}

function hiderLoaderMultiple() {
  loaderStack.pop();
  if (!loaderStack.pop()) {
    hideLoader();
  }
}

function showLoader() {
  pvui.common.showPVLoader();
}

function hideLoader() {
  pvui.common.hidePVLoader();
}

function encodeToHTML(string) {
  var temp = document.createElement("div");
  temp.textContent = string;
  return temp.innerHTML;
}

function decodeFromHTML(encodedString) {
  var textArea = document.createElement("textarea");
  textArea.innerHTML = encodedString;
  return textArea.value;
}

function extractTextFromElement($element) {
  const isMultiline = $element.html().includes('<br>');
  if (!isMultiline) {
    return $element.text();
  } else {
    const tempElement = document.createElement("div");
    tempElement.innerHTML = $element.html().replace(/<br>/g, "\r");
    return $(tempElement).text();
  }
}

function isValidPattern(term) {
  return !term.match(/[\^$|?*+()=]/);
}

function replaceBracketsAndQuotes(string) {
  return string != undefined
    ? string.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;")
    : "";
}

function messageOnJSONUpload(ele_id, message, ele_class) {
  $(ele_id)
    .html(
      '<div class="alert alert-dismissable' +
        ele_class +
        '">' +
        '<button type="button" class="close" ' +
        'data-dismiss="alert" aria-hidden="true">' +
        "&times;" +
        "</button>" +
        message +
        "</div>"
    )
    .show();
}

function checkEmailFormatInACommaSeperatedList(listString) {
  if (listString == "") return true;

  var emailList = listString.toString().replace(/\s/g, "").split(",");
  var valid = true;
  var regex =
    /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

  for (var i = 0; i < emailList.length; i++) {
    if (emailList[i] == "" || !regex.test(emailList[i])) {
      valid = false;
    }
  }
  return valid;
}

//https://gasparesganga.com/labs/jquery-loading-overlay/
function showDataTableLoader($selector) {
  pvui.common.pvWidgetLoader.hideLoader($selector);
  pvui.common.pvWidgetLoader.showLoader($selector);
}

function hideDataTableLoader($selector) {
  pvui.common.pvWidgetLoader.hideLoader($selector);
}

function showModalLoader($selector) {
  pvui.common.pvWidgetLoader.showLoader($selector.find("div.modal-content"));
}

function hideModalLoader($selector) {
  pvui.common.pvWidgetLoader.hideLoader($selector.find("div.modal-content"));
}

$.extend(true, $.fn.dataTable.defaults, {
  processing: true,
  language: {
    url: APP_ASSETS_PATH + "i18n/dataTables_" + userLocale + ".json",
    processing: pvui.common.showPVLoader(),
  },
});

function iniFrameInit() {
  if (window.location !== window.parent.location) {
    $(".topbar").hide();
    $(".side-menu").hide();
    $("#mainContent").css("padding", "0").css("margin-left", "-60px");
    $("#wrapper").attr("style", "padding-top: 5px !important;");
  }
}

$(function () {
  // workaround for ".val() on <textarea> elements strips carriage return characters from the browser-reported value"
  // see https://api.jquery.com/val/
  $.valHooks.textarea = {
    get: function (textarea) {
      if ($(textarea).hasClass("multiline-text")) {
        return textarea.value.replace(/\r?\n/g, "\r\n");
      } else {
        return textarea.value;
      }
    },
  };
  // extra symbols input prevent for multiline textareas - using .val() with configured valHooks.textarea>get
  $(document).on("input", "textarea.multiline-text[maxlength]", function () {
    var maxLength = parseInt($(this).attr("maxlength"));
    var value = $(this).val();
    if (value.length > maxLength) {
      $(this).val(value.slice(0, maxLength));
    }
  });

  // Calling iniFrame function
  iniFrameInit();
  var productGrpSelectLabelExt = $.i18n._("app.product.group.select.label");
  var productGrpUpdateLabelExt = $.i18n._("app.product.group.update.label");

  // To handle custom error of datatables globally.
  $.fn.dataTable.ext.errMode = function (settings, tn, msg) {
    try {
      console.log(
        "An error has been reported by DataTables: ",
        settings,
        tn,
        msg
      );
    } catch (e) {}
    alert($.i18n._("datatable.load.data.error"));
  };

  //https://datatables.net/forums/discussion/45737/the-search-input-length-limit
  $(document).on("preInit.dt", function (e,settings) {
    $(".dt-search input[type='search']").attr("maxlength", 1500);
  var tableId = settings.sTableId;
        if (tableId === "taskTemplateList") {
            $(".dt-search input[type='search']").attr("maxlength", 4000);
        }
    });

  $(".dataTable").on("processing.dt", function (e, settings, processing) {
    try {
      if (settings.oInit.customProcessing) {
        if (processing) {
          showDataTableLoader($(e.target));
        } else {
          hideDataTableLoader($(e.target));
        }
      }
    } catch (e) {
      console.error("Error while reading settings of datatable");
    }
  });

  $(".modal.auto-close-alerts").on("hidden.bs.modal", function () {
    $(this).find(".alert").alert("close");
  });

  $(document).on("click", ".createAdhocFromTemplate", function () {
    $("#createFromTemplateModal .alert-dismissable").hide();
    $("#createFromTemplateModal").modal("show");
    initTableTemplateConfiguration(
      APP_PATH + "/configuration/listTemplates",
      APP_PATH + "/configuration/createFromTemplate"
    );
  });

  $(document).on("click", ".createAggregateFromTemplate", function () {
    $("#createFromTemplateModal .alert-dismissable").hide();
    $("#createFromTemplateModal").modal("show");
    initTableTemplateConfiguration(
      APP_PATH + "/periodicReport/listTemplates",
      APP_PATH + "/periodicReport/createFromTemplate"
    );
  });

    $("#currentUserTenantSelect").select2().on("select2-selecting", function (e) {
        var tenantId = e.val;
        $("#currentUserTenantSelect").select2('close');
        if (!tenantId) {
            return false;
        }
        $('#TenantWarningModal').modal('show');

        $("#continueTenantSwitch").off().click(function () {
            location.href = APP_PATH + "/preference/updateCurrentTenant?tenantId=" + tenantId;
            return false;
        });

        return false;
    });

  var table = null;

  function initTableTemplateConfiguration(listUrl, createUrl) {
    if (table !== null) table.destroy();
    table = $("#rxTableTemplateConfiguration")
      .DataTable({
        //"sPaginationType": "bootstrap",
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
        language: {
          search: '',
          searchPlaceholder: $.i18n._("fieldprofile.search.label"),
        },
        stateSave: false,
        stateDuration: -1,
        // "processing": true,
        serverSide: true,
        ajax: {
          url: listUrl,
          type: "POST",
          dataSrc: "data",
          data: function (d) {
            d.searchString = d.search.value;
            if (d.order.length > 0) {
              d.direction = d.order[0].dir;
              d.sort = d.columns[d.order[0].column].data;
            }
          },
        },
        aaSorting: [],
        order: [[0, "asc"]],
        bLengthChange: true,
        iDisplayLength: 10,
        aLengthMenu: [
          [10, 50, 100],
          [10, 50, 100],
        ],
        pagination: true,

        drawCallback: function (settings) {
          if (
            typeof settings.json == "undefined" ||
            (Array.isArray(settings.json) && settings.json.length === 0)
          ) {
            settings.json = { recordsFiltered: 0 };
          }
          pageDictionary(
            $("#rxTableTemplateConfiguration_wrapper")[0],
            settings.aLengthMenu[0][0],
            settings.json.recordsFiltered
          );

          getReloader(
            $(".rxTableTemplateConfiguration_info"),
            $("#rxTableTemplateConfiguration")
          );
        },
        initComplete: function () {
          $("#rxTableTemplateConfiguration .dt-empty").attr(
            "colspan",
            "3"
          );
        },
        aoColumns: [
          {
            mData: "reportName",
            sClass: "add-cursor",
            mRender: function (data, type, row) {
              return data
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");
            },
          },
          {
            mData: "description",
            sClass: "add-cursor",
            mRender: function (data, type, row) {
              var text =
                data == null
                  ? ""
                  : data
                      .replace(/&/g, "&amp;")
                      .replace(/</g, "&lt;")
                      .replace(/>/g, "&gt;");
              return '<div class="comment">' + text + "</div>";
            },
          },
          {
            mData: "dateCreated",
            aTargets: ["dateCreated"],
            sClass: "dataTableColumnCenter forceLineWrapDate add-cursor",
            mRender: function (data, type, full) {
              return moment
                .utc(data)
                .tz(userTimeZone)
                .format(DEFAULT_DATE_TIME_DISPLAY_FORMAT);
            },
          },
        ],
      })
      .on("xhr.dt", function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json);
      })
      .on("click", "tbody tr", function () {
        $("#createFromTemplateModal").modal("hide");
        const id = table.row(this).data()?.["id"];
        if (!id) {
          return;
        }
        window.location = createUrl + "?id=" + id;
      });
  }

  $("#qualityChecked").on("click", function () {
    showTemplateCheckbox();
  });

  function showTemplateCheckbox() {
    if (
      $("#qualityChecked").is(":checked") &&
      hasConfigTemplateCreatorRole == "true"
    ) {
      $(".isTemplate").show();
    } else {
      $(".isTemplate").prop("checked", false);
      $(".isTemplate").hide();
    }
  }

  showTemplateCheckbox();

  $("#search-box").on("click", function () {
    $(".columnSearch").toggle();
    $(".columnSearchCll").toggle();
  });

  $(document).on('click', '[data-evt-clk]', function(e) {
      e.preventDefault();
      const eventData = JSON.parse($(this).attr("data-evt-clk"));
      const methodName = eventData.method;
      const params = eventData.params;

    switch (methodName) {
      case "closeAllCopyPasteModals":
        closeAllCopyPasteModals();
        break;
      case "goToUrl":
        goToUrl(...params);
        break;
      case "closeAllImportValueModal":
        closeAllImportValueModal();
        break;
      case "readMore":
        readMore($(this)[0]);
        break;
      case "modalHide":
        $(params[0]).modal('hide');
        break;
      case "divHide":
        $(params[0]).hide();
        break;
      case "modalShow":
        $(params[0]).modal('show');
        break;
      case "showLoader":
        showLoader();
        break;
      case "addClassHide":
        $(this).closest(params[0]).hide();
        break;
      case "selectMethod":
        $(params[0]).select();
        break;
      case "setDataValue":
        $(params[0]).val(params[1]);
        break;
      case "preventDefault":
        event.preventDefault();
        break;
      case "copyToClipboard":
        copyToClipboard(params[0]);
        break;
      default :
        $(document).trigger("data-clk", $(this));
    }
  });

  $(document).on('mouseover', '[data-evt-mouseOver]', function() {
    const eventData = JSON.parse($(this).attr("data-evt-mouseOver"));
    const methodName = eventData.method;
    const params = eventData.params;
    // Call the method from the eventHandlers object with the params
    if (methodName == 'xmlMouseOver') {
      var id = $(this).attr("id");
      $("#"+id).attr("title", $(this)[0].value.trim());
    }
  });

  $(document).on('input', '[data-evt-input]', function(e) {
    e.preventDefault();
    const eventData = JSON.parse($(this).attr("data-evt-input"));
    if (eventData.method == "validateInput") {
      if (!this.validity.valid) {
        this.value = ''; // Clear the value if invalid
      }
    }
  });
});

function showTotalPage(totalCase) {}

function pageDictionary(table, miminumLengthMenu, totalCase) {
    var displayLength = $(table).find(".dt-length");
    var displayInfo = $(table).find(".dt-info")[0];
    var displayPagination = $(table).find(".dt-paging");
    displayLength.parent().attr("style", "margin-top: 15px;");
    if (totalCase < miminumLengthMenu) {
      displayLength.hide();
      displayPagination.hide();
    } else {
      displayLength.show();
      displayPagination.show();
      showTotalPageOnDashboard(table, totalCase);
    }
}

function showTotalPageOnDashboard(table, totalCase) {
  var element = $(table).find(".dt-info")[0];
  if (element) {
    element.innerText = $.i18n._("data.of") + " " + totalCase + " " + $.i18n._("data.entries");
  }
}

function replaceAndFormatRows(value) {
  if (value === null) {
    value = "";
  }
  value = ("" + value).replace(/(?:\r\n|\r|\n)/g, "<br>");
  if (value.startsWith('"') && value.endsWith('"')) {
    value = value.substring(1, value.length - 1);
  }
  value = value.replace(/\"\"/g, '"');
  return value;
}

function showModalLoader($selector) {
  showLoader();
}

function hideModalLoader($selector) {
  hideLoader();
}

function updatePopoverWithLatestComment(popover) {
  var popoverSelector = $(popover);
  var parentCommentButtonSelector = popoverSelector.parent().parent();
  var ownerId = parentCommentButtonSelector.attr("data-owner-id");
  var commentType = parentCommentButtonSelector.attr("data-comment-type");
  $.ajax({
    url: $("#appContext").val() + "/commentRest/loadLatestComment",
    data: {
      ownerId: ownerId,
      commentType: commentType,
    },
    method: "POST",
    dataType: "html",
  })
    .done(function (result) {
      popoverSelector.attr("data-content", encodeToHTML(result));
      popoverSelector.data("bs.popover").setContent();
    })
    .fail(function () {
      alert("Sorry! System level error");
    });
}

function bindPopOverEvents(popover) {
  popover.popover({
    trigger: "hover focus",
    html: true,
  });
  popover.on("show.bs.popover", function () {
    $(popover).attr(
      "data-content",
      encodeToHTML($(popover).attr("data-content"))
    );
    updatePopoverWithLatestComment(this);
  });
}

function unescapeHTML(esacpedHtml) {
  return esacpedHtml != undefined
    ? esacpedHtml
        .toString()
        .replace(/&amp;/g, "&")
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&quot;/g, '"')
        .replace(/&#039;/g, "'")
    : "";
}

function escapeHTML(unesacpedHtml) {
  return unesacpedHtml != undefined
    ? unesacpedHtml
        .toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;")
    : "";
}

function encodeToHTML(str) {
  var temp = document.createElement("div");
  temp.textContent = str;
  var res = temp.innerHTML;
  temp.remove();
  return res;
}
/******************************************* Data table Column Ellipsis**************************************************/
var colEllipsis = function () {
  $(".col-height").each(function () {
    if ($(this).height() > 30 || $(this).width() > $(this).parent().width()) {
      $(this).find(".ico-dots").css("display", "inline-block");
    } else {
      $(this).find(".ico-dots").css("display", "none");
    }
  });
};

function focusRow(selector) {
    var allRow = selector[0].getElementsByTagName('tr');
    var currRowIndex = -1;
    $(selector).on('mouseover', 'tr', function () {
        var unfreezedRow = document.getElementsByClassName('DTFC_LeftBodyLiner')[0].getElementsByTagName('table')[0].getElementsByTagName('tbody')[0].getElementsByTagName('tr');
        currRowIndex = this.rowIndex - 1;
        $(this).addClass('curr-row');
        allRow[currRowIndex].classList.add('curr-row');
        unfreezedRow[currRowIndex].classList.add(('curr-row'));
    }).on('mouseleave', 'tr', function () {
        var unfreezedRow = document.getElementsByClassName('DTFC_LeftBodyLiner')[0].getElementsByTagName('table')[0].getElementsByTagName('tbody')[0].getElementsByTagName('tr');
        allRow[currRowIndex].classList.remove('curr-row');
        unfreezedRow[currRowIndex].classList.remove(('curr-row'));
        $(this).removeClass('curr-row');
        currRowIndex = -1;
    });
}

function initPSGrid(container) {
  var psGrid;
  psGrid = new PerfectScrollbar(container.find(".dt-scroll-body")[0]);
}

function naturalNumberInputCheck(field, inputText) {
  var valueAsString = field.val()
    ? field.val().toString() + inputText
    : inputText;
  var maxValue = parseInt(field.attr("max")) || 999999999;
  var minValue = parseInt(field.attr("min")) || 0;
  var value = parseInt(valueAsString);
  return !(
    !/^[0-9]+$/.test(valueAsString) ||
    value > maxValue ||
    value < minValue
  );
}

function limitContenteditableMaxLength(event) {
  var editableElement = $(event.target);
  var maxLength = editableElement.attr("maxlength");

  var allowedKeysPressed = false;
  if (event.type === "keydown") {
    var ctrlPressed = (event.ctrlKey || event.metaKey) === true;
    allowedKeysPressed =
      event.which === 8 /* Backspace */ ||
      event.which === 35 /* End */ ||
      event.which === 36 /* Home */ ||
      event.which === 37 /* Left */ ||
      event.which === 38 /* Up */ ||
      event.which === 39 /* Right*/ ||
      event.which === 40 /* Down */ ||
      event.which === 46 /* Del*/ ||
      (ctrlPressed && event.which === 65) /* Ctrl+A */ ||
      (ctrlPressed && event.which === 88) /* Ctrl+X */ ||
      (ctrlPressed && event.which === 67) /* Ctrl+C */ ||
      (ctrlPressed && event.which === 86) /* Ctrl+V */ ||
      (ctrlPressed && event.which === 90) /* Ctrl+Z */;
  } else if (event.type === "paste") {
    setTimeout(function () {
      editableElement.text(editableElement.text().slice(0, maxLength));
    });
  }

  if (!allowedKeysPressed && editableElement.text().length >= maxLength) {
    event.preventDefault();
  }
}

function naturalNumberInputCheck(field, inputText) {
  var valueAsString = field.val()
    ? field.val().toString() + inputText
    : inputText;
  var maxValue = parseInt(field.attr("max")) || 999999999;
  var minValue = parseInt(field.attr("min")) || 0;
  var value = parseInt(valueAsString);
  return !(
    !/^[0-9]+$/.test(valueAsString) ||
    value > maxValue ||
    value < minValue
  );
}

$(function () {
  $(document).on(
    "keydown paste",
    "[contenteditable='true'][maxlength]",
    limitContenteditableMaxLength
  );

  $(document).on("click", ".inline-alert-close", function () {
    $(this).closest(".closable-inner-alert").hide();
  });

  $(document).on(
    "keypress",
    ".natural-number, .natural-number-filter",
    function (event) {
      return naturalNumberInputCheck($(this), event.key.toString());
    }
  );

  $(document).on(
    "paste",
    ".natural-number, .natural-number-filter",
    function (event) {
      var clipboardText = event.originalEvent.clipboardData.getData("text");
      return naturalNumberInputCheck($(this), clipboardText);
    }
  );

  $(document).on('keydown', 'form.field-enter-submit-off input:not([type=submit]):not([type=button])', function(event) {
    if(event.keyCode === 13) {
      //disable form submit on enter pressed in form fields
      event.preventDefault();
      return false;
    }
  });

  //workaround for bootstrap issue when a modal applies additional right padding to the body to compensate scroll,
    //but does not revert it after the modal closing (e.g. in OS Windows)
    $(document).on("show.bs.modal", ".modal", function () {
        $('body').attr('data-right-padding', $('body').css('padding-right'));
    });
    $(document).on("hidden.bs.modal", ".modal", function () {
        var bodyRightPadding = $('body').attr('data-right-padding');
        if (bodyRightPadding !== $('body').css('padding-right')) {
            $('body').css('padding-right', bodyRightPadding);
        }
    });

    // Add auto flip dropdown/dropup menu depending on screen position
  $(document)
    .on("shown.bs.dropdown", ".dropdown", function () {
      // calculate the required sizes, spaces
      var $ul = $(this).children(".dropdown-menu");
      if ($ul.length == 0) return;
      var $button = $(this).children(".dropdown-toggle");
      $(".pv-grp-btn").removeClass("active");
      $(this).toggleClass("active");
      var ulOffset = $ul.offset();
      // how much space would be left on the top if the dropdown opened that direction
      var spaceUp =
        ulOffset.top -
        $button.height() -
        $ul.height() -
        $(window).scrollTop() -
        ($(this).parent(".dataTables_scrollHead").height() ?? 0) -
        ($(this).parent(".rxmain-container-header").height() ?? 0);

      if ($("#qualityChart").length) {
        // use this to check if quality Chart container exists
        spaceUp -= inViewport($("#qualityChart"));
      }

      function inViewport($el) {
        var elH = $el.outerHeight(),
          H = $(window).height(),
          r = $el[0].getBoundingClientRect(),
          t = r.top,
          b = r.bottom;
        return Math.max(0, t > 0 ? Math.min(elH, H - t) : Math.min(b, H));
      }

      // how much space is left at the bottom
      var spaceDown = $(document).height() - (ulOffset.top + $ul.height());
      //Taking drop down menu height, so in future if the listing increased in menu, it will remain dynamic
      var dropDownMenuHeight = $ul.height();
      //switch to dropdown only if there's space available at the bottom
      if (spaceDown > 0 || dropDownMenuHeight > spaceUp) {
        $(this).addClass("dropdown");
      } else {
        $(this).addClass("dropup");
      }
    })
    .on("hidden.bs.dropdown", ".dropdown", function () {
      // always reset after close
      $(this).removeClass("dropup");
    });

  $(".withCharCounter").each(function () {
    initTexareaRemainingChar($(this));
  });
  $(document).on("keyup change", ".withCharCounter", function () {
    if (
      $(this).parent().find(".textAreaCharCounter").length == 0 ||
      !$._data(this, "events") ||
      !_.find($._data(this, "events").keyup, function (el) {
        return el.handler && el.handler.name == countChars.name;
      })
    )
      initTexareaRemainingChar($(this));
  });
});
var countChars = function (e) {
  $(e.currentTarget);
  var remainingCharsDiv = $(e.currentTarget)
    .parent()
    .find(".textAreaCharCounter");
  remainingCharsDiv.show();
  var text_length = $(e.currentTarget).val().length;
  remainingCharsDiv.html(
    text_length + "/" + $(e.currentTarget).attr("maxlength")
  );
};
function initTexareaRemainingChar(control) {
  var maxLength = control.attr("maxlength");
  if (maxLength && maxLength > 0) {
    var remainingCharsDiv;
    if (!control.parent().hasClass("textAreaCharCounterWrapper")) {
      control.wrap(
        "<div class='textAreaCharCounterWrapper' style='position: relative;width: 100%;'></div>"
      );
      remainingCharsDiv = $(
        "<div class='textAreaCharCounter' style='font-size: 10px; display:none;opacity: 0.5; position: absolute;width: 100%;text-align: right;' ></div>"
      );
      control.after(remainingCharsDiv);
    } else {
      remainingCharsDiv = control.parent().find(".textAreaCharCounter");
      remainingCharsDiv.hide();
    }
    var text_length = control.val().length;
    remainingCharsDiv.html(text_length + "/" + maxLength);
    if (
      !_.find($._data(this, "events").keyup, function (el) {
        return el.handler && el.handler.name == countChars.name;
      })
    ) {
      $(control).on("keyup change", countChars);
    }
  }
}

var customSortDataTableIds = ["workflowRuleList", "workflowStateList"];
customSortDataTableIds.forEach(function (tableId) {
  jQuery.fn.dataTableExt.oSort[tableId + "-string-asc"] = function (a, b) {
    var x = a === null || a === "" ? "\uFFFF" : a;
    var y = b === null || b === "" ? "\uFFFF" : b;
    return x < y ? -1 : x > y ? 1 : 0;
  };
  jQuery.fn.dataTableExt.oSort[tableId + "-string-desc"] = function (a, b) {
    var x = a === null || a === "" ? "\uFFFF" : a;
    var y = b === null || b === "" ? "\uFFFF" : b;
    return x < y ? 1 : x > y ? -1 : 0;
  };
});

function addReadMoreButton(field, noOfChars) {
  //Start - Implement Read More/Read Less feature
  var contents = document.querySelectorAll(field);
  contents.forEach(function (content) {
    $(content).after(
      '<button class="btn btn-default btn-xs workflowButton" style="min-width: 10px; line-height: 1.20; font-size:12px"  data-evt-clk=\'{\"method\": \"readMore\", \"params\":[]}\'>' +
        $.i18n._("readMore") +
        "</button>"
    );
    if (content.textContent.length < noOfChars) {
      content.nextElementSibling.style.display = "none";
    } else {
      var displayText = content.textContent.slice(0, noOfChars);
      var moreText = content.textContent.slice(noOfChars);
      content.innerHTML =
        displayText +
        '<span class="dots">...</span><span class="hide more">' +
        moreText +
        "</span>";
    }
  });
  //End - Implement Read More/Read Less feature
}

function readMore(btn) {
  var post = btn.parentElement;
  post.querySelector(".dots").classList.toggle("hide");
  post.querySelector(".more").classList.toggle("hide");
  btn.textContent =
    btn.textContent === $.i18n._("readMore")
      ? $.i18n._("readLess")
      : $.i18n._("readMore");
}

$(document).on('click', ".closeError", function () {
    $(this).closest('.alert').hide();
});

function setSelect2InputWidth(selector) {
  var selectorField = $(selector).parent();
  var selectorWidth = $(selectorField).find(".select2-selection__rendered").width();
  $(selectorField).find(".select2-search__field").css("width", selectorWidth);
}

function initializeSelect2(selector) {
  $(selector).select2({
    language: {
      noResults: function () {
        return "No matches found";
      }
    },
    matcher: function (params, data) {
      if (!data.text) {
        return null;
      }
      const searchTerm = $.trim(params.term).toLowerCase();
      const optionsText = data.text.toLowerCase();
      // If there is no search term, show unselected options only
      if (!searchTerm) {
        return data.selected ? null : data;
      }
      // Show unselected options that match the search term
      if (optionsText.includes(searchTerm) && !data.selected) {
        return data;
      }
      return null;
    }
  });
}

function showEditDiv(parent, div, enterField, leftShift, checkBoundaries) {
  var position = parent.offset();
  let left = position.left - (leftShift ? leftShift : 0);
  const top = position.top + 25;
  const leftPadding = 25;
  div.css("left", left).css("top", top).show();

  if (checkBoundaries === true) {
    const divClientRect = div[0].getBoundingClientRect();
    const widthOffset = window.innerWidth - (divClientRect.x + divClientRect.width);
    left = widthOffset < leftPadding ? left + widthOffset - leftPadding : divClientRect.x < 0 ? leftPadding : left;
    div.css("left", left);
  }

  if (enterField) {
    enterField.on("keydown", function (evt) {
      evt = evt || window.event;
      if (evt.keyCode == 13) {//27 is the code for Enter
        div.find(".saveButton").click();
      }
    });
  }
}

$(document).on("click", ".popupBox .cancelButton", function () {
  $(".popupBox").hide();
  $(".saveButton").off();
});
$(document).on("mouseup scroll", function (e) {
  var container = $(".popupBox:visible");
  if (container.length === 0) return;
  if (e.type == "scroll" && checkVisible(container[0])) return;
  if (!$(e.target).hasClass("select2-drop-mask")
      && !($(e.target).closest('.select2-dropdown').length > 0)
      && !container.is(e.target) && container.has(e.target).length === 0) {
    container.hide();
    container.find(".select2-offscreen").select2("close");
  }

  function checkVisible(elm) {
    var rect = elm.getBoundingClientRect();
    var viewHeight = Math.max(document.documentElement.clientHeight, window.innerHeight);
    // var viewWidth = Math.max(document.documentElement.clientWidth, window.innerWidth);
    var top = $(".fixedHeader-floating").height();
    if (!top) top = 0
    top = top + 60;
    // return !(rect.top < top || rect.top - viewHeight >= 0) || !(rect.left < 0 || rect.right-viewWidth >=0);
    return !(rect.top < top || rect.top - viewHeight >= 0);
  }

});

function isAlreadyFormatted(date) {
  return moment(date, "DD-MMM-YYYY", true).isValid();
}
// Function to check if the date is in "YYYY/MM/DD" format and convert if necessary
function formatJapaneseDate(date) {
  if (isAlreadyFormatted(date)) {
    return date;
  }
  return moment(date, "YYYY/MM/DD", true).isValid()
      ? moment(date, "YYYY/MM/DD").format("DD-MMM-YYYY")
      : "Invalid date";
}

$(function () {
  $(document).find(".showQueryStructure").on('mouseenter', function () {
    $(this).popover('show');
  }).on('mouseleave', function () {
    $(this).popover('hide');
  });
});