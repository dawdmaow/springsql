package com.example.demo.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import com.example.demo.sqlite.model.SqlExecutionResult;
import com.example.demo.sqlite.model.SqlExecutionResult.QueryColumn;

@Service
public class SqliteQueryService {

	private static final DateTimeFormatter EXECUTION_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final DataSource dataSource;
	private final SqliteProperties properties;

	public SqliteQueryService(DataSource dataSource, SqliteProperties properties) {
		this.dataSource = dataSource;
		this.properties = properties;
	}

	public SqlExecutionResult execute(String sql) {
		if (sql == null || sql.isBlank()) {
			throw new SqliteOperationException("Enter an SQL statement first.");
		}

		sql = requireSingleStatement(sql);

		try (var connection = dataSource.getConnection()) {
			return executeStatement(connection, sql);
		} catch (SQLException ex) {
			throw new SqliteOperationException(ex.getMessage(), ex);
		}
	}

	private SqlExecutionResult executeStatement(Connection connection, String sql) throws SQLException {
		try (var statement = connection.createStatement()) {
			boolean hasResultSet = statement.execute(sql);

			if (hasResultSet) {
				try (var resultSet = statement.getResultSet()) {
					return readTabularResult(resultSet);
				}
			}

			var updateCount = statement.getUpdateCount();
			var message = withTimestamp("Statement executed successfully.");

			return SqlExecutionResult.update(updateCount >= 0 ? updateCount : null, message);
		}
	}

	private SqlExecutionResult readTabularResult(ResultSet resultSet) throws SQLException {
		var metadata = resultSet.getMetaData();
		var columnCount = metadata.getColumnCount();
		var columns = new ArrayList<QueryColumn>(columnCount);

		for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
			columns.add(new QueryColumn(
					metadata.getColumnLabel(columnIndex),
					metadata.getColumnTypeName(columnIndex)));
		}

		var rows = new ArrayList<List<Object>>();
		boolean truncated = false;
		int maxRows = safeMaxRows();

		while (resultSet.next()) {
			if (rows.size() == maxRows) {
				truncated = true;
				break;
			}

			var row = new ArrayList<Object>(columnCount);
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				row.add(normalizeCellValue(resultSet.getObject(columnIndex)));
			}
			rows.add(row);
		}

		var message = truncated
				? withTimestamp("Showing the first " + rows.size() + " rows.")
				: withTimestamp("Returned " + rows.size() + " row" + (rows.size() == 1 ? "" : "s") + ".");

		return SqlExecutionResult.table(columns, rows, truncated, message);
	}

	private String withTimestamp(String message) {
		var executionTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(EXECUTION_TIME_FORMAT);
		if (message.endsWith(".")) {
			return message.substring(0, message.length() - 1) + " at " + executionTime + ".";
		}

		return message + " at " + executionTime + ".";
	}

	private int safeMaxRows() {
		return Math.max(1, properties.getMaxResultRows());
	}

	private String requireSingleStatement(String sql) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;
		boolean statementTerminated = false;

		for (int i = 0; i < sql.length(); i++) {
			char currentChar = sql.charAt(i);
			char nextChar = i + 1 < sql.length() ? sql.charAt(i + 1) : '\0';

			if (inLineComment) {
				if (currentChar == '\n' || currentChar == '\r') {
					inLineComment = false;
				}
				continue;
			}

			if (inBlockComment) {
				if (currentChar == '*' && nextChar == '/') {
					inBlockComment = false;
					i++;
				}
				continue;
			}

			if (inSingleQuote) {
				if (currentChar == '\'') {
					if (nextChar == '\'') {
						i++;
					} else {
						inSingleQuote = false;
					}
				}
				continue;
			}

			if (inDoubleQuote) {
				if (currentChar == '"') {
					if (nextChar == '"') {
						i++;
					} else {
						inDoubleQuote = false;
					}
				}
				continue;
			}

			if (currentChar == '-' && nextChar == '-') {
				inLineComment = true;
				i++;
				continue;
			}

			if (currentChar == '/' && nextChar == '*') {
				inBlockComment = true;
				i++;
				continue;
			}

			if (currentChar == '\'') {
				if (statementTerminated) {
					throw new SqliteOperationException("Only one SQL statement is allowed at a time.");
				}
				inSingleQuote = true;
				continue;
			}

			if (currentChar == '"') {
				if (statementTerminated) {
					throw new SqliteOperationException("Only one SQL statement is allowed at a time.");
				}
				inDoubleQuote = true;
				continue;
			}

			if (currentChar == ';') {
				statementTerminated = true;
				continue;
			}

			if (statementTerminated && !Character.isWhitespace(currentChar)) {
				throw new SqliteOperationException("Only one SQL statement is allowed at a time.");
			}
		}

		return sql;
	}

	private Object normalizeCellValue(Object value) {
		if (value instanceof byte[] bytes) {
			// We don't show blobs in the UI, just their size.
			return "<" + bytes.length + " bytes>";
		}

		return value;
	}
}
