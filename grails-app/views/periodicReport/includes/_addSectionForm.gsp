<%@ page import="com.rxlogix.config.Configuration; com.rxlogix.config.TemplateQuery; com.rxlogix.util.ViewHelper; com.rxlogix.enums.DateRangeEnum" %>
<!-- Modal header -->
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <span class="modalHeader"><g:message code="app.label.add.section" /></span>
</div>
<asset:javascript src="app/configuration/blankParameters.js"/>
<!-- Modal body -->
<div class="modal-body periodicReport">
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
            <div class="row">
                <div class="col-md-9">
                    <a class="add-cursor showHeaderFooterArea"><g:message
                            code="add.header.title.and.footer"/></a>
                </div>
                    <div class="col-md-3 pull-right">
                        <label class="no-bold add-cursor">
                            <g:checkBox name="draftOnly"/>
                            <span style="margin-left: 5px"><g:message code="app.label.draftOnly"/></span>
                        </label>
                    </div>
                <div class="clearfix"></div>
            </div>

            <div class="row headerFooterArea" hidden="hidden">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-4">
                            <g:textField name="header"
                                         maxlength="${TemplateQuery.constrainedProperties.header.maxSize}"
                                         placeholder="${message(code: "placeholder.templateQuery.header")}"
                                         class="form-control"/>
                        </div>

                        <div class="col-md-4">
                            <g:textField name="title"
                                         maxlength="${Configuration.constrainedProperties.reportName.maxSize}"
                                         placeholder="${message(code: "placeholder.templateQuery.title")}"
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
                                        from="${ViewHelper.getDateRange(true)}"
                                        optionValue="display"
                                        optionKey="name"
                                        value="${DateRangeEnum.CUMULATIVE}"

                                        class="form-control dateRangeEnumClass"/>
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
