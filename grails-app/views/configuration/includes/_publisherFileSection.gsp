<%@ page import="grails.converters.JSON; com.rxlogix.config.publisher.PublisherTemplateParameter; com.rxlogix.config.publisher.PublisherTemplate" %>

<div class="publisherContainer pvpOnly" style="display: none">

    <div class="row">
        <div class="col-lg-9">
            <div class="input-group" style="width: 100%;">
                <input type="text" class="form-control fileName" value="${filename}" readonly>
                <label class="input-group-btn">
                    <span class="btn btn-primary inputbtn-height" style="border-radius:0px 16px 16px 0px;">
                        <g:message code="app.label.attach"/>
                        <input type="file" class="file_input" name="publisherTemplateFile"
                               accept=".docx"
                               style="display: none;">
                    </span>
                </label>
            </div>
        </div>

        <div class="col-lg-3" style="padding-right: 10px;">
            <button class="btn btn-primary fetchButton" disabled type="button" style="line-height: 16px;"><g:message
                    code="app.label.PublisherTemplate.fetchParameters" default="Fetch Params"/></button>
        </div>
    </div>

    <div class="row m-t-10">
        <div class="alert alert-warning alert-dismissible forceLineWrap fetchParametersError" role="alert" hidden="hidden">
            <button type="button" class="close" name="pubDocErrorClose">
                <span aria-hidden="true">&times;</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <p class="errorContent"></p>
        </div>
    </div>

    <table width="100%" class="table parameterTable">
        <thead>
        <th><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.name"/></label></th>
        <th style="min-width: 220px !important;"><label><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/> <span class="glyphicon glyphicon-question-sign modal-link-" style="cursor:pointer" data-toggle="modal" data-target="#publisherHelpModal"></span></th>
        </thead>
        <tbody>
        <tr class="parameterTemplateRow" style="display: none">
            <td><input type="hidden" name="parameterName" value="" class="form-control"/><span class="parametersNameSpan"></span></td>
            <td class="composerButtonContainer"><textarea style='height: 35px;' name="parameterValue" class="form-control"></textarea>
                <span  class="composerButton" ><g:message code="app.pvp.composer.composeParam"/></span>
            </td>
        </tr>
        <g:if test="${parameters}">
            <g:each in="${parameters}" var="parameter">
                <tr class="parameterRow">
                    <td><input type="hidden" name="parameterName" value="${parameter.key}" class="form-control"/>${parameter.key}</td>
                    <td class="composerButtonContainer"><textarea style='height: 35px;' name="parameterValue" class="form-control">${parameter.value}</textarea>
                        <span  class="composerButton" ><g:message code="app.pvp.composer.composeParam"/></span>
                    </td>
                </tr>
            </g:each>
        </g:if>
        </tbody>
    </table>
    <input type="hidden" class="publisherParameterInput" name="${publisherParameterName}">

</div>
<hr class="pvpOnly" style="display:none;border-top: 2px solid #eee;">