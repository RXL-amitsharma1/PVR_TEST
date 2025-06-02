<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.rxlogix.config.Tag" %>


<label><g:message code="app.label.tag"/></label>
<div>
    <g:select id="tags" name="tags"
              from="${Tag.getAllExcludingQualityTags()}"
              value="${domainInstance?.tags?.name}"
              optionKey="name"
              optionValue="name"
              multiple="true"
              class="form-control"/>
</div>

<script>
    $(function () {
        var isAdmin = ${SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")};
        showTagWidget(isAdmin);
    });

</script>