package com.wsc.security;

public record LoginRequest(String username, String password, String role) {}
