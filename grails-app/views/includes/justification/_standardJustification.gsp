<div style="margin-top: 15px; display: flex">
    <span id="justification-ja-box" style="display: none; width: 100%; margin-right: 10px;">
        <label>${justificationLabel}<span id="req-indicator-standard-justification-ja" class="required-indicator" style="display: none">*</span>:</label>
        <textarea rows="3" placeholder="<g:message code="placeholder.justification.label"/>" class="form-control justification-control-ja textarea-countable" name="${justificationJaId}" id="${justificationJaId}" maxlength="${maxlength}" ></textarea>
        <div class="textarea-counter-box text-right" style="font-size: 11px;">
            <span class="textarea-counter-value">0</span><span>/${maxlength}</span>
        </div>
    </span>

    <span style="width: 100%;">
        <label>${justificationLabel}<span id="justification-label-postfix" style="display: none">&nbsp;(英語)</span><span id="req-indicator-standard-justification" class="required-indicator">*</span></label>:
        <textarea rows="3" placeholder="<g:message code="placeholder.justification.label"/>" class="form-control justification-control textarea-countable" name="${justificationId}" id="${justificationId}"  maxlength="${maxlength}" ></textarea>
        <div class="textarea-counter-box text-right" style="font-size: 11px;">
            <span class="textarea-counter-value">0</span><span>/${maxlength}</span>
        </div>
    </span>
</div>

<div style="display: none" id="standard-justifications-box">
    <label for="standard-justifications"><g:message code="app.select.standard.justification.label"/></label>
    <div>
        <select class="form-control" id="standard-justifications"></select>
    </div>
</div>