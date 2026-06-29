package com.gymconnect.health;

import com.gymconnect.dao.TrainingTypeDao;
import com.gymconnect.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingDataHealthIndicatorTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private TransactionStatus transactionStatus;

    private TrainingDataHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        indicator = new TrainingDataHealthIndicator(trainingTypeDao, transactionManager);
    }

    @Test
    void health_shouldReturnUp_whenTrainingTypesExist() {
        TrainingType fitness = new TrainingType("FITNESS");
        TrainingType yoga = new TrainingType("YOGA");
        when(trainingTypeDao.findAll()).thenReturn(List.of(fitness, yoga));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("trainingTypesCount"));
    }

    @Test
    void health_shouldReturnDown_whenNoTrainingTypesExist() {
        when(trainingTypeDao.findAll()).thenReturn(Collections.emptyList());

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("No training types seeded in database", health.getDetails().get("reason"));
    }

    @Test
    void health_shouldReturnDown_whenExceptionThrown() {
        when(trainingTypeDao.findAll()).thenThrow(new RuntimeException("DB error"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
