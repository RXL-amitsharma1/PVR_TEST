<%@ page import="com.rxlogix.enums.TemplateTypeEnum" %>

<div class="row">
    <div class="col-xs-3">
        <div class="bs-callout bs-callout-info" style="margin: 0px;">
        </div>
    </div>

    <div class="col-xs-5" id="templateSetContainer">
        <g:each in="${reportTemplateInstance?.nestedTemplates}" var="cll" status="index">
            <g:render template="templateSetCLL"
                      model="['cll': cll, 'index': index, 'editable': editable, isExecuted: isExecuted]"/>
        </g:each>
    </div>

    <div hidden="hidden"><g:render template="templateSetCLL" model="['index': '']"/></div>
    <g:hiddenField name="templateSetNestedIds"/>

    <div class="col-xs-1">
        <input type="button" class="btn btn-primary" id="addCLL" value="${message(code: "default.button.add.label")}"
               autocomplete="off">
    </div>

    <div class="col-xs-3">
        <div class="checkbox checkbox-primary">
            <g:checkBox name="excludeEmptySections" value="${reportTemplateInstance?.excludeEmptySections}"/>
            <label for="excludeEmptySections">
                <g:message code="app.templateSet.exclude.empty.sections"/>
            </label>
        </div>

        <div class="checkbox checkbox-primary">
            <g:checkBox name="linkSectionsByGrouping" value="${reportTemplateInstance?.linkSectionsByGrouping}"/>
            <label for="linkSectionsByGrouping">
                <g:message code="app.templateSet.link.sections.by.grouping"/>
            </label>
        </div>

        <div class="checkbox checkbox-primary">
            <g:checkBox name="sectionBreakByEachTemplate"
                        value="${reportTemplateInstance?.sectionBreakByEachTemplate}"/>
            <label for="sectionBreakByEachTemplate">
                <g:message code="app.templateSet.section.break.by.each.template"/>
            </label>
        </div>
    </div>
</div>
