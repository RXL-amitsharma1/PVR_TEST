<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.config.DataTabulationTemplate; com.rxlogix.config.TemplateQuery; com.rxlogix.config.TemplateQuery;com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeEnum" %>
<!-- Modal header -->
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <span class="modalHeader"><g:message code="app.label.add.section" /></span>
</div>
<asset:javascript src="app/configuration/blankParameters.js"/>
<!-- Modal body -->
<div class="modal-body adhocReport">
    <div class="alert alert-danger hide">
        <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        <span id="errorNotification"></span>
    </div>
    <g:form>
        <div class="templateQuery-div templateContainer">
            <div class="row">
                <div class="col-md-12">
                    <label><g:message code="app.label.chooseAReportTemplate"/><span
                            class="required-indicator">*</span></label>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div>
                        <g:select name="template.id"
                                  from="${[]}"
                                  class="form-control selectTemplate"/>
                        <g:hiddenField name="executedConfiguration.id" value="${executedConfiguration.id}"/>
                        <div class="requiredTemplate" hidden="hidden" style="color: #ff0000"><g:message code="app.reportTemplate.required.error"/> </div>
                    </div>
                </div>
            </div>
            <div class="row granularityDiv" style="display:none; padding-top: 5px;">
                <div class="col-md-2">
                    <label><g:message code="app.label.granularity"/></label>
                </div>
                <div class="col-md-4">
                    <g:select name="granularity" optionKey="name" optionValue="display" disabled="true"
                              from="${com.rxlogix.util.ViewHelper.granularity}" value ="${com.rxlogix.enums.GranularityEnum.MONTHLY}"
                              class="form-control select2-box granularitySelect"/>
                </div>
            </div>
            <div class="row templtReassessDateDiv" style="display: ${executedTemplateInstance?.showReassessDateDiv() ? 'block' : 'none'};
                 margin-top: 6px;">
                <div class="col-xs-4">
                    <label><g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/></label>
                </div>
                <div class="col-xs-3 fuelux datepicker input-group templtDatePicker ">
                    <g:textField name="templtReassessDate" class="form-control templtReassessDate"  value="${renderShortFormattedDate(date: new Date())}"/>
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>

            <div class="row">
                <div class="col-md-9">
                    <a class="add-cursor showHeaderFooterArea"><g:message
                            code="add.header.title.and.footer"/></a>
                </div>
                <div class="clearfix"></div>
            </div>

            <div class="row headerFooterArea" hidden="hidden">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-4">
                            <g:textField name="header"
                                         maxlength="${TemplateQuery.constrainedProperties.header.maxSize}"
                                         placeholder="Header"
                                         class="form-control"/>
                        </div>

                        <div class="col-md-4">
                            <g:textField name="title"
                                         maxlength="${Configuration.constrainedProperties.reportName.maxSize}"
                                         placeholder="Title"
                                         class="form-control"/>
                        </div>

                        <div class="col-md-4">
                            <g:footerSelect name="footer" class="form-control footerSelect"/>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="headerProductSelection"/>
                                <label for="headerProductSelection">
                                    <g:message code="templateQuery.headerProductSelection.label" />
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="headerDateRange"/>
                                <label for="headerDateRange">
                                    <g:message code="templateQuery.headerDateRange.label" />
                                </label>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="checkbox checkbox-primary">
                                <g:checkBox name="displayMedDraVersionNumber"/>
                                <label for="displayMedDraVersionNumber">
                                    <g:message code="templateQuery.displayMedDraVersionNumber.label" />
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="ciomsProtectedArea" hidden="hidden">
                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="privacyProtected"/>
                            <label for="privacyProtected">
                                <g:message code="templateQuery.privacyProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-12">
                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="blindProtected"/>
                            <label for="blindProtected">
                                <g:message code="templateQuery.blindProtected.label"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row" id="templateValuesContainer">
                %{--Space for Custom Sql--}%
            </div>
            <g:hiddenField class="validQueries" name="validQueries"/>

            <div class="row">
                <div class="col-md-12">
                    <label><g:message code="app.label.chooseAQuery"/></label>
                </div>
            </div>

            <div class="row queryContainer">
                <div class="col-lg-12">
                    <div class="doneLoading" style="padding-bottom: 5px;">
                        <g:select name="query.id"
                                  from="${[]}"
                                  class="form-control selectQuery"/>
                    </div>
                </div>
            </div>

            <div class="row" id="templateQueryValuesContainer">
            </div>

            <div class="col-md-12 showCustomReassess" style="display: ${executedTemplateInstance?.showQueryReassessDateDiv() ? 'block' : 'none'}; padding-left: 30px !important;">
                <div class="col-md-4">
                    <label><g:message code="app.label.reassessListedness"/> <g:message code="${ReassessListednessEnum.CUSTOM_START_DATE.getI18nKey()}"/></label>
                </div>
                <div class="col-md-3 fuelux datepicker input-group customDatePicker" style="right: 20px;">
                    <g:textField name="reassessListednessDate" class="form-control reassessDate" value="${renderShortFormattedDate(date: new Date())}"
                    />
                    <g:render template="/includes/widgets/datePickerTemplate"/>
                </div>
            </div>
            <div class="row">
                <div class="col-md-6">
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.queryLevel"/></label>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div>
                                <g:select name="queryLevel"
                                          from="${ViewHelper.getQueryLevels()}"
                                          valueMessagePrefix="app.queryLevel"
                                          class="form-control selectQueryLevel"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="row">
                        <div class="col-md-12">
                            <label><g:message code="app.label.DateRange"/></label>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div>
                                <g:select
                                        name="executedDateRangeInformationForTemplateQuery.dateRangeEnum"
                                        from="${ViewHelper.getDateRange(false,true)}"
                                        optionValue="display"
                                        optionKey="name"
                                        value="${DateRangeEnum.CUMULATIVE}"
                                        class="form-control dateRangeEnumClass"/>
                            </div>
                        </div>
                    </div>

                    <div class="col-xs-12">
                        <g:textField class="top-buffer form-control relativeDateRangeValue" name="executedDateRangeInformationForTemplateQuery.relativeDateRangeValue"
                                     placeholder="${message(code: 'enter.x.here')}"
                                     style="display: none; width: 50%;" value="1"
                        />
                        <div class="notValidNumberErrorMessage" hidden="hidden" style="color: #ff0000"> <g:message code="app.query.value.invalid.number" /> </div>

                        <div class="fuelux datePickerParentDiv">
                            <div class="datepicker" id="executedDatePickerFromDiv" style="display:none">
                                <div style="margin-top: 10px;">
                                    <g:message code="app.dateFilter.from"/>
                                </div>
                                <div class="input-group">
                                    <g:textField name="executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute"

                                                 placeholder="${message(code: 'app.label.startDate')}"
                                                 class="form-control"/>

                                    <g:render template="/includes/widgets/datePickerTemplate"/>
                                </div>
                            </div>

                            <div class="datepicker" id="executedDatePickerToDiv" style="display:none">
                                <div style="margin-top: 10px;">
                                    <g:message code="app.dateFilter.to" />
                                </div>
                                <div class="input-group">
                                    <g:textField name="executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute"
                                                 placeholder="${message(code: 'app.label.endDate')}"
                                                 class="form-control"/>

                                    <g:render template="/includes/widgets/datePickerTemplate" />
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </div>

        </div>
    </g:form>
    <div>

    </div>
</div>

<!-- Modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-primary submit-section"><g:message code="app.label.submit"/></button>
    <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message
            code="default.button.cancel.label"/></button>
</div>