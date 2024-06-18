package com.is550.lmsrest.variables;

public class AddBorrowBookRequest {

    protected long loginId;
    protected BorrowBook borrowBook;

    public long getLoginId() {
        return loginId;
    }
    public void setLoginId(long value) {
        this.loginId = value;
    }
    public BorrowBook getBorrowBook() {
        return borrowBook;
    }
    public void setBorrowBook(BorrowBook value) {
        this.borrowBook = value;
    }

}
