package gpt;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import worker.GptProcessorWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
public class GptService {
    private final OpenAiService openAiService;
    private static final Logger logger = LoggerFactory.getLogger(GptProcessorWorker.class);

    public GptService(@Value("${gpt.apiKey}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    public GptResponse getGPTResponseForTriggerWord(String phraseText) {

            try {
                String prompt = "Проанализируйте следующие фразы на наличие триггерных слов. Для каждой фразы определите, какое конкретно слово является триггерным. Пожалуйста, обратите внимание: если я задаю вопрос на русском языке, ожидаю вашего ответа также на русском. Если же вопрос будет задан на английском, ответите мне, пожалуйста, на английском. Вот примеры фраз: Объясняю: Ниже я пишу токсичную фразу, триггерное (манипулятивное) слово, и объясняю почему это слово триггерное, и дальше ответ пользователя на эту фразу. Если во фразе есть глагол и перед ним частица 'не', то нельзя разделять частицу и глагол. Если фраза на английском, то и отвечать надо на английском " +
                        "1. фраза: мне было стыдно за тебя. слово: мне. причина: стыд другого человека за тебя не несет ценности и необходимости это говорить. ответная фраза: что значит ТЕБЕ было стыдно? 2. фраза: че ты несешь. слово: че. причина: нет необходимости отвечать на эту фразу, вся фраза - обесценивание, а слово 'че' усиливает и является триггерным. ответная фраза:  что значит че?" +
                        "3. фраза: вокруг меня всегда были умные люди . слово: всегда. причина: это неправда, не может быть такого что заходишь и везде одни умные, токсичность в том, чтобы указать, что конкретно тот кому говорят  эту фразу не умный. ответная фраза: что значит всегда, заходишь в трамвай и там все умные? " +
                        "4.  фраза: ты что не понимаешь. слово: что (частица). причина: указать факт того, что человек не понимает, не выясняя причину и не пытаясь помочь разобраться, без этого слова есть заинтересованность объяснить, значит оно триггерное. ответная фраза: ты хочешь мне помочь понять или указать на факт отсутствия у меня информации по этому вопросу?" +
                        "5. фраза: давай лучше пойдем в кино. слово: лучше. причина: то что сказал второй человек - плохо, а автор фразы знает лучше, и вместо того чтобы сказать что ему не нравится идея по таким-то причинам, он обесценивает что сказал собеседник словом лучше - значит оно триггерное. ответная фраза: почему лучше?  " +
                        "6. фраза: я почитал правила и получается надо спросить. слово: получается. причина: спрашивать не надо, нужно указать человеку что из правил это не получается, а это он манипулирует и инфантильно себя ведет. ответная фраза: исходя из какого конкретно правила получается? " +
                        "7. фраза: я хочу пойти в кино. слово: я. причина: если эта фраза является не ответом на вопрос 'что ты хочешь', то она триггерная, потому что человек выражает только свое личное желание. ответная фраза:  " +
                        "8. фраза: ты должна меня любить. слово: должна. причина: принуждение к действия без уточнения желания. ответная фраза: что значит должна? " +
                        "9. фраза: давай лучше пойдем в кино. слово: лучше. причина: то что сказал второй человек - плохо, а автор фразы знает лучше, и вместо того чтобы сказать что ему не нравится идея по таким-то причинам, он обесценивает что сказал собеседник словом лучше - значит оно триггерное. ответная фраза: почему лучше?  " +
                        "10. фраза: Ты опять сделал это. слово: опять. причина: указание на то, что человек ошибался без указания на факты, из-за этого может возникнуть чувство виновности. ответная фраза: Что значит опять? Про какие ещё примеры ты говоришь.  " +
                        "Шаблон твоего ответа пользователю (после двоеточия ответ).  В данной фразе триггерное слово: ' Причина:  Ответная фраза: ." +
                        "If the phrase is in English, then you must provide a trigger word and an explanation is mandatory. The template of your response to the user (after the colon is the response). In this phrase, the trigger word is: ' Reason: Response phrase: .";


                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest

                        .builder()
                    .model("gpt-3.5-turbo")
//                        .model("gpt-4")

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
