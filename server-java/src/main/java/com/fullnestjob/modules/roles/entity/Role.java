package com.fullnestjob.modules.roles.entity;

import com.fullnestjob.modules.common.Actor;
import com.fullnestjob.modules.common.BaseAuditEntity;
import com.fullnestjob.modules.permissions.entity.Permission;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	private String name;
	private String description;
	private Boolean isActive = true;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "role_permissions",
		joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id")
	)
	private List<Permission> permissions = new ArrayList<>();

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "_id", column = @Column(name = "created_by_id")),
		@AttributeOverride(name = "email", column = @Column(name = "created_by_email"))
	})
	private Actor createdBy;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "_id", column = @Column(name = "updated_by_id")),
		@AttributeOverride(name = "email", column = @Column(name = "updated_by_email"))
	})
	private Actor updatedBy;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "_id", column = @Column(name = "deleted_by_id")),
		@AttributeOverride(name = "email", column = @Column(name = "deleted_by_email"))
	})
	private Actor deletedBy;

	public String get_id() { return _id; }
	public void set_id(String _id) { this._id = _id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean isActive) { this.isActive = isActive; }
	public List<Permission> getPermissions() { return permissions; }
	public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }
	public Actor getCreatedBy() { return createdBy; }
	public void setCreatedBy(Actor createdBy) { this.createdBy = createdBy; }
	public Actor getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(Actor updatedBy) { this.updatedBy = updatedBy; }
	public Actor getDeletedBy() { return deletedBy; }
	public void setDeletedBy(Actor deletedBy) { this.deletedBy = deletedBy; }
}