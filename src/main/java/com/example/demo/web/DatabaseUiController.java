package com.example.demo.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.sqlite.SqliteOperationException;
import com.example.demo.sqlite.SqliteMetadataService;
import com.example.demo.sqlite.SqliteQueryService;
import com.example.demo.sqlite.model.DatabaseObjectSummary;

@Controller
@RequestMapping("/db")
public class DatabaseUiController {

	private final SqliteMetadataService metadataService;
	private final SqliteQueryService queryService;

	public DatabaseUiController(SqliteMetadataService metadataService, SqliteQueryService queryService) {
		this.metadataService = metadataService;
		this.queryService = queryService;
	}

	@GetMapping
	public String index(Model model) {
		populateCommonAttrs(model, null);

		return "db/index";
	}

	@GetMapping("/schema")
	public String schema(@RequestParam("name") String objectName, Model model) {
		populateCommonAttrs(model, objectName);

		return "db/fragments/schema-response";
	}

	@PostMapping("/query")
	public String query(
			@RequestParam("sql") String sql,
			@RequestParam(value = "selectedObject", required = false) String selectedObject,
			Model model) {
		return runQuery(sql, selectedObject, model);
	}

	private String runQuery(String sql, String selectedObject, Model model) {
		model.addAttribute("submittedSql", sql);

		try {
			model.addAttribute("queryResult", queryService.execute(sql));
			model.addAttribute("queryError", null);
		} catch (SqliteOperationException ex) {
			model.addAttribute("queryResult", null);
			model.addAttribute("queryError", ex.getMessage());
		}

		// Update modified tables in UI
		populateCommonAttrs(model, selectedObject);

		return "db/fragments/query-response";
	}

	private void populateCommonAttrs(Model model, String requestedObjectName) {
		var databaseObjects = metadataService.listObjects();
		var selectedObject = resolveSelectedObject(databaseObjects, requestedObjectName);

		model.addAttribute("databaseObjects", databaseObjects);
		model.addAttribute("tableObjects", databaseObjects.stream()
				.filter(DatabaseObjectSummary::isTable)
				.toList());
		model.addAttribute("viewObjects", databaseObjects.stream()
				.filter(databaseObject -> !databaseObject.isTable())
				.toList());
		model.addAttribute("selectedObject", selectedObject);

		if (selectedObject == null) {
			model.addAttribute("schema", null);
			model.addAttribute("schemaError", null);
			return;
		}

		// Re-read these on every request so create/dropped tables are updated.
		try {
			model.addAttribute("schema", metadataService.describeObject(selectedObject));
			model.addAttribute("schemaError", null);
		} catch (SqliteOperationException ex) {
			model.addAttribute("schema", null);
			model.addAttribute("schemaError", ex.getMessage());
		}
	}

	private String resolveSelectedObject(List<DatabaseObjectSummary> databaseObjects, String requestedObjectName) {
		if (requestedObjectName != null) {
			for (var databaseObject : databaseObjects) {
				if (databaseObject.name().equals(requestedObjectName)) {
					return requestedObjectName;
				}
			}
		}

		return databaseObjects.isEmpty() ? null : databaseObjects.get(0).name();
	}
}
