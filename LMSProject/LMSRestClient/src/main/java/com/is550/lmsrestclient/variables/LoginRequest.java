package com.is550.lmsrestclient.variables;

public class LoginRequest {

    protected String email;
    protected String password;
    public String getEmail() {
        return email;
    }
    public void setEmail(String value) {
        this.email = value;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String value) {
        this.password = value;
    }
}