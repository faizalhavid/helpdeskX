package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity(name = "user_group")
public class UserGroup extends BaseEntity {
    @Column(unique = true,  nullable = false)
    private String name;

    private String description;

    @Column(unique = true,  nullable = true)
    private String email;

    @OneToMany(mappedBy = "userGroup")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users;
}
