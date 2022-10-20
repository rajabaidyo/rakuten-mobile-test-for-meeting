package com.rakutenmobile.userapi.user.adapter.out.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<UserEntity, UUID> {
}
