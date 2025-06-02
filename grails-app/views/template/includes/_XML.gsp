<%@ page import="grails.converters.JSON; com.rxlogix.helper.LocaleHelper; com.rxlogix.mapping.PvcmFieldLabel; com.rxlogix.config.ReportFieldInfo; com.rxlogix.enums.ReportFieldSelectionTypeEnum; com.rxlogix.enums.TemplateTypeEnum; com.rxlogix.util.ViewHelper; com.rxlogix.enums.XMLNodeType;" %>

<asset:javascript src="vendorUi/backbone/backbone-min.js"/>
<asset:javascript src="app/query/queryValueSelect2.js"/>
<asset:javascript src="app/template/customExpression.js"/>
<asset:javascript src="vendorUi/fancytree/jquery.fancytree-all.min.js"/>
<asset:javascript src="app/template/editXMLTemplate.js"/>

<asset:stylesheet src="query.css"/>
<asset:stylesheet src="ui.fancytree.min.css"/>

<g:javascript>
        var templateSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateList')}";
        var templateNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getTemplateNameDescription')}";
        var reportFieldInfoSearchUrl = "${createLink(controller: 'reportTemplateRest', action: 'getReportFieldInfoList')}";
        var reportFieldInfoNameUrl = "${createLink(controller: 'reportTemplateRest', action: 'getReportFieldInfoNameDescription')}";
        var stringOperatorsUrl =  "${createLink(controller: 'query', action: 'getStringOperators')}";
        var numOperatorsUrl =  "${createLink(controller: 'query', action: 'getNumOperators')}";
        var dateOperatorsUrl =  "${createLink(controller: 'query', action: 'getDateOperators')}";
        var valuelessOperatorsUrl = "${createLink(controller: 'query', action: 'getValuelessOperators')}";
        var keywordsUrl =  "${createLink(controller: 'query', action: 'getAllKeywords')}";
        var fieldsValueUrl = "${createLink(controller: 'query', action: 'getFieldsValue')}";
        var possibleValuesUrl = "${createLink(controller: 'query', action: 'possibleValues')}";
        var extraValuesUrl = "${createLink(controller: 'query', action: 'extraValues')}";
        var selectAutoUrl = "${createLink(controller: 'query', action: 'ajaxReportFieldSearch')}";
        var selectNonCacheUrl = "${createLink(controller: 'query', action: 'possiblePaginatedValues')}";

        var importExcel="${createLink(controller: 'query', action: 'importExcel')}";
        var validateValue="${createLink(controller: 'query', action: 'validateValue')}";
        var queryDefaultReportFieldsOpts = "${createLink(controller: "query",action: "userDefaultReportFieldsOpts",params: [lastModified: ViewHelper.getCacheLastModified(currentUser,session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'])])}";
        var templateViewUrl = "${createLink(controller: 'template', action: isExecuted ? 'viewExecutedTemplate' : 'view')}";
        var editable = ${editable};
</g:javascript>
<g:set var="userService" bean="userService"/>
<g:set var="currentUserLocale" value="${userService.currentUser.preference.locale.language}"/>

