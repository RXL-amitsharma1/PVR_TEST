<div style="width: 100%"><g:textArea name="description" value="${value}"  autocomplete="off"
            maxlength="${maxlength}" placeholder="${message(code: 'fieldProfile.description.label')}"
            class="form-control" style="width: calc(100% - 23px);height: 24px;float: left"/>
    <i class="fa fa-edit descriptionEditModaIcon" style="cursor:pointer;margin-left: 3px;margin-top: 5px;font-size: 19px;"></i>
</div>


<div class="modal fade" id="descriptionEditModal" data-backdrop="static" tabindex="-1" role="dialog" aria-labelledby="descriptionEditModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteWarningModalLabel">
                    <g:message code="app.label.reportDescription"/>
                </h4>
            </div>

            <div class="modal-body">
                <textarea maxlength="${maxlength}" class="form-control modalDescription withCharCounter" style="width:100%;" rows="6"></textarea>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey" data-dismiss="modal"><g:message code="default.button.cancel.label"/></button>
                <button type="button" class="btn btn-primary submitChanges"><g:message code="default.button.save.label"/></button>
            </div>
        </div>
    </div>
</div>
<script>
    $(".descriptionEditModaIcon").click(function () {
        var descriptionElement = $(this).parent().find("textarea");
        var modal = $("#descriptionEditModal");
        var modalDescription = modal.find(".modalDescription");
        modalDescription.val(descriptionElement.val());
        modal.modal("show");
        modal.find('.submitChanges').off().click(function () {
            descriptionElement.val(modalDescription.val());
            modal.modal("hide");
        });
    });
</script>