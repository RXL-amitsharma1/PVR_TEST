<div class="grid-stack-item-content rx-widget panel">

    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="app/dataTablesActionButtons.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/commonGeneratedReportsActions.js"/>
    <asset:javascript src="app/standardJustification.js"/>
    <asset:stylesheet src="executionStatus.css"/>
    <asset:stylesheet src="copyPasteModal.css"/>

    <asset:javascript src="datatables/extendedDataTable.js"/>
    <asset:stylesheet src="datatables/extendedDataTable.css"/>


    <g:javascript>
        var icsrCaseTrackingListUrl = "${createLink(controller: 'icsrCaseTrackingRest', action: 'index', params: [caseNumber: caseNumber, versionNumber: versionNumber, exIcsrProfileId: exIcsrProfileId, exIcsrTemplateQueryId: exIcsrTemplateQueryId])}";
        var generatePDF= "${createLink(controller: "report", action: "downloadPdf")}";
        var generateCioms = "${createLink(controller: "report", action: "drillDown")}";
        var generateBatchXML = "${createLink(controller: 'icsrCaseTrackingRest', action: 'downloadBatchXML')}";
        var generateBulkXML = "${createLink(controller: 'icsrCaseTrackingRest', action: 'downloadBulkXML')}";
        var APP_ASSETS_PATH='${request.contextPath}/assets/';
        var markAsSubmittedUrl= "${createLink(controller: "icsrProfileConfiguration", action: "loadIcsrReportSubmissionForm")}";
        var bulkMarkAsSubmittedURL= "${createLink(controller: "icsrProfileConfiguration", action: "loadBulkIcsrReportSubmissionForm")}";
        var caseSubmitUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "submitIscrCase")}";
        var icsrProfileViewUrl = "${createLink(controller: "executedIcsrProfile", action: "view")}";
        var submissionHistoryCase= "${createLink(controller: 'icsrCaseTrackingRest', action: 'caseHistory')}";
        var transmitCase = "${createLink(controller: 'icsrCaseTrackingRest', action: 'transmitCase')}";
        var showIcsrReportUrl = "${createLink(controller: 'icsr', action: 'showReport')}";
        var caseErrorDetails= "${createLink(controller: 'icsrCaseTrackingRest', action: 'getErrorDetails')}";
        var caseHistoryData= "${createLink(controller: 'icsrCaseTrackingRest', action: 'caseAllReceipentHistory')}";
        var viewDownloadPage = "${createLink(controller: "report", action: "showFirstSection")}";
        var listE2BStatuses = "${createLink(controller: "icsrCaseTrackingRest", action: "listE2BStatuses")}";
        var listProfiles = "${createLink(controller: "icsrProfileConfiguration", action: "listProfiles")}";
        var bulkTransmissionUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "bulkTransmitCases")}";
        var bulkCaseSubmitUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "bulkSubmitIscrCase")}";
        var generateCaseData = "${createLink(controller: "icsr", action: "generateCaseData")}";
        var generatedCaseDataScheduled = "${createLink(controller: "icsr", action: "generatedCaseDataScheduled")}";
        var deleteCaseUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "deleteCase")}";
        var reportSubmitUrl="${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}";
        var saveLocalCp = "${createLink(controller: "icsrCaseTrackingRest", action: "saveLocalCp")}";
        var checkFileExistUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'checkFileExist')}";
        var downloadAckFileUrl = "${createLink(controller: "icsrCaseTrackingRest", action: 'downloadAckFile')}";
        var standardJustificationsUrl = "${createLink(controller: "icsrCaseTrackingRest", action: "listStandardJustification")}";
        var periodicReportConfig = {
            generateDraftUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "generateDraft")}",
            markAsSubmittedUrl: "${createLink(controller: "reportSubmission", action: "loadReportSubmissionForm")}",
            reportSubmitUrl: "${createLink(controller: "reportSubmission", action: "submitReport")}${_csrf ? ("?" + _csrf?.parameterName + "=" + _csrf?.token) : ""}",
            viewCasesUrl: "${createLink(controller: "caseList", action: "index")}",
            reportsListUrl: "${createLink(controller: "periodicReportConfigurationRest", action: "reportsList")}",
            targetStatesAndApplicationsUrl: "${createLink(controller: "periodicReport", action: "targetStatesAndApplications")}",
            updateReportStateUrl: "${createLink(controller: "periodicReport", action: "updateReportState")}",
            reportViewUrl: "${createLink(controller: "pvp", action: "sections")}",
            configurationViewUrl: "${createLink(controller: "periodicReport", action: "viewExecutedConfig")}",
            reportingDestinationsUrl: "${createLink(controller: 'queryRest', action: 'getReportingDestinations')}",
            stateListUrl: "${createLink(controller: 'workflowJustificationRest', action: 'getStateListAdhoc')}",
            downloadPublisherFileURL: "${createLink(controller: 'pvp', action: 'downloadPublisherReport', absolute: true)}",
        }
    </g:javascript>
    %{--    <asset:javascript src="app/icsrCaseTracking/icsr-case-tracking.js"/>--}%
    <asset:javascript src="app/configuration/deliveryOption.js"/>

    <div id="container${index}" class="row rx-widget-content nicescroll pv-caselist">
        <table id="rxTableIcsrCaseTracking${index}" class="table table-striped pv-list-table dataTable no-footer"
               width="100%">
            <thead>
            <tr>
                <th data-id="caseNumber" data-type="text"><g:message code="icsr.case.tracking.case.number.label"/></th>
                <th style="text-align: center" data-id="versionNumber" data-type="number"><g:message
                        code="icsr.case.tracking.version"/></th>
                <th data-id="recipient" data-type="text"><g:message code="icsr.case.tracking.recipient"/></th>
                <th data-id="profileName" data-type="text"><g:message code="icsr.case.tracking.profile"/></th>
                <th data-id="reportForm" data-type="disabled"><g:message code="icsr.case.tracking.reportForm"/></th>
                <th class="col-min-120 text-center" data-id="dueDate" data-type="date"><g:message code="icsr.case.tracking.dueDate"/></th>
                <th class="col-min-110 text-center" data-id="scheduledDate" data-type="date"><g:message code="icsr.case.tracking.scheduledDate"/></th>
                <th class="col-min-110 text-center" data-id="generationDate" data-type="date"><g:message code="icsr.case.tracking.generationDate"/></th>
                <th class="col-min-110 text-center" data-id="submissionDate" data-type="date"><g:message code="icsr.case.tracking.submissionDate"/></th>
                <th style="text-align: center"data-id="e2BStatus" data-type="text"><g:message code="icsr.case.tracking.state"/></th>
                <th class="col-min-100" style="text-align: center"><g:message code="app.label.action"/></th>
            </tr>
            </thead>
        </table>
    </div>

    <g:form controller="icsrProfileConfiguration" method="delete">
        <g:render template="/includes/widgets/deleteRecord"/>
    </g:form>



    <script>
        var IcsrCaseStateEnum = Object.freeze({
            "ERROR": 'ERROR',
            "COMMIT_REJECTED": 'COMMIT_REJECTED',
            "PARSER_REJECTED": 'PARSER_REJECTED',
            "ACKNOWLEDGED": 'ACKNOWLEDGED',
            "COMMIT_ACCEPTED": 'COMMIT_ACCEPTED',
            "SCHEDULED": 'SCHEDULED',
            "MANUAL": 'MANUAL',
            "SUBMISSION_NOT_REQUIRED": 'SUBMISSION_NOT_REQUIRED',
            "COMMIT_RECEIVED": 'COMMIT_RECEIVED',
            "TRANSMITTING_ATTACHMENT": 'TRANSMITTING_ATTACHMENT',
            "TRANSMITTED_ATTACHMENT": 'TRANSMITTED_ATTACHMENT'
        });

        $(function () {
            var tableFilter = {};
            var advancedFilter = false;
            var searchString = {};

            $("#exportUrl${index}").on("click", function () {
                var url = $(".exportUrl").attr("data-content", location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '') + "/reports/icsrProfileConfiguration/viewCases?frame=true")
                $("#exportUrlField").val($(this).attr("data-content"));
                $("#exportUrlModal").modal("show");
            });

            $(document).on("click", ".localCp", function () {
                var confirmationModal = $("#confirmationModal");
                var url = $(this).attr("data-url");
                confirmationModal.modal("show");
                confirmationModal.find('.modalHeader').html($.i18n._('icsr.confirmation'));
                confirmationModal.find('.confirmationMessage').html($.i18n._('icsr.confirmation.msg'));
                confirmationModal.find('.okButton').html($.i18n._('continue'));
                confirmationModal.find('.okButton').removeClass("btn btn-danger").addClass("btn btn-primary");
                confirmationModal.find('.okButton').off().on('click', function () {
                    $.ajax({
                        type: 'POST',
                        url: url,
                        async: true,
                        dataType: 'json'
                    })
                        .done(function (response) {
                            $("#confirmationModal").modal("hide");
                            successNotificationForIcsrCaseTracking(response.message);
                        })
                        .fail(function (err) {
                            errorNotificationForIcsrCaseTracking((err.responseJSON.message ? err.responseJSON.message : "") +
                                (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                        });
                });
            });

            var tableShowNumber${index} = 10;

            var qualityIssuesTable;
            var loadData = function () {
                qualityIssuesTable = initTable();
            };

            function initTable() {
                var rrtable = $('#rxTableIcsrCaseTracking${index}').ExtendedDataTable({
                    "layout": {
                        topStart: null,
                        topEnd: {search: {placeholder: 'Search'}},
                        bottomStart: ['pageLength', 'info', {
                            paging: {
                                type: 'full_numbers'
                            }
                        }],
                        bottomEnd: null,
                    },
                    language: { search: ''},
                    "stateDuration": -1,
                    "bAutoWidth": false,
                    "stateSave": false,
                    "customProcessing": true, //handled using processing.dt event
                    "serverSide": true,
                    "iDisplayLength": tableShowNumber${index},
                    "bLengthChange": true,
                    "aLengthMenu": [[10, 25, 50, 100], [10, 25, 50, 100]],
                    "pagination": true,
                    "pagingType": "full_numbers",
                    drawCallback: function (settings) {
                        pageDictionary($('#rxTableIcsrCaseTracking${index}_wrapper')[0], settings.aLengthMenu[0][0], settings.json.recordsFiltered);
                        hideLoader();
                    },
                    "ajax": {
                        "url": "${createLink(controller: 'icsrCaseTrackingRest', action: 'index')}",
                        "dataSrc": "data",
                        "data": function (d) {
                            tableShowNumber${index} = d.length;
                            d.icsrWidgets = true;
                        }
                    },
                    initComplete: function () {
                        $($("#rxTableIcsrCaseTracking${index}_wrapper")).find(">:first-child").css({ "display": "none"});
                        submission_modal_after_load();
                        delete_case_modal_after_load();
                    },
                    "columnDefs": [
                        {"orderable": false, "targets": [4, 9, 10]},
                    ],
                    "order": {
                        data: 'dueDate',
                        dir: 'desc'
                    },
                    "searching": false,
                    "columns": [
                        {
                            "mData": "caseNumber",
                            stackId: 1,
                            inlineFilter: true,
                            mRender: function (data, type, row) {
                                var link = '';
                                if (row.showReportLink == true) {
                                    link = '<a href="' + generateCioms + '?caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '" target="_blank">' + encodeToHTML(data) + '</a>';
                                } else {
                                    link = encodeToHTML(data);
                                }
                                return link
                            }
                        },
                        {
                            "mData": "versionNumber",
                            stackId: 1,
                            inlineFilter: {
                                type: 'number'
                            },
                            "sClass": "dt-center",
                            mRender: function (data, type, row) {
                                var link = '';
                                if (row.showReportLink == true) {
                                    link = '<a href="#" class="caseDetail" data-toggle="modal" data-case-number="' + row.caseNumber + '" data-version-number="' + row.versionNumber + '"' + '>' + encodeToHTML(data) + '</a>';
                                } else {
                                    link = encodeToHTML(data);
                                }
                                return link;
                            }
                        },
                        {
                            "mData": "recipient",
                            inlineFilter: true,
                            "sClass":"dt-center",
                            mRender: function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                colElement += encodeToHTML(data);
                                colElement += '<a tabindex="0" title="' + encodeToHTML(data) + '" class="ico-dots view-all"><i class="md md-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            }
                        },
                        {
                            "mData": "profileName",
                            stackId: 2,
                            inlineFilter: true,
                            mRender: function (data, type, row) {
                                var profileName = '';
                                var title = '';
                                if (row.showReportLink == true) {
                                    profileName = '<a href="' + icsrProfileViewUrl + '?id=' + row.exIcsrProfileId + '" target="_blank">' + encodeToHTML(data) + '</a>';
                                } else {
                                    profileName = encodeToHTML(data);
                                }
                                title = encodeToHTML(data);
                                if (row.queryName) {
                                    profileName = profileName + " (" + row.queryName + ")";
                                    title = title + " (" + row.queryName + ")";
                                }
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                colElement += profileName;
                                colElement += '<a tabindex="0" title="' + title + '" class="ico-dots view-all"><i class="md md-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            }
                        },
                        {
                            "mData": "reportForm",
                            stackId: 2,
                            "sClass": "col-min-100",
                            "mRender": function (data, type, row) {
                                var link = '';
                                if (row.showReportLink == true && row.isGenerated == true) {
                                    if (~[IcsrCaseStateEnum.SCHEDULED, IcsrCaseStateEnum.MANUAL].indexOf(row.e2BStatus)) {
                                        link = '<a href="' + showIcsrReportUrl + '?isInDraftMode=true&exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' + row.processedReportId + '" target="_blank">' + encodeToHTML(data) + '</a>';
                                    } else {
                                        link = '<a href="' + showIcsrReportUrl + '?exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' + row.processedReportId + '" target="_blank">' + encodeToHTML(data) + '</a>';
                                    }
                                } else {
                                    link = encodeToHTML(data);
                                }
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                var allData = data ? data : "";
                                colElement += link;
                                colElement += '<a tabindex="0" title="' + allData + '" class="ico-dots view-all"><i class="md md-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            }
                        },
                        {
                            "mData": "dueDate",
                            inlineFilter: {
                                type: 'date-range'
                            },
                            "sClass": "dt-center",
                            mRender: function (data, type, row) {
                                var clazz = "";
                                if (row.indicator == "red") {
                                    clazz = 'class="label-danger" style="padding: 2px; font-size:12px; padding: 1px 10px!important; border-radius: 2em!important;"';
                                } else if (row.indicator == "yellow") {
                                    clazz = 'class="label-primary" style="padding: 2px; font-size:12px; padding: 1px 10px!important; border-radius: 2em!important;"';
                                } else {
                                    clazz = 'class="label-normal" style="padding: 2px; font-size:12px; padding: 1px 10px!important; border-radius: 2em!important;"';
                                }
                                if (data) {
                                    return '<span ' + clazz + '>' + moment(data).format(DEFAULT_DATE_DISPLAY_FORMAT) + "</span>";
                                }
                                return ""
                            }
                        },
                        {
                            "mData": "scheduledDate",
                            stackId: 3,
                            inlineFilter: {
                                type: 'date-range'
                            },
                            "sClass":"col-min-100",
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                var allData = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                                colElement += allData;
                                colElement += '<a tabindex="0" title="' + allData + '" class="ico-dots view-all"><i class="md md-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            }
                        },
                        {
                            "mData": "generationDate",
                            stackId: 3,
                            inlineFilter: {
                                type: 'date-range'
                            },
                            "sClass":"col-min-100 dt-center",
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                var allData = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                                colElement += allData;
                                colElement += '<a tabindex="0" title="' + allData + '" class="ico-dots view-all"><i class="md md-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            }
                        },
                        {
                            "mData": "submissionDate",
                            inlineFilter: {
                                type: 'date-range'
                            },
                            "sClass":"col-min-110 dt-center",
                            "mRender": function (data, type, row) {
                                var colElement = '<div class="col-container"><div class="col-height commentDisplay">';
                                var allData = data ? moment.utc(data).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) : "";
                                colElement += allData;
                                colElement += '<a tabindex="0" title="' + allData + '" class="ico-dots view-all"><i class="md md-dots-horizontal font-20 blue-1"> </i></a>';
                                colElement += '</div></div>';
                                return colElement;
                            }
                        },
                        {
                            "mData": "e2BStatus",
                            "sClass": "dt-center",
                            mRender: function (data, type, row) {
                                var dataShow = '';
                                if (data) {
                                    var msg = data;
                                    if (data == IcsrCaseStateEnum.COMMIT_RECEIVED) {
                                        dataShow = ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' + $.i18n._('icsr.case.tracking.status.' + msg) + ' and Attachment Not started">' + $.i18n._('icsr.case.tracking.status.' + msg) + ' <i class="fa fa-clock-o" aria-hidden="true"></i></button>';
                                    } else if (data == IcsrCaseStateEnum.TRANSMITTING_ATTACHMENT) {
                                        dataShow = ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' + $.i18n._('icsr.case.tracking.status.' + msg) + ' and Attachment In Progress">' + $.i18n._('icsr.case.tracking.status.' + msg) + ' <i class="fa fa-spinner" aria-hidden="true"></i></button>';
                                    } else if (data == IcsrCaseStateEnum.TRANSMITTED_ATTACHMENT) {
                                        dataShow = ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' + $.i18n._('icsr.case.tracking.status.' + msg) + ' and Attachment Completed">' + $.i18n._('icsr.case.tracking.status.' + msg) + ' <i class="fa fa-paperclip" aria-hidden="true"></i></button>';
                                    } else {
                                        dataShow = ' <div style="white-space: nowrap;"><button class="btn btn-default btn-xs stateHistory statusButton" href="#" style="width: 100px;" title="' + $.i18n._('icsr.case.tracking.status.' + msg) + '">' + $.i18n._('icsr.case.tracking.status.' + msg) + '</button>';
                                    }
                                    if (~[IcsrCaseStateEnum.ERROR, IcsrCaseStateEnum.COMMIT_REJECTED, IcsrCaseStateEnum.PARSER_REJECTED].indexOf(msg)) {
                                        dataShow = dataShow + ' <a class="errorInfo" style="cursor: pointer; margin-right: -15px;">' + '<i class="fa fa-exclamation-circle es-error fa-sm"></i> </a>';
                                    }
                                    dataShow = dataShow + '</div>'

                                }
                                return dataShow;
                            }
                        },
                        {
                            "mData": null,
                            "sClass": "dataTableColumnCenter",
                            "aTargets": ["id"],
                            "mRender": function (data, type, row) {
                                var actionButton = '';
                                if (row.showReportLink == true) {
                                    var link = showIcsrReportUrl + '?exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' + row.processedReportId;
                                    ;
                                    var label = 'view';
                                    if (~[IcsrCaseStateEnum.SCHEDULED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED].indexOf(row.e2BStatus)) {
                                        if (row.report == true) {
                                            link = showIcsrReportUrl + '?isInDraftMode=true&exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&processReportId=' + row.processedReportId;
                                            ;
                                        } else if (row.isGenerated == false && ~[IcsrCaseStateEnum.SCHEDULED].indexOf(row.e2BStatus)) {
                                            link = generatedCaseDataScheduled + '?exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&status=' + row.e2BStatus;
                                        } else if (row.isGenerated == false) {
                                            link = generateCaseData + '?exIcsrTemplateQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '&status=' + row.e2BStatus;
                                        }
                                        label = 'generateCaseData';
                                    }
                                    actionButton = '<div class="btn-group dropdown dataTableHideCellContent" align="center"> \
                        <a class="btn btn-success btn-xs" target="_blank" href="' + link + '">' + $.i18n._(label) + '</a> \
                        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
                            <span class="caret"></span> \
                            <span class="sr-only">Toggle Dropdown</span> \
                        </button> \
                        <ul class="dropdown-menu dropdown-menu-right" role="menu" style="min-width: 80px !important; font-size: 12px;">';
                                    if (~[IcsrCaseStateEnum.SCHEDULED].indexOf(row.e2BStatus)) {
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-target="#reportSubmissionModal" data-url="' + markAsSubmittedUrl + "?noSubmisson=true&icsrTempQueryId=" + row.exIcsrTemplateQueryId + "&caseNumber=" + row.caseNumber + "&versionNumber=" + row.versionNumber + '">' + $.i18n._('labelNotSubmit') + '</li>';
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-target="#deleteCaseModal" data-url="' + deleteCaseUrl + "?exTempQueryId=" + row.exIcsrTemplateQueryId + "&caseNumber=" + row.caseNumber + "&versionNumber=" + row.versionNumber + '">' + $.i18n._('labelDelete') + '</a></li>';
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" class="localCp" data-url="' + saveLocalCp + '?exTempQueryId=' + row.exIcsrTemplateQueryId + '&caseId=' + row.caseId + '&versionNumber=' + row.versionNumber + "&caseNumber=" + row.caseNumber + '&prodHashCode=' + row.prodHashCode + '&profileId=' + row.profileId + '">' + $.i18n._('local.cp') + '</a></li>';
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" class="localCp" data-url="' + saveLocalCp + '?exTempQueryId=' + row.exIcsrTemplateQueryId + '&caseId=' + row.caseId + '&versionNumber=' + row.versionNumber + "&caseNumber=" + row.caseNumber + '&prodHashCode=' + row.prodHashCode + ' &profileId=' + row.profileId + '">' + $.i18n._('generate.report') + '</a></li>';
                                    } else {
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-target="#transmitJustification" data-url="' + transmitCase + '?icsrTempQueryId=' + row.exIcsrTemplateQueryId + '&caseNumber=' + row.caseNumber + '&versionNumber=' + row.versionNumber + '">' + $.i18n._('transmitCase') + '</a></li>';
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-target="#reportSubmissionModal" data-url="' + markAsSubmittedUrl + "?icsrTempQueryId=" + row.exIcsrTemplateQueryId + "&caseNumber=" + row.caseNumber + "&versionNumber=" + row.versionNumber + '">' + $.i18n._('labelSubmit') + '</li>';
                                        actionButton = actionButton + '<li role="presentation"><a role="menuitem" href="#" data-toggle="modal" data-target="#emailToModal" data-ex-icsr-template-query-id="' + row.exIcsrTemplateQueryId + '" data-case-number= "' + row.caseNumber + '" data-version-number= "' + row.versionNumber + '">' + $.i18n._('labelEmailTo') + '</a></li>';
                                    }

                                    actionButton = actionButton + '</ul> \
                            </div>';
                                }
                                return actionButton;
                            }
                        }
                    ]
                });
                var Id = "#rxTableIcsrCaseTracking${index}";
                loadTableOption(Id);
                actionButton(Id);
                $('#rxTableIcsrCaseTracking${index} tbody').on('click', 'tr td button.stateHistory', function () {
                    openStateHistoryModal(rrtable.row($(this).closest('tr')).data());
                }).on('click', 'tr td a.errorInfo', function () {
                    openErrorDetailsModal(rrtable.row($(this).closest('tr')).data());
                }).on('click', 'tr td a.caseDetail', function () {
                    openCaseDetailsModal(rrtable.row($(this).closest('tr')).data());
                });
                return rrtable
            };

            $('#refresh-widget${index}').hide();
            loadData();

            function openStateHistoryModal(rowData) {
                var caseNumber = rowData.caseNumber;
                var versionNumber = rowData.versionNumber;
                var profileName = rowData.profileName;
                var exIcsrTemplateQueryId = rowData.exIcsrTemplateQueryId;
                var recipientName = rowData.recipient;
                var followupNumber = rowData.followupNumber;
                var localReportMessage = rowData.localReportMessage;
                $.ajax({
                    type: "GET",
                    url: submissionHistoryCase,
                    data: {
                        caseNumber: caseNumber,
                        versionNumber: versionNumber,
                        profileName: profileName,
                        exIcsrTemplateQueryId: exIcsrTemplateQueryId
                    },
                    dataType: 'json'
                })
                    .done(function (result) {
                        $("#caseSubmissionData").html('');
                        $("#submissionHistoryCase").modal("show");
                        $("#submissionHistoryCase span#caseNumber").html(caseNumber);
                        $("#submissionHistoryCase span#versionNumber").html(versionNumber);
                        $("#submissionHistoryCase span#recipientName").html(recipientName);
                        $("#submissionHistoryCase span#followupNumber").html(followupNumber);
                        if(localReportMessage == null){
                            $("#submissionHistoryCase span#localReportMessage").html("N/A");
                        } else {
                            $("#submissionHistoryCase span#localReportMessage").html(localReportMessage);
                        }
                        if ($.isEmptyObject(result)) {
                            var tr = '<tr><td colspan="4" style="text-align: center" class="padding">No data found.</td></tr>';
                            $('#submissionHistoryCaseTable').append(tr);
                            $('.alert-danger').hide(tr)
                        } else {
                            $.map(result, function (v, k) {
                                var tr = '<tr>';
                                tr = tr + '<td class="padding">' + $.i18n._('icsr.case.tracking.status.' + v.e2BStatus);
                                if (v.ackFileName != null) {
                                    tr = tr + '&nbsp;' + '<a id="checkFileExist" data-ackFileName="' + v.ackFileName + '"><i class="fa fa-file fa-sm" title="' + v.ackFileName + '"></i></a>';
                                }
                                tr = tr + '</td>';
                                if (v.statusDate != null) {
                                    tr = tr + '<td class="padding">' + moment.utc(v.statusDate).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                if (v.lastUpdatedBy != null) {
                                    tr = tr + '<td class="padding">' + v.lastUpdatedBy + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                if (v.lastUpdateDate != null) {
                                    tr = tr + '<td class="padding">' + moment.utc(v.lastUpdateDate).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                tr = tr + '</tr>';
                                $('#submissionHistoryCaseTable').append(tr);
                                $('.alert-danger').hide(tr)
                            })
                        }
                    })
                    .fail(function (err) {
                        var responseText = err.responseText;
                        var responseTextObj = JSON.parse(responseText);
                        if (responseTextObj.message) {
                            return;
                        }
                    });
            };
            $(document).on("click", "#checkFileExist", function () {
                var fileName = $(this).attr("data-ackFileName");
                $.ajax({
                    url: checkFileExistUrl + '?ackFileName=' + fileName,
                    async: true,
                    dataType: 'html'
                })
                    .done(function (data) {
                        window.location.href = downloadAckFileUrl + "?ackFileName=" + fileName
                    })
                    .fail(function (err) {
                        errorNotification(err.responseJSON.message ? err.responseJSON.message : "");
                    });
            });

            function errorNotification(message) {
                $(".alert-danger").alert('close');
                if (message != undefined && message != "")
                    $(".modal-body").prepend(
                        '<div class="alert alert-danger alert-dismissable">' +
                        '<button type="button" class="close" ' +
                        'data-dismiss="alert" aria-hidden="true">' +
                        '&times;' +
                        '</button>' +
                        message +
                        '</div>'
                    );
            }

            function openErrorDetailsModal(rowData) {
                var caseNumber = rowData.caseNumber;
                var versionNumber = rowData.versionNumber;
                var profileName = rowData.profileName;
                var exIcsrTemplateQueryId = rowData.exIcsrTemplateQueryId;
                var ackFileName = rowData.ackFileName;
                var e2BStatus = rowData.e2BStatus;
                $.ajax({
                    type: "POST",
                    url: caseErrorDetails,
                    data: {
                        caseNumber: caseNumber,
                        versionNumber: versionNumber,
                        profileName: profileName,
                        exIcsrTemplateQueryId: exIcsrTemplateQueryId,
                        status: e2BStatus
                    },
                    dataType: 'html'
                })
                    .done(function (result) {
                        $("#errorDetailsData").html('');
                        $("#errorDetails").modal("show");
                        $("#errorDetailCaseNumber").text(caseNumber);
                        $("#errorDetailVersionNumber").text(versionNumber);
                        $("#errorAckFileName").text(ackFileName);
                        if ($.isEmptyObject(result)) {
                            var tr = '<tr><td colspan="5" style="text-align: center" class="padding">No data found.</td></tr>';
                            $('#errorDetailsTable').append(tr);
                        } else {

                            var tr = '<tr>';
                            if (result != null) {
                                tr = tr + '<td class="padding">' + result + '</td>';
                            } else {
                                tr = tr + '<td class="padding">No data found.</td>';
                            }
                            $('#errorDetailsTable').append(tr);

                        }
                    })
                    .fail(function (err) {
                        var responseText = err.responseText;
                        var responseTextObj = JSON.parse(responseText);
                        if (responseTextObj.message) {
                            return;
                        }
                    });
            };

            function openCaseDetailsModal(rowData) {
                var caseNumber = rowData.caseNumber;
                var versionNumber = rowData.versionNumber;
                var profileName = rowData.profileName;
                $.ajax({
                    type: "GET",
                    url: caseHistoryData,
                    data: {caseNumber: caseNumber, versionNumber: versionNumber},
                    dataType: 'json'
                })
                    .done(function (result) {
                        $("#caseHistoryData").html('');
                        $("#caseHistoryDetails").modal("show");
                        $("#caseDetailsCaseNumber").text(caseNumber);
                        $("#caseDetailsVersionNumber").text(versionNumber);
                        if ($.isEmptyObject(result)) {
                            var tr = '<tr><td colspan="5" style="text-align: center" class="padding">No data found.</td></tr>';
                            $('#caseHistoryTable').append(tr);
                        } else {
                            $.map(result, function (v, k) {
                                var tr = '<tr>';
                                if (v.reportDestination != null) {
                                    tr = tr + '<td class="padding">' + v.reportDestination + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                tr = tr + '<td class="padding">' + $.i18n._('icsr.case.tracking.status.' + v.e2BStatus);
                                if (v.ackFileName != null) {
                                    tr = tr + ' <i class="fa fa-file fa-sm" title="' + v.ackFileName + '"></i>';
                                }
                                tr = tr + '</td>';
                                if (v.statusDate != null) {
                                    tr = tr + '<td class="padding">' + moment.utc(v.statusDate).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                if (v.lastUpdatedBy != null) {
                                    tr = tr + '<td class="padding">' + v.lastUpdatedBy + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                if (v.lastUpdateDate != null) {
                                    tr = tr + '<td class="padding">' + moment.utc(v.lastUpdateDate).tz(userTimeZone).format(DEFAULT_DATE_TIME_DISPLAY_FORMAT) + '</td>';
                                } else {
                                    tr = tr + '<td class="padding"></td>';
                                }
                                tr = tr + '</tr>';
                                $('#caseHistoryTable').append(tr);
                            })
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
        });

        var delete_case_modal_after_load = function () {
            var deleteCaseModal = $("#deleteCaseModal");
            deleteCaseModal.on('shown.bs.modal', function (e) {
                var rowObj = $(e.relatedTarget);
                var deleteCaseUrl = $(e.relatedTarget).data('url');
                //showModalLoader(deleteCaseModal);
                var instanceType = $.i18n._($.i18n._('app.configurationType.CASE'));
                deleteCaseModal.find(".btn").removeAttr("disabled", "disabled");

                deleteCaseModal.find('#deleteCaseModalLabel').text("");
                deleteCaseModal.find('#deleteCaseModalLabel').text($.i18n._('modal.delete.title', instanceType));

                var nameToDeleteLabel = $.i18n._('deleteThis', instanceType);
                deleteCaseModal.find('#nameToDelete').text("");
                deleteCaseModal.find('#nameToDelete').text(nameToDeleteLabel);

                deleteCaseModal.find('.description').empty();
                deleteCaseModal.find('.description').text("CaseNumber");
                deleteCaseModal.find("#deleteCaseButton").off().on("click", function () {
                    var confirmation = $("#deleteCaseJustification").val();
                    if (confirmation != "" && confirmation.trim().length > 0) {
                        //modal.modal("hide");
                        showLoader();
                        $.ajax({
                            type: "GET",
                            url: deleteCaseUrl,
                            data: {justification: confirmation},
                            dataType: 'json'
                        })
                            .done(function (result) {
                                successNotificationForIcsrCaseTracking(result.message);
                                var dataTable = $("#rxTableIcsrCaseTracking${index}").DataTable();
                                dataTable.ajax.reload(function () {
                                    dataTable.row(rowObj).remove();
                                });
                                deleteCaseModal.modal("hide");
                                hideLoader();
                            })
                            .fail(function (err) {
                                errorNotificationForIcsrCaseTracking((err.responseJSON.message ? err.responseJSON.message : "") +
                                    (err.responseJSON.stackTrace ? "<br>" + err.responseJSON.stackTrace : ""));
                                hideLoader();
                                deleteCaseModal.modal("hide");
                                window.scrollTo(0, 0);
                            });
                    } else {
                        $('#deleteCaseDlgErrorDiv').show();
                    }
                });
            }).on("hidden.bs.modal", function () {
                $('#deleteCaseDlgErrorDiv').hide();
                $("#deleteCaseJustification").val("");

            });
            $(document).trigger("loadStandardJustifications", ['ICSRMarkReportDeletion']);
        };

        var submission_modal_after_load = function () {
            var reportSubmissionModal = $("#reportSubmissionModal");
            reportSubmissionModal.on('shown.bs.modal', function (e) {
                var rowObj = $(e.relatedTarget);
                var rowId = rowObj.closest('tr').attr('id');
                showModalLoader(reportSubmissionModal);
                reportSubmissionModal.find(".modal-content:first").load(rowObj.data('url'), function () {
                    hideModalLoader(reportSubmissionModal);
                    // buildReportingDestinationsSelectBox(reportSubmissionModal.find("[name=reportingDestinations]"), periodicReportConfig.reportingDestinationsUrl, reportSubmissionModal.find("input[name='primaryReportingDestination']"), false);
                    var submissionDate = reportSubmissionModal.find('.datepicker input[name="submissionDate"]').val();
                    $('#submissionDateDiv').datepicker({
                        allowPastDates: true,
                        date: submissionDate,
                        twoDigitYearProtection: true,
                        culture: userLocale,
                        momentConfig: {
                            format: DEFAULT_DATE_DISPLAY_FORMAT
                        }
                    });
                    var dueDate = reportSubmissionModal.find('.datepicker input[name="dueDate"]').val();
                    $('#dueDateDiv').datepicker({
                        allowPastDates: true,
                        date: dueDate,
                        twoDigitYearProtection: true,
                        culture: userLocale,
                        momentConfig: {
                            format: DEFAULT_DATE_DISPLAY_FORMAT
                        }
                    });
                    reportSubmissionModal.find('button.submit-draft').off().on('click', function () {
                        var data = new FormData();
                        var form_data = reportSubmissionModal.find('form').serializeArray();
                        $.each(form_data, function (key, input) {
                            data.append(input.name, input.value);
                        });
                        data.append('password', reportSubmissionModal.find("#password-input").val());
                        var file_data = $('#file_input').get(0).files;
                        for (var i = 0; i < file_data.length; i++) {
                            data.append("file", file_data[i]);
                        }
                        console.log("Data : " + data);
                        $.ajax({
                            url: caseSubmitUrl,
                            method: 'POST',
                            data: data,
                            processData: false,
                            contentType: false,
                            mimeType: "multipart/form-data",
                            dataType: 'html'
                        })
                            .done(function (result) {
                                reportSubmissionModal.modal('hide');
                                window.location.reload();
                                // reloadData(rowId);
                                // successNotificationForIcsrCaseTracking(result.message);
                            })
                            .fail(function (err) {
                                var responseText = err.responseText;
                                var responseTextObj = JSON.parse(responseText);
                                if (responseTextObj.message != undefined) {
                                    $("#submitErrorMessage").parent().removeClass('hide');
                                    $("#submitErrorMessage").html(responseTextObj.message.replace(/,/g, "<br/>"));
                                } else {
                                    $("#submitErrorMessage").parent().removeClass('hide');
                                    $("#submitErrorMessage").html("Failed due to some reason!");
                                }
                            });
                    });
                });
            }).on("hidden.bs.modal", function () {
                reportSubmissionModal.find(".modal-content:first").html('');
                $("#submitErrorMessage").parent().addClass('hide');
                $("#submitErrorMessage").html('');

            });
            var transmissionModal = $('#transmitJustification');
            transmissionModal.on('shown.bs.modal', function (e) {
                var transmitUrl = $(e.relatedTarget).data('url');
                var rowId = $(e.relatedTarget).parents('tr').attr('id');
                transmissionModal.find('#transmitButton').off().on('click', function () {
                    var parameters = transmissionModal.find("#justificationForm").serialize();
                    parameters += "&password=" + encodeURIComponent(transmissionModal.find("#password-input").val());
                    $.ajax({
                        url: transmitUrl,
                        type: 'post',
                        data: parameters,
                        dataType: 'json',
                        beforeSend: function () {
                            transmissionModal.find("#transmitButton").attr('disabled', 'disabled');
                            showModalLoader(transmissionModal);
                        }
                    })
                        .done(function (data) {
                            transmissionModal.modal('hide');
                            reloadIcsrTrackingWidgetData(rowId);
                            successNotificationForIcsrCaseTracking(data.message);
                        })
                        .fail(function (err) {
                            var responseText = err.responseText;
                            var responseTextObj = JSON.parse(responseText);
                            if (responseTextObj.message != undefined) {
                                $("#transmitErrorMessage").parent().removeClass('hide');
                                $("#transmitErrorMessage").html(responseTextObj.message);
                            } else {
                                $("#transmitErrorMessage").parent().removeClass('hide');
                                $("#transmitErrorMessage").html("Failed due to some reason!");
                            }
                        })
                        .always(function () {
                            transmissionModal.find("#transmitButton").removeAttr('disabled');
                            hideModalLoader(transmissionModal);
                        });

                });
            }).on("hidden.bs.modal", function () {
                transmissionModal.find("#password-input").val('');
                $("#transmitErrorMessage").parent().addClass('hide');
                $("#transmitErrorMessage").html('');
                transmissionModal.find("#comments").val('');
                transmissionModal.find('#transmitButton').off();

            });

            $('#emailToModal').on('show.bs.modal', function (e) {
                var rowObj = $(e.relatedTarget);
                $('form[name="emailForm"] #exIcsrTemplateQueryId').val(rowObj.data('ex-icsr-template-query-id'));
                $('form[name="emailForm"] #caseNumber').val(rowObj.data('case-number'));
                $('form[name="emailForm"] #versionNumber').val(rowObj.data('version-number'));
                $('#emailUsers').val(null).trigger("change");
                $('#emailUsers').parent().removeClass('has-error');
                $('#formatError').hide();
                $('#emailUsers').parents('div.modal-body').find('.alert-danger').addClass('hide');

                // clear checkbox for attachemnt formats
                $('.emailOption').prop("checked", false);
                emailToModalShow = true;
            }).on('hide.bs.modal', function (e) {
                emailToModalShow = false;
                $('form[name="emailForm"] #exIcsrTemplateQueryId').val('');
                $('form[name="emailForm"] #caseNumber').val('');
                $('form[name="emailForm"] #versionNumber').val('');
            })

        };

        var reloadIcsrTrackingWidgetData = function (rowId, resetPagination) {
            if (resetPagination != true) {
                resetPagination = false
            }
            var dataTable = $("#rxTableIcsrCaseTracking${index}").DataTable();
            dataTable.ajax.reload(function () {
                highlightSelectedRow(rowId);
            }, resetPagination);
        };


        var highlightSelectedRow = function (rowId) {
            if (rowId != undefined && rowId != "") {
                var dataTable = $("#rxTableIcsrCaseTracking${index}").DataTable();
                dataTable.row('#' + rowId).nodes()
                    .to$()
                    .addClass('flash-row');
            }
        };
    </script>
</div>
