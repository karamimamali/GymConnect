package com.gymconnect.dao;

import com.gymconnect.model.Trainee;
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
public class TraineeDao implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDao.class);

    private Map<Long, Trainee> storage;
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public void afterPropertiesSet() {
        long maxId = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        idCounter.set(maxId);
        logger.debug("Initialized trainee ID counter to: {}", maxId);
    }

    public Trainee save(Trainee trainee) {
        long id = idCounter.incrementAndGet();
        trainee.setId(id);
        storage.put(id, trainee);
        logger.debug("Saved trainee with id: {}", id);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        Trainee trainee = storage.get(id);
        if (trainee == null) {
            logger.warn("Trainee not found with id: {}", id);
        } else {
            logger.debug("Found trainee with id: {}", id);
        }
        return Optional.ofNullable(trainee);
    }

    public List<Trainee> findAll() {
        logger.debug("Retrieving all trainees, count: {}", storage.size());
        return new ArrayList<>(storage.values());
    }

    public Trainee update(Trainee trainee) {
        if (!storage.containsKey(trainee.getId())) {
            logger.warn("Cannot update trainee: not found with id: {}", trainee.getId());
            return null;
        }
        storage.put(trainee.getId(), trainee);
        logger.debug("Updated trainee with id: {}", trainee.getId());
        return trainee;
    }

    public boolean delete(Long id) {
        Trainee removed = storage.remove(id);
        if (removed == null) {
            logger.warn("Cannot delete trainee: not found with id: {}", id);
            return false;
        }
        logger.debug("Deleted trainee with id: {}", id);
        return true;
    }

    @Autowired
    @Qualifier("traineeStorage")
    public void setStorage(Map<Long, Trainee> storage) {
        this.storage = storage;
    }
}
