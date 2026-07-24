package com.mtxrii.wikiforge.alpinebackend.user;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.auth.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    List<UserResponse> list() {
        return userService.list();
    }

    @GetMapping("/{userId}")
    UserResponse get(@PathVariable UUID userId) {
        return userService.get(userId);
    }
}
