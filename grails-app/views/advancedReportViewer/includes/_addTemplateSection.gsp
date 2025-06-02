<%@ page import="com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<style>
td.remove .removeSectionIconBtn {
    cursor: pointer;
}
</style>
<asset:javascript src="app/addSection.js"/>
<asset:javascript src="app/addTemplateSection.js"/>
<asset:javascript src="app/expandingTextarea.js"/>

<div class="modal fade" id="addTemplateSectionModal" data-keyboard="false"  data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="addTemplateSectionModalLabel" aria-hidden="true">

    <div class="vertical-alignment-helper">
        <!-- Modal Dialog starts -->
        <div class="modal-dialog modal-lg vertical-align-center">

            <div class="modal-content">

            </div><!-- modal-content ends-->
        </div><!-- modal-dialog ends-->
    </div>

    <div style="display: none">
        <g:render template="/query/customSQLValue"/>
        <g:render template="/query/poiInputValue"/>
        <g:render template="/query/toAddContainerQEV"/>
    </div>
</div><!-- modal -->