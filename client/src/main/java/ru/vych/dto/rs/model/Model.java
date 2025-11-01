package ru.vych.dto.rs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.vych.dto.rs.ApiResponseDTO;

import java.util.Map;

/**
 * DTO с информацией о модели
 *
 * @see <a href="https://docs.ollama.com/api/ps">API Reference PS</a>
 * @see <a href="https://docs.ollama.com/api/tags">API Reference TAGS</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Model implements ApiResponseDTO {
    /**
     * Параметры конфигурации модели по умолчанию.
     * <p>
     * Обычно включает:
     * <ul>
     *     <li>"temperature" — параметр “творчества” модели (0.7 означает умеренную вариативность ответов);</li>
     *     <li>"num_ctx" — максимальная длина контекста (в токенах), которую модель может учитывать;</li>
     * </ul>
     */
    private String parameters;

    /**
     * Текст лицензии или условий использования модели, предоставленных разработчиком.
     */
    private String license;

    /**
     * Список возможностей, поддерживаемых моделью.
     * <p>
     * Возможные значения:
     * <ul>
     *     <li>"completion" — модель может генерировать текст по подсказке;</li>
     *     <li>"chat" — поддерживает диалоговый формат;</li>
     *     <li>"vision" — может обрабатывать изображения (мультимодальная модель);</li>
     *     <li>"embedding" — поддерживает создание векторных представлений текста;</li>
     * </ul>
     */
    private String[] capabilities;

    /**
     * Имя модели
     */
    private String name;

    /**
     * Имя модели
     */
    private String model;

    /**
     * Дата последней модификации модели в формате ISO 8601
     */
    @JsonProperty("modified_at")
    private String modifiedAt;

    /**
     * Общий размер модели занимаемый на диске в байтах.
     * Включает все веса, квантованные параметры и метаданные.
     */
    private Long size;

    /**
     * Уникальная хеш-сигнатура (SHA-256) модели.
     * Используется для проверки целостности и идентификации версии модели.
     */
    private String digest;

    /**
     * Дополнительная информация о модели
     */
    private ModelDetails details;

    /**
     * Шаблон, определяющий, как Ollama формирует входную подсказку для модели при запросах через API
     */
    private String template;

    /**
     * Дополнительные мета-данные модели
     */
    @JsonProperty("model_info")
    private Map<String, String> modelInfo;

    /**
     * Время, когда модель будет выгружена из памяти
     */
    @JsonProperty("expires_at")
    private String expiresAt;

    /**
     * Объём видеопамяти, который занимает модель в байтах
     */
    @JsonProperty("size_vram")
    private Long sizeVram;

    /**
     * Длина контекста модели
     */
    @JsonProperty("context_length")
    private Long contextLength;

    /**
     * Скопировать подробные данные из одной модели в другую.
     *
     * @param from объект из которого будет производиться копирование
     * @return текущий объект со скопированными данными из <i>from</i>
     */
    public Model copyDetails(Model from) {
        parameters = from.getParameters() != null ? from.getParameters() : parameters;
        license = from.getLicense() != null ? from.getLicense() : license;
        capabilities = from.getCapabilities() != null ? from.getCapabilities() : capabilities;
        model = from.getModel() != null ? from.getModel() : model;
        modifiedAt = from.getModifiedAt() != null ? from.getModifiedAt() : modifiedAt;
        size = from.getSize() != null ? from.getSize() : size;
        digest = from.getDigest() != null ? from.getDigest() : digest;
        details = from.getDetails() != null ? from.getDetails() : details;
        modelInfo = from.getModelInfo() != null ? from.getModelInfo() : modelInfo;
        return this;
    }
}
