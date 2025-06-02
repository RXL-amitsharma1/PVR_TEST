<div style="margin-bottom: 20px;">
  <b><g:formatNumber number="${theInstanceListTotal}" /></b>
  <g:if test="${theInstanceListTotal == 1}">
    <g:message code="app.label.record"/>
  </g:if>
  <g:else>
    <g:message code="app.label.records"/>
  </g:else>
</div>
