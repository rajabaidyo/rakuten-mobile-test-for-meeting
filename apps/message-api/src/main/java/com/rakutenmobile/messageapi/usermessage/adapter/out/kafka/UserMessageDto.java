package com.rakutenmobile.messageapi.usermessage.adapter.out.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public  class UserMessageDto {
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

    public UserMessageDto(String content, String topic, java.time.OffsetDateTime createdAt, String userId) {
        this.content = content;
        this.topic = topic;
        this.createdAt = createdAt;
        this.userId = userId;
    }
}
