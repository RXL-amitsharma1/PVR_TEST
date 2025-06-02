<%@ page import="com.rxlogix.config.SourceProfile; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.config.CaseLineListingTemplate; com.rxlogix.config.ReportFieldInfo; com.rxlogix.config.ReportField; com.rxlogix.enums.ReportFieldSelectionTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.TemplateTypeEnum;" %>
<asset:javascript src="vendorUi/backbone/backbone-min.js"/>
<asset:javascript src="app/query/queryValueSelect2.js"/>
<asset:javascript src="app/template/customExpression.js"/>
<asset:javascript src="app/disableAutocomplete.js"/>
<asset:stylesheet src="query.css"/>
<asset:stylesheet src="copyPasteModal.css"/>
<g:javascript>
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action:'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var extraValuesUrl = "${createLink(controller: 'query', action: 'extraValues')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";
        var importExcel="${createLink(controller: 'query', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'configuration', action: 'validateValue')}";
        var editable = ${editable};
        var tmpltReportFieldsOptsBySource="${createLink(controller: 'template', action: 'userReportFieldsOptsBySource')}";
        var tmpltDefaultReportFieldsOpts="${createLink(controller: 'template', action: 'userDefaultReportFieldsOpts', params: [lastModified: ViewHelper.getCacheLastModified(currentUser,session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'])])}";
        var templateViewUrl = "${createLink(controller: 'template', action: 'view')}";
        var datasheetUrl = "${createLink(controller: 'reportTemplateRest', action: 'getDatasheetValues')}";
</g:javascript>

