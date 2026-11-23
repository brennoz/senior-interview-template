package com.elemental.tech.workitems.api.dto;

import com.elemental.tech.workitems.domain.WorkItem;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkItemStatusRequest(
        @NotNull(message = "Status is required")
        WorkItem.Status status
) {}
