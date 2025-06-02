<%@ page import="com.rxlogix.config.publisher.PublisherCommonParameter" %>
<div class="row form-group">
    <div class="col-md-6">
        <label for="name"><g:message code="app.label.name"/><span class="required-indicator">*</span></label>
        <input required id="name" name="name" value="${instance?.name}" class="form-control emailField" maxlength="${PublisherCommonParameter.constrainedProperties.name.maxSize}"/>

        <label for="value" class="m-t-10"><g:message code="app.label.PublisherTemplate.PublisherTemplateParameter.value"/><span class="required-indicator">*</span></label>
        <span class="glyphicon glyphicon-question-sign modal-link-" style="cursor:pointer" data-toggle="modal" data-target="#publisherHelpModal"></span>
        <input required id="value" name="value" value="${instance?.value}" class="form-control emailField" maxlength="${PublisherCommonParameter.constrainedProperties.value.maxSize}"/>
    </div>

    <div class="col-md-6">
            <label for="description"><g:message code="app.label.description"/></label>
        <textarea id="description" name="description" value="${instance?.description}" rows="4" class="form-control emailField" maxlength="${PublisherCommonParameter.constrainedProperties.description.maxSize}">${instance?.description}</textarea>
    </div>
</div>
<g:render template="/publisherTemplate/includes/publisherHelp"/>