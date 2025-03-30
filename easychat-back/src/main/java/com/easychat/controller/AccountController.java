package com.easychat.controller;

import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.redis.RedisUtils;
import com.easychat.exception.BusinessException;
import com.easychat.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/account")
public class AccountController  extends ABaseController{

    private static final Logger logger = (Logger) LoggerFactory.getLogger(AccountController.class);
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String code =captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_TIME_1MIN*10);
//        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_TIME_1MIN*10);

//        logger.info("验证码是{}",code);
        String checkCodeBase64 =captcha.toBase64();
        Map<String,String> result=new HashMap<String,String>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey",checkCodeKey);

        return  getSuccessResponseVO(result);
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode){
        try {
            //!checkCode.equalsIgnoreCase((String) redisUtils.get( Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))
            if(!checkCode.equalsIgnoreCase((String) redisUtils.get( Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))){
//                logger.info("正确的验证码是{}", redisUtils.get( Constants.REDIS_KEY_CHECK_CODE+checkCodeKey));
//                logger.info("收到的验证码是{}",checkCode);
                throw new BusinessException("图片验证码错误");
            }
//            logger.info("到这里了");
            userInfoService.register(email,nickName,password);
            return getSuccessResponseVO(null);
        }
        finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase((String) redisUtils.get( Constants.REDIS_KEY_CHECK_CODE+checkCodeKey))){
                throw new BusinessException("图片验证码错误");
            }
            UserInfoVO userInfoVO=userInfoService.login(email, password);
            return getSuccessResponseVO(userInfoVO);
        }
        finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }
    }
}
