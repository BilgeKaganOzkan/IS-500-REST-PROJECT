package com.is550.lmsrestclient.variables;

public enum ReturnType {

    OK("ok");
    private final String value;

    ReturnType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReturnType fromValue(String v) {
        for (ReturnType c: ReturnType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

