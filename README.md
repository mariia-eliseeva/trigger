# Trigger Phrase Bot

## Overview

The Trigger Phrase Bot is a Telegram bot designed to identify trigger words within phrases that could potentially cause negative reactions. Utilizing advanced natural language processing powered by OpenAI's GPT, this bot offers support in both English and Russian languages to encourage positive and respectful online communication.

## Screenshots

### Bot Introduction
![Bot Introduction](1.jpeg)

### Language Selection
![Language Selection](2.jpeg)

### Bot Interaction
![Bot Interaction](3.jpeg)

### Analysis Result
![Analysis Result](4.jpeg)

## Features

- **Phrase Analysis**: The bot examines phrases provided by users, highlighting any trigger words.
- **AI-Powered**: Utilizes OpenAI's ChatGPT for intelligent text analysis.
- **Multi-language Support**: Offers services in both English and Russian.
- **User-Friendly**: Easy to use with clear instructions and a straightforward startup process.

## Future Scope

- **Custom Large Language Model**: To further refine detection capabilities.
- **Language Expansion**: To include more languages and broaden the user base.

## Requirements

- Java JDK 11 or higher
- Maven
- Telegram Bot API key
- OpenAI API key

## Installation

```shell
git clone [repository link]
cd [project name]
mvn install
```

## Configuration
Create a .txt file application.properties in the root directory of the project and add the following variables with your respective API keys:
```bash
bot.token=your_telegram_bot_token
bot.username=your_telegram_bot_username
database.url=your_database_url
gpt.api.key=your_openai_api_key
```

## Usage
- Send the /start command to the bot in Telegram.
- Follow the instructions provided by the bot to input and analyze phrases.

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your features or fixes.