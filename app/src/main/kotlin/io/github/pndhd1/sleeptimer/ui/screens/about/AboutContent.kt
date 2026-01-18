package io.github.pndhd1.sleeptimer.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import io.github.pndhd1.sleeptimer.R
import io.github.pndhd1.sleeptimer.ui.theme.SleepTimerTheme
import io.github.pndhd1.sleeptimer.utils.plus
import io.github.pndhd1.sleeptimer.utils.ui.UIDefaults
import io.github.pndhd1.sleeptimer.utils.ui.UIDefaults.SystemBarsBackgroundColor
import io.github.pndhd1.sleeptimer.utils.ui.VisibilityCrossfade
import kotlinx.coroutines.launch

private const val LicensesScrollOffset = 800f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutContent(
    component: AboutComponent,
    modifier: Modifier = Modifier,
) {
    val state = component.state
    val libraries by produceLibraries(R.raw.aboutlibraries)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = component::onBackClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.about_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LibrariesContainer(
                libraries = libraries,
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxSize(),
                lazyListState = listState,
                contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                padding = LibraryDefaults.libraryPadding(
                    contentPadding = UIDefaults.defaultInsets
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                        .plus(PaddingValues(vertical = 16.dp)),
                ),
                header = {
                    item {
                        AppInfoSection(
                            appVersion = state.appVersion,
                            onGithubClick = {
                                try {
                                    uriHandler.openUri(state.githubUrl)
                                } catch (_: Exception) {
                                    // Ignore
                                }
                            },
                        )
                        HorizontalDivider()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        listState.animateScrollBy(LicensesScrollOffset)
                                    }
                                }
                                .padding(vertical = 16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.about_licenses_title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.windowInsetsPadding(
                                    UIDefaults.defaultInsets.only(WindowInsetsSides.Horizontal)
                                )
                            )
                        }
                    }
                },
            )

            VisibilityCrossfade(
                isVisible = listState.canScrollForward,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .background(SystemBarsBackgroundColor),
                )
            }
        }
    }
}

@Composable
private fun AppInfoSection(
    appVersion: String,
    onGithubClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = stringResource(R.string.about_version, appVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .clickable(onClick = onGithubClick)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_open_in_new),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.about_github),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutContentPreview() {
    SleepTimerTheme {
        AboutContent(component = PreviewAboutComponent)
    }
}
