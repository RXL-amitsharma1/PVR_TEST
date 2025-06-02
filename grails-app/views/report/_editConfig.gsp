
<body>

<asset:javascript src="app/tags.js"/>
<asset:javascript src="app/scheduler.js"/>
<asset:javascript src="app/configuration/deliveryOption.js"/>
<asset:javascript src="app/disableAutocomplete.js"/>

<div id="editConfigModal" class="modal fade" role="dialog">
    <div class="modal-dialog modal-lg" style="width: 80%" role="document">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.CONFIGURATION}">
                    <h4 class="modal-title"><g:message code="app.label.editReport"/></h4>
                </g:if>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.PERIODIC_REPORT_CONFIGURATION}">
                    <h4 class="modal-title"><g:message code="app.label.editReport"/></h4>
                </g:if>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.ICSR_REPORT_CONFIGURATION}">
                    <h4 class="modal-title"><g:message code="app.label.editReport"/></h4>
                </g:if>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.ICSR_PROFILE_CONFIGURATION}">
                    <h4 class="modal-title">${message(code: "default.edit.label", args: [entityName])}</h4>
                </g:if>
            </div>

            <div class="modal-body ${forbidden?"":"blue-bg"}">
                <g:if test="${!forbidden}">
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.CONFIGURATION}">
                    <g:render template="includes/editConfiguration" model="['isEditUsingModal':true, configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, configType: configType, id: id]" />
                </g:if>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.PERIODIC_REPORT_CONFIGURATION}">
                    <g:render template="includes/editPeriodic" model="['isEditUsingModal':true, configurationInstance: configurationInstance, sourceProfiles: sourceProfiles, configType: configType, id: id]" />
                </g:if>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.ICSR_REPORT_CONFIGURATION}">
                    <g:render template="includes/editIcsrReport"/>
                </g:if>
                <g:if test="${configType == com.rxlogix.config.ConfigTypes.ICSR_PROFILE_CONFIGURATION}">
                    <g:render template="includes/editIcsrProfile"/>
                </g:if>
                </g:if>
                <g:else>
                    <div class="alert alert-warning alert-dismissible forceLineWrap" role="alert">
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only">Close</span>
                        </button>
                        <i class="fa fa-warning"></i>
                        <g:message code="app.configuration.edit.permission" args="[configurationInstance.reportName]"/>
                    </div>
                    <div style="width: 100%; text-align: right">
                        <button type="button" class="btn pv-btn-grey" data-dismiss="modal">${message(code: "default.button.cancel.label")}</button>
                    </div>
                </g:else>
            </div>

        </div>
    </div>
</div>
<g:hiddenField name="reportId"  id="reportId" value="${id}"/>
</body>
<head>
    <style>
    .modal-open {
        overflow-y: scroll;
    }
    #editConfigModal .select2-container .indicator.primary {
        max-height: 20px;
    }
    #editConfigModal .select2-container .select2-selection__choice {
        max-width: 300px;
        white-space: break-spaces;
    }
    #editConfigModal .fuelux-date-picker-box .timezone-select-div ul.dropdown-menu {
        max-height: 300px;
    }
    </style>
    <title><g:message code="app.viewResult.title"/></title>
</head>
<g:if test="${!forbidden}">
<script>
    $(function () {
        var module = sessionStorage.getItem("module")
        if (module != "pvp") {
            $(".nonPvpRemove").remove();
        }
    });
    $("#cancelButton").on("click", function () {
        $("#editConfigModal").modal('hide');
        $('#editConfigErrorDiv').hide();
    });
    $('#productSearchIconLoading').remove();
    $('#productSearchIcon').show();
    $("#cancelButton").on("click", goToUrl());
    function goToUrl() {
        //overriding method so that user remains at the show screen
    }
    $contentHeader = $(".report-header-collapse");
    $(".report-content").addClass("rxmain-container-hide");
    $contentHeader.prepend($.parseHTML("<i class=\"fa fa-caret-down fa-lg click\" data-evt-clk='{\"method\": \"hideShowContent\", \"params\": []}'></i>"));
    $contentHeader = $(".report-lable-collapse");
    $contentHeader.addClass("click");
    $contentHeader.on('click', function () {hideShowContent(this)});
    $("#editConfigModal input[name=qbeForm]").parent().hide();
    $("#editConfigModal").on("shown.bs.modal", function (e) {
        showHideSpecialFields();
    });
    $("#editConfigModal").on("hidden.bs.modal", function (e) {
        if ($(e.target).attr("id") == 'editConfigModal') {
            $('#editConfigModal').find('form').trigger('reset');
        }
    });
</script>
</g:if>