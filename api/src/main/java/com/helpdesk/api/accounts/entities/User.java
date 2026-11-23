package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.core.models.BaseEntity;
import com.helpdesk.api.incidents.entities.Incident;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "\"user\"")
public class User extends BaseEntity {

    @Column(length = 100, unique = true, nullable = false)
    private String username;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    private String password;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /** Access tokens issued before this instant are rejected (set on every password change). */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @OneToMany(mappedBy = "assignedTo")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Incident> incidents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_group_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserGroup userGroup;

}
