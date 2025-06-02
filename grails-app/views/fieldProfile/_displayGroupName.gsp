<style>
.rxmain-container-inner {
    overflow: hidden;
}
</style>
<div class="col-md-12">
    <h3 class="sectionHeader"><span class="headerCols"><g:message code="app.field.group.label"/></span></h3>

    <div class="row">
        <div class="col-md-12 groupNameList panel-group">
            <g:each in="${reportFieldGroupList}" status="i" var="reportFieldGroup">
                <div class="panel panel-default groupName-${reportFieldGroup?.name?.replaceAll(' ', '___')}">
                    <div class="panel-heading">
                        <h4 class="panel-title">
                            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapse${i}" data-groupname="${reportFieldGroup.name}">
                                <label class="rxmain-container-header-label"><g:message code="app.reportFieldGroup.${reportFieldGroup.name}"/></label>
                            </a>
                            <span class="editProfile pull-right">
                                <span class="reportFieldCount"></span>
                                <a class="btn btn-sm btn-primary groupNameKey edit-padding" id="${reportFieldGroup?.name?.replaceAll(' ', '___')}" data-value="groupName-${reportFieldGroup?.name?.replaceAll(' ', '___')}">
                                    <g:message code="app.edit.button.label"/>
                                </a>
                            </span>
                        </h4>
                    </div>

                    <div id="collapse${i}" class="panel-collapse collapse in p-10">
                        <div class="horizontalRuleFull"></div>
                        <div class="col-xs-3" id="fieldGroup-${reportFieldGroup?.name}-loader" style="margin-top: -10px;display: none">
                            <i class="fa fa-refresh fa-spin"></i>
                        </div>
                        <div class="chip-grid selectedFields"></div>
                    </div>
                    <div class="unSelectedFields" style="display: none;"></div>
                </div>
            </g:each>
        </div>
    </div>
</div>
<g:render template="addRemoveField"/>
<script type="text/javascript">
    $(function () {
        $('#rxTableFieldProfileContent').DataTable();

        $(document).on('click', '[data-evt-clk]', function () {
            const eventData = JSON.parse($(this).attr("data-evt-clk"));
            const methodName = eventData.method;
            if (methodName == "removeParentNode") {
                // Call the method from the eventHandlers object with the params
                $(this).closest('.demo-chip').remove()
            }
        });

    });
</script>