package com.example.demo.sqlite.model;

public record DatabaseObjectSummary(String name, String type) {

	public boolean isTable() {
		return "table".equalsIgnoreCase(type);
	}
}
