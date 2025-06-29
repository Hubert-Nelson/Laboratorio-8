package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab08.ui.theme.Lab08Theme

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()


                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)


                TaskScreen(viewModel)
            }
        }
    }
}


@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }

    // Estado para el diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text("Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )


        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Agregar tarea")
        }


        Spacer(modifier = Modifier.height(16.dp))


        tasks.forEach { task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Centra verticalmente
            ) {
                Text(
                    text = task.description,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { // <-- Nuevo modificador clickable
                            taskToEdit = task
                            editedDescription = task.description
                            showEditDialog = true
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = { viewModel.toggleTaskCompletion(task) }) {
                    Text(if (task.isCompleted) "Completada" else "Pendiente")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // boton para eliminar tarea individual
                Button(
                    onClick = { viewModel.deleteTask(task) }, // Llama a la nueva función del ViewModel
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Color rojo para eliminar
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }


        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false }, // Cierra el diálogo al hacer clic fuera de él
                title = { Text("Editar Tarea") },
                text = {
                    TextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Nueva descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            taskToEdit?.let { task ->
                                if (editedDescription.isNotEmpty()) {
                                    viewModel.editTask(task, editedDescription) // Llama a la función de edición
                                }
                            }
                            showEditDialog = false
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}