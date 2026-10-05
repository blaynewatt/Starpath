package net.syserr.starpathtracker.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import net.syserr.starpathtracker.R
import net.syserr.starpathtracker.data.StarPathRepository
import net.syserr.starpathtracker.data.model.StarPathDuty
import net.syserr.starpathtracker.data.model.StarPathEntry
import net.syserr.starpathtracker.data.model.StarPathList
import net.syserr.starpathtracker.data.scraper.StarPathScraper
import net.syserr.starpathtracker.ui.components.BannerAdView
import net.syserr.starpathtracker.theme.CelestialBackground
import net.syserr.starpathtracker.theme.CelestialCard
import net.syserr.starpathtracker.theme.CelestialCardBorder
import net.syserr.starpathtracker.theme.CelestialCardSelected
import net.syserr.starpathtracker.theme.CelestialSurface
import net.syserr.starpathtracker.theme.CompletionGreen
import net.syserr.starpathtracker.theme.DreamlightCyan
import net.syserr.starpathtracker.theme.DreamlightPurple
import net.syserr.starpathtracker.theme.StarlightGold
import net.syserr.starpathtracker.theme.StarlightGoldDark
import net.syserr.starpathtracker.theme.TextMuted
import net.syserr.starpathtracker.theme.TextPrimary
import net.syserr.starpathtracker.theme.TextSecondary

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel {
        val context = checkNotNull(this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        MainScreenViewModel(StarPathRepository(context))
    }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CelestialBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StarPathTopBar(
                isScraping = state.isScraping,
                hideCompleted = state.hideCompleted,
                onToggleHideCompleted = { viewModel.toggleHideCompleted() },
                onRefreshClick = { viewModel.refreshFromWeb() },
                onResetClick = { viewModel.openResetDialog() },
                onMarkAllClick = { viewModel.markAllDuties() }
            )
        },
        bottomBar = {
            BannerAdView()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Star Path Dropdown Selector
            StarPathDropdownSelector(
                selectedTitle = state.starPathList?.title ?: "Select Star Path",
                availablePaths = state.availableStarPaths,
                currentUrl = state.starPathList?.sourceUrl ?: "",
                isExpanded = state.isDropdownExpanded,
                onExpandedChange = { viewModel.setDropdownExpanded(it) },
                onSelectPath = { viewModel.selectStarPath(it.url) }
            )

            if (state.isLoading && state.starPathList == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = StarlightGold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading Star Path Duties...",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                state.starPathList?.let { starPath ->
                    // Summary Stats Card
                    StarPathStatsCard(starPath = starPath)

                    // Tab Control (Path vs Weekly)
                    StarPathTabControl(
                        selectedTab = state.selectedTab,
                        pathCount = state.pathCount,
                        weeklyCount = state.weeklyCount,
                        onTabSelected = { viewModel.selectTab(it) }
                    )

                    // Filter Row: Search, Hide Completed toggle, Sort
                    StarPathSearchBarAndFilters(
                        searchQuery = state.searchQuery,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        hideCompleted = state.hideCompleted,
                        onToggleHideCompleted = viewModel::toggleHideCompleted,
                        sortOrder = state.sortOrder,
                        onSortOrderChanged = viewModel::onSortOrderChanged,
                        filteredCount = state.filteredDuties.size,
                        totalTabCount = if (state.selectedTab == DutyTab.PATH) state.pathCount else state.weeklyCount
                    )

                    // Duties List
                    if (state.filteredDuties.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (state.hideCompleted) Icons.Default.VisibilityOff else Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (state.hideCompleted) "All duties in this tab are completed!" else "No duties found in this tab",
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (state.hideCompleted) "Tap the eye icon to view completed items" else "Check the other tab or adjust your search",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.filteredDuties,
                                key = { it.id }
                            ) { duty ->
                                StarPathDutyCard(
                                    duty = duty,
                                    onToggle = { viewModel.toggleDuty(duty.id) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (state.isResetConfirmDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeResetDialog() },
            containerColor = CelestialCard,
            icon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = StarlightGold,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Reset All Duties?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will uncheck all completed duties for this Star Path. Your progress will be reset.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetAllDuties() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Progress", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeResetDialog() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarPathTopBar(
    isScraping: Boolean,
    hideCompleted: Boolean,
    onToggleHideCompleted: () -> Unit,
    onRefreshClick: () -> Unit,
    onResetClick: () -> Unit,
    onMarkAllClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CelestialSurface,
            titleContentColor = TextPrimary,
            actionIconContentColor = StarlightGold
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_dreamlight_clipboard),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Star Path Tracker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Disney Dreamlight Valley",
                        fontSize = 11.sp,
                        color = DreamlightCyan
                    )
                }
            }
        },
        actions = {
            // Show / Hide completed toggle icon
            IconButton(onClick = onToggleHideCompleted) {
                Icon(
                    imageVector = if (hideCompleted) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (hideCompleted) "Show Completed Items" else "Hide Completed Items",
                    tint = if (hideCompleted) CompletionGreen else TextSecondary
                )
            }

            // Re-scrape / Refresh button
            IconButton(onClick = onRefreshClick, enabled = !isScraping) {
                if (isScraping) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = StarlightGold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh from Web",
                        tint = StarlightGold
                    )
                }
            }

            // More Options Dropdown
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = TextSecondary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(CelestialCard)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (hideCompleted) "Show Completed Duties" else "Hide Completed Duties",
                                color = TextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (hideCompleted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = DreamlightCyan
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleHideCompleted()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Mark All Complete", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.DoneAll, contentDescription = null, tint = CompletionGreen) },
                        onClick = {
                            showMenu = false
                            onMarkAllClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset All Checks", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onResetClick()
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun StarPathDropdownSelector(
    selectedTitle: String,
    availablePaths: List<StarPathEntry>,
    currentUrl: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectPath: (StarPathEntry) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CelestialSurface,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(CelestialCardBorder, StarlightGold.copy(alpha = 0.5f)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Star Path",
                        fontSize = 11.sp,
                        color = StarlightGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = StarPathScraper.cleanPathTitle(selectedTitle),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Star Path",
                    tint = StarlightGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(CelestialCard)
        ) {
            availablePaths.forEach { entry ->
                val isSelected = entry.url.equals(currentUrl, ignoreCase = true)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = StarPathScraper.cleanPathTitle(entry.title),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StarlightGold else TextPrimary
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = StarlightGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    onClick = { onSelectPath(entry) }
                )
            }
        }
    }
}

