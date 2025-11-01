package ru.vych;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.vych.dto.rq.ApiRequestDTO;
import ru.vych.dto.rq.generate.GenerateRequestBody;
import ru.vych.dto.rq.model.ModelDetailsBody;
import ru.vych.dto.rs.ApiResponseDTO;
import ru.vych.dto.rs.generate.GenerateResponse;
import ru.vych.dto.rs.model.Model;
import ru.vych.dto.rs.model.ModelsList;
import ru.vych.dto.rs.version.Version;

import javax.swing.text.html.FormSubmitEvent.MethodType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static javax.swing.text.html.FormSubmitEvent.MethodType.GET;
import static javax.swing.text.html.FormSubmitEvent.MethodType.POST;

/**
 * Класс реализует методы для обращения к Ollama.
 * Может работать как с локальной установкой(по умолчанию), так и с удалённой.
 *
 * @see <a href="https://docs.ollama.com/api">API Reference</a>
 * См. список доступных методов
 */
@Slf4j
public class OllamaClient {
    public static final String DEFAULT_OLLAMA_PROTOCOL = "http";
    public static final String DEFAULT_OLLAMA_HOST = "localhost";
    public static final String DEFAULT_OLLAMA_PORT = "11434";

    private static final String OLLAMA_URL_PATTERN = "%s://%s:%s/api/";

    //region ENDPOINTS
    private static final String VERSION_ENDPOINT = "version";
    private static final String TAGS_ENDPOINT = "tags";
    private static final String PS_ENDPOINT = "ps";
    private static final String SHOW_ENDPOINT = "show";
    private static final String GENERATE_ENDPOINT = "generate";
    //endregion ENDPOINTS

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final String ollamaUrl;
    private final HttpClient httpClient;

    /**
     * Создаёт клиент со значениями сервера(протокол, хост, порт) по умолчанию
     */
    public OllamaClient() {
        ollamaUrl = String.format(OLLAMA_URL_PATTERN, DEFAULT_OLLAMA_PROTOCOL, DEFAULT_OLLAMA_HOST, DEFAULT_OLLAMA_PORT);
        httpClient = HttpClient.newHttpClient();
        log.debug("Created default Ollama client for {}", ollamaUrl);
    }

    /**
     * Создаёт клиент с определёнными значениями сервера(протокол, хост, порт).
     */
    public OllamaClient(String protocol, String host, String port) {
        ollamaUrl = String.format(OLLAMA_URL_PATTERN, protocol, host, port);
        httpClient = HttpClient.newHttpClient();
        log.debug("Created Ollama client for {}", ollamaUrl);
    }

    /**
     * @return Версия Ollama
     * @see <a href="https://docs.ollama.com/api-reference/get-version">API Reference</a>
     */
    public Version version() {
        return callApi(VERSION_ENDPOINT, GET, Version.class);
    }

    /**
     * @return Список моделей доступных на сервере Ollama
     * @see <a href="https://docs.ollama.com/api/tags">API Reference</a>
     */
    public ModelsList availableModels() {
        return callApi(TAGS_ENDPOINT, GET, ModelsList.class);
    }

    /**
     * @return Список моделей загруженных в память
     * @see <a href="https://docs.ollama.com/api/ps">API Reference</a>
     */
    public ModelsList loadedModels() {
        return callApi(PS_ENDPOINT, GET, ModelsList.class);
    }

    /**
     * Получить подробную информацию о модели.
     * Полученные данные будут дописаны в объект, который был передан в метод.
     *
     * @param model модель для обогащения подробными данными
     * @return обогащённый данными объект модели, который был передан в метод
     * @see <a href="https://docs.ollama.com/api-reference/show-model-details">API Reference</a>
     */
    public Model modelDetails(Model model) {
        return modelDetails(model, false);
    }

    /**
     * Получить подробную информацию о модели.
     * Полученные данные будут дописаны в объект, который был передан в метод.
     *
     * @param model   модель для обогащения подробными данными
     * @param verbose если true, возвращает расширенные данные, включая подробности по слоям, токенизатору и параметрам
     * @return обогащённый данными объект модели, который был передан в метод
     * @see <a href="https://docs.ollama.com/api-reference/show-model-details">API Reference</a>
     */
    // TODO: https://github.com/vyacheslavchernov/ollama-client-java/issues/1 При verbose==true падает. Требуется правка в ModelDetails
    public Model modelDetails(Model model, boolean verbose) {
        return model.copyDetails(modelDetails(model.getName(), verbose));
    }

    /**
     * Получить подробную информацию о модели.
     * Полученные данные будут дописаны в объект, который был передан в метод.
     *
     * @param name имя модели
     * @return подробные данные о модели
     * @see <a href="https://docs.ollama.com/api-reference/show-model-details">API Reference</a>
     */
    public Model modelDetails(String name) {
        return modelDetails(name, false);
    }

