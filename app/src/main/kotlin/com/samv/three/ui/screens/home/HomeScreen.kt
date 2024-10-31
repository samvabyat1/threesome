package com.samv.three.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import com.samv.compose.persist.PersistMapCleanup
import com.samv.compose.routing.RouteHandler
import com.samv.compose.routing.defaultStacking
import com.samv.compose.routing.defaultStill
import com.samv.compose.routing.defaultUnstacking
import com.samv.compose.routing.isStacking
import com.samv.compose.routing.isUnknown
import com.samv.compose.routing.isUnstacking
import com.samv.three.R
import com.samv.three.models.SearchQuery
import com.samv.three.query
import com.samv.three.ui.components.themed.Scaffold
import com.samv.three.ui.screens.albumRoute
import com.samv.three.ui.screens.artistRoute
import com.samv.three.ui.screens.builtInPlaylistRoute
import com.samv.three.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import com.samv.three.ui.screens.globalRoutes
import com.samv.three.ui.screens.localPlaylistRoute
import com.samv.three.ui.screens.localplaylist.LocalPlaylistScreen
import com.samv.three.ui.screens.playlistRoute
import com.samv.three.ui.screens.search.SearchScreen
import com.samv.three.ui.screens.searchResultRoute
import com.samv.three.ui.screens.searchRoute
import com.samv.three.ui.screens.searchresult.SearchResultScreen
import com.samv.three.ui.screens.settings.SettingsScreen
import com.samv.three.ui.screens.settingsRoute
import com.samv.three.utils.homeScreenTabIndexKey
import com.samv.three.utils.pauseSearchHistoryKey
import com.samv.three.utils.preferences
import com.samv.three.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen(onPlaylistUrl: (String) -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        com.samv.three.query {
                            com.samv.three.Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {
            val (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

            Scaffold(
                topIconButtonId = R.drawable.equalizer,
                onTopIconButtonClick = { settingsRoute() },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Quick picks", R.drawable.sparkles)
                    Item(1, "Songs", R.drawable.musical_notes)
                    Item(2, "Playlists", R.drawable.playlist)
                    Item(3, "Artists", R.drawable.person)
                    Item(4, "Albums", R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                    }
                }
            }
        }
    }
}
