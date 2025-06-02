<sec:ifAnyGranted roles="ROLE_DEV">
    <g:link action="exportJson"><i class='pull-right md-file-download md rxmain-dropdown-settings' style="font-size: 15px;padding-top:4px;" title="Download json file"></i></g:link>
</sec:ifAnyGranted>
<sec:ifAnyGranted roles="ROLE_SYSTEM_CONFIGURATION">
    <i class='pull-right md fieldProfileUpload md-upload md-lg rxmain-dropdown-settings m-r-2' title="Upload json file"></i>
    <g:form action="importJson" style="display: none" name="importJSONForm" enctype="multipart/form-data" method="post">
        <input type="file" name="importJSONFile" id="importJSONFile" style="display: none" accept="application/JSON"/>
    </g:form>
    <g:javascript>
        $(".fieldProfileUpload").on('click', function (e) {
            e.preventDefault();
            $("form#importJSONForm #importJSONFile").trigger('click');
        });
        $("form#importJSONForm #importJSONFile").on('change', function (e) {
            e.preventDefault();
            $('form#importJSONForm').submit();
        });
    </g:javascript>
</sec:ifAnyGranted>