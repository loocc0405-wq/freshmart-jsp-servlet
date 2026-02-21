package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.exception.AuthenticationException;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.PasswordUtil;

public class AuthService {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepo = new UserRepository();

    public User login(String username, String password) {
        return executor.execute(em -> {
            User u = userRepo.findByUsername(em, username)
                    .orElseThrow(() -> new AuthenticationException("Invalid username/password"));
            if (!PasswordUtil.matches(password, u.getPasswordHash())) {
                throw new AuthenticationException("Invalid username/password");
            }
            return u;
        });
    }
}
