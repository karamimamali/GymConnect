package com.gymconnect.dao;

import com.gymconnect.model.Trainer;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDao.class);

    private SessionFactory sessionFactory;

    public Trainer save(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
        logger.debug("Saved trainer with id: {}", trainer.getId());
        return trainer;
    }

    public Optional<Trainer> findByUsername(String username) {
        Trainer trainer = sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization "
                                + "WHERE t.user.username = :username",
                        Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional()
                .orElse(null);
        if (trainer == null) {
            logger.warn("Trainer not found with username: {}", username);
        } else {
            logger.debug("Found trainer with username: {}", username);
        }
        return Optional.ofNullable(trainer);
    }

    public Trainer update(Trainer trainer) {
        Trainer merged = sessionFactory.getCurrentSession().merge(trainer);
        logger.debug("Updated trainer with id: {}", merged.getId());
        return merged;
    }

    public List<Trainer> findAll() {
        List<Trainer> trainers = sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization",
                        Trainer.class)
                .list();
        logger.debug("Retrieving all trainers, count: {}", trainers.size());
        return trainers;
    }

    public List<Trainer> findUnassignedTrainers(String traineeUsername) {
        List<Trainer> trainers = sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization "
                                + "WHERE t NOT IN ("
                                + "SELECT tr FROM Trainee te JOIN te.trainers tr "
                                + "WHERE te.user.username = :username)",
                        Trainer.class)
                .setParameter("username", traineeUsername)
                .list();
        logger.debug("Found {} unassigned trainers for trainee: {}",
                trainers.size(), traineeUsername);
        return trainers;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
