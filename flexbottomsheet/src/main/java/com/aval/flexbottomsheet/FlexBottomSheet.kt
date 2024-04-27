package com.aval.flexbottomsheet

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
@ExperimentalMaterial3Api
fun FlexModalBottomSheet(
    isVisible: Boolean,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    bottomPeek: Dp,
    partPeek: Dp,
    sheetState: FlexSheetState = rememberFlexBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable (height: Dp,progress: Float,isPart:Boolean) -> Unit,
) {
    val density = LocalDensity.current
    SideEffect {
        sheetState.density = density
    }
    val scope = rememberCoroutineScope()
    val animateToDismiss: () -> Unit = {

    }
    val settleToDismiss: (velocity: Float) -> Unit = {
        scope.launch { sheetState.settle(it) }
    }
    val nestedScroll = remember(sheetState) {
        consumeSwipeWithinFlexSheetBoundsNestedScrollConnection(
            sheetState = sheetState,
            orientation = Orientation.Vertical,
            onFling = settleToDismiss
        )
    }
    AnimatedVisibility (
        visible = isVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }

    ) {

        BoxWithConstraints(
            modifier,
        ) {
            val fullHeight = constraints.maxHeight
            val fullHeightDp = with(density) { fullHeight.toDp() }
            val fullWidthDp = with(density) { constraints.maxWidth.toDp() }
            Surface(
                modifier = modifier
                    .padding(paddingValues)
                    .widthIn(max = sheetMaxWidth)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset {
                        IntOffset(
                            0,
                            sheetState
                                .requireOffset()
                                .toInt()
                        )
                    }
                    .nestedScroll(nestedScroll)
                    .anchoredDraggable(
                        state = sheetState.anchoredDraggableState,
                        orientation = Orientation.Vertical,
                        enabled = true,
                        startDragImmediately = sheetState.anchoredDraggableState.isAnimationRunning,
                    )
                    .flexBottomSheetAnchors(
                        bottomPeek = bottomPeek,
                        sheetState = sheetState,
                        fullHeight = fullHeightDp,
                        partPeek = partPeek,
                        density = density
                    ),
                shape = shape,
                color = containerColor,
                contentColor = contentColor,
                tonalElevation = tonalElevation,
            ) {
                Column(
                    Modifier
                        .height(fullHeightDp)
                        .fillMaxWidth()
                ) {
                    if (dragHandle != null) {
                        val collapseActionLabel =
                            "getString(BiometricManager.Strings.BottomSheetPartialExpandDescription)"
                        val dismissActionLabel = "getString(Strings.BottomSheetDismissDescription)"
                        val expandActionLabel = "getString(Strings.BottomSheetExpandDescription)"
                        Box(
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .semantics(mergeDescendants = true) {
                                    with(sheetState) {
                                        // Provides semantics to interact with the bottomsheet if there is more
                                        // than one anchor to swipe to and swiping is enabled.
                                        if (anchoredDraggableState.anchors.size > 1) {
                                            if (currentValue == FlexSheetValue.PART) {
                                                if (anchoredDraggableState.targetValue == FlexSheetValue.FULL) {
                                                    expand(expandActionLabel) {
                                                        scope.launch { expand() }; true
                                                    }
                                                }
                                            } else {
                                                if (anchoredDraggableState.targetValue == FlexSheetValue.PART
                                                ) {
                                                    collapse("partialExpandActionLabel") {
                                                        scope.launch { partExpand() }; true
                                                    }
                                                }
                                            }
                                            dismiss(dismissActionLabel) {
                                                scope.launch { bottomExpand() }
                                                true
                                            }
                                        }
                                    }
                                },
                        ) {
                            dragHandle()
                        }
                    }
                    val offsetDp = with(density) { (sheetState.offset ?: 0f).toDp() }
                    val offsetZero = fullHeightDp.value - offsetDp.value - bottomPeek.value
                    val secondZero = fullHeightDp.value - offsetDp.value - partPeek.value
                    val second = secondZero / (fullHeightDp.value - partPeek.value)
                    val first = offsetZero / (partPeek.value - bottomPeek.value)
                    val res = if (first <= 1) {
                        first
                    } else {
                        second
                    }
                    content(fullHeightDp, res, first <= 1)
                }
            }

        }
    }

    LaunchedEffect(sheetState) {
        sheetState.show(sheetState.targetValue)
    }
}