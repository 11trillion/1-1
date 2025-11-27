package com.oneonone.userservice.domain.repository;

import com.oneonone.userservice.domain.entity.User;

public interface UserRepository {
    User save(User user);
}
