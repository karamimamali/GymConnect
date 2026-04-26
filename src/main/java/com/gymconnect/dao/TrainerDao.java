package com.gymconnect.dao;

import com.gymconnect.model.Trainer;
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
public class TrainerDao implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDao.class);

    private Map<Long, Trainer> storage;
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public void afterPropertiesSet() {
        long maxId = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        idCounter.set(maxId);
        logger.debug("Initialized trainer ID counter to: {}", maxId);
    }

    public Trainer save(Trainer trainer) {
        long id = idCounter.incrementAndGet();
        trainer.setId(id);
        storage.put(id, trainer);
        logger.debug("Saved trainer with id: {}", id);
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        Trainer trainer = storage.get(id);
        if (trainer == null) {
            logger.warn("Trainer not found with id: {}", id);
        } else {
            logger.debug("Found trainer with id: {}", id);
        }
        return Optional.ofNullable(trainer);
    }

    public List<Trainer> findAll() {
        logger.debug("Retrieving all trainers, count: {}", storage.size());
        return new ArrayList<>(storage.values());
    }

    public Trainer update(Trainer trainer) {
        if (!storage.containsKey(trainer.getId())) {
            logger.warn("Cannot update trainer: not found with id: {}", trainer.getId());
            return null;
        }
        storage.put(trainer.getId(), trainer);
        logger.debug("Updated trainer with id: {}", trainer.getId());
        return trainer;
    }

    @Autowired
    @Qualifier("trainerStorage")
    public void setStorage(Map<Long, Trainer> storage) {
        this.storage = storage;
    }
}
