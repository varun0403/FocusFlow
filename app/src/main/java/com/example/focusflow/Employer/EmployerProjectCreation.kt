package com.example.focusflow.Employer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.*

@Composable
fun CreateProjectScreen(onProjectCreated: () -> Unit, navController: NavController, email: String) {
    val context = LocalContext.current
    var goal by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var employeeEmail by remember { mutableStateOf("") }
    var employeeList by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Create New Project", style = MaterialTheme.typography.headlineSmall, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Project Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("Project Goal") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = deadline,
            onValueChange = { deadline = it },
            label = { Text("Deadline (e.g. 30 April 2025)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = employeeEmail,
                onValueChange = { employeeEmail = it },
                label = { Text("Employee Email") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (employeeEmail.isNotBlank()) {
                    employeeList = employeeList + employeeEmail.trim()
                    employeeEmail = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Added Employees:", fontSize = 16.sp)
        employeeList.forEach { email ->
            Text("- $email", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val db = Firebase.firestore
            val projectId = UUID.randomUUID().toString()

            val projectMap = mapOf(
                "title" to title,
                "goal" to goal,
                "deadline" to deadline,
                "employees" to employeeList,
                "createdBy" to email
            )

            db.collection("Projects")
                .document(projectId)
                .set(projectMap)
                .addOnSuccessListener {
                    Toast.makeText(context, "Project created!", Toast.LENGTH_SHORT).show()
                    onProjectCreated()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
                }

            navController.navigate("employer_home/$email")
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Create Project")
        }
    }
}
