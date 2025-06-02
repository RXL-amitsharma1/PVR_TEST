<%@ page import="com.rxlogix.util.ViewHelper;" %>
<g:set var="pvqModule" value="${ViewHelper.isPvqModule(request)}"/>
<g:set var="pvpModule" value="${ViewHelper.isPvPModule(request)}"/>
<g:set var="pvcModule" value="${ViewHelper.isPvcModule(request)}"/>
<g:set var="pvrModule" value="${!pvqModule && !pvpModule && !pvcModule}"/>
<div class="pvqLeftNav" style="display: none">
    <g:render template="/includes/layout/leftNavPVQ"/>
</div>
<div class="pvrLeftNav" style="display: none">
    <g:render template="/includes/layout/leftNavPVR"/>
</div>
<div class="pvpLeftNav" style="display: none">
    <g:render template="/includes/layout/leftNavPVP"/>
</div>
<div class="pvcLeftNav" style="display: none">
    <g:render template="/includes/layout/leftNavPVC"/>
</div>
<g:render template="/includes/layout/templateModal"  model="[:]"/>