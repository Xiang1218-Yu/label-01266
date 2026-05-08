package com.rental.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginUserTest {

    @Test
    @DisplayName("全参构造")
    void allArgsConstructor() {
        LoginUser loginUser = new LoginUser(1L, "admin");

        assertEquals(1L, loginUser.getUserId());
        assertEquals("admin", loginUser.getUsername());
    }

    @Test
    @DisplayName("无参构造和Setter")
    void noArgsConstructor() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(2L);
        loginUser.setUsername("user");

        assertEquals(2L, loginUser.getUserId());
        assertEquals("user", loginUser.getUsername());
    }
}
