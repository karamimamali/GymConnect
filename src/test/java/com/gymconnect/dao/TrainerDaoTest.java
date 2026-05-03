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
class TrainerDaoTest {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private SessionFactory sessionFactory;

    private TrainingType fitnessType;
    private TrainingType yogaType;

    @BeforeEach
    void setUp() {
        fitnessType = new TrainingType("FITNESS");
        yogaType = new TrainingType("YOGA");
        sessionFactory.getCurrentSession().persist(fitnessType);
        sessionFactory.getCurrentSession().persist(yogaType);
    }

    @Test
    void save_shouldPersistTrainer() {
        Trainer trainer = createTrainer("Mike", "Johnson", "Mike.Johnson", fitnessType);

        Trainer saved = trainerDao.save(trainer);

        assertNotNull(saved.getId());
        assertEquals("Mike.Johnson", saved.getUser().getUsername());
    }

    @Test
    void findByUsername_shouldReturnTrainer_whenExists() {
        trainerDao.save(createTrainer("Mike", "Johnson", "Mike.Johnson", fitnessType));

        Optional<Trainer> result = trainerDao.findByUsername("Mike.Johnson");

        assertTrue(result.isPresent());
        assertEquals("Mike", result.get().getUser().getFirstName());
        assertEquals("FITNESS", result.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        Optional<Trainer> result = trainerDao.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void update_shouldModifyTrainer() {
        Trainer trainer = createTrainer("Mike", "Johnson", "Mike.Johnson", fitnessType);
        trainerDao.save(trainer);

        trainer.setSpecialization(yogaType);
        Trainer updated = trainerDao.update(trainer);

        assertEquals("YOGA", updated.getSpecialization().getTrainingTypeName());
    }

    @Test
    void findAll_shouldReturnAllTrainers() {
        trainerDao.save(createTrainer("Mike", "Johnson", "Mike.Johnson", fitnessType));
        trainerDao.save(createTrainer("Sarah", "Williams", "Sarah.Williams", yogaType));

        List<Trainer> trainers = trainerDao.findAll();

        assertEquals(2, trainers.size());
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoTrainers() {
        List<Trainer> trainers = trainerDao.findAll();

        assertTrue(trainers.isEmpty());
    }

    @Test
    void findUnassignedTrainers_shouldReturnAllTrainers_whenNoneAssigned() {
        trainerDao.save(createTrainer("Mike", "Johnson", "Mike.Johnson", fitnessType));
        trainerDao.save(createTrainer("Sarah", "Williams", "Sarah.Williams", yogaType));

        User traineeUser = new User("John", "Doe", true);
        traineeUser.setUsername("John.Doe");
        traineeUser.setPassword("password10");
        Trainee trainee = new Trainee(traineeUser, LocalDate.of(1995, 6, 15), "123 Main St");
        traineeDao.save(trainee);

        List<Trainer> unassigned = trainerDao.findUnassignedTrainers("John.Doe");

        assertEquals(2, unassigned.size());
    }

    @Test
    void findUnassignedTrainers_shouldExcludeAssignedTrainers() {
        Trainer trainer1 = createTrainer("Mike", "Johnson", "Mike.Johnson", fitnessType);
        Trainer trainer2 = createTrainer("Sarah", "Williams", "Sarah.Williams", yogaType);
        trainerDao.save(trainer1);
        trainerDao.save(trainer2);

        User traineeUser = new User("John", "Doe", true);
        traineeUser.setUsername("John.Doe");
        traineeUser.setPassword("password10");
        Trainee trainee = new Trainee(traineeUser, LocalDate.of(1995, 6, 15), "123 Main St");
        trainee.getTrainers().add(trainer1);
        traineeDao.save(trainee);
        sessionFactory.getCurrentSession().flush();

        List<Trainer> unassigned = trainerDao.findUnassignedTrainers("John.Doe");

        assertEquals(1, unassigned.size());
        assertEquals("Sarah.Williams", unassigned.get(0).getUser().getUsername());
    }

    private Trainer createTrainer(String firstName, String lastName, String username,
                                   TrainingType specialization) {
        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword("password10");
        return new Trainer(user, specialization);
    }
}
