package com.samv.three.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samv.compose.persist.persistList
import com.samv.three.LocalPlayerAwareWindowInsets
import com.samv.three.LocalPlayerServiceBinder
import com.samv.three.R
import com.samv.three.enums.SongSortBy
import com.samv.three.enums.SortOrder
import com.samv.three.models.Song
import com.samv.three.ui.components.LocalMenuState
import com.samv.three.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.samv.three.ui.components.themed.Header
import com.samv.three.ui.components.themed.HeaderIconButton
import com.samv.three.ui.components.themed.InHistoryMediaItemMenu
import com.samv.three.ui.items.SongItem
import com.samv.three.ui.styling.Dimensions
import com.samv.three.ui.styling.LocalAppearance
import com.samv.three.ui.styling.onOverlay
import com.samv.three.ui.styling.overlay
import com.samv.three.ui.styling.px
import com.samv.three.utils.asMediaItem
import com.samv.three.utils.center
import com.samv.three.utils.color
import com.samv.three.utils.forcePlayAtIndex
import com.samv.three.utils.rememberPreference
import com.samv.three.utils.semiBold
import com.samv.three.utils.songSortByKey
import com.samv.three.utils.songSortOrderKey

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeSongs(
    onSearchClick: () -> Unit
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current

    val thumbnailSizeDp = Dimensions.thumbnails.song
    val thumbnailSizePx = thumbnailSizeDp.px

    var sortBy by rememberPreference(songSortByKey, SongSortBy.DateAdded)
    var sortOrder by rememberPreference(songSortOrderKey, SortOrder.Descending)

    var items by persistList<Song>("home/songs")

    LaunchedEffect(sortBy, sortOrder) {
        com.samv.three.Database.songs(sortBy, sortOrder).collect { items = it }
    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val lazyListState = rememberLazyListState()

    Box(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(title = "Songs") {
                    HeaderIconButton(
                        icon = R.drawable.trending,
                        color = if (sortBy == SongSortBy.PlayTime) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = SongSortBy.PlayTime }
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        color = if (sortBy == SongSortBy.Title) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = SongSortBy.Title }
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        color = if (sortBy == SongSortBy.DateAdded) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = SongSortBy.DateAdded }
                    )

                    Spacer(
                        modifier = Modifier
                            .width(2.dp)
                    )

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { sortOrder = !sortOrder },
                        modifier = Modifier
                            .graphicsLayer { rotationZ = sortOrderIconRotation }
                    )
                }
            }

            itemsIndexed(
                items = items,
                key = { _, song -> song.id }
            ) { index, song ->
                SongItem(
                    song = song,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    onThumbnailContent = if (sortBy == SongSortBy.PlayTime) ({
                        BasicText(
                            text = song.formattedTotalPlayTime,
                            style = typography.xxs.semiBold.center.color(colorPalette.onOverlay),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, colorPalette.overlay)
                                    ),
                                    shape = thumbnailShape
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }) else null,
                    modifier = Modifier
                        .combinedClickable(
                            onLongClick = {
                                menuState.display {
                                    InHistoryMediaItemMenu(
                                        song = song,
                                        onDismiss = menuState::hide
                                    )
                                }
                            },
                            onClick = {
                                binder?.stopRadio()
                                binder?.player?.forcePlayAtIndex(
                                    items.map(Song::asMediaItem),
                                    index
                                )
                            }
                        )
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )
    }
}