@Composable
fun StarPathStatsCard(starPath: StarPathList) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CelestialCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CelestialCardBorder, DreamlightPurple.copy(alpha = 0.5f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${starPath.completedDuties} of ${starPath.totalDuties} Duties Completed",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                // Tokens Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StarlightGold.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(StarlightGold, StarlightGoldDark))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarlightGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${starPath.earnedTokens} / ${starPath.totalTokens} Tokens",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = StarlightGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val animatedProgress by animateFloatAsState(
                targetValue = starPath.progressFraction,
                animationSpec = tween(durationMillis = 500),
                label = "progress_bar"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StarlightGold,
                trackColor = CelestialBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${starPath.progressPercentage}% Done",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (starPath.progressPercentage == 100) CompletionGreen else StarlightGold
                )
            }
        }
    }
}

@Composable
fun StarPathTabControl(
    selectedTab: DutyTab,
    pathCount: Int,
    weeklyCount: Int,
    onTabSelected: (DutyTab) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = CelestialBackground,
        contentColor = StarlightGold,
        divider = {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CelestialCardBorder)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Tab(
            selected = selectedTab == DutyTab.PATH,
            onClick = { onTabSelected(DutyTab.PATH) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Path",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == DutyTab.PATH) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == DutyTab.PATH) StarlightGold else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == DutyTab.PATH) StarlightGold.copy(alpha = 0.2f) else CelestialCard
                    ) {
                        Text(
                            text = "$pathCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == DutyTab.PATH) StarlightGold else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        )

        Tab(
            selected = selectedTab == DutyTab.WEEKLY,
            onClick = { onTabSelected(DutyTab.WEEKLY) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Weekly",
                        fontSize = 15.sp,
                        fontWeight = if (selectedTab == DutyTab.WEEKLY) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == DutyTab.WEEKLY) StarlightGold else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == DutyTab.WEEKLY) StarlightGold.copy(alpha = 0.2f) else CelestialCard
                    ) {
                        Text(
                            text = "$weeklyCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == DutyTab.WEEKLY) StarlightGold else TextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun StarPathSearchBarAndFilters(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    hideCompleted: Boolean,
    onToggleHideCompleted: () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    filteredCount: Int,
    totalTabCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Input
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = CelestialSurface,
                border = BorderStroke(1.dp, if (isFocused) StarlightGold else CelestialCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (isFocused) StarlightGold else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search duties...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            interactionSource = interactionSource,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(StarlightGold)
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChanged("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Hide/Show Completed Toggle Button Chip
            FilterChip(
                selected = hideCompleted,
                onClick = onToggleHideCompleted,
                modifier = Modifier.height(44.dp),
                label = { Text("Hide Done", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = if (hideCompleted) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CompletionGreen.copy(alpha = 0.2f),
                    selectedLabelColor = CompletionGreen,
                    selectedLeadingIconColor = CompletionGreen,
                    containerColor = CelestialSurface,
                    labelColor = TextSecondary,
                    iconColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = hideCompleted,
                    selectedBorderColor = CompletionGreen,
                    borderColor = CelestialCardBorder
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing $filteredCount of $totalTabCount duties",
                fontSize = 12.sp,
                color = TextMuted
            )

            // Sort Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val next = when (sortOrder) {
                            SortOrder.DEFAULT -> SortOrder.REWARD_HIGH_TO_LOW
                            SortOrder.REWARD_HIGH_TO_LOW -> SortOrder.TITLE
                            SortOrder.TITLE -> SortOrder.DEFAULT
                        }
                        onSortOrderChanged(next)
                    }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sortOrder.label,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun StarPathDutyCard(
    duty: StarPathDuty,
    onToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val cardBg by animateColorAsState(
        targetValue = if (duty.isCompleted) CelestialCardSelected else CelestialCard,
        label = "card_bg"
    )

    val borderStrokeColor by animateColorAsState(
        targetValue = if (duty.isCompleted) CompletionGreen.copy(alpha = 0.4f) else CelestialCardBorder,
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(borderStrokeColor, borderStrokeColor))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Checkbox
                Checkbox(
                    checked = duty.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CompletionGreen,
                        checkmarkColor = Color.White,
                        uncheckedColor = TextMuted
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Duty Title & Section
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = duty.title,
                        fontWeight = if (duty.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (duty.isCompleted) TextMuted else TextPrimary,
                        textDecoration = if (duty.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = duty.section,
                            fontSize = 11.sp,
                            color = DreamlightPurple,
                            fontWeight = FontWeight.Medium
                        )
                        if (duty.requirement.isNotEmpty()) {
                            Text(
                                text = " • ${duty.requirement}",
                                fontSize = 11.sp,
                                color = DreamlightCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Token Reward Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (duty.isCompleted) CompletionGreen.copy(alpha = 0.2f) else StarlightGold.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (duty.isCompleted) CompletionGreen else StarlightGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+${duty.tokenReward}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (duty.isCompleted) CompletionGreen else StarlightGold
                        )
                    }
                }

                // Expand icon
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Details",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expandable "How to Complete" Hint
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 8.dp, end = 8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CelestialSurface,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(CelestialCardBorder, CelestialCardBorder))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = StarlightGold,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "How to Complete:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarlightGold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (duty.howToComplete.isNotBlank()) duty.howToComplete else "Follow the in-game task requirement to finish this duty.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                                if (duty.requirement.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Requirement: ${duty.requirement}",
                                        fontSize = 12.sp,
                                        color = DreamlightCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
