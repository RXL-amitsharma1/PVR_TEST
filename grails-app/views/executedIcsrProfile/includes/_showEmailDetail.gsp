<%@ page import="com.rxlogix.util.ViewHelper" %>
<div class="row">
    <div class="col-xs-12">
        <label><g:message code="app.label.emailTo"/></label>
        <g:if test="${deliveryOption?.emailToUsers}">
            <g:each in="${deliveryOption?.emailToUsers}">
                <div>${it}</div>
            </g:each>
        </g:if>
        <g:else>
            <div>
                <g:message code="app.label.none"/>
            </div>
        </g:else>
    </div>
</div>

<g:if test="${!instance.emailConfiguration?.isDeleted && instance.emailConfiguration?.cc}">
    <div class="row">
        <div class="col-xs-12">
            <label><g:message code="app.label.emailConfiguration.cc"/></label>
            <g:each in="${instance.emailConfiguration?.cc?.split(',')}">
                <div>${it}</div>
            </g:each>
        </div>
    </div>
</g:if>
