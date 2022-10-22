package com.rakutenmobile.messageapi.usermessage.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class UserMessage {
    private java.util.UUID id;
    private String content;
    private String topic;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime deletedAt;
    private String userId;
}
