<%@ page import="com.rxlogix.enums.DateRangeEnum; com.rxlogix.util.ViewHelper" %>
<div class="modal fade" id="addSectionModal"  data-backdrop="static" tabindex="-1" role="dialog"
     aria-labelledby="addSectionModalLabel" aria-hidden="true">

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