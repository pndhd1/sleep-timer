package io.github.pndhd1.sleeptimer.ui.screen.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.pndhd1.sleeptimer.utils.isPortrait
import io.github.pndhd1.sleeptimer.utils.ui.InsetsBackground
import io.github.pndhd1.sleeptimer.utils.ui.LocalNavigationMode
import io.github.pndhd1.sleeptimer.utils.ui.NavigationMode
import io.github.pndhd1.sleeptimer.utils.ui.systemBarsForVisualComponents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (!isPortrait() && LocalNavigationMode.current == NavigationMode.Buttons) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            Color.Unspecified
                        }
                    ),
                    scrollBehavior = scrollBehavior,
                )
            },
            // Insets are handled by the content
            contentWindowInsets = WindowInsets(0),
        ) { innerPadding ->
            val contentInsets = WindowInsets.systemBarsForVisualComponents
                .only(WindowInsetsSides.Horizontal)
                .add(WindowInsets(left = 20.dp, right = 20.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                LibrariesContainer(
                    libraries = libraries,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    lazyListState = listState,
                    contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                        .add(WindowInsets(bottom = 12.dp))
                        .asPaddingValues(),
                    padding = LibraryDefaults.libraryPadding(
                        contentPadding = contentInsets
                            .add(WindowInsets(top = 12.dp, bottom = 12.dp))
                            .asPaddingValues()
                    ),
                    header = {
                        item {
                            Column {
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
                                            scope.launch { listState.animateScrollToItem(1) }
                                        }
                                        .windowInsetsPadding(contentInsets)
                                        .padding(vertical = 12.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.about_licenses_title),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }

        // there is a little delay before canScrollForward is correctly set
        // so we delay collecting it to avoid flickering
        var bottomInsetVisible by remember {
            mutableStateOf(true)
        }
        LaunchedEffect(listState) {
            delay(200)
            snapshotFlow { listState.canScrollForward }.collect {
                bottomInsetVisible = it
            }
        }

        InsetsBackground(
            visible = bottomInsetVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars),
        )
        InsetsBackground(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .windowInsetsStartWidth(WindowInsets.navigationBars)
        )
        InsetsBackground(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .windowInsetsEndWidth(WindowInsets.navigationBars)
        )
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
