package userresponse;

import config.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
public class MessageSender {

    @Autowired
    private TelegramBotService telegramBotService;

    public void sendResponseToUser(long userId, String response) {
        System.out.println("Reply sent to Chat ID " + userId);
        telegramBotService.sendMessage(userId, response);
    }


}
