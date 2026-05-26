(function () {
	const textarea = document.getElementById("sql-input");
	const host = document.getElementById("sql-editor-monaco");

	if (!textarea || !host || !window.require) {
		return;
	}

	const monacoBaseUrl = "https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min";
	const form = textarea.form;

	// Keep the textarea as the source of truth (despite using Monaco for the UI)
	window.MonacoEnvironment = {
		getWorkerUrl: function () {
			return "data:text/javascript;charset=utf-8," + encodeURIComponent(
				"self.MonacoEnvironment = { baseUrl: '" + monacoBaseUrl + "' };\n" +
				"importScripts('" + monacoBaseUrl + "/vs/base/worker/workerMain.js');"
			);
		}
	};

	window.require.config({
		paths: {
			vs: monacoBaseUrl + "/vs"
		}
	});

	window.require(["vs/editor/editor.main"], function () {
		// Only swap the textarea out once Monaco is actually ready.
		host.hidden = false;

		const editor = monaco.editor.create(host, {
			value: textarea.value,
			language: "sql",
			theme: "vs-dark",
			automaticLayout: true,
			minimap: {
				enabled: false
			},
			scrollBeyondLastLine: false,
			wordWrap: "on",
			fontSize: 14,
			lineNumbersMinChars: 3,
			padding: {
				top: 12,
				bottom: 12
			},
			ariaLabel: "SQL editor"
		});

		textarea.hidden = true;

		// Mirror Monaco changes back into the real form field before form submits
		editor.onDidChangeModelContent(function () {
			textarea.value = editor.getValue();
		});

		if (form) {
			form.addEventListener("submit", function () {
				textarea.value = editor.getValue();
			});
		}
	});
})();
