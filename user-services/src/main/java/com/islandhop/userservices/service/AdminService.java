package com.islandhop.userservices.service;
import com.islandhop.userservices.model.AdminAccount;

public interface AdminService {
    String getEmailFromIdToken(String idToken);
    boolean isAdmin(String email);
    AdminAccount createAdminAccount(String email);
}