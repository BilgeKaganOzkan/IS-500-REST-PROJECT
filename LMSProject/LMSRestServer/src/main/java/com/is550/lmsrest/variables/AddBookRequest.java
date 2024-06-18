package com.is550.lmsrest.variables;

public class AddBookRequest {

    protected long loginId;
    protected AddBook addBook;

    public long getLoginId() {
        return loginId;
    }
    public void setLoginId(long value) {
        this.loginId = value;
    }
    public AddBook getAddBook() {
        return addBook;
    }
    public void setAddBook(AddBook value) {
        this.addBook = value;
    }

}