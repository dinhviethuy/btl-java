package com.fullnestjob.modules.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "registration_otps")
public class RegistrationOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String _id;

    @Column(unique = true)
    private String email;
    private String name;
    private String passwordHash;
    private Integer age;
    private String gender;
    private String address;

    private String otpCode;
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date otpExpiredAt;

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public java.util.Date getOtpExpiredAt() { return otpExpiredAt; }
    public void setOtpExpiredAt(java.util.Date otpExpiredAt) { this.otpExpiredAt = otpExpiredAt; }
}


