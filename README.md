**Trigger Phrase Bot**


**Overview**

The Trigger Phrase Bot is a Telegram bot specialized in identifying trigger words within potentially toxic phrases. Currently functioning in English and Russian, it leverages OpenAI's GPT for zero-shot learning to intelligently parse and analyze text, pinpointing words that could trigger negative reactions. This tool is vital for enhancing positive and respectful communication.

**Future Scope**

Plans are underway to develop a custom Large Language Model (LLM) for more refined detection capabilities and to expand language support.



**Features**

* Phrase Analysis: Detects trigger words in user-provided text, focusing on nuances in the English and Russian languages.
* AI-Powered: Employs ChatGPT for sophisticated natural language processing.
* User-Friendly: Offers easy interaction with a straightforward start-up message.

**Requirements**
* Java JDK 11 or higher.
* Maven
* Telegram Bot API key.
* OpenAI API key.

**Installation**

1. Clone the repository: git clone [repository link]
2. Navigate to the project directory: cd [project name]
3. Install dependencies: mvn install


**Configuration**
* Create a .env file in the root directory of the project.
* Add the following variables with your API keys to the file:
```bash

bot.token=YOUR_TELEGRAM_BOT_TOKEN
bot.username=YOUR_TELEGRAM_BOT_USERNAME
database.url=YOUR_DATABASE_URL
gpt.api.key=YOUR_OPENAI_API_KEY

```

**Usage**
* To start, push the button /start to the bot in Telegram.
* Follow the bot's instructions to record and analyze phrases.
