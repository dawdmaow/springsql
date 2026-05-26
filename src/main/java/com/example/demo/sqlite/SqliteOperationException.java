package com.example.demo.sqlite;

public class SqliteOperationException extends RuntimeException {

	public SqliteOperationException(String message) {
		super(message);
	}

	public SqliteOperationException(String message, Throwable cause) {
		super(message, cause);
	}
}
