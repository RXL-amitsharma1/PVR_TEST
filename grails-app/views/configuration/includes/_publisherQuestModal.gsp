<div id="questModal" class="modal fade " role="dialog">
    <div class="modal-dialog modal-lg">

        <!-- Modal content-->
        <div class="modal-content">


            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h5 class="modal-title modalHeader"><g:message code="app.label.PublisherTemplate.questionnaire"/></h5>
            </div>

            <div class="modal-body">

                <label><span  class="questQuestion"></span></label>

                <hr>
                <div class="row">
                    <label><g:message code="app.label.PublisherTemplate.chooseAnswer" default="Choose Answer"/></label>
                    <select class="questAnswers form-control"></select>
                </div>

                <div class="row m-t-10">
                <label><g:message code="app.label.PublisherTemplate.value" default="Value"/></label>
                <textarea class="form-control questValue" disabled="disabled" style="background: transparent"></textarea>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary questSave" ><g:message code="app.update.button.label"/></button>
                <button type="button" data-dismiss="modal" class="btn pv-btn-grey" ><g:message code="default.button.cancel.label"/></button>
            </div>

        </div>

    </div>
</div>