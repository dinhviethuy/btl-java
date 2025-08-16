package com.fullnestjob.modules.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Actor {
    @Column(name = "actor_id")
    private String _id;

    @Column(name = "actor_email")
    private String email;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


