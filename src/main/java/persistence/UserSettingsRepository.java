package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class UserSettingsRepository {

    @Value("${database.url}")
    private String dbUrl;

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public void saveUserSettings(UserSettings userSetting) throws SQLException {
        String sql = "UPDATE user_settings SET language = ?, city = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userSetting.getLanguage());
            pstmt.setString(2, userSetting.getCity());
            pstmt.setLong(3, userSetting.getUserId());
            pstmt.executeUpdate();
        }
    }

    public UserSettings getUserSettings(long userId) throws SQLException {
        String sql = "SELECT * FROM user_settings WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserSettings(userId, rs.getString("language"), rs.getString("city"));
            }
        }
        return null;
    }
}
