<!-- Modal -->

%{--
Input parameters:
    (required) data-instancename : Name of the object to be deleted
    (required) data-instanceid   : Database identifier of the object
    (required) data-instancetype : Type of the object
                                   Currently there are 5 types, 'configuration', query', 'template', 'cognosReport' and 'accessControlGroup'.
                                   Additonal types may be added to i18n json files as needed.
    (optional) data-extramessage : Any additional comments you want inserted into the dialog
    (optional) data-controller: Controller name
    (optional) data-action : Action name
    (optional) data-deleteForAllAllowed : Show additional checkbox to delete report for all users

All variable must be passed to the dialog as request parameters, not in a view model map.

To include the gsp in the page, use:
<g:render template="/includes/widgets/deleteRecord"/>
--}%


<div class="modal fade" id="deleteModal"  data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="deleteModalLabel"
     style="z-index: 9999;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteModalLabel"></h4>
            </div>

            <div class="modal-body">
                <g:renderClosableInlineAlert id="deleteDlgErrorDiv" icon="warning" type="danger" forceLineWrap="true"
                        message="${g.message(code: 'app.label.justification.cannotbeblank')}" />

                <div id="nameToDelete"></div>
                <div id="displayFieldLevelCaseMsg"></div>
                <p></p>
                <div class="description" style="font-weight:bold; overflow-wrap: anywhere"></div>
                <div class="extramessage"></div>

                <div id="deleteForAllAllowed" class="checkbox checkbox-primary"  style="display: none">
                    <g:checkBox name="deleteForAll" />
                    <label for="deleteForAll" class="add-margin-bottom" style="margin-bottom: 5px;">
                        <g:message code="default.button.deleteForAll.label"/>
                    </label>
                </div>

                <label style="margin-top: 25px"><g:message code="app.label.justification"/><span class="required-indicator">*</span> :</label>
                <textarea placeholder="<g:message code="placeholder.justification.label"/>" class="form-control" name="deleteJustification" id="deleteJustification" maxlength="4000"></textarea>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button id="deleteButton" class="btn btn-danger">
                    <span class="glyphicon glyphicon-trash icon-white"></span>
                    ${message(code: 'default.button.deleteRecord.label', default: 'Delete Record')}
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<asset:javascript src="app/deleteModal.js"/>

