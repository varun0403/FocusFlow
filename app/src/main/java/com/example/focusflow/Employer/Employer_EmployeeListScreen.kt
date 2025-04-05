package com.example.focusflow.Employer

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun EmployeeListScreen(projectId: String, navController: NavController) {
    val firestore = Firebase.firestore
    var employeeList by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            val snapshot = firestore.collection("Projects").document(projectId).get().await()
            val employees = snapshot.get("employees") as? List<String> ?: emptyList()
            employeeList = employees
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching employees", e)
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(employeeList) { email ->
                EmployeeCard(email = email) {
                    navController.navigate("employer_task_manager/${projectId}/${email}")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

        }
    }
}

@Composable
fun EmployeeCard(email: String, onClick: () -> Unit) {
    val emojis = listOf("🧑‍💼", "👩‍💻", "🧑‍🔧", "👨‍🏫", "👨‍🚀", "🧑‍🎨", "👨‍🔬")
    val emoji = remember { emojis.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }, // 👈 clickable
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 30.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = email,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

