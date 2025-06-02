<%@ page import="groovy.json.JsonOutput" %>
<div class="row templateSetCLL" id="templateSetCLL_${index}">
    <div class="col-xs-11">
        %{--TODO No need to make as hidden as we are using non hidden only in editTemplateSet.js--}%
        <g:if test="${editable}">
            <select name="selectCLL_${index}" id="selectCLL_${index}"
                      data-data='[{"id":"${cll?.id}", "text":"${cll?.name}", "groupingColumns":[${cll?.groupingList?.reportFieldInfoList?.collect{'"'+it.reportField.name+'"'}?.join(",")}]}]'
                    data-value="${cll?.id}" class="form-control selectCLL"></select>
        </g:if>
        <g:else>
            <g:select readonly="" from="${[]}" noSelection="['': message(code: 'select.operator')]"
                      name="selectCLL_${index}" data-value="${cll?.id}" class="form-control selectCLL"/>

        </g:else>
    </div>
    <g:if test="${!editable}">
        <div class="col-xs-1">
            <g:if test="${isExecuted}">
                <g:link controller="template" action="viewExecutedTemplate" target="_blank" id="${cll?.id}"><g:message
                        code="app.label.view"/></g:link>
            </g:if>
            <g:else>
                <g:link controller="template" action="view" target="_blank" id="${cll?.id}"><g:message code="app.label.view"/></g:link>
            </g:else>
        </div>
    </g:if>
    <div class="col-xs-1" style="padding-left: 0px;">
        <i class="fa fa-times add-cursor removeCLL"></i>
    </div>
</div>