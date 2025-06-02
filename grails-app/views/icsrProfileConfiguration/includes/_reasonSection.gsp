<%@ page import="com.rxlogix.util.ViewHelper;" %>
<tr class="reasonRow${i}">
    <g:hiddenField name="lateReasons[${i}].deleted" id="lateReasons[${i}].deleted" value="false"/>
    <th style="vertical-align: middle;max-width:30px" class="pull-right"><label style="font-size: 19px;"><span title="Most Important Reason" class="fa fa-exclamation-circle"></span></label></th>
    <td style="vertical-align: middle;max-width:120px">
        <g:select id='lateReasons[${i}].responsibleParty' name='lateReasons[${i}].responsibleParty' from="${ViewHelper.getResponsiblePartyEnumI18n()}"  class="form-control select2-box responsibleParty" style="margin-left: 2px; width: 100%;"
                  optionKey="name" optionValue="display" noSelection="['':message(code: 'select.one')]"/></td>
    <td style="vertical-align: middle;max-width:120px"><g:select id='lateReasons[${i}].reason' name='lateReasons[${i}].reason' from="${ViewHelper.getReasonEnumI18n()}" class="form-control select2-box reason" style="margin-left: 2px; width: 100%;"
                                                                 optionKey="name" optionValue="display" noSelection="['':message(code: 'select.one')]"/></td>
    <td style="vertical-align: middle;max-width:30px"><span id="removeReason${i}" class="removeReason glyphicon glyphicon-remove" style="cursor: pointer;color: #700; display:none"></span></td>
</tr>