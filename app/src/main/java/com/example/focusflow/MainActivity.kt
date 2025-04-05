package com.example.focusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.focusflow.Employer.*
import com.example.focusflow.Employee.*
import com.example.focusflow.ui.theme.FocusFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusFlowTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable(
                            "employer_home/{email}",
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email").orEmpty()
                            EmployerHomeScreen(navController, email)
                        }
                        composable(
                            "employee_home/{email}",
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email").orEmpty()
                            EmployeeHomeScreen(navController, email)
                        }
                        composable(
                            "employee_task_viewer/{projectId}/{employeeEmail}",
                            arguments = listOf(
                                navArgument("projectId") { type = NavType.StringType },
                                navArgument("employeeEmail") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                            val employeeEmail = backStackEntry.arguments?.getString("employeeEmail") ?: ""
                            EmployeeTaskViewer(projectId, employeeEmail)
                        }

                        composable(
                            "employer_task_manager/{projectId}/{employeeEmail}",
                            arguments = listOf(
                                navArgument("projectId") { type = NavType.StringType },
                                navArgument("employeeEmail") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                            val employeeEmail = backStackEntry.arguments?.getString("employeeEmail") ?: ""

                            EmployerTaskManager(
                                projectId = projectId,
                                employeeEmail = employeeEmail,
                                navController = navController
                            )
                        }

                        composable(
                            route = "create_project/{email}",
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            CreateProjectScreen(
                                onProjectCreated = {
                                    navController.popBackStack()
                                },
                                navController = navController,
                                email = email
                            )
                        }
                        composable(
                            "employee_list/{projectId}",
                            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
                            EmployeeListScreen(projectId = projectId, navController = navController)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HomeScreen(navcontroller: NavController){
    Column(modifier = Modifier.padding(16.dp)){
        var email by remember { mutableStateOf("") }
        Text("Authenication")
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter email: ") }
        )
        Button(
            onClick = {
                if (email.contains("employer")){
                    navcontroller.navigate("employer_home/$email")
                }
                else{
                    navcontroller.navigate("employee_home/$email")
                }
            }
        ) { Text(text = "Login") }
    }
}
