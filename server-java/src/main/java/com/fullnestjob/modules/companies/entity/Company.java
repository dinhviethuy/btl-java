package com.fullnestjob.modules.companies.entity;

import com.fullnestjob.modules.common.Actor;
import com.fullnestjob.modules.common.BaseAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	private String name;
	@jakarta.persistence.Column(columnDefinition = "TEXT")
	private String description;
	private String logo;
	private String address;

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
	public String getLogo() { return logo; }
	public void setLogo(String logo) { this.logo = logo; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public Actor getCreatedBy() { return createdBy; }
	public void setCreatedBy(Actor createdBy) { this.createdBy = createdBy; }
	public Actor getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(Actor updatedBy) { this.updatedBy = updatedBy; }
	public Actor getDeletedBy() { return deletedBy; }
	public void setDeletedBy(Actor deletedBy) { this.deletedBy = deletedBy; }
}