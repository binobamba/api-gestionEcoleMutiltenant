package com.edusecure.modules.tenant;
import com.edusecure.core.exception.BusinessException;
import com.edusecure.core.tenant.TenantProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository       tenantRepository;
    private final TenantProvisioningService provisioningService;

    // ─── Créer un tenant ─────────────────────────────────────────────
    @Transactional
    public TenantDto.Response create(TenantDto.CreateRequest req) {

        // Vérifications unicité
        if (tenantRepository.existsByTenantId(req.getTenantId())) {
            throw BusinessException.conflict(
                    "TenantId déjà utilisé : " + req.getTenantId());
        }
        if (tenantRepository.existsByAdminEmail(req.getAdminEmail())) {
            throw BusinessException.conflict(
                    "Email déjà utilisé : " + req.getAdminEmail());
        }

        // Sauvegarde en BDD master avec statut PENDING
        Tenant tenant = Tenant.builder()
                .tenantId(req.getTenantId())
                .name(req.getName())
                .description(req.getDescription())
                .adminEmail(req.getAdminEmail())
                .status(Tenant.Status.PENDING)
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("Tenant créé en BDD : {}", tenant.getTenantId());

        // Provisionnement : crée la BDD + exécute Flyway
        try {
            provisioningService.provisionTenant(tenant.getTenantId());
            tenant.setStatus(Tenant.Status.ACTIVE);
            tenant = tenantRepository.save(tenant);
            log.info("Tenant provisionné avec succès : {}", tenant.getTenantId());

        } catch (Exception e) {
            // Si le provisionnement échoue, on marque le tenant en erreur
            tenant.setStatus(Tenant.Status.INACTIVE);
            tenantRepository.save(tenant);
            log.error("Erreur provisionnement tenant : {}", tenant.getTenantId(), e);
            throw BusinessException.badRequest(
                    "Erreur lors du provisionnement : " + e.getMessage());
        }

        return toResponse(tenant);
    }

    // ─── Récupérer par UUID ──────────────────────────────────────────
    public TenantDto.Response getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    // ─── Récupérer par tenantId ──────────────────────────────────────
    public TenantDto.Response getByTenantId(String tenantId) {
        Tenant tenant = tenantRepository
                .findByTenantIdAndIsDeletedFalse(tenantId)
                .orElseThrow(() -> BusinessException.notFound(
                        "Tenant introuvable : " + tenantId));
        return toResponse(tenant);
    }

    // ─── Lister tous les tenants ─────────────────────────────────────
    public List<TenantDto.Summary> getAll() {
        return tenantRepository.findAllByIsDeletedFalse()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    // ─── Rechercher ──────────────────────────────────────────────────
    public List<TenantDto.Summary> search(String query) {
        if (!StringUtils.hasText(query)) return getAll();
        return tenantRepository.search(query)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    // ─── Mettre à jour ───────────────────────────────────────────────
    @Transactional
    public TenantDto.Response update(UUID id, TenantDto.UpdateRequest req) {
        Tenant tenant = findOrThrow(id);

        if (StringUtils.hasText(req.getName())) {
            tenant.setName(req.getName());
        }
        if (StringUtils.hasText(req.getDescription())) {
            tenant.setDescription(req.getDescription());
        }
        if (StringUtils.hasText(req.getAdminEmail())) {
            tenant.setAdminEmail(req.getAdminEmail());
        }
        if (req.getStatus() != null) {
            tenant.setStatus(req.getStatus());

            // Suspendre le tenant = fermer son DataSource
            if (Tenant.Status.INACTIVE.equals(req.getStatus())) {
                provisioningService.suspendTenant(tenant.getTenantId());
            }
        }

        return toResponse(tenantRepository.save(tenant));
    }

    // ─── Supprimer (soft delete) ─────────────────────────────────────
    @Transactional
    public void delete(UUID id) {
        Tenant tenant = findOrThrow(id);
        tenant.softDelete();
        tenantRepository.save(tenant);

        // Ferme le pool de connexions du tenant
        provisioningService.suspendTenant(tenant.getTenantId());
        log.info("Tenant supprimé (soft) : {}", tenant.getTenantId());
    }

    // ─── Utilitaires privés ──────────────────────────────────────────
    private Tenant findOrThrow(UUID id) {
        return tenantRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> BusinessException.notFound(
                        "Tenant introuvable : " + id));
    }

    private TenantDto.Response toResponse(Tenant t) {
        return TenantDto.Response.builder()
                .id(t.getId())
                .tenantId(t.getTenantId())
                .name(t.getName())
                .description(t.getDescription())
                .adminEmail(t.getAdminEmail())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TenantDto.Summary toSummary(Tenant t) {
        return TenantDto.Summary.builder()
                .id(t.getId())
                .tenantId(t.getTenantId())
                .name(t.getName())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .build();
    }
}