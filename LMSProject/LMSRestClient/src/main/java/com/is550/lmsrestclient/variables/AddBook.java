package com.is550.lmsrestclient.variables;

public class AddBook {

    protected String name;
    protected String author;
    protected BookType type;
    protected String location;

    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String value) {
        this.author = value;
    }
    public BookType getType() {
        return type;
    }
    public void setType(BookType value) {
        this.type = value;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String value) {
        this.location = value;
    }
}
