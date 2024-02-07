
package persistence;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import core.Phrase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;


@Repository
public class TriggerPhraseRepository {

    @Value("${database.url}")
    private String dbUrl;

    private String currentPhrase;


    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    public void setCurrentPhrase(String phrase) {
        this.currentPhrase = phrase;
    }

    public long savePhrase(long userId, String phrase, LocalDateTime phraseDate) {
        String sql = "INSERT INTO phrases (user_id, phrase, phrase_date) VALUES (?, ?, ?)";
        long phraseID = -1;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = connect();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, userId);
            pstmt.setString(2, phrase);
            pstmt.setObject(3, phraseDate);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        phraseID = rs.getLong(1);
                    }
                }
            }

            conn.commit();

            System.out.println("Фраза сохранена в базу данных для UserID = " + userId);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Ошибка при откате: " + ex.getMessage());
                }
            }
            System.out.println("Ошибка при сохранении фразы: " + e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return phraseID;
    }


    public List<Phrase> getPhrasesToProcess() {
        String sql = "SELECT p.phrase_id, p.user_id, p.phrase as phrase_text, q.status, q.gpt_response " +
                "FROM processing_phrase_queue q " +
                "INNER JOIN phrases p ON q.phrase_id = p.phrase_id " +
                "WHERE q.status = 'waiting for GPT-processing'";
        List<Phrase> phrases = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                long phraseId = rs.getLong("phrase_id");
                long userId = rs.getLong("user_id");
                String phraseText = rs.getString("phrase_text");
                String status = rs.getString("status");
                String gptResponse = rs.getString("gpt_response");

                Phrase phrase = new Phrase(phraseId, userId, phraseText, status, gptResponse);
                phrases.add(phrase);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return phrases;
    }

    public boolean isPhraseProcessed(long phraseId) {
        String sql = "SELECT status FROM phrases WHERE phrase_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, phraseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                if (status != null) {
                    setCurrentPhrase(status);
                    return status.equals("processed");
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}