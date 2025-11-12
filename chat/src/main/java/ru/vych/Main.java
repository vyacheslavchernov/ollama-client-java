package ru.vych;

import ru.vych.dto.rq.generate.GenerateRequestBody;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var client = new OllamaClient();
        var avail = client.availableModels();
        var version = client.version();

        for (var model : avail.getModels()) {
            client.modelDetails(model);
        }

        var modelName = avail.getModels().get(0).getName();

        var generationParams = new GenerateRequestBody(modelName)
                .setPrompt("Привет")
                .setStream(false)
                .setKeepAlive("1m");

        var generation = client.generateResponse(generationParams);

        var genStream = client.generateAsyncResponse(generationParams).thenAccept(stream -> {
            try (stream) {
                stream.forEach(dto -> System.out.print(dto.getResponse()));
            }
        }).get();

        var bankAgent = new BankAgent(client, avail.getModels().getFirst());

        bankAgent.chat("Спиши со счёта 50 рублей");
        bankAgent.chat("Зачисли на счёт 150 рублей");
        bankAgent.chat("Вычти со счёта 500000 рублей");


        var loaded = client.loadedModels();

        bankAgent.getMessages().forEach(msg -> System.out.printf(
                        "\n=======================================\n<think>%s</think>\n\n<content>%s</content>\n",
                        msg.getThinking(), msg.getContent()
                )
        );

        int a = 2;
    }
}