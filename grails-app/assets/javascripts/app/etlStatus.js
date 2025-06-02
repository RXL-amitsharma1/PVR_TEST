$(function () {

    //Put the pre-mart etl status. It will periodically poll the pre mart etl status.
    //The polling interval will be cleared when the pre mart etl status is not running.
    var preMartEtlStatusTimer = setInterval(function () {
        if (typeof window.preMartEtlStatus === "undefined" || window.preMartEtlStatus == 'RUNNING') {
            poll_pre_mart_etl_status();
        } else {
            clearInterval(preMartEtlStatusTimer);
        }
    }, refreshInterval);

    //Put the pre-mart etl status. It will periodically poll the affiliate mart etl status.
    //The polling interval will be cleared when the affiliate mart etl status is not running.
    var affEtlStatusTimer = setInterval(function () {
        if (typeof window.affEtlStatus === "undefined" || window.affEtlStatus == 'RUNNING') {
            poll_aff_mart_etl_status();
        } else {
            clearInterval(affEtlStatusTimer);
        }
    }, refreshInterval);

    //Put the etl status. It will periodically poll the etl status.
    //The polling interval will be cleared when the etl status is not running.
    var etlStatusTimer = setInterval(function () {
        if (typeof window.etlStatus === "undefined" || window.etlStatus == 'RUNNING') {
            poll_etl_status();
        } else {
            clearInterval(etlStatusTimer);
        }
    }, refreshInterval);

    var runOrPausedEtlBtn = setInterval(function () {
        if (typeof window.etlStatus !== "undefined" && typeof window.affEtlStatus !== "undefined" && (window.etlStatus == 'RUNNING' || window.affEtlStatus == 'RUNNING')) {
            // $(".runOrPauseBtn").html('<a href="#" class="runOrPauseBtn" data-toggle="modal" data-target="#pauseEtlModal"><i class="md md-pause" title="<g:message code="pause.initial.etl"/>"></i></a>');
            // $(".resumeBtn").css("display", "none");
        } else if (typeof window.etlStatus !== "undefined" && typeof window.affEtlStatus !== "undefined" && (window.etlStatus == 'SUCCESS' && window.affEtlStatus == 'SUCCESS')) {
            $(".runOrPauseBtn").html('<a href="#" class="runOrPauseBtn" data-toggle="modal" data-target="#initialEtlModal"><i class="md md-play" title="<g:message code="run.initial.etl"/>"></i></a>');
            // $(".resumeBtn").css("display", "block");
        } else {
            clearInterval(runOrPausedEtlBtn);
        }
    }, refreshInterval);

    pauseEtlJustification();

});

function poll_pre_mart_etl_status() {
    $.ajax({
        type: "GET",
        url: preMartEtlStatusUrl,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.isPreMartStatusApplicable == true) {
                var preMartEtlStatus = result.preMartEtlStatus
                window.preMartEtlStatus = preMartEtlStatus;
                var preMartEtlStatusValue = result.preMartEtlStatusValue;
                var html = '';
                switch (preMartEtlStatus) {
                    case 'SUCCESS':
                        html = '<span class="label label-success">' + preMartEtlStatusValue + '</span>';
                        break;
                    case 'RUNNING':
                        html = '<span class="label label-primary">' + preMartEtlStatusValue + '</span>';
                        break;
                    case 'FAILED':
                        html = '<span class="label label-danger">' + preMartEtlStatusValue + '</span>';
                        break;
                    default:
                        html = '<span>' + $.i18n._("etl.execution.no.status") + '</span>';
                        break;
                }
            } else {
                html = '<span class="label label-default">' + $.i18n._("app.label.not.applicable") + '</span>'
            }
            $('.preMartEtlStatus').html(html);
        });
}

