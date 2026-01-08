package com.example.services.user.application.usecase;

import com.example.services.user.domain.User;
import com.example.services.user.domain.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> execute(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> execute(String email) {
        return userRepository.findByEmail(email);
    }
}
