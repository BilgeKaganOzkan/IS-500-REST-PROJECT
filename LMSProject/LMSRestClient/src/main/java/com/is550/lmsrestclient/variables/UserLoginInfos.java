package com.is550.lmsrestclient.variables;


public class UserLoginInfos {
    protected long userId;
    protected UserType userType;

    public long getUserId() {
        return userId;
    }
    public void setUserId(long value) {
        this.userId = value;
    }
    public UserType getUserType() {
        return userType;
    }
    public void setUserType(UserType value) {
        this.userType = value;
    }
}