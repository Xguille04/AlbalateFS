package com.albalatefs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta paginada genérica para endpoints de listado.
 * Facilita navegación de resultados grandes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private int pageNumber;       // Página actual (0-indexed)
    private int pageSize;         // Elementos por página
    private int totalPages;       // Total de páginas
    private long totalElements;   // Total de elementos
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    public static <T> PaginatedResponse<T> of(List<T> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return new PaginatedResponse<>(
            content,
            page,
            size,
            totalPages,
            total,
            page < totalPages - 1,
            page > 0
        );
    }
}
