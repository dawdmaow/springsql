package com.example.demo.sqlite;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
class SqliteSeedDataInitializer implements ApplicationRunner {

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	private final SqliteProperties properties;

	SqliteSeedDataInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate, SqliteProperties properties) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.isSeedIfEmpty()) {
			return;
		}

		var userObjectCount = jdbcTemplate.queryForObject(
				"""
						SELECT COUNT(*)
						FROM sqlite_schema
						WHERE type IN ('table', 'view')
						  AND name NOT LIKE 'sqlite_%'
						""",
				Integer.class);

		if (userObjectCount != null && userObjectCount > 0) {
			return;
		}

		var populator = new ResourceDatabasePopulator(new ClassPathResource("sqlite/demo-data.sql"));
		populator.execute(dataSource);
	}
}
