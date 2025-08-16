package com.fullnestjob.modules.resumes.entity;

import com.fullnestjob.modules.common.Actor;
import com.fullnestjob.modules.common.BaseAuditEntity;
import com.fullnestjob.modules.companies.entity.Company;
import com.fullnestjob.modules.jobs.entity.Job;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	private String email;
	private String userId;
	private String url;
	private String status;

	@ManyToOne
	@JoinColumn(name = "company_id", referencedColumnName = "id")
	private Company companyId;

	@ManyToOne
	@JoinColumn(name = "job_id", referencedColumnName = "id")
	private Job jobId;

	@ElementCollection
	@CollectionTable(name = "resume_history", joinColumns = @JoinColumn(name = "resume_id", referencedColumnName = "id"))
	private List<ResumeHistory> history = new ArrayList<>();

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
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Company getCompanyId() { return companyId; }
	public void setCompanyId(Company companyId) { this.companyId = companyId; }
	public Job getJobId() { return jobId; }
	public void setJobId(Job jobId) { this.jobId = jobId; }
	public List<ResumeHistory> getHistory() { return history; }
	public void setHistory(List<ResumeHistory> history) { this.history = history; }
}