package com.codebit.api.service.impl;

import com.codebit.api.dto.authDto.AuthResponse;
import com.codebit.api.dto.loginDto.LoginRequest;
import com.codebit.api.dto.loginDto.RegisterRequest;
import com.codebit.api.entity.User;
import com.codebit.api.repository.UserRepository;
import com.codebit.api.service.UserService;
import com.codebit.api.utils.BusinessException;
import com.codebit.api.utils.JwtUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserServiceImpl implements UserService {

    // 使用 final + RequiredArgsConstructor 实现构造器注入 ，优点：依赖不可变、便于单元测试、明确必需的依赖
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MeterRegistry registry;
    // ========== 注册相关指标 ==========
    private final Counter registerTotal;
    private final Counter registerSuccess;
    private final Counter registerFailUsernameExist;
    private final Counter registerFailEmailExist;
    private final Timer registerTimer;

    // ========== 登录相关指标 ==========
    private final Counter loginTotal;
    private final Counter loginSuccess;
    private final Counter loginFailCredentials;
    private final Timer loginTimer;

    // 自定义构造器：利用 Lombok 的 @RequiredArgsConstructor 无法覆盖，
    // 需要显式声明构造器，并调用 super()（如果有父类）
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder,
                           MeterRegistry registry) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.registry = registry;

        // 初始化注册指标
        this.registerTotal = Counter.builder("user_register_total")
                .description("Total number of user registration requests")
                .register(registry);

        this.registerSuccess = Counter.builder("user_register_success_total")
                .description("Total number of successful registrations")
                .register(registry);

        this.registerFailUsernameExist = Counter.builder("user_register_fail_total")
                .description("Registration failures due to username already exists")
                .tag("reason", "username_exists")
                .register(registry);

        this.registerFailEmailExist = Counter.builder("user_register_fail_total")
                .description("Registration failures due to email already exists")
                .tag("reason", "email_exists")
                .register(registry);

        this.registerTimer = Timer.builder("user_register_duration")
                .description("Registration request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        // 初始化登录指标
        this.loginTotal = Counter.builder("user_login_total")
                .description("Total number of user login requests")
                .register(registry);

        this.loginSuccess = Counter.builder("user_login_success_total")
                .description("Total number of successful logins")
                .register(registry);

        this.loginFailCredentials = Counter.builder("user_login_fail_total")
                .description("Login failures due to incorrect credentials")
                .tag("reason", "invalid_credentials")
                .register(registry);

        this.loginTimer = Timer.builder("user_login_duration")
                .description("Login request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }


    /**
     * 用户注册
     *
     * @return
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        registerTotal.increment();
        Timer.Sample sample = Timer.start();

        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                registerFailUsernameExist.increment(); // 用户名已存在失败
                throw new BusinessException(409, "用户名已存在");
            }

            // 2. 如果提供了邮箱，检查邮箱是否已存在
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    registerFailEmailExist.increment();  //  邮箱已被注册失败
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
        } finally {
            // 4. 无论成功失败，记录耗时
            sample.stop(registerTimer);
        }


    }

    /**
     * 用户登录验证
     *
     * @param requestVo
     * @return
     */
    @Override
    public AuthResponse login(LoginRequest requestVo) {
        // 1. 记录登录请求总数 & 开始计时
        loginTotal.increment();
        Timer.Sample sample = Timer.start();

        try {
            User user = userRepository.findByUsername(requestVo.getUsername())
                    .orElseThrow(() -> {
                        loginFailCredentials.increment();  // 用户名不存在失败
                        return new BusinessException(401, "用户名或者密码错误");
                    });

            if (!passwordEncoder.matches(requestVo.getPassword(), user.getPassword())) {
                loginFailCredentials.increment();  //  密码错误失败
                throw new BusinessException(401, "用户名或者密码错误");
            }

            String token = jwtUtil.generateToken(Long.valueOf(user.getId()), user.getUsername());

            log.info("用户登录成功: username={}, id={}", user.getUsername(), user.getId());

            // 2. 登录成功
            loginSuccess.increment();
            return new AuthResponse(
                    token,
                    user.getUsername(),
                    Long.valueOf(user.getId()),
                    jwtUtil.getExpirationRemaining(token)
            );

        } finally {
            // 3. 无论成功失败，记录耗时
            sample.stop(loginTimer);
        }
    }
}
