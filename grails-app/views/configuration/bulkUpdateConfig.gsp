<%@ page import="grails.util.Holders; com.rxlogix.user.User; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.user.UserGroup; com.rxlogix.Constants; com.rxlogix.config.Configuration; com.rxlogix.util.FilterUtil; com.rxlogix.enums.PeriodicReportTypeEnum;com.rxlogix.enums.DateRangeEnum;com.rxlogix.util.DateUtil; com.rxlogix.config.ReportConfiguration" %>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.PeriodicReport.bulkScheduling.title"/></title>
    <asset:javascript src="vendorUi/shorten/jquery.shorten.js"/>
    <asset:javascript src="vendorUi/handlebar/handlebars-v4.7.8.js"/>
    <asset:javascript src="app/utils/pvr-common-util.js"/>
    <asset:javascript src="app/utils/pvr-filter-util.js"/>
    <asset:javascript src="app/hbs-templates/datepicker.precompiler.js"/>
    <asset:javascript src="app/hbs-templates/filter_panel.precompiler.js"/>
    <asset:javascript src="app/scheduler.js"/>
    <asset:javascript src="app/configuration/bulkUpdateConfig.js"/>
    <asset:javascript src="app/configuration/periodicReport.js"/>
    <asset:stylesheet src="copyPasteModal.css"/>
    <asset:stylesheet src="expandingTextarea.css"/>
    <asset:javascript src="app/emailModal.js"/>
    <asset:javascript src="app/configuration/deliveryOption.js"/>
    <asset:javascript src="app/configuration/copyPasteValues.js"/>
    <asset:javascript src="app/configuration/emailConfiguration.js"/>
    <asset:javascript src="app/pushNotification.js"/>

    <g:javascript>
        var emailAddUrl = "${createLink(controller: 'email', action: 'axajAdd')}";
        var listUrl="${createLink(controller: 'configurationRest', action: 'bulkSchedulingList')}";
        var editFieldUrl="${createLink(controller: 'configuration', action: 'editField')}";
        var selectTemplatesUrl = "${createLink(controller: 'configuration', action: 'listTemplates')}";
        var CONFIGURATION = {
             listUrl: "${createLink(controller: 'periodicReportConfigurationRest', action: 'index')}",
             deleteUrl: "${createLink(controller: 'configuration', action: 'ajaxDelete')}",
             editUrl: "${createLink(controller: 'configuration', action: 'edit')}",
             viewUrl: "${createLink(controller: 'configuration', action: 'view')}",
             copyUrl: "${createLink(controller: 'configuration', action: 'ajaxCopy')}",
             runUrl:"${createLink(controller: 'configuration', action: 'ajaxRun')}",
             unscheduleUrl:"${createLink(controller: 'configurationRest', action: 'unschedule')}"
        };
        var periodicReportTypes= JSON.parse("${FilterUtil.buildEnumOptions(PeriodicReportTypeEnum)}")
        var dateRanges= JSON.parse("${FilterUtil.buildEnumOptions(DateRangeEnum)}")
        var statuses = [{key: "SCHEDULED", value: "${message(code: 'app.label.scheduled')}"}, {key: "UNSCHEDULED", value: "${message(code: 'app.keyword.NOT')} ${message(code: 'app.label.scheduled')}"}]
    </g:javascript>
    <style>
    table.datepicker-calendar-days tbody th,
    table.datepicker-calendar-days thead tr th,
    table.datepicker-calendar-days thead td,
    table.datepicker-calendar-days tbody tr td {
        padding: 0 !important;
    }
    div.dt-container .dt-layout-row .topControls {
        width: 80%;
    }
    .bulk-update-templates-box .select2-container {
        max-width: 200px;
    }
    </style>
</head>

<body>
<g:set var="userService" bean="userService"/>
<g:render template="/includes/layout/flashErrorsDivs"/>
<rx:container title="${message(code: "app.menu.bulkImportConfiguration")}" options="${true}" filterButton="${true}">


    <div class="topControls" style="float: right;display: none">
        <div class="input-group add-margin" style="width: 250px; float: left">
        <span class="bulk-update-templates-box"><select id="templates" class="form-control" data-placeholder="${message(code:'app.menu.createFromTemplate')}"></select></span>
        <div class="input-group-btn"     style="text-align: left;">
            <button class="btn btn-primary   create" disabled><i class="fa fa-forward"></i></button>
        </div>
        </div>
          <form id="excelImportForm" name="excelImportForm" action="${createLink(controller: 'configuration', action: 'importBulkExcel')}" enctype="multipart/form-data" method="post" style="float: left; width: 300px">
                <div class="input-group add-margin pull-right" style="float: left">
                    <input type="text" class="form-control" id="file_name" readonly>
                    <label class="input-group-btn ">
                        <span class="btn btn-primary inputbtn-height">
                            <i class="fa fa-upload"></i>
                            <input type="file" id="file_input" name="file" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                   style="display: none;">
                        </span>
                    </label>
                </div>
            </form>
        <form id="excelExportForm" action="${createLink(controller: 'configuration', action: 'exportToExcel')}" style="float: left">
            <input name="filter" type="hidden">
            <button class="btn btn-primary export" id="exportBulkConfig" style="height: 25px"><i class="fa fa-download" title="${message(code:'app.label.exportTo')} ${message(code:'app.reportFormat.XLSX')}"></i> </button>
        </form>
        <span class="glyphicon glyphicon-question-sign modal-link " style="cursor:pointer;font-size: 20px;margin-left: 10px;" data-toggle="modal"
              data-target="#importHelpModal"></span>
    </div>
    <div class="pv-caselist">
    <table id="rxTableBulkSheduling" class="table table-striped pv-list-table dataTable no-footer" width="100%">
        <thead>
        <tr>
            <th></th>
            <th><g:message code="app.label.reportName"/></th>
            <th><g:message code="app.PeriodicReport.configuration.template.label"/></th>
            <th><g:message code="app.widget.button.quality.product.label"/></th>
            <th><g:message code="app.label.scheduler"/></th>
            <th><g:message code="next.run.date"/></th>
            <th><g:message code="app.label.shared"/></th>
            <th><g:message code="app.label.emailTo"/></th>
            <th><g:message code="app.label.action"/></th>
        </tr>
        </thead>
    </table>
    </div>
