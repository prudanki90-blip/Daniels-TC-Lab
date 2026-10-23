package com.tclab.android
import com.tclab.android.feature.calculator.screens.CalculatorScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tclab.android.feature.protocols.screens.ProtocolLoaderScreen

private data class LabSection(
    val title: String,
    val description: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TCLabApp()
                }
            }
        }
    }
}

private val labSections = listOf(
    LabSection(
        "Calculator",
        "Scientific calculations and laboratory solution tools."
    ),
    LabSection(
        "Protocol Library",
        "Organize protocols, stages, materials, conditions and sources."
    ),
    LabSection(
        "Research Notebook",
        "Record experiments, observations and measured results."
    ),
    LabSection(
        "Culture Records",
        "Track culture batches, identities and their histories."
    ),
    LabSection(
        "Timeline & Reminders",
        "Plan tasks, record completions and track due dates."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TCLabApp() {
    var selectedSection by remember {
        mutableIntStateOf(0)
    }

    val titles = listOf(
        "Home",
        "Calculator",
        "Protocols",
        "Research",
        "Cultures",
        "Timeline"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TC Lab")
                }
            )
        },
        bottomBar = {
            NavigationBar {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedSection == index,
                        onClick = {
                            selectedSection = index
                        },
                        icon = {},
                        label = {
                            Text(title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedSection) {
            0 -> HomeScreen(
                padding = innerPadding,
                onSectionSelected = { selectedSection = it }
            )
            1 -> Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
) {
    CalculatorScreen()
}
            
            

           

            2 -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ProtocolLoaderScreen(
                    onBack = {
                        selectedSection = 0
                    }
                )
            }

            3 -> SectionScreen(
                padding = innerPadding,
                title = "Research Notebook",
                description = labSections[2].description,
                details = listOf(
                    "Research records",
                    "Observations and measurements",
                    "Experimental notes",
                    "Published references"
                )
            )

            4 -> SectionScreen(
                padding = innerPadding,
                title = "Culture Records",
                description = labSections[3].description,
                details = listOf(
                    "Culture batches",
                    "Plant and protocol identity",
                    "Batch history",
                    "Observation records"
                )
            )

            5 -> SectionScreen(
                padding = innerPadding,
                title = "Timeline & Reminders",
                description = labSections[4].description,
                details = listOf(
                    "Upcoming tasks",
                    "Due and overdue tasks",
                    "Completion records",
                    "Rescheduled tasks"
                )
            )
        }
    }
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    onSectionSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Laboratory workspace",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Text(
                text = "TC Lab is being built as an offline-first " +
                    "Android laboratory workspace. Choose a section " +
                    "to begin."
            )
        }

        items(labSections) { section ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(text = section.description)

                    val index = labSections.indexOf(section) + 1

                    Button(
                        onClick = {
                            onSectionSelected(index)
                        }
                    ) {
                        Text("Open section")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionScreen(
    padding: PaddingValues,
    title: String,
    description: String,
    details: List<String>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Text(text = description)
        }

        item {
            Text(
                text = "Planned tools",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(details) { detail ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = detail,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            Text(
                text = "This section is a starting interface. " +
                    "Data entry, saving, editing and record tables " +
                    "will be implemented in the following steps.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
