<script>
    var listTaskUrl;
    var aggregateReportViewTaskMode=false;
</script>
<asset:javascript src="app/publisherSectionTask.js"/>
<div class="row form-group">
    <div class="${hasErrors(bean: taskTemplateInstance, field: 'name', 'has-error')} col-lg-3">
        <label for="name"><g:message code="app.label.task.template.name" /><span class="required-indicator">*</span></label>
        <g:textField name="name" value="${taskTemplateInstance?.name}" class="form-control taskTemplateField"/>
    </div>
</div>
<div class="row">
    <div class="col-xs-12">
        <g:if test="${mode != 'show'}" >
        <button type="button" class="btn btn-primary table-add addTaskTable"><g:message code="app.label.task.template.addTasks" default="Add"/></button>
        </g:if>
    </div>
</div>
<div class="row">
    <g:render template="includes/publisherSectionTaskTable" />
</div>
<g:hiddenField id="tasks"  name="tasks" value="${taskTemplateInstance.getPublisherSectionTasksAsJson()}" />
<g:hiddenField name="type" value="${com.rxlogix.enums.TaskTemplateTypeEnum.PUBLISHER_SECTION.name()}" />
<g:hiddenField name="id" value="${taskTemplateInstance.id}" />
<g:hiddenField id="mode" name="mode" value="${mode}" />
