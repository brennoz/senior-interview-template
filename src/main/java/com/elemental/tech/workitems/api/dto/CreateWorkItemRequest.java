package com.elemental.tech.workitems.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
    TODO: please enhance with validation
 */
public record CreateWorkItemRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must not be greater than 120 characters")
        String title,

        @Size(max = 2000, message = "Description must not be greater than 2000 characters")
        String description,

        @NotNull(message = "Priority is required")
        Priority priority
) {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
