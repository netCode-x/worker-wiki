package com.codebit.api.service;

import com.codebit.api.dto.likeDto.LikeResponse;
import com.codebit.api.dto.footerDto.ForgetPasswordRequest;

public interface PasswordResetService {


    void sendResetCode(ForgetPasswordRequest request);

    void resetPassword(LikeResponse.ResetPasswordRequest request);

}