<div class="row">
    <div class="col-xs-4" id="treePanel">
        <g:if test="${editable}">
            <span class="required-indicator">*</span>
            <a id="addTag" class="btn btn-icon waves-effect btn-default m-b-5" title="Add a tag inside the current selected tag"><i class="md md-add"></i><i class="glyphicon glyphicon-tag"></i></a>
            <a id="deleteTag" class="btn btn-icon waves-effect btn-default m-b-5 deleteTag" title="Delete the selected tag and any sub tags"> <i class="md md-remove"></i><i class="glyphicon glyphicon-tag"></i></a>
            <a id="duplicateTag" class="btn btn-icon waves-effect btn-default m-b-5" title="Copy selected tag and all sub-tags"> <i class="md md-content-copy"></i> </a>
        </g:if>
        <div id="tagsTree" style="overflow-y: auto; max-height:400px;"></div>
    </div>

    <div class="col-xs-8" id="treeContent">
        <div class="form-group">
            <label class="tag-only-related"><g:message code="app.label.template.xml.tagName" /></label>
            <label class="attribute-only-related" hidden="hidden"><g:message code="app.label.template.xml.attributeName" /></label>
            <input id="tName" type="text" placeholder="" class="form-control"/>
        </div>

        <div role="tabpanel">
            <div class="form-group">
                <div class="radio radio-inline tag-only-related" data-target="#tagPropertiesPanel">
                    <input type="radio" id="${XMLNodeType.TAG_PROPERTIES}" value="tagProperties" name="tagType" checked/>
                    <label for="${XMLNodeType.TAG_PROPERTIES}"><g:message code="${XMLNodeType.TAG_PROPERTIES.i18nKey}"/></label>
                </div>
                <div class="radio radio-inline sourceFieldDiv" data-target="#sourceFieldPanel">
                    <input type="radio" id="${XMLNodeType.SOURCE_FIELD}" value="sourceField" name="tagType"/>
                    <label for="${XMLNodeType.SOURCE_FIELD}"><g:message code="${XMLNodeType.SOURCE_FIELD.i18nKey}"/></label>
                </div>
                <div class="radio-inline color-picker-option"></div>
                <label style="margin-left: 20px; display: none" class="forField"><g:message code="app.label.template.xml.e2b" /></label>
                <input id="e2bElement" name="e2bElement" maxlength="50" class="form-control forField" style="width: 212px;display: none; margin-left: 10px;">
            </div>
            <div class="form-group">
                <label><g:message code="app.label.template.xml.e2bElementName" /></label>
                <input id="e2bElementName" name="e2bElementName" class="form-control" style="width: 456px; display: inline-block;margin-left: 10px;" data-evt-mouseOver='{"method": "xmlMouseOver", "params": []}'>
            </div>

            <g:if test="${currentUserLocale != 'en'}">
                <div class="form-group row">
                    <div class="col-md-8">
                        <label><g:message code="app.label.template.xml.e2bElementNameLocale" /></label>
                        <input id="e2bLocaleElementName" name="e2bLocaleElementName" class="form-control" disabled="disabled"
                               data-evt-mouseOver='{"method": "xmlMouseOver", "params": []}'>
                    </div>

                    <div class="col-md-4">
                        <label><g:message code="app.label.template.xml.e2bLocale" /></label>
                        <g:select name="e2bLocale" id="e2bLocale"
                                  from="${LocaleHelper.buildLocaleSelectListAsPerUserLocale(userService.currentUser.preference.locale.toString(), true)}"
                                  optionValue="display"
                                  optionKey="lang_code"
                                  class="form-control"/>
                    </div>
                </div>
            </g:if>
        </div>
        <!-- Tab panes -->
        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="tagPropertiesPanel">
                <div class="form-group">
                    <label><g:message code="app.label.template.xml.template" /></label>

                    <div class="row">
                        <div class="${editable ? 'col-lg-12' : 'col-lg-11'}">
                            <g:select from="${[]}" noSelection="['': message(code: 'select.operator')]" name="selectCLL" class="form-control selectCLL"/>
                        </div>

                        <g:if test="${!editable}">
                            <div class="col-lg-1">
                                <a href="#" target="_blank" id="selectCllId"><g:message
                                        code="app.label.view"/></a>
                            </div>
                        </g:if>
                    </div>

                    <label><g:message code="app.label.template.xml.filterBy" /></label>
                    <g:select from="${[]}" noSelection="['': message(code: 'select.operator')]" name="filterFieldInfo" id="filterFieldInfo" class="form-control selectField"/>
                </div>
                <div style="display:none;">
                    <div id="builderAll" class="builderAll doneLoading">
                    </div>
                </div>
            </div>

            <div role="tabpanel" class="tab-pane" id="sourceFieldPanel">
                <div class="form-group">
                    <label><g:message code="app.label.template.xml.field" /></label> <i style="cursor:pointer;" class="fa fa-edit pencilOptionSelectedInTag"></i>
                    <g:select from="${[]}" noSelection="['': message(code: 'select.operator')]" name="reportFieldInfo" id="reportFieldInfo" class="form-control selectField"/>
                </div>
%{--                TODO need to add UI handling--}%
                <div class="form-group hide">
                    <label><g:message code="app.label.template.xml.dateFormat" /></label>
                    <g:select name="dateFormat"
                              from="${ViewHelper.getXMLNodeDateFormatI18n()}"
                              optionValue="display"
                              optionKey="name"
                               noSelection="['':'Select one']"
                              class="form-control node-field"/>
                </div>
            </div>

            <div role="tabpanel" class="tab-pane" id="staticValuePanel">
                <div class="form-group">
                    <label><g:message code="app.label.template.xml.value" /></label> <i style="cursor:pointer;color:red;" class="fa fa-edit pencilOptionSelectedInTag"></i>
                    <g:textField id="value" name="value" class="form-control node-field"/>
                </div>
            </div>
        <div class="form-group forField" style="display:none;">
            <label><g:message code="app.label.template.xml.sourceFieldLabel" /> </label>
            <g:if test="${isExecuted}">
                <input class="form-control" disabled id="sourceFieldLabelVal">
            </g:if>
            <g:else>
            <select name="sourceFieldLabel" id="sourceFieldLabel" class="form-control">
                <option value=""></option>
                <g:set var="groups" value="${PvcmFieldLabel.withNewSession {PvcmFieldLabel.findAll()?.findAll{it}?.groupBy{it?.section}}}"/>
                <g:each var="group" in="${groups?.keySet()?.sort()}">
                    <optgroup label="${group}">
                    <g:each var="item" in="${groups[group]?.sort()}">
                        <option value="${item?.id}">${item?.displayText}</option>
                    </g:each>
                    </optgroup>
                </g:each>
            </select>
            </g:else>
        </div>
        </div>
    </div>
</div>
<g:hiddenField name="rootNode" value="${reportTemplateInstance?.rootNode as JSON}"/>
<g:render template="/includes/widgets/deleteWarningWithoutJustification" />
