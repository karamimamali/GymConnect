package com.gymconnect.dto;

/**
 * Action carried by a workload event sent to the reporting microservice: ADD to
 * accrue a training's duration, DELETE to reverse it (e.g. on trainee deletion).
 */
public enum WorkloadActionType {
    ADD,
    DELETE
}
