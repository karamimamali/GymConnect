package com.gymconnect.workload.dto;

/**
 * Whether an incoming workload event adds a training session to a trainer's
 * monthly total or removes one (e.g. when a training is cancelled or the owning
 * trainee is deleted).
 */
public enum ActionType {
    ADD,
    DELETE
}
