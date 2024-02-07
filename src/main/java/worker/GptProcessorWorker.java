package worker;

import core.Database;
import core.Phrase;

import gpt.GptResponse;
import gpt.GptService;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import persistence.TriggerPhraseRepository;
import userresponse.MessageSender;

import java.util.List;


@Component
public class GptProcessorWorker {
    private final Database database;
    private final GptService gptService;
    private final MessageSender messageSender;
    private final TriggerPhraseRepository triggerPhraseRepository;

    private static final Logger logger = LoggerFactory.getLogger(GptProcessorWorker.class);


    @Autowired
    public GptProcessorWorker(Database database, GptService gptService, MessageSender messageSender, TriggerPhraseRepository triggerPhraseRepository) {
        this.database = database;
        this.gptService = gptService;
        this.messageSender = messageSender;
        this.triggerPhraseRepository = triggerPhraseRepository;
    }

    @Scheduled(fixedDelay = 5000)
    private void processQueue() {

        try {
            logger.info("Checked the phrases table. Nothing new for processing.");
            List<Phrase> phrasesToProcess = triggerPhraseRepository.getPhrasesToProcess();
            for (Phrase phrase : phrasesToProcess) {
                if (!triggerPhraseRepository.isPhraseProcessed(phrase.getPhraseId())) {
                    logger.info("New raw phrase detected");
                    GptResponse gptResponse = gptService.getGPTResponseForTriggerWord(phrase.getPhraseText());
                    messageSender.sendResponseToUser(phrase.getUserId(), gptResponse.getFullResponse());
                    database.markPhraseAsProcessed(phrase.getPhraseId());

                }
            }
        } catch (Exception e) {
            logger.error("Error processing queue: ", e);
        }
    }
}