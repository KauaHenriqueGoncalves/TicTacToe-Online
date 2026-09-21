package com.tic.tac.toe.domain.validation;

public final class EmailValidation {
    private EmailValidation() {
    }

    public static boolean isValid(String email) {
        return email != null
                && !email.trim().isEmpty()
                && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
