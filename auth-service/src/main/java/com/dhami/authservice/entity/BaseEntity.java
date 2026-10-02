package com.dhami.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/*
Common audit fields centralized
A base entity represents common behavior
 */

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

/*

why abstract -> So abstract expresses intent: This class is only for inheritance.
protected -> Accessible - Same class, same package, child classes, Not accessible from random classes.

Why Abstract Class?

Represents common behavior only.
Not meant to be instantiated.
Avoids meaningless objects.
Provides reusable fields and methods for child entities.

Why Protected Methods?

Accessible to child entities.
Hidden from external classes.
Prevents accidental manual invocation.
Common practice for JPA lifecycle callback methods.

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}

Auditing is the process of automatically tracking entity changes such as when a record was created,
modified, and optionally who performed the operation. In Spring Data JPA, auditing is implemented
using annotations like @CreatedDate, @LastModifiedDate, @CreatedBy, and @LastModifiedBy.

 */
