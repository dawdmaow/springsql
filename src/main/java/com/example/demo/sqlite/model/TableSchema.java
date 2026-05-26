package com.example.demo.sqlite.model;

import java.util.List;

public record TableSchema(
		String objectName,
		String objectType,
		List<ColumnDefinition> columns,
		List<ForeignKeyDefinition> foreignKeys,
		List<IndexDefinition> indexes) {

	public String foreignKeyReference(String columnName) {
		for (var foreignKey : foreignKeys) {
			if (foreignKey.fromColumn().equals(columnName)) {
				if (foreignKey.targetColumn() == null || foreignKey.targetColumn().isBlank()) {
					return foreignKey.targetTable();
				}

				return foreignKey.targetTable() + "." + foreignKey.targetColumn();
			}
		}

		return "";
	}

	public boolean isForeignKeyColumn(String columnName) {
		for (var foreignKey : foreignKeys) {
			if (foreignKey.fromColumn().equals(columnName)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasIndexes() {
		return !indexes.isEmpty();
	}

	public record ColumnDefinition(
			int ordinal,
			String name,
			String declaredType,
			boolean notNull,
			String defaultValue,
			int primaryKeyPosition) {

		public boolean isPrimaryKey() {
			return primaryKeyPosition > 0;
		}

		public boolean isNullable() {
			return !notNull;
		}
	}

	public record ForeignKeyDefinition(
			int id,
			int sequence,
			String fromColumn,
			String targetTable,
			String targetColumn,
			String onUpdate,
			String onDelete,
			String matchRule) {
	}

	public record IndexDefinition(
			String name,
			boolean unique,
			String origin,
			boolean partial,
			List<String> columns) {

		public String originDisplayName() {
			var trimmedOrigin = trimmedOrigin();
			if (trimmedOrigin.equalsIgnoreCase("c")) {
				return "CREATE";
			}

			if (trimmedOrigin.equalsIgnoreCase("u")) {
				return "UNIQUE";
			}

			if (trimmedOrigin.equalsIgnoreCase("pk")) {
				return "PRIMARY";
			}

			return trimmedOrigin.isBlank() ? "(unknown)" : origin;
		}

		private String trimmedOrigin() {
			return origin == null ? "" : origin.trim();
		}
	}
}
