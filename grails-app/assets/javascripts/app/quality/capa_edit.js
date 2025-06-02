$(function () {
  var correctiveActions = [];
  var preventiveActions = [];
  var selectedIds = [];
  var selectAll;
  var CapaAttachId = [];
  var filename = [];
  var attachmentIdx = 0;
  var attachmentCount = 0;
  var attachmentList = 0;
  var isLargeFile = false;
  var capaInsId;
  $(".attachment-button").attr("disabled", true);
  var init = function () {
    $('#rxTableAttachment tbody tr').each(function () {
      $(".attachments").removeClass("hidden");
      $(".removes").removeClass("hidden");
    })
    init_table($("#corrective-actions"));
    init_table($("#preventive-actions"));
    $("#teamLead,#initiator,#approvedBy").select2({
      allowClear: true,
      placeholder: $.i18n._("placeholder.selectUsers"),
    });
    $("#teamMembers").select2({
      allowClear: true,
      placeholder: $.i18n._("placeholder.selectUsers"),
    });
      $('#mainContent').on('click', ".createActionItem", function () {
          if (!$(this).hasClass("disabled")) {
              if ($(this).attr('data-ownerType') === 'PVC')
                  actionItem.actionItemModal.init_action_item_modal(false, PV_CENTRAL_CAPA, this);
              else
                  actionItem.actionItemModal.init_action_item_modal(false, QUALITY_MODULE_CAPA, this);
          }
      });

    $("#attach_file_input").on("change", function (evt, numFiles, label) {
      $(".btn").removeAttr("disabled");
      $("#attach_file_name").val($("#attach_file_input").get(0).files[0].name);
      isLargeFile = false;
      if ($("#attach_file_input").get(0).files[0].size > AttachmentSizeLimit) {
        $("#attach_file_input").val("");
        isLargeFile = true;
      }
    });

    $("#mainContent").on("click", ".action-item-remove", function () {
      var actionItemId = $(this).attr("data-actionId");
      if ($(this).attr("data-ownerType") === "PVC")
        actionItem.actionItemModal.delete_action_item(
          actionItemId,
          false,
          PV_CENTRAL_CAPA
        );
      else
        actionItem.actionItemModal.delete_action_item(
          actionItemId,
          false,
          QUALITY_MODULE_CAPA
        );
    });

    $("#mainContent")
      .on("click", ".selectAllCheckbox", function (e) {
        if ($(".selectAllCheckbox").is(":checked")) {
          $(".selectCheckbox").prop("checked", true);
        }
      })
      .on("click", "input.selectAllCheckbox", function (e) {
        if ($(this).is(":checked")) {
          selectAll = true;
          selectedIds = [];
          $(".selectCheckbox").each(function () {
            var id = $(this).attr("_id");
            selectedIds.push(id);
            $(this).prop("checked", true);
          });
        } else {
          selectAll = false;
          selectedIds = [];
          $(".selectCheckbox").prop("checked", false);
        }
      });

    $("#mainContent").on("change", "input.selectCheckbox", function () {
      var isChecked = $(this).prop("checked");
      var id = $(this).attr("_id");
      var idx = findIdx(selectedIds, id);
      if (isChecked) {
        if (idx < 0) {
          if (id) {
            selectedIds.push(id);
          }
        }
      } else {
        if (idx >= 0) {
          selectedIds.splice(idx, 1);
        }
        if (selectAll == true) {
          $("#selectAll").prop("checked", false);
          selectAll = false;
        }
      }
    });

    $("#mainContent").on("click", ".attachments", function () {
      if (selectedIds.length > 0 || selectAll) {
        $("#selectAll").val(selectAll);
        $("#selectedIds").val(selectedIds);
        var formId = $("#attachForm");
        formId.attr("action", downloadAllAttachmentUrl);
        formId.trigger("submit");
      } else {
        $("#warningModal .description").text(
          $.i18n._("qualityModule.capa.attachment.download")
        );
        $("#warningModal").modal("show");
        $("#warningButton")
          .off("click")
          .on("click", function () {
            $("#warningModal").modal("hide");
          });
      }
    });

    $("#mainContent").on("click", ".removes", function () {
      if (selectedIds.length > 0 || selectAll) {
        var modal = $("#deleteModal");
        modal.modal("show");
        $("#deleteDlgErrorDiv").hide();
        modal
          .find("#deleteModalLabel")
          .text(
            $.i18n._("modal.delete.title", $.i18n._("app.label.attachment"))
          );
        modal.find("#nameToDelete").text($.i18n._("deleteAllAttachments"));
        $("#deleteButton").off();
        $("#deleteButton").on("click", function () {
          if (!$("#deleteJustification").val().trim()) {
            $("#deleteDlgErrorDiv").show();
          } else {
            $("#deleteDlgErrorDiv").hide();
            var deletejustification = $("#deleteJustification").val();
            $("#deletejustification").val(deletejustification);
            $("#selectAll").val(selectAll);
            $("#selectedIds").val(selectedIds);
            $("#attachForm").attr("action", removeAllAttachmentUrl);
            removeAttachments(selectedIds);
          }
        });
      } else {
        $("#warningModal .description").text(
          $.i18n._("qualityModule.capa.attachment.delete")
        );
        $("#warningModal").modal("show");
        $("#warningButton")
          .off("click")
          .on("click", function () {
            $("#warningModal").modal("hide");
          });
      }
    });

    $("#mainContent").on("click", ".remove", function () {
      attachmentIdx--;
      attachmentCount--;
      var attachId = $(this).attr("id");
       capaInsId = $(this).attr("capaId");
      var modal = $("#deleteModal");
      modal.modal("show");
      $("#deleteDlgErrorDiv").hide();
      modal
        .find("#deleteModalLabel")
        .text($.i18n._("modal.delete.title", $.i18n._("app.label.attachment")));
      modal
        .find("#nameToDelete")
        .text($.i18n._("deleteThis", $.i18n._("app.label.attachment")));
      $("#deleteButton").off();
      $("#deleteButton").on("click", function () {
        if (!$("#deleteJustification").val().trim()) {
          $("#deleteDlgErrorDiv").show();
        } else {
          $("#deleteDlgErrorDiv").hide();
          var deletejustification = $("#deleteJustification").val();
          $(
            "#deletejustification" ).val(
            deletejustification)
                        removeAttachments(attachId);
        }
      });
    });

    function findIdx(arItems, searchValue) {
      var found = false;
      var return_idx = -1;
      for (var i = 0; i < arItems.length && !found; i++) {
        if (arItems[i] == searchValue) {
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

    //Click event bind to the edit icon.
    $("#mainContent").on("click", ".action-item-edit", function () {
      var actionItemId = $(this).attr("data-actionId");
      if ($(this).attr("data-ownerType") === "PVC")
        actionItem.actionItemModal.edit_action_item(
          hasAccessOnActionItem,
          actionItemId,
          false,
          PV_CENTRAL_CAPA,
          null,
          this
        );
      else
        actionItem.actionItemModal.edit_action_item(
          hasAccessOnActionItem,
          actionItemId,
          false,
          QUALITY_MODULE_CAPA,
          null,
          this
        );
    });

    $("input[name=issueNumber]").on("keyup", function (event) {
      var charCode = event.which ? event.which : event.keyCode;
      return !(charCode > 31 && (charCode < 48 || charCode > 57));
    });

    $(document).on("click", ".action-item-view", function () {
      var actionItemId = $(this).attr("data-actionId");
      if ($(this).attr("data-ownerType") === "PVC")
        actionItem.actionItemModal.view_action_item(
          actionItemId,
          "PV_CENTRAL_CAPA",
          this
        );
      else
        actionItem.actionItemModal.view_action_item(
          actionItemId,
          "QUALITY_MODULE",
          this
        );
    });
  };

  var init_table = function (table) {
    $(table).DataTable({
      "layout": {
        topStart: null,
        topEnd: null,
        bottomStart: null,
        bottomEnd: null,
      },
    });
  };

  init();

  $("#mainContent").on("click", "#attachment-capa", function (e) {
    var container = $(this).closest(".attachment-container");
    var jForm = new FormData();
    if (!validateAttachmentFileSize(container.find("#attach_file_input"))) {
      return false;
    }
    if (!validateAttachmentFileNameSize(container.find(".filename_attach"))) {
      return false;
    }
    var rows = document.getElementById("rxTableAttachment").rows;
    if (filename.length == 0) {
      for (var i = 1; i < rows.length; i++) {
        var rowText = rows[i].innerText;
        filename.push(attachmentCount);
        filename.push(rowText.substring(1, rowText.indexOf(".")));
        attachmentCount++;
        attachmentIdx++;
        attachmentList++;
      }
    }
    var attach = container.find(".filename_attach").val()
      ? container.find(".filename_attach").val()
      : container.find("#attach_file_input").get(0).files[0].name.split(".")[0];
    for (var i = 0; i < filename.length; i++) {
      if (attach == filename[i]) {
        $(".attachSizeExceed").show();
        $("#message").html($.i18n._("issue.file.exists"));
        $(window).scrollTop(0);
        return false;
      }
    }
    filename.push(attachmentCount);
    filename.push(attach);

    jForm.append("file", container.find("#attach_file_input").get(0).files[0]);
    jForm.append("filename_attach", container.find(".filename_attach").val());
    jForm.append("ownerType", $(this).attr("data-ownerType"));
    jForm.append("counter", ++attachmentCount);
    $.ajax({
      url: attachmentParametersUrl,
      async: false,
      type: "POST",
      data: jForm,
      mimeType: "multipart/form-data",
      contentType: false,
      cache: false,
      processData: false,
      dataType: "json",
    })
      .done(function (data) {
        CapaAttachId.push(data);
        var attachmentListHtml =
          '<tr class="actionTableRow"> ' +
          '<td style="vertical-align: middle;text-align: left; min-width: 30px"> ' +
          "<div> " +
          '<input type="checkbox" class="selectCheckbox" name="selected" disabled/> ' +
          "</div> " +
          "</td> " +
          "<td>" +
          data.filename +
          "</td>" +
          "<td>" +
          data.createdby +
          "</td>" +
          "<td>" +
          moment
            .utc(data.datecreated)
            .tz(userTimeZone)
            .format(DEFAULT_DATE_DISPLAY_FORMAT) +
          "</td> " +
          "<td>" +
          '<a href="javascript:void(0)" id="remove-attachment" value=' +
          attachmentIdx +
          ">" +
          '<i class="md-lg md-close theme-color v-a-initial p-l-5"/>' +
          "</a>" +
          "</td> " +
          "</tr>";
        $("#attachmentListcapa").append(attachmentListHtml);
        attachmentIdx++;
        e.preventDefault();
      })
      .fail(function (data) {
        alert($.i18n._("Error"));
      });
    $(".attachment-button").attr("disabled", true);
    $(".filename_attach").val(null);
    $("#attach_file_name").val(null);
    $("#attach_file_input").val("");
  });

  $("#mainContent").on("click", "#remove-attachment", function (e) {
    var value = $(this).attr("value");
    var attach = $(this).parent().parent().find("td")[1].innerText;
    for (var i = 0; i < filename.length; i++) {
      if (attach.substring(0, attach.indexOf(".")) == filename[i]) {
        filename.splice(i - 1, 2);
      }
    }
    $(this).parent().parent().remove();
    $.ajax({
      url: deleteTempFiles,
      async: false,
      type: "POST",
      data: {
        filename: filename[parseInt(value) * 2 + 1],
        counter: filename[parseInt(value) * 2 + 2],
      },
      dataType: "json",
    }).done(function (data) {
      attachmentCount--;
      attachmentIdx--;
      filename.splice(parseInt(value) * 2, 2);
      delete CapaAttachId[value - attachmentList];
    });
  });

  $("#mainContent").on("click", "#saveButton", function (e) {
    var jsonValue=CapaAttachId.map(obj => JSON.stringify(obj)).join(',');
    $("#AttachJson").val(jsonValue);
  });

  function validateAttachmentFileSize(AttachmentFileContainer) {
    if (
      isLargeFile ||
      AttachmentFileContainer.get(0).files[0].size > AttachmentSizeLimit
    ) {
      $(".alert-danger").hide();
      $(".attachSizeExceed").show();
      $("#message").html($.i18n._("issue.Attachment.data.maxSize"));
      $(window).scrollTop(0);
      return false;
    }
    $(".attachSizeExceed").hide();
    return true;
  }

  function validateAttachmentFileNameSize(AttachmentFileContainer) {
    if (AttachmentFileContainer.val().length > 255) {
      $(".alert-danger").hide();
      $(".attachSizeExceed").show();
      $("#message").html($.i18n._("issue.Attachment.file.name.maxSize"));
      $(window).scrollTop(0);
      return false;
    }
    $(".attachSizeExceed").hide();
    return true;
  }

  $("#attachSizeExceed").on("click", function () {
    $(".attachSizeExceed").hide();
  });

    function removeAttachments(ids) {
        var issueNumber = $('#issueNumber').val().trim();
        var deleteJustification = $("#deletejustification").val();
        $.ajax({
          type: "POST",
          async: false,
          url: removeIssueAttachmentsUrl + "?selectedIds=" + ids + "&selectAll=" + selectAll + "&deleteJustification="
              + deleteJustification + "&issueNumber=" + issueNumber + (capaInsId ? "&capaId=" + capaInsId : ""),
        }).done(function () {
            $("#deleteModal").modal("hide");
            reloadCapaAttchmentList($.i18n._('capa.success'));
            $(".selectAllCheckbox").prop("checked", false);
            selectAll = false;
            $("#successMessage").html($.i18n._('quality.attachment.removed'));
            $(".actionItemSuccess").show();
            setTimeout(function () {
                $(".actionItemSuccess").hide();
            }, 3000);
        });
    }

    function reloadCapaAttchmentList(txt) {
        var issueNumber = $("#issueNumber").val();
        $.ajax({
          method: 'get',
          url: fetchDataIssueUrl,
          data: {
            "issueNumber": issueNumber
          },
          async: false,
        }).done(function (data) {
            var attachmentList = data.capaData.attachments;
            $("#attachmentListcapa").html('');
            for (var i = 0; i < attachmentList.length; i++) {
                if(attachmentList[i].isDeleted == false) {
                    $("#attachmentListcapa").append('<tr class="actionTableRow"> ' +
                        '<td style="vertical-align: middle;text-align: left; min-width: 30px"> ' +
                        '<div> ' +
                        '<input type="checkbox" _id=' + attachmentList[i].id + ' class="selectCheckbox"  name="selected" /> ' +
                        '</div> ' +
                        '</td> ' +
                        '<td>' + attachmentList[i].filename + '</td>' +
                        '<td>' + attachmentList[i].createdBy + '</td>' +
                        '<td>' + moment.utc(attachmentList[i].dateCreated).tz(userTimeZone).format(DEFAULT_DATE_DISPLAY_FORMAT) + '</td> ' +
                        '<td>' +
                        '<a href="' + downloadAttachmentUrl + '?id=' + attachmentList[i].id + '">' +
                        '<i id="saveButton" class="glyphicon glyphicon-download-alt theme-color v-a-initial" title=' + $.i18n._('app.download.label') + '></i>' +
                        '</a>' +
                        '<a href="javascript:void(0);" class="remove" id='+attachmentList[i].id+' capaId='+capaInsId+'>' +
                            '<i data-toggle="popover" data-trigger="hover" data-container="body" class="md-lg md-close theme-color v-a-initial" data-content=' + $.i18n._('issue.delete.attachment') + '></i>' +
                        '</a> '+
                        '</td> '+
                        '</tr>');
                }
            }
            $(".attachment-button").attr("disabled", true);
        }).fail(function (errorData) {
            console.log("error");
            alert(errorData);
        })
    }
});

function reloadCapaActionList(txt) {
  //$(".actionTableRow").remove();
  //$(".dataTables_empty").parent().remove();
  $("#corrective-actions").DataTable().clear();
  $("#preventive-actions").DataTable().clear();
  $.ajax({
    method: "get",
    url: actionItemUrl + "?id=" + capaId,
    async: false,
    dataType: "json",
  })
    .done(function (data) {
      var preventiveTable = $("#preventive-actions").DataTable();
      var correctiveTable = $("#corrective-actions").DataTable();
      for (var i = 0; i < data.preventive.length; i++) {
        preventiveTable.row.add(createActionRow(data.preventive[i]));
      }
      for (var i = 0; i < data.corrective.length; i++) {
        correctiveTable.row.add(createActionRow(data.corrective[i]));
      }

      correctiveTable.draw();
      preventiveTable.draw();

      $("#successMessage").html(txt ? txt : "Success");
      $(".actionItemSuccess").show();
      setTimeout(function () {
        $(".actionItemSuccess").hide();
      }, 2000);
    })
    .fail(function (errorData) {
      console.log("error");
      alert(errorData);
    });
}

function createActionRow(item) {
  return [
    '<a href="javascript:void(0)" data-actionId="' +
      item.actionItemId +
      '" class="action-item-edit">' +
      item.description +
      "</a>",
    item.dueDate,
    item.priority,
    item.status,
    '<a href="javascript:void(0)" data-actionId="' +
      item.actionItemId +
      '" class="btn btn-success btn-xs action-item-remove">Remove</a>\n',
  ];
}
