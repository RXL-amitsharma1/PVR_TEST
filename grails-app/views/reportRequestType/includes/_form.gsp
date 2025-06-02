<%@ page import="com.rxlogix.config.UserDictionary; com.rxlogix.config.PeriodicReportConfiguration; com.rxlogix.config.ReportRequestType; com.rxlogix.enums.ReportActionEnum;com.rxlogix.config.ReportRequestField" %>
<div class="row ">
    <div class="col-lg-3">
        <label for="name">
            <g:if test="${reportRequestTypeInstance instanceof ReportRequestField}">
                <g:message code="app.reportRequest.customField.name" default="Name (no space or special characters allowed)"/>
            </g:if>
            <g:else>
                <g:message code="app.label.reportRequestType.name"/>
            </g:else>
            <span class="required-indicator">*</span>
        </label>
        <input name="name" id="name" value="${reportRequestTypeInstance?.name}" maxlength="${ReportRequestType.constrainedProperties.name.maxSize}" ${reportRequestTypeInstance instanceof ReportRequestField ? raw('pattern="[a-zA-Z0-9_]*"') : ""}
               class="form-control reportRequestTypeField">
    </div>
    <g:if test="${reportRequestTypeInstance instanceof ReportRequestType}">

        <g:if test="${aggEditable}">
            <div class="col-lg-3" style="padding-top: 30px;">
                <div class="checkbox checkbox-primary" style="margin-top:33px">
                    <g:checkBox name="aggregate" value="${reportRequestTypeInstance?.aggregate}"
                                checked="${reportRequestTypeInstance?.aggregate}"/>
                    <label for="aggregate">
                        <g:message code="app.configurationType.PERIODIC_REPORT"/>
                    </label>
                </div>
            </div>
        </g:if>
        <g:else>
            <div class="col-lg-3" style="padding-top: 15px;">
                <div class="checkbox checkbox-primary" style="margin-top:23px">
                    <g:checkBox name="aggregate1" value="${reportRequestTypeInstance?.aggregate}" disabled="true"
                                checked="${reportRequestTypeInstance?.aggregate}"/>
                    <label for="aggregate1">
                        <g:message code="app.configurationType.PERIODIC_REPORT"/>
                    </label><br>
                    <span style="color: #777777; font-size: 10px"><g:message code="app.label.reportRequestType.aggWarn" default="This parameter cannot be changed because there are Report Requests of this type."/></span>
                    <input type="hidden" name="aggregate" value="${reportRequestTypeInstance?.aggregate ? 'on' : ''}"/>
                </div>
            </div>
        </g:else>
    </g:if>
</div>
<g:if test="${reportRequestTypeInstance instanceof ReportRequestType}">
    <div class="row">
        <div class="col-lg-6">
            <label for="name"><g:message code="app.PeriodicReport.configuration.template.label"/></label>
            <select class="form-control select2-box" name="configuration" id="configuration">
                <g:each var="t" in="${PeriodicReportConfiguration.findAllByIsDeletedAndIsTemplate(false, true)}">
                    <option ${reportRequestTypeInstance?.configurationId == t.id ? "selected" : ""} value="${t.id}">${t.reportName}</option>
                </g:each>
            </select>
        </div>
    </div>
</g:if>
<g:if test="${!(reportRequestTypeInstance instanceof ReportRequestField)}">
    <div class="row">
        <div class="col-lg-6">
            <label for="description"><g:message code="app.label.reportRequestType.description"/></label>
            <g:if test="${(params.type== 'PSR_TYPE_FILE') || (params.type== 'INN') || (params.type == 'DRUG')}">
                <g:textArea name="description" maxlength="${com.rxlogix.config.UserDictionary.constrainedProperties.description.maxSize}" value="${reportRequestTypeInstance?.description}" class="form-control reportRequestTypeField"/>
            </g:if>
            <g:else>
            <g:textArea name="description" value="${reportRequestTypeInstance?.description}" maxlength="${ReportRequestType.constrainedProperties.description.maxSize}" class="form-control reportRequestTypeField"/>
            </g:else>
        </div>
    </div>
</g:if>