    /**
     * Получить подробную информацию о модели.
     * Полученные данные будут дописаны в объект, который был передан в метод.
     *
     * @param name    имя модели
     * @param verbose если true, возвращает расширенные данные, включая подробности по слоям, токенизатору и параметрам
     * @return подробные данные о модели
     * @see <a href="https://docs.ollama.com/api-reference/show-model-details">API Reference</a>
     */
    // TODO: https://github.com/vyacheslavchernov/ollama-client-java/issues/1 При verbose==true падает. Требуется правка в ModelDetails
    public Model modelDetails(String name, boolean verbose) {
        return callApi(SHOW_ENDPOINT, POST, Model.class, new ModelDetailsBody(name, verbose));
    }

    /**
     * Сгенерировать ответ по предоставленным параметрам.
     * Метод для использования с параметром stream==false. Параметр будет принудительно выставлен.
     * Для потокового получения ответа необходимо использовать перегрузку метод {@link OllamaClient#generateAsyncResponse(GenerateRequestBody)}
     *
     * @param parameters параметры генерации, включая промпт
     * @return ответ от Ollama с результатами генерации
     * @see <a href="https://docs.ollama.com/api/generate">Api Reference</a>
     */
    public GenerateResponse generateResponse(GenerateRequestBody parameters) {
        parameters.setStream(false);
        return callApi(GENERATE_ENDPOINT, POST, GenerateResponse.class, parameters);
    }

    /**
     * Сгенерировать ответ по предоставленным параметрам.
     * Метод для использования с параметром stream==true. Параметр будет принудительно выставлен.
     * Для получения ответа без потока необходимо использовать перегрузку метод {@link OllamaClient#generateResponse(GenerateRequestBody)}
     *
     * @param parameters параметры генерации, включая промпт
     * @return поток с частичным ответом генерации
     * @see <a href="https://docs.ollama.com/api/generate">Api Reference</a>
     */
    public CompletableFuture<Stream<GenerateResponse>> generateAsyncResponse(GenerateRequestBody parameters) {
        parameters.setStream(true);
        return callApiAsync(GENERATE_ENDPOINT, POST, GenerateResponse.class, parameters);
    }

    /**
     * Вызов API Ollama
     *
     * @param endpoint    вызываемый эндпоинт API
     * @param method      HTTP метод вызова
     * @param responseDto класс DTO в который будет помещён ответ от API
     * @param payload     нагрузка запроса к API
     * @param <T>         класс DTO в который будет помещён ответ от API
     * @return ответ от API Ollama
     */
    @SneakyThrows
    private <T extends ApiResponseDTO> T callApi(String endpoint, MethodType method, Class<T> responseDto, ApiRequestDTO... payload) {
        var uri = new URI(ollamaUrl + endpoint);
        var rq = buildRequest(uri, method, false, payload);
        var rs = httpClient.send(rq, HttpResponse.BodyHandlers.ofString());
        T rsDto = mapper.readValue(rs.body(), responseDto);
        log.debug("Got response from [{}] : {}", uri, mapper.writeValueAsString(rsDto));
        return rsDto;
    }

    /**
     * Асинхронный вызов API Ollama
     *
     * @param endpoint    вызываемый эндпоинт API
     * @param method      HTTP метод вызова
     * @param responseDto класс DTO в который будет помещён ответ от API
     * @param payload     нагрузка запроса к API
     * @param <T>         класс DTO в который будет помещён ответ от API
     * @return {@link Stream} ответов от API Ollama в виде {@link CompletableFuture}
     */
    @SneakyThrows
    private <T extends ApiResponseDTO> CompletableFuture<Stream<T>> callApiAsync(String endpoint, MethodType method, Class<T> responseDto, ApiRequestDTO... payload) {
        var uri = new URI(ollamaUrl + endpoint);
        var rq = buildRequest(uri, method, true, payload);
        return httpClient.sendAsync(rq, HttpResponse.BodyHandlers.ofLines())
                .thenApply(rs ->
                        rs.body()
                                .filter(line -> !line.isBlank())
                                .map(line -> {
                                    try {
                                        T rsDto = mapper.readValue(line, responseDto);
                                        log.debug("Got partial response from [{}] : {}", uri, mapper.writeValueAsString(rsDto));
                                        return rsDto;
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failed to parse line: " + line, e);
                                    }
                                })
                );
    }

    /**
     * Собрать объект запроса к API Ollama
     *
     * @param uri     путь запроса
     * @param method  метод запроса
     * @param async   признак асинхронности запроса
     * @param payload тело запроса
     * @return сформированный, на основе переданных данных, объект запроса
     */
    @SneakyThrows
    private HttpRequest buildRequest(URI uri, MethodType method, boolean async, ApiRequestDTO... payload) {
        var rqBody = payload.length == 0 ? "" : mapper.writeValueAsString(payload[0]);
        log.debug("Sending {} {} request on [{}] with payload: {}", async ? "async" : "", method, uri, rqBody);
        var rq = HttpRequest.newBuilder(uri);
        switch (method) {
            case GET -> rq.GET();
            case POST -> rq.POST(HttpRequest.BodyPublishers.ofString(rqBody));
            default -> throw new RuntimeException("Unexpected http method - " + method);
        }
        return rq.build();
    }
}
