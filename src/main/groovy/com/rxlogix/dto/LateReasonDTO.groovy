package com.rxlogix.dto

import com.rxlogix.enums.ReasonEnum
import com.rxlogix.enums.ResponsiblePartyEnum

class LateReasonDTO {

    ResponsiblePartyEnum responsibleParty
    ReasonEnum reason

    //Transient fields
    boolean deleted

    LateReasonDTO(){
    }

    LateReasonDTO(def resultSet) {
        responsibleParty = resultSet.TEXT
        reason = resultSet.CODE
    }
}