<div class="row m-t-10">

    <div class="col-xs-12" style="padding-left: 25px; padding-right: 25px;">
        <div class="row rxmain-container-header">
            <div class="col-xs-12">
                <label class="m-b-0"><g:message code="line.listing.preview" /></label>
            </div>
        </div>

        <g:if test="${editable}">
            <div class="col-xs-3 mt-10" id="cLLReportFieldDropDownLoading" >
                <i class="fa fa-refresh fa-spin " ></i>
            </div>
            <div class="col-xs-3 mt-10" id="cLLReportFieldDropDown" style="display: none;">
                <select name="selectField" id="selectField_lineListing" class="form-control selectField">
                </select>
            </div>
        </g:if>

        <g:if test="${sourceProfiles?.size() > 1}">
            <div class="col-xs-6  mt-10">
                <div class="col-xs-3">
                    <g:select name="sourceProfile.id" id="sourceProfile"
                              from="${sourceProfiles}"
                              optionValue="sourceName" optionKey="sourceId"
                              value="${SourceProfile.fetchAllDataSource().sourceId}"
                              class="form-control"/>
                </div>
            </div>
        </g:if>

        <div class="row">
            <div class="col-xs-12 m-t-5">
                <label><g:message code="app.label.grouping"/></label>
                <div class="checkbox checkbox-primary" style="float: right">
                    <g:checkBox name="pageBreakByGroup" value="${reportTemplateInstance.pageBreakByGroup}" />
                    <label for="pageBreakByGroup" class="no-bold add-cursor">
                        <g:message code="page.break.by.group" />
                    </label>
                </div>
            </div>
        </div>

        <div class="row groupingContainer shortBorder rowsAndColumnsContainer">
            <div class="col-xs-12 no-padding columnScroll">
                <div id="groupingContainer" class="containerToBeSelected groupingContainer"></div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12 m-t-5">
                <label><g:message code="app.label.columns"/></label>
            </div>
        </div>

        %{--
            todo:  selectedColumnsContainer:  this needs to be populated from the model
            The widget needs to paint itself, not be dependent on a second server call to initiate JS to fill the widget
        --}%
        <div id="columnsContainerContainer">

            <i id="removeCLLSet0" class='fa fa-times removeCLLSet' data-setid='0'></i>
            <div class="row columnsContainer rowsAndColumnsContainer rowsAndColumnsContainerBorder selectedContainerBorder">
                <div class="col-xs-12 no-padding columnScroll">
                    <div id="columnsContainer0" data-setid="0" class="containerToBeSelected columnsContainer"></div>
                </div>
            </div>

            <div hidden="hidden"><g:render template="toAddColumn" /></div>

            <a id="addCLLSet" class="btn btn-primary "><g:message code="app.add.button.label"/></a>

        </div>
        <div class="row">
            <div class="col-xs-12">
                <label><g:message code="app.label.rowColumns" /></label>
            </div>
        </div>

        <div class="row rowColumnsContainer shortBorder rowsAndColumnsContainer">
            <div class="col-xs-12 no-padding columnScroll">
                <div id="rowColumnsContainer" class="containerToBeSelected rowColumnsContainer"></div>
            </div>
        </div>
        <div class="row">
            <div class="col-xs-12" style="padding: 5px">
                <div class="checkbox checkbox-primary" style="float: right;margin-left: 10px !important;">
                    <g:checkBox name="columnShowDistinct" value="${reportTemplateInstance?.columnShowDistinct}" />
                    <label for="columnShowDistinct" class="no-bold add-cursor"><g:message code="show.distinct" /></label>
                </div>
                <div class="checkbox checkbox-primary" style="float: right;margin-left: 10px !important;">
                    <g:checkBox name="columnShowSubTotal" value="${reportTemplateInstance?.columnShowSubTotal}" />
                    <label for="columnShowSubTotal"  class="no-bold add-cursor">
                        <g:message code="show.subtotal"/>
                    </label>
                </div>
                <div class="checkbox checkbox-primary" style="float: right;margin-left: 10px !important;">
                    <g:checkBox name="columnShowTotal" value="${reportTemplateInstance?.columnShowTotal}" />
                    <label for="columnShowTotal"  class="no-bold add-cursor">
                        <g:message code="show.total"/>
                    </label>
                </div>
                <div class="checkbox checkbox-primary" style="float: right;margin-left: 10px !important;">
                    <g:checkBox name="hideTotalRowCount" value="${reportTemplateInstance?.hideTotalRowCount}" />
                    <label for="hideTotalRowCount"  class="no-bold add-cursor">
                        <g:message code="app.template.hideTotalRowCount"/>
                    </label>
                </div>

            </div>
        </div>

        <div class="row">


            <div class="col-xs-12 fieldOptions columnRenameArea" hidden="hidden">
                <i class="fa fa-times add-cursor closeRenameArea" style="float: right;"></i>
                <div class="d-t-border" style="margin:0px">
                    <div class="rxmain-container-header">
                        <a class="theme-color case-count-strip" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg click fa-caret-down"></i> <span class="fieldSectionHeader"></span></a>
                    </div>
                    <div class="row case-count-section p-10">
                    <div class="col-xs-5">
                        <label><g:message code="app.label.name" />:</label>
                        <div class="input-group">
                            <input class="selectedColumnName form-control" maxlength="${ReportFieldInfo.constrainedProperties.renameValue.maxSize}">
                            <span class="input-group-btn" style="padding-left: 5px;">
                                <input type="button" class="btn btn-default btn-sm resetThisCol" value="${message(code: 'default.button.reset.label')}">
                            </span>
                        </div>
                        <label><g:message code="app.legend.label" />:</label>
                        <div class="input-group">
                            <input class="selectedColumnLegend form-control" maxlength="2000">
                            <span class="input-group-btn" style="padding-left: 5px;">
                                <input type="button" class="btn btn-default btn-sm resetThisColLegend" value="${message(code: 'default.button.reset.label')}">
                            </span>
                        </div>
                        <label><g:message code="app.label.columnWidth" />:</label>
                        <div class="input-group">
                            <g:select name="selectedColumnWidth" from="${(5..100).step(5)}" class="selectedColumnWidth form-control"
                                      optionValue="${{it + "%"}}" noSelection="['0':'Auto']"></g:select>
                            <span class="input-group-btn" style="padding-left: 5px;">
                                <input type="button" class="btn btn-default btn-sm resetThisColWidth" value="${message(code: 'default.button.reset.label')}">
                            </span>
                        </div>
                    </div>
                    <div class="col-xs-2 datasheetOption" hidden="hidden">
                        <label><g:message code="app.label.datasheet"/>: </label>
                        <div id="datasheetDropDownLoading" style="display: none">
                            <i class="fa fa-refresh fa-spin " ></i>
                        </div>
                        <div id="datasheetDropDown">
                            <select name="selectDatasheet" id="selectDatasheet" class="form-control"></select>
                        </div>
                        <div class="checkbox checkbox-primary onPrimaryDatasheet">
                            <input type="checkbox" id="onPrimaryDatasheet">
                            <label class="add-cursor no-bold" for="onPrimaryDatasheet" style="white-space: nowrap;">
                                <g:message code="app.label.onPrimaryDatasheet"/>
                            </label>
                        </div>
                    </div>
                    <div class="col-xs-2">
                        <label class="add-cursor no-bold">
                            <sec:ifAnyGranted roles="ROLE_CUSTOM_EXPRESSION">
                                <a class="add-cursor showCustomExpression"><g:message code="app.template.customExpression" default="Custom Expression"/></a>
                            </sec:ifAnyGranted>
                        </label>
                        <label class="add-cursor no-bold">
                            <a class="add-cursor showAdvancedSorting" hidden="hidden"><g:message code="app.label.caseLineListing.advancedSorting" default="Advanced Sorting"/></a>
                        </label>
                    </div>
                    <div class="col-xs-3 cLLCheckBoxes">
                        <div class="checkbox checkbox-primary">
                            <input type="checkbox" class="commaSeparated" id="commaSeparated">
                            <label class="add-cursor no-bold" for="commaSeparated">
                                <g:message code="app.template.CommaSeparatedValue"/>
                            </label>
                        </div>
                        <div class="checkbox checkbox-primary">
                            <input type="checkbox" class="suppressRepeating" id="suppressRepeating">
                            <label class="add-cursor no-bold" for="suppressRepeating">
                                 <g:message code="app.template.suppressRepeatingValues" />
                            </label>
                        </div>
                        <div class="checkbox checkbox-primary">
                            <input type="checkbox" class="suppressLabel" id="suppressLabel">
                            <label for="suppressLabel" class="add-cursor no-bold">
                                <g:message code="app.template.suppressLabel" />
                            </label>
                        </div>
                        <div class="checkbox checkbox-primary">
                            <input type="checkbox" class="redactedValue" id="redactedValue">
                            <label  for="redactedValue" class="add-cursor no-bold">
                                <g:message code="app.label.redact.values" />
                            </label>
                        </div>
                        <div class="checkbox checkbox-primary">
                            <input type="checkbox" class="blindedValues" id="blindedValues">
                            <label for="blindedValues" class="add-cursor no-bold">
                                 <g:message code="blinded.values" />
                            </label>
                        </div>
                    </div>

                    <div class="customExpressionArea col-xs-6" hidden="hidden">
                        <label><g:message code="app.template.customExpression" default="Custom Expression"/></label>
                        <textarea class="form-control customExpressionValue m-t-5"
                                  maxlength="${ReportFieldInfo.constrainedProperties.customExpression.maxSize}" rows="4"></textarea>
                    </div>

                    <div class="advancedSortingArea col-xs-6" hidden="hidden">
                        <label><g:message code="app.label.caseLineListing.advancedSorting" default="Advanced Sorting"/></label>
                        <textArea class="form-control advancedSortingValue m-t-5"
                                  maxlength="${ReportFieldInfo.constrainedProperties.advancedSorting.maxSize}" rows="4"></textarea>
                    </div>
                    </div>
                <sec:ifAnyGranted roles="ROLE_CUSTOM_EXPRESSION">
                <div class="row d-t-border case-count-section" style="margin:0px; margin-top: 5px;">
                    <div class="rxmain-container-header">
                        <a class="theme-color showHideAdvancedSettings" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg click fa-caret-right"></i> <g:message code="app.dataTabulation.AdvancedSettings" default="Advanced Settings" /></a>
                    </div>

                    <div class="col-xs-12 advancedSettings" style="display: none;padding-bottom: 5px; ">

                        <div class="row">
                            <div class="col-xs-6">
                                <label><g:message code="app.label.drillDownTemplate" /></label>
                                <g:select noSelection="['': message(code: 'select.operator')]" from="${[]}" name="drillDownTemplateCll" class="form-control drillDownTemplateCll"/>
                            </div>

                            <div class="col-xs-2">
                                <label>&nbsp;</label><div>
                                <a href="${templateQueryInstance?.id ?createLink(controller: 'template' , action: 'view', id: templateQueryInstance?.id):'#'}"
                                   title="${message(code: 'app.label.viewTemplate')}" target="_blank" class="pv-ic templateQueryIcon templateViewButton glyphicon glyphicon-info-sign ${templateQueryInstance?.id ? '' : 'hide'}"></a>
                            </div>
                            </div>

                            <div class="col-xs-4">
                                <label><g:message code="app.label.drillDownFilterColumns" /></label>
                                <g:select from="${[]}" name="drillDownFilerColumns" class="form-control drillDownFilerColumns"/>
                            </div>
                        </div>

                    </div>

                </div>
                <div class="row d-t-border colorConditionsCantainer case-count-section" style="margin:0px; margin-top: 5px;">
                    <div class="col-xs-12 ">
                        <g:hiddenField name="colorConditions"
                                       value="" class="colorConditionsJson"/>
                        <g:render template="includes/colorConditionsTable"/>
                    </div>
                </div>
                </sec:ifAnyGranted>
            </div>
        </div>
        </div>

        <div class="row d-t-border" style="margin-top: 5px">
            <div class="rxmain-container-header">

                <a href="#advancedCustomExpression" id="showAdvancedCustomExpression" class="theme-color"> <i class="openCloseIcon fa fa-lg fa-caret-right "></i> <g:message code="app.label.caseLineListing.advanced.custom.expression" default="Additional Where Clause"/>
                </a>
            </div>

            <div id="advancedCustomExpression" class="p-10" hidden="hidden">
                <div class="row">
                    <div class="col-xs-11">
                        <g:if test="${editable}">
                            <div class="row">
                                <div class="col-xs-2">
                                    <label><g:message code="app.label.queryCriteria"/></label>
                                </div>
                                <g:if test="${sourceProfiles?.size() > 1}">
                                    <div class="col-xs-2">
                                        <label><g:message code="userGroup.source.profiles.label"/></label>
                                        <g:select name="sourceProfile" id="sourceProfile"
                                                  from="${sourceProfiles}"
                                                  optionValue="sourceName" optionKey="sourceId"
                                                  value="${SourceProfile.fetchAllDataSource().sourceId}"
                                                  class="form-control"/>
                                    </div>
                                </g:if>
                            </div>

                            <div class="form-group row loading">
                                <i class="fa fa-refresh fa-spin"></i>
                            </div>
                        </g:if>

                        <div class="form-group row doneLoading addContainerTopmost" id="addContainerWithSubmit">
                            <g:render template="/query/toAddExtraValues" />
                            <g:render template="/query/toAddContainer" model="[currentUser:currentUser]" />
                        </div>

                        <div hidden="hidden">
                            <input name="JSONQuery" id="queryJSON"
                                   value="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? reportTemplateInstance?.JSONQuery : null}"/>
                            <a href="http://jsonlint.com/" target="_blank"
                               rel="noopener noreferrer">Prettify my JSON here!</a>

                            <div>has blanks?
                                <input name="hasBlanks" id="hasBlanksQuery" value=""/>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="loading container">
                        <i class="fa fa-refresh fa-spin"></i>
                    </div>

                    <div id="builderAll" class="builderAll doneLoading">
                    </div>
                </div>

                <div id="extraOptions" class="row extraOptions" hidden="hidden">
                    <div class="col-xs-4">
                        <label><g:message code="app.label.reassessListedness"/>:</label>
                        <g:select class="form-control" name="customExpressionListedness"
                                  from="${ViewHelper.getReassessListedness()}" optionKey="name"
                                  optionValue="display"
                                  value="${reportTemplateInstance?.customExpressionListedness}"/>
                    </div>
                </div>
                <g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
            </div>
        </div>





        <g:render template="includes/reportFooter" model="[readonly: !editable]" />

        <div class="row reassessListedness" hidden="hidden" style="padding-top: 10px;">
            <div class="col-xs-4" style="padding-left: 0px;">
                <label><g:message code="app.label.reassessListedness"/>: </label>
                <g:select class="form-control" name="reassessListedness"
                          from="${ViewHelper.getReassessListedness()}" optionKey="name" optionValue="display"
                          value="${reportTemplateInstance?.reassessListedness}"/>
            </div>

            <div class="col-xs-2" id="templtCustomDateSelector" style="display: ${reportTemplateInstance.instanceOf(CaseLineListingTemplate) && reportTemplateInstance.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE ? 'block' : 'none'}">
                <label></label>
                <div class="fuelux p-t-5">
                    <div class="datepicker input-group" id="templtCustomDatePicker">
                        <g:textField name="templtReassessDate" class="form-control"
                                 value="${renderShortFormattedDate(date: reportTemplateInstance?.templtReassessDate)}"
                        />
                        <g:render template="/includes/widgets/datePickerTemplate"/>
                    </div>
                </div>
            </div>

            <div class="col-xs-4">
                <label></label>
                <div class="checkbox checkbox-primary p-t-10">
                    <g:checkBox name="reassessForProduct" value="${reportTemplateInstance?.reassessForProduct}"/>
                    <label for="reassessForProduct" class="add-margin-bottom" style="margin-bottom: 5px;">
                        <g:message code="app.label.reassessForProduct"/>
                    </label>
                </div>
            </div>
        </div>
    </div>
</div>

<g:render template="/includes/widgets/warningTemplate" model="[messageBody: message(code: 'app.label.caseLineListing.save.emptyset.warning'), queryType: ' ']"/>

<g:templateListAsJSONStringHidden name="columns"
                                  list="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? reportTemplateInstance?.columnList : null}"
                                  selectedLocale="${selectedLocale}"/>
<g:templateListAsJSONStringHidden name="grouping"
                                  list="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? reportTemplateInstance?.groupingList : null}"
                                  selectedLocale="${selectedLocale}"/>
<g:templateListAsJSONStringHidden name="rowCols"
                                  list="${reportTemplateInstance?.templateType == TemplateTypeEnum.CASE_LINE ? reportTemplateInstance?.rowColumnList : null}"
                                  selectedLocale="${selectedLocale}"/>
<asset:javascript src="/plugin/dictionary/dictionaryMultiSearch.js"/>
<g:render template="includes/colorConditionsModals"/>