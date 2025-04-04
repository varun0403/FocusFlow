import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmployeeTaskManager()
        }
    }
}

data class TaskData(val taskDesc: String, val category: String, val type: String)

@Composable
fun EmployeeTaskManager() {
    var selectedYear by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("") }
    var selectedWeek by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("") }

    val taskList = remember { mutableStateListOf(TaskData("", "", "")) }
    val taskDataMap = remember {
        mutableStateOf(
            mutableMapOf<String, MutableMap<String, MutableMap<String, MutableMap<String, MutableList<TaskData>>>>>()
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Employee Task Manager", style = MaterialTheme.typography.headlineSmall, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // First row (Year + Month)
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

        if (selectedYear.isNotEmpty() && selectedMonth.isNotEmpty() &&
            selectedWeek.isNotEmpty() && selectedDay.isNotEmpty()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Assign Tasks:", style = MaterialTheme.typography.titleMedium)

            taskList.forEachIndexed { index, task ->
                TaskEntry(task) { updatedTask -> taskList[index] = updatedTask }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { taskList.add(TaskData("", "", "")) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("+ Add Task")
                }

                Button(
                    onClick = {
                        val tasksForDay = taskList.toMutableList()
                        val yearMap = taskDataMap.value.getOrPut(selectedYear) { mutableMapOf() }
                        val monthMap = yearMap.getOrPut(selectedMonth) { mutableMapOf() }
                        val weekMap = monthMap.getOrPut(selectedWeek) { mutableMapOf() }
                        weekMap[selectedDay] = tasksForDay.toMutableList()
                        Log.d("OP", taskDataMap.value.toString())
                        uploadTasks(taskDataMap.value)
                        // ✅ Clear text fields by resetting task list
                        taskList.clear()
                        taskList.add(TaskData("", "", ""))  // Add an empty task to keep UI structure
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

    Box(
        modifier = modifier
            .height(56.dp)
    ) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = selectedOption.ifEmpty { label })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 180.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
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
        DropdownField(
            label = "Category",
            options = listOf("Delegate", "Growth", "Important", "Not Important"),
            selectedOption = taskData.category,
            onOptionSelected = { onTaskChange(taskData.copy(category = it)) },
            modifier = Modifier.weight(1f)
        )
        DropdownField(
            label = "Type",
            options = listOf("Call", "Meeting", "Physical Task"),
            selectedOption = taskData.type, // ✅ Used `type` instead of `category`
            onOptionSelected = { onTaskChange(taskData.copy(type = it)) }, // ✅ Updated correctly
            modifier = Modifier.weight(1f)
        )
    }
}

fun uploadTasks(taskDataMap: Map<String, Map<String, Map<String, Map<String, List<TaskData>>>>>) {
    val db = Firebase.firestore

    taskDataMap.forEach { (year, months) ->
        months.forEach { (month, weeks) ->
            weeks.forEach { (week, days) ->
                days.forEach { (day, tasks) ->

                    // Reference to: Tasks → year → month → week → day (collection)
                    val dayCollectionRef = db
                        .collection("Tasks")
                        .document(year)
                        .collection(month)
                        .document(week)
                        .collection(day)

                    tasks.forEach { task ->
                        val taskMap = mapOf(
                            "taskDesc" to task.taskDesc,
                            "category" to task.category,
                            "type" to task.type
                        )

                        dayCollectionRef
                            .add(taskMap)  // auto-generates TaskID
                            .addOnSuccessListener {
                                Log.d("Firestore", "Task uploaded successfully: ${it.id}")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error uploading task", e)
                            }
                    }
                }
            }
        }
    }
}
