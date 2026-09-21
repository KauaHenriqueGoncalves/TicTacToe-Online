package com.tic.tac.toe.infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;
import com.tic.tac.toe.domain.exception.UnauthorizedException;

public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * @param password its password will be compared
     * @param hash its hashed password to be compared
     * @throws UnauthorizedException if the password not matched.
     * @return boolean true=matched or false=not matched
     */
    public static boolean matches(String password, String hash) throws UnauthorizedException {
        return BCrypt.checkpw(password, hash);
    }
}
