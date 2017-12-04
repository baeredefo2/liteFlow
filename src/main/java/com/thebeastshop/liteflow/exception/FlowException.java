package com.thebeastshop.liteflow.exception;

public class FlowException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	/** 异常信息 */
	private String message;

	public FlowException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
