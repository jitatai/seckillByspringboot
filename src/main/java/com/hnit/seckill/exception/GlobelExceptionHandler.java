package com.hnit.seckill.exception;

import com.hnit.seckill.result.CodeMsg;
import com.hnit.seckill.result.Result;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;

import java.util.List;

@RestControllerAdvice
public class GlobelExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        e.printStackTrace();
        if(e instanceof BindException){
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(error.getDefaultMessage()));
        }else if (e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
          return Result.error(ex.getCm());
        } else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
