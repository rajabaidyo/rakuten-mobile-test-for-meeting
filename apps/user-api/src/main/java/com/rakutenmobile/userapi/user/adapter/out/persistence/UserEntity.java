package com.rakutenmobile.userapi.user.adapter.out.persistence;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Value
@Builder
@Table(name = "users")
public class UserEntity {
    @Id
    private java.util.UUID userId;
    private String name;
    private String password;
    private java.time.OffsetDateTime createdAt;
}
