package com.rakutenmobile.messageapi.usermessage.adapter.out.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
@Builder
@Table(name = "messages")
public class MessageEntity {
    @Id
    private java.util.UUID id;
    private String userId;
    private String topic;
    private String content;
    private java.time.OffsetDateTime createdAt;
    private java.time.OffsetDateTime deletedAt;
}
