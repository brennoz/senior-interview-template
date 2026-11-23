package com.elemental.tech.workitems.domain;

import java.time.Instant;

import com.elemental.tech.workitems.exception.WrongStatusTransitionException;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

/*
    TODO: Refactor this, use a library that will simplify this if we can?.
 */

@Getter
@Entity
@Table(name = "work_item")
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Priority priority;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WorkItem() {
    }

    public WorkItem(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = Status.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void transitionTo(Status newStatus) {
        boolean allowed =
                (this.status == Status.OPEN        && (newStatus == Status.IN_PROGRESS || newStatus == Status.CANCELLED)) ||
                        (this.status == Status.IN_PROGRESS && (newStatus == Status.DONE        || newStatus == Status.CANCELLED));

        if (!allowed) {
            throw new WrongStatusTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public enum Status {
        OPEN,
        IN_PROGRESS,
        DONE,
        CANCELLED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
