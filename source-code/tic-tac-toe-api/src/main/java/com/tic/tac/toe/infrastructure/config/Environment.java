package com.tic.tac.toe.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Environment {
    private static final Logger log =
            LoggerFactory.getLogger(Environment.class);
    private static final Dotenv DOTENV;

    static {
        DOTENV = Dotenv.load();
        log.info("Instance {} initialized.", Environment.class.getSimpleName());
    }

    public static String get(String key) {
        return DOTENV.get(key);
    }
}
