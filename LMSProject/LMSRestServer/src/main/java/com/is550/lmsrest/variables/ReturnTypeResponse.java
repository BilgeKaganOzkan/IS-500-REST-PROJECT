package com.is550.lmsrest.variables;

public class ReturnTypeResponse {

    protected ReturnType returnVal;
    protected Long returnLongVal;

    public ReturnType getReturnVal() {
        return returnVal;
    }
    public Long getReturnLongVal() {
        return returnLongVal;
    }
    public void setReturnVal(ReturnType value) {
        this.returnVal = value;
    }
    public void setReturnLongVal(Long value) {
        this.returnLongVal = value;
    }

}
