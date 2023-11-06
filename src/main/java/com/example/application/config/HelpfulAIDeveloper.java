package com.example.application.config;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.UUID;

public interface HelpfulAIDeveloper {

    @SystemMessage({
            "You are a helpful AI developer who has deep knowledge about LLM and LangChain4J.",
            "Given the source code and some examples, help the user about any question about LangChain4J"
    })
    TokenStream chat(@MemoryId UUID memoryId, @UserMessage String userMessage);
}
