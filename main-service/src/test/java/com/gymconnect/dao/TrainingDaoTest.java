package com.gymconnect.dao;

import com.gymconnect.config.TestHibernateConfig;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestHibernateConfig.class)
@Transactional
class TrainingDaoTest {

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private SessionFactory sessionFactory;

    private TrainingType fitnessType;
    private TrainingType yogaType;
    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        fitnessType = new TrainingType("FITNESS");
        yogaType = new TrainingType("YOGA");
        sessionFactory.getCurrentSession().persist(fitnessType);
        sessionFactory.getCurrentSession().persist(yogaType);

        User traineeUser = new User("John", "Doe", true);
        traineeUser.setUsername("John.Doe");
        traineeUser.setPassword("password10");
        trainee = new Trainee(traineeUser, LocalDate.of(1995, 6, 15), "123 Main St");
        traineeDao.save(trainee);

        User trainerUser = new User("Mike", "Johnson", true);
        trainerUser.setUsername("Mike.Johnson");
        trainerUser.setPassword("password10");
        trainer = new Trainer(trainerUser, fitnessType);
        trainerDao.save(trainer);
    }

    @Test
    void save_shouldPersistTraining() {
        Training training = new Training(trainee, trainer, "Morning Fitness",
                fitnessType, LocalDate.of(2026, 5, 1), 60);

        Training saved = trainingDao.save(training);

        assertNotNull(saved.getId());
        assertEquals("Morning Fitness", saved.getTrainingName());
    }

    @Test
    void findTraineeTrainings_shouldReturnAll_whenNoCriteria() {
        trainingDao.save(new Training(trainee, trainer, "Session 1",
                fitnessType, LocalDate.of(2026, 5, 1), 60));
        trainingDao.save(new Training(trainee, trainer, "Session 2",
                fitnessType, LocalDate.of(2026, 5, 2), 45));

        List<Training> result = trainingDao.findTraineeTrainings(
                "John.Doe", null, null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void findTraineeTrainings_shouldFilterByDateRange() {
        trainingDao.save(new Training(trainee, trainer, "Session 1",
                fitnessType, LocalDate.of(2026, 5, 1), 60));
        trainingDao.save(new Training(trainee, trainer, "Session 2",
                fitnessType, LocalDate.of(2026, 6, 1), 45));

        List<Training> result = trainingDao.findTraineeTrainings(
                "John.Doe", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                null, null);

        assertEquals(1, result.size());
        assertEquals("Session 1", result.get(0).getTrainingName());
    }

    @Test
    void findTraineeTrainings_shouldFilterByTrainerName() {
        User trainer2User = new User("Sarah", "Williams", true);
        trainer2User.setUsername("Sarah.Williams");
        trainer2User.setPassword("password10");
        Trainer trainer2 = new Trainer(trainer2User, yogaType);
        trainerDao.save(trainer2);

        trainingDao.save(new Training(trainee, trainer, "Fitness Session",
                fitnessType, LocalDate.of(2026, 5, 1), 60));
        trainingDao.save(new Training(trainee, trainer2, "Yoga Session",
                yogaType, LocalDate.of(2026, 5, 2), 45));

        List<Training> result = trainingDao.findTraineeTrainings(
                "John.Doe", null, null, "Mike.Johnson", null);

        assertEquals(1, result.size());
        assertEquals("Fitness Session", result.get(0).getTrainingName());
    }

    @Test
    void findTraineeTrainings_shouldFilterByTrainingType() {
        trainingDao.save(new Training(trainee, trainer, "Fitness Session",
                fitnessType, LocalDate.of(2026, 5, 1), 60));

        List<Training> result = trainingDao.findTraineeTrainings(
                "John.Doe", null, null, null, "FITNESS");

        assertEquals(1, result.size());
    }

    @Test
    void findTraineeTrainings_shouldReturnEmpty_whenNoMatch() {
        List<Training> result = trainingDao.findTraineeTrainings(
                "John.Doe", null, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void findTrainerTrainings_shouldReturnAll_whenNoCriteria() {
        trainingDao.save(new Training(trainee, trainer, "Session 1",
                fitnessType, LocalDate.of(2026, 5, 1), 60));

        List<Training> result = trainingDao.findTrainerTrainings(
                "Mike.Johnson", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void findTrainerTrainings_shouldFilterByDateRange() {
        trainingDao.save(new Training(trainee, trainer, "Session 1",
                fitnessType, LocalDate.of(2026, 5, 1), 60));
        trainingDao.save(new Training(trainee, trainer, "Session 2",
                fitnessType, LocalDate.of(2026, 6, 1), 45));

        List<Training> result = trainingDao.findTrainerTrainings(
                "Mike.Johnson", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                null);

        assertEquals(1, result.size());
    }

    @Test
    void findTrainerTrainings_shouldFilterByTraineeName() {
        User trainee2User = new User("Jane", "Smith", true);
        trainee2User.setUsername("Jane.Smith");
        trainee2User.setPassword("password10");
        Trainee trainee2 = new Trainee(trainee2User, LocalDate.of(1998, 3, 22), "456 Oak Ave");
        traineeDao.save(trainee2);

        trainingDao.save(new Training(trainee, trainer, "Session 1",
                fitnessType, LocalDate.of(2026, 5, 1), 60));
        trainingDao.save(new Training(trainee2, trainer, "Session 2",
                fitnessType, LocalDate.of(2026, 5, 2), 45));

        List<Training> result = trainingDao.findTrainerTrainings(
                "Mike.Johnson", null, null, "John.Doe");

        assertEquals(1, result.size());
        assertEquals("Session 1", result.get(0).getTrainingName());
    }

    @Test
    void findTrainerTrainings_shouldReturnEmpty_whenNoMatch() {
        List<Training> result = trainingDao.findTrainerTrainings(
                "Mike.Johnson", null, null, null);

        assertTrue(result.isEmpty());
    }
}
