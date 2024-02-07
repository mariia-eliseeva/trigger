package config;

import core.Database;
import gpt.GptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import persistence.TriggerPhraseRepository;
import userresponse.MessageSender;
import worker.GptProcessorWorker;

@Configuration
@EnableScheduling
public class BotConfig {

    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${database.url}")
    private String dbUrl;

    @Bean
    public Database database() {
        return new Database(dbUrl);
    }

    @Bean
    public TelegramBotsApi botsApi(TelegramLongPollingBot telegramBot) throws TelegramApiException {
        logger.info("Initialized Telegram API");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBot);
        return botsApi;
    }

    @Bean
    public TelegramLongPollingBot telegramBot(Database database) {
        TriggerPhraseBot bot = new TriggerPhraseBot(database);
        bot.setBotUsername(botUsername);
        bot.setBotToken(botToken);
        return bot;
    }

    @Bean
    public GptProcessorWorker gptProcessorWorker(Database database, GptService gptService, MessageSender messageSender, TriggerPhraseRepository triggerPhraseRepository) {
        return new GptProcessorWorker(database, gptService, messageSender, triggerPhraseRepository);
    }
}