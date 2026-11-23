package com.helpdesk.api.incidents.repositories;

import com.helpdesk.api.incidents.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
