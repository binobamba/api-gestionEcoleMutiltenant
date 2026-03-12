package com.edusecure.modules.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    // Trouve un tenant par son tenantId (ex: "lycee-hugo")
    Optional<Tenant> findByTenantIdAndIsDeletedFalse(String tenantId);

    // Trouve un tenant par son id UUID
    Optional<Tenant> findByIdAndIsDeletedFalse(UUID id);

    // Liste tous les tenants actifs
    List<Tenant> findAllByIsDeletedFalse();

    // Vérifie si un tenantId existe déjà
    boolean existsByTenantId(String tenantId);

    // Vérifie si un email admin existe déjà
    boolean existsByAdminEmail(String adminEmail);

    // Recherche par nom ou tenantId
    @Query("""
            SELECT t FROM Tenant t
            WHERE t.isDeleted = false
            AND (
                LOWER(t.name)     LIKE LOWER(CONCAT('%', :q, '%'))
                OR
                LOWER(t.tenantId) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    List<Tenant> search(@Param("q") String query);

    // Tous les tenants actifs uniquement
    @Query("""
            SELECT t FROM Tenant t
            WHERE t.isDeleted = false
            AND t.status = 'ACTIVE'
            """)
    List<Tenant> findAllActive();
}