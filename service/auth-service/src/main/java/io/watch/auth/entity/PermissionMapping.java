package io.watch.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a mapping between HTTP method + path and a permission.
 * This is used to map API endpoints to permissions for authorization.
 */
@Entity
@Table(name = "permission_mappings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"http_method", "path_pattern"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    @Column(name = "path_pattern", nullable = false)
    private String pathPattern;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "description")
    private String description;
}