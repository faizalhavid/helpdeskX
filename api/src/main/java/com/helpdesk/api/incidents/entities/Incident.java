package com.helpdesk.api.incidents.entities;

import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.core.models.BaseEntity;
import com.helpdesk.api.incidents.entities.enums.IncidentPriority;
import com.helpdesk.api.incidents.entities.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Incident extends BaseEntity {

    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;

    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.NEW;

    @Enumerated(EnumType.STRING)
    private IncidentPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User assignedTo;

}
