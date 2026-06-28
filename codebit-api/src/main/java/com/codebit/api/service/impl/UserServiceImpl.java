package com.codebit.api.service.impl;

import com.codebit.api.dto.AuthResponse;
import com.codebit.api.dto.LoginRequest;
import com.codebit.api.dto.RegisterRequest;
import com.codebit.api.entity.User;
import com.codebit.api.repository.UserRepository;
import com.codebit.api.service.UserService;
import com.codebit.api.utils.JwtUtil;
import com.codebit.api.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/5 星期日
 * @Description:
 * @VERSON: 17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {

    // 使用 final + RequiredArgsConstructor 实现构造器注入 ，优点：依赖不可变、便于单元测试、明确必需的依赖
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     *
     * @return
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }

        // 2. 如果提供了邮箱，检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException(409, "邮箱已被注册");
            }
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());
        User saveUser = userRepository.save(new User(
                request.getUsername(),
                encodePassword,
                request.getEmail())
        );

        //  生成 JWT Token（注册成功后直接登录，无需再次输入密码）
        String token = jwtUtil.generateToken(Long.valueOf(saveUser.getId()), saveUser.getUsername());

        log.info("用户注册成功: username={}, id={}", saveUser.getUsername(), saveUser.getId());
        // 返回认证响应，包含 Token，前端可以自动完成登录
        return new AuthResponse(
                token,
                saveUser.getUsername(),
                Long.valueOf(saveUser.getId()),
                jwtUtil.getExpirationRemaining(token)
        );
    }

    /**
     * 用户登录验证
     * @param requestVo
     * @return
     */
    @Override
    public AuthResponse login(LoginRequest requestVo) {
        // 根据用户名查找用户，Optional 避免空指针
        User user = userRepository.findByUsername(requestVo.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或者密码错误"));

        if (!passwordEncoder.matches(requestVo.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或者密码错误");
        }
        // 3. 认证成功，生成 Token
        String token = jwtUtil.generateToken(Long.valueOf(user.getId()), user.getUsername());

        log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());

        return new AuthResponse(
                token,
                user.getUsername(),
                Long.valueOf(user.getId()),
                jwtUtil.getExpirationRemaining(token)
        );
    }
}
