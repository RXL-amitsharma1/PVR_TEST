<div role="tabpanel" class="tab-pane " id="attachmentTab">

    <div class="rxmain-container-content ">
        <g:render template="/periodicReport/includes/configurationAttchment" model="[attachments: executedConfigurationInstance?.attachments, executedConfigurationInstanceId: executedConfigurationInstance?.id, showAttachmentWarning: showAttachmentWarning, publisherMode: true]"/>
    </div>

</div>