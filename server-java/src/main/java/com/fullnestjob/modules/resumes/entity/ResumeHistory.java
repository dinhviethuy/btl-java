package com.fullnestjob.modules.resumes.entity;

import com.fullnestjob.modules.common.Actor;
import jakarta.persistence.*;

import java.util.Date;

@Embeddable
public class ResumeHistory {
    private String status;
    private Date updatedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "_id", column = @Column(name = "updated_by_id")),
            @AttributeOverride(name = "email", column = @Column(name = "updated_by_email"))
    })
    private Actor updatedBy;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Actor getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Actor updatedBy) { this.updatedBy = updatedBy; }
}


