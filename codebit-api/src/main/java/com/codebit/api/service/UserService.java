package com.codebit.api.service;

import com.codebit.api.dto.AuthResponse;
import com.codebit.api.dto.LoginRequest;
import com.codebit.api.dto.RegisterRequest;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/2 星期四
 * @Description:
 * @VERSON: 17
 */

public interface UserService {


    AuthResponse register(RegisterRequest request);


    AuthResponse login(LoginRequest request);
}
