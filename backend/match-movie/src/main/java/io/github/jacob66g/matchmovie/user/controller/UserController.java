package io.github.jacob66g.matchmovie.user.controller;

import io.github.jacob66g.matchmovie.security.annotations.AuthenticatedUser;
import io.github.jacob66g.matchmovie.user.dto.UserResponse;
import io.github.jacob66g.matchmovie.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController()
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserInfo(@AuthenticatedUser UUID userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }


}
