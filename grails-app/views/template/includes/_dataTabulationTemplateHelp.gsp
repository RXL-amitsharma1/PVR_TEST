
<div class="modal fade datatabulationHelp" id="datatabulationHelp" tabindex="-1" role="dialog" aria-labelledby="Data Tabulation Help">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <span><b><g:message code="app.data.tabulation.label" /></b></span>
            </div>
            <div class="modal-body container-fluid">
                <div>
                    <g:message code="app.data.tabulation.description.help" />
                </div>
                <br>
                <div>
                    <div><g:message code="app.data.tabulation.attributes.description.help" />:</div>
                    <div><b><g:message code="app.label.rows" />:</b> <g:message code="app.data.tabulation.row.help" /></div>
                    <div><b><g:message code="app.label.columns" />:</b> <g:message code="app.data.tabulation.column.help" /></div>
                    <div><b><g:message code="app.label.measures" />:</b> <g:message code="app.data.tabulation.measures.help" /></div>
                </div>
                <br>
                <div>
                    <g:message code="app.data.tabulation.type.help" />:
                </div>

                    <!-- Accordion Starts -->
                    <div class="accordion">
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="collapse" href="#simpleDataTabulation">
                                    <g:message code="app.data.tabulation.simple.help" />
                                </a>
                            </div>
                            <div id="simpleDataTabulation" class="accordion-body collapse in">
                                <div class="accordion-inner">
                                    <img id="simpleDTImage" height="500" width="850"
                                         src="${asset.assetPath(src: 'SimpleDataTabulation.jpg')}" alt="spinner"/>
                                </div>
                            </div>
                        </div>
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="collapse" href="#stackedColumnsTabulation">
                                    <g:message code="app.data.tabulation.stacked.help" />
                                </a>
                            </div>
                            <div id="stackedColumnsTabulation" class="accordion-body collapse">
                                <div class="accordion-inner">
                                    <img id="stackedColumnImg" height="500" width="850"
                                         src="${asset.assetPath(src: 'StackedColumn.jpg')}" alt="spinner"/>
                                </div>
                            </div>
                        </div>
                        <div class="accordion-group">
                            <div class="accordion-heading">
                                <a class="accordion-toggle" data-toggle="collapse" href="#sideWaysColumn">
                                    <g:message code="app.data.tabulation.sideways.help" />
                                </a>
                            </div>
                            <div id="sideWaysColumn" class="accordion-body collapse">
                                <div class="accordion-inner">
                                    <img id="sideWaysColumnImg" height="500" width="850"
                                         src="${asset.assetPath(src: 'SideWaysColumn.jpg')}" alt="spinner"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Accordion Ends -->


            </div>
            <div class="modal-footer">
                <button type="button" class="btn pv-btn-grey cancel" data-dismiss="modal">Ok</button>
            </div>
        </div>
    </div>
</div>