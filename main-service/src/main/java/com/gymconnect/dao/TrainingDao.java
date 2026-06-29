package com.gymconnect.dao;

import com.gymconnect.model.Training;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainingDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainingDao.class);

    private SessionFactory sessionFactory;

    public Training save(Training training) {
        sessionFactory.getCurrentSession().persist(training);
        logger.debug("Saved training with id: {}", training.getId());
        return training;
    }

    public List<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate,
                                                LocalDate toDate, String trainerName,
                                                String trainingTypeName) {
        StringBuilder hql = new StringBuilder(
                "FROM Training t "
                        + "JOIN FETCH t.trainee te JOIN FETCH te.user "
                        + "JOIN FETCH t.trainer tr JOIN FETCH tr.user "
                        + "JOIN FETCH t.trainingType "
                        + "WHERE te.user.username = :username");

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (trainerName != null) {
            hql.append(" AND tr.user.username = :trainerName");
        }
        if (trainingTypeName != null) {
            hql.append(" AND t.trainingType.trainingTypeName = :trainingType");
        }

        Query<Training> query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("username", traineeUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (trainerName != null) {
            query.setParameter("trainerName", trainerName);
        }
        if (trainingTypeName != null) {
            query.setParameter("trainingType", trainingTypeName);
        }

        List<Training> trainings = query.list();
        logger.debug("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);
        return trainings;
    }

    public List<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate,
                                                LocalDate toDate, String traineeName) {
        StringBuilder hql = new StringBuilder(
                "FROM Training t "
                        + "JOIN FETCH t.trainee te JOIN FETCH te.user "
                        + "JOIN FETCH t.trainer tr JOIN FETCH tr.user "
                        + "JOIN FETCH t.trainingType "
                        + "WHERE tr.user.username = :username");

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (traineeName != null) {
            hql.append(" AND te.user.username = :traineeName");
        }

        Query<Training> query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Training.class)
                .setParameter("username", trainerUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (traineeName != null) {
            query.setParameter("traineeName", traineeName);
        }

        List<Training> trainings = query.list();
        logger.debug("Found {} trainings for trainer: {}", trainings.size(), trainerUsername);
        return trainings;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
