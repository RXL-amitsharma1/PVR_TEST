var $notificationContainer;
var notificationCount = 0;
var $notificationRows;
var $notificationHeader;

// NotificationLevel values
var N_LEVEL_INFO_NAME = "Information";
var N_LEVEL_WARN_NAME = "Warning";
var N_LEVEL_ERROR_NAME = "Error";
var INDEX = "index";
var SHOW = "show";
var PREVIEW = "Preview of";

var userNotificationSubscribe = function () {
  var client = new StompJs.Client({
    brokerURL: notificationWSURL,
    onConnect: () => {
      client.subscribe(notificationChannel, function (messageData) {
        var data = messageData.body;
        if (data == "X") {
          return;
        }
        var message = JSON.parse(data);
        if (message == "") {
          return;
        }
        if (message.delete) {
          $(".list-group-item[notificationid=" + message.id + "]").detach();
          setNotificationCount(notificationCount - 1);
          return
        }
        if (message.id) {
          addNotification($notificationRows, message);
          setNotificationCount(notificationCount + 1);
          triggerNotificationEvent(message);
        }
      });
    }
  });
  client.onWebSocketError = (error) => {
    console.error('Error with websocket!', error);
  }
  client.activate();
};

$(function () {
  refershNotifications();
  $notificationContainer = $("#notificationContainer");
  $notificationRows = $("#notificationRows");
  $notificationHeader = $("#notificationHeader");
  $(document).on("click", "#menuNotification", function () {
    if ($notificationContainer.is(":visible")) {
      $notificationContainer.hide();
    } else {
      $notificationContainer.show();
    }
  });

  $(document).on("click", "#settingIcon", function () {
    $(".nav-item").removeClass("active");
  });

  $(document).on("click", "#notificationContainer", function () {
    //        event.stopPropagation();
    event.preventDefault();
    return false;
  });

  $(document).on("click", "#clearNotifications", function () {
    // Call server method to delete all notifications for this user.
    if (notificationCount > 0) {
      $.post(
        notificationDeleteByUserURL,
        { id: $(this).attr("userId") },
        function (result) {
          // result is true/false if deletion was successful.
        },
        "text"
      );

      $notificationRows.empty();

      setNotificationCount(0);
    }

    //        event.stopPropagation();
    return false;
  });

  $(document).on("click", "#notificationRows a", function () {
    var notificationRow = $(this);

    var link = $(notificationRow).attr("executedconfigid");
    var id = $(notificationRow).attr("executionstatusid");
    var notificationId = $(notificationRow).attr("notificationid");
    var notificationParameters = $(notificationRow).attr("notificationParameters");
    var hasError = $(notificationRow).find(".noti-danger").length > 0;
    var appName = $(notificationRow).attr("appName");
    var preview = $(notificationRow).text();
    var showCases = preview.indexOf(PREVIEW);
    var currentContextPath = window.location.pathname.substring(
      0,
      window.location.pathname.indexOf("/", 2));
    var exIcsrTemplateQueryId = $(notificationRow).attr(
      "executedTemplateQueryId"
    );
    var caseNumber = $(notificationRow).attr("caseNumber");
    var versionNumber = $(notificationRow).attr("versionNumber");
    var isInDraftMode = $(notificationRow).attr("isInDraftMode");
    var fileName = $(notificationRow).attr("fileName");

    if (hasError && !caseNumber && appName !== "inboundCompliance" && appName !== 'error') {
      if (appName === "download") {
        return false;
      }
      var notificationid = $(notificationRow).attr("notificationid");
      window.location.href = viewNotificationErrorURL + "/" + notificationid;
      return;
    }

    if (showCases == 1) {
      window.location.href =
        currentContextPath + "/" + "caseList/index?cid=" + id;
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "actionItem"
    ) {
      window.location.href = currentContextPath + "/actionItem/index?id=" + id;
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "quality"
    ) {
      window.location.href =
        currentContextPath + "/quality/actionItems?id=" + id;
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "caseSeries"
    ) {
      window.location.href =
        currentContextPath + "/" + "caseList/index?cid=" + id;
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "reportRequest"
    ) {
      window.location.href =
        currentContextPath + "/" + "reportRequest/show?id=" + id;
    } else if (typeof appName != "undefined" && appName && appName === "pvcReport") {
        window.location.href = reportRedirectURL + "/" + id;
    } else if (typeof appName != "undefined" && appName && appName === "export") {
        var args = JSON.parse(notificationParameters);
        //to exclude redundant the leave site message displaying beforeunload events temporary will be off
        let beforeunloadEvents = [];
        //getting beforeunload events before off ones
        $.each(jQuery._data(window, 'events').beforeunload || [], function (index, value) {
          beforeunloadEvents.push(value.handler.bind({}));
        })
        $(window).off('beforeunload');
        window.location.href = currentContextPath + "/advancedReportViewer/downloadAsyncExportFile" + "?id=" + notificationId + "&sourceFileName=" + args.sourceFileName + "&userFileName=" + args.userFileName;
        //attaching beforeunload events back after window.location.href is changed
        $.each(beforeunloadEvents, function (index, value) {
          $(window).on('beforeunload', value);
        });
    } else if (typeof appName != "undefined" && appName && appName === "error") {
        //to action
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "comments" &&
      !hasError
    ) {
      window.location.href =
        currentContextPath +
        "/" +
        "icsr/showReport?exIcsrTemplateQueryId=" +
        exIcsrTemplateQueryId +
        "&caseNumber=" +
        caseNumber +
        "&versionNumber=" +
        versionNumber +
        "&isInDraftMode=" +
        isInDraftMode;
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "inboundCompliance"
    ) {
      var path = hasError ? "executionError" : "viewExecutedConfig";
      window.location.href =
        currentContextPath + "/" + "executedInbound/" + path + "?id=" + link;
    } else if (
      typeof appName != "undefined" &&
      appName &&
      appName === "download"
    ) {
      window.location.href =
        notificationBulkDownloadIcsrURL + "?name=" + fileName;
      $.post(
        notificationDeleteURL,
        { id: $(notificationRow).attr("notificationId") },
        function (result) {},
        "text"
      );
      $(notificationRow).remove();
      notificationCount = notificationCount - 1;
      setNotificationCount(notificationCount);
      return false;
    } else {
      // Redirect to the clicked report's criteria page if we can
      if (link && !hasError) {
        window.location.href = reportRedirectURL + "/" + link;
      }
    }
    return false;
  });

  $(document).on("click", ".removeNotification", function () {
    var notificationRow = $(this).closest("a");

    $.post(
      notificationDeleteURL,
      { id: $(notificationRow).attr("notificationId") },
      function (result) {
        // result is true/false if deletion was successful.
      },
      "text"
    );
    $(notificationRow).remove();
    notificationCount = notificationCount - 1;
    setNotificationCount(notificationCount);

    //        event.stopPropagation();
    return false;
  });
  userNotificationSubscribe();
});

