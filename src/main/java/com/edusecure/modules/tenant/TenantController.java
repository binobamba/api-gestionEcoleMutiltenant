package com.edusecure.modules.tenant;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")  // Tout le controller est protégé
@Tag(name = "Tenants", description = "Gestion des écoles — SUPER_ADMIN uniquement")
public class TenantController {

    private final TenantService tenantService;

    // ─── POST /admin/tenants ─────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Créer un tenants")
    public ResponseEntity<TenantDto.Response> create(
            @Valid @RequestBody TenantDto.CreateRequest req) {
        return ResponseEntity
                .status(201)
                .body(tenantService.create(req));
    }

    // ─── GET /admin/tenants ──────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister tous les tenants")
    public ResponseEntity<List<TenantDto.Summary>> getAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(
                search != null
                        ? tenantService.search(search)
                        : tenantService.getAll()
        );
    }

    // ─── GET /admin/tenants/{id} ─────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un tenant par son UUID")
    public ResponseEntity<TenantDto.Response> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getById(id));
    }

    // ─── GET /admin/tenants/by-tenant-id/{tenantId} ──────────────────
    @GetMapping("/by-tenant-id/{tenantId}")
    @Operation(summary = "Récupérer un tenant par son tenantId (ex: lycee-hugo)")
    public ResponseEntity<TenantDto.Response> getByTenantId(
            @PathVariable String tenantId) {
        return ResponseEntity.ok(tenantService.getByTenantId(tenantId));
    }

    // ─── PUT /admin/tenants/{id} ─────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un tenant")
    public ResponseEntity<TenantDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody TenantDto.UpdateRequest req) {
        return ResponseEntity.ok(tenantService.update(id, req));
    }

    // ─── DELETE /admin/tenants/{id} ──────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un tenant (soft delete)")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
