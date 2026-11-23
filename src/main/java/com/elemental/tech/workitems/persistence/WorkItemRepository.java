package com.elemental.tech.workitems.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.elemental.tech.workitems.domain.WorkItem;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    Page<WorkItem> findAll(Pageable pageable);
    Page<WorkItem> findByStatus(WorkItem.Status status, Pageable pageable);
    Page<WorkItem> findByPriority(WorkItem.Priority priority, Pageable pageable);
    Page<WorkItem> findByStatusAndPriority(WorkItem.Status status, WorkItem.Priority priority, Pageable pageable);
}
