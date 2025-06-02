<g:if test="${mode != 'show'}" >
    <div class="buttonBar">
        <span name="edit" class="btn btn-primary add-task taskTemplateField">
            <g:message code="app.label.task.template.addTasks" />
        </span>
    </div>
</g:if>
<br/>
<table id="taskListTable" class="table">
    <thead>
    <tr>
        <th></th>
        <th style="width: 50%;padding-left: 20px;"><label>Task</label><span class="required-indicator">*</span></th>
        <th style="width: 20%;padding-left: 20px;"><label>Priority</label><span class="required-indicator">*</span></th>
        <th style="width: 30%;padding-left: 20px;"><label>Due Date</label></th>
    </tr>
    </thead>
    <tbody id="tasksBody">
       <g:each var="task" in="${taskTemplateInstance?.tasks}" status="i">
          <g:render template="includes/task" model="['i':i, 'task' :task]" />
       </g:each>
       <g:if test="${taskTemplateInstance?.tasks == null || taskTemplateInstance?.tasks?.size() == 0}">
          <g:render template="includes/task"  model="['i':0, 'task' :new com.rxlogix.config.Task()]" />
       </g:if>
    </tbody>
</table>
