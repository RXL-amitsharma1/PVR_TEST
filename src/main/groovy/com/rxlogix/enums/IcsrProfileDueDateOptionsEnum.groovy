package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil

enum IcsrProfileDueDateOptionsEnum {
    
    INCLUDE_HOLIDAY_ONLY,
    INCLUDE_WEEKEND_ONLY,
    INCLUDE_HOLIDAY_AND_WEEKEND,
    DO_NOT_ADJUST
    
    
    public getI18nKey() {
        return "app.label.due.Date.Options.${this.name()}"
    }
    
    String getKey() {
        name()
    }
    
    static List<IcsrProfileDueDateOptionsEnum> getAsList() {
        
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        return values().collect { [id: it.key, name: customMessageService.getMessage(it.getI18nKey())] }
    }
}