package com.sport.gymtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.sport.gymtracker.R
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sport.gymtracker.ui.screens.ExerciseEditorScreen
import com.sport.gymtracker.ui.screens.ExerciseBlueprintEditorScreen
import com.sport.gymtracker.ui.screens.ExerciseLibraryScreen
import com.sport.gymtracker.ui.screens.ExerciseProgressDetailScreen
import com.sport.gymtracker.ui.screens.ExerciseProgressListScreen
import com.sport.gymtracker.ui.screens.BackupScreen
import com.sport.gymtracker.ui.screens.HomeScreen
import com.sport.gymtracker.ui.screens.StatisticsScreen
import com.sport.gymtracker.ui.screens.SessionDetailScreen
import com.sport.gymtracker.ui.screens.SessionsScreen
import com.sport.gymtracker.ui.screens.TemplateDetailScreen
import com.sport.gymtracker.ui.screens.TemplateExerciseEditorScreen
import com.sport.gymtracker.ui.screens.TemplatesListScreen

private object Routes {
    const val HOME = "home"
    const val STATISTICS = "statistics"
    const val SESSIONS = "sessions"
    const val TEMPLATES = "templates"
    const val SESSION = "session/{sessionId}"
    const val EXERCISE = "session/{sessionId}/exercise/{exerciseId}"
    const val TEMPLATE_DETAIL = "template/{templateId}"
    const val TEMPLATE_EXERCISE = "template/{templateId}/exercise/{exerciseId}"
    const val EXERCISE_LIBRARY = "exercise_library"
    const val EXERCISE_LIBRARY_BLUEPRINT = "exercise_library/blueprint/{blueprintId}"
    const val EXERCISE_PROGRESS = "exercise_progress"
    const val EXERCISE_PROGRESS_DETAIL = "exercise_progress/{blueprintId}"
    const val BACKUP = "backup"
}

private val bottomItems = listOf(
    Triple(Routes.HOME, "Accueil", Icons.Default.Home),
    Triple(Routes.TEMPLATES, "Modèles", Icons.AutoMirrored.Filled.Assignment),
    Triple(Routes.SESSIONS, "Séances", Icons.AutoMirrored.Filled.List),
    Triple(Routes.STATISTICS, "Stats", Icons.Default.BarChart),
    Triple(Routes.BACKUP, "Données", Icons.Filled.Save),
)

private val bottomRouteSet =
    setOf(Routes.HOME, Routes.SESSIONS, Routes.TEMPLATES, Routes.STATISTICS, Routes.BACKUP)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTrackerNavHost(
    darkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val dest = backStack?.destination
    val showBar = dest?.route in bottomRouteSet

    Scaffold(
        topBar = {
            if (showBar) {
                TopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = onToggleDarkTheme) {
                            Icon(
                                painter =
                                    painterResource(
                                        if (darkTheme) {
                                            R.drawable.ic_theme_light
                                        } else {
                                            R.drawable.ic_theme_dark
                                        },
                                    ),
                                contentDescription =
                                    if (darkTheme) {
                                        "Passer en thème clair"
                                    } else {
                                        "Passer en thème sombre"
                                    },
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            }
        },
        bottomBar = {
            if (showBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    bottomItems.forEach { (route, label, icon) ->
                        val selected = dest?.hierarchy?.any { it.route == route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenSession = { id -> navController.navigate("session/$id") },
                )
            }
            composable(Routes.STATISTICS) {
                StatisticsScreen(
                    onOpenExerciseProgress = { navController.navigate(Routes.EXERCISE_PROGRESS) },
                )
            }
            composable(Routes.EXERCISE_PROGRESS) {
                ExerciseProgressListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenExercise = { id ->
                        navController.navigate("exercise_progress/$id")
                    },
                )
            }
            composable(
                Routes.EXERCISE_PROGRESS_DETAIL,
                arguments = listOf(navArgument("blueprintId") { type = NavType.LongType }),
            ) { entry ->
                val bid = entry.arguments!!.getLong("blueprintId")
                ExerciseProgressDetailScreen(
                    blueprintId = bid,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SESSIONS) {
                SessionsScreen(
                    onSessionClick = { id -> navController.navigate("session/$id") },
                )
            }
            composable(Routes.TEMPLATES) {
                TemplatesListScreen(
                    onTemplateClick = { id -> navController.navigate("template/$id") },
                    onOpenExerciseLibrary = { navController.navigate(Routes.EXERCISE_LIBRARY) },
                )
            }
            composable(Routes.BACKUP) {
                BackupScreen()
            }
            composable(Routes.EXERCISE_LIBRARY) {
                ExerciseLibraryScreen(
                    onBack = { navController.popBackStack() },
                    onEditBlueprint = { id ->
                        navController.navigate("exercise_library/blueprint/$id")
                    },
                )
            }
            composable(
                Routes.EXERCISE_LIBRARY_BLUEPRINT,
                arguments = listOf(navArgument("blueprintId") { type = NavType.LongType }),
            ) { entry ->
                val blueprintId = entry.arguments!!.getLong("blueprintId")
                ExerciseBlueprintEditorScreen(
                    blueprintId = blueprintId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.SESSION,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments!!.getLong("sessionId")
                SessionDetailScreen(
                    sessionId = id,
                    onBack = { navController.popBackStack() },
                    onAddNewExercise = {
                        navController.navigate("session/$id/exercise/0")
                    },
                    onEditExercise = { exId ->
                        navController.navigate("session/$id/exercise/$exId")
                    },
                )
            }
            composable(
                Routes.EXERCISE,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.LongType },
                    navArgument("exerciseId") { type = NavType.LongType },
                ),
            ) { entry ->
                val sid = entry.arguments!!.getLong("sessionId")
                val eid = entry.arguments!!.getLong("exerciseId")
                val editId = if (eid == 0L) null else eid
                ExerciseEditorScreen(
                    sessionId = sid,
                    exerciseId = editId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.TEMPLATE_DETAIL,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType }),
            ) { entry ->
                val tid = entry.arguments!!.getLong("templateId")
                TemplateDetailScreen(
                    templateId = tid,
                    onBack = { navController.popBackStack() },
                    onAddExercise = { navController.navigate("template/$tid/exercise/0") },
                    onEditExercise = { exId -> navController.navigate("template/$tid/exercise/$exId") },
                )
            }
            composable(
                Routes.TEMPLATE_EXERCISE,
                arguments = listOf(
                    navArgument("templateId") { type = NavType.LongType },
                    navArgument("exerciseId") { type = NavType.LongType },
                ),
            ) { entry ->
                val tid = entry.arguments!!.getLong("templateId")
                val eid = entry.arguments!!.getLong("exerciseId")
                val editId = if (eid == 0L) null else eid
                TemplateExerciseEditorScreen(
                    templateId = tid,
                    exerciseId = editId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
