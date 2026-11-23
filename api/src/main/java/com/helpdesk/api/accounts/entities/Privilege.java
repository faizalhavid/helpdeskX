package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.accounts.entities.enums.ActionType;
import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A fine-grained permission: an {@link ActionType} on a resource, e.g. {@code USER} + {@code READ}.
 * It is granted to roles and checked with {@code @PreAuthorize("hasAuthority('USER:READ')")};
 * see {@link #toAuthority()}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uc_privilege_resource_action", columnNames = {"resource", "action"}))
public class Privilege extends BaseEntity {

    /** Stored upper case (normalised by PrivilegeService), e.g. {@code USER_GROUP}. */
    @Column(nullable = false, length = 50)
    private String resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType action;

    private String description;

    @ManyToMany(mappedBy = "privileges")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Role> roles = new HashSet<>();

    /** The Spring Security authority string, e.g. {@code USER:READ}. */
    public String toAuthority(){
        return this.resource.toUpperCase(Locale.ROOT) + ":" + this.action.name();
    }
}
