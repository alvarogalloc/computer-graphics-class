package edu.up.cg.pipeline;

public final class StepExecutionResult {
    private final PipelineStage stage;
    private final String stepName;
    private final boolean completed;
    private final String details;

    public StepExecutionResult(PipelineStage stage, String stepName, boolean completed, String details) {
        this.stage = stage;
        this.stepName = stepName;
        this.completed = completed;
        this.details = details;
    }

    public PipelineStage getStage() {
        return stage;
    }

    public String getStepName() {
        return stepName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getDetails() {
        return details;
    }
}
