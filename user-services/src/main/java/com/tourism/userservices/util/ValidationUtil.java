package com.tourism.userservices.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ValidationUtil {

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    public static boolean isValidDateOfBirth(String dob) {
        // Implement date format validation logic (e.g., yyyy-MM-dd)
        return dob != null && dob.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    public static boolean isValidLanguages(List<String> languages) {
        return languages != null && !languages.isEmpty() && languages.stream().allMatch(lang -> lang != null && !lang.isEmpty());
    }
}