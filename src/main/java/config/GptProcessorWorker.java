package config;

import core.Database;
import core.Phrase;

import gpt.GptResponse;
import gpt.GptService;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Component
public class GptProcessorWorker {
    private static final Logger logger = LoggerFactory.getLogger(GptProcessorWorker.class);
    private Database database;
    private GptService gptService;
    private final MessageSender messageSender;



    public GptProcessorWorker(@Value("${gpt.api.key}") String gptApiKey,
                              @Value("${database.url}") String dbUrl,
                              MessageSender messageSender) {
        this.database = new Database(dbUrl);
        this.gptService = new GptService(gptApiKey);
        this.messageSender = messageSender;

        logger.info("GPT API Key: {}", gptApiKey);
    }




    @Scheduled(fixedDelay = 5000)
    private void processQueue() {

        try {
            logger.info("Checked the phrases table. Nothing new for processing.");
            List<Phrase> phrasesToProcess = database.getPhrasesToProcess();
            for (Phrase phrase : phrasesToProcess) {
                if (!database.isPhraseProcessed(phrase.getPhraseId())) {
                    logger.info("New raw phrase detected");
                    GptResponse gptResponse = gptService.getGptResponse(phrase.getPhraseText());
                    database.updateProcessedPhrase(phrase.getPhraseId(), gptResponse.getFullResponse());
                    messageSender.sendResponseToUser(phrase.getUserId(), gptResponse.getFullResponse());
                    database.markPhraseAsProcessed(phrase.getPhraseId());

                }
            }
        } catch (Exception e) {
            logger.error("Error processing queue: ", e);
        }
    }
}