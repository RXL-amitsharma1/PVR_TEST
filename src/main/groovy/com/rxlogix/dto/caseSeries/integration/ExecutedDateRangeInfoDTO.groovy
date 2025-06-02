package com.rxlogix.dto.caseSeries.integration

import com.rxlogix.enums.DateRangeEnum

class ExecutedDateRangeInfoDTO {
    Integer relativeDateRangeValue = 1
    Date dateRangeStartAbsolute
    Date dateRangeEndAbsolute
    DateRangeEnum dateRangeEnum = DateRangeEnum.CUMULATIVE

}
