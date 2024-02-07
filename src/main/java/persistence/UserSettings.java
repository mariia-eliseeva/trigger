package persistence;

public class UserSettings {
    private long userId;
    private String language;
    private String city;

    public UserSettings(long userId, String language, String city) {
        this.userId = userId;
        this.language = language;
        this.city = city;
    }

    public long getUserId() {
        return userId;
    }

    public String getLanguage() {
        return language;
    }

    public String getCity() {
        return city;
    }
}
