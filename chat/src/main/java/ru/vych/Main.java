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

        var generationParams = new GenerateRequestBody(avail.getModels().get(1).getName())
                .setPrompt("Привет")
                .setStream(false)
                .setKeepAlive("1m");

        var generation = client.generateResponse(generationParams);

        var genStream = client.generateAsyncResponse(generationParams).thenAccept(stream -> {
            try (stream) {
                stream.forEach(dto -> System.out.print(dto.getResponse()));
            }
        }).get();

        var loaded = client.loadedModels();

        int a = 2;
    }
}