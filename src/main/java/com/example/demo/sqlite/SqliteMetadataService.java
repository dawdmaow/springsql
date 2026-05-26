package com.example.demo.sqlite;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.sqlite.model.DatabaseObjectSummary;
import com.example.demo.sqlite.model.TableSchema;
import com.example.demo.sqlite.model.TableSchema.ColumnDefinition;
import com.example.demo.sqlite.model.TableSchema.ForeignKeyDefinition;
import com.example.demo.sqlite.model.TableSchema.IndexDefinition;

@Service
public class SqliteMetadataService {

	private final JdbcTemplate jdbcTemplate;

	public SqliteMetadataService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<DatabaseObjectSummary> listObjects() {
		return jdbcTemplate.query(
				"""
						SELECT name, type
						FROM sqlite_schema
						WHERE type IN ('table', 'view')
						  AND name NOT LIKE 'sqlite_%'
						ORDER BY CASE type WHEN 'table' THEN 0 ELSE 1 END, name
						""",
				(rs, rowNum) -> new DatabaseObjectSummary(rs.getString("name"), rs.getString("type")));
	}

	public TableSchema describeObject(String objectName) {
		var databaseObject = jdbcTemplate.query(
				"""
						SELECT name, type
						FROM sqlite_schema
						WHERE name = ?
						  AND type IN ('table', 'view')
						  AND name NOT LIKE 'sqlite_%'
						LIMIT 1
						""",
				rs -> rs.next() ? new DatabaseObjectSummary(rs.getString("name"), rs.getString("type")) : null,
				objectName);

		if (databaseObject == null) {
			throw new SqliteOperationException("Could not find a table or view named '" + objectName + "'.");
		}

		var columns = readColumns(databaseObject.name());
		var foreignKeys = databaseObject.isTable() ? readForeignKeys(databaseObject.name())
				: List.<ForeignKeyDefinition>of();
		var indexes = databaseObject.isTable() ? readIndexes(databaseObject.name()) : List.<IndexDefinition>of();

		return new TableSchema(databaseObject.name(), databaseObject.type(), columns, foreignKeys, indexes);
	}

	private List<ColumnDefinition> readColumns(String objectName) {
		return jdbcTemplate.query(
				"SELECT * FROM pragma_table_info(?)",
				(rs, rowNum) -> new ColumnDefinition(
						rs.getInt("cid"),
						rs.getString("name"),
						rs.getString("type"),
						rs.getBoolean("notnull"),
						rs.getString("dflt_value"),
						rs.getInt("pk")),
				objectName);
	}

	private List<ForeignKeyDefinition> readForeignKeys(String objectName) {
		return jdbcTemplate.query(
				"SELECT * FROM pragma_foreign_key_list(?)",
				(rs, rowNum) -> new ForeignKeyDefinition(
						rs.getInt("id"),
						rs.getInt("seq"),
						rs.getString("from"),
						rs.getString("table"),
						rs.getString("to"),
						rs.getString("on_update"),
						rs.getString("on_delete"),
						rs.getString("match")),
				objectName);
	}

	private List<IndexDefinition> readIndexes(String objectName) {
		return jdbcTemplate.query(
				"SELECT * FROM pragma_index_list(?)",
				(rs, rowNum) -> {
					var indexName = rs.getString("name");
					var columns = jdbcTemplate.query(
							"SELECT * FROM pragma_index_info(?)",
							(indexRs, ignored) -> indexRs.getString("name"),
							indexName);

					return new IndexDefinition(
							indexName,
							rs.getBoolean("unique"),
							rs.getString("origin"),
							rs.getBoolean("partial"),
							columns);
				},
				objectName);
	}
}
