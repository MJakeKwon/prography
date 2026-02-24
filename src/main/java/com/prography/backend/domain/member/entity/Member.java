package com.prography.backend.domain.member.entity;

import com.prography.backend.global.common.BaseEntity;
import com.prography.backend.global.common.MemberRole;
import com.prography.backend.global.common.MemberStatus;
import jakarta.persistence.*;

@Entity
@Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(name = "uk_members_login_id", columnNames = "login_id")
)
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role = MemberRole.MEMBER;

    protected Member() {}

    public Member(String loginId, String passwordHash, String name, String phone, MemberRole role) {
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public MemberStatus getStatus() { return status; }
    public MemberRole getRole() { return role; }

    public void withdraw() { this.status = MemberStatus.WITHDRAWN; }
    public boolean isWithdrawn() { return status == MemberStatus.WITHDRAWN; }
}