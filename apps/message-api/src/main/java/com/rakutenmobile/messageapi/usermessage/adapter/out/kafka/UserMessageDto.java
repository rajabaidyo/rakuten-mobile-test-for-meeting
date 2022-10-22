package com.rakutenmobile.messageapi.usermessage.adapter.out.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public  class UserMessageDto {
    @JsonProperty
    public java.util.UUID id;
    @JsonProperty
    public String content;
    @JsonProperty
    public String topic;
    @JsonProperty
    public java.time.OffsetDateTime createdAt;
    @JsonProperty
    public String userId;

    public UserMessageDto() {

    }

    public UserMessageDto(java.util.UUID id, String content, String topic, java.time.OffsetDateTime createdAt, String userId) {
        this.id = id;
        this.content = content;
        this.topic = topic;
        this.createdAt = createdAt;
        this.userId = userId;
    }
}
