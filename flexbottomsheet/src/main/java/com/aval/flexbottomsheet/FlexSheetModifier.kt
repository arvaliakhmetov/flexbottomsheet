package com.aval.flexbottomsheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun Modifier.flexBottomSheetAnchors(
    density: Density,
    bottomPeek: Dp,
    partPeek: Dp,
    sheetState: FlexSheetState,
    fullHeight: Dp
) = onSizeChanged { sheetSize ->
    val newAnchors = androidx.compose.foundation.gestures.DraggableAnchors {
        FlexSheetValue.BOTTOM at with(density){fullHeight.toPx() - bottomPeek.toPx()}
        FlexSheetValue.PART at with(density){fullHeight.toPx() - partPeek.toPx()}
        FlexSheetValue.FULL at 0f
    }
    val newTarget = when (sheetState.anchoredDraggableState.targetValue) {
        FlexSheetValue.BOTTOM -> {
            val hasPartiallyExpandedState = newAnchors.hasAnchorFor(FlexSheetValue.PART)
            val newTarget = if(hasPartiallyExpandedState) FlexSheetValue.BOTTOM else {
                FlexSheetValue.PART
            }
            newTarget
        }
        FlexSheetValue.PART -> {
            FlexSheetValue.PART
        }
        FlexSheetValue.FULL -> {
            FlexSheetValue.FULL
        }
    }

    sheetState.anchoredDraggableState.updateAnchors(newAnchors, newTarget)
}