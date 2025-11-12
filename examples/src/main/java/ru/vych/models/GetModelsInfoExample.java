package ru.vych.models;

import ru.vych.OllamaClient;
import ru.vych.dto.rq.generate.GenerateRequestBody;

/**
 * Пример получения списка доступных моделей,
 * дополнительной информации по этим моделям и списка загруженных в память моделей.
 */
public class GetModelsInfoExample {
    public static void main(String[] args) {
        // Создание клиента
        var client = new OllamaClient();

        // Запрос установленных в Ollama моделей
        var avail = client.availableModels();
        System.out.printf("Первая доступная модель в Ollama:\n%s\n\n", avail.getModels().getFirst());

        // Запрос расширенной информации о модели в Ollama
        var details = client.modelDetails(avail.getModels().getFirst());
        System.out.printf("Детальная информация по первой доступной модели в Ollama:\n%s\n\n", details);

        // Получение списка загруженных в видеопамять моделей
        client.generateResponse(new GenerateRequestBody(details.getName())
                .setPrompt("Hi!")
                .setKeepAlive("1m"));
        var loaded = client.loadedModels();
        System.out.println("Список моделей загруженных в видеопамять:");
        loaded.getModels().forEach(model -> System.out.printf(
                "Модель '%s' будет выгружена [%s]\n",
                model.getName(), model.getExpiresAt())
        );
    }
}
