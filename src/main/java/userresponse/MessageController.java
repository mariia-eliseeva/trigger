package userresponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import config.TelegramBotService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private TelegramBotService telegramBotService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageRequest messageRequest) {
        telegramBotService.sendMessage(messageRequest.getUserId(), messageRequest.getMessage());
        return ResponseEntity.ok().build();
    }
}

