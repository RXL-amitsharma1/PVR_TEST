var reportRequest = reportRequest || {}

reportRequest.reportRequestActionItems = (function() {

    var getActionItemModalObj = function(actionItemModalObj) {

        var actionItemObj = {
            "assignedToLabel": actionItemModalObj.find("#assignedTo").select2('data').text,
            "assignedTo": actionItemModalObj.find("#assignedTo").val(),
            "dueDate": actionItemModalObj.find("#dueDate").val(),
            "status": actionItemModalObj.find("#status").val(),
            "actionCategory": actionItemModalObj.find("#actionCategory").val(),
            "completionDate": actionItemModalObj.find("#completionDate").val(),
            "priority": actionItemModalObj.find("#priority").val(),
            "description": actionItemModalObj.find("#description").val(),
            "comment": actionItemModalObj.find("#comment").val()
        }
        return actionItemObj
    }

    var add_action_item_to_request = function(actionItemModalObj) {
        var actionItem = getActionItemModalObj(actionItemModalObj);
        actionItem.description = encodeToHTML(actionItem.description);
        actionItem.comment = encodeToHTML(actionItem.comment);
        add_action_item(actionItem);
    }

    var add_action_item_from_task = function (taskObj, dueDate, startDate, endDate) {
        var taskDueDate = taskObj.dueDate;
        var taskDueDateSign = taskObj.dueDateSign;
        var requestDueDate = dueDate;
        if (taskObj.baseDate == "REPORT_PERIOD_START") requestDueDate = startDate;
        if (taskObj.baseDate == "REPORT_PERIOD_END") requestDueDate = endDate;
        var actionItemDueDate = ''
        if (typeof requestDueDate != "undefined" && requestDueDate != null
            && requestDueDate != '') {

            //Prepare the date from request due date
            var requestDueDateMoment = moment.tz(requestDueDate,DEFAULT_DATE_DISPLAY_FORMAT,userTimeZone);
            var daysToAdd = taskDueDate;
            var operand = taskDueDateSign;
            //Based on the operand's value the dates will be adjusted
            //If any other operand will be send then due date will be used as it is.
            if (operand == '+') {
                requestDueDateMoment.add(parseInt(daysToAdd), 'days');
            } else if (operand == '-') {
                requestDueDateMoment.subtract(parseInt(daysToAdd), 'days');
            }

            actionItemDueDate = requestDueDateMoment.format(DEFAULT_DATE_DISPLAY_FORMAT);
        }

        var actionItemObj = {
            "assignedToLabel": $("#assignedTo").select2('data').text,
            "assignedTo": $("#assignedTo").val(),
            "dueDate": actionItemDueDate,
            "status": $("#status").val(),
            "actionCategory": ACTION_ITEM_CATEGORY_ENUM.REPORT_REQUEST,
            "completionDate": '',
            "priority": taskObj.priority,
            "description": taskObj.taskName,
            dueDateTask:taskObj.dueDate,
            signDueDateTask:taskObj.dueDateSign,
            comment:""
        };
        add_action_item(actionItemObj);
    };

    var add_action_item = function(actionItemObj) {

        if (typeof taskCounter == "undefined" || taskCounter == '') {
            taskCounter = 0;
        }

        //The panel parent div
        var reportCommentDiv = $("<div>");
        reportCommentDiv.attr("class", "panelDiv");
        reportCommentDiv.attr("data-id", taskCounter);

        //Pane div
        var panelDiv = $("<div>");
        panelDiv.attr("class", "panel panel-default");

        //Panel head
        var panelHeadDiv = $("<div>")
        //panelHeadDiv.attr("class", "panel-heading");

        var hiddenDate= $("<input type='hidden'>");
        hiddenDate.attr("class","aiDate")
        hiddenDate.val(actionItemObj.dueDateTask);

        var hiddenDateSign= $("<input type='hidden'>");
        hiddenDateSign.attr("class","aiDateSign");
        hiddenDateSign.val(actionItemObj.signDueDateTask);

        var assignedToUser = actionItemObj.assignedToLabel;

        //User
        var user = $("<strong>");
        user.attr("class", "assignedTo");
        user.html("Assigned to:  " + encodeToHTML(assignedToUser));

        //Commented on span
        var spanElement = $("<span>");
        spanElement.attr("class", "text-muted");
        var date = new Date();
        var dueDate = actionItemObj.dueDate ? moment(actionItemObj.dueDate).format(DEFAULT_DATE_DISPLAY_FORMAT) : ''
        spanElement.html("  with due date on : <span class='actionItemDueDateAssignedTo'>"+ dueDate +"</span>");

        //Delete image div
        var deleteImageDiv = $("<div>");
        deleteImageDiv.attr("style", "float: right; cursor: pointer");

        //Delete image
        var deleteImage = $("<span>");
        deleteImage.attr("class", "glyphicon glyphicon-trash reportRequest");
        deleteImageDiv.append(deleteImage);

        deleteImage.on("click", function() {
            var panel = $(this).parents('.panel');
            panel.find('.actionItemDeleted').val(true);
            panel.closest('.panelDiv').hide();
            var actionItemDeleteMsg=$.i18n._('app.label.action.app.name') + " " + $.i18n._('app.reportRequest.actionItem.comment.delete');
            showDeleteMessage(actionItemDeleteMsg);
        });

        //Edit image div
        var editImageDiv = $("<div>");
        editImageDiv.attr("style", "float:right; cursor:pointer");

        //Edit Image
        var editImage = $("<span>");
        editImage.attr("class", "glyphicon glyphicon-edit reportRequest");
        editImageDiv.append(editImage);

        editImage.on("click", function() {
            var index = $(this).closest(".panelDiv").attr("data-id");
            actionItem.actionItemModal.edit_action_item(hasAccessOnActionItem,null, false, REPOERT_REQUEST, index);
        });

        //Status Icons Div
        var statusIconsDiv = $("<div>");
        statusIconsDiv.attr("style", "float: right");

        var status = actionItemObj.status;

        var statusImg = getStatusImage(status);

        var spaceSpan = $("<span>");
        spaceSpan.html("&nbsp");

        statusIconsDiv.append(statusImg);
        statusIconsDiv.append(spaceSpan);

        panelHeadDiv.append(hiddenDate);
        panelHeadDiv.append(hiddenDateSign);
        panelHeadDiv.append(user);
        panelHeadDiv.append(spanElement);
        panelHeadDiv.append(deleteImageDiv);
        panelHeadDiv.append(editImageDiv);
        panelHeadDiv.append(statusIconsDiv);

        var panelBodyDiv = $("<div>");
        panelBodyDiv.attr("style", "height: auto; width:80%");

        populateActionItems(taskCounter, actionItemObj, panelBodyDiv);

        panelDiv.append(panelHeadDiv);
        panelDiv.append(panelBodyDiv);

        reportCommentDiv.append(panelDiv);

        $(".reportActionItemDiv").append(reportCommentDiv)
        $(".noActionItem").hide();


        taskCounter++
    }

    var getStatusImage = function(status) {
        var statusImg = $("<span>");
        statusImg.attr("style", getStatusColor(status))
        statusImg.attr("class", "statusString");
        statusImg.html($.i18n._('status_enum.'+status))
        return statusImg
    }

    var getStatusColor = function (status) {
        var styleAttr = '';
        if (status == STATUS_ENUM.OPEN) {
            styleAttr = "color:blue";
        } else if (status == STATUS_ENUM.IN_PROGRESS || status == STATUS_ENUM.NEED_CLARIFICATION) {
            styleAttr = "color:orange";
        } else if (status == STATUS_ENUM.CLOSED) {
            styleAttr = "color:green";
        }
        return styleAttr
    };

    //Method to populate the action Item divs.
    //TODO: Need to write better solution than this.
    var populateActionItems = function(taskCounter, actionItemObj, panelBodyDiv) {

        var actionCategory = getNewHiddenElement();
        actionCategory.attr("id", "actionItems[" + taskCounter + "].actionCategory");
        actionCategory.attr("name", "actionItems[" + taskCounter + "].actionCategory");
        actionCategory.attr("value", actionItemObj.actionCategory);
        panelBodyDiv.append(actionCategory);

        var assignedTo = getNewHiddenElement();
        assignedTo.attr("id", "actionItems[" + taskCounter + "].assignedTo");
        assignedTo.attr("name", "actionItems[" + taskCounter + "].assignedTo");
        assignedTo.attr("value", actionItemObj.assignedTo);
        panelBodyDiv.append(assignedTo);

        var dueDate = getNewHiddenElement();
        dueDate.attr("id", "actionItems[" + taskCounter + "].dueDate");
        dueDate.attr("name", "actionItems[" + taskCounter + "].dueDate");
        dueDate.attr("value", actionItemObj.dueDate ? moment(actionItemObj.dueDate).format(DEFAULT_DATE_DISPLAY_FORMAT) : '');
        panelBodyDiv.append(dueDate);

        var completionDate = getNewHiddenElement();
        completionDate.attr("id", "actionItems[" + taskCounter + "].completionDate");
        completionDate.attr("name", "actionItems[" + taskCounter + "].completionDate");
        completionDate.attr("value", actionItemObj.completionDate ? moment(actionItemObj.completionDate).format(DEFAULT_DATE_DISPLAY_FORMAT) : '');
        panelBodyDiv.append(completionDate);

        var dateCreated = getNewHiddenElement();
        dateCreated.attr("id", "actionItems[" + taskCounter + "].dateCreated");
        dateCreated.attr("name", "actionItems[" + taskCounter + "].dateCreatedObj");
        dateCreated.attr("value", moment(new Date()).tz(userTimeZone).format(DATEPICKER_FORMAT_AM_PM));
        panelBodyDiv.append(dateCreated);

        var priority = getNewHiddenElement();
        priority.attr("id", "actionItems[" + taskCounter + "].priority");
        priority.attr("name", "actionItems[" + taskCounter + "].priority");
        priority.attr("value", actionItemObj.priority);
        panelBodyDiv.append(priority);

        var status = getNewHiddenElement();
        status.attr("id", "actionItems[" + taskCounter + "].status");
        status.attr("name", "actionItems[" + taskCounter + "].status");
        status.attr("value", actionItemObj.status);
        panelBodyDiv.append(status);

        var appType = getNewHiddenElement();
        appType.attr("id", "actionItems[" + taskCounter + "].appType");
        appType.attr("name", "actionItems[" + taskCounter + "].appType");
        appType.attr("value", "REPORT_REQUEST");
        panelBodyDiv.append(appType);

        var deletedEle = getNewHiddenElement();
        deletedEle.attr("id", "actionItems[" + taskCounter + "].deleted");
        deletedEle.attr("name", "actionItems[" + taskCounter + "].deleted");
        deletedEle.attr("class", "reportRequest actionItemDeleted");
        deletedEle.attr("value", false);
        panelBodyDiv.append(deletedEle);

        var idEle = getNewHiddenElement();
        idEle.attr("id", "actionItems[" + taskCounter + "].id");
        idEle.attr("name", "actionItems[" + taskCounter + "].id");
        idEle.attr("value", null);
        panelBodyDiv.append(idEle);

        var newEle = getNewHiddenElement();
        newEle.attr("id", "actionItems[" + taskCounter + "].newObj");
        newEle.attr("name", "actionItems[" + taskCounter + "].newObj");
        newEle.attr("value", true);
        panelBodyDiv.append(newEle);

        var description = getNewHiddenElement();
        description.attr("id", "actionItems[" + taskCounter + "].description");
        description.attr("name", "actionItems[" + taskCounter + "].description");
        //description.attr("class", "commentInput");
        description.attr("value", actionItemObj.description);
        panelBodyDiv.append(description);

        var comment = getNewHiddenElement();
        comment.attr("id", "actionItems[" + taskCounter + "].comment");
        comment.attr("name", "actionItems[" + taskCounter + "].comment");
        comment.attr("value", actionItemObj.comment);
        panelBodyDiv.append(comment);

        var descriptionMessage = pvr.common_util.preserve_line_break(actionItemObj.description);
        var descriptionSpan = $("<span>");
        descriptionSpan.attr("id", "actionItems[" + taskCounter + "].display");
        descriptionSpan.attr("class", "reportRequest");
        descriptionSpan.attr("style", "word-wrap: break-word");
        descriptionSpan.html(descriptionMessage);
        panelBodyDiv.append(descriptionSpan);

    }

    //This method modifies the added action item divs in the report request form.
    var modifyAddedActionItem = function(index, actionItemModalObj) {

        var reportActionItemElements = $(".reportActionItemDiv").find('.panelDiv[data-id='+index+']').find('.reportRequest')

        //Update the values for the report request.
        reportActionItemElements.each(function() {

            var currentElement = $(this);
            var id = currentElement.attr("id");

            if (typeof id != "undefined" && id != null) {

                if (id == getActionItemElementId(index, "actionCategory")) {
                    currentElement.val(actionItemModalObj.find("#actionCategory").val());
                }

                if (id == getActionItemElementId(index, "assignedTo")) {
                    currentElement.val(actionItemModalObj.find("#assignedTo").val());
                }

                if (id == getActionItemElementId(index, "dueDate")) {
                    currentElement.val(actionItemModalObj.find("#dueDate").val());
                }

                if (id == getActionItemElementId(index, "completionDate")) {
                    currentElement.val(actionItemModalObj.find("#completionDate").val());
                }

                if (id == getActionItemElementId(index, "priority")) {
                    currentElement.val(actionItemModalObj.find("#priority").val());
                }

                if (id == getActionItemElementId(index, "status")) {
                    currentElement.val(actionItemModalObj.find("#status").val());
                }

                if (id == getActionItemElementId(index, "description")) {
                    currentElement.val(actionItemModalObj.find("#description").val());
                }
                if (id == getActionItemElementId(index, "comment")) {
                    currentElement.val(actionItemModalObj.find("#comment").val());
                }
            }

        });

        //Now update the status icon and data.
        $(".reportActionItemDiv").find('.panelDiv[data-id='+index+']').find('.text-muted').html("  with due date on : "+ actionItemModalObj.find("#dueDate").val());
        var statusString = $(".reportActionItemDiv").find('.panelDiv[data-id='+index+']').find('.statusString')
        statusString.attr("style", getStatusColor(actionItemModalObj.find("#status").val()));
        statusString.html(actionItemModalObj.find("#status").val());
        var assignedToUser = actionItemModalObj.find("#assignedTo").select2('data').text;
        $(".reportActionItemDiv").find('.panelDiv[data-id='+index+']').find('.assignedTo').html("Assigned to: "+ encodeToHTML(assignedToUser));
        $(".reportActionItemDiv").find('.panelDiv[data-id='+index+']').find('#actionItems\\['+index+'\\]\\.display').html(encodeToHTML(actionItemModalObj.find("#description").val()));

    }

    var getActionItemElementId = function(index, field) {
        return "actionItems["+index+"]."+field
    }

    //Method to generate the hidden elements.
    var getNewHiddenElement = function() {
        var input = $("<input>");
        input.attr("class", "reportRequest");
        input.attr("type", "hidden");
        return input;
    }

    return {
        add_action_item : add_action_item,
        modifyAddedActionItem : modifyAddedActionItem,
        add_action_item_to_request : add_action_item_to_request,
        add_action_item_from_task : add_action_item_from_task
    }

})()