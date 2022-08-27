package com.yang.gulimall.auth.mail;

import com.yang.common.utils.AuthServerConstant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MailUtils {

    @Resource
    JavaMailSender javaMailSender;
    @Autowired
    SimpleMailMessage message;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     *
     * @param from 邮件接收者
     */
    public boolean sendCode(String from)
    {

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_PREFIX+from);
        if(StringUtils.isNotEmpty(redisCode))
        {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis()-time<60000) {
            //60秒内不能再发
                return false;
            }
        }
        String code= UUID.randomUUID().toString().substring(0,5);
        //redis缓存验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_PREFIX+from, code+"_"+System.currentTimeMillis(),10, TimeUnit.MINUTES);
        message.setTo(from);
        message.setText("(谷粒商城)你的验证码为"+code+",30分钟内有效");
        javaMailSender.send(message);
        return true;
    }

    public boolean checkCode(String from ,String code) {
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_PREFIX+from);
        if(StringUtils.isNotEmpty(s)) {
            String s1 = s.split("_")[0];
            if(code.equals(s1))
            {
                redisTemplate.delete(AuthServerConstant.SMS_CODE_PREFIX+from);
                //正确，删除验证码
                return true;
            }
            //验证码错误
        }
        return false;
    }
}
