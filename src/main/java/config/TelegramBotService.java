package config;

import org.springframework.stereotype.Service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Service
public class TelegramBotService {
    private final TelegramLongPollingBot telegramBot;


    public TelegramBotService(TelegramLongPollingBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendMessage(Long userId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText(messageText);
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
