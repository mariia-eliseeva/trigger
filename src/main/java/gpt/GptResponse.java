package gpt;

public class GptResponse {
    private final String fullResponse;
    private final String triggerWord;


    public GptResponse(String fullResponse, String triggerWord) {
        this.fullResponse = fullResponse;
        this.triggerWord = triggerWord;
    }

    public String getFullResponse() {
        return fullResponse;
    }

    public String getTriggerWord() {
        return triggerWord;
    }
}
