<%@ page import="com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.reportTemplate.ReassessListednessEnum; com.rxlogix.config.DataTabulationTemplate; com.rxlogix.config.ReportFieldInfo; com.rxlogix.ChartOptionsUtils; com.rxlogix.enums.ReportFieldSelectionTypeEnum; com.rxlogix.config.SourceProfile; com.rxlogix.util.ViewHelper" %>
<asset:javascript src="vendorUi/backbone/backbone-min.js"/>
<asset:javascript src="app/query/queryValueSelect2.js"/>
<asset:javascript src="app/template/customExpression.js"/>
<asset:javascript src="app/disableAutocomplete.js"/>
<asset:stylesheet src="query.css"/>
<asset:stylesheet src="copyPasteModal.css"/>
<script>
    $(function () {
        //For the edit mode the elements will be avaliable thus
        $(".sortOrderSeq").hide();
    });
    var tmpltReportFieldsOptsBySource = "${createLink(controller: 'template', action: 'userReportFieldsOptsBySource')}";
    var tmpltDefaultReportFieldsOpts = "${createLink(controller: 'template', action: 'userDefaultReportFieldsOpts',params: [lastModified: ViewHelper.getCacheLastModified(currentUser,session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'])])}";
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
    var datasheetUrl = "${createLink(controller: 'reportTemplateRest', action: 'getDatasheetValues')}";
</script>
<style>
.text-muted {
    background:white;
    -webkit-transition:background 1s;
    -moz-transition:background 1s;
    -o-transition:background 1s;
    transition:background 1s
}
</style>

<div class="row m-t-20">
    <div class="col-xs-12 previewLabel" style="padding-left: 25px;">
        <div class="row rxmain-container-header">
            <div class="col-xs-12">
                <label class="mb-0"><g:message code="data.tabulation.preview"/></label>
                <span class="glyphicon glyphicon-question-sign modal-link pull-right" style="cursor:pointer;margin-top: 2px;" data-toggle="modal"
                      data-target="#datatabulationHelp"></span>
            </div>
        </div>
        <g:if test="${editable}">
            <div class="col-xs-3 mt-10" id="dTTReportFieldDropDownLoading">
                <i class="fa fa-refresh fa-spin " ></i>
            </div>
            <div class="col-xs-3 mt-10" id="dTTReportFieldDropDown"  style="display: none;">
                <div id="dtColumnSelect2" hidden="hidden">
                    <select name="selectField" id="selectField_dataTabulation_column" class="form-control selectField">
                    </select>
                </div>

                <div id="dtRowSelect2">
                    <select name="selectField" id="selectField_dataTabulation_row" class="form-control selectField">
                    </select>
                </div>
            </div>
        </g:if>
        <g:if test="${sourceProfiles?.size() > 1}">
            <div class="col-xs-1  mt-10">
