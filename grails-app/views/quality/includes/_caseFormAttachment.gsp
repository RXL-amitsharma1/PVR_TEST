<rx:container title="${message(code: 'app.label.attachments')}">
    <div style="position: absolute; width: 100%; height:100%; background: #0c7cd5; display: none; opacity: 0" id="ifremeHover" ></div>
    <div class="row">
        <div class="alert alert-danger alert-dismissible forceLineWrap errorDiv" role="alert" hidden="hidden">
            <button type="button" class="close" name="errorsCountCloseButton">
                <span aria-hidden="true">&times;</span>
                <span class="sr-only"><g:message code="default.button.close.label"/></span>
            </button>
            <p class="errorContent"></p>
        </div>
        <div style="width: 100%;">
            <select style="display: inline-block; width: calc(100% - 100px);" class="form-control" id="files" data-attachment-size="${attachmentsList?.size()}" disabled="disabled">
                <g:if test="${attachmentsList?.size()==0}">
                    <option>${message(code: 'qualityModule.noSource.documents')}</option>
                </g:if>
                <g:else>
                    <g:each in="${attachmentsList}" var="file">
                        <g:if test="${file.notes != '' && file.notes != null}">
                            <option value="${file.id}" data-filename="${file.fileName}">${file.fileName} (${file.notes})</option>
                        </g:if>
                        <g:else>
                            <option value="${file.id}" data-filename="${file.fileName}">${file.fileName}</option>
                        </g:else>
                    </g:each>
                </g:else>
            </select>

            <button style="width: 95px;" class="btn btn-primary showDocument" disabled="disabled">${message(code: 'app.label.show')}</button>
        </div>
    </div>
    <iframe style="width: 100%;"></iframe>
</rx:container>