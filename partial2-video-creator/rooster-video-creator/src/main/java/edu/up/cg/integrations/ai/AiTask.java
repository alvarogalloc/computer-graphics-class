package edu.up.cg.integrations.ai;

public enum AiTask {
    ESSENCE_IMAGE_PROMPT(
        "Create one highly descriptive, photorealistic, cinematic image-generation prompt that captures the visual essence of the media collection. " +
            "No markdown, no bullets. Context: "
    ),
    NARRATION_SCRIPT(
        "Write a short, engaging voice-over script describing the journey through the locations provided in the coordinates. " +
            "IMPORTANT: You MUST return exactly one sentence of spoken text for each numbered location in the input. " +
            "Separate each location's spoken text exactly with the '|' character. Example output: 'Here we are at location one. | Moving to location two. | Looking at location three.' " +
            "No stage directions, no timestamps, no 'VO:' labels, no markdown, no intro. " +
            "Just the flowing words to be spoken. Context: "
    ),
    INSPIRATIONAL_PHRASE(
        "Write one inspirational phrase tied to the visited places. Max 25 words. Context: "
    );

    private final String promptPrefix;

    AiTask(String promptPrefix) {
        this.promptPrefix = promptPrefix;
    }

    public String promptPrefix() {
        return promptPrefix;
    }
}