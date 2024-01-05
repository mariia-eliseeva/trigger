package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import core.Database;
import core.TriggerPhraseBot;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

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
    public TelegramBotsApi botsApi(TelegramLongPollingBot telegramBot) throws TelegramApiException {
        logger.info("Initialized Telegram API");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBot);
        return botsApi;
    }

    @Bean
    public TelegramLongPollingBot telegramBot() {
        Database database = new Database(dbUrl);
        TriggerPhraseBot bot = new TriggerPhraseBot(database);
        bot.setBotUsername(botUsername);
        bot.setBotToken(botToken);
        return bot;
    }

    @Bean
    public GptProcessorWorker gptProcessorWorker(@Value("${gpt.api.key}") String gptApiKey,
                                                 @Value("${database.url}") String dbUrl,
                                                 MessageSender messageSender) {
        return new GptProcessorWorker(gptApiKey, dbUrl, messageSender);
    }
}