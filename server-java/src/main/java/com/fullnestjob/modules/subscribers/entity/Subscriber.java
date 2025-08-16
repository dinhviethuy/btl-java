package com.fullnestjob.modules.subscribers.entity;

import com.fullnestjob.modules.common.BaseAuditEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscribers")
public class Subscriber extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String _id;

	private String name;
	private String email;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "subscriber_skills", joinColumns = @JoinColumn(name = "subscriber_id", referencedColumnName = "id"))
	@Column(name = "skill")
	private List<String> skills = new ArrayList<>();

	public String get_id() { return _id; }
	public void set_id(String _id) { this._id = _id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public List<String> getSkills() { return skills; }
	public void setSkills(List<String> skills) { this.skills = skills; }
}