package com.example.greetingcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.greetingcard.ui.theme.GreetingCardTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreetingCardTheme {
                KanbanScreen()
            }
        }
    }
}

@Composable
fun CategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.SUPERMERCADO -> Color(0xFFC8E6C9) // Light Green
        TaskCategory.FARMACIA -> Color(0xFFBBDEFB)     // Light Blue
        TaskCategory.HOGAR -> Color(0xFFFFECB3)         // Light Amber
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanScreen() {
    val tasks = remember {
        mutableStateListOf(
            Task(1, "Comprar leche", TaskCategory.SUPERMERCADO, TaskStatus.TODO),
            Task(2, "Pagar cuentas", TaskCategory.HOGAR, TaskStatus.TODO),
            Task(3, "Comprar ibuprofeno", TaskCategory.FARMACIA, TaskStatus.IN_PROGRESS),
            Task(4, "Llamar al plomero", TaskCategory.HOGAR, TaskStatus.DONE)
        )
    }
    var showDialog by remember { mutableStateOf(false) }

    fun moveTask(taskId: Int, newStatus: TaskStatus) {
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = tasks[taskIndex]
            tasks[taskIndex] = task.copy(status = newStatus)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_task))
            }
        }
    ) { paddingValues ->
        KanbanBoard(
            tasks = tasks,
            onMoveTask = ::moveTask,
            modifier = Modifier.padding(paddingValues)
        )

        if (showDialog) {
            AddTaskDialog(
                onTaskCreate = { title, category ->
                    val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                    tasks.add(Task(newId, title, category, TaskStatus.TODO))
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onTaskCreate: (String, TaskCategory) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.HOGAR) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.dialog_new_task_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.field_title)) })
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = stringResource(id = selectedCategory.displayName),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.field_category)) },
                        leadingIcon = {
                            Box(modifier = Modifier
                                .size(16.dp)
                                .background(CategoryColor(category = selectedCategory), shape = CircleShape))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        TaskCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier
                                            .size(16.dp)
                                            .background(CategoryColor(category = category), shape = CircleShape))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(id = category.displayName))
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.button_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onTaskCreate(title, selectedCategory) },
                        enabled = title.isNotBlank()
                    ) {
                        Text(stringResource(R.string.button_create))
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanBoard(tasks: List<Task>, onMoveTask: (Int, TaskStatus) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxSize().padding(4.dp)) {
        TaskColumn(title = stringResource(R.string.column_todo), tasks = tasks.filter { it.status == TaskStatus.TODO }, onMoveTask = onMoveTask, modifier = Modifier.weight(1f))
        TaskColumn(title = stringResource(R.string.column_in_progress), tasks = tasks.filter { it.status == TaskStatus.IN_PROGRESS }, onMoveTask = onMoveTask, modifier = Modifier.weight(1f))
        TaskColumn(title = stringResource(R.string.column_done), tasks = tasks.filter { it.status == TaskStatus.DONE }, onMoveTask = onMoveTask, modifier = Modifier.weight(1f))
    }
}

@Composable
fun TaskColumn(title: String, tasks: List<Task>, onMoveTask: (Int, TaskStatus) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(16.dp))
            .fillMaxHeight()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
            items(tasks) { task ->
                TaskCard(task = task, onMoveTask = onMoveTask)
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onMoveTask: (Int, TaskStatus) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CategoryColor(category = task.category)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(id = task.category.displayName), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (task.status) {
                    TaskStatus.TODO -> {
                        IconButton(onClick = { onMoveTask(task.id, TaskStatus.IN_PROGRESS) }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.cd_move_to_in_progress))
                        }
                    }
                    TaskStatus.IN_PROGRESS -> {
                        IconButton(onClick = { onMoveTask(task.id, TaskStatus.TODO) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_move_to_todo))
                        }
                        IconButton(onClick = { onMoveTask(task.id, TaskStatus.DONE) }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.cd_move_to_done))
                        }
                    }
                    TaskStatus.DONE -> {
                        IconButton(onClick = { onMoveTask(task.id, TaskStatus.IN_PROGRESS) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_move_to_in_progress))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KanbanScreenPreview() {
    GreetingCardTheme {
        KanbanScreen()
    }
}