function refershNotifications() {
  $.ajax({
    url: notificationURL,
    cache: false,
    dataType: "json",
  }).done(function (data) {
    $notificationRows.empty();
    // Sorts notifications by date, oldest to newest as we are using prepend
    data.sort(function (a, b) {
      return (
        new Date(a.dateCreated).getTime() - new Date(b.dateCreated).getTime()
      );
    });

    if (data.length > 0) {
      $.each(data, function () {
        addNotification($notificationRows, this);
      });
    }

    setNotificationCount(data.length);
  });
}

function addNotification($container, JSONObject) {
  var toAdd = document.createElement("a");
  toAdd.classList.add("list-group-item");
  toAdd.setAttribute("notificationId", JSONObject.id);
  toAdd.setAttribute("href", "#");
  if (JSONObject.executedConfigId && JSONObject.executedConfigId > 0) {
    toAdd.setAttribute("executedConfigId", JSONObject.executedConfigId);
  }

  //Set the app name
  if (JSONObject.appName) {
    toAdd.setAttribute("appName", JSONObject.appName);
  }
  if (JSONObject.executionStatusId) {
    toAdd.setAttribute("executionStatusId", JSONObject.executionStatusId);
  }

  if (JSONObject.notificationParameters) {
    if (JSONObject.appName == "download") {
      toAdd.setAttribute("fileName", JSONObject.notificationParameters);
    } else {
      var notificationParametersList = JSONObject.notificationParameters
        .split(" : ")
        .map(function (value) {
          var trimmedValue = value.trim();
          if (!isNaN(trimmedValue) && trimmedValue !== "") {
            return parseInt(trimmedValue);
          }
          return trimmedValue;
            });
            if (notificationParametersList && notificationParametersList.length == 4) {
                toAdd.setAttribute('caseNumber', notificationParametersList[0]);
                toAdd.setAttribute('versionNumber', notificationParametersList[1]);
                toAdd.setAttribute('executedTemplateQueryId', notificationParametersList[2]);
                toAdd.setAttribute('isInDraftMode', notificationParametersList[3]);
            } else {
                toAdd.setAttribute('notificationParameters', JSONObject.notificationParameters);
            }
        }
    }

  var divMedia = document.createElement("div");
  divMedia.classList.add("media");

  var divLeft = document.createElement("div");
  divLeft.classList.add("pull-left", "p-r-10");

  var iconLeft = document.createElement("em");
  iconLeft.classList.add("fa");

  if (JSONObject.level === N_LEVEL_INFO_NAME) {
    iconLeft.classList.add("noti-success");
    iconLeft.classList.add("fa-envelope");
  } else if (JSONObject.level === N_LEVEL_WARN_NAME) {
    iconLeft.classList.add("noti-warning");
    iconLeft.classList.add("fa-warning");
  } else if (JSONObject.level === N_LEVEL_ERROR_NAME) {
    iconLeft.classList.add("noti-danger");
    iconLeft.classList.add("fa-minus-circle");
  }

  divLeft.appendChild(iconLeft);

  var divMiddle = document.createElement("div");
  divMiddle.classList.add("media-body");

  var headerMiddle = document.createElement("h5");
  headerMiddle.classList.add("media-heading");
  headerMiddle.setAttribute("style", "overflow-wrap: break-word;");
  headerMiddle.appendChild(document.createTextNode(JSONObject.message));
  var parMiddle = document.createElement("p");
  parMiddle.classList.add("m-0");

  var timeSpan = document.createElement("span");
  timeSpan.classList.add("small");
  timeSpan.classList.add("italic");

  // 2015-09-03T22:42:55Z
  var relativeDate = moment
    .utc(JSONObject.dateCreated)
    .tz(userTimeZone)
    .fromNow();
  var textTime = document.createTextNode(relativeDate);

  timeSpan.appendChild(textTime);
  parMiddle.appendChild(timeSpan);

  divMiddle.appendChild(headerMiddle);
  divMiddle.appendChild(parMiddle);

  var divRight = document.createElement("div");
  divRight.classList.add("pull-right");

  var iconRight = document.createElement("em");
  iconRight.classList.add("fa");
  iconRight.classList.add("fa-times");
  iconRight.classList.add("fa-lg");
  iconRight.classList.add("removeNotification");

  divRight.appendChild(iconRight);

  divMedia.appendChild(divLeft);
  divMedia.appendChild(divMiddle);
  divMedia.appendChild(divRight);

  toAdd.appendChild(divMedia);
  $container.prepend(toAdd);
}

function setNotificationCount(count) {
  notificationCount = count;
  $("#notificationBadge").text(count);
  if (count == 1) {
    $notificationHeader.text($.i18n._("notificationEndOne", count));
  } else {
    $notificationHeader.text($.i18n._("notificationEndMultiple", count));
  }
}

function triggerNotificationEvent(JSONObject) {
  $(".chart-container").trigger("pushNotification", JSONObject);
}
