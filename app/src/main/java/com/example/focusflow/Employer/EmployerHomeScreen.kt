package com.example.focusflow.Employer

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// Data model for a project
data class Project(
    val name: String = "",
    val goal: String = "",
    val deadline: String = "",
    val employees: List<String> = emptyList(),
    val id: String = "" // Firestore doc ID
)

@Composable
fun EmployerHomeScreen(navController: NavController, email: String) {
    val firestore = Firebase.firestore
    var projectList by remember { mutableStateOf<List<Project>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            val snapshot = firestore
                .collection("Projects")
                .whereEqualTo("createdBy", email) // 👈 filtering by employer
                .get()
                .await()

            val projects = snapshot.documents.map { doc ->
                Project(
                    name = doc.getString("title") ?: "",
                    goal = doc.getString("goal") ?: "",
                    deadline = doc.getString("deadline") ?: "",
                    employees = doc.get("employees") as? List<String> ?: emptyList(),
                    id = doc.id
                )
            }
            projectList = projects
        } catch (e: Exception) {
            Log.e("Firestore", "Error loading projects", e)
        } finally {
            loading = false
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("create_project/$email")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(8.dp)) {
            Text("Welcome $email")
            Text("Your Projects", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else if (projectList.isEmpty()) {
                Text("No projects found.", color = Color.Gray)
            }
            else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projectList) { project ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("employee_list/${project.id}")
                                },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(project.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                                Text("Goal: ${project.goal}", fontSize = 14.sp)
                                Text("Deadline: ${project.deadline}", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
