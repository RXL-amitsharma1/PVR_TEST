$(function () {
    var fullReportDestinationFilter = '';
    var $title = $("#criteriaDiv").closest(".rxmain-container").find(".rxmain-container-header-label:first");
    $title.html($title.html() + '<span class="fa fa-info-circle toggleCriteria" style="font-size:20px;margin-left: 10px !important;cursor: pointer"><\/span>');
    $(document).on("click", ".toggleCriteria", function () {
        $("#criteriaDiv").toggle()
    });
    $(".queryTemplateUserGroupSelect").select2();
    $(".userSelectSelect").select2();
    $(".destinationSelect").select2({
        multiple: true, tags: destinationList,
        separator: ";"
    });

    $(".fullReportDestinations").select2({
        multiple: true, tags: destinationList,
        separator: ";"
    });

    $(".destinationFilter").on("click", function () {
        if ($(this).hasClass("md-filter-remove")) {
            $(this).removeClass("md-filter-remove").addClass("md-filter");
            fullReportDestinationFilter = '';
        } else {
            $(this).removeClass("md-filter").addClass("md-filter-remove");
            fullReportDestinationFilter = $("#publishModal").find("[name=fullReportDestinations]").val();
        }
        fullGenerationTable.ajax.reload();
    })

    if (selectedTab) {
        $("#" + selectedTab).trigger('click');
    } else {
        if (sessionStorage.getItem("publisherSelectedTab_" + reportId))
            $("#" + sessionStorage.getItem("publisherSelectedTab_" + reportId)).trigger('click');
        else
            $("#overviewTabLink").trigger('click');
    }

    $(document).on("click", ".tab-ref", function () {
        sessionStorage.setItem("publisherSelectedTab_" + reportId, $(this).attr("id"));
    });


    function initInlineSelect(cell, label, editDiv, select, url, left) {

        $(document).on("click", cell, function (e) {
            var $this = $(this);
            content = $this.parent().find(".content").text();
            $label = $this.parent().find(label);
            $sectionId = $this.parent().find(".sectionId").val();
            $publisherId = $this.parent().find(".publisherReportId").val();
            var $textEditDiv = $(editDiv);
            var enterField = $textEditDiv.find(select);
            if (content && content != "") {
                enterField.val(content)
            }
            var position = $this.offset();
            $textEditDiv.css("left", position.left - (left ? 200 : 0));
            $textEditDiv.css("top", position.top);
            $textEditDiv.show();
            if (enterField) {
                enterField.on("keydown", function (evt) {
                    evt = evt || window.event;
                    if (evt.keyCode == 13) {//27 is the code for Enter
                        $textEditDiv.find(".saveButton").trigger('click');
                    }
                });
            }


            enterField.focus();
            $textEditDiv.find(".saveButton").one('click', function (e) {
                var newVal, name;
                if (select.toLowerCase().indexOf("select") > -1) {
                    var d = enterField.select2("data");
                    newVal = typeof d.id === "undefined" ? _.map(d, function (e) {
                        return e.id
                    }).join(";") : d.id;
                    name = typeof d.text === "undefined" ? _.map(d, function (e) {
                        return e.text
                    }).join(";") : d.text;
                } else {
                    newVal = name = $(select).val()
                }
                showLoader();

                $.ajax({
                    url: url,
                    data: {sectionId: $sectionId, publisherId: $publisherId, value: name, id: newVal},
                    dataType: 'html'
                })
                    .done(function (result) {
                        if (newVal && newVal != 0) {
                            // $input.val(newVal);
                            $label.html(name)
                        } else {
                            // $input.val("");
                            $label.html(" no ")
                        }
                        hideLoader();
                    });

                $(".popupBox").hide();

            });
            $textEditDiv.find(".cancelButton").one('click', function (e) {
                $(".popupBox").hide();
            });
        });
    }

    initInlineSelect(".updateQueryTemplateUserGroup", ".queryTemplateUserGroupLabel", "#userGroupEditDiv", '.queryTemplateUserGroupSelect', updateAssignedToUrl);
    initInlineSelect(".author", ".authorLabel", "#userEditDiv", '.userSelectSelect', updateAuthorUrl);
    initInlineSelect(".reviewer", ".reviewerLabel", "#userEditDiv", '.userSelectSelect', updateReviewerUrl);
    initInlineSelect(".approver", ".approverLabel", "#userEditDiv", '.userSelectSelect', updateApproverUrl);
    initInlineSelect(".destinationUpdate", ".destinationLabel", "#destinationEditDiv", ".destinationSelect", updateDestinationUrl);
    initInlineSelect(".nameUpdate", ".nameLabel", "#nameEditDiv", ".nameInput", updateNameUrl);
    initInlineSelect(".dueUpdate", ".dueLabel", "#dueEditDiv", ".dueInput", updateDueUrl);
    initInlineSelect(".pcommentUpdate", ".pcommentLabel", "#pcommentEditDiv", ".pcommentInput", updateCommentUrl, true);
    periodicReport.periodicReportList.initSpecialActions($(".sectionsActions"));

    function initActionItemList(tableSelector, hasAccessOnActionItem) {
        var dataTable = $(tableSelector).DataTable({
            "language": {
                "url": "../assets/i18n/dataTables_" + userLocale + ".json"
            },
            "processing": true,
            "serverSide": true,
            "searching": false,
            "paging": false,
            "info": true,
            "ajax": {
                "url": listPublisherActionItemUrl + "&user=" + hasAccessOnActionItem
            },
            "order": [[1, "asc"], [2, "desc"]],
            "columns": [
                {
                    "mData": "assignedTo",
                    "bSortable": false
                }, {
                    "mData": "relatedFor",
                    "bSortable": false
                },
                {
                    "data": "description",
                    "render": function (data, type, row) {
                        var truncatedDes = "";
                        if (row.description.length > 30) {
                            truncatedDes = row.description.substring(0, 29) + "...";
                        } else {
                            truncatedDes = row.description
                        }

                        return '<a href="javascript:actionItem.actionItemModal.edit_action_item(true,' + row.actionItemId + ', false, \'PERIODIC_REPORT\', null);">'
                            + encodeToHTML(truncatedDes) + '</a>';
                    }
                },
                {
                    "data": "dueDate",
                    "className": "dataTableColumnCenter",
                    "render": function (data, type, full) {
                        return data ? moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) : "";
                    }
                },
                {
                    "data": "priority",
                    "render": function (data, type, full) {
                        var prioritySpan = document.createElement("span");
                        prioritySpan.classList.add("label");
                        switch (data) {
                            case "HIGH":
                                prioritySpan.classList.add("label-danger");
                                break;
                            case "MEDIUM":
                                prioritySpan.classList.add("label-warning");
                                break;
                            default:
                                prioritySpan.classList.add("label-default");
                                break;

                        }
                        prioritySpan.appendChild(document.createTextNode(data));
                        return prioritySpan.outerHTML;
                    }
                },
                {
                    "mData": "status",
                    "aTargets": ["status"],
                    "mRender": function (data, type, full) {
                        return data ? $.i18n._('status_enum.' + data) : "";
                    }
                }
            ]
        });
    }

    initActionItemList('#actionItemListPublisher', true);
    initActionItemList('#allActionItemListPublisher', false);

    $('#file_input, #file_input2, #file_input3').on('change', function (evt, numFiles, label) {
        var input = $(this).closest(".input-group").find("input[type=text]");
        input.val($.map($(this)[0].files, function (val) {
            return val.name;
        }).join(";"));
    });

    $('#uploadFileName_input').on('change', function (evt, numFiles, label) {
        $("#uploadFileName").val($.map($('#uploadFileName_input')[0].files, function (val) {
            return val.name;
        }).join(";"));
    });

    $(document).on("click", ".deleteAttachment", function () {
        $("#attachmentsToDelete").val($("#attachmentsToDelete").val() + "," + $(this).attr("data-id"));
        $(this).parent().css("text-decoration", "line-through");
        $(this).find(".btn").prop('disabled', true);
    });

    $(document).on("click", ".uploadDocument", function (e) {
        $("#uploadSectionType").val($(this).attr("data-type"));
        $("#uploadSectionId").val($(this).attr("data-id"));
        $("#uploadModal").modal("show");
    });


    $(document).on("click", ".sectionUp,.sectionDown", function () {
        var row = $(this).parents("tr:first");
        if ($(this).is(".sectionUp")) {
            row.insertBefore(row.prev());
        } else {
            row.insertAfter(row.next());
        }
    });

    $(document).on("click", ".submitPublish", function (e) {
        var sectionIds = "", $publishModal = $("#publishModal");
        var comment = $publishModal.find("[name=comment]").val();
        var name = $publishModal.find("[name=name]").val();
        var destinations = $publishModal.find("[name=fullReportDestinations]").val();
        var nullValues = [];
        if (!name) nullValues.push($.i18n._("app.advancedFilter.name"));
        if (!comment) nullValues.push($.i18n._("app.caseList.comment"));
        if (!destinations) nullValues.push($.i18n._("app.advancedFilter.destination"));
        if (nullValues.length > 0) {
            $(".fullGenerationError p").text(nullValues.join(", ") + " " + $.i18n._('publisher.fields.cannot.empty'));
            $(".fullGenerationError").show();
        } else {
            $("#publishModal").modal("hide");
            showLoader();
            $publishModal.find(".sectionsToPublish").each(function (i) {
                if ($(this).find("[name=skip]").val() == "false") {
                    sectionIds += $(this).find("[name=sectionId]").val() + ","
                }
            });

            var data = {id: reportId, comment: comment, name: name, destinations: destinations}
            $.ajax({
                type: "POST",
                url: publishURL + "?ids=" + sectionIds,
                data: data,
                dataType: 'json'
            })
                .fail(function (err) {
                    console.error(err);
                    alert("Unexpected Error!");
                })
                .done(function (response) {
                    location.reload(true);
                    $("#publisherTabLink").trigger('click');
                });
        }
    });

    $(document).on("click", '[name="fullGenErrorClose"]', function () {
        $(".fullGenerationError p").text('');
        $(".fullGenerationError").hide();
    });

    $(document).on("click", '.publishReportSubmit', function () {
        if ($("#file_input2").val() && $('#publishReportComment').val() && $('#publishReportName').val()) {
            $("#publishReportForm").trigger('submit');
        } else {
            $(".publishReportError p").text($.i18n._('app.actionItem.fill.all.fields'));
            $(".publishReportError").show();
        }
    });

    $(document).on("click", '.addPublisherReportDocument', function () {
        $(".publishReportError").hide();
    });

    $(document).on("click", '[name="publishReportErrorClose"]', function () {
        $(".publishReportError p").text('');
        $(".publishReportError").hide();
    });

    $(document).on("click", '.publishReportUpdateSubmit', function () {
        if ($("#file_input3").val()) {
            $("#publishReportUpdateForm").trigger('submit');
        } else {
            $(".publishReportUpdateError p").text($.i18n._('publisher.report.document.empty'));
            $(".publishReportUpdateError").show();
        }
    });

    $(document).on("click", '.updatePublisherReportDocument', function () {
        $(".publishReportUpdateError").hide();
    });

    $(document).on("click", '[name="publishReportUpdateErrorClose"]', function () {
        $(".publishReportUpdateError p").text('');
        $(".publishReportUpdateError").hide();
    });

    if ($(".generatePublisherTemplate").length == 0) {
        $(".generateAll").attr("disabled", true)
    }

    $(document).on("click", ".downloadUrl", function (e) {
        $("#historyModal").modal("hide");
    });

    $(document).on("click", ".publish", function (e) {
        var $modal = $("#publishModal");
        fullReportDestinationFilter = '';
        $modal.find("[name=fullReportDestinations]").val([]).trigger('change');
        $modal.find("[name=comment]").val("");
        $modal.find(".destinationFilter").removeClass("md-filter-remove").addClass("md-filter");
        $(".fullGenerationError").hide();
        fullGenerationTable.ajax.reload();
        $modal.find("[name=skip]").val("false");
        $modal.find(".sectionsToPublish").show();
        $modal.modal("show");
    });

    var fullGenerationTable = $("#fullGenerationTable").DataTable({
        "language": {
            "url": "../assets/i18n/dataTables_" + userLocale + ".json"
        },
        "deferLoading": 0,
        "serverSide": true,
        "searching": false,
        "bPaginate": false,
        "paging": false,
        "ordering": false,
        "info": false,
        "ajax": {
            "url": getAllowedSectionsUrl,
            "dataSrc": "data",
            "data": function (d) {
                d.destinationFilter = fullReportDestinationFilter;
            }
        },
        "columns": [
            {
                mRender: function (data, type, row) {
                    return '<input type="hidden" name="skip" value="false">' +
                        '<input type="hidden" name="sectionId" value="' + row.id + '"> ' +
                        row.name;
                }
            }, {
                mRender: function (data, type, row) {
                    return row.destination ? row.destination : "";
                }
            }, {
                mRender: function (data, type, row) {
                    return row.workflowSate;
                }
            }, {
                mRender: function (data, type, row) {
                    return '<span class="md md-close pv-cross sectionHide"></span>\n' +
                        '  <span class="md md-arrow-up pv-cross sectionUp"></span>\n' +
                        '  <span class="md md-arrow-down pv-cross sectionDown"></span>'
                }
            }
        ]
    }).on('draw.dt', function () {
        setTimeout(function () {
            $('#fullGenerationTable tbody tr').each(function () {
                $(this).addClass("sectionsToPublish");
            });
        }, 100);
    }).on('xhr.dt', function (e, settings, json, xhr) {
        checkIfSessionTimeOutThenReload(e, json)
    });

    $(document).on("click", ".sectionHide", function (e) {
        var tr = $(this).closest("tr");
        tr.hide()
        tr.find("[name=skip]").val("true");
    });

    $(document).on("click", ".generateAll", function (e) {
        var ids = []
        $(".generatePublisherTemplate").each(function () {
            ids.push($(this).attr("data-id"));
        });
        showLoader();
        location.href = generateAllDraftURL + "?sectionsId=" + ids.join(",") + "&reportId=" + reportId;
    });

    $(document).on("click", ".showPublisherLog", function (e) {
        var id = $(this).attr("data-id");
        $.ajax({
            type: "POST",
            url: listPublisherExecutedLogUrl + "?id=" + id,
            dataType: 'json'
        })
            .done(function (result) {
                $(".publisherLogModalBody").val(result.log);
                $("#publisherLogModal").modal("show");
            });
    })

    $(document).on("click", ".saveParamsAndGenerate", function (e) {
        var id = $(this).attr("data-id");
        var data = {id: id};
        $(".pendingInput").each(function () {
            data["data_" + $(this).attr("name")] = $(this).val();
        });
        showLoader();
        $.ajax({
            type: "POST",
            url: saveParamsAndGenerateURL,
            data: data,
            dataType: 'json'
        })
            .fail(function (err) {
                console.error(err);
            })
            .done(function (response) {
                location.reload(true);
            });
    });

    $(document).on("click", ".setAsFinal", function (e) {
        var id = $(this).attr("data-id");
        $(".saveParamsAndGenerate").attr("data-id", id);
        var href = $(this).attr("data-href");
        var actionItem = $(this).data('actionitems')
        showLoader();
        $.ajax({
            type: "GET",
            url: fetchPendingParametersURL + "?id=" + id,
            dataType: 'json'
        })
            .done(function (result) {
                hideLoader();
                if (actionItem == "Waiting") {
                    $('#actionCompletionModal #warningType').text($.i18n._('publisher.section.action.complete.warning'));
                    $('#actionCompletionModal').modal('show');
                    $('#actionCompletionWarningButton').off().on('click', function (e) {
                        showCommentModal(href, result.comment.length)
                        $('#actionCompletionModal').modal('hide');

                    });
                } else {
                    showCommentModal(href, result.comment.length)
                }
            });
    });

    function showCommentModal(href, length) {
        if (length > 0) {
            $('#warningModal #warningType').text($.i18n._('publisher.commentConfirm'));
            $('#warningModal').modal('show');
            $('#warningButton').off('click').on('click', function () {
                location.href = href;
            });
        } else {
            location.href = href;
        }
    }

    $(document).on("click", ".pendingBtn", function (e) {
        var id = $(this).attr("data-id");
        $(".saveParamsAndGenerate").attr("data-id", id);
        showLoader();
        $.ajax({
            type: "GET",
            url: fetchPendingParametersURL + "?id=" + id,
            dataType: 'json'
        })
            .done(function (result) {
                hideLoader();
                $("#pendingModal").modal("show");
                $(".pendingTotalParameters").html(" (" + result.variable.length + ")");
                $(".pendingTotalManual").html(" (" + result.manual.length + ")");
                $(".pendingTotalComments").html(" (" + result.comment.length + ")");
                var content = "";
                for (var i = 0; i < result.variable.length; i++) {
                    var name = result.variable[i].split("::")[0];
                    content += "<tr><td>" + name + "</td><td class='composerButtonContainer'><textarea style='height: 35px;'  name='" + name + "' class='form-control pendingInput'/>" +
                        "<span class=\"composerButton\" >" + $.i18n._('compose.parameter') + "</span></td></tr>";
                }
                $("#pendingParameterTable").html(content);
                content = "";
                for (var i = 0; i < result.manual.length; i++) {
                    content += result.manual[i] + "<br><br>";
                }
                $("#manualTab").html(content);
                content = "";
                for (var i = 0; i < result.comment.length; i++) {
                    content += result.comment[i] + "<br><br>";
                }
                $("#commentTab").html(content);
                updateParameterValueAutoComplete()
            });
    });
    $(document).on("click", ".restoreDraft", function (e) {
        var id = $(this).attr("data-id");
        var sectionId = $(this).attr("data-sectionId");
        $('#warningModal #warningType').text($.i18n._('publisher.restoreConfirm'));
        //  $('#warningModal .description').text(errorMessage);
        $('#warningModal').modal('show');
        $('#warningButton').off('click').on('click', function () {
            $.ajax({
                type: "GET",
                url: restoreDraftURL + "?id=" + id,
                dataType: 'json'
            })
                .done(function (result) {
                    $('#warningModal').modal('hide');
                    location.reload(true);
                });
        });
    })
    $(document).on("click", ".graftPublisherTemplate", function (e) {
        var id = $(this).attr("data-id");
        showHistoryModal(id);
    });

    function showHistoryModal(id) {
        $.ajax({
            type: "POST",
            url: listPublisherExecutedTemplatesUrl + "?id=" + id + "&reportId=" + reportId,
            dataType: 'html'
        })
            .done(function (result) {
                var restore = (result[result.length - 1].status != "FINAL")
                var content = '<table class=\'table\' style=\'text-align: center\'><thead style=\'font-weight: bold\'><td>' + $.i18n._('version') + '</td>' +
                    '<td>' + $.i18n._('output') + '</td>' +
                    '<td>' + $.i18n._('execution') + '</td>' +
                    '<td>' + $.i18n._('last.updated') + '</td>' +
                    '<td>' + $.i18n._('name') + '</td>' +
                    '<td>' + $.i18n._('modified.by') + '</td></thead>';
                for (var i = 0; i < result.length; i++) {
                    content += "<tr><td>" + (i + 1)
                    content += "</td><td>"
                    if (result[i].status != "EMPTY")
                        content += '<a class=\'btn  btn-xs btn-success downloadUrl\' title=\'Download\'' +
                            'data-name="' + result[i].name + '" ' +
                            'data-url="' + downloadURL + '?id=' + result[i].id + '"' + 'style="margin:4px;"' +
                            'href="javascript:void(0)"> <span class="fa fa-download" style="cursor: pointer"></span> </a>' +
                            (officeOnlineEnabled ? (
                                '<a target="_blank" class=\'btn  btn-xs btn-success wopiLink\' title="PDF" href="' + viewOfficeURL + '?type=publisherExecutedTemplate&id=' + result[i].id + '"' + 'style="margin:5px;"' +
                                '"> <span class="fa fa-file-pdf-o" style="cursor: pointer"></span> </a>'

                            ) : '') +

                            (restore ? (
                                '<a class=\'btn  btn-xs btn-success restoreDraft \' title="Restore" href=\'javascript:void(0)\' data-sectionId="' + id + '"  data-id="' + result[i].id + '" style="margin:5px;"' + '"> <span class="fa fa-undo" style="cursor: pointer"></span> </a>'
                            ) : '')

                    content += "</td><td align='center' style='padding-top: 11px;'>" +
                        "<a href='javascript:void(0)'  data-id='" + result[i].id + "' class='showPublisherLog '><span style='padding-left: 3px;padding-right: 3px;' class='" + result[i].executionStatusCss + "'>" + result[i].executionStatus + "</span></a>";
                    content += "</td><td>" +
                        result[i].lastUpdated + "</td><td> " + result[i].name + "</td><td>" + result[i].modifiedBy
                    "</td></tr>";
                }

                $(".historyModalBody").html(content + "</table>");
                $("#historyModal").modal("show");
            })
            .fail(function (error) {
                alert("Something went wrong!")
            });
    }

    $(".datepicker").datepicker({
        momentConfig: {
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    });
    $(".statusSelect").select2();
    $(document).on("click", ".updateStatus", function (e) {
        var $this = $(this);
        $label = $this.parent().find(".statusLabel");
        $queryid = $this.parent().find(".queryTemplateId").val();
        $attachid = $this.parent().find(".attachmentId").val();
        $reportid = reportId;
        var $textEditDiv = $("#statusEditDiv");
        var enterField = $textEditDiv.find('.statusSelect');
        $textEditDiv.modal("show");
        if (enterField) {
            enterField.on("keydown", function (evt) {
                evt = evt || window.event;
                if (evt.keyCode == 13) {//27 is the code for Enter
                    $textEditDiv.find(".saveButton").trigger('click');
                }
            });
        }
        var $input = $this.parent().find("input");
        var oldVal = $input.val();

        enterField.focus();
        $textEditDiv.find(".saveButton").one('click', function (e) {
            var newVal = enterField.select2("data").id;
            var name = enterField.select2("data").text;
            if (newVal !== oldVal)
                $.ajax({
                    url: updateStatusUrl,
                    data: {queryid: $queryid, attachid: $attachid, reportid: $reportid, status: name},
                    dataType: 'html'
                })
                    .done(function (result) {
                        $label.html(name)
                    })
            $textEditDiv.modal("hide");

        });
    });

    $(".removeSectionBtn").off().on('click', function () {
        var sectionId = $(this).data('id');
        var sectiontitle = $(this).data('instancename');
        var confirmationModal = $("#confirmationModal");
        confirmationModal.modal("show");
        confirmationModal.find('.modalHeader').html($.i18n._('delete.confirm'));
        confirmationModal.find('.confirmationMessage').html($.i18n._('delete.section.confirm'));
        confirmationModal.find('.description').empty();
        confirmationModal.find('.description').text(sectiontitle);
        confirmationModal.find('.okButton').off().on('click', function () {
            $.ajax({
                type: "POST",
                url: removeSectiontUrl + "?id=" + sectionId,
                dataType: 'html'
            })
                .done(function (result) {
                    confirmationModal.modal('hide').data('bs.modal', null);
                    location.reload(true);
                })
                .fail(function (error) {
                    var responseText = error.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    if (responseTextObj.errors != undefined) {
                        alert("Sorry! Validation error");
                    } else {
                        alert(responseTextObj.message);
                    }
                })
        });
    });
    $(".removePublisherReportBtn").off().on('click', function () {
        var attachmentId = $(this).data('id');
        var sectiontitle = $(this).data('instancename');
        var confirmationModal = $("#confirmationModal");
        confirmationModal.modal("show");
        confirmationModal.find('.modalHeader').html($.i18n._('delete.confirm'));
        confirmationModal.find('.confirmationMessage').html($.i18n._('deleteThis', $.i18n._('periodicReportConfiguration')));
        confirmationModal.find('.description').empty();
        confirmationModal.find('.description').text(sectiontitle);
        confirmationModal.find('.okButton').off().on('click', function () {
            $.ajax({
                type: "POST",
                url: removePublisherReportUrl + "?id=" + attachmentId,
                dataType: 'html'
            })
                .done(function (result) {
                    confirmationModal.modal('hide').data('bs.modal', null);
                    location.reload(true);
                })
                .fail(function (error) {
                    var responseText = error.responseText;
                    var responseTextObj = JSON.parse(responseText);
                    if (responseTextObj.errors != undefined) {
                        alert("Sorry! Validation error");
                    } else {
                        alert(responseTextObj.message);
                    }
                })
        });
    });

    $(document).on("click", ".createPublisherSectionActionItem", function () {
        actionItem.actionItemModal.set_section_id($(this).data('section-id'));
        actionItem.actionItemModal.init_action_item_modal(false, "PERIODIC_REPORT");
    });

    $(document).on("click ", ".actionItemModalIcon", function () {
        actionItem.actionItemModal.set_section_id($(this).data('section-id'));
        actionItem.actionItemModal.view_action_item_list(true, false, "PERIODIC_REPORT");
    });

    $(document).on("click", ".createPublisherDocumentActionItem", function () {
        actionItem.actionItemModal.set_publisher_id($(this).data('publisher-id'));
        actionItem.actionItemModal.init_action_item_modal(false, "PERIODIC_REPORT");
    });

    $(document).on("click ", ".actionItemPublisherModalIcon", function () {
        actionItem.actionItemModal.set_publisher_id($(this).data('publisher-id'));
        actionItem.actionItemModal.view_action_item_list(true, false, "PERIODIC_REPORT");
    });

    $(document).on("click ", ".updatePublisherReportDocument", function () {
        var id = $(this).attr("data-id");
        var modal = $("#publishReportUpdateModal")
        modal.find("[name=id]").val(id);
        modal.modal("show");
    });

    $(document).on("change", "#processAsTemplate", function () {
        if ($(this).is(":checked")) {
            $(".parameterTable").show();
        } else {
            $(".parameterTable").hide();
        }
    });

    $(document).on("click", ".removeParameter", function () {
        $(this).closest("tr").remove()
    });

    var editingRow;

    $(document).on("click", ".generateBtn", function () {

        var clone;
        var $fillParametersModal = $("#fillParametersModal");
        var td = $(this).closest("td");
        if (td.find(".publisherFileParameterValueDiv").find(".fileName").val() == "") {
            clone = td.find(".publisherParameterValueDiv").clone();
            clone.find(".col-xs-2").remove();
            clone.find(".col-xs-10").removeClass("col-xs-10").addClass("col-xs-12");
            clone.find(".publisherTemplateSelect").hide();
            var showModel = false;
            clone.find('[name=parameterValue]').each(function () {
                if ($(this).val() == "")
                    showModel = true;
                else
                    $(this).closest("tr").hide();
            });
            if (!showModel) {
                showLoader();
                location.href = $(this).attr("data-href");
                return;
            }

            $("#fillParametersModalContent").empty().append(clone);
            clone.show();

            $fillParametersModal.modal("show");
            updateParameterValueAutoComplete();
        } else {
            showLoader();
            location.href = $(this).attr("data-href");
            return;
        }

    });

    $(document).on("click", ".editTemplateBtn", function () {
        var clone = $(this).closest("td").find(".publisherParameterValueDiv").clone();
        bindSelect2WithUrl(clone.find(".publisherTemplateSelect"), PVPTemplateSearchUrl, PVPTemplateNameUrl, false);
        bindSelect2WithUrl(clone.find("[name=publisherSectionTaskTemplate]"), PVPTaskTemplateSearchUrl, PVPTaskTemplateNameUrl, true);
        $("#publisherTemplateModalContent").empty().append(clone);
        clone.show();
        clone.find(".col-xs-2").remove();
        clone.find(".col-xs-10").removeClass("col-xs-10").addClass("col-xs-12");

        clone = $(this).closest("td").find(".publisherFileParameterValueDiv").clone();
        $("#publisherFileModalContent").empty().append(clone);
        clone.show();

        $("#publisherTemplateModal").modal("show");
        if ($("#publisherTemplateModal").find(".fileName").val() !== "") {
            $(".addFileLink").trigger('click');
        } else {
            $(".addLibraryLink").trigger('click');
        }
        updateParameterValueAutoComplete();
    });

    $(document).on("click", ".publisherExecutedSectionUp, .publisherExecutedSectionDown", function () {
        var row = $(this).parents("tr:first");
        var changeWith;
        if ($(this).is(".publisherExecutedSectionUp")) {
            if (!row.prev().hasClass("publisherSectionRowFile")) {
                changeWith = row.prev();
                if (changeWith.length > 0)
                    changeOrder(row, changeWith, function () {
                        row.insertBefore(changeWith);
                    });
            }
        } else {
            changeWith = row.next();
            if (changeWith.length > 0)
                changeOrder(row, changeWith, function () {
                    row.insertAfter(changeWith);
                });
        }
    });

    function changeOrder(row1, row2, callback) {

        $.ajax({
            url: changeSortOrderUrl,
            data: {id1: row1.data("id"), id2: row2.data("id")},
            dataType: 'json'
        })
            .done(function (result) {
                callback();
            });
    }

    $("#showPublishedHistory").on('click', function () {
        if ($(this).is(':checked'))
            $(".publishedHistory").show();
        else
            $(".publishedHistory").hide();
    });

});