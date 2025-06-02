<%@ page import="com.rxlogix.Constants" %>

<g:if test="${params.pagination == 'off'}">
  <g:if test="${theInstanceListTotal >= Constants.Search.MAX_SEARCH_RESULTS_EVENTS}">
    <div><g:message code="app.label.onlyTheFirst"/><b>${Constants.Search.MAX_SEARCH_RESULTS_EVENTS}</b> <g:message code="app.label.recordsShown"/></div>
  </g:if>
</g:if>

<g:else>
  <g:if test="${theInstanceListTotal >= maxPageResults}">
    <div><g:message code="app.label.shownInBatchesOf"/> <b>${maxPageResults}</b> <g:message code="app.label.records"/></div>
  </g:if>
</g:else>
