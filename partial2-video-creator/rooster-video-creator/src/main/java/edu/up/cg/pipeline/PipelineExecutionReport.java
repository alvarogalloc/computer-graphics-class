package edu.up.cg.pipeline;

import java.util.List;

public final class PipelineExecutionReport {
    private final List<StepExecutionResult> stepResults;

    public PipelineExecutionReport(List<StepExecutionResult> stepResults) {
        this.stepResults = List.copyOf(stepResults);
    }

    public List<StepExecutionResult> getStepResults() {
        return stepResults;
    }

    public boolean isSuccessful() {
        for (StepExecutionResult result : stepResults) {
            if (!result.isCompleted()) {
                return false;
            }
        }
        return true;
    }
}
