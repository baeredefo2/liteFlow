package com.yomahub.liteflow.exception;

public class IfTargetCannotBePreOrFinallyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常信息
     */
    private String message;

    public IfTargetCannotBePreOrFinallyException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
