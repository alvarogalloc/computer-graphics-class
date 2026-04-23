package edu.up.cg.integrations.ai;

import java.nio.file.Path;

public abstract class AIService {

    public abstract String generateEssenceImagePrompt(String mediaSummary);

    public abstract String generateNarrationScript(String timelineSummary);

    public abstract String generateInspirationalPhrase(String placesSummary);

    public abstract void generateImage(String prompt, Path outputFile);
}
