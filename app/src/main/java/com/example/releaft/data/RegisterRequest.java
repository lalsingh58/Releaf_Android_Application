package com.example.releaft.data;

public class RegisterRequest {
    private final String full_name;
    private final String email;
    private final String password;

    public RegisterRequest(String fullName, String email, String password) {
        this.full_name = fullName;
        this.email = email;
        this.password = password;
    }
}