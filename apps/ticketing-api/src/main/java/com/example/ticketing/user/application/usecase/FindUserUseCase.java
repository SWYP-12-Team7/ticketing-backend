package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User execute(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
