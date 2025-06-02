<div class="modal fade add-case-modal" id="addCaseModal"  data-backdrop="static" tabindex="-1" role="dialog">

    <div class="vertical-alignment-helper">

        <!-- Modal Dialog starts -->
        <div class="modal-dialog vertical-align-center">

            <div class="modal-content">
                <div class="modal-header dropdown">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <h4 class="modal-title"><g:message code="app.add.case.title.label" /></h4>
                </div>

                <div class="modal-body case-list-modal-body">
                    <div id="caseErrordiv" class="alert alert-danger hide">
                        <a href="#" class="caseJustifictaion close" aria-label="close">&times;</a>
                        <span class="errorMessageSpan"></span>
                    </div>

                    <g:uploadForm name="addNewCase">
                        <div class="row">
                            <div class="col-md-6">
                                <label for="caseNumber"><g:message code="app.caseList.caseNumber"/><span
                                        class="required-indicator">*</span></label>
                                <g:hiddenField name="executedCaseSeries.id" value="${caseSeriesId}"/>
                                <g:textField id="caseNumber" placeholder="${message(code: "placeholder.caseNumber.label")}" class="form-control"
                                             name="caseNumber" maxlength="255"/>
                            </div>

                            <div class="col-md-6">
                                <label for="versionNumber"><g:message code="app.caseList.versionNumber"/></label>
                                <g:field type="number" id="versionNumber" placeholder="${message(code: "placeholder.caseVersion.label")}" class="form-control natural-number"
                                         name="versionNumber" min="1" max="999999999999999999"  />
                            </div>
                        </div><br>

                        <div class="checkbox checkbox-primary">
                            <g:checkBox name="importCasesExcel"/>
                            <label for="importCasesExcel">
                                <g:message code="app.label.import.cases.excel"/>
                            </label>
                        </div>

                        <div id="importCasesSection" hidden="hidden">
                            <div class="row">
                                <div class="input-group col-xs-10">
                                    <input type="text" class="form-control" readonly>
                                    <label class="input-group-btn">
                                        <span class="btn btn-primary">
                                            Choose File&hellip; <input type="file" id="file_input"
                                                                       accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                                                       style="display: none;">
                                        </span>
                                    </label>
                                </div>
                            </div>

                            <div id="fileFormatError" hidden="hidden">
                                <div class="row">
                                    <div class="col-xs-12" style="color: #ff0000">
                                        <g:message code="copy.paste.modal.invalid.file.format.error"/>!
                                    </div>
                                </div>
                            </div>
                            <div id="noDataInExcel" hidden="hidden" style="color: #ff0000"></div>

                            <div class="row">
                                <div class="col-xs-11 bs-callout bs-callout-info">
                                    <h5><g:message code="app.label.note"/></h5>

                                    <div><g:message code="copy.paste.modal.import.values.from.file"/>:</div>

                                    <div><g:message code="copy.paste.modal.values.imported.from.first.worksheet"/></div>

                                    <div><g:message code="copy.paste.modal.file.have.one.column"/></div>

                                    <div><g:message code="copy.paste.modal.values.in.separate.row"/></div>

                                    <div><g:message code="copy.paste.modal.values.first.row.label"/></div>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <label for="justification"><g:message code="app.label.justification"/><span
                                        class="required-indicator">*</span></label>
                                <g:textArea id="justification" placeholder="${message(code: "placeholder.justification.label")}" class="form-control" maxlength="1000"
                                             name="justification"/>
                            </div>
                        </div>
                    </g:uploadForm>
                </div>

                <div class="modal-footer">
                    <div class="buttons creationButtons">
                        <input id="addCaseButton" type="button" class="btn btn-primary add-case-to-list" value="${message(code: "app.add.button.label")}">
                        <button type="button" class="btn pv-btn-grey close-add-case" data-dismiss="modal"><g:message
                                code="app.button.close"/></button>
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>
