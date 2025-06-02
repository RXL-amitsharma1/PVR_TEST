package com.rxlogix.enums

public enum SetOperatorEnum {
   INTERSECT('Intersect'), UNION('Union'), EXCEPT('Except')

    private final String val

    SetOperatorEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.setOperator.${this.name()}"
    }

}
