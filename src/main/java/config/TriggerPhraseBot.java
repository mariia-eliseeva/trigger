package config;


import core.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import persistence.TriggerPhraseRepository;
import persistence.UserLanguageRepository;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class TriggerPhraseBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private Database databaseTriggerPhraseBot;

    @Autowired
    private TriggerPhraseRepository triggerPhraseRepository;

    @Autowired
    private UserLanguageRepository userLanguageRepository;

    public TriggerPhraseBot(Database database) {
        this.databaseTriggerPhraseBot = database;
    }

    private Map<Long, String> awaitingPhraseInput = new HashMap<>();
    private Map<Long, Boolean> awaitingPhraseText = new HashMap<>();
    private Map<Long, String> userLanguages = new HashMap<>();
    private Map<Long, Integer> messageWithKeyboardIds = new HashMap<>();


    @Override
    public void onUpdateReceived(Update update) {
        long chatId = 0;
        String messageText = null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            messageText = update.getMessage().getText();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            messageText = update.getCallbackQuery().getData();
            handleLanguageSelection(chatId, messageText);
            return;
        }

        // выбран ли язык
        String selectedLanguage = userLanguageRepository.getUserLanguage(chatId);
        if (selectedLanguage == null) {
            // выбрать если нет
            handleStartCommand(chatId);
        }

        if (messageText != null) {
            if (awaitingPhraseText.getOrDefault(chatId, false)) {
                awaitingPhraseInput.put(chatId, messageText);
                processPhrase(chatId, messageText);
            }
        }
    }



    private void handleStartCommand(long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonRu = new InlineKeyboardButton();
        buttonRu.setText("Русский");
        buttonRu.setCallbackData("RU");

        InlineKeyboardButton buttonEn = new InlineKeyboardButton();
        buttonEn.setText("English");
        buttonEn.setCallbackData("EN");

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buttonEn);
        rowInline.add(buttonRu);


        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(rowInline);

        markupInline.setKeyboard(keyboard);

        SendMessage message = new SendMessage(String.valueOf(chatId), "Choose your language | Выберите язык");
        message.setReplyMarkup(markupInline);

        try {
            Message sentMessage = execute(message);
            messageWithKeyboardIds.put(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void handleLanguageSelection(long chatId, String callData) {
        String language = callData.equals("EN") ? "English" : "Русский";
        userLanguageRepository.setUserLanguage(chatId, callData);
        awaitingPhraseText.put(chatId, true);

        String textEn = "Send me a phrase, and I will find the trigger word.";
        String textRu = "Отправь мне фразу, и я укажу слово, которое могло вызвать твою негативную реакцию.";

        String responseText = callData.equals("EN") ? textEn : textRu;

        Integer messageId = messageWithKeyboardIds.get(chatId);
        if (messageId != null) {
            EditMessageText newMessageText = new EditMessageText();
            newMessageText.setChatId(String.valueOf(chatId));
            newMessageText.setMessageId(messageId);
            newMessageText.setText(responseText);
            newMessageText.setReplyMarkup(null);

            try {
                execute(newMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }



    private void sendMessage(long chatId, String textRu, String textEn) {
        String language = userLanguages.getOrDefault(chatId, "EN");
        String text = language.equals("RU") ? textRu : textEn;

        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private void processPhrase(long chatId, String messageText) {
        String phraseText = awaitingPhraseInput.get(chatId);
        if (messageText == null || messageText.isEmpty()) {
            System.out.println("No phrase from Chat ID: " + chatId);
            return;
        }
        long phraseID = triggerPhraseRepository.savePhrase(chatId, messageText, LocalDateTime.now());

        String textRuProcessing = "Ваша фраза принята в обработку. Время ожидания меньше минуты.";
        String textEnProcessing = "Your phrase has been accepted for processing. Waiting time is less than a minute.";
        String textRuFailed = "Не удалось сохранить фразу для обработки.";
        String textEnFailed = "Failed to save the phrase for processing.";

        if (phraseID != -1) {
            databaseTriggerPhraseBot.addToQueueToProcess(chatId, phraseID, phraseText);
            sendMessage(chatId, textRuProcessing, textEnProcessing);
        } else {
            sendMessage(chatId, textRuFailed, textEnFailed);
        }

        awaitingPhraseInput.remove(chatId);
    }



    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void setBotUsername(String botUsername) {
    }

    public void setBotToken(String botToken) {
    }
}