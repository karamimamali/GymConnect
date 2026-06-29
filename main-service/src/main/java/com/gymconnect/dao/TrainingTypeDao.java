package com.gymconnect.dao;

import com.gymconnect.model.TrainingType;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeDao.class);

    private SessionFactory sessionFactory;

    public Optional<TrainingType> findByName(String name) {
        TrainingType type = sessionFactory.getCurrentSession()
                .createQuery("FROM TrainingType t WHERE t.trainingTypeName = :name",
                        TrainingType.class)
                .setParameter("name", name)
                .uniqueResultOptional()
                .orElse(null);
        if (type == null) {
            logger.warn("TrainingType not found with name: {}", name);
        } else {
            logger.debug("Found TrainingType: {}", name);
        }
        return Optional.ofNullable(type);
    }

    public List<TrainingType> findAll() {
        List<TrainingType> types = sessionFactory.getCurrentSession()
                .createQuery("FROM TrainingType", TrainingType.class)
                .list();
        logger.debug("Found {} training types", types.size());
        return types;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
