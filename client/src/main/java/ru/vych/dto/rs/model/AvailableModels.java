package ru.vych.dto.rs.model;

import lombok.Getter;
import lombok.Setter;
import ru.vych.dto.rs.ApiResponseDTO;

import java.util.List;

/**
 * DTO для ответа API Ollama на запрос списка доступных моделей.
 * @see <a href="https://docs.ollama.com/api/tags">API Reference</a>
 */
@Getter
@Setter
public class AvailableModels implements ApiResponseDTO {
    /**
     * Список доступных на сервере моделей
     */
    List<Model> models;
}
