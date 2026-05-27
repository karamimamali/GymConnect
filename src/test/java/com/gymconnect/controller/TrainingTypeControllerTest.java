package com.gymconnect.controller;

import com.gymconnect.dao.TrainingTypeDao;
import com.gymconnect.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrainingTypeController(trainingTypeDao))
                .build();
    }

    @Test
    void getAllReturnsTrainingTypes() throws Exception {
        TrainingType fitness = new TrainingType("FITNESS");
        fitness.setId(1L);
        TrainingType yoga = new TrainingType("YOGA");
        yoga.setId(2L);

        when(trainingTypeDao.findAll()).thenReturn(List.of(fitness, yoga));

        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingTypeId").value(1))
                .andExpect(jsonPath("$[0].trainingType").value("FITNESS"))
                .andExpect(jsonPath("$[1].trainingTypeId").value(2))
                .andExpect(jsonPath("$[1].trainingType").value("YOGA"));
    }
}
