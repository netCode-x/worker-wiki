package com.codebit.api.service;

import com.codebit.api.dto.ForgetPasswordRequest;
import com.codebit.api.dto.ResetPasswordRequest;

public interface PasswordResetService {


    void sendResetCode(ForgetPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

}
