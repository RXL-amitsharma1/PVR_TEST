import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.WorkflowState

databaseChangeLog = {

	changeSet(author: "Meenakshi (generated)", id: "1474442185996-1") {
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'SHARED_WITH', columnName: 'STATUS')
		}
		dropColumn(columnName: "STATUS", tableName: "SHARED_WITH")
	}
}
