package com.elemental.tech.workitems.api;

import com.elemental.tech.workitems.api.dto.CreateWorkItemRequest;
import com.elemental.tech.workitems.api.dto.UpdateWorkItemStatusRequest;
import com.elemental.tech.workitems.api.dto.WorkItemResponse;
import com.elemental.tech.workitems.domain.WorkItem;
import com.elemental.tech.workitems.service.WorkItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    TODO: please enhance
 */
@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

    private final WorkItemService service;

    public WorkItemController(WorkItemService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkItemResponse create (@Valid @RequestBody CreateWorkItemRequest request) {
        return service.create(request);
    }

    @GetMapping
    public Page<WorkItemResponse> list(
            @RequestParam(required = false) WorkItem.Status status,
            @RequestParam(required = false) WorkItem.Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return service.list(status, priority, pageable);
    }

    @GetMapping("/{id}")
    public WorkItemResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    public WorkItemResponse updateStatus(
            @PathVariable long id,
            @Valid @RequestBody UpdateWorkItemStatusRequest request) {
        return service.updateStatus(id, request.status());
    }
}
