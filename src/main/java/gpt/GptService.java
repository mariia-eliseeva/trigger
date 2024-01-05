package gpt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import config.GptProcessorWorker;

import java.time.Duration;
import java.util.List;

public class GptService {
    private final OpenAiService openAiService;
    private static final Logger logger = LoggerFactory.getLogger(GptProcessorWorker.class);


    public GptService(String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }


    public GptResponse getGptResponse(String phraseText) {

        try {
            String prompt = "Проанализируй следующую фразу на наличие триггерных слов и определи, какое конкретно слово триггерное. Объясняю: Ниже я пишу токсичную фразу, триггерное (манипулятивное) слово, и объясняю почему это слово триггерное. Если во фразе есть глагол и перед ним частица 'не', то нельзя разделять частицу и глагол. Если фраза на английском, то и отвечать надо на английском " +
                    "1. фраза: мне было стыдно за тебя. слово: мне. причина: стыд другого человека за тебя не несет ценности и необходимости это говорить. 2. фраза: че ты несешь. слово: че. причина: нет необходимости отвечать на эту фразу, вся фраза - обесценивание, а слово 'че' усиливает и является триггерным. " +
                    "3. фраза: вокруг меня всегда были умные люди . слово: всегда. причина: это неправда, не может быть такого что заходишь и везде одни умные, токсичность в том, чтобы указать, что конкретно тот кому говорят  эту фразу не умный. " +
                    "4.  фраза: ты что не понимаешь. слово: что (частица). причина: указать факт того, что человек не понимает, не выясняя причину и не пытаясь помочь разобраться, без этого слова есть заинтересованность объяснить, значит оно триггерное. " +
                    "5. фраза: давай лучше пойдем в кино. слово: лучше. причина: то что сказал второй человек - плохо, а автор фразы знает лучше, и вместо того чтобы сказать что ему не нравится идея по таким-то причинам, он обесценивает что сказал собеседник словом лучше - значит оно триггерное. " +
                    "6. фраза: я почитал правила и получается надо спросить. слово: получается. причина: спрашивать не надо, нужно указать человеку что из правил это не получается, а это он манипулирует и инфантильно себя ведет. " +
                    "Ты должен выдать только триггерное слово, без объяснения. Шаблон твоего ответа пользователю (после двоеточия ответ). - 'В данной фразе триггерное слово: '" +
                    "7. фраза: я хочу пойти в кино. слово: я. причина: если эта фраза является не ответом на вопрос 'что ты хочешь', то она триггерная, потому что человек выражает только свое личное желание. " +
                    "Ты должен выдать триггерное слово и объяснение обязательно. Шаблон твоего ответа пользователю (после двоеточия ответ).  В данной фразе триггерное слово: ' Причина:  .";
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-3.5-turbo")
                    .messages(
                            List.of(
                                    new ChatMessage("system", prompt),
                                    new ChatMessage("user", phraseText)))
                    .build();

            logger.info("The request to GPT was made for the phrase: {}", phraseText);


            StringBuilder builder = new StringBuilder();
            String[] triggerWord = new String[1];
            openAiService.createChatCompletion(chatCompletionRequest)
                    .getChoices().forEach(choice -> {
                        String response = choice.getMessage().getContent();
                        builder.append(response);

                        if (response.contains("триггерное слово:") || response.contains("trigger word:")) {
                            triggerWord[0] = response.substring(response.indexOf("триггерное слово:") + "триггерное слово:".length()).trim().split(" ")[0];
                        }
                        System.out.println("Trigger word: " + triggerWord);

                    });

            String fullResponse = builder.toString();

            logger.info("Received response from GPT. Trigger word: {}", triggerWord[0]);

            return new GptResponse(fullResponse, triggerWord[0]);



        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error during requesting GPT-3: " + e.getMessage());
        }
    }
}
