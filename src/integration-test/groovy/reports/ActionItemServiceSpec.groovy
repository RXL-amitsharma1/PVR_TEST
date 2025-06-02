package reports

import com.rxlogix.config.ActionItem
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.enums.ActionItemGroupState
import com.rxlogix.enums.StatusEnum
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Specification

@Integration
@Rollback
class ActionItemServiceSpec extends Specification {

    def actionItemService

    def setup(){

    }

    def cleanup(){

    }

//    def "test action item status for drilldown"(){
//        given:
//        DrilldownCLLData cllRecord = new DrilldownCLLData(id: 1L, reportResultId: 1, cllRowData: "")
//        ActionItem actionItem = new ActionItem(id: 1, status: StatusEnum.CLOSED)
//        actionItem.save(flush: true)
//        cllRecord.addToActionItems(actionItem)
//        cllRecord.save(flush: true)
//
//        when:
//        def status = actionItemService.getActionItemStatusForDrilldownRecord(1L)
//
//        then
//        assert status == ActionItemGroupState.CLOSED.toString()
//    }
}