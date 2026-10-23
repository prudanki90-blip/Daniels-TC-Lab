
package com.tclab.android.feature.protocols.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tclab.android.feature.protocols.data.Protocol
import com.tclab.android.feature.protocols.data.ProtocolRepository
import com.tclab.android.feature.protocols.extractor.ProtocolExtractor
import com.tclab.android.feature.protocols.loader.ProtocolDataLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Standalone document-import screen for TC Lab Protocols.
 *
 * Loads supported local text documents, extracts provisional protocol
 * fields, and saves reviewed-by-user candidates to offline storage.
 */
@Composable
fun ProtocolLoaderScreen(
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val loader = remember(context) {
        ProtocolDataLoader(context)
    }
    val extractor = remember {
        ProtocolExtractor()
    }
    val repository = remember(context) {
        ProtocolRepository(context)
    }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var documentText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var extraction by remember {
        mutableStateOf<ProtocolExtractor.ExtractionResult?>(null)
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            isLoading = true
            errorMessage = null
            statusMessage = null
            extraction = null
            documentText = ""
            fileName = ""
            title = ""

            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    loader.load(uri)
                }

                isLoading = false

                if (result.success) {
                    fileName = result.fileName
                    documentText = result.text
                    title = result.fileName.substringBeforeLast(
                        '.',
                        result.fileName
                    )

                    statusMessage = "Document loaded. Ready to extract."
                } else {
                    errorMessage = result.error
                        ?: result.warnings.firstOrNull()
                        ?: "The document could not be loaded."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Protocol Document Import",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Import a supported text document, extract possible " +
                "tissue-culture protocol fields, then review the result " +
                "before saving it offline.",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Supported by the current loader",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "TXT, Markdown, CSV, TSV, JSON, XML, HTML and RTF"
                )
                Text(
                    text = "PDF, Word documents, spreadsheets and scanned " +
                        "images are not yet parsed by this loader."
                )
                Text(
                    text = "Text import limit: 10 MB."
                )
            }
        }

        Button(
            onClick = {
                documentPicker.launch(
                    arrayOf(
                        "text/plain",
                        "text/markdown",
                        "text/csv",
                        "text/tab-separated-values",
                        "application/json",
                        "application/xml",
                        "text/xml",
                        "text/html",
                        "application/rtf",
                        "text/rtf",
                        "*/*"
                    )
                )
            },
            enabled = !isLoading && !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Choose document")
        }

        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text("Reading document…")
            }
        }

        if (fileName.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Selected document",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(fileName)
                    Text("${documentText.length} characters loaded")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Protocol title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    extraction = null
                    errorMessage = null
                    statusMessage = null

                    scope.launch {
                        val result = withContext(Dispatchers.Default) {
                            extractor.extract(
                                text = documentText,
                                fileName = fileName,
                                mimeType = context.contentResolver
                                    .getType(selectedUri ?: Uri.EMPTY)
                                    .orEmpty()
                            )
                        }

                        extraction = result
                        statusMessage = "Extraction finished. " +
                            "Check every field against the original document."
                    }
                },
                enabled = documentText.isNotBlank() &&
                    !isLoading &&
                    !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Extract protocol information")
            }
        }

        statusMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        extraction?.let { result ->
            HorizontalDivider()

            Text(
                text = "Extraction results",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Detected topics: " +
                    (result.detectedTopics.joinToString().ifBlank {
                        "No known topic keywords detected"
                    })
            )

            Text(
                text = "Plant information",
                style = MaterialTheme.typography.titleMedium
            )
            ResultRow(
                label = "Scientific name",
                value = result.protocol.plant.scientificName
            )
            ResultRow(
                label = "Common name",
                value = result.protocol.plant.commonName
            )
            ResultRow(
                label = "Family",
                value = result.protocol.plant.family
            )
            ResultRow(
                label = "Cultivar / variety",
                value = result.protocol.plant.cultivarOrVariety
            )

            Text(
                text = "Explant",
                style = MaterialTheme.typography.titleMedium
            )
            ResultRow(
                label = "Explant type",
                value = result.protocol.explant.explantType
            )
            ResultRow(
                label = "Source tissue",
                value = result.protocol.explant.sourceTissue
            )

            Text(
                text = "Culture media",
                style = MaterialTheme.typography.titleMedium
            )
            if (result.protocol.media.isEmpty()) {
                Text("No basal medium or pH field was identified.")
            } else {
                result.protocol.media.forEachIndexed { index, medium ->
                    ResultRow(
                        label = "Medium ${index + 1}",
                        value = medium.basalMedium.ifBlank {
                            medium.name.ifBlank { "Not identified" }
                        }
                    )
                    ResultRow(
                        label = "Target pH",
                        value = medium.targetPh
                    )
                }
            }

            Text(
                text = "Culture conditions",
                style = MaterialTheme.typography.titleMedium
            )
            ResultRow(
                label = "Temperature",
                value = result.protocol.generalConditions.temperature
            )
            ResultRow(
                label = "Photoperiod",
                value = result.protocol.generalConditions.photoperiod
            )
            ResultRow(
                label = "Light intensity",
                value = result.protocol.generalConditions.lightIntensity
            )
            ResultRow(
                label = "Humidity",
                value = result.protocol.generalConditions.humidity
            )

            Text(
                text = "Sterilization lines: " +
                    result.protocol.sterilizationSteps.size
            )
            Text(
                text = "Culture stages detected: " +
                    result.protocol.stages.size
            )
            Text(
                text = "Evidence records: " +
                    result.protocol.evidence.size
            )

            Text(
                text = "Warnings",
                style = MaterialTheme.typography.titleMedium
            )
            result.warnings.forEach { warning ->
                Text("• $warning")
            }

            Text(
                text = "Source evidence",
                style = MaterialTheme.typography.titleMedium
            )
            if (result.protocol.evidence.isEmpty()) {
                Text("No explicit labelled fields were extracted.")
            } else {
                result.protocol.evidence.take(20).forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                item.fieldPath,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(item.extractedValue)
                            Text(
                                text = item.sourceSnippet,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = item.sourceLocation,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                if (result.protocol.evidence.size > 20) {
                    Text(
                        "Showing the first 20 evidence records. " +
                            "The full evidence list remains in the saved protocol."
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    extraction = null
                    statusMessage = "Extraction results cleared. " +
                        "The original loaded text is still available."
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear extraction results")
            }

            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    statusMessage = null

                    scope.launch {
                        try {
                            val protocolToSave = result.protocol.copy(
                                title = title.trim().ifBlank {
                                    result.protocol.title.ifBlank {
                                        fileName
                                    }
                                }
                            )

                            withContext(Dispatchers.IO) {
                                repository.save(protocolToSave)
                            }

                            statusMessage =
                                "Protocol saved to the offline catalogue."
                        } catch (exception: Exception) {
                            errorMessage =
                                "Could not save the protocol: " +
                                    (exception.message
                                        ?: "Unknown storage error.")
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isSaving) "Saving…" else "Save protocol offline"
                )
            }
        }

        onBack?.let { back ->
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = back,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.ifBlank { "Not detected" },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f)
        )
    }
}
