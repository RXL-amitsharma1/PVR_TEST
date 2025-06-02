<%@ page import="com.rxlogix.enums.PageSizeEnum; com.rxlogix.enums.SensitivityLabelEnum; net.sf.dynamicreports.report.constant.PageType; net.sf.dynamicreports.report.constant.PageOrientation; com.rxlogix.enums.PageOrientationEnum; com.rxlogix.enums.ReportFormatEnum; com.rxlogix.util.ViewHelper;" %>

    <!-- Modal -->
    <div class="modal fade" id="saveAsModal" tabindex="-1" role="dialog" aria-labelledby="saveAsModalLabel"
         aria-hidden="true">

        <g:form controller="report" action="advancedOptionsExport" method="post">

            <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title" id="saveAsModalLabel"><g:message code="app.label.saveAs" /></h4>
                </div>

                <div class="modal-body" style="padding-left: 40px; padding-right: 40px">

                    <g:hiddenField name="isInDraftMode" value="${params.boolean("isInDraftMode")}"/>
                    <g:hiddenField name="actionToExecute" value="${params.action}"/>
                    <g:hiddenField name="id" value="${params.id}"/>

                    %{--Identify operation as coming from Advanced Options to turn the cache off so these settings can take effect--}%
                    <g:hiddenField name="advancedOptions" value="1"/>

                    <div class="row">
                        <div class="col-md-6">
                            <label class="dialogBox"><g:message code="file.format" /></label>

                            <div class="form-group">
                                <div class="radio radio-primary">
                                    <g:radio id="${ReportFormatEnum.PDF}" name="outputFormat" value="${ReportFormatEnum.PDF}" checked="true"
                                             disabled="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.PDF, reportType)}"/>
                                    <label for="${ReportFormatEnum.PDF}">
                                        <g:message code="save.as.pdf" />
                                    </label>
                                </div>
                                <div class="radio radio-primary">
                                    <g:radio id="${ReportFormatEnum.XLSX}" name="outputFormat" value="${ReportFormatEnum.XLSX}"
                                             disabled="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.XLSX, reportType)}"/>
                                    <label for="${ReportFormatEnum.XLSX}">
                                        <g:message code="save.as.microsoft.excel" />
                                    </label>
                                </div>
                                <div class="radio radio-primary">
                                    <g:radio id="${ReportFormatEnum.DOCX}" name="outputFormat" value="${ReportFormatEnum.DOCX}"
                                             disabled="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.DOCX, reportType)}"/>
                                    <label for="${ReportFormatEnum.DOCX}">
                                        <g:message code="save.as.microsoft.word" />
                                    </label>
                                </div>
                                <div class="radio radio-primary">
                                    <g:radio id="${ReportFormatEnum.PPTX}" name="outputFormat" value="${ReportFormatEnum.PPTX}"
                                             disabled="${ViewHelper.isNotExportable(executedConfigurationInstance, executedTemplateInstance, ReportFormatEnum.PPTX, reportType)}"/>
                                    <label for="${ReportFormatEnum.PPTX}">
                                        <g:message code="save.as.microsoft.powerpoint" />
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label class="dialogBox"><g:message code="page.orientation.pdf.and.word" /></label>

                            <div class="form-group">
                                <div class="radio radio-primary">
                                    <g:radio id="${PageOrientation.PORTRAIT}" name="pageOrientation" value="${PageOrientation.PORTRAIT}"/>
                                    <label for="${PageOrientation.PORTRAIT}">
                                        <g:message code="app.pageOrientation.PORTRAIT" />
                                    </label>
                                </div>

                                <div class="radio radio-primary">
                                    <g:radio id="${PageOrientation.LANDSCAPE}" name="pageOrientation" value="${PageOrientation.LANDSCAPE}"
                                             checked="true"/>
                                    <label for="${PageOrientation.LANDSCAPE}">
                                        <g:message code="app.pageOrientation.LANDSCAPE" />
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="horizontalRuleFull"></div>

                    <div class="row">
                        <div class="col-md-6">
                            <label class="dialogBox"><g:message code="options" /></label>

                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="showPageNumbering" checked="true"/>
                                    <label for="showPageNumbering">
                                        <g:message code="show.page.numbering" />
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="showCompanyLogo" checked="true"/>
                                    <label for="showCompanyLogo">
                                        <g:message code="show.company.logo" />
                                    </label>
                                </div>
                            </div>
                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="appendDrillDown"/>
                                    <label for="appendDrillDown">
                                        Append Drill Down Data
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label class="dialogBox"></label>
                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="excludeCriteriaSheet"/>
                                    <label for="excludeCriteriaSheet">
                                        <g:message code="exclude.criteria.sheet" />
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="excludeAppendix"/>
                                    <label for="excludeAppendix">
                                        <g:message code="exclude.appendix" />
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="excludeComments"/>
                                    <label for="excludeComments">
                                        <g:message code="exclude.comments" />
                                    </label>
                                </div>
                            </div>

                            <div class="form-group">
                                <div class="checkbox checkbox-primary">
                                    <g:checkBox name="excludeLegend"/>
                                    <label for="excludeLegend">
                                        <g:message code="exclude.legend" />
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <label class="dialogBox"><g:message code="paper.size.pdf.and.word" /></label>

                            <div class="form-group">
                                <g:select name="paperSize"
                                          from="${ViewHelper.getPageSizeEnum()}"
                                          optionKey="name"
                                          optionValue="display"
                                          class="form-control"/>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <label class="dialogBox"><g:message code="sensitivity.label" /></label>

                            <div class="form-group">
                                <g:select name="sensitivityLabel"
                                          from="${ViewHelper.getSensitivityLabelEnum()}"
                                          optionKey="name"
                                          optionValue="display"
                                          class="form-control"/>
                            </div>
                        </div>
                    </div>
                    <div class="row" id="sectionSelect" style="display: none">
                        <div class="col-md-12">
                            <label class="dialogBox"><g:message code="app.label.reportSections" /></label>

                            <div class="form-group">
                                <select id="sectionsToExport" name="sectionsToExport" class="form-control" multiple>
                                    <g:if test="${reportResult}">
                                        <g:set var="executedConfigurationInstance" value="${reportResult.executedTemplateQuery.executedConfiguration}"/>
                                        <g:each var="section" in="${executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus()}">
                                            <option selected value="${section.id}">
                                                <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}" executedTemplateQuery="${section}" />
                                            </option>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <g:each var="section" in="${executedConfigurationInstance.fetchExecutedTemplateQueriesByCompletedStatus()}">
                                            <option selected value="${section.id}">
                                                <g:renderDynamicReportName executedConfiguration="${executedConfigurationInstance}" executedTemplateQuery="${section}" />
                                            </option>
                                        </g:each>
                                    </g:else>
                                </select>

                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">

                    <g:if test="${grailsApplication.config.oneDrive.enabled}">
                        <button type="button" class="btn pv-btn-grey downloadUrl uploadToOneDriveModalShow"
                                data-url="${createLink(controller: "report", action: 'advancedOptionsExport', absolute: true)}" data-reportName="${executedConfigurationInstance.reportName}">
                            <span class="glyphicon glyphicon-ok icon-white"></span>
                            <g:message code="app.label.oneDrive.uploadToOneDrive"/>
                        </button>
                    </g:if>

                    <button type="submit" id="saveAsButton" class="btn btn-primary">
                        <span class="glyphicon glyphicon-ok icon-white"></span>
                        ${message(code: 'default.button.save.label', default: 'Save')}
                    </button>
                    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
        </g:form>
    </div><!-- /.modal -->
