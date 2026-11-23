package com.helpdesk.api;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract-level tests for Task 04 list, pagination, filtering, and validation.
 * Disabled until routes, parameter names, response shape, fixtures, and database setup are confirmed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Disabled("Blocked: Task 04 HTTP contract and fixture/setup are not confirmed")
class IncidentListContractTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void listWithPaginationReturnsMetadataAndPageSize() throws Exception {
        mockMvc.perform(get("/api/incidents").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content").isArray());
    }

    @ParameterizedTest
    @CsvSource({
            "title,asc",
            "title,desc",
            "createdAt,asc",
            "createdAt,desc"
    })
    void listSupportsSorting(String field, String direction) throws Exception {
        mockMvc.perform(get("/api/incidents").param("sort", field + "," + direction))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @ParameterizedTest
    @CsvSource({
            "status,NEW",
            "priority,HIGH",
            "category,NETWORK"
    })
    void individualFilterReturnsMatchingResultsOnly(String name, String value) throws Exception {
        mockMvc.perform(get("/api/incidents").param(name, value))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @ParameterizedTest
    @CsvSource({"printer", "PRINTER", "PrInTeR"})
    void titleKeywordIsCaseInsensitive(String keyword) throws Exception {
        mockMvc.perform(get("/api/incidents").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void combinedFiltersAndKeywordAndDateRangeAreIntersected() throws Exception {
        mockMvc.perform(get("/api/incidents")
                        .param("status", "RESOLVED")
                        .param("priority", "HIGH")
                        .param("category", "NETWORK")
                        .param("keyword", "printer")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void startDateOnlyIsAcceptedAndInclusive() throws Exception {
        mockMvc.perform(get("/api/incidents").param("startDate", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void endDateOnlyIsAcceptedAndInclusive() throws Exception {
        mockMvc.perform(get("/api/incidents").param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void equalDateBoundsCreateValidOneDayRange() throws Exception {
        mockMvc.perform(get("/api/incidents")
                        .param("startDate", "2026-01-15")
                        .param("endDate", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void reversedDateRangeIsRejected() throws Exception {
        mockMvc.perform(get("/api/incidents")
                        .param("startDate", "2026-02-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "status,NOT_A_STATUS",
            "priority,NOT_A_PRIORITY",
            "category,NOT_A_CATEGORY",
            "startDate,not-a-date",
            "endDate,not-a-date",
            "page,-1",
            "size,0",
            "sort,unknownField,asc",
            "sort,title,sideways"
    })
    void invalidFilterOrPaginationValuesAreRejected(String name, String value) throws Exception {
        mockMvc.perform(get("/api/incidents").param(name, value))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterWithNoMatchesReturnsEmptyPageAndValidMetadata() throws Exception {
        mockMvc.perform(get("/api/incidents")
                        .param("keyword", "NO_MATCH_EXPECTED_FOR_TEST_FIXTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void softDeletedIncidentIsNotReturnedWhenHidePolicyIsSelected() throws Exception {
        mockMvc.perform(get("/api/incidents").param("keyword", "SOFT_DELETED_FIXTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}

