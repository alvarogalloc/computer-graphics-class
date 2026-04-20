package edu.up.cg.pipeline;

public interface PipelineStep {
    String getStepName();

    PipelineStage getStage();

    StepExecutionResult execute(PipelineContext context);
}
