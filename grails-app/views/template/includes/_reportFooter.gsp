<div class="row">
    <div class="col-xs-12">
        <label>${message(code: "placeholder.templateQuery.footer")}</label>
    </div>
</div>

<div class="row" style="margin-bottom: 15px;">
    <g:if test="${readonly}">
        <g:templateFooterSelect name="templateFooter" value="${reportTemplateInstance?.templateFooter}" class="form-control footerSelect" style="width: 100%"
                        readonly="${readonly}" disabled="true"/>
    </g:if>
    <g:else>
        <g:templateFooterSelect name="templateFooter" value="${reportTemplateInstance?.templateFooter}" class="form-control footerSelect" style="width: 100%"
                        readonly="${readonly}" />
    </g:else>
</div>
