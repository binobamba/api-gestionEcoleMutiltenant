package com.edusecure.modules.tenant;


import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class TenantDto {

    // ─── Création d'un tenant ────────────────────────────────────────
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "L'identifiant est obligatoire")
        @Size(min = 3, max = 50, message = "Entre 3 et 50 caractères")
        @Pattern(
                regexp = "^[a-z0-9_-]{3,50}$",
                message = "Minuscules, chiffres, tirets et underscores uniquement"
        )
        private String tenantId;

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200)
        private String name;

        @Size(max = 200)
        private String description;

        @NotBlank(message = "L'email admin est obligatoire")
        @Email(message = "Email invalide")
        private String adminEmail;
    }

    // ─── Mise à jour d'un tenant ─────────────────────────────────────
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Size(max = 200)
        private String name;

        @Size(max = 200)
        private String description;

        @Email(message = "Email invalide")
        private String adminEmail;

        private Tenant.Status status;
    }

    // ─── Réponse API ─────────────────────────────────────────────────
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private UUID           id;
        private String         tenantId;
        private String         name;
        private String         description;
        private String         adminEmail;
        private Tenant.Status  status;
        private LocalDateTime  createdAt;
        private LocalDateTime  updatedAt;
    }

    // ─── Résumé (liste) ──────────────────────────────────────────────
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        private UUID          id;
        private String        tenantId;
        private String        name;
        private Tenant.Status status;
        private LocalDateTime createdAt;
    }
}