</rx:container>

<div id="reportNameEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
    <input class="form-control newVal" maxlength="500">

    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
<g:render plugin="pv-dictionary" template="/plugin/dictionary/copyPasteModal"/>
<g:render template="/includes/widgets/deleteRecord"/>
<div id="showProductSelection" class="showDictionarySelection" style="display: none">
</div>
<input type="hidden" name="productSelection" id="productSelection"/>
<input type="hidden" name="productGroupSelection" id="productGroupSelection" data-hide-dictionary-group="${!Holders.config.pv.dictionary.group.enabled}"/>

<div id="schedulerEditDiv" class="popupBox" style="position: absolute; display: none">
    <div class="fuelux" id="schedulerDiv">
        <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>

        <g:render template="/configuration/schedulerTemplate" model="[adhoc:true]"/>
        <g:hiddenField name="schedulerTime" value="${com.rxlogix.util.RelativeDateConverter.getCurrentTimeWRTTimeZone(userService.getCurrentUser())}"/>
        <g:hiddenField name="scheduleDateJSON" value=""/>
        <input type="hidden" name="configSelectedTimeZone" id="configSelectedTimeZone" value="${userService.getCurrentUser()?.preference?.timeZone}"/>
        <input type="hidden" id="timezoneFromServer" name="timezone" value="${DateUtil.getTimezone(userService.getCurrentUser())}"/>

    </div>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="sharedWithEditDiv" class="popupBox" style="position: absolute; display: none; width: 20%">
    <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>
    <g:set var="userService" bean="userService"/>
    <g:set var="currentUser" value="${userService.currentUser}"/>
    <div class="row">
        <div class="col-xs-12">
            <script>
                sharedWithListUrl = "${createLink(controller: 'userRest', action: 'sharedWithList')}";
                sharedWithValuesUrl = "${createLink(controller: 'userRest', action: 'sharedWithValues')}";
                $(function () {
                    bindShareWith($('.sharedWithControlEdit'), sharedWithListUrl, sharedWithValuesUrl, "100%")
                });
            </script>
            <label><g:message code="shared.with"/></label>
            <input class="sharedWithControlEdit form-control" id="sharedWithEdit" name="sharedWith">
        </div>
    </div>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<div id="emailToEditDiv" class="popupBox" style="position: absolute; display: none; width: 22%">
    <g:hiddenField name="isEnabled" id="isEnabled" value="true"/>
    <label><g:message code="app.label.emailTo"/></label>
    <i class="fa fa-pencil-square-o copyPasteEmailButton"></i>
    <g:render template="/email/includes/copyPasteEmailModal"/>
    <g:select id="newEmailUsers"
              name="deliveryOption.emailToUsers"
              from="${[]}"
              class="form-control emailUsers" multiple="true"
              data-options-url="${createLink(controller: 'email', action: 'allEmails')}"/>
    <div style="margin-top: 10px; width: 100%; text-align: right;">
        <button class="btn btn-primary saveButton"><g:message code="default.button.save.label"/></button>
        <button class="btn btn-default cancelButton"><g:message code="default.button.cancel.label"/></button>
    </div>
</div>

<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>

<!-- Modal for queryLevelHelp -->
<div class="modal fade importHelpModal" id="importHelpModal" tabindex="-1" role="dialog" aria-labelledby="Import From Excel Help">
    <div class="modal-dialog modal-lg " role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <span><b><g:message code="app.bulkUpdate.help.title"/></b></span>
            </div>
            <div class="modal-body container-fluid">
                <div> <g:message code="app.bulkUpdate.help.text1" /></div><br>
                <div> <g:message code="app.bulkUpdate.help.text2" /></div><br>
                <div> <g:message code="app.bulkUpdate.help.text3" /></div><br>
                <div> <g:message code="app.bulkUpdate.help.text4" /></div><br>
                <div> <g:message code="app.bulkUpdate.help.text5" /></div><br>
                <div> <g:message code="app.bulkUpdate.help.text10" /></div><br>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default cancel" data-dismiss="modal"><g:message code="default.button.ok.label"/></button>
            </div>
        </div>
    </div>
</div>
</body>
