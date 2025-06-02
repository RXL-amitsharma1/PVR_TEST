<g:hasErrors bean="${theInstance}">
    <div class="alert alert-danger alert-dismissible forceLineWrap errorflashmessage" role="alert">
        <button type="button" class="close" id="errorflashmessage" data-dismiss="alert">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <g:renderErrors bean="${theInstance}" as="list" codec="HTML"/>
    </div>
</g:hasErrors>

<g:if test="${flash.message}">
    <div class="alert alert-success alert-dismissible forceLineWrap successFlashMsg" role="alert">
        <button type="button" class="close" id="successFlashMsg" data-dismiss="alert">
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-check"></i>
        <g:if test="${flash.html}">
        <g:applyCodec encodeAs="none">
            ${flash.message?.decodeHTML()?.replace("\n", "<br>")}
        </g:applyCodec>
        </g:if>
        <g:else>
        <g:each in="${flash.message?.tokenize("\n")}" var="message">
            <g:applyCodec encodeAs="HTML">${message.decodeHTML()}</g:applyCodec><br/>
        </g:each>
        </g:else>
    </div>
</g:if>

<g:if test="${flash.warn}">
    <div class="alert alert-warning alert-dismissible forceLineWrap alert-warning-flash" role="alert">
        <button type="button" class="close" data-evt-clk='{"method": "divHide", "params": [".alert-warning-flash"]}'>
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-warning"></i>
        <g:if test="${flash.warn.contains('<linkQuery>')}">
            <g:each in="${flash.warn.split('</linkQuery>')}" var="warnMessage">
                ${warnMessage.substring(0, warnMessage.indexOf('<linkQuery>'))}
                <a href="${warnMessage.substring(warnMessage.indexOf('<linkQuery>') + 11)}"><g:message
                        code="see.details"/></a>
                <br>
            </g:each>
        </g:if>
        <g:else>
            <g:each in="${flash.warn?.tokenize("\n")}" var="v">
                <g:applyCodec encodeAs="HTML">${v.decodeHTML()}</g:applyCodec><br/>
            </g:each>
        </g:else>
    </div>
</g:if>

<g:if test="${flash.error}">
    <div class="alert alert-danger alert-dismissible forceLineWrap alert-danger-flash" role="alert">
        <button type="button" class="close" data-evt-clk='{"method": "divHide", "params": [".alert-danger-flash"]}'>
            <span aria-hidden="true">&times;</span>
            <span class="sr-only"><g:message code="default.button.close.label"/></span>
        </button>
        <i class="fa fa-times-circle"></i>
        <g:if test="${flash.error.contains('<linkQuery>')}">
            ${flash.error.substring(0, flash.error.indexOf('<linkQuery>'))}
            <a href="${flash.error.substring(flash.error.indexOf('<linkQuery>') + 11)}"><g:message
                    code="see.details"/></a>
        </g:if>
        <g:else>
            <g:each in="${flash.error?.tokenize("\n")}" var="error">
                <g:applyCodec encodeAs="HTML">${error.decodeHTML()}</g:applyCodec><br/>
            </g:each>
        </g:else>
    </div>
</g:if>