%{--                    <label><g:message code="userGroup.source.profiles.label"/></label>--}%
                    <g:select name="sourceProfile.id" id="sourceProfile"
                              from="${sourceProfiles}"
                              optionValue="sourceName" optionKey="sourceId"
                              value="${SourceProfile.fetchAllDataSource().sourceId}"
                              class="form-control"/>
            </div>
        </g:if>
        <div class="col-xs-8" style="margin-top: 14px; padding-left: 30px !important;">
        <sec:ifAnyGranted roles="ROLE_CHART_TEMPLATE_EDITOR">

            <div class=" checkbox checkbox-primary" style="margin-left: -20px !important;">
                <g:checkBox name="showChartSheet" value="${reportTemplateInstance?.showChartSheet}"/>
                <label style="margin-left: 10px" class="no-bold add-cursor " for="showChartSheet">
                <g:message code="show.chartSheet"/>
                </label>
            </div>
        </sec:ifAnyGranted>

            <div class=" checkbox checkbox-primary">
                <g:checkBox name="supressHeaders" value="${reportTemplateInstance?.supressHeaders}"/>
                <label style="margin-left: 10px" class="no-bold add-cursor " for="supressHeaders">
                    <g:message code="show.supressHeaders"/>
                </label>
            </div>


    <div class="checkbox checkbox-primary">
        <g:checkBox name="drillDownToCaseList" value="${reportTemplateInstance?.drillDownToCaseList}"/>
        <label style="margin-left: 10px" class="no-bold add-cursor " for="drillDownToCaseList">
            <g:message code="show.drillDownToCaseList"/>
        </label>
    </div>
    <div class="checkbox checkbox-primary">
        <g:checkBox name="pageBreakByGroup" value="${reportTemplateInstance?.pageBreakByGroup}"/>
        <label for="pageBreakByGroup">
            <g:message code="page.break.by.group"/>
        </label>
    </div>
    %{--<div class="checkbox checkbox-primary" >
        <g:checkBox name="transposeOutput" value="${reportTemplateInstance?.transposeOutput}"/>
        <label for="transposeOutput">
            <g:message code="show.transposeOutput"/>
        </label>
    </div>--}%
    <div class="checkbox checkbox-primary">
        <g:checkBox name="supressRepeatingExcel" value="${reportTemplateInstance?.supressRepeatingExcel}"/>
        <label style="margin-left: 10px" class="no-bold add-cursor " for="supressRepeatingExcel">
            <g:message code="show.supressRepeatingExcel"/>
        </label>
    </div>
    <div class="checkbox checkbox-primary">
        <g:checkBox name="positiveCountOnly" value="${reportTemplateInstance?.positiveCountOnly}"/>
        <label style="margin-left: 10px" class="no-bold add-cursor " for="positiveCountOnly">
            <g:message code="show.positiveCountOnly"/>
        </label>
    </div>
    <span id="allTimeframesWraper">
    <div class="checkbox checkbox-primary">
        <g:checkBox name="allTimeframes" value="${reportTemplateInstance?.allTimeframes}"/>
        <label style="margin-left: 10px" class="no-bold add-cursor " for="allTimeframes">
            <g:message code="show.allTimeframes"/>
        </label>
    </div>
    </span>
        </div>
    </div>
</div>

    <div class="row m-t-10">
        <div class="col-xs-5" style="padding-left: 30px; padding-right: 20px!important;">
            <div class="row" >
                <div class="col-xs-12">
            <label style="float:right;padding-top: 26px;"><g:message code="app.label.grouping"/></label>
                </div>
            </div>
            <div class="row groupingContainer shortBorder rowsAndColumnsContainer selectedContainerBorderTab" style="white-space: normal;">
                <div class="col-xs-12 no-padding columnScroll">
                    <div id="groupingContainer" class="containerToBeSelected groupingContainer" style="display: flex;"></div>
                </div>
            </div>
            <div class="row" >
                <div class="col-xs-12">
                    <label style="float:right;"><g:message code="app.label.rows" /></label>
                </div>
            </div>

            <div class="row rowsAndColumnsContainer rowsAndColumnsContainerBorder rowsContainer selectedContainerBorderTab selectedContainerBorder ">
                <div class="col-xs-12 no-padding columnScroll">
                    <div hidden="hidden"><g:render template="toAddRow" /></div>
                    <div id="rowsContainer" class="containerToBeSelected"></div>
                </div>
            </div>
        </div>

    <div class="col-xs-7 columnScroll" style="padding-left: 20px; padding-right: 30px;">
        <div id="columnMeasureContainer">
            <g:each in="${reportTemplateInstance?.columnMeasureList}" var="columnMeasure" status="index">
                <g:render template="columnMeasure"
                          model="['columnMeasure': columnMeasure, 'index': index, selectedLocale: selectedLocale]"/>
            </g:each>
            <input class="btn btn-default btn-sm m-t-8" type="button" id="addColumnMeasure"
                   value="${message(code: 'default.button.add.label')}">
        </div>
        <div hidden="hidden"><g:render template="columnMeasure" model="['index': '']"/></div>
        <g:hiddenField name="numColMeas" value="${reportTemplateInstance?.columnMeasureList?.size() ?: 1}"/>
        <g:hiddenField name="validColMeasIndex" value=""/>
    </div>
</div>

