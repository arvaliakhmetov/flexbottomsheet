package com.aval.flexbottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFlexBottomSheetState(
    initialValue: FlexSheetValue = FlexSheetValue.BOTTOM,
    confirmValueChange: (FlexSheetValue) -> Boolean = { true },
    skipHiddenState: Boolean = true,
) = rememberFlexSheetState(confirmValueChange,initialValue)
    @Composable
@ExperimentalMaterial3Api
internal fun rememberFlexSheetState(
        confirmValueChange: (FlexSheetValue) -> Boolean = { true },
        initialValue: FlexSheetValue = FlexSheetValue.BOTTOM,
): FlexSheetState {

    val density = LocalDensity.current
    return rememberSaveable(
        confirmValueChange,
        saver = FlexSheetState.Saver(
            skipPartiallyExpanded = false,
            confirmValueChange = confirmValueChange,
            density = density
        )
    ) {
        FlexSheetState(
            false,
            density,
            initialValue,
            confirmValueChange,
            true
        )
    }
}