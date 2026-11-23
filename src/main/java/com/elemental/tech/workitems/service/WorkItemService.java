package com.elemental.tech.workitems.service;

import com.elemental.tech.workitems.api.dto.CreateWorkItemRequest;
import com.elemental.tech.workitems.api.dto.WorkItemResponse;
import com.elemental.tech.workitems.domain.WorkItem;
import com.elemental.tech.workitems.exception.WorkItemNotFoundException;
import com.elemental.tech.workitems.persistence.WorkItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class WorkItemService {

    private final WorkItemRepository repository;

    public WorkItemService(WorkItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WorkItemResponse create(CreateWorkItemRequest request) {
        log.info("Creating work item with title='{}', priority={}", request.title(), request.priority());
        WorkItem workItem = new WorkItem(
                request.title(),
                request.description(),
                WorkItem.Priority.valueOf(request.priority().name())
        );
        WorkItem saved = repository.save(workItem);
        log.info("Work item created with id={}", saved.getId());
        return WorkItemMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<WorkItemResponse> list(WorkItem.Status status, WorkItem.Priority priority, Pageable pageable) {
        log.debug("Listing work items — status={}, priority={}, page={}", status, priority, pageable.getPageNumber());
        Page<WorkItem> page;
        if (status != null && priority != null) {
            page = repository.findByStatusAndPriority(status, priority, pageable);
        } else if (status != null) {
            page = repository.findByStatus(status, pageable);
        } else if (priority != null) {
            page = repository.findByPriority(priority, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(WorkItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public WorkItemResponse get(long id) {
        log.debug("Fetching work item id={}", id);
        WorkItem workItem = repository.findById(id)
                .orElseThrow(() -> new WorkItemNotFoundException(id));
        return WorkItemMapper.toResponse(workItem);
    }

    public WorkItemResponse updateStatus(long id, WorkItem.Status newStatus) {
        log.info("Updating status of work item id={} to {}", id, newStatus);
        WorkItem workItem = repository.findById(id)
                .orElseThrow(() -> new WorkItemNotFoundException(id));
        workItem.transitionTo(newStatus);
        WorkItem saved = repository.save(workItem);
        log.info("Work item id={} status updated to {}", id, saved.getStatus());
        return WorkItemMapper.toResponse(saved);
    }
}
