package edu.up.cg.pipeline;

public interface PipelineProgressListener {
    void onPipelineStarted(PipelineContext context, int totalSteps);

    void onStepStarted(PipelineStep step, int index, int totalSteps);

    void onStepFinished(StepExecutionResult result, int index, int totalSteps);

    void onPipelineFinished(PipelineExecutionReport report);

    static PipelineProgressListener noOp() {
        return new PipelineProgressListener() {
            @Override
            public void onPipelineStarted(PipelineContext context, int totalSteps) {
            }

            @Override
            public void onStepStarted(PipelineStep step, int index, int totalSteps) {
            }

            @Override
            public void onStepFinished(StepExecutionResult result, int index, int totalSteps) {
            }

            @Override
            public void onPipelineFinished(PipelineExecutionReport report) {
            }
        };
    }
}