package com.rakutenmobile.userapi.user.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private java.util.UUID userId;
    private String name;
    private String password;

    public static User withoutId(String name, String password) {
        return new User(null, name, password);
    }

}
