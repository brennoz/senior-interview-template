package com.elemental.tech.workitems.domain;

import com.elemental.tech.workitems.exception.WrongStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorkItemTransitionTest {
    @Test
    void validTransition() {
        WorkItem item = new WorkItem("title", "desc", WorkItem.Priority.LOW);
        item.transitionTo(WorkItem.Status.IN_PROGRESS);
        assertEquals(WorkItem.Status.IN_PROGRESS, item.getStatus());
    }

    @Test
    void invalidTransitionThrows() {
        WorkItem item = new WorkItem("title", "desc", WorkItem.Priority.LOW);
        assertThrows(WrongStatusTransitionException.class, () -> item.transitionTo(WorkItem.Status.DONE));
    }
}
