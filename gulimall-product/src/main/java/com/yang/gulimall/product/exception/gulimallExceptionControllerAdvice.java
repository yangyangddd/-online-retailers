package com.yang.gulimall.product.exception;

import com.yang.common.utils.R;
import com.yang.exception.bizCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@ControllerAdvice
//集中处理所有异常
@Slf4j
//@ResponseBody
@RestControllerAdvice
public class gulimallExceptionControllerAdvice {


    @ExceptionHandler(value = MethodArgumentNotValidException.class )
    public R handleValidException(MethodArgumentNotValidException e)
    {
//        log.error("数据校验出现异常{},异常类型 {}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> errorMap=new HashMap<>();
        bindingResult.getFieldErrors().forEach(((fieldError) -> {
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        }));
        return R.error(bizCodeEnum.VALID_EXCEPTION.getCode(),bizCodeEnum.VALID_EXCEPTION.getMsg()).put("data",errorMap);
    }
//    @ExceptionHandler(value = Exception.class )
//    public R handleException(Throwable throwable)
//    {
//        log.error(throwable.getMessage());
//        return R.error(bizCodeEnum.UNKNOWN_EXCEPTION.getCode(), bizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
//    }
}
