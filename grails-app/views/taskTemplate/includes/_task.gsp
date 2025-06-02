<%@ page import="com.rxlogix.config.Task; com.rxlogix.util.ViewHelper;" %>
<tr class="taksRow${i}">
    <g:hiddenField name="tasks[${i}].deleted" id="tasks[${i}].deleted" value="false"/>
    <g:hiddenField name="tasks[${i}].newObj" id="tasks[${i}].newObj" value="false"/>
    <g:hiddenField name="tasks[${i}].id" id="tasks[${i}].id" value="${task?.id}" />
    <td style="vertical-align: middle">
        <g:if test="${mode != 'show'}" >
            <span id="removeTask${i}" class="removeTask glyphicon glyphicon-remove taskTemplateField" style="cursor: pointer;color: #700; display:none"></span>
        </g:if>
    </td>
    <td>
        <g:textArea id='tasks[${i}].taskName' name='tasks[${i}].taskName' value="${task?.taskName}" class="form-control taskTemplateField" style="height: 60px;" maxlength="${Task.constrainedProperties.taskName.maxSize}"/>
    </td>
    <td style="vertical-align: middle">
        <g:select id="tasks[${i}].priority" name="tasks[${i}].priority" noSelection="${['': '']}" value="${task?.priority}" optionKey="name" optionValue="display" from="${ViewHelper.priorityEnum}" class="form-control taskTemplateField"/>
    </td>
    <td style="vertical-align: middle">
        <div class="col-lg-6" style="padding-left:1px; padding-right: 1px; vertical-align: middle">
            <g:select style="font-size: 18px" name='tasks[${i}].baseDate' id='tasks[${i}].baseDate' value="${(task?.baseDate?:Task.BaseDate.DUE_DATE).name()}"
                      from="${Task.BaseDate.i18List}" optionKey="name" optionValue="display" class="form-control"/>
        </div>
        <div class="col-lg-3" style=" vertical-align: middle">
            <g:select style="font-size: 18px" name='tasks[${i}].sign' id='tasks[${i}].sign' value="${task?.dueDateSign}" from="${['+','-']}" class="form-control"/>
        </div>
        <div class="col-lg-3" style="padding-left:1px; vertical-align: middle">
            <input type="number" min="0" data-evt-onkeyup='{"method": "checkDecimal", "params": []}' id='tasks[${i}].dueDate' name='tasks[${i}].dueDate' value="${Math.abs((task?.dueDate)?:0)}" class="form-control taskTemplateField "/>
        </div>
    </td>
</tr>
