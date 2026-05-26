package com.example.demo.sqlite.model;

import java.util.List;

public record SqlExecutionResult(
		boolean tabular,
		List<QueryColumn> columns,
		List<List<Object>> rows,
		boolean truncated,
		Integer affectedRowCount,
		String message) {

	public static SqlExecutionResult table(
			List<QueryColumn> columns,
			List<List<Object>> rows,
			boolean truncated,
			String message) {
		return new SqlExecutionResult(true, columns, rows, truncated, null, message);
	}

	public static SqlExecutionResult update(Integer affectedRowCount, String message) {
		return new SqlExecutionResult(
				false,
				List.of(),
				List.of(),
				false,
				affectedRowCount,
				message);
	}

	public boolean hasRows() {
		return !rows.isEmpty();
	}

	public int displayedRowCount() {
		return rows.size();
	}

	public record QueryColumn(String name, String declaredType) {
	}
}
