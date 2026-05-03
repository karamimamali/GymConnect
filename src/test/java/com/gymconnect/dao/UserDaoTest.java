package com.gymconnect.dao;

import com.gymconnect.config.TestHibernateConfig;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestHibernateConfig.class)
@Transactional
class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private SessionFactory sessionFactory;

    private TrainingType fitnessType;

    @BeforeEach
    void setUp() {
        fitnessType = new TrainingType("FITNESS");
        sessionFactory.getCurrentSession().persist(fitnessType);
    }

    @Test
    void findAllUsernames_shouldReturnEmpty_whenNoUsers() {
        List<String> usernames = userDao.findAllUsernames();

        assertTrue(usernames.isEmpty());
    }

    @Test
    void findAllUsernames_shouldReturnAllUsernames() {
        User traineeUser = new User("John", "Doe", true);
        traineeUser.setUsername("John.Doe");
        traineeUser.setPassword("password10");
        traineeDao.save(new Trainee(traineeUser, LocalDate.of(1995, 6, 15), "123 Main St"));

        User trainerUser = new User("Mike", "Johnson", true);
        trainerUser.setUsername("Mike.Johnson");
        trainerUser.setPassword("password10");
        trainerDao.save(new Trainer(trainerUser, fitnessType));

        List<String> usernames = userDao.findAllUsernames();

        assertEquals(2, usernames.size());
        assertTrue(usernames.contains("John.Doe"));
        assertTrue(usernames.contains("Mike.Johnson"));
    }
}
