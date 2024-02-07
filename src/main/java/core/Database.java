package core;

import java.sql.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class Database {
    private final String dbUrl;
    private long currentProcessingPhraseId = -1;

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

    public void setCurrentProcessingPhraseId(long phraseId) {
        this.currentProcessingPhraseId = phraseId;
    }

    public long getCurrentProcessingPhraseId() {
        return this.currentProcessingPhraseId;
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
        String sql = "UPDATE processing_phrase_queue SET status = 'processed' WHERE phrase_id = ?";
        try (Connection conn = connect(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, phraseID);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("A phrase with ID = " + phraseID + " has been marked as processed.");
            if (rowsAffected > 0) {
                removeFromGptQueue(phraseID);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error when processing a phrase: " + e.getMessage());
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


}