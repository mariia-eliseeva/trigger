package core;

public class Phrase {
    private long phraseId;
    private long userId;
    private String phraseText;
    private String status;
    private String gptResponse;

    public Phrase(long phraseId, long userId, String phraseText, String status, String gptResponse) {
        this.phraseId = phraseId;
        this.userId = userId;
        this.phraseText = phraseText;
        this.status = status;
        this.gptResponse = gptResponse;
    }

    public long getPhraseId() {
        return phraseId;
    }

    public void setPhraseId(long phraseId) {
        this.phraseId = phraseId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPhraseText() {
        return phraseText;
    }

    public void setPhraseText(String phraseText) {
        this.phraseText = phraseText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGptResponse() {
        return gptResponse;
    }

    public void setGptResponse(String gptResponse) {
        this.gptResponse = gptResponse;
    }
}
