package com.example.demo.sqlite;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.sqlite")
public class SqliteProperties {
	private String path = "./data/demo.sqlite";
	private int maxResultRows = 200;
	private boolean seedIfEmpty = true;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getMaxResultRows() {
		return maxResultRows;
	}

	public void setMaxResultRows(int maxResultRows) {
		this.maxResultRows = maxResultRows;
	}

	public boolean isSeedIfEmpty() {
		return seedIfEmpty;
	}

	public void setSeedIfEmpty(boolean seedIfEmpty) {
		this.seedIfEmpty = seedIfEmpty;
	}
}
