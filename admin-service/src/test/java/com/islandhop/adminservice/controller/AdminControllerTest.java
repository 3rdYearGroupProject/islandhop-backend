package com.islandhop.adminservice.controller;

import com.islandhop.adminservice.model.SystemStatusResponse;
import com.islandhop.adminservice.service.SystemStatusService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminController.
 */
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemStatusService systemStatusService;

    @Test
    void getSystemStatus_ShouldReturnStatusResponse() throws Exception {
        // Given
        SystemStatusResponse mockResponse = new SystemStatusResponse(
            SystemStatusResponse.Status.UP,
            SystemStatusResponse.Status.UP,
            SystemStatusResponse.Status.DOWN
        );
        when(systemStatusService.getSystemStatus()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/admin/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.redis").value("UP"))
                .andExpect(jsonPath("$.firebase").value("UP"))
                .andExpect(jsonPath("$.mongodb").value("DOWN"));
    }

    @Test
    void getSystemStatus_ShouldHandleServiceException() throws Exception {
        // Given
        when(systemStatusService.getSystemStatus()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/admin/status"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.redis").value("DOWN"))
                .andExpect(jsonPath("$.firebase").value("DOWN"))
                .andExpect(jsonPath("$.mongodb").value("DOWN"));
    }
}
