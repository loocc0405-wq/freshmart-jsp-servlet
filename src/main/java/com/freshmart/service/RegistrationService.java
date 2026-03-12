package com.freshmart.service;

import com.freshmart.entity.User;
import com.freshmart.enums.Gender;
import com.freshmart.enums.Role;
import com.freshmart.enums.Tier;
import com.freshmart.repository.UserRepository;
import com.freshmart.util.JpaExecutor;
import com.freshmart.util.PasswordUtil;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RegistrationService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._-]{4,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)[0-9]{9,10}$");

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile(".*[^A-Za-z0-9].*");

    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "admin", "administrator", "root", "system", "staff",
            "seller", "support", "api", "freshmart"
    );

    private final JpaExecutor executor = new JpaExecutor();
    private final UserRepository userRepository = new UserRepository();

    public Map<String, String> validate(String fullName,
                                        String username,
                                        String email,
                                        String phone,
                                        String password,
                                        String confirmPassword,
                                        String gender,
                                        String dob,
                                        String address,
                                        String agreeTerms) {

        Map<String, String> errors = new LinkedHashMap<>();

        String cleanFullName = normalizeFullName(fullName);
        String cleanUsername = normalizeUsername(username);
        String cleanEmail = normalizeEmail(email);
        String cleanPhone = normalizePhone(phone);
        String cleanAddress = trimToNull(address);

        if (cleanFullName == null || cleanFullName.length() < 2 || cleanFullName.length() > 100) {
            errors.put("fullName", "Họ tên phải từ 2 đến 100 ký tự.");
        }

        if (cleanUsername == null || !USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            errors.put("username", "Username phải 4-30 ký tự, chỉ gồm chữ thường, số, dấu chấm, gạch dưới hoặc gạch ngang.");
        } else if (RESERVED_USERNAMES.contains(cleanUsername)) {
            errors.put("username", "Username này không được phép sử dụng.");
        }

        if (cleanEmail == null || !EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            errors.put("email", "Email không đúng định dạng.");
        }

        if (cleanPhone == null || !PHONE_PATTERN.matcher(cleanPhone).matches()) {
            errors.put("phone", "Số điện thoại phải bắt đầu bằng 0 hoặc +84 và có 10-11 số.");
        }

        if (password == null || password.length() < 8) {
            errors.put("password", "Mật khẩu phải có ít nhất 8 ký tự.");
        } else if (password.length() > 72) {
            errors.put("password", "Mật khẩu tối đa 72 ký tự.");
        } else if (!UPPERCASE_PATTERN.matcher(password).matches()
                || !LOWERCASE_PATTERN.matcher(password).matches()
                || !DIGIT_PATTERN.matcher(password).matches()
                || !SPECIAL_PATTERN.matcher(password).matches()) {
            errors.put("password", "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt.");
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.put("confirmPassword", "Xác nhận mật khẩu chưa khớp.");
        }

        if (agreeTerms == null) {
            errors.put("agreeTerms", "Bạn cần đồng ý với điều khoản sử dụng.");
        }

        if (gender != null && !gender.isBlank()) {
            try {
                Gender.valueOf(gender.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                errors.put("gender", "Giới tính không hợp lệ.");
            }
        }

        if (dob != null && !dob.isBlank()) {
            try {
                LocalDate parsedDob = LocalDate.parse(dob);
                if (parsedDob.isAfter(LocalDate.now())) {
                    errors.put("dob", "Ngày sinh không hợp lệ.");
                } else if (parsedDob.isAfter(LocalDate.now().minusYears(13))) {
                    errors.put("dob", "Người dùng phải từ 13 tuổi trở lên.");
                }
            } catch (Exception ex) {
                errors.put("dob", "Ngày sinh không hợp lệ.");
            }
        }

        if (cleanAddress != null && cleanAddress.length() > 255) {
            errors.put("address", "Địa chỉ tối đa 255 ký tự.");
        }

        return errors;
    }

    public Map<String, String> validateBusinessRules(String username, String email) {
        return executor.execute(em -> {
            Map<String, String> errors = new LinkedHashMap<>();
            String cleanUsername = normalizeUsername(username);
            String cleanEmail = normalizeEmail(email);

            if (cleanUsername != null && userRepository.existsByUsername(em, cleanUsername)) {
                errors.put("username", "Username đã tồn tại.");
            }

            if (cleanEmail != null && userRepository.existsByEmail(em, cleanEmail)) {
                errors.put("email", "Email đã được sử dụng.");
            }

            return errors;
        });
    }

    public User registerCustomer(String fullName,
                                 String username,
                                 String email,
                                 String phone,
                                 String password,
                                 String gender,
                                 String dob,
                                 String address) {

        return executor.execute(em -> {
            String cleanUsername = normalizeUsername(username);
            String cleanEmail = normalizeEmail(email);
            String cleanPhone = normalizePhone(phone);
            String cleanFullName = normalizeFullName(fullName);
            String cleanAddress = trimToNull(address);

            if (userRepository.existsByUsername(em, cleanUsername)) {
                throw new IllegalArgumentException("Username đã tồn tại.");
            }

            if (userRepository.existsByEmail(em, cleanEmail)) {
                throw new IllegalArgumentException("Email đã được sử dụng.");
            }

            User user = new User();
            user.setUsername(cleanUsername);
            user.setEmail(cleanEmail);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setRole(Role.CUSTOMER);
            user.setTier(Tier.FREE);
            user.setFullName(cleanFullName);
            user.setPhone(cleanPhone);
            user.setAddress(cleanAddress);
            user.setActive(true);

            if (gender != null && !gender.isBlank()) {
                user.setGender(Gender.valueOf(gender.toUpperCase(Locale.ROOT)));
            }

            if (dob != null && !dob.isBlank()) {
                user.setDob(LocalDate.parse(dob));
            }

            return userRepository.save(em, user);
        });
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeUsername(String value) {
        String u = trimToNull(value);
        return u == null ? null : u.toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String value) {
        String e = trimToNull(value);
        return e == null ? null : e.toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String value) {
        String p = trimToNull(value);
        if (p == null) return null;
        return p.replaceAll("[\\s().-]", "");
    }

    private String normalizeFullName(String value) {
        String f = trimToNull(value);
        if (f == null) return null;
        return f.replaceAll("\\s+", " ");
    }
}
