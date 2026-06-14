package com.beerme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import com.beerme.ui.details.BeerDetailsScreen
import com.beerme.ui.details.BeerDetailsViewModel
import com.beerme.ui.details.BreweryDetailsScreen
import com.beerme.ui.details.BreweryDetailsViewModel
import com.beerme.ui.feedback.FeedbackScreen
import com.beerme.ui.feedback.FeedbackViewModel
import com.beerme.ui.map.MapScreen
import com.beerme.ui.map.MapViewModel
import com.beerme.ui.navigation.BeerDetailsRoute
import com.beerme.ui.navigation.BreweryDetailsRoute
import com.beerme.ui.navigation.FeedbackRoute
import com.beerme.ui.navigation.MapRoute
import com.beerme.ui.navigation.RoutePlannerRoute
import com.beerme.ui.route.RoutePlannerScreen
import com.beerme.ui.route.RoutePlannerViewModel
import com.beerme.ui.theme.BeerMeMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as BeerMeApplication).container
        
        setContent {
            BeerMeMobileTheme {
                BeerMeApp(appContainer)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BeerMeApp(appContainer: com.beerme.data.AppContainer) {
    val backStack = rememberNavBackStack(MapRoute)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val popBackStack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Each screen handles its own insets (the map is full-bleed).
        contentWindowInsets = WindowInsets(0.dp)
    ) { _ ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            sceneStrategy = listDetailStrategy then SinglePaneSceneStrategy(),
            entryProvider = entryProvider {
                entry<MapRoute> {
                    val viewModel: MapViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return MapViewModel(
                                    appContainer.repository,
                                    appContainer.userPreferencesRepository,
                                    appContainer.geocodingRepository
                                ) as T
                            }
                        }
                    )
                    MapScreen(
                        viewModel = viewModel,
                        onBreweryClick = { id ->
                            backStack.add(BreweryDetailsRoute(id))
                        },
                        onBeerClick = { id ->
                            backStack.add(BeerDetailsRoute(id))
                        },
                        onPlanRoute = {
                            backStack.add(RoutePlannerRoute)
                        },
                        onSuggestBrewery = {
                            backStack.add(FeedbackRoute())
                        }
                    )
                }
                entry<RoutePlannerRoute> {
                    val viewModel: RoutePlannerViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return RoutePlannerViewModel(
                                    appContainer.repository,
                                    appContainer.geocodingRepository,
                                    appContainer.directionsRepository,
                                    appContainer.userPreferencesRepository
                                ) as T
                            }
                        }
                    )
                    RoutePlannerScreen(
                        viewModel = viewModel,
                        onBack = popBackStack
                    )
                }
                entry<BreweryDetailsRoute>(
                    metadata = ListDetailSceneStrategy.listPane()
                ) { key ->
                    val viewModel: BreweryDetailsViewModel = viewModel(
                        key = key.breweryId,
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return BreweryDetailsViewModel(
                                    key.breweryId,
                                    appContainer.repository
                                ) as T
                            }
                        }
                    )
                    BreweryDetailsScreen(
                        viewModel = viewModel,
                        onBeerClick = { beerId ->
                            backStack.add(BeerDetailsRoute(beerId))
                        },
                        onReport = {
                            backStack.add(FeedbackRoute(key.breweryId))
                        },
                        onBack = popBackStack
                    )
                }
                entry<FeedbackRoute> { key ->
                    val viewModel: FeedbackViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return FeedbackViewModel(
                                    key.breweryId,
                                    BuildConfig.VERSION_NAME,
                                    appContainer.feedbackRepository,
                                    appContainer.repository
                                ) as T
                            }
                        }
                    )
                    FeedbackScreen(
                        viewModel = viewModel,
                        onBack = popBackStack
                    )
                }
                entry<BeerDetailsRoute>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { key ->
                    val viewModel: BeerDetailsViewModel = viewModel(
                        key = key.beerId,
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return BeerDetailsViewModel(
                                    key.beerId,
                                    appContainer.repository
                                ) as T
                            }
                        }
                    )
                    BeerDetailsScreen(
                        viewModel = viewModel,
                        onBack = popBackStack
                    )
                }
            },
            entryDecorators = listOf<NavEntryDecorator<NavKey>>(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = { popBackStack() }
        )
    }
}
