package com.spring.security.permission;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table (name = "permission")
@NoArgsConstructor
@Getter
public class Permission implements GrantedAuthority, Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column
    private Role description;

    public Permission(String id, Role description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getAuthority() {
        return this.description.name();
    }
}
