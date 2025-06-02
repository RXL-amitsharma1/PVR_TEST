<g:if test="${warn}">
    <div class="alert alert-warning alert-dismissible forceLineWrap" role="alert">
        <button type="button" class="close" data-dismiss="alert">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-warning"></i>
            ${warn?.decodeHTML()}
    </div>
</g:if>