
package com.tclab.android.feature.culture.screens

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class CultureBatch(
    val id: String,
    val code: String,
    val plant: String,
    val explant: String,
    val startDate: String,
    val status: String
)

private data class CultureNote(
    val id: String,
    val batchId: String,
    val title: String,
    val body: String,
    val updated: String
)

private data class CultureTable(
    val id: String,
    val batchId: String,
    val title: String,
    val columns: List<String>
)

private data class CultureTableRow(
    val id: String,
    val tableId: String,
    val cells: List<String>
)

private fun timestamp(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

private fun newId(): String = UUID.randomUUID().toString()

private class CultureDatabase(context: Context) :
    SQLiteOpenHelper(context, "tc_lab_culture.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE batches (
                id TEXT PRIMARY KEY,
                code TEXT NOT NULL,
                plant TEXT NOT NULL,
                explant TEXT NOT NULL,
                start_date TEXT NOT NULL,
                status TEXT NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE notes (
                id TEXT PRIMARY KEY,
                batch_id TEXT NOT NULL DEFAULT '',
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                updated TEXT NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE custom_tables (
                id TEXT PRIMARY KEY,
                batch_id TEXT NOT NULL DEFAULT '',
                title TEXT NOT NULL,
                columns_json TEXT NOT NULL
            )"""
        )

        db.execSQL(
            """CREATE TABLE custom_table_rows (
                id TEXT PRIMARY KEY,
                table_id TEXT NOT NULL,
                cells_json TEXT NOT NULL
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future database migrations will preserve existing records.
    }

    private fun values(vararg pairs: Pair<String, String>): ContentValues {
        return ContentValues().apply {
            pairs.forEach { (key, value) -> put(key, value) }
        }
    }

    fun batches(): List<CultureBatch> {
        val result = mutableListOf<CultureBatch>()
        readableDatabase.rawQuery(
            "SELECT id,code,plant,explant,start_date,status FROM batches ORDER BY code",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result.add(
                    CultureBatch(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4), c.getString(5)
                    )
                )
            }
        }
        return result
    }

    fun saveBatch(batch: CultureBatch) {
        writableDatabase.insertWithOnConflict(
            "batches",
            null,
            values(
                "id" to batch.id,
                "code" to batch.code,
                "plant" to batch.plant,
                "explant" to batch.explant,
                "start_date" to batch.startDate,
                "status" to batch.status
            ),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun notes(): List<CultureNote> {
        val result = mutableListOf<CultureNote>()
        readableDatabase.rawQuery(
            "SELECT id,batch_id,title,body,updated FROM notes ORDER BY updated DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result.add(
                    CultureNote(
                        c.getString(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getString(4)
                    )
                )
            }
        }
        return result
    }

    fun saveNote(note: CultureNote) {
        writableDatabase.insertWithOnConflict(
            "notes",
            null,
            values(
                "id" to note.id,
                "batch_id" to note.batchId,
                "title" to note.title,
                "body" to note.body,
                "updated" to note.updated
            ),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun deleteNote(id: String) {
        writableDatabase.delete("notes", "id=?", arrayOf(id))
    }

    fun tables(): List<CultureTable> {
        val result = mutableListOf<CultureTable>()
        readableDatabase.rawQuery(
            "SELECT id,batch_id,title,columns_json FROM custom_tables ORDER BY title",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val columns = mutableListOf<String>()
                val json = JSONArray(c.getString(3))
                for (i in 0 until json.length()) columns.add(json.getString(i))
                result.add(
                    CultureTable(c.getString(0), c.getString(1), c.getString(2), columns)
                )
            }
        }
        return result
    }

    fun saveTable(table: CultureTable) {
        val columns = JSONArray()
        table.columns.forEach { columns.put(it) }

        writableDatabase.insertWithOnConflict(
            "custom_tables",
            null,
            values(
                "id" to table.id,
                "batch_id" to table.batchId,
                "title" to table.title,
                "columns_json" to columns.toString()
            ),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun rows(tableId: String): List<CultureTableRow> {
        val result = mutableListOf<CultureTableRow>()
        readableDatabase.rawQuery(
            "SELECT id,table_id,cells_json FROM custom_table_rows WHERE table_id=? ORDER BY rowid",
            arrayOf(tableId)
        ).use { c ->
            while (c.moveToNext()) {
                val cells = mutableListOf<String>()
                val json = JSONArray(c.getString(2))
                for (i in 0 until json.length()) cells.add(json.getString(i))
                result.add(CultureTableRow(c.getString(0), c.getString(1), cells))
            }
        }
        return result
    }

    fun saveRow(row: CultureTableRow) {
        val cells = JSONArray()
        row.cells.forEach { cells.put(it) }

        writableDatabase.insertWithOnConflict(
            "custom_table_rows",
            null,
            values(
                "id" to row.id,
                "table_id" to row.tableId,
                "cells_json" to cells.toString()
            ),
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}

@Composable
fun CultureWorkspaceScreen() {
    val context = LocalContext.current
    val db = remember(context) { CultureDatabase(context.applicationContext) }

    val batches = remember { mutableStateListOf<CultureBatch>() }
    val notes = remember { mutableStateListOf<CultureNote>() }
    val tables = remember { mutableStateListOf<CultureTable>() }
    var refresh by remember { mutableStateOf(0) }

    var section by remember { mutableStateOf("Batches") }
    var selectedBatch by remember { mutableStateOf("") }
    var selectedTable by remember { mutableStateOf("") }

    var showBatchDialog by remember { mutableStateOf(false) }
    var showNoteEditor by remember { mutableStateOf(false) }
    var showTableDialog by remember { mutableStateOf(false) }
    var showRowEditor by remember { mutableStateOf(false) }
var editingRowId by remember { mutableStateOf("") }
val editingCells = remember { mutableStateListOf<String>() }

    var batchCode by remember { mutableStateOf("") }
    var plantName by remember { mutableStateOf("") }
    var explant by remember { mutableStateOf("") }
    var batchDate by remember { mutableStateOf(timestamp().take(10)) }

    var noteId by remember { mutableStateOf("") }
    var noteTitle by remember { mutableStateOf("") }
    var noteBody by remember { mutableStateOf("") }

    var tableTitle by remember { mutableStateOf("") }
    var tableColumns by remember { mutableStateOf("Date,Observation,Result,Comments") }

    fun reload() {
        batches.clear()
        batches.addAll(db.batches())
        notes.clear()
        notes.addAll(db.notes())
        tables.clear()
        tables.addAll(db.tables())
        refresh++
    }

    androidx.compose.runtime.LaunchedEffect(db) {
        reload()
    }

    // Reading refresh makes changes to the selected table update immediately.
    @Suppress("UNUSED_EXPRESSION")
    val refreshToken = refresh

    val currentTable = tables.firstOrNull { it.id == selectedTable }
    val currentBatch = batches.firstOrNull { it.id == selectedBatch }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Culture workspace", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Offline plant tissue culture records. Save your batches, notebook entries and custom data tables on this device."
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Batches", "Notebook", "Tables").forEach { name ->
                    if (section == name) {
                        Button(onClick = { section = name }) { Text(name) }
                    } else {
                        OutlinedButton(onClick = { section = name }) { Text(name) }
                    }
                }
            }

            HorizontalDivider()

            when (section) {
                "Batches" -> {
                    Text("Culture batch register", style = MaterialTheme.typography.titleLarge)
                    Text("Each batch receives a stable ID. Record the plant, explant and start date.")

                    Button(onClick = {
                        batchCode = ""
                        plantName = ""
                        explant = ""
                        batchDate = timestamp().take(10)
                        showBatchDialog = true
                    }) {
                        Text("＋ Register batch")
                    }

                    if (batches.isEmpty()) {
                        Text("No batches registered yet.")
                    }

                    batches.forEach { batch ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(batch.code, style = MaterialTheme.typography.titleMedium)
                                Text("Batch ID: ${batch.id}")
                                Text("Plant: ${batch.plant}")
                                Text("Explant: ${batch.explant.ifBlank { "Not specified" }}")
                                Text("Started: ${batch.startDate}")
                                Text("Status: ${batch.status}")

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = {
                                        selectedBatch = batch.id
                                        section = "Notebook"
                                    }) { Text("Batch notes") }

                                    OutlinedButton(onClick = {
                                        selectedBatch = batch.id
                                        section = "Tables"
                                    }) { Text("Batch tables") }
                                }
                            }
                        }
                    }
                }

                "Notebook" -> {
                    Text("Lab notebook", style = MaterialTheme.typography.titleLarge)
                    Text("Write in your own words. Notes can be linked to a culture batch.")

                    OutlinedTextField(
                        value = selectedBatch,
                        onValueChange = { selectedBatch = it },
                        label = { Text("Batch ID (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (batches.isNotEmpty()) {
                        Text("Choose a registered batch:")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            batches.forEach { batch ->
                                OutlinedButton(onClick = { selectedBatch = batch.id }) {
                                    Text(batch.code)
                                }
                            }
                        }
                    }

                    Button(onClick = {
                        noteId = ""
                        noteTitle = ""
                        noteBody = ""
                        showNoteEditor = true
                    }) { Text("＋ New notebook entry") }

                    val visibleNotes = notes.filter {
                        selectedBatch.isBlank() || it.batchId.isBlank() || it.batchId == selectedBatch
                    }

                    if (visibleNotes.isEmpty()) Text("No notebook entries yet.")

                    visibleNotes.forEach { note ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    note.title.ifBlank { "Untitled entry" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Updated: ${note.updated}")
                                if (note.batchId.isNotBlank()) Text("Batch: ${note.batchId}")
                                Text(note.body, maxLines = 8)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = {
                                        noteId = note.id
                                        noteTitle = note.title
                                        noteBody = note.body
                                        selectedBatch = note.batchId
                                        showNoteEditor = true
                                    }) { Text("Edit") }

                                    TextButton(onClick = {
                                        db.deleteNote(note.id)
                                        reload()
                                    }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text("Custom data tables", style = MaterialTheme.typography.titleLarge)
                    Text("Create your own table names and headings, then add rows and enter observations or measurements.")

                    OutlinedTextField(
                        value = selectedBatch,
                        onValueChange = { selectedBatch = it },
                        label = { Text("Batch ID (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(onClick = {
                        tableTitle = ""
                        tableColumns = "Date,Observation,Result,Comments"
                        showTableDialog = true
                    }) { Text("＋ Create custom table") }

                    if (tables.isEmpty()) Text("No custom tables yet.")

                    tables.forEach { table ->
                        OutlinedButton(
                            onClick = { selectedTable = table.id },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (selectedTable == table.id) "Selected: ${table.title}"
                                else table.title
                            )
                        }
                    }

                    if (currentTable != null) {
                        HorizontalDivider()
                        Text(currentTable.title, style = MaterialTheme.typography.titleLarge)
                        Text("Table ID: ${currentTable.id}")
                        Text("Batch: ${currentTable.batchId.ifBlank { "Not linked" }}")

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentTable.columns.forEach { heading ->
                                Text(
                                    heading,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }

                        val tableRows = db.rows(currentTable.id)
                        tableRows.forEach { row ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    row.cells.forEachIndexed { index, value ->
                                        Text(
                                            "${currentTable.columns.getOrElse(index) { "Column ${index + 1} }}: $value"
                                        )
                                    }
                                    OutlinedButton(onClick = {
                                        // Row editing is available through the row editor below.
                                        selectedTable = currentTable.id
                                        editingRowId = row.id
                                        editingCells.clear()
                                        editingCells.addAll(row.cells)
                                        showRowEditor = true
                                    }) { Text("Edit row") }
                                }
                            }
                        }

                        Button(onClick = {
                            editingRowId = ""
                            editingCells.clear()
                            repeat(currentTable.columns.size) { editingCells.add("") }
                            showRowEditor = true
                        }) { Text("＋ Add row") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Storage: on-device SQLite database. These records are not yet included in an export or backup file.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            title = { Text("Register culture batch") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(batchCode, { batchCode = it }, label = { Text("Batch code") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(plantName, { plantName = it }, label = { Text("Plant / species") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(explant, { explant = it }, label = { Text("Explant / material") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(batchDate, { batchDate = it }, label = { Text("Start date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (batchCode.isNotBlank() && plantName.isNotBlank()) {
                        val id = newId()
                        db.saveBatch(
                            CultureBatch(
                                id, batchCode.trim(), plantName.trim(), explant.trim(),
                                batchDate.trim(), "Active"
                            )
                        )
                        selectedBatch = id
                        showBatchDialog = false
                        reload()
                    }
                }) { Text("Save batch") }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNoteEditor) {
        AlertDialog(
            onDismissRequest = { showNoteEditor = false },
            title = { Text(if (noteId.isBlank()) "New notebook entry" else "Edit notebook entry") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(noteTitle, { noteTitle = it }, label = { Text("Title / heading") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        noteBody,
                        { noteBody = it },
                        label = { Text("Write your notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6
                    )
                    Text("Batch ID: ${selectedBatch.ifBlank { "No batch linked" }}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    db.saveNote(
                        CultureNote(
                            noteId.ifBlank { newId() },
                            selectedBatch.trim(),
                            noteTitle.trim(),
                            noteBody,
                            timestamp()
                        )
                    )
                    showNoteEditor = false
                    reload()
                }) { Text("Save note") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteEditor = false }) { Text("Cancel") }
            }
        )
    }

    if (showTableDialog) {
        AlertDialog(
            onDismissRequest = { showTableDialog = false },
            title = { Text("Create custom table") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(tableTitle, { tableTitle = it }, label = { Text("Table name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        tableColumns,
                        { tableColumns = it },
                        label = { Text("Column headings, separated by commas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Text("Example: Date, pH, Medium, Observation, Result")
                }
            },
            confirmButton = {
                Button(onClick = {
                    val columns = tableColumns.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    if (tableTitle.isNotBlank() && columns.isNotEmpty()) {
                        val id = newId()
                        db.saveTable(
                            CultureTable(
                                id, selectedBatch.trim(), tableTitle.trim(), columns
                            )
                        )
                        selectedTable = id
                        showTableDialog = false
                        reload()
                    }
                }) { Text("Create table") }
            },
            dismissButton = {
                TextButton(onClick = { showTableDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showRowEditor && currentTable != null) {
        AlertDialog(
            onDismissRequest = { showRowEditor = false },
            title = { Text(if (editingRowId.isBlank()) "Add table row" else "Edit table row") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentTable.columns.forEachIndexed { index, heading ->
                        OutlinedTextField(
                            value = editingCells.getOrElse(index) { "" },
                            onValueChange = { value ->
                                while (editingCells.size <= index) editingCells.add("")
                                editingCells[index] = value
                            },
                            label = { Text(heading) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    db.saveRow(
                        CultureTableRow(
                            editingRowId.ifBlank { newId() },
                            currentTable.id,
                            editingCells.toList()
                        )
                    )
                    showRowEditor = false
                    reload()
                }) { Text("Save row") }
            },
            dismissButton = {
                TextButton(onClick = { showRowEditor = false }) { Text("Cancel") }
            }
        )
    }
}
