package com.is550.lmsrestclient.variables;

public enum BookType {

    BIOLOGY("biology"),
    PHYSICS("physics"),
    MATHEMATICS("mathematics"),
    SOCIAL("social");
    private final String value;

    BookType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BookType fromValue(String v) {
        for (BookType c: BookType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
