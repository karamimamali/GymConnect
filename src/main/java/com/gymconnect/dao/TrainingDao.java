package com.gymconnect.dao;

import com.gymconnect.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
@DependsOn("storageInitializer")
public class TrainingDao implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDao.class);

    private Map<Long, Training> storage;
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public void afterPropertiesSet() {
        long maxId = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        idCounter.set(maxId);
        logger.debug("Initialized training ID counter to: {}", maxId);
    }

    public Training save(Training training) {
        long id = idCounter.incrementAndGet();
        training.setId(id);
        storage.put(id, training);
        logger.debug("Saved training with id: {}", id);
        return training;
    }

    public Optional<Training> findById(Long id) {
        Training training = storage.get(id);
        if (training == null) {
            logger.warn("Training not found with id: {}", id);
        } else {
            logger.debug("Found training with id: {}", id);
        }
        return Optional.ofNullable(training);
    }

    public List<Training> findAll() {
        logger.debug("Retrieving all trainings, count: {}", storage.size());
        return new ArrayList<>(storage.values());
    }

    @Autowired
    @Qualifier("trainingStorage")
    public void setStorage(Map<Long, Training> storage) {
        this.storage = storage;
    }
}
