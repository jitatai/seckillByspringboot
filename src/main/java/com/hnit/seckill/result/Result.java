package com.hnit.seckill.result;

public class Result<T> {
    private int code;
    private String msg;
    private T data;




    public static <T> Result<T>  error(CodeMsg codeMsg) {
        return  new Result<T>(codeMsg);
    }
    public static <T> Result<T>  success(T data){
        return new Result<T>(data);
    }
    public int getCode() {
        return code;
    }

    private Result(T data){
        this.data = data;
        this.code = 0;
        this.msg = "success";
    }
    private Result(CodeMsg codeMsg) {
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMessage();
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
