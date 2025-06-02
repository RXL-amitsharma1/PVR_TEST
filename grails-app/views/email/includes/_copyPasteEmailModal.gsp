<asset:javascript src="/app/emailModal.js" />
<script>
    var emailAddAllUrl="${createLink(controller: 'email', action: 'ajaxAddAll')}";
</script>
<style>
    #copyAndPasteEmailModal.modal {
        /* due to this modal will be opened from another one need to set z-index greater than default modal z-index */
        z-index: 1051 !important;
    }
</style>
<div class="modal fade copyAndPasteEmailModal" id="copyAndPasteEmailModal" role="dialog" aria-labelledby="Copy/Paste Dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-evt-clk='{"method": "closeAllCopyPasteModals", "params": []}' aria-label="Close">
                    <span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel"><g:message code="paste.values" />:</h4>
            </div>
            <div class="modal-body container-fluid">
                <div class="row">
                    <label><g:message code="app.delimiters" />:</label>
                </div>
                <div class="row" id="delimiter-options">
                    <div class="icon-col" title="No delimiters">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value="none" checked="checked"/>
                            <g:message code="app.label.none"/>
                        </label>
                    </div>
                    <div class="icon-col" title="comma">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value=","/>
                            <g:message code="app.label.comma"/>
                        </label>
                    </div>
                    <div class="icon-col" title="semi-column">
                        <label class="no-bold add-cursor">
                            <input type="radio" id="semicolon-delimiter" name="delimiter" value=";"/>
                            <g:message code="app.label.semi.colon"/>
                        </label>
                    </div>
                    <div class="icon-col" title="space">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value=" "/>
                            <g:message code="app.label.space"/>
                        </label>
                    </div>
                    <div class="icon-col" title="new-line">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value="\n"/>
                            <g:message code="app.label.new.line"/>
                        </label>
                    </div>
                    <div class="icon-col" title="Others">
                        <label class="no-bold add-cursor">
                            <input type="radio" name="delimiter" value="others"/>
                            <g:message code="app.others"/>
                        </label>
                    </div>
                    <div class="icon-col">
                        <input type="text" class="c_n_p_other_delimiter" style="width: 80px;">
                    </div>
                </div>
                <div class="row content-row">
                    <textarea class="copyPasteContent" style="border: 0;"></textarea>
                </div>
                <div class="row">
                    <div id="emailCopyPasteError" class="alert alert-danger alert-dismissible forceLineWrap" role="alert" style="display: none; margin-right: 20px;">
                        <button type="button" class="close" id="closeErrorJustificaton">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <div id="emailCopyPasteErrorContent"></div>
                    </div>
                    <div id="emailCopyPasteWarn" class="alert alert-warning alert-dismissible forceLineWrap" role="alert" style="display: none; margin-right: 20px;">
                        <button type="button" class="close" data-dismiss="alert">
                            <span aria-hidden="true">&times;</span>
                            <span class="sr-only"><g:message code="default.button.close.label"/></span>
                        </button>
                        <div id="emailCopyPasteWarnContent"></div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey cancel" data-evt-clk='{"method": "closeAllCopyPasteModals", "params": []}'><g:message
                        code="default.button.cancel.label"/></button>
                <button type="button" class="btn btn-success confirm-paste-email-values"><g:message code="default.button.confirm.label"/></button>
            </div>
        </div>
    </div>
</div>