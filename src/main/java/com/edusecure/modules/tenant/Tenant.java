package com.edusecure.modules.tenant;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", unique = true, nullable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String description;

    // nom Java camelCase → nom colonne SQL snake_case
    @Column(name = "admin_email", nullable = false, length = 200)
    private String adminEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "db_url", length = 500)
    private String dbUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public void softDelete() {
        this.isDeleted = true;
        this.status    = Status.INACTIVE;
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status) && !this.isDeleted;
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        PENDING,
        DELETED
    }
}