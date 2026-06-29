package com.gymconnect.health;

import com.gymconnect.dao.TrainingTypeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Custom health indicator that verifies the application data is properly seeded
 * by checking whether training types are present in the database.
 */
@Component("trainingData")
public class TrainingDataHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDataHealthIndicator.class);

    private final TrainingTypeDao trainingTypeDao;
    private final TransactionTemplate transactionTemplate;

    public TrainingDataHealthIndicator(TrainingTypeDao trainingTypeDao,
                                       PlatformTransactionManager transactionManager) {
        this.trainingTypeDao = trainingTypeDao;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
    }

    @Override
    public Health health() {
        logger.debug("Checking training data health");
        try {
            Integer count = transactionTemplate.execute(
                    status -> trainingTypeDao.findAll().size());
            if (count != null && count > 0) {
                logger.debug("Training data health check passed: {} types found", count);
                return Health.up()
                        .withDetail("trainingTypesCount", count)
                        .build();
            }
            logger.warn("No training types found in database");
            return Health.down()
                    .withDetail("reason", "No training types seeded in database")
                    .build();
        } catch (Exception e) {
            logger.error("Training data health check failed", e);
            return Health.down(e).build();
        }
    }
}
