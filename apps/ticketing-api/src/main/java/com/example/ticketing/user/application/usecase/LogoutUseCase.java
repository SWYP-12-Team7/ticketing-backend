package com.example.ticketing.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    public void execute(Long userId) {
        // JWT는 stateless하므로 서버에서 할 작업이 특별히 없음
        // 클라이언트에서 토큰 삭제로 충분
    }
}
