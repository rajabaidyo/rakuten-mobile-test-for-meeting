package com.rakutenmobile.userapi.user.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {
    private java.util.UUID userId;
    private String name;
    private String password;
    private java.time.OffsetDateTime createdAt;
}
