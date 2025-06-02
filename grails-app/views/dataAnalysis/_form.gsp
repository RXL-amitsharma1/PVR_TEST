<%@ page import="com.rxlogix.util.ViewHelper; com.rxlogix.mapping.LmProductFamily; com.rxlogix.Constants" %>

<div class="rxmain-container rxmain-container-top">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-row rxmain-container-header">
            <i class="fa fa-caret-down fa-lg click" data-evt-clk='{"method": "hideShowContent", "params": []}'></i>
            <label class="rxmain-container-header-label click" data-evt-clk='{"method": "hideShowContent", "params": []}'>
                <g:message code="app.label.generateFile"/>
            </label>
        </div>

        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div class="col-md-4">
                    <div class="form-group">
                        <g:if test="${grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)}">
                            <g:set var="productFamilyLabel" value="${message(code: 'app.product.name', default: 'Product')}"/>
                        </g:if>
                        <g:else>
                            <g:set var="productFamilyLabel" value="${message(code: 'app.product.family', default: 'Product Family')}"/>
                        </g:else>
                        <label>
                            ${productFamilyLabel}
                            <span class="required-indicator">*</span>
                        </label>
                        <div>
                            <g:select id="productFamilyIdsSelect" name="productFamilyIdsSelect" from="${[]}"
                                      maxlength="${LmProductFamily.constrainedProperties.name.maxSize}"
                                      placeholder="${productFamilyLabel}"
                                      class="form-control"/>
                            <g:hiddenField name="productFamilyIds" value="" id="productFamilyIds"/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>
                            <g:message code="app.label.useCaseSeries" default="Use Cases"/>
                        </label>
                        <div>
                            <select name="caseSeriesId" id="caseSeriesId" value="" class="form-control"></select>
                        </div>
                    </div>

                </div>

                <div class="col-md-4">
                    <div class="fuelux">
                        <div class="form-group">
                            <label>
                                <g:message code="app.spotfire.report.period.start.date" default="Report Period Start Date"/><span
                                    class="required-indicator">*</span>
                            </label>
                            <div class="datepicker input-group" id="spotfireFromDate">
                                <g:textField name="fromDate" placeholder="Start Date" class="form-control"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>
                                <g:message code="app.spotfire.report.period.end.date" default="Report Period End Date"/><span
                                    class="required-indicator">*</span>
                            </label>

                            <div class="datepicker input-group" id="spotfireEndDate">
                                <g:textField name="endDate" placeholder="End Date" class="form-control"/>
                                <g:render template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="fuelux form-group">
                        <label>
                            <g:message code="app.asof.date" default="As of Date"/><span
                                class="required-indicator">*</span>
                        </label>

                        <div class="datepicker input-group" id="spotfireAsOfDate">
                            <g:textField name="asOfDate" placeholder="As of Date" class="form-control"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>
                            <g:message code="app.spotfire.file.type" default="File Type"/>
                        </label>
                        <div>
                            <g:if test="${grailsApplication.config.spotfire.fileType.drug.enabled}">
                                <div class="radio radio-primary radio-inline">
                                    <input type="radio" name="type" id="spotfireDrug" value="drug"/>
                                    <label for="spotfireDrug">
                                        <g:message code="app.spotfire.drug"/>
                                    </label>
                                </div>
                            </g:if>
                            <g:if test="${grailsApplication.config.spotfire.fileType.vaccine.enabled}">
                                <div class="radio radio-primary radio-inline">
                                    <input type="radio" name="type" id="spotfireVaccine" value="vacc"/>
                                    <label for="spotfireVaccine">
                                        <g:message code="app.spotfire.vaccine"/>
                                    </label>
                                </div>
                            </g:if>
                            <g:if test="${grailsApplication.config.spotfire.fileType.pmpr.enabled}">
                                <div class="radio radio-primary radio-inline">
                                    <input type="radio" name="type" id="spotfirePMPR" value="pmpr"/>
                                    <label for="spotfirePMPR">
                                        <g:message code="app.spotfire.pmpr" default="PMPR"/>
                                    </label>
                                </div>
                            </g:if>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                 <div class="col-md-12">
                     <div class="fuelux">
                        <div class="form-group">
                            <label>
                                <g:message code="app.spotfire.filename" default="File Name"/><span
                                    class="required-indicator">*</span>
                            </label>
                            <div>
                                <g:textField class="col-md-12 form-control"
                                             name="fullFileName" id="fullFileName" />
                                <small class="text-muted"><g:message code="app.spotfire.filename.help.text" /></small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="pull-right">
                        <button type="submit" class="btn btn-primary" id="generate">
                            <span class="glyphicon glyphicon-flash icon-white"></span>
                            <g:message code="app.odataConfig.genarate"/>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
