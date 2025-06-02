<%@ page import="com.rxlogix.Constants" %>

%{--Hide the return value after setting the params.max value; GSP doesn't need to display it. --}%
<div style="display: none">
    ${params.max = Constants.Search.MAX_SEARCH_RESULTS}
</div>

<g:if test="${total}">
    <g:if test="${total > params.max}">

        <div class="paginateButtons">
            <util:remotePaginate controller="${controller}"
                                 action="${action}"
                                 total="${total}"
                                 update="${update}"
                                 params="${params}"/>
        </div>
    </g:if>
</g:if>

<script>
    $('#auditLogSearchResultsTable').on('mouseover', 'tr', function () {
        $('.popoverMessage').popover({
            placement: 'right',
            trigger: 'hover focus',
            viewport: '#auditLogSearchResultsTable',
            html: true
        });
    });
</script>