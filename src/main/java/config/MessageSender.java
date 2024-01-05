package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class MessageSender {

    @Autowired
    private TelegramBotService telegramBotService;

    public void sendResponseToUser(long userId, String response) {
        System.out.println("Reply sent to Chat ID " + userId);
        telegramBotService.sendMessage(userId, response);
    }


}