function poll_aff_mart_etl_status() {
    $.ajax({
        type: "GET",
        url: affEtlStatusUrl,
        dataType: 'json'
    })
        .done(function (result) {
            if (result.isAffEtlStatusApplicable == true) {
                var affEtlStatus = result.affEtlStatus
                window.affEtlStatus = affEtlStatus;
                var affEtlStatusValue = result.affEtlStatusValue;
                var html = '';
                var runOrPauseIcon = '';
                switch (affEtlStatus) {
                    case 'SUCCESS':
                        html = '<span class="label label-success">' + affEtlStatusValue + '</span>';
                        // runOrPauseIcon = '<i class="md md-play" title="' + $.i18n._("run.initial.etl") + '" />';
                        // $(".runOrPauseBtn").attr("data-target", "#initialEtlModal");
                        break;
                    case 'RUNNING':
                        html = '<span class="label label-primary">' + affEtlStatusValue + '</span>';
                        // runOrPauseIcon = '<i class="md md-pause" title="' + $.i18n._("pause.initial.etl") + '" />';
                        // $(".runOrPauseBtn").attr("data-target", "#pauseEtlModal");
                        // $(".resumeBtn").css("display", "none");
                        break;
                    case 'FAILED':
                        html = '<span class="label label-danger">' + affEtlStatusValue + '</span>';
                        //runOrPauseIcon = '<i class="md md-play" title="' + $.i18n._("run.initial.etl") + '" />';
                        // $(".runOrPauseBtn").attr("data-target", "#initialEtlModal");
                        break;
                    // case 'ETL_PAUSED':
                    //     html = '<span class="label label-primary">' + affEtlStatusValue+ '</span>';
                    //     runOrPauseIcon = '<i class="md md-play" title="'+ $.i18n._("run.initial.etl") +'" />';
                    //     $(".runOrPauseBtn").attr("data-target", "#initialEtlModal");
                    //     break;
                    default:
                        html = '<span>' + $.i18n._("etl.execution.no.status") + '</span>';
                        break;
                }
            }
            $('.affEtlStatus').html(html);
            $(".runOrPauseBtn").html(runOrPauseIcon);
        });
}

function poll_etl_status() {
    $.ajax({
        type: "GET",
        url: etlStatusUrl,
        dataType: 'json'
    })
        .done(function (result) {
            var status = result.status
            window.etlStatus = status;
            var statusValue = result.statusValue;
            var html = '';
            var runOrPauseIcon = '';
            switch (status) {
                case 'SUCCESS':
                    html = '<span class="label label-success">' + statusValue + '</span>';
                    // runOrPauseIcon = '<i class="md md-play" title="' + $.i18n._("run.initial.etl") + '" />';
                    // $(".runOrPauseBtn").attr("data-target", "#initialEtlModal");
                    break;
                case 'RUNNING':
                    html = '<span class="label label-primary">' + statusValue + '</span>';
                    // runOrPauseIcon = '<i class="md md-pause" title="' + $.i18n._("pause.initial.etl") + '" />';
                    // $(".runOrPauseBtn").attr("data-target", "#pauseEtlModal");
                    // $(".resumeBtn").css("display", "none");
                    break;
                case 'FAILED':
                    html = '<span class="label label-danger">' + statusValue + '</span>';
                    //runOrPauseIcon = '<i class="md md-play" title="' + $.i18n._("run.initial.etl") + '" />';
                    // $(".runOrPauseBtn").attr("data-target", "#initialEtlModal");
                    break;
                // case 'ETL_PAUSED':
                //     html = '<span class="label label-primary">' + statusValue + '</span>';
                //     runOrPauseIcon = '<i class="md md-play" title="'+ $.i18n._("run.initial.etl") +'" />';
                //     $(".runOrPauseBtn").attr("data-target", "#initialEtlModal");
                //     break;
                default:
                    html = '<span>' + $.i18n._("etl.execution.no.status") + '</span>';
                    break;

            }
            $('.etlStatus').html(html);
            $(".runOrPauseBtn").html(runOrPauseIcon);
        });
}

function pauseEtlJustification() {
    var modal = $("#pauseEtlModal");
    $('#pauseEtlErrorDiv').hide();
    $("#pausedBtn").on("click", function () {
        if (!$('#pauseJustification').val().trim()) {
            $('#pauseEtlErrorDiv').show();
        } else {
            $('#pauseEtlErrorDiv').hide();
            var pauseJustification = $('#pauseJustification').val()
            $.ajax({
                type: "POST",
                data: {pauseJustification: pauseJustification},
                url: pauseEtlUrl,
                dataType: 'html'
            })
                .done(function (data) {
                    $('.alert-success').hide();
                    $(".pauseETLSuccess").show();
                    $('#pauseJustification').val("")
                    $('#message').html($.i18n._('etl.paused.success.message'));
                })
                .fail(function (e) {
                    $('.alert-success').hide();
                    $('.alert-danger').show();
                    $('#pauseEtlErrorDiv').hide();
                    $('#pauseJustification').val("")
                    $('#errormessage').html($.i18n._('etl.request.to.paused.failed'));
                });
            modal.modal("hide");
        }
    });
}