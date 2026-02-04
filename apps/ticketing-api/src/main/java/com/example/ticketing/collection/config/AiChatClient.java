package com.example.ticketing.collection.config;

/**
 * AI Chat 추상화 인터페이스
 * - 구현체에 대한 의존 없이 AI 채팅 기능 사용
 */
@FunctionalInterface
public interface AiChatClient {

    String chat(String prompt);
}
