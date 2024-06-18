package com.is550.lmsrestclient.variables;

public class AddReturnBookRequest {

    protected long loginId;
    protected ReturnBook returnBook;

    public long getLoginId() {
        return loginId;
    }
    public void setLoginId(long value) {
        this.loginId = value;
    }
    public ReturnBook getReturnBook() {
        return returnBook;
    }
    public void setReturnBook(ReturnBook value) {
        this.returnBook = value;
    }

}