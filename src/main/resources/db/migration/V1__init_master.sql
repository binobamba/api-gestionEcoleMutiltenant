
CREATE TABLE tenants
(
    id          UUID         NOT NULL,
    tenant_id   VARCHAR(50)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(200),
    admin_email VARCHAR(200) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    db_url      VARCHAR(500),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    is_deleted  BOOLEAN      NOT NULL,
    CONSTRAINT pk_tenants PRIMARY KEY (id)
);