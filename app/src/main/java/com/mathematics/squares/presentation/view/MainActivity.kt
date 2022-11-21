package com.mathematics.squares.presentation.view

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.mathematics.squares.R
import com.mathematics.squares.SquaresApplication
import com.mathematics.squares.data.repository.SquaresRepository
import com.mathematics.squares.presentation.model.Cords
import com.mathematics.squares.presentation.model.Square
import com.mathematics.squares.presentation.view.theme.*
import com.mathematics.squares.presentation.viewmodel.SettingsViewModel
import com.mathematics.squares.presentation.viewmodel.SquaresViewModel
import kotlinx.coroutines.launch

private const val squareSize = 12

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalStdlibApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsViewModel = SettingsViewModel.getInstance(
            this,
            (application as SquaresApplication).viewModelFactory
        )
        val squaresViewModel = SquaresViewModel.getInstance(
            this,
            (application as SquaresApplication).viewModelFactory
        )

        setContent {
            var statusBarColor: Color? by remember { mutableStateOf(null) }

            SquaresTheme(settingsViewModel.settingsLiveData, statusBarColor?.animate()) { theme ->
                MainScreen(
                    squaresViewModel,
                    theme,
                    updateStatusBarColor = { statusBarColor = it },
                    settingsViewModel::updateTheme
                )
            }
        }
    }
}


@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
private fun MainScreen(
    squaresViewModel: SquaresViewModel,
    theme: Themes,
    updateStatusBarColor: (Color) -> Unit = {},
    updateTheme: (Themes) -> Unit,
) {
    val squaresMap by squaresViewModel.squaresMap.observeAsState(initial = mapOf())
    val scope = rememberCoroutineScope()
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    val scrollState = rememberScrollState()
    var calculating by rememberSaveable { mutableStateOf(false) }

    val orientation = LocalConfiguration.current.orientation

    val showSnackbar = { text: String ->
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    val squareCountItems = rememberSaveable {
        mutableMapOf("5x5" to 0, "4x4" to 0, "3x3" to 0, "2x2" to 0)
    }
    var recalculateSquares by rememberSaveable { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { updateTheme(theme.switch()) }) {
                        Icon(imageVector = theme.icon, contentDescription = "switchMode")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    scrolledContainerColor = MaterialTheme.colorScheme.background.animate()
                ),
                modifier = Modifier
                    .alpha(
                        (1 - scrollState.value / scrollState.maxValue.toFloat())
                            .animate()
                            .also {
                                updateStatusBarColor(
                                    if (it == 0f)
                                        MaterialTheme.colorScheme.background
                                    else
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = if (it.isNaN()) 1f else it
                                        )
                                )
                            },
                    )
                    .fillMaxWidth()
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    shape = RoundedCornerShape(cornerSize)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background.animate(),
        contentColor = MaterialTheme.colorScheme.onBackground.animate(),
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        val paddingValues = if (
            scrollState.value >= scrollState.maxValue / 8 &&
            orientation == Configuration.ORIENTATION_LANDSCAPE
        ) PaddingValues(0.dp)
        else contentPadding

        Box(
            modifier = Modifier
                .padding(paddingValues.animate(Spring.StiffnessVeryLow))
                .background(Color.Transparent)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                SquareField(squaresMap = squaresMap, padding = 16.dp)

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        squareCountItems.keys.sortedDescending().forEach { key ->
                            SquaresCountField(hint = key) {
                                recalculateSquares = true
                                squareCountItems[key] = it
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                calculating = true
                                val result = squaresViewModel.updateSquaresMap(
                                    squareSize,
                                    squareCountItems.values.toIntArray(),
                                    recalculate = recalculateSquares,
                                )
                                recalculateSquares = !result
                                calculating = false

                                if (!result) {
                                    showSnackbar("Не получилось расставить квадраты \\(o_o)/")
                                    squaresViewModel.updateSquaresMap(mapOf())
                                }
                            }
                        },
                        enabled = !calculating,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.animate(),
                            contentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                            disabledContentColor = MaterialTheme.colorScheme.primary
                                .copy(alpha = 0.4f).animate()
                        ),
                        border = BorderStroke(
                            width = 0.7.dp,
                            color = MaterialTheme.colorScheme.primary
                                .copy(alpha = if (calculating) 0.4f else 1f),
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Сгенерировать новую комбинацию",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }

            Loading(calculating)

            Version()
        }
    }
}


@ExperimentalMaterial3Api
@Composable
private fun RowScope.SquaresCountField(
    hint: String,
    onTextChange: (Int) -> Unit,
) {
    var rememberedText by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = rememberedText,
        onValueChange = {
            rememberedText = it
            onTextChange(it.toIntOrNull() ?: 0)
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = "0",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingText = { Text(text = hint, style = MaterialTheme.typography.bodySmall) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.colorScheme.onBackground.animate(),
            containerColor = MaterialTheme.colorScheme.primaryContainer.animate(),
            placeholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f).animate(),
            focusedBorderColor = MaterialTheme.colorScheme.primary.animate(),
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.animate(),
            focusedSupportingTextColor = MaterialTheme.colorScheme.primary.animate()
        ),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .weight(1f)
    )
}




