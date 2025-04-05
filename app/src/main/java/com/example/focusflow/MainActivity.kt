package com.example.focusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                        composable("employer_home") {
                            EmployerTaskManager()
                        }
                        composable("employee_home") {
                            EmployeeTaskViewer()
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
        Button(
            onClick = {
                navcontroller.navigate("employer_home")
            }
        ) {Text("Employer")}
        Button(
            onClick = {
                navcontroller.navigate("employee_home")
            }
        ) {Text("Employee")}
    }
}