<div class="row" style="padding-top: 30px;">
    <div class="col-xs-12 fieldOptions columnRenameArea" hidden="hidden">
        <i class="fa fa-times add-cursor closeRenameArea" style="float: right;"></i>

       <div class="d-t-border" style="margin:0px">
                    <div class="rxmain-container-header">
                        <a class="theme-color case-count-strip" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg click fa-caret-down"></i> <span class="fieldSectionHeader"></span></a>
                    </div>
                    <div class="row case-count-section p-10">
            <div class="col-xs-5">
                <label><g:message code="cognosReport.name.label"/>:</label>

                <div class="input-group">
                    <input class="selectedColumnName form-control">
                    <span class="input-group-btn" style="padding-left: 5px;">
                        <input type="button" class="btn btn-default btn-sm resetThisCol"
                               value="${message(code: 'default.button.reset.label')}">
                    </span>
                </div>
                <label><g:message code="app.legend.label"/>:</label>

                <div class="input-group">
                    <input class="selectedColumnLegend form-control" maxlength="2000">
                    <span class="input-group-btn" style="padding-left: 5px;">
                        <input type="button" class="btn btn-default btn-sm resetThisColLegend"
                               value="${message(code: 'default.button.reset.label')}">
                    </span>
                </div>
                <label><g:message code="app.label.columnWidth"/>:</label>

                <div class="input-group">
                    <g:select name="selectedColumnWidth" from="${(5..100).step(5)}"
                              class="selectedColumnWidth form-control"
                              optionValue="${{ it + "%" }}" noSelection="['0': 'Auto']"></g:select>
                    <span class="input-group-btn" style="padding-left: 5px;">
                        <input type="button" class="btn btn-default btn-sm resetThisColWidth"
                               value="${message(code: 'default.button.reset.label')}">
                    </span>
                </div>
            </div>

            <div class="col-xs-1 datasheetOption" hidden="hidden">
                <label><g:message code="app.label.datasheet"/></label>
                <div id="datasheetDropDownLoading" style="display: none">
                    <i class="fa fa-refresh fa-spin " ></i>
                </div>
                <div id="datasheetDropDown">
                    <select name="selectDatasheet" id="selectDatasheet" class="form-control"></select>
                </div>                <div class="checkbox checkbox-primary onPrimaryDatasheet">
                    <input type="checkbox" class="" id="onPrimaryDatasheet">
                    <label class="add-cursor no-bold" for="onPrimaryDatasheet" style="white-space: nowrap;">
                        <g:message code="app.label.onPrimaryDatasheet"/>
                    </label>
                </div>
            </div>

            <div class="col-xs-2">
                <sec:ifAnyGranted roles="ROLE_CUSTOM_EXPRESSION">
                    <a class="add-cursor showCustomExpression"><g:message code="app.template.customExpression"/></a>
                </sec:ifAnyGranted>
            </div>
            <div class="col-xs-4 cLLCheckBoxes d-inline">
                <div class="checkbox checkbox-primary">
                    <input type="checkbox" class="redactedValue" id="redactedValue">
                    <label for="redactedValue"><g:message code="app.label.redact.values"/></label>
                </div>
                <div class="checkbox checkbox-primary" >
                    <input type="checkbox" class="blindedValues" id="blindedValues">
                    <label for="blindedValues"><g:message code="blinded.values"/></label>
                </div>
                <div class="checkbox checkbox-primary">
                    <input type="checkbox" class="hideSubtotal" id="hideSubtotal">
                    <label for="hideSubtotal"><g:message code="app.template.hideSubtotal"/></label>
                </div>
            </div>
        </div>
       </div>
        <div class="row customExpressionArea" hidden="hidden">
            <div class="col-xs-12">
                <textarea class="form-control customExpressionValue m-t-5"
                          maxlength="${ReportFieldInfo.constrainedProperties.customExpression.maxSize}" rows="4"></textarea>
            </div>
        </div>
    </div>
</div>

<div id="measureOptionsArea">
    <g:each in="${reportTemplateInstance?.columnMeasureList}" var="columnMeasure" status="i">
        <g:each in="${columnMeasure?.measures}" var="measure" status="j">
            <g:render template="measureOptions" model="['measure': measure, 'colMeasIndex': i, 'measIndex': j]"/>
        </g:each>
    </g:each>

    <g:render template="measureOptions" model="['colMeasIndex': '', 'measIndex': '']"/>
</div>

