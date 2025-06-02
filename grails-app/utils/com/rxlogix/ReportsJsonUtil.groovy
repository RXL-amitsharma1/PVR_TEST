package com.rxlogix

import com.rxlogix.util.MiscUtil


class ReportsJsonUtil {

    public static getNameFieldFromSelectionJson(jsonString) {

        def prdName = ""
        if (jsonString) {
            if (!isJsonString(jsonString))
                prdName = jsonString
            else {
                def jsonObj = MiscUtil.parseJsonText(jsonString?: "")
                def prdVal = jsonObj.find {k,v->
                    v.find { it.containsKey('name')}
                }.value.findAll{
                    it.containsKey('name')
                }.collect {it.name}

                prdName = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdName
    }

    public static isJsonString(String str) {
        try {
            MiscUtil.parseJsonText(str)

            return true
        } catch (Throwable t) {
        }

        return false
    }
}
