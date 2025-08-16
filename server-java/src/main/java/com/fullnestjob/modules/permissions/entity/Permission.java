package com.fullnestjob.modules.permissions.entity;

import com.fullnestjob.modules.common.Actor;
import com.fullnestjob.modules.common.BaseAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	private String name;
	private String apiPath;
	private String method;
	private String module;

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
	public String getApiPath() { return apiPath; }
	public void setApiPath(String apiPath) { this.apiPath = apiPath; }
	public String getMethod() { return method; }
	public void setMethod(String method) { this.method = method; }
	public String getModule() { return module; }
	public void setModule(String module) { this.module = module; }
	public Actor getCreatedBy() { return createdBy; }
	public void setCreatedBy(Actor createdBy) { this.createdBy = createdBy; }
	public Actor getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(Actor updatedBy) { this.updatedBy = updatedBy; }
	public Actor getDeletedBy() { return deletedBy; }
	public void setDeletedBy(Actor deletedBy) { this.deletedBy = deletedBy; }
}