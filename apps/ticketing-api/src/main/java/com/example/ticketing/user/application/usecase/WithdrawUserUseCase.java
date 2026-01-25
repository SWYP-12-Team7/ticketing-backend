package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.user.domain.User;
import com.example.ticketing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 회원탈퇴 UseCase
 * Soft Delete 방식으로 처리
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawUserUseCase {

    private final UserRepository userRepository;

    public void execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        user.withdraw();
    }
}
