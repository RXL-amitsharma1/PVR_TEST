var multipleId;
var selectedIds = [];
var commentUpdated = false;
$(function () {

    var commentDialog = new CommentDialog('', '', $('#appContext').val(), $("#commentsList"), $('form[name="annotationForm"]'));

    $(document).on('click', '.commentModalTrigger', function (e) {
        var viewMode = ($(this).attr("data-viewMode") == "true" || ((typeof isPvcEditor != 'undefined') && !isPvcEditor));
        commentDialog.ownerId = this.getAttribute('data-owner-id');
        commentDialog.commentType = this.getAttribute('data-comment-type');
        commentDialog.triggerPoint = $(this).find(".annotationPopover i");
        commentUpdated = false;

        hideCommentForm();

        $("input[name='ownerId']").val(commentDialog.ownerId);
        $("input[name='commentType']").val(commentDialog.commentType);
        $("#commentsTitle").text($("input[name='" + commentDialog.commentType + "']").val());
        commentDialog.reloadComments(viewMode);
    });

    $('.saveComment').on('click', function () {
        commentDialog.save();
    });

    $('.hideCommentForm').on('click', function () {
        hideCommentForm();
    });

    bindPopOverEvents($('.commentPopoverMessage'));

    $("[data-evt-clk]").on('click', function(e) {
        e.preventDefault();
        const eventData = JSON.parse($(this).attr("data-evt-clk"));
        const methodName = eventData.method;
        const params = eventData.params;

        if (methodName == "showCommentForm") {
            // Call the method from the eventHandlers object with the params
            showCommentForm();
        }
    });
});

function CommentDialog(ownerId, commentType, appContext, commentsDiv, formObject, triggerPoint) {
    this.ownerId = ownerId;
    this.commentType = commentType;
    this.formObj = formObject;
    this.appContext = appContext;
    this.commentsToDisplayIn = commentsDiv;
    this.triggerPoint = triggerPoint;

    this.save = function () {
        commentUpdated = true;
        multipleId = ""
        if ((typeof selectedIds !== "undefined") && selectedIds.length > 0 && selectedIds.length != 0) {
            $.each(selectedIds, function (index, jsonObject) {
                if (multipleId != null && multipleId.length > 0) {
                    multipleId += "," + jsonObject.cllRowId;
                } else {
                    multipleId = jsonObject.cllRowId;
                }
            });
        }
        var parent = this;
        var dataSave = parent.formObj.serialize();

        if (multipleId != null && multipleId.length > 0) {
            dataSave += '&multipleIds=' + multipleId;
        }
        $('.saveComment').prop('disabled',true);
        $.ajax({
            url: parent.appContext + "/commentRest/save",
            method: 'POST',
            data: dataSave,
        })
            .done(function (result) {
                $('.saveComment').prop('disabled',false);
                if (result.success) {
                    parent.reloadComments();
                    parent.resetForm();
                    changeAnnotationIcon(parent.triggerPoint, false);
                    if ($("#commentType").prop('value') === "REPORT_RESULT") {
                        updateChartAnnotation(reportId);
                    }
                } else {
                    if (result.errors != undefined) {
                        $.each(result.errors, function (index, e) {
                            var field = parent.formObj.find('[name="' + e + '"]');
                            if (field != undefined) {
                                field.parent().addClass('has-error');
                            }
                        });
                    }
                    if (parent.formObj.find('.has-error').size() == 0) {
                        var errorMessage = result.msg;
                        if (errorMessage){
                            $('#errorModal .description').text(errorMessage);
                            $("#errorModal").modal('show');
                            return;
                        }
                    }
                }
                if (multipleId != null && multipleId.length > 0) {
                    $("#commentModal").modal("hide");
                    reloadRodTable($.i18n._('comment.success'));
                }

            })
            .fail(function () {
                $('.saveComment').prop('disabled',false);
                alert("Sorry! System level error")
            });
    };

    this.delete = function (commentId) {
        commentUpdated = true;
        multipleId = ""
        if (selectedIds.length > 0 && selectedIds.length != 0) {
            $.each(selectedIds, function (index, jsonObject) {
                    if (multipleId != null && multipleId.length > 0) {
                        multipleId += "," + jsonObject.cllRowId;
                    } else {
                        multipleId = jsonObject.cllRowId;
                    }
            });
        }
        var parent = this;
        if (multipleId == undefined || multipleId.length == 0) {
            multipleId = "";
        }
        var imgTag = $(`img.delete[data-id="${commentId}"]`);
        imgTag.addClass('disabled');
        $.ajax({
            url: this.appContext + "/commentRest/delete",
            data: {
                ownerId: this.ownerId,
                commentType: this.commentType,
                "comment.id": commentId,
                "multipleIds": multipleId
            },
            method: 'POST',
        })
            .done(function (result) {
                imgTag.removeClass('disabled');
                if (result.success) {
                    parent.reloadComments();
                    if (result.isCommentListEmpty) {
                        changeAnnotationIcon(parent.triggerPoint, true);
                    }
                    if ($("#commentType").prop('value') === "REPORT_RESULT") {
                        updateChartAnnotation(reportId);
                    }
                } else {
                    var errorMessage = result.msg;
                    if (errorMessage){
                        $('#errorModal .description').text(errorMessage);
                        $("#errorModal").modal('show');
                        return;
                    }
                }
                if (multipleId != null && multipleId.length > 0) {
                    $("#commentModal").modal("hide");
                    reloadRodTable($.i18n._('comment.success'));
                }
            })
            .fail(function () {
                imgTag.removeClass('disabled');
                alert("Sorry! System level error")
            });
    };

    this.reloadComments = function (viewMode) {
        var parent = this;
        $.ajax({
            url: this.appContext + "/commentRest/loadComments",
            data: {
                ownerId: this.ownerId,
                commentType: this.commentType
            },
            method: 'POST',
        })
            .done(function (result) {
                parent.commentsToDisplayIn.html(result);
                if (!viewMode) {
                    parent.commentsToDisplayIn.find(".delete").on("click", function (e) {
                        parent.delete($(this).data('id'));
                    })
                } else {
                    parent.commentsToDisplayIn.find(".delete").detach();
                    $(".btn-add-comment").hide();
                }

            })
            .fail(function () {
                alert("Sorry! System level error")
            });
    };

    this.resetForm = function () {
        this.formObj.find(".has-error").removeClass("has-error");
        this.formObj.find("textArea").val("")
    }
}

