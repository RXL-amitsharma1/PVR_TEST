<%@ page import="com.rxlogix.enums.BalanceQueryPeriodEnum; com.rxlogix.util.RelativeDateConverter; com.rxlogix.util.DateUtil; java.util.Date; com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.user.UserGroup; com.rxlogix.config.SourceProfile;com.rxlogix.Constants"%>
<style>
.execute-mh{min-height: 307px;}
.casesDiv{display: flex;}
</style>

<div id="bmQuerySections${i}" class="" <g:if test="${hidden}">style="display:none;"</g:if>>
    <g:hiddenField name='bmQuerySections[${i}].id' value="${bmQuerySection?.id}"/>
    <g:hiddenField name='bmQuerySections[${i}].dynamicFormEntryDeleted' value='false'/>
    <g:hiddenField id="bmQuerySections[${i}].sourceProfile" name="bmQuerySections[${i}].sourceProfile" class="sourceProfile${i}" value="${bmQuerySection?.sourceProfile?.id}" />
    <input type="hidden" class="selectedDistinctTables" value="${bmQuerySection?.distinctTables?.collect{it.entity}}" />
    <div class="row m-b-10">
        <div class="col-md-4">
            <div class="col-md-12 border-around-1"><label><g:message code="balanceMinusQuery.executeFor.label"/></label></div>
            <div class="clearfix"></div>
            <div class="row border-around-2 m-0 execute-mh multi-copy-n-paste">
                <div class="">
                    <div class="col-lg-12 border-around-1 m-5" style="width:98%"><label><g:message code="app.label.odataSource.label"/> - <span id="sourceProfileLabel${i}"></span>${bmQuerySection?.sourceProfile?.sourceName} </label> </div>
                    <div class="col-lg-12 m-t-5">
                        <div class="col-lg-12">
                            <label><g:message code="app.label.period.and.case.list"/></label>
                            <g:select name="bmQuerySections[${i}].executeFor" id="bmQuerySections[${i}].executeFor"
                                      from="${ViewHelper.getBalanceQueryPeriodEnum()}" disabled="${bmQueryInstance?.isDisabled}"
                                      optionValue="display" value="${bmQuerySection?.executeFor}" optionKey="name"
                                      noSelection="['':'Select one']"
                                      class="form-control select2-box executeFor"/>
                        </div>
                        <div class="col-lg-12 fuelux">
                            <div class="bqmqDateCls-${i}" style="${bmQuerySection && bmQuerySection?.executeFor == BalanceQueryPeriodEnum.ETL_START_DATE.name() ? 'display: block' : 'display: none'}">
                                <label for="executionStartDate">From :</label>
                                <div class="datepicker executionStartDatePicker">
                                    <div class="input-group">
                                        <g:textField name="bmQuerySections[${i}].executionStartDate" id="bmQuerySections[${i}].executionStartDate"
                                                     class="form-control executionStartDate"
                                                     placeholder="Start Date" disabled="${bmQueryInstance?.isDisabled}"
                                                     value="${renderShortFormattedDate(date: bmQuerySection?.executionStartDate)}"/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-12 fuelux">
                            <div class="bqmqDateCls-${i}" style="${bmQuerySection && bmQuerySection?.executeFor == BalanceQueryPeriodEnum.ETL_START_DATE.name() ? 'display: block' : 'display: none'}">
                                <label for="executionEndDate">To :</label>
                                <div class="datepicker executionEndDatePicker">
                                    <div class="input-group">
                                        <g:textField name="bmQuerySections[${i}].executionEndDate" id="bmQuerySections[${i}].executionEndDate"
                                                     class="form-control executionEndDate"
                                                     placeholder="End Date" disabled="${bmQueryInstance?.isDisabled}"
                                                     value="${renderShortFormattedDate(date: bmQuerySection?.executionEndDate)}"/>
                                        <g:render template="/includes/widgets/datePickerTemplate"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-12">
                            <div class="lastXDaysCls-${i}" style="${bmQuerySection && (bmQuerySection?.executeFor == BalanceQueryPeriodEnum.LAST_X_ETL.name()  || bmQuerySection?.executeFor == BalanceQueryPeriodEnum.LAST_X_DAYS.name()) ? 'display: block' : 'display: none'}">
                                <input class="form-control m-t-4" type="number" name="bmQuerySections[${i}].xValue" id="bmQuerySections[${i}].xValue" min="1" max="1000" value="${bmQuerySection?.xValue}"
                                ${bmQueryInstance?.isDisabled ? 'disabled' : ''}/>
                            </div>
                        </div>

                        <!------------------------------------------Case List code has started------------------------------------->
                        <div class="includeCasesCls-${i} toAddContainer" style="${bmQuerySection && bmQuerySection?.executeFor == BalanceQueryPeriodEnum.CASE_LIST.name() ? 'display: block' : 'display: none'}">
                            <div class="col-md-12 m-t-10 casesDiv">
                                <label>Case List</label>
                                <a data-evt-clk='{"method": "copyPasteClick", "params": []}'><i class="fa fa-pencil-square-o copy-n-paste modal-link"  data-toggle="modal" data-target="#copyAndPasteModal"
                                      style="cursor:pointer"></i>
                                </a>
                            </div>
                            <div class="col-md-12">
                                <g:textArea name="bmQuerySections[${i}].includeCases" id="bmQuerySections[${i}].includeCases" type="text" value="${(bmQuerySection?.includeCases?.collect{it.caseNumber})?.join(";")}"
                                            disabled="${bmQueryInstance?.isDisabled}"
                                            class="form-control columnSearchCll caseNum-filter" copyandpastewithdelimiter="true" rows="3"/>
                            </div>
                        </div>
                        <!------------------------------------------Case List code has closed------------------------------------------>

                        <!------------------------------------------Exclude code has started--------------------------------------->
                        <div class="toAddContainer">
                            <div class="col-md-12 m-t-10">
                                <div class="checkbox checkbox-primary casesDiv">
                                    <g:checkBox id="bmQuerySections[${i}].flagCaseExclude" disabled="${bmQueryInstance?.isDisabled ? 'true' : 'false'}"
                                                name="bmQuerySections[${i}].flagCaseExclude" class="excludedCases"
                                                checked="${bmQuerySection && bmQuerySection?.flagCaseExclude}"/>
                                    <label for="flagCaseExclude">Exclude</label>
                                    <a class="flagCaseExcludeCls-${i}" data-evt-clk='{"method": "copyPasteClick", "params": []}' style="${bmQuerySection && bmQuerySection?.flagCaseExclude == true ? 'display: block' : 'display: none'}">
                                        <i class="fa fa-pencil-square-o copy-n-paste modal-link" data-toggle="modal" data-target="#copyAndPasteModal"
                                           style="cursor:pointer"></i>
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <g:textArea name="bmQuerySections[${i}].excludeCases" id="bmQuerySections[${i}].excludeCases" type="text" class="form-control caseNum-filter flagCaseExcludeCls-${i}" value="${(bmQuerySection?.excludeCases?.collect{it.caseNumber})?.join(";")}"
                                            disabled="${bmQueryInstance?.isDisabled}"
                                            copyandpastewithdelimiter="true" style="${bmQuerySection && bmQuerySection?.flagCaseExclude == true ? 'display: block' : 'display: none'}" rows="3"/>
                            </div>
                        </div>
                        <!------------------------------------------Exclude code has closed---------------------------------------->
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-8">
            <div class="col-md-12 border-around-1">
                <label class="dateRangeLabel"><g:message code="balanceMinusQuery.tableForExecution.label"/></label>
            </div>
            <div class="col-md-12 border-around-2">
                <div class="row">
                    <g:select id="bmQuerySections[${i}].distinctTables" name="bmQuerySections[${i}].distinctTables"
                              from=""
                              optionKey="id" optionValue="label"
                              multiple="true" class="distinctTables"
                              value="${bmQuerySection?.distinctTables?.collect{it.entity}}">
                    </g:select>
                </div>
            </div>
        </div>
    </div>
</div>