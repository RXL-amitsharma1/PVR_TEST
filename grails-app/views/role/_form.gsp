<%@ page import="com.rxlogix.user.Role" %>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div class="row">
    <div class="col-md-6">

        <div class="form-group">
            <label class="col-md-4 control-label" for="authority">
                <g:message code="role.authority.label"/><span class="required-indicator">*</span>
            </label>

            <div class="col-md-8">
                <g:textField name="authority" maxlength="${Role.constrainedProperties.authority.maxSize}"
                             value="${roleInstance?.authority}"
                             placeholder="${message(code: 'role.authority.label')}"
                             class="form-control"/>
            </div>
        </div>

        <div class="form-group">
            <label class="col-md-4 control-label" for="description"><g:message code="role.description.label"/></label>

            <div class="col-md-8">
                <g:textArea name="description" value="${roleInstance?.description}"
                            rows="5" cols="40" maxlength="${Role.constrainedProperties.description.maxSize}"
                            placeholder="${message(code: 'role.description.label')}"
                            class="form-control"/>
                <small class="text-muted">Max: ${Role.constrainedProperties.description.maxSize} characters</small>

            </div>
        </div>

    </div>

</div>