<%@ page import="grails.converters.JSON; com.rxlogix.config.ReportRequestField; com.rxlogix.config.UserDictionary; com.rxlogix.enums.ReportRequestFrequencyEnum; com.rxlogix.config.ReportRequest; com.rxlogix.Constants; com.rxlogix.util.ViewHelper; com.rxlogix.enums.PriorityEnum; com.rxlogix.enums.StatusEnum;com.rxlogix.user.UserGroup; com.rxlogix.user.User; com.rxlogix.enums.CommentTypeEnum; com.rxlogix.util.DateUtil; com.rxlogix.config.WorkflowState" %>
<div class="row">
    <g:set var="customValues" value="${reportRequestInstance?.customValues ? JSON.parse(reportRequestInstance?.customValues)?.collectEntries{k,v->[(k):v]} : [:]}"/>
    <g:each var="field" in="${ReportRequestField.findAllByIsDeletedAndSection(false, section)?.sort { it.index }}" status="i">
        <div class="col-md-${field.width ?: "3"} ${field.masterPlanningRequest ? "masterPlanningRequest" : ""} ${field.reportRequestType ? (" reportRequestTypeSpecific reportRequestType_" + field.reportRequestType?.id) : ""}">
            <g:if test="${field.fieldType == ReportRequestField.Type.STRING}">
                <label>${field.label}</label>
                <input id="customValue.${field.name}" ${field.disabled?'readonly=readonly':''} name="customValue.${field.name}" class="form-control" value="${customValues?.get(field.name) ?: ""}">
            </g:if>
            <g:elseif test="${field.fieldType == ReportRequestField.Type.LONG}">
                <label>${field.label}</label>
                <input id="customValue.${field.name}" ${field.disabled?'readonly=readonly':''} name="customValue.${field.name}" class="form-control" type="number" value="${customValues?.get(field.name) ?: ""}">
            </g:elseif>
            <g:elseif test="${field.fieldType == ReportRequestField.Type.DATE}">
                <label>${field.label}</label>
                <g:if test="${field.disabled}">
                    <input id="customValue.${field.name}" readonly=readonly name="customValue.${field.name}" class="form-control" value="${customValues?.get(field.name) ?: (new Date().format("dd-MMM-yyyy"))}">
                </g:if>
                <g:else>
                <div class="fuelux ">
                    <div>
                        <div class="datepicker toolbarInline customFieldDatepicker">
                            <div class="input-group">
                                <g:textField id="customValue.${field.name}" name="customValue.${field.name}" placeholder="${message(code: "select.date")}" class="form-control fuelux date reportRequest "
                                             value="${customValues?.get(field.name) ?: ""}"/>
                                <g:render class="datePickerIcon" template="/includes/widgets/datePickerTemplate"/>
                            </div>
                        </div>
                    </div>
                </div>
                </g:else>
            </g:elseif>
            <g:elseif test="${field.fieldType == ReportRequestField.Type.BOOLEAN}">
                <label></label>
                <div class="checkbox checkbox-primary ">
                    <input type="checkbox" ${field.disabled?'readonly=readonly':''} name="customValue.${field.name}" ${customValues?.get(field.name) ? "checked" : ""}/>
                    <label for="customValue.${field.name}">${field.label}</label>
                </div>
            </g:elseif>
            <g:elseif test="${field.fieldType in [ReportRequestField.Type.SELECT,ReportRequestField.Type.CASCADE]}">
                <label>${field.label}</label>
                <select name="customValue.${field.name}" id="customValue.${field.name}" class="form-control reportRequest select2-box">
                    <option value=""></option>
                    <g:each in="${field.allowedValues?.split(";")?.collect { it.trim() }}" var="opt">
                        <option value="${opt}" ${customValues?.get(field.name) == opt ? 'selected="selected"' : ''}>${opt}</option>
                    </g:each>
                </select>
                <g:if test="${field.fieldType == ReportRequestField.Type.CASCADE}">
                    <script>
                        $(function () {
                            $(document).on("change","#customValue\\.${field.name}", function(){
                                var val = $(this).val()
                                $(".secondaryFieldOption-${field.name}").addClass("hide");
                                $(".option-${field.name}-"+val).removeClass("hide");
                                var firstvalue = $($("#customValue\\.secondary${field.name}").find("option[value^='"+val+"~']")[0]).attr("value");
                                $("#customValue\\.secondary${field.name}").select2("val", firstvalue);
                            } );
                            var val = $("#customValue\\.${field.name}").select2("val");
                            $(".secondaryFieldOption-${field.name}").addClass("hide");
                            $(".option-${field.name}-"+val).removeClass("hide");
                        })
                    </script>
                </g:if>
            </g:elseif>
            <g:elseif test="${field.fieldType == ReportRequestField.Type.LIST}">
                <label>${field.label}</label>
                <select  name="customValue.${field.name}" id="customValue.${field.name}" class="form-control reportRequest select2-box" multiple>
                    <g:each in="${field.allowedValues?.split(";")?.collect { it.trim() }}" var="opt">
                        <option value="${opt}" ${(customValues?.get(field.name) == opt) || (opt in customValues?.get(field.name)?.split(";")) ? 'selected="selected"' : ''}>${opt}</option>
                    </g:each>
                </select>
            </g:elseif>
            <g:elseif test="${field.fieldType == ReportRequestField.Type.TEXTAREA}">
                <label>${field.label}</label>
                <textarea id="customValue.${field.name}" name="customValue.${field.name}" rows="4" class="form-control">${customValues?.get(field.name) ?: ""}</textarea>
            </g:elseif>
            <g:if test="${field.jscript}">
                <script>
                    $(function () {
                        ${raw(field.jscript)}
                    });
                </script>
            </g:if>

        </div>
        <g:if test="${field.fieldType == ReportRequestField.Type.CASCADE}">
            <div class="col-md-${field.width?:"3"} ${field.masterPlanningRequest ? "masterPlanningRequest" : ""}">
                <label>${field.secondaryLabel}</label>


                <select name="customValue.secondary${field.name}" id="customValue.secondary${field.name}" class="form-control reportRequest select2-box" >
                    <option value="" class="secondaryFieldOption-${field.name}  option-${field.name}-empty"></option>
                    <g:each in="${JSON.parse(field.secondaryAllowedValues?:"{}")}" var="valueSet">
                        <g:each in="${valueSet.value?.split(";")?.collect { it.trim() }}" var="opt">
                            <option value="${valueSet.key+"~"+opt}" class="secondaryFieldOption-${field.name}  option-${field.name}-${valueSet.key}" ${(customValues?.get("secondary"+field.name) == (valueSet.key+"~"+opt)) ? 'selected="selected"' : ''}>${opt}</option>
                        </g:each>
                    </g:each>
                </select>

            </div>
        </g:if>
    </g:each>
</div>