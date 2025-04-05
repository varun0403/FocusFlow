package com.example.focusflow.Employee

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.focusflow.Employer.DropdownField
import com.example.focusflow.Employer.TaskData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow

@Composable
fun EmployeeTaskViewer() {
    val firestore = Firebase.firestore
    var selectedYear by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("") }
    var selectedWeek by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Now holds both TaskData and its Firestore ID
    var taskList by remember { mutableStateOf<List<Pair<TaskData, String>>?>(null) }
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Employee Task Viewer", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        // Year & Month Dropdown
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownField(
                label = "Year",
                options = listOf("2024", "2025"),
                selectedOption = selectedYear,
                onOptionSelected = { selectedYear = it },
                modifier = Modifier.weight(1f)
            )
            DropdownField(
                label = "Month",
                options = listOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                ),
                selectedOption = selectedMonth,
                onOptionSelected = { selectedMonth = it },
                modifier = Modifier.weight(1f)
            )
        }

        // Second row (Week + Day)
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownField(
                label = "Week",
                options = listOf("Week 1", "Week 2", "Week 3", "Week 4"),
                selectedOption = selectedWeek,
                onOptionSelected = { selectedWeek = it },
                modifier = Modifier.weight(1f)
            )
            DropdownField(
                label = "Day",
                options = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                selectedOption = selectedDay,
                onOptionSelected = { selectedDay = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔍 Fetch tasks button
        Button(
            onClick = {
                if (selectedYear.isNotEmpty() && selectedMonth.isNotEmpty()
                    && selectedWeek.isNotEmpty() && selectedDay.isNotEmpty()
                ) {
                    isLoading = true // Start loading
                    val path = firestore
                        .collection("Tasks")
                        .document(selectedYear)
                        .collection(selectedMonth)
                        .document(selectedWeek)
                        .collection(selectedDay)

                    path.get().addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val tasksWithIds = snapshot.documents.mapNotNull { doc ->
                                val task = doc.toObject(TaskData::class.java)
                                val taskId = doc.id
                                if (task != null) task to taskId else null
                            }
                            taskList = tasksWithIds
                        } else {
                            taskList = emptyList()
                        }
                        isLoading = false // Stop loading
                    }.addOnFailureListener {
                        taskList = emptyList()
                        isLoading = false // Stop loading even on failure
                    }
                }
            },
            enabled = !isLoading // Disable button while loading
        ) {
            Text(if (isLoading) "Fetching..." else "Fetch Tasks")
        }

        // 👇 Show loading spinner if loading
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📋 Display tasks or no data
        when {
            taskList == null -> {}
            taskList!!.isEmpty() -> {
                Text("No data available", color = Color.Gray)
            }
            else -> {
                taskList!!.forEach { (task, taskId) ->
                    TaskCard(task, selectedYear, selectedMonth, selectedWeek, selectedDay, taskId)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskData,
    year: String,
    month: String,
    week: String,
    day: String,
    taskId: String
) {
    val db = Firebase.firestore
    val context = LocalContext.current
    var status by rememberSaveable(taskId) { mutableStateOf(task.status) }

    Log.d("TaskCard", "taskId: $taskId | status: $status")

    val backgroundColor = if (status == 1)
        Color(0xCCB2FFB2) // light green
    else
        Color(0xFFFFCCCC) // light red

    val swipeState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.5f },
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd && status != 1) {
                val taskRef = db.collection("Tasks")
                    .document(year)
                    .collection(month)
                    .document(week)
                    .collection(day)
                    .document(taskId)

                taskRef.update("status", 1)
                    .addOnSuccessListener {
                        status = 1
                        Toast.makeText(context, "Task marked as completed!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            false
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        backgroundContent = {
            if (status != 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFB2FFB2), shape = RoundedCornerShape(8.dp))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Complete", tint = Color.White)
                }
            } else {
                // Completely empty background — no Box, no shape, no color
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }
        ,
        content = {
            // 👇 Apply card-like design here
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                    .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Task: ${task.taskDesc}", style = MaterialTheme.typography.titleMedium)
                    Text("Category: ${task.category}", style = MaterialTheme.typography.bodyMedium)
                    Text("Type: ${task.type}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (status == 1) {
                        Text("✅ Completed", color = Color(0xFF388E3C))
                    }
                }
            }
        }
    )
}
