package com.rxlogix.enums

import com.rxlogix.CustomMessageService
import com.rxlogix.util.MiscUtil

enum IcsrProfileDueDateAdjustmentEnum {
    
    AFTER,
    BEFORE
  
    
    
    public getI18nKey() {
        return "app.label.due.Date.Adjustment.${this.name()}"
    }
    
    String getKey() {
        name()
    }
    
    static List<IcsrProfileDueDateAdjustmentEnum> getAsList() {
        
        CustomMessageService customMessageService = MiscUtil.getBean("customMessageService")
        return values().collect { [id: it.key, name: customMessageService.getMessage(it.getI18nKey())] }
    }
}