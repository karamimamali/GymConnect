package com.gymconnect.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymconnect.workload.dto.ActionType;
import com.gymconnect.workload.dto.MonthSummaryResponse;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.dto.TrainerWorkloadSummaryResponse;
import com.gymconnect.workload.dto.YearSummaryResponse;
import com.gymconnect.workload.exception.GlobalExceptionHandler;
import com.gymconnect.workload.exception.TrainerWorkloadNotFoundException;
import com.gymconnect.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadControllerTest {

    @Mock
    private TrainerWorkloadService workloadService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrainerWorkloadController(workloadService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void submitWorkload_shouldReturn200_andDelegateToService() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest("Mike.Johnson", "Mike",
                "Johnson", true, LocalDate.of(2026, 5, 10), 60, ActionType.ADD);

        mockMvc.perform(post("/api/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(workloadService).process(any(TrainerWorkloadRequest.class));
    }

    @Test
    void submitWorkload_shouldReturn400_whenPayloadInvalid() throws Exception {
        // Missing username, negative duration -> bean validation fails.
        String body = """
                {"username":"","firstName":"Mike","lastName":"Johnson","active":true,
                 "trainingDate":"2026-05-10","trainingDuration":-5,"actionType":"ADD"}
                """;

        mockMvc.perform(post("/api/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitWorkload_shouldReturn400_whenActionTypeUnknown() throws Exception {
        String body = """
                {"username":"Mike.Johnson","firstName":"Mike","lastName":"Johnson","active":true,
                 "trainingDate":"2026-05-10","trainingDuration":60,"actionType":"PAUSE"}
                """;

        mockMvc.perform(post("/api/workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldReturn200_withSummary() throws Exception {
        TrainerWorkloadSummaryResponse summary = new TrainerWorkloadSummaryResponse(
                "Mike.Johnson", "Mike", "Johnson", true,
                List.of(new YearSummaryResponse(2026, List.of(new MonthSummaryResponse(5, 90)))));
        when(workloadService.getSummary("Mike.Johnson")).thenReturn(summary);

        mockMvc.perform(get("/api/workloads/Mike.Johnson"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Mike.Johnson"))
                .andExpect(jsonPath("$.years[0].year").value(2026))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(90));
    }

    @Test
    void getSummary_shouldReturn404_whenTrainerHasNoData() throws Exception {
        when(workloadService.getSummary(eq("Ghost")))
                .thenThrow(new TrainerWorkloadNotFoundException("Ghost"));

        mockMvc.perform(get("/api/workloads/Ghost"))
                .andExpect(status().isNotFound());
    }
}
