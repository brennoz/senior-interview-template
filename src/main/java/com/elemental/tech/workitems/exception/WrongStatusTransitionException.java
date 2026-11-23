package com.elemental.tech.workitems.exception;

import com.elemental.tech.workitems.domain.WorkItem;

public class WrongStatusTransitionException extends RuntimeException {

    public WrongStatusTransitionException(WorkItem.Status from, WorkItem.Status to) {
        super("Status cannot move from " + from + " to " + to);
    }
}
