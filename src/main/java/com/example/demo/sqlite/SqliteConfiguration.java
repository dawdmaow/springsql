package com.example.demo.sqlite;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.sqlite.JDBC;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SqliteProperties.class)
public class SqliteConfiguration {

	@Bean
	DataSource sqliteDataSource(SqliteProperties properties) {
		var databasePath = Path.of(properties.getPath()).toAbsolutePath().normalize();
		var parent = databasePath.getParent();

		// SQLite happily creates the file for us, but it will not create missing folders.
		if (parent != null) {
			try {
				Files.createDirectories(parent);
			}
			catch (java.io.IOException ex) {
				throw new UncheckedIOException("Could not create the SQLite data directory.", ex);
			}
		}

		var jdbcUrl = "jdbc:sqlite:" + databasePath + "?busy_timeout=5000";
		return new SimpleDriverDataSource(new JDBC(), jdbcUrl);
	}
}
