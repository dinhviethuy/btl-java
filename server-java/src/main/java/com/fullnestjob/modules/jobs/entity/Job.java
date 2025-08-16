package com.fullnestjob.modules.jobs.entity;

import com.fullnestjob.modules.common.Actor;
import com.fullnestjob.modules.common.BaseAuditEntity;
import com.fullnestjob.modules.companies.entity.Company;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	private String location;
	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id", referencedColumnName = "id"))
	@Column(name = "skill")
	private List<String> skills = new ArrayList<>();

	private Double salary;
	private Integer quantity;
	private String level;
	@jakarta.persistence.Column(columnDefinition = "LONGTEXT")
	private String description;
	private Date startDate;
	private Date endDate;
	private Boolean isActive = true;

	@ManyToOne
	@JoinColumn(name = "company_id", referencedColumnName = "id")
	private Company company;

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
	public String getLocation() { return location; }
	public void setLocation(String location) { this.location = location; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public List<String> getSkills() { return skills; }
	public void setSkills(List<String> skills) { this.skills = skills; }
	public Double getSalary() { return salary; }
	public void setSalary(Double salary) { this.salary = salary; }
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Date getStartDate() { return startDate; }
	public void setStartDate(Date startDate) { this.startDate = startDate; }
	public Date getEndDate() { return endDate; }
	public void setEndDate(Date endDate) { this.endDate = endDate; }
	public Boolean getIsActive() { return isActive; }
	public void setIsActive(Boolean active) { isActive = active; }
	public Company getCompany() { return company; }
	public void setCompany(Company company) { this.company = company; }
}