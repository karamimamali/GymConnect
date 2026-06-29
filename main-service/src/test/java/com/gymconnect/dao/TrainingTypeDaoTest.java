package com.gymconnect.dao;

import com.gymconnect.config.TestHibernateConfig;
import com.gymconnect.model.TrainingType;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestHibernateConfig.class)
@Transactional
class TrainingTypeDaoTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        sessionFactory.getCurrentSession().persist(new TrainingType("FITNESS"));
        sessionFactory.getCurrentSession().persist(new TrainingType("YOGA"));
    }

    @Test
    void findByName_shouldReturnType_whenExists() {
        Optional<TrainingType> result = trainingTypeDao.findByName("FITNESS");

        assertTrue(result.isPresent());
        assertEquals("FITNESS", result.get().getTrainingTypeName());
    }

    @Test
    void findByName_shouldReturnEmpty_whenNotExists() {
        Optional<TrainingType> result = trainingTypeDao.findByName("BOXING");

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnAllTypes() {
        List<TrainingType> types = trainingTypeDao.findAll();

        assertEquals(2, types.size());
    }
}
