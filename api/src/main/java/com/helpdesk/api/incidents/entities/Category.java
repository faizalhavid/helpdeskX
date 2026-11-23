package com.helpdesk.api.incidents.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.helpdesk.api.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    @JsonBackReference
    private Category subCategory;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Incident> incidents;
}