<g:hiddenField name="ciomsITemplateId" id="ciomsITemplateId" value="${ciomsITemplateId}"/>

<script type="text/javascript">
    $(function () {
        var ciomsId = $('#ciomsITemplateId').val();
        var xlsxOutput = $('#XLSX');

        $(document).on("click", '.uploadToOneDriveModalShow', function () {
            var $this = $(this);
            $this.attr("data-postParams", $this.closest("form").serialize());
            $this.attr("data-uploadOnly", "true");
            $this.attr("data-name", $this.attr("data-reportName") + "." + $this.closest("form").find("input[name='outputFormat']:checked").val());
        });

        $('#saveAsButton').on('click', function () {
            $('#saveAsModal').modal('hide');
        });

        $("#sectionsToExport").select2({
            templateSelection: function (label) {
                return '<span title="' + encodeToHTML(label.text) + '">' +  encodeToHTML(label.text) + '</span>';
            },
            escapeMarkup: function (m) {
                return m;
            },
        });

        if (ciomsId) {
            $("#sectionsToExport").on('select2-selecting', function (eventData) {
                if (eventData.choice.id == ciomsId) {
                    xlsxOutput.prop('disabled', true);
                }
            });
            $("#sectionsToExport").on('select2-removing', function (eventData) {
                if (eventData.choice.id == ciomsId) {
                    xlsxOutput.prop('disabled', false);
                }
            });
        }

        $("#saveAsModal").on("show.bs.modal", function (e) {
            $('#saveAsModal').find('form')[0].reset();
            $("#sectionsToExport").trigger("change");
            var action = $(e.relatedTarget).data('action');
            if (action) {
                $("input[name='actionToExecute']").val(action);
            }
            if ($("input[name='actionToExecute']").val() === "viewMultiTemplateReport") {
                $("#sectionsToExport").prop("disabled", false);
                $("#sectionSelect").show();
            } else {
                $("#sectionsToExport").prop("disabled", true);
                $("#sectionSelect").hide();
            }
            var id = $(e.relatedTarget).data('id');
            if (id) {
                $("input[name='id']").val(id);
            }

            if (ciomsId) {
                xlsxOutput.prop('disabled', true);
            }
        });
    });
</script>
