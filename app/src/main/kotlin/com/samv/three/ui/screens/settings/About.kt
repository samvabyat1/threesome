package com.samv.three.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.samv.three.BuildConfig
import com.samv.three.LocalPlayerAwareWindowInsets
import com.samv.three.ui.components.themed.Header
import com.samv.three.ui.styling.LocalAppearance
import com.samv.three.utils.secondary

@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = "About") {
            BasicText(
                text = "v${BuildConfig.VERSION_NAME} by samv",
                style = typography.s.secondary
            )
        }

        SettingsEntryGroupText(title = "SOCIAL")

        SettingsEntry(
            title = "GitHub",
            text = "View the source code",
            onClick = {
                uriHandler.openUri("https://github.com/samvabyat1/threesome")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "TROUBLESHOOTING")

        SettingsEntry(
            title = "Report an issue",
            text = "You will be redirected to GitHub",
            onClick = {
//                uriHandler.openUri("https://github.com/samvabyat1/threesome/issues/new?assignees=&labels=bug&template=bug_report.yaml")
            }
        )

        SettingsEntry(
            title = "Request a feature or suggest an idea",
            text = "You will be redirected to GitHub",
            onClick = {
//                uriHandler.openUri("https://github.com/vfsfitvnm/ViMusic/issues/new?assignees=&labels=enhancement&template=feature_request.yaml")
            }
        )
    }
}
