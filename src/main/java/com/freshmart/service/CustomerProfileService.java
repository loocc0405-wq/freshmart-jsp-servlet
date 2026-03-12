package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;

import java.time.LocalDate;

public class CustomerProfileService {

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepository = new UserRepository();

    public User getById(Long userId) {
        return executor.execute(em ->
                userRepository.findById(em, userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"))
        );
    }

    public User updateProfile(Long userId,
                              String fullName,
                              String gender,
                              String dob,
                              String phone,
                              String address) {
        return executor.execute(em -> {
            User user = userRepository.findById(em, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String cleanFullName = trimToNull(fullName);
            String cleanPhone = trimToNull(phone);
            String cleanAddress = trimToNull(address);

            if (cleanFullName == null || cleanFullName.length() < 2 || cleanFullName.length() > 100) {
                throw new IllegalArgumentException("Họ tên phải từ 2 đến 100 ký tự.");
            }

            if (cleanPhone == null || !cleanPhone.matches("^(0|\\+84)[0-9]{9,10}$")) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
            }

            if (cleanAddress != null && cleanAddress.length() > 255) {
                throw new IllegalArgumentException("Địa chỉ tối đa 255 ký tự.");
            }

            user.setFullName(cleanFullName);
            user.setPhone(cleanPhone);
            user.setAddress(cleanAddress);

            if (gender != null && !gender.isBlank()) {
                user.setGender(Enum.valueOf(com.freshmart.enums.Gender.class, gender.toUpperCase()));
            } else {
                user.setGender(null);
            }

            if (dob != null && !dob.isBlank()) {
                user.setDob(LocalDate.parse(dob));
            } else {
                user.setDob(null);
            }

            return userRepository.save(em, user);
        });
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}