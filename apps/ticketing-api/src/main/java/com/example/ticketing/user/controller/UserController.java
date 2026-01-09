package com.example.ticketing.user.controller;

import com.example.ticketing.user.controller.dto.UserResponse;
import com.example.ticketing.user.application.usecase.FindUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final FindUserUseCase findUserUseCase;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(findUserUseCase.execute(id));
    }
}
