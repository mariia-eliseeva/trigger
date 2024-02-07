package persistence;



import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserLanguageRepository {

    private Map<Long, String> userLanguages = new HashMap<>();

    public String getUserLanguage(long userId) {
        return userLanguages.get(userId);
    }

    public void setUserLanguage(long userId, String language) {
        userLanguages.put(userId, language);
    }
}