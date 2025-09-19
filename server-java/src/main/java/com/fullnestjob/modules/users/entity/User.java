package com.fullnestjob.modules.users.entity;

import com.fullnestjob.modules.common.Actor;
import com.fullnestjob.modules.common.BaseAuditEntity;
import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.roles.entity.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	@Column(name = "email", unique = true)
	private String email;
	private String password;
	private String address;
	private String name;
	private Integer age;
	private String gender;

	private String avatar; // URL avatar (optional)

	@ManyToOne
	@JoinColumn(name = "company_id", referencedColumnName = "id")
	private Company company;

	@ManyToOne
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String refreshToken;

	// OTP for forgot-password flow
	private String otpCode;
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date otpExpiredAt;

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
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }
	public String getAvatar() { return avatar; }
	public void setAvatar(String avatar) { this.avatar = avatar; }
	public Company getCompany() { return company; }
	public void setCompany(Company company) { this.company = company; }
	public Role getRole() { return role; }
	public void setRole(Role role) { this.role = role; }
	public String getRefreshToken() { return refreshToken; }
	public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
	public String getOtpCode() { return otpCode; }
	public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
	public java.util.Date getOtpExpiredAt() { return otpExpiredAt; }
	public void setOtpExpiredAt(java.util.Date otpExpiredAt) { this.otpExpiredAt = otpExpiredAt; }
	public Actor getCreatedBy() { return createdBy; }
	public void setCreatedBy(Actor createdBy) { this.createdBy = createdBy; }
	public Actor getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(Actor updatedBy) { this.updatedBy = updatedBy; }
	public Actor getDeletedBy() { return deletedBy; }
	public void setDeletedBy(Actor deletedBy) { this.deletedBy = deletedBy; }
}