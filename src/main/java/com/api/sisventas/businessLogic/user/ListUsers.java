package com.api.sisventas.businessLogic.user;

import com.api.sisventas.common.PaginatedResponse;
import com.api.sisventas.common.Pagination;
import com.api.sisventas.dataSources.UserRepository;
import com.api.sisventas.models.dtos.user.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsers {

    private final UserRepository userRepository;

    public ListUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<UserResponse> execute(int page, int limit) {
        return PaginatedResponse.from(
                userRepository.findAll(Pagination.of(page, limit, "createdAt")),
                UserResponse::from);
    }
}