<g:if test="${reportRequestTypeInstance instanceof ReportRequestField}">
    <div class="row">

        <div class="col-lg-3">
            <label for="label"><g:message code="app.label.reportRequestField.label"/><span class="required-indicator">*</span>
            </label>
            <input name="label" id="label" value="${reportRequestTypeInstance?.label}" maxlength="${ReportRequestField.constrainedProperties.label.maxSize}" class="form-control"/>
        </div>
        <div class="col-lg-3 cascade">
            <label for="secondaryLabel"><g:message code="app.label.reportRequestField.labelForSecondField"/><span class="required-indicator">*</span>
            </label>
            <input name="secondaryLabel" id="secondaryLabel" value="${reportRequestTypeInstance?.secondaryLabel}" class="form-control" />
        </div>
        <div class="col-lg-4" style="margin-top: 10px;">
            <div class="checkbox checkbox-primary">
                <g:checkBox name="masterPlanningRequest" value="${reportRequestTypeInstance?.masterPlanningRequest}"
                            checked="${reportRequestTypeInstance?.masterPlanningRequest}"/>
                <label for="masterPlanningRequest">
                    <g:message code="app.label.reportRequestField.master"/>
                </label>
            </div>
            <div class="checkbox checkbox-primary" >
                <g:checkBox name="showInPlan" value="${reportRequestTypeInstance?.showInPlan}"
                            checked="${reportRequestTypeInstance?.showInPlan}"/>
                <label for="showInPlan">
                    <g:message default="Show on Aggregate Reports Planning page" code="app.label.reportRequestType.showInPlan"/>
                </label>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-2">
            <label for="section"><g:message code="app.label.reportRequestField.section"/><span class="required-indicator">*</span>
            </label>
            <g:select id="section" name="section" required="true"
                      optionKey="name" optionValue="display" value="${reportRequestTypeInstance?.section?.name()?:ReportRequestField.Section.ADDITIONAL.name()}"
                      from="${ReportRequestField.Section.getI18List()}"
                      class="form-control select2-box"/>
        </div>
        <div class="col-lg-1">
            <label for="index"><g:message code="app.label.reportRequestField.index"/></label>
            <input name="index" id="index" value="${reportRequestTypeInstance?.index ?: 0}" type="number"
                   min="${ReportRequestField.constrainedProperties.index.min}" max="${ReportRequestField.constrainedProperties.index.max}"
                   class="form-control reportRequestTypeField natural-number">
        </div>
        <div class="col-lg-2">
            <label for="width"><g:message code="app.label.reportRequestField.width"/></label>
            <input name="width" style="width: 150px" id="width" value="${reportRequestTypeInstance?.width ?: 3}" type="number"
                   min="${ReportRequestField.constrainedProperties.width.min}" max="${ReportRequestField.constrainedProperties.width.max}"
                   class="form-control reportRequestTypeField natural-number">
        </div>


    </div>
    <div class="row">

        <div class="col-lg-3">
            <label for="fieldType"><g:message code="app.label.reportRequestField.type"/><span class="required-indicator">*</span>
            </label>
            <g:select id="fieldType" name="fieldType"
                      data-placeholder="${message(code: 'select.operator')}"
                      optionKey="name" optionValue="display"
                      value="${reportRequestTypeInstance?.fieldType ? reportRequestTypeInstance?.fieldType?.name() : ""}"
                      from="${ReportRequestField.Type.getI18List()}"
                      class="form-control select2-required select2-box"/>
        </div>

        <div class="col-lg-3">
            <label for="reportRequestType"><g:message code="auditLog.domainObject.ReportRequestType"/>
            </label>
            <g:select id="reportRequestType" name="reportRequestType" data-placeholder="Any"
                      optionKey="id" optionValue="name"
                      noSelection="['': message(code: 'app.label.all')]"
                      value="${reportRequestTypeInstance?.reportRequestType?.id}"
                      data-value="${reportRequestTypeInstance?.reportRequestType ? reportRequestTypeInstance?.reportRequestType?.id : ''}"
                      from="${ReportRequestType.findAllByIsDeleted(false)}"
                      class="form-control"/>
        </div>

    </div>
    <div class="row">
        <div class="col-lg-6">
            <label for="allowedValues"><g:message code="app.label.reportRequestField.allowedValues"/><span class="required-indicator allowedValuesIndicator">*</span>
            </label>
            <g:textArea name="allowedValues" placeholder="${message(code: "app.label.reportRequestField.allowedValues.placeholder")}" value="${reportRequestTypeInstance?.allowedValues}" maxlength="${ReportRequestField.constrainedProperties.allowedValues.maxSize}" class="form-control multiline-text"/>
        </div>
    </div>
    <div class="row cascade" style="margin-top: 20px">
    <div class="col-lg-6">
    <a class="btn btn-primary formCascadeInputs"><g:message code="app.label.reportRequestField.createCascade"/></a>
        <input type="hidden" name="secondaryAllowedValues" id="secondaryAllowedValues" value="${reportRequestTypeInstance?.secondaryAllowedValues}">
        <div class="cascadeContent"></div>
    </div>
    </div>
    <div class="row">
        <div class="col-lg-6">
            <label for="jscript"><g:message code="app.label.reportRequestField.jscript"/></label>
            <br>
            <small class="form-text text-muted " style="font-size:1em;"><g:message code="app.reportRequestField.jscript.note"/></small>
            <g:textArea name="jscript" rows="10" value="${reportRequestTypeInstance?.jscript}" maxlength="${ReportRequestField.constrainedProperties.jscript.maxSize}" class="form-control multiline-text"/>
                <div class="checkbox checkbox-primary" style="margin-top:133px">
                    <g:checkBox name="disabled" value="${reportRequestTypeInstance.disabled}"
                                checked="${reportRequestTypeInstance?.disabled}"/>
                    <label for="disabled">
                        <g:message code="app.label.reportRequestField.disabled"/>
                    </label>
                </div>

        </div>
    </div>
</g:if>
<input type="hidden" name="type" value="${params.type}">
