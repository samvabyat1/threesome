package com.samv.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ThumbnailRenderer(
    @JsonNames("croppedSquareThumbnailRenderer")
    val musicThumbnailRenderer: MusicThumbnailRenderer?
) {
    @Serializable
    data class MusicThumbnailRenderer(
        val thumbnail: Thumbnail?
    ) {
        @Serializable
        data class Thumbnail(
            val thumbnails: List<com.samv.innertube.models.Thumbnail>?
        )
    }
}
