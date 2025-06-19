package com.islandhop.userservices.service;

public interface AdminService {
    String getEmailFromIdToken(String idToken);
    boolean isAdmin(String email);
}