<sec:ifAnyGranted roles="ROLE_ADMIN">
    <g:link action="create">
        <i class="pull-right md md-add md-lg icon-color m-r-2" id="createButton" title="${message(code: "app.label.icsr.organization.configuration.create")}" style="margin-top: 7px;"></i>
    </g:link>
</sec:ifAnyGranted>
<sec:ifAnyGranted roles="ROLE_DEV">
    <g:link action="exportJson"><i class='pull-right md md-export md-lg rxmain-dropdown-settings m-r-2' title="Download json file"></i></g:link>
</sec:ifAnyGranted>
<sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
    <i class='pull-right md unitConfigurationUpload md-upload md-lg rxmain-dropdown-settings m-r-2' title="Upload json file"></i>
    <g:form action="importJson" style="display: none" name="importJSONForm" enctype="multipart/form-data" method="post">
        <input type="file" name="importJSONFile" id="importJSONFile" style="display: none" accept="application/JSON"/>
    </g:form>
    <g:javascript>
        $(".unitConfigurationUpload").on('click', function (e) {
            e.preventDefault();
            $("form#importJSONForm #importJSONFile").trigger('click');
        });
        $("form#importJSONForm #importJSONFile").on('change', function (e) {
            e.preventDefault();
            $('form#importJSONForm').submit();
        });
    </g:javascript>
</sec:ifAnyGranted>