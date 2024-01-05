package core;

import java.sql.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Component
public class Database {
    private final String dbUrl;

    public Database(@Value("${database.url}") String dbUrl) {
        this.dbUrl = dbUrl;
    }
    private Connection connect(String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl);
            Statement statement = conn.createStatement();
            statement.execute("PRAGMA busy_timeout = 10000;");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    public long savePhrase(long userId, String phrase, LocalDateTime phraseDate) {
        String sql = "INSERT INTO phrases (user_id, phrase, phrase_date) VALUES (?, ?, ?)";
        long phraseID = -1;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = connect(dbUrl);
            conn.setAutoCommit(false); // Отключение автокоммита

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


    public boolean addToQueueToProcess(long userId, long phraseID, String phraseText) {
        String sql = "INSERT INTO processing_phrase_queue (user_id, phrase_id, status, phrase_text) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setLong(2, phraseID);
            pstmt.setString(3, "waiting for GPT-processing");
            pstmt.setString(4, phraseText);
            pstmt.executeUpdate();
            System.out.println("A phrase with ID " + phraseID + " has been added for processing for  Chat ID = " + userId);
            return true;
        } catch (SQLException e) {
            System.out.println("Error when adding a phrase for processing: " + e.getMessage());
            return false;
        }
    }


    public boolean markPhraseAsProcessed(long phraseID) {
        String sql = "UPDATE phrases SET has_processed_by_gpt = ? WHERE phrase_id = ?";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, true);
            pstmt.setLong(2, phraseID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("A phrase with ID = " + phraseID + " has been processed.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error when processing a phrase: " + e.getMessage());
            return false;
        }
    }


    public boolean markResultAsSentToUser(long phraseID) {
        String sql = "UPDATE phrases SET has_sent_result_to_user = ? WHERE phrase_id = ?";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, true);
            pstmt.setLong(2, phraseID);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("The result for the phrase ID " + phraseID + " is sent to the user.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error when updating result submission status: " + e.getMessage());
            return false;
        }
    }


    public boolean removeFromGptQueue(long phraseID) {
        String sql = "DELETE FROM processing_phrase_queue WHERE phrase_id = ?";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, phraseID);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Phrase with ID " + phraseID + " has been removed from the GPT queue.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error when deleting phrase from GPT queue: " + e.getMessage());
            return false;
        }
    }


    public List<Phrase> getPhrasesToProcess() {
        String sql = "SELECT p.phrase_id, p.user_id, p.phrase as phrase_text, q.status, q.gpt_response " +
                "FROM processing_phrase_queue q " +
                "INNER JOIN phrases p ON q.phrase_id = p.phrase_id " +
                "WHERE q.status = 'waiting for GPT-processing'";
        List<Phrase> phrases = new ArrayList<>();

        try (Connection conn = connect(dbUrl);
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


    public void updateProcessedPhrase(long phraseId, String gptResponse) {
        String sql = "UPDATE processing_phrase_queue SET status = 'processed', gpt_response = ? WHERE phrase_id = ?";

        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gptResponse);
            pstmt.setLong(2, phraseId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Phrase with ID  " + phraseId + " updated in the GPT processing queue.");
            } else {
                System.out.println("Failed to update phrase with ID " + phraseId);
            }
        } catch (SQLException e) {
            System.out.println("Error updating phrase: " + e.getMessage());
        }
    }


    public long saveAndSendWord(long userId, String word, LocalDateTime dateSentWord, boolean sentWordToUSer, long phraseID) {
        String sql = "INSERT INTO words (user_id, word, date_sent_word, has_sent_word_to_user, phrase_id) VALUES (?, ?, ?, ?, ?)";
        long wordID = -1;
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, word);
            pstmt.setObject(3, dateSentWord);
            pstmt.setBoolean(4, sentWordToUSer);
            pstmt.setLong(5, phraseID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        wordID = rs.getLong(1);
                    }
                }
            }
            System.out.println("Phrase has been sent to Chat ID = " + userId);
        } catch (SQLException e) {
            System.out.println("Error sending phrase: " + e.getMessage());
        }
        return wordID;
    }

    public boolean isPhraseProcessed(long phraseId) {
        String sql = "SELECT status FROM processing_phrase_queue WHERE phrase_id = ?";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, phraseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                return status.equals("processed");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}