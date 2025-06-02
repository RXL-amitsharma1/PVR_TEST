var commentCounter = 0;
var taskCounter = 0;
var REPORT_REQUEST = ""
$(function() {

    $('.select2-box').select2();
    commentCounter = $("#commentSize").val();
    taskCounter = $("#actionItems").val();

    //This will make the due date, completion date and version as of as date pickers with back dates not allowed.
    initializePastDatesNotAllowedDatePicker();

    //This will make the start date and end date as date pickers with back dates enabled.
    $('#startDateDiv,#endDateDiv').datepicker({
        allowPastDates : true,
        momentConfig: {
            format: DEFAULT_DATE_DISPLAY_FORMAT
        }
    }).on('changed.fu.datepicker inputParsingFailed.fu.datepicker dateClicked.fu.datepicker', function (evt, date) {
        start = $('#startDateDiv').datepicker('getDate');
        end = $('#endDateDiv').datepicker('getDate');
        $('#startDateDiv,#endDateDiv').removeClass('has-error');

        if (start && end && start > end) {
            $(this).addClass('has-error');
        }
    });
    //Fetch the mode.
    var mode = $("#mode").val();

    //Mode is creation then we clear up the date values.
    if (mode == 'create') {
        $('.date').val('');
        if($('#dueDateHidden').val()){
            $('#dueDate').val($('#dueDateHidden').val());
        }
    } else {
        //TODO: Ugly hack need to correct it.
        $('#dueDate').val($('#dueDateHidden').val());
        $('#completionDate').val($('#completionDateHidden').val());
        $('#startDate').val($('#startDateHidden').val());
        $('#endDate').val($('#endDateHidden').val());
        $('#asOfVersionDate').val($('#asOfVersionDateHidden').val());
    }

    //Bind the click event on the add new comment icon.
    $(".add-comment").on('click', function() {
        showCommentModal(null, null, commentCounter, true);
    });

    $('#closeCommentModal').on('click', function () {
       $('#reportComment').val('');
    });
    //Opens the comment modal window.
    var showCommentModal = function(comment, commentId, commentIdx, newComment) {

        //Show the modal.
        var reportCommentModalObj = $('#reportCommentModal');
        reportCommentModalObj.modal('show');
        reportCommentModalObj.find("#index").val(commentIdx);
        reportCommentModalObj.find("#commentId").val(commentId);
        reportCommentModalObj.find("#newComment").val(newComment);

        //If comment is passed as an argument then
        //set it to the report comment field.
        if (comment) {
            comment = decodeFromHTML(comment);
            reportCommentModalObj.find("#reportComment").val(comment);
        }

        //Binding the click event on the creation button on the modal window.
        reportCommentModalObj.find(".creationButton").on('click', function() {

            var commentEntered = reportCommentModalObj.find("#reportComment").val();
            var indexForComment = reportCommentModalObj.find("#index").val();
            var commentObjId = reportCommentModalObj.find("#commentId").val();
            var newCommentObj = reportCommentModalObj.find("#newComment").val();
            commentEntered = encodeToHTML(commentEntered);

            if (commentEntered != '' && commentEntered != null) {

                if (commentObjId) {
                    document.getElementById("comments["+indexForComment+"].reportComment").value = commentEntered;
                    document.getElementById("comments["+indexForComment+"].display").innerHTML = pvr.common_util.preserve_line_break(commentEntered);
                } else {

                   //If the call is made from the new comment link then we need to populate
                   //the modal window.
                   if (JSON.parse(newCommentObj)) {
                       populateComments(commentEntered);
                       commentCounter++
                   } else {
                       //If call is made from the edit button then we need to set the values for newly entered comment.
                       document.getElementById("comments["+indexForComment+"].reportComment").value = commentEntered;
                       document.getElementById("comments["+indexForComment+"].display").innerHTML = pvr.common_util.preserve_line_break(commentEntered);
                   }

                }
            }
            reportCommentModalObj.find('#reportComment').val('');
            reportCommentModalObj.modal('hide');
            $('#reportCommentModal').data('modal', null);
            $(".noCommentSpan").hide();
        })
    }

    //The method that populate the comment divisions on the UI.
    var populateComments = function(comment) {

        if (typeof commentCounter == "undefined" || commentCounter == '') {
            commentCounter = 0;
        }

        //The panel parent div
        var reportCommentDiv = $("<div>");
        reportCommentDiv.attr("class", "panelDiv");
        reportCommentDiv.attr("data-id",commentCounter);

        //Pane div
        var panelDiv = $("<div>");
        panelDiv.attr("class", "panel panel-default");

        //Panel head
        var panelHeadDiv = $("<div>")

        //User
        var user = $("<strong>");
        user.html($("#loggedInUser").html());

        //Commented on span
        var spanElement = $("<span>");
        spanElement.attr("class", "text-muted");
        spanElement.html("  "+ $.i18n._('reportRequest.comment.date.label',moment(new Date()).tz(userTimeZone).format(DATEPICKER_FORMAT_AM_PM)));

        //Delete image div
        var deleteImageDiv = $("<div>");
        deleteImageDiv.attr("class", "deleteComment");
        deleteImageDiv.attr("style", "float: right; cursor: pointer");

        //Delete image
        var deleteImage = $("<span>");
        deleteImage.attr("class", "delete glyphicon glyphicon-trash reportRequest");
        deleteImage.attr("data-name", "COMMENT");
        deleteImageDiv.append(deleteImage);

        deleteImage.on('click', function() {
            deleteElement(deleteImage);
        });

        //Edit image div
        var editImageDiv = $("<div>");
        editImageDiv.attr("style", "float: right; cursor: pointer");

        //Edit Image
        var editImage = $("<span>");
        editImage.attr("class", "glyphicon glyphicon-edit reportRequest");
        editImageDiv.append(editImage);

        editImage.on('click', function() {
            var indexComment = $(this).closest('.panelDiv').attr("data-id");
            showCommentModal(comment, null, indexComment, false);
        });

        panelHeadDiv.append(user);
        panelHeadDiv.append(spanElement);
        panelHeadDiv.append(deleteImageDiv);
        panelHeadDiv.append(editImageDiv);

        var panelBodyDiv = $("<div>");

        var input = $("<input>");
        input.attr("class", "commentInput reportRequest");

        input.attr("type", "hidden");
        input.attr("id", "comments["+commentCounter+"].reportComment");
        input.attr("name", "comments["+commentCounter+"].reportComment");
        input.attr("value", comment);

        var spanEle = $("<span>");
        spanEle.attr("id", "comments["+commentCounter+"].display");
        spanEle.attr("style", "word-wrap: break-word");
        spanEle.html(pvr.common_util.preserve_line_break(comment));

        var deletedEle = $("<input>");
        deletedEle.attr("class", "reportRequest commentDeleted");
        deletedEle.attr("type", "hidden");
        deletedEle.attr("id", "comments[" + commentCounter + "].deleted");
        deletedEle.attr("name", "comments[" + commentCounter + "].deleted");
        deletedEle.attr("value", false);
        panelBodyDiv.append(deletedEle);

        var idEle = $("<input>");
        idEle.attr("class", "reportRequest");
        idEle.attr("type", "hidden");
        idEle.attr("id", "comments[" + commentCounter + "].id");
        idEle.attr("name", "comments[" + commentCounter + "].id");
        idEle.attr("value", null);
        panelBodyDiv.append(idEle);

        var newObjEle = $("<input>");
        newObjEle.attr("class", "reportRequest");
        newObjEle.attr("type", "hidden");
        newObjEle.attr("id", "comments[" + commentCounter + "].newObj");
        newObjEle.attr("name", "comments[" + commentCounter + "].newObj");
        newObjEle.attr("value", true);
        panelBodyDiv.append(newObjEle);

        var newObjDateCreated = $("<input>");
        newObjDateCreated.attr("class", "reportRequest commentDateCreated");
        newObjDateCreated.attr("type", "hidden");
        newObjDateCreated.attr("id", "comments[" + commentCounter + "].dateCreated");
        newObjDateCreated.attr("name", "comments[" + commentCounter + "].dateCreated");
        newObjDateCreated.attr("value", moment(new Date()).tz(userTimeZone).format(DATEPICKER_FORMAT_AM_PM));
        panelBodyDiv.append(newObjDateCreated);

        panelBodyDiv.append(input);
        panelBodyDiv.append(spanEle);

        panelDiv.append(panelHeadDiv);
        panelDiv.append(panelBodyDiv);
        reportCommentDiv.append(panelDiv);
        $(".reportCommentDiv").append(reportCommentDiv);
    };

    $(".deleteIcon").on('click', function() {
        deleteElement($(this));
    });

    //Global event linker for the edit comment.
    //This will be called from the already existing comments.
    $(".editComment").on('click', function() {
        var comment = $(this).closest(".panelDiv").find(".reportComment").val();
        var commentId = $(this).closest(".panelDiv").find(".commentId").val();
        var index = $(this).closest(".panelDiv").attr("data-id");
        showCommentModal(comment, commentId, index, false);
    });

    var deleteElement = function(element) {

        var panel = $(element).parents('.panel');
        var appType = $(element).attr("data-name");

        var deletedId;
        var deletionMessage;

        if (appType === "ACTIONITEM") {
            deletedId = ".actionItemDeleted"
            deletionMessage=$.i18n._('app.label.action.app.name');
        } else if (appType === "COMMENT") {
            deletedId = ".commentDeleted"
            deletionMessage=$.i18n._('app.caseList.comment');
        }
        deletionMessage += " " + $.i18n._('app.reportRequest.actionItem.comment.delete');

        var associationsDiv = panel.parents(".associationsDiv");
        if ( associationsDiv.children().length === 0 ) {
            $(".noAssociations").show();
        } else {
            if (associationsDiv.children(':visible').length === 0) {
                $(".noAssociations").show();
            } else {
                $(".noAssociations").hide();
            }
        }

        panel.find(deletedId).val(true);
        panel.closest('.panelDiv').hide();
        showDeleteMessage(deletionMessage);
    };
     window.showDeleteMessage=function (deletionMessage){
        var reportRequestAlert=$('.reportRequestAlert');
         var timeoutId = reportRequestAlert.data('timeoutId');
         if (timeoutId) {
             clearTimeout(timeoutId);
         }
        reportRequestAlert.find('#successMessage').html(deletionMessage);
        reportRequestAlert.show();
        window.scrollTo({ top: 0, behavior: 'smooth' });
         timeoutId = setTimeout(function () {
             reportRequestAlert.hide();
         }, 5000);
         reportRequestAlert.data('timeoutId', timeoutId);
    };

    $(".editActionItem").on("click", function() {
        var actionItemId = $(this).parent().attr("data-id");
        var index = $(this).closest(".panelDiv").attr("data-id");
        actionItem.actionItemModal.edit_action_item(hasAccessOnActionItem,actionItemId, false, REPOERT_REQUEST, index);
    });

    //Bind the click event on the create action item button.
    $("#createActionItem").on('click', function() {
        actionItem.actionItemModal.init_action_item_modal(false, REPOERT_REQUEST);
    });


    //Click event on the add from task template
    $(".addTaskFromTemplate").on('click', function() {

        var dueDate = $("#dueDate").val();
        var assignedTo = $("#assignedTo").val();

        var startDate = $("#startDate").val();
        var endDate = $("#endDate").val();
        if ($("#reportRequestType\\.id").select2().find(":selected").data("aggregate") === true) {
            startDate = $("#reportingPeriodStart").val();
            endDate = $("#reportingPeriodEnd").val();
            dueDate=$("#curPrdDueDate").val();
        }

        if (dueDate && assignedTo) {
            $("#noDueDate").removeClass("show");
            //Fetch the selected task template value

            var taskTemplateId = $(".taskTemplate").select2("data").id;

            $.ajax({
                url: findTaskUrl,
                data: "taskTemplateId=" + taskTemplateId,
                dataType: 'json'
            })
                .fail(function(err) {
                    console.log(err);
                })
                .done(function (data) {
                    for (var taskIndex = 0; taskIndex < data.length; taskIndex++) {
                        if ((data[taskIndex].baseDate == "REPORT_PERIOD_START") && (!startDate)) {
                            $("#noDueDate").html($.i18n._('startDateNotNull')).addClass("show");
                            return
                        }
                        if ((data[taskIndex].baseDate == "REPORT_PERIOD_END") && (!endDate)) {
                            $("#noDueDate").html($.i18n._('endDateNotNull')).addClass("show");
                            return
                        }
                    }
                    for (var taskIndex = 0; taskIndex < data.length; taskIndex++) {
                        reportRequest.reportRequestActionItems.add_action_item_from_task(data[taskIndex], dueDate, startDate, endDate);
                    }
            })
        }
        else{
            $("#noDueDate").html($.i18n._('dueDateNotNull') );
            $("#noDueDate").addClass("show");
        }

    });

    $("#dueDateDiv").on('changed.fu.datepicker dateClicked.fu.datepicker', function (){
        var dueDate=$("#dueDate").val();
        $(".reportActionItemDiv .panelDiv .panel ").each(function(i,v){
            var requestDueDateMoment = moment(dueDate, DEFAULT_DATE_DISPLAY_FORMAT);
            var operand=$(this).find(".aiDateSign").val();
            var date=$(this).find(".aiDate").val();
            if (operand == '+') {
                requestDueDateMoment.add(parseInt(date), 'days');
            } else if (operand == '-') {
                requestDueDateMoment.subtract(parseInt(date), 'days');
            }
            actionItemDueDate = requestDueDateMoment.format(DEFAULT_DATE_DISPLAY_FORMAT);
            $(".actionItemDueDateAssignedTo").html(actionItemDueDate);
        })
    })

    $(".update_button").on("click", function () {
        var errMessage = "";
        if ($("#reportRequestType\\.id").select2().find(":selected").data("aggregate") === true) {
            var error = "";
            if (_.isEmpty($("#reportingPeriodStart").val())) {
                error += "Please fill Reporting Period Start date!";
            }
            if ($("#frequency").val() == "RUN_ONCE" && _.isEmpty($("#reportingPeriodEnd").val())) {
                error += "Please fill Reporting Period End date!";
            }
            if ($("#frequency").val() != "RUN_ONCE" && _.isEmpty($("#frequencyX").val())) {
                error += "Please fill frequency value!";
            }
            if ($("#frequency").val() != "RUN_ONCE" && _.isEmpty($("#occurrences").val())) {
                error += "Please fill End After X Occurrences value!";
            }
            if (!_.isEmpty(error)) {
                $('#errorModal .description').text(error);
                $('#errorModal').modal('show');

                return
            }
        }
        if ($("#status").val() == "CLOSED") {
            $statuses = $(".statusString");
            for(var i=0;i<$statuses.length;i++){
                if($($statuses[i]).html()!="Closed"){
                    errMessage = $.i18n._('reportRequest.actionItem.warning.label');
                    break;
                }
            }
        }
        showWarningOrSubmit(saveUrl, errMessage)
    });

    $(".createReport_button").on("click",function(){
        var forAdhoc = $(this).attr('forAdhoc');
        var createReportUrl = (forAdhoc === "true") ? createAdhocReportUrl : createAggregateReportUrl;
        $("form#configurationForm").attr("action", createReportUrl);
        $("form#configurationForm").trigger('submit');
    });

    $(document).on("click", ".deleteAttachment", function () {
        $("#attachmentsToDelete").val($("#attachmentsToDelete").val() + "," + $(this).attr("data-id"));
        $(this).parent().css("text-decoration", "line-through");
        $(this).find(".btn").prop('disabled', true);
    });
    $("#reportRequestLinkModal").on('shown.bs.modal', function (e) {
        $('#deleteDlgErrorDiv').hide();
        $("#RRLinkTo").val(null).trigger('change');
        $("#RRLinkDescription").val("")
        $('#linkType').prop("disabled", false);
        $('#RRLinkTo').prop("disabled", false);
    });
    $(document).on("click", "#saveLinkButton", function () {
        var linksToAddString = $("#linksToAdd").val();
        var linksToAdd = linksToAddString ? JSON.parse(linksToAddString) : [];
        var data = {
            "id": 0,
            "altId": linksToAdd.length,
            "from": $("#RRLinkFrom").val(),
            "to": $("#RRLinkTo").select2('val'),
            "toname": $("#RRLinkTo").select2('data').text,
            "linkType": $("#linkType").select2('val'),
            "linkTypename": $("#linkType").select2('data').text,
            "description": $("#RRLinkDescription").val()
        };
        if (data.to === "" || data.linkType === "") {
            $('#deleteDlgErrorDiv').show();
        } else {
            linksToAdd.push(data);
            $("#linksToAdd").val(JSON.stringify(linksToAdd));
            $("#RRlinkList").append(createRRLink(data));
            $('#reportRequestLinkModal').modal('hide');
        }
    });

    function createRRLink(data) {
        var description = pvr.common_util.preserve_line_break(encodeToHTML(data.description));
        return '<div style="margin-top: 5px;"><a href="javascript:void(0)" class="btn btn-xs btn-primary deleteNewLink" data-id="' + data.altId + '"><span class="fa fa-remove" title="' + $.i18n._('delete') + '"></span> </a>' +
            '<b>' + encodeToHTML(data.linkTypename) + '</b> <span class="glyphicon glyphicon-arrow-right"></span><a href="show?id=' + data.to + '" >' + encodeToHTML(data.toname) + '</a> ' +
            description + '</div>';
    }

    $(document).on("click", ".deleteNewLink", function () {
        var id = $(this).attr("data-id");
        var linksToAddString = $("#linksToAdd").val();
        var linksToAdd = linksToAddString ? JSON.parse(linksToAddString) : [];
        linksToAdd = _(linksToAdd).filter(function (item) {
            return item.altId !== id
        });
        $("#linksToAdd").val(JSON.stringify(linksToAdd));
        $(this).parent().remove();
    });

    $(document).on("click", ".deleteLink", function () {
        $("#linksToDelete").val($("#linksToDelete").val() + "," + $(this).attr("data-id"));
        $(this).parent().remove();
    });

    $('[name=file]').on('change', function (evt, numFiles, label) {
        $("#file_Status").hide().empty();
        var files = $('[name=file]')[0].files;
            var fileSize = 0;
            var fileNames = [];
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                fileStatus(file.name);
                fileSize += file.size;
                fileNames.push(file.name);
            }

        if (fileSize > AttachmentSizeLimit) {
            var maxSize = (AttachmentSizeLimit / (1024 * 1024)).toFixed(2);
            $("#file_Status").show().html('<span style="color:red">'+ $.i18n._('issue.File.maxSize')+ ' ' + maxSize + ' MB. ' + $.i18n._('attachment.compress.request') + '</span>');
            $("#file_input").val("");
            $("#file_name").val("");
        } else {
            $("#file_name").val(fileNames.join(";"));
        }
    });

    function removeDeletedLinks() {
        var toDelete = $("#linksToDelete").val();
        if (toDelete) {
            _.each(toDelete.split(","), function (value, key, list) {
                $(".deleteLink[data-id='" + value + "']").parent().remove();
            });
        }

    }

    function addAddedLinks() {
        var toAdd = $("#linksToAdd").val();
        if (toAdd) {
            _.each(JSON.parse(toAdd), function (value, key, list) {
                $("#RRlinkList").append(createRRLink(value));
            });
        }
    }

    removeDeletedLinks();
    addAddedLinks();
    bindSelect2WithUrl($("#RRLinkTo"), reportRequestDropdownURL, null, true);

    if (typeof configurationRedirectURL !== 'undefined') {

        $(".datepicker").each(function() {
            var params = {
                allowPastDates: true,
                twoDigitYearProtection: true,
                momentConfig: {
                    format: DEFAULT_DATE_DISPLAY_FORMAT
                }
            }
            var val = $(this).find("input").val();
            if(val) params.date=val;
            $(this).datepicker(params);
        });

        buildReportingDestinationsSelectBox($("select[name='reportingDestinations']"), reportingDestinationsUrl, $("input[name='primaryReportingDestination']"), true);

        function updateLinkedConfigurationList() {
            var cfgString = $('#linkedConfigurations').val();
            var data = cfgString ? JSON.parse(cfgString) : [];
            var content = "";
            for (var i = 0; i < data.length; i++) {
                content += '<div style="margin-left: 20px;margin-top: 5px;"> <a href="javascript:void(0)" class="btn btn-xs btn-primary deleteLinkedConfiguration" data-id="' + data[i].id + '"><span class="fa fa-remove" ></span> </a>' +
                    ' <a href="' + configurationRedirectURL + '?id=' + data[i].id + '">' + encodeToHTML(data[i].name) + '</a></div>';
            }
            $("#linkedConfigurationsDiv").html(content);
        }

        $(document).on("click", ".linkConfiguration", function () {
            var selected = $("#configurationDropdown").select2('data')[0];
            var cfgString = $('#linkedConfigurations').val();
            var data = cfgString ? JSON.parse(cfgString) : [];
            if (!_.find(data, function (e) {
                return e.id == selected.id
            })) {
                data.push({id: selected.id, name: selected.text});
                $('#linkedConfigurations').val(JSON.stringify(data));
                updateLinkedConfigurationList();
            }
        });

        $(document).on("click", ".deleteLinkedConfiguration", function () {
            var cfgString = $('#linkedConfigurations').val();
            var id = $(this).attr("data-id")
            var data = cfgString ? JSON.parse(cfgString) : [];
            data = _.filter(data, function (el) {
                return el.id != id
            });
            $('#linkedConfigurations').val(JSON.stringify(data));
            updateLinkedConfigurationList()
        });

        bindSelect2WithUrl($("#configurationDropdown"), configurationsListUrl, null, true);
        updateLinkedConfigurationList();

        function updateLinkedGeneratedReportList() {
            var cfgString = $('#linkedGeneratedReports').val();
            var data = cfgString ? JSON.parse(cfgString) : [];
            var content = "";
            for (var i = 0; i < data.length; i++) {
                content += '<div style="margin-left: 20px;margin-top: 5px;"> <a href="javascript:void(0)" class="btn btn-xs btn-primary deleteLinkedGeneratedReport" data-id="' + data[i].id + '"><span class="fa fa-remove" ></span> </a>' +
                    ' <a href="' + reportRedirectURL + '?id=' + data[i].id + '">' + encodeToHTML(data[i].name) + '</a></div>';
            }
            $("#linkedGeneratedReportsDiv").html(content);
        }

        $(document).on("click", ".linkedGeneratedReport", function () {
            var selected = $("#generatedReportsDropdown").select2('data')[0];
            var cfgString = $('#linkedGeneratedReports').val();
            var data = cfgString ? JSON.parse(cfgString) : [];
            if (!_.find(data, function (e) {
                return e.id == selected.id
            })) {
                data.push({id: selected.id, name: selected.text});
                $('#linkedGeneratedReports').val(JSON.stringify(data));
                updateLinkedGeneratedReportList();
            }
        });

        $(document).on("click", ".deleteLinkedGeneratedReport", function () {
            var cfgString = $('#linkedGeneratedReports').val();
            var id = $(this).attr("data-id")
            var data = cfgString ? JSON.parse(cfgString) : [];
            data = _.filter(data, function (el) {
                return el.id != id
            });
            $('#linkedGeneratedReports').val(JSON.stringify(data));
            updateLinkedGeneratedReportList()
        });

        $(document).on("change", "#reportRequestType\\.id", function () {
            showHideAggregateReportInformation()
        });

        function showHideAggregateReportInformation() {
            if ($("#reportRequestType\\.id").select2().find(":selected").data("aggregate") === true) {
                $(".aggregateReportInformation").show();
                $(".adhocReportInformation").hide();
                $("#eventSelection").parent().hide();
                $("#excludeFollowUp").parent().parent().hide();
                $(".createAggregateReport").show();
                $(".createAdhocReport").hide();
                showHideMasterPlanningRequest();
                if(!$("#curPrdDueDate").val()) recalculateCurPrdDueDate();
                $("button[forAdhoc=false]").show();
                $("button[forAdhoc=true]").hide();
            } else {
                $(".aggregateReportInformation").hide();
                $(".adhocReportInformation").show();
                $("#eventSelection").parent().show();
                $("#excludeFollowUp").parent().parent().show();
                $(".createAggregateReport").hide();
                $(".createAdhocReport").show();
                $("button[forAdhoc=false]").hide();
                $("button[forAdhoc=true]").show();
            }
            var val = $("#reportRequestType\\.id").val();
            $(".reportRequestTypeSpecific").each(function () {
                if ($(this).hasClass("reportRequestType_" + val))
                    $(this).show();
                else
                    $(this).hide();
            });

        }

        showDictionaryWidget($("#psrTypeFile"));
        bindSelect2WithUrl($("#generatedReportsDropdown"), reportsListUrl, null, true);
        buildReportingDestinationsSelectBox($("select[name='publisherContributors']"), publisherContributorsUrl, $("input[name='primaryPublisherContributor']"), true,userValuesUrl);
        updateLinkedGeneratedReportList();
        showHideAggregateReportInformation();

        function showHideMasterPlanningRequest() {
            if ($("#masterPlanningRequest").is(":checked"))
                $(".masterPlanningRequest").show();
            else
                $(".masterPlanningRequest").hide();
        }

        $(document).on("change", "#masterPlanningRequest", function () {
            showHideMasterPlanningRequest()
        });

        $(document).on("click", ".setAsParent", function () {
            var $this = $(this);
            $("#parentLink").html('<a href="' + reportRequestURL + '?id=' + $this.attr("data-id") + '">(ID:' + $this.attr("data-id") + ') ' + $this.attr("data-name") + '</a>')
            $("#parentReportRequest").val($this.attr("data-id"))

        });

        $(document).on("change", "#frequency", function () {
            checkFrequence()
        });

        function checkFrequence() {
            if ($("#frequency").val() == "RUN_ONCE") {
                $("#frequencyX").attr("disabled", true);
                $("#reportingPeriodEnd").attr("disabled", false);
                $("#occurrences").attr("disabled", true);
            } else {
                $("#frequencyX").attr("disabled", false);
                $("#reportingPeriodEnd").val("");
                $("#reportingPeriodEnd").attr("disabled", true);
                $("#occurrences").attr("disabled", false);
            }
        }

        checkFrequence();

        $(document).on("click", ".remooveParent", function () {
            $("#parentLink").html('');
            $("#parentReportRequest").val("");
        });


    }

    $(document).on("change", "#dueInToHa, #reportingPeriodStart, #frequency, #frequencyX, #reportingPeriodEnd,#occurrences", function () {
        recalculateCurPrdDueDate();
    });
    $(document).on("changed.fu.datepicker dateClicked.fu.datepicker", "#reportingPeriodStartDiv, #reportingPeriodEndDiv", function () {
        recalculateCurPrdDueDate();
    });

    function getReportingPeriodEnd() {
        if (($("#frequency").val() == "RUN_ONCE") && !_.isEmpty($("#reportingPeriodEnd").val())) {
            return moment($("#reportingPeriodEnd").val(), DEFAULT_DATE_DISPLAY_FORMAT);
        }
        if (($("#frequency").val() != "RUN_ONCE") && !_.isEmpty($("#reportingPeriodStart").val())) {
            var frequencyX = $("#frequencyX").val();
            if(!frequencyX) frequencyX=1;
            var endMoment = moment($("#reportingPeriodStart").val(), DEFAULT_DATE_DISPLAY_FORMAT);

            switch ($("#frequency").val()) {
                case "DAILY":
                    endMoment.add(frequencyX , 'days');
                    break;
                case "WEEKLY":
                    endMoment.add(frequencyX, 'week');
                    break;
                case "MONTHLY":
                    endMoment.add(frequencyX , 'month');
                    break;
                case "YEARLY":
                    endMoment.add(frequencyX , 'year');
                    break;
                case "HOURLY":
                    endMoment.add(frequencyX , 'hour');
                    break
            }
            return endMoment;
        }
        return null;
    }

    function recalculateCurPrdDueDate() {

        var endMoment = getReportingPeriodEnd();
        if (endMoment) {
            var dueInToHa = $("#dueInToHa").val()
            if(!dueInToHa) dueInToHa=0;
            endMoment.add($("#dueInToHa").val(), 'days');
            $("#curPrdDueDate").val(endMoment.format(DEFAULT_DATE_DISPLAY_FORMAT));
            $("#dueDate").val(endMoment.format(DEFAULT_DATE_DISPLAY_FORMAT));
            if ($("#reportRequestType\\.id").select2("data").text == "ACO")
                endMoment.subtract(5, 'days');
            else
                endMoment.subtract(10, 'days');
            $("#dueDateForDistribution").val(endMoment.format(DEFAULT_DATE_DISPLAY_FORMAT));

            var to = getReportingPeriodEnd();
            var from = moment($("#reportingPeriodStart").val(), DEFAULT_DATE_DISPLAY_FORMAT).subtract(1, 'days');
            var out = to.diff(from, 'days') + " days";

            $("#periodCoveredByReport1").val(out);
            $("#periodCoveredByReport2").val(out);
        } else{
            $("#curPrdDueDate").val(endMoment.format(DEFAULT_DATE_DISPLAY_FORMAT));
        }


    }

    function fileStatus(fileName){
        var fileExtension = fileName.match(/(\w+)$/);
        if(fileName.length < 255 ){
            if(!extensionCheck.includes(fileExtension[0].toLowerCase())){
                $("#file_Status").show();
                $("#file_Status").html('<span style=\'color:red\'>File type not supported</span>');
            }
            else
                $("#file_Status").hide();
        } else
            $("#file_Status").html('<span style=\'color:#ff0000\'>Invalid file name</span>');
    };
    $(document).on("click", ".closeLinkSelect", function () {
        $('#linkType').prop("disabled", true);
        $('#RRLinkTo').prop("disabled", true);

    });

    $("#RRLinkTypeDiv").mouseover(function(){
        $("#linkType").attr('disabled', false);
        $("#RRLinkTo").attr("disabled",true);

    })
    $("#RRLinkToDiv").mouseover(function(){
        $("#linkType").attr('disabled', true);
        $("#RRLinkTo").attr("disabled",false);
    })

});
