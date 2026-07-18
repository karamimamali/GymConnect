package com.gymconnect.dao;

import com.gymconnect.model.Trainee;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDao.class);

    private SessionFactory sessionFactory;

    public Trainee save(Trainee trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
        logger.debug("Saved trainee with id: {}", trainee.getId());
        return trainee;
    }

    public Optional<Trainee> findByUsername(String username) {
        Trainee trainee = sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Trainee t JOIN FETCH t.user "
                                + "LEFT JOIN FETCH t.trainers tr "
                                + "LEFT JOIN FETCH tr.user "
                                + "LEFT JOIN FETCH tr.specialization "
                                + "WHERE t.user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional()
                .orElse(null);
        if (trainee == null) {
            logger.warn("Trainee not found with username: {}", username);
        } else {
            logger.debug("Found trainee with username: {}", username);
        }
        return Optional.ofNullable(trainee);
    }

    public Trainee update(Trainee trainee) {
        Trainee merged = sessionFactory.getCurrentSession().merge(trainee);
        logger.debug("Updated trainee with id: {}", merged.getId());
        return merged;
    }

    public void deleteByUsername(String username) {
        findByUsername(username).ifPresent(trainee -> {
            sessionFactory.getCurrentSession().remove(trainee);
            logger.debug("Deleted trainee with username: {}", username);
        });
    }

    public List<Trainee> findAll() {
        List<Trainee> trainees = sessionFactory.getCurrentSession()
                .createQuery("FROM Trainee t JOIN FETCH t.user", Trainee.class)
                .list();
        logger.debug("Retrieving all trainees, count: {}", trainees.size());
        return trainees;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
