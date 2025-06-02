<%@ page import="com.rxlogix.user.FieldProfile" %>
<style type="text/css">
.headerCols {
    font-size: 20px;
}
</style>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>
<div class="rxmain-container rxmain-container-top form-border">
    <div class="rxmain-container-inner">
        <div class="rxmain-container-content rxmain-container-show">
            <div class="row">
                <div>
                    <div class="${hasErrors(bean: fieldProfileInstance, field: 'name', 'has-error')} col-xs-4">
                        <label><g:message code="field.profile.name.label"/><span class="required-indicator">*</span>
                        </label>
                        <g:textField name="name" maxlength="${FieldProfile.constrainedProperties.name.maxSize}"
                                     value="${fieldProfileInstance?.name}"
                                     placeholder="${message(code: 'field.profile.name.label')}"
                                     class="form-control"/>
                    </div>
                    %{--Description--}%
                    <label for="description" style="padding-left: 5px;padding-right: 5px"><g:message code="fieldProfile.description.label"/></label>
                    <div class="col-xs-8">
                        <g:render template="/includes/widgets/descriptionControl" model="[value: fieldProfileInstance?.description, maxlength: FieldProfile.constrainedProperties.description.maxSize]"/>
                    </div>
                </div>
                <div class="clearfix"></div>
                <g:render template="displayGroupName" model="[checkBoxParameter: checkBoxParameter, reportFieldGroupList: reportFieldGroupList]"/>
                <g:hiddenField name="blindedReportFields" id="blindedFields"/>
                <g:hiddenField name="protectedReportFields" id="protectedFields"/>
                <g:hiddenField name="hiddenReportFields" id="hiddenFields"/>
            </div>
        </div>
    </div>
</div>