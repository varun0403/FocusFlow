package com.example.focusflow.Employee

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.focusflow.Employer.Project
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp


data class Project(
    val id: String = "",
    val name: String = "",
    val goal: String = "",
    val deadline: String = "",
    val employees: List<String> = emptyList()
)

@Composable
fun EmployeeHomeScreen(navController: NavController, employeeEmail: String) {
    val firestore = Firebase.firestore
    var projectList by remember { mutableStateOf<List<Project>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            val snapshot = firestore
                .collection("Projects")
                .whereArrayContains("employees", employeeEmail)
                .get()
                .await()

            val projects = snapshot.documents.map { doc ->
                Project(
                    id = doc.id,
                    name = doc.getString("title") ?: "",
                    goal = doc.getString("goal") ?: "",
                    deadline = doc.getString("deadline") ?: "",
                    employees = doc.get("employees") as? List<String> ?: emptyList()
                )
            }

            projectList = projects
        } catch (e: Exception) {
            Log.e("Firestore", "Error loading employee's projects", e)
        } finally {
            loading = false
        }
    }
    if (loading) {
        CircularProgressIndicator()
    }
    else {
        LazyColumn {
            items(projectList) { project ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(
                                "employee_task_viewer/${project.id}/${employeeEmail}"
                            )
                        },
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = project.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Goal: ${project.goal}")
                        Text(text = "Deadline: ${project.deadline}")
                    }
                }
            }
        }
    }
}


