package com.albalatefs.backend.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
