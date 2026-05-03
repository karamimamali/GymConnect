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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestHibernateConfig.class)
@Transactional
class TraineeDaoTest {

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private SessionFactory sessionFactory;

    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        trainingType = new TrainingType("FITNESS");
        sessionFactory.getCurrentSession().persist(trainingType);
    }

    @Test
    void save_shouldPersistTrainee() {
        Trainee trainee = createTrainee("John", "Doe", "John.Doe");

        Trainee saved = traineeDao.save(trainee);

        assertNotNull(saved.getId());
        assertEquals("John.Doe", saved.getUser().getUsername());
    }

    @Test
    void findByUsername_shouldReturnTrainee_whenExists() {
        traineeDao.save(createTrainee("John", "Doe", "John.Doe"));

        Optional<Trainee> result = traineeDao.findByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getUser().getFirstName());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        Optional<Trainee> result = traineeDao.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void update_shouldModifyTrainee() {
        Trainee trainee = createTrainee("John", "Doe", "John.Doe");
        traineeDao.save(trainee);

        trainee.setAddress("New Address");
        Trainee updated = traineeDao.update(trainee);

        assertEquals("New Address", updated.getAddress());
    }

    @Test
    void deleteByUsername_shouldRemoveTrainee() {
        traineeDao.save(createTrainee("John", "Doe", "John.Doe"));

        traineeDao.deleteByUsername("John.Doe");
        sessionFactory.getCurrentSession().flush();

        Optional<Trainee> result = traineeDao.findByUsername("John.Doe");
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnAllTrainees() {
        traineeDao.save(createTrainee("John", "Doe", "John.Doe"));
        traineeDao.save(createTrainee("Jane", "Smith", "Jane.Smith"));

        List<Trainee> trainees = traineeDao.findAll();

        assertEquals(2, trainees.size());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoTrainees() {
        List<Trainee> trainees = traineeDao.findAll();

        assertTrue(trainees.isEmpty());
    }

    @Test
    void deleteByUsername_shouldNotFail_whenNotExists() {
        traineeDao.deleteByUsername("nonexistent");
        // No exception should be thrown
    }

    private Trainee createTrainee(String firstName, String lastName, String username) {
        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword("password10");
        return new Trainee(user, LocalDate.of(1995, 6, 15), "123 Main St");
    }
}
