<%@ page import="com.rxlogix.Constants" %>

<script type="text/javascript">

  //This is done as a GSP include/render vs. pure Javascript externalization due to the remoteFunction call.

  function refreshTable(){

    var maxResultsPerPage = ${Constants.Search.MAX_SEARCH_RESULTS};

    ${remoteFunction(action: 'listRefresh', method: "get",
                               update: "auditLogSearchResultsTableDiv",
                               params: '''\'username=\'               + $(\'#username\').val() +
                                          \'&category=\'              + $(\'#category\').val() +
                                          \'&fromDate=\'              + $(\'#fromDate\').val() +
                                          \'&toDate=\'                + $(\'#toDate\').val() +
                                          \'&max=\'                   + maxResultsPerPage
                                       ''',
                               before: "showSpinnerMessage()",
                               onComplete: 'clearSpinnerMessage()'
    )}

  }

</script>