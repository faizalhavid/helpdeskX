package com.helpdesk.api.incidents.controllers;

import com.helpdesk.api.core.models.ApiResponse;
import com.helpdesk.api.incidents.dto.requests.AssignIncidentRequest;
import com.helpdesk.api.incidents.dto.requests.IncidentFilterRequest;
import com.helpdesk.api.incidents.dto.requests.IncidentRequest;
import com.helpdesk.api.incidents.dto.requests.UpdateIncidentStatusRequest;
import com.helpdesk.api.incidents.dto.responses.IncidentResponse;
import com.helpdesk.api.incidents.services.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService  incidentService;

    @PreAuthorize("hasAuthority('INCIDENT:CREATE')")
    @PostMapping()
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident(@RequestBody IncidentRequest incidentRequest) {
      IncidentResponse createdIncident =  incidentService.createIncident(incidentRequest);
      ResponseEntity.ok(ApiResponse.ok("Successfully Created Incident", createdIncident));
      return  ResponseEntity.ok(ApiResponse.ok("Created Incident", createdIncident));
    }

    @PreAuthorize("hasAuthority('INCIDENT:READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncident(@PathVariable String id){
        IncidentResponse incident = incidentService.getDetailsIncidents(id);
        return ResponseEntity.ok(ApiResponse.ok("Successfully get detail incident",  incident));
    }

    @PreAuthorize("hasAuthority('INCIDENT:READ')")
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getAllIncidents(@ModelAttribute IncidentFilterRequest incidentFilterRequest, Pageable pageable){
        Page<IncidentResponse> incidents = incidentService.getAllIncidents(incidentFilterRequest, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Successfully retrieved incidents", incidents));
    }

    @PreAuthorize("hasAuthority('INCIDENT:UPDATE')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateIncident(@PathVariable String id, @RequestBody IncidentRequest incidentRequest){
            IncidentResponse updateIncident = incidentService.updateIncident(id,incidentRequest);
        return ResponseEntity.ok(
                ApiResponse.ok("Incident updated successfully", updateIncident));
    }

    // Any status change needs UPDATE; moving to RESOLVED additionally needs CLOSE.
    @PreAuthorize("hasAuthority('INCIDENT:UPDATE') "
            + "and (#request.status().name() != 'RESOLVED' or hasAuthority('INCIDENT:CLOSE'))")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateIncidentStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateIncidentStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Incident updated successfully",
                        incidentService.updateIncidentStatus(id, request.status()))
        );
    }

    @PreAuthorize("hasAuthority('INCIDENT:ASSIGN')")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<IncidentResponse>> assignIncident(
            @PathVariable String id,
            @Valid @RequestBody AssignIncidentRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Incident Assignment updated successfully",
                        incidentService.assignIncident(id, request.userId()))
        );
    }

    @PreAuthorize("hasAuthority('INCIDENT:DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> softDeleteIncident(@PathVariable String id){
        incidentService.softDeleteIncident(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Incident deleted successfully", true)
        );
    }

    // Irreversible (removes the row), so a plain DELETE privilege is not enough.
    @PreAuthorize("hasAuthority('INCIDENT:DELETE') and hasRole('ADMIN')")
    @DeleteMapping("/{id}/force-delete")
    public ResponseEntity<ApiResponse<?>> forceDeleteIncident(@PathVariable String id){
        incidentService.forceDeleteIncident(id);
        return  ResponseEntity.ok(
                ApiResponse.ok("Incident deleted permanent successfully", true)
        );
    }

}
