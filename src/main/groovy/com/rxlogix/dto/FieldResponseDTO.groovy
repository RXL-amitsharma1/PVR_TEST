package com.rxlogix.dto

class FieldResponseDTO {
    Integer resultCode
    String resultStatus
    String resultMsg
    Map<String , String> result

    public Map<String, String> getResult() {
        return result
    }

    public void setResult(Map<String, String> result) {
        this.result = result
    }

    public Integer getResultCode() {
        return resultCode
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode
    }

    public String getResultStatus() {
        return resultStatus
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus
    }

    public String getresultMsg() {
        return resultMsg
    }

    public void setresultMsg(String resultMsg) {
        this.resultMsg = resultMsg
    }
    public void setFailureResponse(Integer resultCode, String resultStatus, String resultMsg, Map result) {
        this.resultCode = resultCode
        this.resultStatus = resultStatus
        this.resultMsg = resultMsg
        this.result = result

    }

    public void setSuccessResponse(Integer resultCode, String resultStatus, String resultMsg, Map result) {
        this.resultCode = resultCode
        this.resultStatus = resultStatus
        this.resultMsg = resultMsg
        this.result = result
    }
}
