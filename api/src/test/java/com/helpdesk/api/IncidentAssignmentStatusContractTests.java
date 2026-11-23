package com.helpdesk.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract-level tests for Task 03.
 * Disabled until routes, JSON fields, response statuses, fixtures, and database setup are confirmed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Disabled("Blocked: Task 03 HTTP contract and fixture/setup are not confirmed")
class IncidentAssignmentStatusContractTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void assignExistingUserToNewIncidentSucceedsAndChangesStatusToAssigned() throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "new-id")
                        .contentType("application/json")
                        .content("{\"assigneeId\":\"existing-user-id\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void reassignAssignedIncidentSucceedsAndKeepsAssignedStatus() throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "assigned-id")
                        .contentType("application/json")
                        .content("{\"assigneeId\":\"another-existing-user-id\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void assignmentRejectsMissingIncident() throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "missing-id")
                        .contentType("application/json")
                        .content("{\"assigneeId\":\"existing-user-id\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignmentRejectsMissingUser() throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "new-id")
                        .contentType("application/json")
                        .content("{\"assigneeId\":\"missing-user-id\"}"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({"'',", "null,"})
    void assignmentRejectsBlankOrNullAssignee(String assigneeId) throws Exception {
        String body = "null".equals(assigneeId)
                ? "{\"assigneeId\":null}"
                : "{\"assigneeId\":\"\"}";

        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "new-id")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "NEW, ASSIGNED, true",
            "ASSIGNED, IN_PROGRESS, true",
            "IN_PROGRESS, PENDING, true",
            "IN_PROGRESS, RESOLVED, true",
            "PENDING, IN_PROGRESS, true",
            "RESOLVED, CLOSED, true",
            "NEW, IN_PROGRESS, false",
            "NEW, PENDING, false",
            "NEW, RESOLVED, false",
            "NEW, CLOSED, false",
            "ASSIGNED, PENDING, false",
            "ASSIGNED, RESOLVED, false",
            "ASSIGNED, CLOSED, false",
            "IN_PROGRESS, ASSIGNED, false",
            "IN_PROGRESS, NEW, false",
            "IN_PROGRESS, CLOSED, false",
            "PENDING, ASSIGNED, false",
            "PENDING, RESOLVED, false",
            "PENDING, CLOSED, false",
            "RESOLVED, ASSIGNED, false",
            "RESOLVED, IN_PROGRESS, false",
            "RESOLVED, PENDING, false",
            "CLOSED, NEW, false",
            "CLOSED, ASSIGNED, false",
            "CLOSED, IN_PROGRESS, false",
            "CLOSED, PENDING, false",
            "CLOSED, RESOLVED, false",
            "CLOSED, CLOSED, false"
    })
    void validAndInvalidStatusTransitionsAreEnforced(String from, String to, boolean valid) throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/status", "fixture-id")
                        .contentType("application/json")
                        .content("{\"status\":\"" + to + "\"}"));
        // Fixture setup for 'from' and assertions for valid/invalid must be supplied by contract-confirmed tests.
    }

    @ParameterizedTest
    @CsvSource({"IN_PROGRESS", "PENDING", "RESOLVED", "CLOSED"})
    void reassignmentOutsideAssignedStatusIsRejected(String status) throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "fixture-id")
                        .contentType("application/json")
                        .content("{\"assigneeId\":\"existing-user-id\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignmentMustNotUpdateIncidentContent() throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/assign", "assigned-id")
                        .contentType("application/json")
                        .content("{\"assigneeId\":\"existing-user-id\",\"title\":\"MUST_NOT_CHANGE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusOperationMustNotChangeAssigneeOrIncidentContent() throws Exception {
        mockMvc.perform(patch("/api/incidents/{incidentId}/status", "assigned-id")
                        .contentType("application/json")
                        .content("{\"status\":\"IN_PROGRESS\",\"assigneeId\":\"must-not-change\",\"title\":\"must-not-change\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusRequestRejectsNullUnknownOrBlankStatus() throws Exception {
        for (String body : new String[]{
                "{\"status\":null}",
                "{\"status\":\"\"}",
                "{\"status\":\"UNKNOWN\"}"
        }) {
            mockMvc.perform(patch("/api/incidents/{incidentId}/status", "assigned-id")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}

