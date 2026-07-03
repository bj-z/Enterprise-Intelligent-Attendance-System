package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.domain.SysUser;
import com.attendance.repository.SysUserRepository;
import com.attendance.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserRepository sysUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");

        SysUser user = sysUserRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return Result.unauthorized("账号不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.unauthorized("密码错误");
        }

        if ("禁用".equals(user.getStatus())) {
            return Result.forbidden("账号已被禁用，请联系管理员");
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        sysUserRepository.save(user);

        String token = jwtUtil.generateToken(username);

        return Result.success(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        SysUser user = sysUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Result.unauthorized("用户不存在");
        }
        return Result.success(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "phone", user.getPhone() != null ? user.getPhone() : ""
        ));
    }
}
