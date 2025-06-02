<%@ page import="com.rxlogix.util.DateUtil; com.rxlogix.util.ViewHelper" %>
<g:renderLongFormattedDate date="${date}" showTimeZone="${showTimeZone}" timeZone="${g.getCurrentUserTimezone()}"/>