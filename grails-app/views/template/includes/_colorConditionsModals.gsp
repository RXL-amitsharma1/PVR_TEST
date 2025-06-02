<%@ page import="com.rxlogix.enums.QueryOperatorEnum" %>
<div id="colorConditionModal" class="modal fade" data-keyboard="false" data-backdrop="static" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close m-t-0 cancelColorCondition" data-dismiss="modal">&times;</button>
                <i class="fa fa-question-circle iconHelp close" style="cursor: pointer;margin-top: 4px;margin-right: 10px;"></i>
                <h4 class="modal-title modalHeader"><g:message code="app.dataTabulation.condition"/></h4>
            </div>

            <div class="modal-body" style="min-height: 50px">
                <div>
                    <div class="row firstColorConditionRowModal colorConditionRowModal" style="margin-bottom: 10px;">
                        <div class="col-md-4">
                            <select class="form-control colorConditionFieldSelect">
                            </select>
                        </div>
                        <div class="col-md-4" style="width:32%">
                            <select class="form-control colorConditionOperatorSelect">
                                <g:each var="v" in="${QueryOperatorEnum.getNumericAndStringOperators()}">
                                    <option value="${v.name()}">${message(code:v.getI18nKey())}</option>
                                </g:each>
                            </select>
                        </div>
                        <div class="col-md-4 p-r-0" style="width:30%">
                            <input class="form-control colorConditionValueSelect">
                        </div>
                        <div>
                            <i class="md md-plus md-lg addColorConditionModal"></i>
                        </div>
                    </div>
                </div>
                <div style="border: #cccccc thin solid; border-radius: 5px; height: 120px;">
                    <div class= "conditional-toolbar" style="border-bottom: #cccccc thin solid; color: #595959;" >
                        <i class="md  md-format-bold toolbar-button boldStyle enabled md-lg" style="color:#595959"></i>
                        <i class="md md-format-italic toolbar-button italicStyle enabled md-lg" style="color:#595959"></i>
                        <i class="md md-format-underline toolbar-button underlineStyle enabled md-lg" style="color:#595959"></i>
                         <span class="svg-height">
                        <i class="toolbar-button colorTextStyle enabled"><svg xmlns="http://www.w3.org/2000/svg" enable-background="new 0 0 24 24" height="24px" viewBox="0 0 24 24" width="24px" fill="#000000"><g><rect fill="none" height="24" width="24"/></g><g><path d="M2,20h20v4H2V20z M5.49,17h2.42l1.27-3.58h5.65L16.09,17h2.42L13.25,3h-2.5L5.49,17z M9.91,11.39l2.03-5.79h0.12l2.03,5.79 H9.91z"/></g></svg></i><input type="color" style="width: 0;height: 0;opacity: 0;" id="colorTextInput">
                        <i class="toolbar-button colorBgStyle enabled"><svg xmlns="http://www.w3.org/2000/svg" enable-background="new 0 0 24 24" height="24px" viewBox="0 0 24 24" width="24px" fill="#000000"><g><rect fill="none" height="24" width="24"/></g><g><path d="M16.56,8.94L7.62,0L6.21,1.41l2.38,2.38L3.44,8.94c-0.59,0.59-0.59,1.54,0,2.12l5.5,5.5C9.23,16.85,9.62,17,10,17 s0.77-0.15,1.06-0.44l5.5-5.5C17.15,10.48,17.15,9.53,16.56,8.94z M5.21,10L10,5.21L14.79,10H5.21z M19,11.5c0,0-2,2.17-2,3.5 c0,1.1,0.9,2,2,2s2-0.9,2-2C21,13.67,19,11.5,19,11.5z M2,20h20v4H2V20z"/></g></svg></i><input type="color" style="width: 0;height: 0;opacity: 0;" id="colorBgInput">
                        </span>
                        <span>
                        <i class="md md-eraser  toolbar-button clearStyle enabled md-lg"></i>
                        </span>
                        <span class="toolbar-button htmlStyle enabled source-code">
                            <i class="md md-chevron-left md-lg"></i>
                            <i class="md md-chevron-right md-lg ml-arrow"></i>
                            <span>Source Code</span>
                        </span>
                    </div>

                    <input type="hidden" id="cellFormattingModal">
                    <input type="hidden" id="cellColorModal">
                    <div style="height: 82px;display: table;width: 100%;">
                        <div style=" display: table-cell; vertical-align: middle; width: 100%;text-align: center;" class="sampleLabel">
                            AaBbCcYyZz
                        </div>
                        <textarea style="display: none;width: 100%;height: 82px" class="cellFormattingEdit"></textarea>
                        <small class="errorWrongFormat" style="color:red;display: none"><g:message code="app.label.wrongFormat" default="Wrong Format!"/></small>
                    </div>
                </div>
            </div>
        <div style="margin: 5px;    line-height: 1;">
            <small class="text-muted"><g:message code="app.dataTabulation.typeinfo"/></small>
        </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary addColorCondition"><g:message code="app.pvc.import.Apply" default="add"/></button>
                <button type="button" class="btn btn-default cancelColorCondition" data-dismiss="modal"><g:message code="app.button.close"/></button>

            </div>
        </div>

    </div>
