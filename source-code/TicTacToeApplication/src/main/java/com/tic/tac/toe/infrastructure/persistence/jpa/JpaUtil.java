package com.tic.tac.toe.infrastructure.persistence.jpa;

import com.tic.tac.toe.infrastructure.config.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public final class JpaUtil {
    private static final Logger log = LoggerFactory.getLogger(JpaUtil.class);
    private static final EntityManagerFactory FACTORY;

    static {
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", Environment.get("DB_URL"));
        properties.put("javax.persistence.jdbc.user", Environment.get("DB_USERNAME"));
        properties.put("javax.persistence.jdbc.password", Environment.get("DB_PASSWORD"));
        FACTORY = Persistence.createEntityManagerFactory(
                Environment.get("ORM_PERSISTENCE"),
                properties
        );
        log.info("Instance {} initialized.", JpaUtil.class.getSimpleName());
    }

    public static EntityManagerFactory getFactory() {
        return FACTORY;
    }
}