<div class="row d-t-border chartSettingsHeader" style="margin-top: 5px;display: none">
    <div class="rxmain-container-header"><a class="theme-color chartSettings" href="javascript:void(0)"><i class="openCloseIcon fa fa-lg click fa-caret-down"></i> <g:message code="app.label.chartSettings"/> </a></div>
    <span style="margin-left: 5px"><g:message code="app.label.maxChartPoints" default="Max Chart Points"/></span>
    <input name="maxChartPoints" id="maxChartPoints" class="form-control" type="number" min="1" max="999" value="${reportTemplateInstance?.maxChartPoints?:50}" style="width: 50px; display: inline; margin-top: 2px">

    <div class="checkbox checkbox-primary" style="display: inline">
        <g:checkBox name="chartExportAsImage" value="${reportTemplateInstance?.chartExportAsImage}"/>
        <label style="margin-left: 10px; margin-top: 5px;" class="no-bold add-cursor chartExportAsImageDiv" for="chartExportAsImage">
            <g:message code="show.chartExportAsImage"/>
        </label>
    </div>
    <i class="glyphicon glyphicon-info-sign"  title="${message(code: 'app.data.tabulation.measures.imageAsChart.info')}"></i>
    <div class="checkbox checkbox-primary" style="display: inline">
        <g:checkBox name="worldMap" value="${reportTemplateInstance?.worldMap}"/>
        <label style="margin-left: 10px; margin-top: 5px;" class="no-bold add-cursor worldMapDiv" for="worldMap">
            <g:message code="show.worldMap"/>
        </label>
    </div>
    <div class="worldMapConfigDiv" style="display: none">
        <label for="worldMapConfig">colorAxis:</label>
        <textarea style="width: 100%; height: 300px" id="worldMapConfig" name="worldMapConfig" placeholder="
        {
            min: 1,
            max: 100,
            type: 'logarithmic',
            minColor: '#00ff00',
            maxColor: '#ff0000',
            stops: [
                [0, '#00ff00'],
                [0.5, '#ffff00'],
                [1, '#ff0000']
            ]
        }
    or
       { dataClasses: [{
                to: 3,
                color: '#0000ee'
            }, {
                from: 3,
                to: 10,
                color: '#00eeee'
            }, {
                from: 10,
                color: '#00ee00'
            }]
       }

">${reportTemplateInstance?.worldMapConfig}</textarea>
    </div>
    <div class="chartSettings-section row p-10">
        <div id="easychart-preview" hidden="hidden"></div>
    </div>
</div>

<div class="row reassessListedness" hidden="hidden" style="padding-top: 10px;">
    <div class="col-xs-4">
        <label><g:message code="app.label.reassessListedness"/></label>
        <g:select class="form-control" name="reassessListedness"
                  from="${ViewHelper.getReassessListedness()}" optionKey="name" optionValue="display"
                  value="${reportTemplateInstance?.reassessListedness}"/>
    </div>

        <div class="col-xs-2" id="templtCustomDateSelector" style="display: ${reportTemplateInstance.instanceOf(DataTabulationTemplate) && reportTemplateInstance.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE ? 'block' : 'none'}">
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

<div class="row">
    <div class="col-xs-12 m-t-10">
        <input id="JSONMeasures" name="JSONMeasures" hidden="hidden"
               value="${reportTemplateInstance.getJSONStringMeasures()}">
    </div>
</div>

<g:templateListAsJSONStringHidden name="grouping" list="${reportTemplateInstance?.groupingList}"
                                  selectedLocale="${selectedLocale}"/>
<g:templateListAsJSONStringHidden name="rows" list="${reportTemplateInstance?.rowList}"
                                  selectedLocale="${selectedLocale}"/>
<g:chartDefaultOptionsHidden name="chartDefaultOptions"/>
<g:hiddenField name="chartCustomOptions" value="${reportTemplateInstance?.chartCustomOptions}"/>
<div class="row d-t-border">
    <div class="rxmain-container-header">
        <a href="#advancedCustomExpression" id="showAdvancedCustomExpression" class="theme-color"> <i class="openCloseIcon fa fa-lg fa-caret-right "></i> <g:message code="app.label.caseLineListing.advanced.custom.expression" default="Additional Where Clause"/>
           </a>
    </div>

    <div id="advancedCustomExpression" class="p-10" hidden="hidden">
        <div class="row">
            <div class="col-xs-11">
                <g:if test="${editable}">
                    <div class="row">
                       %{-- <div class="col-xs-2">
                            <label><g:message code="app.label.queryCriteria"/></label>
                        </div>--}%
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
                           value="${reportTemplateInstance?.templateType == TemplateTypeEnum.DATA_TAB ? reportTemplateInstance?.JSONQuery : null}"/>
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
                          optionValue="display"/>
            </div>
        </div>
        <g:render plugin="pv-dictionary" template="/plugin/dictionary/dictionaryModals"/>
    </div>
</div>
<g:render template="includes/reportFooter" model="[readonly: !editable]"/>
<g:render template="includes/dataTabulationTemplateHelp"/>
<g:render template="includes/colorConditionsModals"/>