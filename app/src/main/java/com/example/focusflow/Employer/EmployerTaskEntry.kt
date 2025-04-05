package com.example.focusflow.Employer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.UUID

data class TaskData(
    val taskDesc: String = "",
    val category: String = "",
    val type: String = "",
    val status: Int = 0
)

@Composable
fun EmployerTaskManager(projectId: String, employeeEmail: String, navController: NavController) {
    var selectedYear by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("") }
    var selectedWeek by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("") }

    val taskList = remember { mutableStateListOf(TaskData("", "", "", 0)) }
    val fetchedTasks = remember { mutableStateListOf<TaskData>() }

    val taskDataMap = remember {
        mutableStateOf(
            mutableMapOf<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableList<TaskData>>>>>()
        )
    }

    LaunchedEffect(selectedYear, selectedMonth, selectedWeek, selectedDay) {
        if (selectedYear.isNotEmpty() && selectedMonth.isNotEmpty() && selectedWeek.isNotEmpty() && selectedDay.isNotEmpty()) {
            fetchExistingTasks(selectedYear, selectedMonth, selectedWeek, selectedDay, fetchedTasks)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Employer Task Manager", style = MaterialTheme.typography.headlineSmall, fontSize = 22.sp)
        Text("Task Allocation to employee: $employeeEmail", style = MaterialTheme.typography.headlineSmall, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // First row (Year + Month)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownField("Year", listOf("2024", "2025"), selectedYear, { selectedYear = it }, Modifier.weight(1f))
            DropdownField(
                "Month",
                listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"),
                selectedMonth, { selectedMonth = it }, Modifier.weight(1f)
            )
        }

        // Second row (Week + Day)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DropdownField("Week", listOf("Week 1", "Week 2", "Week 3", "Week 4"), selectedWeek, { selectedWeek = it }, Modifier.weight(1f))
            DropdownField("Day", listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"), selectedDay, { selectedDay = it }, Modifier.weight(1f))
        }

        if (selectedYear.isNotEmpty() && selectedMonth.isNotEmpty() && selectedWeek.isNotEmpty() && selectedDay.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Already Assigned Tasks:", style = MaterialTheme.typography.titleMedium)
            if (fetchedTasks.isEmpty()) {
                Text("No tasks found for this day.", style = MaterialTheme.typography.bodySmall)
            } else {
                fetchedTasks.forEach {
                    ExistingTaskCard(it)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Assign New Tasks:", style = MaterialTheme.typography.titleMedium)

            taskList.forEachIndexed { index, task ->
                TaskEntry(task) { updatedTask -> taskList[index] = updatedTask }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { taskList.add(TaskData()) }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("+ Add Task")
                }

                Button(
                    onClick = {
                        val tasksForDay = taskList.toMutableList()
                        // Update the in-memory task map
                        val yearMap = taskDataMap.value.getOrPut(selectedYear) { mutableMapOf() }
                        val monthMap = yearMap.getOrPut(selectedMonth) { mutableMapOf() }
                        val weekMap = monthMap.getOrPut(selectedWeek) { mutableMapOf() }
                        weekMap[selectedDay] = tasksForDay
                        uploadTasks(
                            projectId = projectId,
                            employeeEmail = employeeEmail,
                            taskMap = taskDataMap.value
                        )
                        taskList.clear()
                        taskList.add(TaskData())
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Submit")
                }

            }
        }
    }
}

@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.height(56.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = selectedOption.ifEmpty { label })
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun TaskEntry(taskData: TaskData, onTaskChange: (TaskData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = taskData.taskDesc,
            onValueChange = { onTaskChange(taskData.copy(taskDesc = it)) },
            modifier = Modifier.weight(1f),
            label = { Text("Task Description") }
        )
        DropdownField("Category", listOf("Delegate", "Growth", "Important", "Not Important"), taskData.category, {
            onTaskChange(taskData.copy(category = it))
        }, Modifier.weight(1f))
        DropdownField("Type", listOf("Call", "Meeting", "Physical Task"), taskData.type, {
            onTaskChange(taskData.copy(type = it))
        }, Modifier.weight(1f))
    }
}

@Composable
fun ExistingTaskCard(task: TaskData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.LightGray),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Task: ${task.taskDesc}", fontSize = 14.sp)
            Text("Category: ${task.category}", fontSize = 14.sp)
            Text("Type: ${task.type}", fontSize = 14.sp)
            Text("Status: ${task.status}", fontSize = 14.sp)
        }
    }
}

fun uploadTasks(
    projectId: String,
    employeeEmail: String,
    taskMap: Map<String, Map<String, Map<String, Map<String, List<TaskData>>>>>
) {
    val firestore = Firebase.firestore

    taskMap.forEach { (year, monthMap) ->
        monthMap.forEach { (month, weekMap) ->
            weekMap.forEach { (week, dayMap) ->
                dayMap.forEach { (day, tasks) ->
                    tasks.forEach { task ->
                        val taskId = UUID.randomUUID().toString()
                        firestore
                            .collection("Tasks")
                            .document(projectId)
                            .collection(employeeEmail)
                            .document(year)
                            .collection(month)
                            .document(week)
                            .collection(day)
                            .document(taskId)
                            .set(task)
                    }
                }
            }
        }
    }
}


fun fetchExistingTasks(
    year: String,
    month: String,
    week: String,
    day: String,
    targetList: MutableList<TaskData>
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Tasks")
        .document(year)
        .collection(month)
        .document(week)
        .collection(day)
        .get()
        .addOnSuccessListener { snapshot ->
            targetList.clear()
            for (doc in snapshot.documents) {
                val task = TaskData(
                    taskDesc = doc.getString("taskDesc") ?: "",
                    category = doc.getString("category") ?: "",
                    type = doc.getString("type") ?: "",
                    status = (doc.getLong("status") ?: 0).toInt()
                )
                targetList.add(task)
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching tasks", e)
        }
}
