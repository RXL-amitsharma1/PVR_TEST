package com.rxlogix.enums


public enum ConfigurationTypeEnum {

    ADHOC_REPORT("Adhoc Report"),
    PERIODIC_REPORT("Periodic Report"),
    ICSR_REPORT("Icsr Report")

    final String value

    ConfigurationTypeEnum(String value){
        this.value = value
    }

    //Used to get to values for dropdown lists
    String toString(){
        value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    //Used to match up a value from the DB against the enum
    static String getValue(String theValue) {
        for (ConfigurationTypeEnum theEnum : values()){
            String name = theEnum.name();
            if (theValue == name) {
                return (theEnum.value)
            }
        }
        return ""
    }

    public getI18nKey() {
        return "app.configurationType.${this.name()}"
    }

}
