package com.fullnestjob.modules.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Date;

@MappedSuperclass
public abstract class BaseAuditEntity {

    @Column(name = "created_at")
    protected Date createdAt;

    @Column(name = "updated_at")
    protected Date updatedAt;

    @Column(name = "deleted_at")
    protected Date deletedAt;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
}


