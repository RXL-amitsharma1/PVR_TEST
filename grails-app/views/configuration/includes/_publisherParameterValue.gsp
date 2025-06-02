<%@ page import="grails.converters.JSON; com.rxlogix.config.publisher.PublisherTemplateParameter; com.rxlogix.config.publisher.PublisherTemplate" %>

<div class="publisherContainer pvpOnly" style="display: none">

    <g:select name="${publisherParameterName}Select"
                   value="${publisherTemplate?.id}"
                   from="${[]}"
                   noSelection="['': '']"
                   class="form-control publisherTemplateSelect"/>

    <table width="100%" class="table ${publisherParameterName}Table">
        <thead>
        <tr>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.parameter" default="Parameter"/></label></th>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.title"/></label></th>
            <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/></label> <span class="glyphicon glyphicon-question-sign modal-link-" style="cursor:pointer" data-toggle="modal" data-target="#publisherHelpModal"></span></th>
        </tr>
        </thead>
        <tbody>
        <g:if test="${publisherTemplate}">
            <g:each in="${PublisherTemplate.get(publisherTemplate?.id)?.parameters?.findAll { it.type != PublisherTemplateParameter.Type.CODE }?.sort { it.name }}" var="parameter">
                <g:if test="${parameter.hidden}">
                    <tr class="parameterRow" style="display: none">
                </g:if>
                <g:else>
                    <tr class="parameterRow">
                </g:else>
                    <td class='name'>${parameter.name}</td>
                    <td>${parameter.title} <span class="fa fa-info-circle" title="${parameter.description?:""}"></span></td>
                    <td class="composerButtonContainer"><textarea style='height: 35px;' name="parameterValue" class="form-control value ${parameter.type == PublisherTemplateParameter.Type.QUESTIONNAIRE ? "editQuest" : ""}">${parameterValues? ((parameterValues as Map)[parameter.name]?:""):""}</textarea>
                        <span  class="composerButton" ><g:message code="app.pvp.composer.composeParam"/></span>
                    </td>
                    <input type='hidden' name='parameterName' value='${parameter.name}'/>
                    <input type='hidden' class='parameterTitle' value='${parameter.title}'/>
                    <input type='hidden' class='parameterValue' value='${parameter.value}'/>
                </tr>
            </g:each>
        </g:if>
        </tbody>
    </table>
    <input type="hidden" class="publisherParameterInput" name="${publisherParameterName}">
    <input type="file" class="file_input" name="publisherTemplateFile" accept="application/vnd.openxmlformats-officedocument.wordprocessingml.document" style="display: none;">

</div>
<hr class="pvpOnly" style="display:none;border-top: 2px solid #eee;">