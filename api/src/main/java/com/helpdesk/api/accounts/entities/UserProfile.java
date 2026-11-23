package com.helpdesk.api.accounts.entities;

import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends BaseEntity {
    @Column(name = "full_name")
    private String fullName;
    private String picture;

    @OneToOne
    @JoinColumn(name= "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
