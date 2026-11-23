-- =====================================================================
-- Incidents module: categories and incidents.
-- =====================================================================

-- ---------------------------------------------------------------------
-- category
-- sub_category_id points to the PARENT category (Category.subCategory),
-- so top-level categories have NULL here.
-- ---------------------------------------------------------------------
CREATE TABLE category (
    id              UUID         NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    name            VARCHAR(255),
    description     VARCHAR(255),
    sub_category_id UUID,
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT fk_category_sub_category FOREIGN KEY (sub_category_id) REFERENCES category (id)
);

CREATE INDEX idx_category_sub_category ON category (sub_category_id);

-- ---------------------------------------------------------------------
-- incident
-- user_id is the assignee (Incident.assignedTo); NULL while unassigned.
-- ---------------------------------------------------------------------
CREATE TABLE incident (
    id            UUID         NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMP WITHOUT TIME ZONE,
    ticket_number VARCHAR(255) NOT NULL,
    title         VARCHAR(255),
    description   VARCHAR(255),
    category_id   UUID,
    status        VARCHAR(255) NOT NULL DEFAULT 'NEW',
    priority      VARCHAR(255),
    user_id       UUID,
    CONSTRAINT pk_incident PRIMARY KEY (id),
    CONSTRAINT uc_incident_ticket_number UNIQUE (ticket_number),
    CONSTRAINT ck_incident_status
        CHECK (status IN ('NEW', 'ASSIGNED', 'IN_PROGRESS', 'PENDING', 'RESOLVED', 'CANCELED')),
    CONSTRAINT ck_incident_priority
        CHECK (priority IS NULL OR priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT fk_incident_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_incident_user FOREIGN KEY (user_id) REFERENCES "user" (id)
);

CREATE INDEX idx_incident_category ON incident (category_id);
CREATE INDEX idx_incident_user ON incident (user_id);
CREATE INDEX idx_incident_status ON incident (status);
CREATE INDEX idx_incident_priority ON incident (priority);
