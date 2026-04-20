package edu.up.cg.integrations.ai;

public abstract class AIService {

    public abstract String generateEssenceImagePrompt(String mediaSummary);

    public abstract String generateNarrationScript(String timelineSummary);

    public abstract String generateInspirationalPhrase(String placesSummary);
}