package com.example.app.link;

import java.time.OffsetDateTime;

public record LinkResponse(
        Long id,
        String code,
        String originalUrl,
        OffsetDateTime createdAt
) {

    public static LinkResponse from(Link link) {
        return new LinkResponse(
                link.getId(),
                link.getCode(),
                link.getOriginalUrl(),
                link.getCreatedAt()
        );
    }
}