package com.samv.three.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.samv.compose.persist.persist
import com.samv.three.LocalPlayerAwareWindowInsets
import com.samv.three.R
import com.samv.three.enums.AlbumSortBy
import com.samv.three.enums.SortOrder
import com.samv.three.models.Album
import com.samv.three.ui.components.themed.FloatingActionsContainerWithScrollToTop
import com.samv.three.ui.components.themed.Header
import com.samv.three.ui.components.themed.HeaderIconButton
import com.samv.three.ui.items.AlbumItem
import com.samv.three.ui.styling.Dimensions
import com.samv.three.ui.styling.LocalAppearance
import com.samv.three.ui.styling.px
import com.samv.three.utils.albumSortByKey
import com.samv.three.utils.albumSortOrderKey
import com.samv.three.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeAlbums(
    onAlbumClick: (Album) -> Unit,
    onSearchClick: () -> Unit,
) {
    val (colorPalette) = LocalAppearance.current

    var sortBy by rememberPreference(albumSortByKey, AlbumSortBy.DateAdded)
    var sortOrder by rememberPreference(albumSortOrderKey, SortOrder.Descending)

    var items by persist<List<Album>>(tag = "home/albums", emptyList())

    LaunchedEffect(sortBy, sortOrder) {
        com.samv.three.Database.albums(sortBy, sortOrder).collect { items = it }
    }

    val thumbnailSizeDp = Dimensions.thumbnails.song * 2
    val thumbnailSizePx = thumbnailSizeDp.px

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val lazyListState = rememberLazyListState()

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(title = "Albums") {
                    HeaderIconButton(
                        icon = R.drawable.calendar,
                        color = if (sortBy == AlbumSortBy.Year) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = AlbumSortBy.Year }
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        color = if (sortBy == AlbumSortBy.Title) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = AlbumSortBy.Title }
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        color = if (sortBy == AlbumSortBy.DateAdded) colorPalette.text else colorPalette.textDisabled,
                        onClick = { sortBy = AlbumSortBy.DateAdded }
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

            items(
                items = items,
                key = Album::id
            ) { album ->
                AlbumItem(
                    album = album,
                    thumbnailSizePx = thumbnailSizePx,
                    thumbnailSizeDp = thumbnailSizeDp,
                    modifier = Modifier
                        .clickable(onClick = { onAlbumClick(album) })
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