</div>
<div id="helpIconModal" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h5 class="modal-title modalHeader"><g:message code="app.label.help"/></h5>
            </div>
            <div class="modal-body" style="min-height: 50px">

                Use $value to place cell value in formatted string.<br>
                You can also use the following formatting tags:<br>
                &lt;b&gt;&lt;/b&gt; - bold<br>
                &lt;i&gt;&lt;/i&gt; - italic<br>
                &lt;u&gt;&lt;/u&gt; - underline<br>
                &lt;sub&gt;&lt;/sub&gt; - subscript<br>
                &lt;sup&gt;&lt;/sup&gt; - superscript<br>
                &lt;br&gt; - new line<br>
                &lt;font color="#######"  highlightcolor="#######" highlighttext="text" &gt;&lt;/font&gt;<br><br>

                <b>Example 1:</b> if cell value is "123" then format: "&lt;font color="#ff0000" &gt; &lt;b&gt;$value &lt;/b&gt;&lt;/font&gt;" will be rendered as "<span style="color: #Ff0000; font-weight:bold;">123</span>"<br><br>
                <b>Example 2:</b> if cell value is "some text" then format: "&lt;font highlighttext="ex" highlightcolor="#ff0000"&gt;$value&lt;/font&gt;" will be rendered as "some t<span style="color: #Ff0000;">ex</span>t"<br><br>
                You can use Font Awesome icons.  Font Awesome  supported for html and pdf format only. To use icon - use "\" and it hex number. Below frequently used icons:
                <table width="100%" class="table">
                    <tr>
                        <td> <i class="fa fa-info" >\f129</i></td>
                        <td><i class="fa fa-info-circle">\f05a</i></td>
                        <td><i class="fa fa-long-arrow-up">\f176</i></td>
                        <td><i class="fa fa-long-arrow-down ">\f175</i></td>
                        <td><i class="fa fa-exclamation-triangle">\f071</i></td>
                        <td><i class="fa fa-exclamation-circle ">\f06a</i></td>
                    </tr>
                    <tr>
                        <td><i class="fa fa-bolt ">\f0e7</i></td>
                        <td><i class="fa fa-exclamation-triangle ">\f071</i></td>
                        <td><i class="fa fa-star ">\f005</i></td>
                        <td> <i class="fa fa-star-o">\f006</i></td>
                        <td> <i class="fa fa-heart-o ">\f08a</i></td>
                        <td><i class="fa fa-heartbeat ">\f21e</i></td>
                    </tr>
                    <tr>
                        <td> <i class="fa fa-heart ">\f004</i></td>
                        <td><i class="fa fa-smile-o">\f118</i></td>
                        <td>  <i class="fa fa-frown-o">\f119</i></td>
                        <td> <i class="fa fa-flag ">\f024</i></td>
                        <td><i class="fa fa-flag-checkered">\f11e</i></td>
                        <td> <i class="fa fa-flag-o ">\f11d</i></td>
                    </tr> <tr>
                    <td><i class="fa fa-certificate ">\f0a3</i></td>
                    <td><i class="fa fa-check-circle ">\f058</i></td>
                    <td><i class="fa fa-check ">\f00c</i></td>
                    <td><i class="fa fa-ban ">\f05e</i></td>
                    <td><i class="fa fa-thumbs-up">\f164</i></td>
                    <td> <i class="fa fa-thumbs-down ">\f165</i></td>
                </tr>
                </table>
                <b>Example 3:</b> if cell value is "123" then format "&lt;font color="#ff0000" &gt;\f129 &lt;/font&gt;$value" will be rendered as "<span style="color: #Ff0000; " class="fa fa-info"></span>123"<br><br>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default " data-dismiss="modal"><g:message code="app.button.close"/></button>
            </div>
        </div>

    </div>
</div>