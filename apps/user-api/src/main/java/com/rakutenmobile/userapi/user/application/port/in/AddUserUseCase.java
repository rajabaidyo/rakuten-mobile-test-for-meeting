package com.rakutenmobile.userapi.user.application.port.in;

import com.rakutenmobile.userapi.user.domain.User;

public interface AddUserUseCase {
    User AddSingleUser(String name, String password);
}