function changeAnnotationIcon(iconElement, isCommentListEmpty) {
    if (isCommentListEmpty) {
        iconElement.removeClass("fa-commenting-o commentPopoverMessage");
        iconElement.addClass("fa-comment-o");
        iconElement.find(".badge").hide();
    } else {
        if (iconElement.hasClass("fa-comment-o")) {
            iconElement.removeClass("fa-comment-o");
            iconElement.addClass("fa-commenting-o commentPopoverMessage");
            iconElement.find(".badge").show();
            bindPopOverEvents(iconElement);
        }
    }
}

function hideCommentForm() {
    var commentModal = $('#commentModal');
    commentModal.find('div.add-comment-component').hide();
    commentModal.find('div.btn-add-comment').show();
}

function showCommentForm() {
    var commentModal = $('#commentModal');
    commentModal.find('div.add-comment-component').show();
    commentModal.find('div.btn-add-comment').hide();
}

function updateChartAnnotation(reportId) {
    $.ajax({
        url: reportResultLatestCommentUrl,
        data: {'reportId': reportId}
    })
        .done(function (result) {
            result = encodeToHTML(result);
            var chartObj = $(".highcharts-container").parent().highcharts();
            chartObj.removeAnnotation('current');
            if (result != null && result.length > 0) {
                var annotationXValue = 0.9 * (chartObj.chartWidth);
                var annotationYValue = 0.1 * (chartObj.chartHeight);
                var labelsObj = {};
                var point = {};
                point.x = annotationXValue;
                point.y = annotationYValue;
                labelsObj.text = result;
                labelsObj.point = point;
                var styleObj = {};
                styleObj.width = 200;
                labelsObj.style = styleObj;
                var labels = [];
                labels.push(labelsObj);
                var labelOptions = {};
                labelOptions.backgroundColor = 'rgba(236,236,236,1)';
                labelOptions.borderWidth = 0;
                labelOptions.borderRadius = 40;
                var annotationObj = {};
                annotationObj.labelOptions = labelOptions;
                annotationObj.id = 'current';
                annotationObj.labels = labels;
                annotationObj.draggable = 'xy';
                chartObj.addAnnotation(annotationObj);
            }
        });
}