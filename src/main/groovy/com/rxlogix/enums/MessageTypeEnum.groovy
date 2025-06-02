package com.rxlogix.enums

public enum MessageTypeEnum {
    ICHICSR("ichicsr"),
    PSUR("psur"),
    BACKLOG("backlog"),
    BACKLOGCT("backlogct"),
    MASTER("master"),
    RECODED("recoded"),
    MASTER_RECODED("master recoded")

    private final String val

    MessageTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.messageType.${this.name()}"
    }

    static List<MessageTypeEnum> getApplicableList(){
        return [ICHICSR]
    }

}