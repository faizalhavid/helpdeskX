package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
public class Role extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    private String description;

    @OneToMany(mappedBy = "role")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_privilege",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    @BatchSize(size = 50)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Privilege> privileges = new HashSet<>();
}
