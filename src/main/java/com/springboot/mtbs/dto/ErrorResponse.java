package com.springboot.mtbs.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(String code,
                            String message,
                            Map<String, Object> details,
                            String traceId,
                            LocalDateTime timestamp) {
}
