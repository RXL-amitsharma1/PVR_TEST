<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.enums.SensitivityLabelEnum; com.rxlogix.enums.PageSizeEnum; net.sf.dynamicreports.report.constant.PageOrientation" %>
<g:javascript>


</g:javascript>
<div class="modal fade" id="dmsConfiguration" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><g:message code="app.label.emailConfiguration.Compose" default="Compose"/></h4>
            </div>

            <div class="modal-body">
                <g:set var="dmsConfiguration" value="${configurationInstance?.dmsConfiguration}"/>

                <div class="row">
                    <div class="col-xs-12">
                        <g:message code="app.label.dms.folder"/>
                        <div>
                            <span id="place_0"></span>
                        </div>
                        <g:hiddenField name="dmsConfiguration.folder" value="${dmsConfiguration?.folder ?: ""}"  />
                        <g:hiddenField name="folderValue" value="${dmsConfiguration?.folder ?: ""}"  />
                    </div>
                </div>
                <div class="row">
                    <div class="col-xs-12">
                        <g:message code="app.label.dms.name"/>
                        <g:textField name="dmsConfiguration.name"
                                       value="${dmsConfiguration?.name?: ""}" class="form-control add-margin-bottom"/>
                        <g:hiddenField name="nameValue" value="${dmsConfiguration?.name ?: ""}" />
                    </div>
                </div>

                <div class="row">
                    <div class="col-xs-12">
                        <g:message code="app.label.dms.description"/>
                        <g:textArea name="dmsConfiguration.description"
                                       value="${dmsConfiguration?.description ?: ""}" class="form-control add-margin-bottom"/>
                        <g:hiddenField name="descriptionValue" value="${dmsConfiguration?.description ?: ""}"  />
                    </div>

                </div>
                <div class="row">
                    <div class="col-xs-12">
                        <g:message code="app.label.dms.tags"/>
                        <g:textField name="dmsConfiguration.tag"
                                       value="${dmsConfiguration?.tag ?: ""}" class="form-control add-margin-bottom"/>
                        <g:hiddenField value="${dmsConfiguration?.tag ?: ""}" name="tagValue" />
                    </div>
                </div>

                <div class="checkbox checkbox-primary">
                    <g:checkBox name="dmsConfiguration.noDocumentOnNoData" id="noDocumentOnNoData"  value="${dmsConfiguration?.noDocumentOnNoData? true:false}"/>
                    <label for="noDocumentOnNoData">
                        <g:hiddenField name="noDocumentOnNoDataValue" value="${dmsConfiguration?.noDocumentOnNoData? true:false}"/>
                        <g:message code="app.label.dms.do.not.send"
                                   default="Do not send when report returns no data"/>
                    </label>
                </div>
                <div id="advancedEmailOptions" style="padding-top: 25px">

                <div class="row">
                    <div class="col-md-6">
                        <label class="dialogBox"><g:message code="page.orientation.pdf.and.word" /></label>
                        <div class="form-group">
                            <select name="dmsConfiguration.pageOrientation" id="dmsConfiguration.pageOrientation" class="form-control">
                                <option value="${PageOrientation.PORTRAIT}"><g:message code="app.pageOrientation.PORTRAIT" /></option>
                                <option ${(dmsConfiguration?.pageOrientation==PageOrientation.LANDSCAPE)?"selected": "" } value="${PageOrientation.LANDSCAPE}"><g:message code="app.pageOrientation.LANDSCAPE" /></option>
                            </select>
                            <g:hiddenField name="pageOrientationValue" value="${dmsConfiguration?.pageOrientation?:PageOrientation.PORTRAIT}"/>
                        </div>

                        <label class="dialogBox"><g:message code="paper.size.pdf.and.word" /></label>
                        <div class="form-group">
                            <g:select name="dmsConfiguration.paperSize"
                                      from="${ViewHelper.getPageSizeEnum()}"
                                      optionKey="name"
                                      optionValue="display"
                                      value="${dmsConfiguration?.paperSize ?: PageSizeEnum.LETTER }"
                                      class="form-control"/>
                            <g:hiddenField name="paperSizeValue" value="${dmsConfiguration?.paperSize ?: PageSizeEnum.LETTER }"/>
                        </div>

                        <label class="dialogBox"><g:message code="sensitivity.label" /></label>
                        <div class="form-group">
                            <g:select name="dmsConfiguration.sensitivityLabel"
                                      from="${ViewHelper.getSensitivityLabelEnum()}"
                                      optionKey="name"
                                      optionValue="display"
                                      value="${dmsConfiguration?.sensitivityLabel ?: SensitivityLabelEnum.SENSITIVE }"
                                      class="form-control"/>
                            <g:hiddenField name="sensitivityLabelValue" value="${dmsConfiguration?.sensitivityLabel ?: SensitivityLabelEnum.SENSITIVE }"/>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <label class="dialogBox"><g:message code="options" /></label>
                        <div class="form-group">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="dmsConfiguration.showPageNumbering" checked="${dmsConfiguration? dmsConfiguration.showPageNumbering : "true" }"/>
                                <label for="dmsConfiguration.showPageNumbering">
                                    <g:message code="show.page.numbering" />
                                </label>
                                <g:hiddenField name="showPageNumberingValue" value="${dmsConfiguration? dmsConfiguration.showPageNumbering : "true" }"/>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="dmsConfiguration.showCompanyLogo" checked="${dmsConfiguration? dmsConfiguration.showCompanyLogo : "true" }"/>
                                <label for="dmsConfiguration.showCompanyLogo">
                                    <g:message code="show.company.logo" />
                                </label>
                                <g:hiddenField name="showCompanyLogoValue" value="${dmsConfiguration?.showCompanyLogo ?: "true"  }"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="dmsConfiguration.excludeCriteriaSheet" checked="${dmsConfiguration?.excludeCriteriaSheet ?: "false" }"/>
                                <label for="dmsConfiguration.excludeCriteriaSheet">
                                    <g:message code="exclude.criteria.sheet" />
                                </label>
                                <g:hiddenField name="excludeCriteriaSheetValue" value="${dmsConfiguration?.excludeCriteriaSheet ?: "false"  }"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="dmsConfiguration.excludeAppendix" checked="${dmsConfiguration?.excludeAppendix ?: "false" }"/>
                                <label for="dmsConfiguration.excludeAppendix">
                                    <g:message code="exclude.appendix" />
                                </label>
                                <g:hiddenField name="excludeAppendixValue" value="${dmsConfiguration?.excludeAppendix ?: "false"  }"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="dmsConfiguration.excludeComments" checked="${dmsConfiguration?.excludeComments ?: "false" }"/>
                                <label for="dmsConfiguration.excludeComments">
                                    <g:message code="exclude.comments" />
                                </label>
                                <g:hiddenField name="excludeCommentsValue" value="${dmsConfiguration?.excludeComments ?: "false"  }"/>
                            </div>
                        </div>

                        <div class="form-group noForCaseSeries">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="dmsConfiguration.excludeLegend" checked="${dmsConfiguration? dmsConfiguration.excludeLegend : "false" }"/>
                                <label for="dmsConfiguration.excludeLegend">
                                    <g:message code="exclude.legend" />
                                </label>
                                <g:hiddenField name="excludeLegendValue" value="${dmsConfiguration?.excludeLegend ?: "false"  }"/>
                            </div>
                        </div>
                    </div>
                </div>
                    <g:if test="${showSections}">
                    <div class="row" id="sectionSelect" style="display: block">
                        <div class="col-md-12">
                            <label class="dialogBox"><g:message code="app.label.reportSections" /></label>

                            <div class="form-group">
                                <select id="sectionsToExport" name="sectionsToExport" class="form-control" multiple>
                                        <g:each var="section" in="${configurationInstance.fetchExecutedTemplateQueriesByCompletedStatus()}">
                                            <option selected value="${section.id}">
                                                <g:renderDynamicReportName executedConfiguration="${configurationInstance}" executedTemplateQuery="${section}" />
                                            </option>
                                        </g:each>
                                </select>
                            </div>
                        </div>
                    </div>
                    </g:if>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" id="cancelDmsConfiguration">
                    <g:message code="default.button.cancel.label" />
                </button>
                <button type="button" class="btn btn-primary" id="saveDmsConfiguration"  data-dismiss="modal">
                    <g:message code="default.button.save.label"/>
                </button>
                <button type="button" class="btn pv-btn-grey" id="resetDmsConfiguration">
                    <g:message code="default.button.reset.label"/>
                </button>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    $(function () {
        $("#sectionsToExport").select2({
            templateSelection: function (label) {
                return '<span title="' + encodeToHTML(label.text) + '">' + encodeToHTML(label.text) + '</span>';
            }
        });
    });
</script>