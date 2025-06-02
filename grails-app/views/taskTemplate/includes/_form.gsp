<%@ page import="com.rxlogix.config.TaskTemplate" %>
<div class="row form-group">
    <div class="${hasErrors(bean: taskTemplateInstance, field: 'name', 'has-error')} col-lg-3">
        <label for="name"><g:message code="app.label.task.template.name" /><span class="required-indicator">*</span></label>
        <g:textField name="name" value="${taskTemplateInstance?.name}" class="form-control taskTemplateField" maxlength="${TaskTemplate.constrainedProperties.name.maxSize}"/>
    </div>
</div>
<g:render template="includes/tasks" model="['taskTemplateInstance' : taskTemplateInstance]" />
<g:hiddenField id="taskSize" class="taskSize" name="taskSize" value="${taskTemplateInstance?.tasks?.size() ? taskTemplateInstance?.tasks?.size() : 1}" />
<g:hiddenField name="type" value="${com.rxlogix.enums.TaskTemplateTypeEnum.REPORT_REQUEST.name()}" />
<g:hiddenField name="id" value="${taskTemplateInstance.id}" />
<g:hiddenField id="mode" name="mode" value="${mode}" />
