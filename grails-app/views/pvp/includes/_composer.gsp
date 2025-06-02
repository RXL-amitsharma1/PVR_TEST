<%@ page import="com.rxlogix.config.ExecutedPublisherSource" %>
<div class="modal fade" id="composerModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel1">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel1"><g:message code="app.pvp.composer.compose"/></h4>
            </div>
            <div class="modal-body">
                <div class="alert alert-warning alert-dismissible forceLineWrap unableToParseWarning" role="alert" style="display: none">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only"><g:message code="default.button.close.label"/></span>
                    </button>
                    <i class="fa fa-warning"></i>
                    <g:message code="app.pvp.composer.warning"/>
                </div>
                <g:message code="app.pvp.composer.fetchFrom"/>
                <select id="composer1" class="form-control">
                    <option></option>
                    <option value="report"><g:message code="app.pvp.composer.currentReport"/></option>
                    <option value="previous"><g:message code="app.pvp.composer.previousReport"/></option>
                </select>
                <g:message code="app.pvp.composer.from"/>
                <select id="composer2" class="form-control">
                    <option></option>
                    <option value="section"><g:message code="app.pvp.composer.section"/></option>
                    <option value="source"><g:message code="app.pvp.composer.source"/></option>
                    <option value="doc"><g:message code="app.pvp.composer.doc"/></option>
                </select>
                <g:message code="app.pvp.composer.withName"/>
                <select id="composer3" class="form-control">
                    <option></option>
                </select>
                as
                <select id="composer4" class="form-control">
                    <option></option>
                    <option type="section" value="table"><g:message code="app.pvp.composer.table"/></option>
                    <option type="section" value="chart"><g:message code="app.pvp.composer.chart"/></option>
                    <option type="section" value="cell"><g:message code="app.pvp.composer.cell"/></option>
                    <option type="section" value="range"><g:message code="app.pvp.composer.range"/></option>
                    <option type="section" value="data"><g:message code="app.pvp.composer.data"/></option>

                    <option type="source" sourceType="EXCEL,PDF,WORD,IMAGE" value="content"><g:message code="app.pvp.composer.content"/></option>
                    <option type="source" sourceType="WORD" value="text"><g:message code="app.pvp.composer.text"/></option>
                    <option type="source" sourceType="WORD" value="paragraph"><g:message code="app.pvp.composer.paragraph"/></option>
                    <option type="source" sourceType="WORD" value="bookmark"><g:message code="app.pvp.composer.bookmark"/></option>
                    <option type="source" sourceType="EXCEL" value="cell"><g:message code="app.pvp.composer.cell2"/></option>
                    <option type="source" sourceType="EXCEL" value="range"><g:message code="app.pvp.composer.range2"/></option>
                    <option type="source" sourceType="PDF" value="img"><g:message code="app.pvp.composer.img"/></option>
                    <option type="source" sourceType="EXCEL,JSON,XML" value="data"><g:message code="app.pvp.composer.data"/></option>


                    <option type="doc" sourceType="word" value="text"><g:message code="app.pvp.composer.text2"/></option>
                    <option type="doc" sourceType="word" value="paragraph"><g:message code="app.pvp.composer.paragraph2"/></option>
                    <option type="doc" sourceType="word" value="bookmark"><g:message code="app.pvp.composer.bookmark2"/></option>

                </select>
                <span id="composerCellParams" class="composerParams" style="display: none"> <g:message code="app.pvp.composer.with"/>
                    <div class="row"><div class="col-lg-3"><g:message code="app.pvp.composer.rowNum"/></div><div class="col-lg-2"><input id="composer5" class="composerParamsValue form-control" width="50px"> </div>
                        <div class="col-lg-3"><g:message code="app.pvp.composer.colNum"/></div><div class="col-lg-2"><input id="composer6" class="form-control composerParamsValue " width="50px"></div> </div>
                    <g:message code="app.pvp.composer.cellInfo"/>
                </span>
                <span id="composerRangeParams" class="composerParams" style="display: none"> <g:message code="app.pvp.composer.with"/>
                    <div class="row">
                        <div class="col-lg-3"><g:message code="app.pvp.composer.fromRowNum"/><input id="composer7" class="form-control composerParamsValue " style="width: 50px;display: inline"> </div>
                        <div class="col-lg-3"><g:message code="app.pvp.composer.fromColNum"/><input id="composer8" class="form-control composerParamsValue "  style="width: 50px;display: inline"></div>
                        <div class="col-lg-3"><g:message code="app.pvp.composer.toRowNum"/><input id="composer9" class="form-control composerParamsValue "  style="width: 50px;display: inline"> </div>
                        <div class="col-lg-3"><g:message code="app.pvp.composer.toColNum"/><input id="composer10" class="form-control composerParamsValue "  style="width: 50px;display: inline"></div>
                    </div>
                    <g:message code="app.pvp.composer.rangeInfo"/>
                </span>
                <span id="composerTextParams" class="composerParams" style="display: none"> <g:message code="app.pvp.composer.with"/>
                    <div class="row">
                        <div class="col-lg-2"><g:message code="app.pvp.composer.startsFrom"/></div><div class="col-lg-4"><input id="composer11" class="form-control composerParamsValue " > </div>
                        <div class="col-lg-2"><g:message code="app.pvp.composer.endsTo"/></div><div class="col-lg-4"><input id="composer12"  class="form-control composerParamsValue " width="50px"></div>
                        <div class="col-lg-6">
                            <div class="checkbox checkbox-primary checkbox-inline">
                                <g:checkBox id="includingStartsFrom"  name="includingStartsFrom" checked="true"/>
                                <label for="includingStartsFrom"><g:message code="app.pvp.composer.incStW"/></label>
                            </div>
                        </div>
                        <div class="col-lg-6">
                            <div class="checkbox checkbox-primary checkbox-inline">
                                <g:checkBox id="includingEndsWith"  name="includingEndsWith" checked="true"/>
                                <label for="includingEndsWith"><g:message code="app.pvp.composer.incEndW"/>  </label>
                            </div>
                        </div>
                    </div>

                </span>
                <span id="composerImgParams" class="composerParams" style="display: none"> <g:message code="app.pvp.composer.with"/>
                    <div class="row"><div class="col-lg-3"><g:message code="app.pvp.composer.fromPage"/></div><div class="col-lg-2"><input id="composer13" class="composerParamsValue form-control" width="50px"> </div>
                        <div class="col-lg-3"><g:message code="app.pvp.composer.toPage"/></div><div class="col-lg-2"><input id="composer14" class="form-control composerParamsValue " width="50px"></div> </div>
                    <g:message code="app.pvp.composer.imgInfo"/>

                </span>
                <br>
                <br>
                <div class="row" style="padding: 5px; border: #cccccc solid 1px"><div class="col-lg-2"><g:message code="app.pvp.composer.result"/></div>
                    <div class="col-lg-10" >
                        <input class="form-control" id="parameterValueResult" disabled>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="app.button.close"/></button>
                <button type="button" id="composerSave"  class="btn btn-primary"><g:message code="app.actionPlan.saveChanges"/></button>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="commonPublisherSources" value="${ExecutedPublisherSource.findAllByConfigurationIsNull()?.collect{it.name}?.join("@")}">