@Composable
internal fun SquareField(squaresMap: Map<Cords, Square>, padding: Dp) {
    val configuration = LocalConfiguration.current
    val minLengthDp = min(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
    val squareSizeDp = minLengthDp - padding * 2
    val cellSize = squareSizeDp / squareSize

    fun getShape(x: Int, y: Int, size: Int) = when {
        x == 0 && y == 0 -> RoundedCornerShape(topStart = cornerSize)
        x == 0 && y == squareSize - size -> RoundedCornerShape(topEnd = cornerSize)
        x == squareSize - size && y == 0 -> RoundedCornerShape(bottomStart = cornerSize)
        x == squareSize - size && y == squareSize - size ->
            RoundedCornerShape(bottomEnd = cornerSize)
        else -> RectangleShape
    }

    fun getShape(cords: Cords, size: Int) = getShape(cords.x, cords.y, size)

    ConstraintLayout(modifier = Modifier.padding(padding)) {
        val field = createRef()

        EmptyCellsField(
            cellSize = cellSize,
            getShape = ::getShape,
            modifier = Modifier.constrainAs(field) {
                top.linkTo(parent.top)
            },
        )

        squaresMap.forEach { (cords, square) ->
            Square(
                topLeftCords = cords,
                cellSize = cellSize,
                square = square,
                shape = getShape(cords, size = square.sideLength)
            )
        }
    }
}




@Composable
private fun EmptyCellsField(
    cellSize: Dp,
    modifier: Modifier = Modifier,
    getShape: (i: Int, j: Int, size: Int) -> Shape,
) {
    Box(
        modifier = modifier
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.onBackground.animate(),
                shape = RoundedCornerShape(cornerSize)
            )
            .background(
                color = MaterialTheme.colorScheme.background.animate(),
                shape = RoundedCornerShape(cornerSize)
            )
    ) {
        Column {
            repeat(squareSize) { i ->
                Row {
                    repeat(squareSize) { j ->
                        Cell(
                            size = cellSize,
                            backgroundColor = MaterialTheme.colorScheme.background.animate(),
                            border = BorderStroke(
                                width = borderWidth / 2,
                                color = MaterialTheme.colorScheme.primaryContainer.animate()
                            ),
                            shape = getShape(i, j, 1)
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun Cell(size: Dp, backgroundColor: Color, border: BorderStroke, shape: Shape) {
    Box(
        modifier = Modifier
            .border(border, shape)
            .background(backgroundColor, shape)
            .size(size)
    )
}



@Composable
private fun ConstraintLayoutScope.Box(
    topLeftCords: Cords,
    cellSize: Dp,
    sideLength: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val ref = createRef()
    Box(
        modifier = modifier
            .size(cellSize * sideLength)
            .constrainAs(ref) {
                top.linkTo(parent.top, margin = cellSize * topLeftCords.x + borderWidth / 2)
                start.linkTo(parent.start, margin = cellSize * topLeftCords.y + borderWidth / 2)
            },
        content = content
    )
}



@Composable
private fun ConstraintLayoutScope.Square(
    topLeftCords: Cords,
    cellSize: Dp,
    square: Square,
    shape: Shape,
) {
    Box(
        topLeftCords = topLeftCords,
        cellSize = cellSize,
        sideLength = square.sideLength,
        modifier = Modifier
            .background(square.color.animate(), shape)
            .border(
                width = borderWidth / 2,
                color = MaterialTheme.colorScheme.onBackground
                    .copy(alpha = 0.7f)
                    .animate(),
                shape = shape
            )
    )
}


@Composable
private fun BoxScope.Version() {
    Text(
        text = stringResource(R.string.app_version).lowercase(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.animate(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
    )
}



@ExperimentalAnimationApi
@Composable
private fun Loading(isLoading: Boolean) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessVeryLow)),
    ) {
        Box(
            modifier = Modifier
                .animateEnterExit(enter = fadeIn(), exit = fadeOut())
                .background(MaterialTheme.colorScheme.background.animate())
                .fillMaxSize(),
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary.animate(),
                strokeWidth = 3.4.dp,
                modifier = Modifier
                    .animateEnterExit(
                        enter = slideInVertically(spring(stiffness = Spring.StiffnessLow)),
                        exit = slideOutVertically(spring(stiffness = Spring.StiffnessLow))
                    )
                    .scale(1.8f)
                    .align(Alignment.Center)
            )
        }
    }
}






@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    SquaresTheme {
        var theme by remember { mutableStateOf(it) }
        MainScreen(
            squaresViewModel = SquaresViewModel(SquaresRepository()),
            theme = theme,
            updateTheme = { theme = it }
        )
    }
}


@Preview(showSystemUi = true, backgroundColor = 0xFFFFFF)
@Composable
private fun SquarePreview() {
    var squaresMap by remember {
        mutableStateOf(mapOf(
            Cords(0, 8) to Square(sideLength = 4, color = Color.Red)
        ))
    }

    val squaresViewModel = SquaresViewModel(SquaresRepository())
    val scope = rememberCoroutineScope()

    SquaresTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SquareField(squaresMap = squaresMap, padding = 16.dp)
            OutlinedButton(
                onClick = {
                    scope.launch {
                        squaresViewModel
                            .calculateSquaresPositions(12, x4 = 4, x3 = 2)
                            ?.let { squaresMap = it }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Сгенерировать новую комбинацию",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}