<%@ page import="com.rxlogix.config.SourceProfile; com.rxlogix.config.ReportField; com.rxlogix.util.ViewHelper; com.rxlogix.enums.TemplateTypeEnum" %>
<input type="hidden" id="isExecuted" value="${isExecuted}" />
<div class="margin20Top row" id="templateSourceProfileDiv">
    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.CASE_LINE}">
        <g:render template="includes/caseLineListing"
                  model="['reportTemplateInstance': reportTemplateInstance, 'editable': editable, currentUser: currentUser, selectedLocale: selectedLocale, sourceProfiles: sourceProfiles]"/>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.DATA_TAB}">
        <div class="tab-pane" id="dataTabulation">
            <g:render template="includes/dataTabulation"
                      model="['reportTemplateInstance': reportTemplateInstance, 'editable': editable, currentUser: currentUser, selectedLocale: selectedLocale, sourceProfiles: sourceProfiles]"/>
        </div>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.CUSTOM_SQL}">
        <div class="tab-pane" id="customSQL">
            <g:render template="includes/customSQL" model="['template': reportTemplateInstance, 'editable': editable, ciomsITemplate: ciomsITemplate, 'isExecuted':isExecuted]"/>
        </div>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.NON_CASE}">
        <div class="tab-pane" id="nonCase">
            <g:render template="includes/nonCase" model="['template': reportTemplateInstance, 'editable': editable, 'isExecuted':isExecuted]"/>
        </div>
    </g:if>

    <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.TEMPLATE_SET}">
        <div class="tab-pane" id="templateSet">
            <g:render template="includes/templateSet" model="['reportTemplateInstance': reportTemplateInstance, 'editable': editable, isExecuted: isExecuted]"/>
        </div>
    </g:if>

    <rx:showXMLOption>
        <g:if test="${reportTemplateInstance.templateType == TemplateTypeEnum.ICSR_XML}">
            <div class="tab-pane" id="XML">
                <g:render template="includes/XML" model="['reportTemplateInstance': reportTemplateInstance, 'editable': editable, currentUser: currentUser, isExecuted: isExecuted]"/>
            </div>
        </g:if>
    </rx:showXMLOption>
</div>


%{-- //todo:  Push the items below into the specific template where they are used--}%
<g:hiddenField name="templateType" value="${reportTemplateInstance.templateType.key}"/>
