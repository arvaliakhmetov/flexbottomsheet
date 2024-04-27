package com.aval.flexbottomsheet

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException



@Stable
@ExperimentalMaterial3Api
@OptIn(ExperimentalFoundationApi::class)
class FlexSheetState @Deprecated(
    message = "This constructor is deprecated. " +
            "Please use the constructor that provides a [Density]",
    replaceWith = ReplaceWith(
        "SheetState(" +
                "skipPartiallyExpanded, LocalDensity.current, initialValue, " +
                "confirmValueChange, skipHiddenState)"
    )
) constructor(
    internal val skipPartiallyExpanded: Boolean,
    initialValue: FlexSheetValue = FlexSheetValue.BOTTOM,
    confirmValueChange: (FlexSheetValue) -> Boolean = { true },
    internal val skipHiddenState: Boolean = false,
) {


    @ExperimentalMaterial3Api
    @Suppress("Deprecation")
    constructor(
        skipPartiallyExpanded: Boolean,
        density: Density,
        initialValue: FlexSheetValue = FlexSheetValue.BOTTOM,
        confirmValueChange: (FlexSheetValue) -> Boolean = { true },
        skipHiddenState: Boolean = false,
    ) : this(skipPartiallyExpanded, initialValue, confirmValueChange, skipHiddenState) {
        this.density = density
    }

    /**
     * The current value of the state.
     *
     * If no swipe or animation is in progress, this corresponds to the state the bottom sheet is
     * currently in. If a swipe or an animation is in progress, this corresponds the state the sheet
     * was in before the swipe or animation started.
     */

    val currentValue: FlexSheetValue get() = anchoredDraggableState.currentValue

    /**
     * The target value of the bottom sheet state.
     *
     * If a swipe is in progress, this is the value that the sheet would animate to if the
     * swipe finishes. If an animation is running, this is the target value of that animation.
     * Finally, if no swipe or animation is in progress, this is the same as the [currentValue].
     */
    val targetValue: FlexSheetValue get() = anchoredDraggableState.targetValue

    /**
     * Whether the modal bottom sheet is visible.
     */

    /**
     * Require the current offset (in pixels) of the bottom sheet.
     *
     * The offset will be initialized during the first measurement phase of the provided sheet
     * content.
     *
     * These are the phases:
     * Composition { -> Effects } -> Layout { Measurement -> Placement } -> Drawing
     *
     * During the first composition, an [IllegalStateException] is thrown. In subsequent
     * compositions, the offset will be derived from the anchors of the previous pass. Always prefer
     * accessing the offset from a LaunchedEffect as it will be scheduled to be executed the next
     * frame, after layout.
     *
     * @throws IllegalStateException If the offset has not been initialized yet
     */
    fun requireOffset(): Float {
        val offset = anchoredDraggableState.offset
        return if(offset.isNaN()){
            0f
        } else {
            anchoredDraggableState.offset
        }

    }

    /**
     * Whether the sheet has an expanded state defined.
     */

    val hasPartExpanded: Boolean
        get() = anchoredDraggableState.anchors.hasAnchorFor(FlexSheetValue.PART)

    val hasFullExpanded: Boolean
        get() = anchoredDraggableState.anchors.hasAnchorFor(FlexSheetValue.FULL)

    /**
     * Whether the modal bottom sheet has a partially expanded state defined.
     */
    val hasBottomExpandedState: Boolean
        get() = anchoredDraggableState.anchors.hasAnchorFor(FlexSheetValue.BOTTOM)

    /**
     * Fully expand the bottom sheet with animation and suspend until it is fully expanded or
     * animation has been cancelled.
     * *
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun expand() {
        anchoredDraggableState.animateTo(FlexSheetValue.FULL)
    }

    /**
     * Animate the bottom sheet and suspend until it is partially expanded or animation has been
     * cancelled.
     * @throws [CancellationException] if the animation is interrupted
     * @throws [IllegalStateException] if [skipPartiallyExpanded] is set to true
     */
    suspend fun partExpand() {
        animateTo(FlexSheetValue.PART)
    }

    suspend fun bottomExpand() {
        animateTo(FlexSheetValue.BOTTOM)
    }

    /**
     * Expand the bottom sheet with animation and suspend until it is [PartiallyExpanded] if defined
     * else [Expanded].
     * @throws [CancellationException] if the animation is interrupted
     */
    suspend fun show(targetValue: FlexSheetValue) {
        animateTo(targetValue)
    }

    /**
     * Hide the bottom sheet with animation and suspend until it is fully hidden or animation has
     * been cancelled.
     * @throws [CancellationException] if the animation is interrupted
     */


    /**
     * Animate to a [targetValue].
     * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to the
     * [targetValue] without updating the offset.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    internal suspend fun animateTo(
        targetValue: FlexSheetValue,
        velocity: Float = anchoredDraggableState.lastVelocity
    ) {
        anchoredDraggableState.animateTo(targetValue, velocity)
    }

    /**
     * Snap to a [targetValue] without any animation.
     *
     * @throws CancellationException if the interaction interrupted by another interaction like a
     * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
     *
     * @param targetValue The target value of the animation
     */
    internal suspend fun snapTo(targetValue: FlexSheetValue) {
        anchoredDraggableState.snapTo(targetValue)
    }

    /**
     * Find the closest anchor taking into account the velocity and settle at it with an animation.
     */
    internal suspend fun settle(velocity: Float) {
        anchoredDraggableState.settle(velocity)
    }

    @OptIn(ExperimentalFoundationApi::class)
    var anchoredDraggableState =
        AnchoredDraggableState<FlexSheetValue>(
            initialValue = initialValue,
            animationSpec = tween(200),
            confirmValueChange = confirmValueChange,
            positionalThreshold = { with(requireDensity()) { 56.dp.toPx() } },
            velocityThreshold = { with(requireDensity()) { 125.dp.toPx() } }
        )

    internal val offset: Float? get() = anchoredDraggableState.offset

    internal var density: Density? = null
    private fun requireDensity() = requireNotNull(density) {
        "SheetState did not have a density attached. Are you using SheetState with " +
                "BottomSheetScaffold or ModalBottomSheet component?"
    }

    companion object {
        /**
         * The default [Saver] implementation for [SheetState].
         */
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (FlexSheetValue) -> Boolean,
            density: Density
        ) = Saver<FlexSheetState, FlexSheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                FlexSheetState(skipPartiallyExpanded, density, savedValue, confirmValueChange)
            }
        )

        /**
         * The default [Saver] implementation for [SheetState].
         */
        @Deprecated(
            message = "This function is deprecated. Please use the overload where Density is" +
                    " provided.",
            replaceWith = ReplaceWith(
                "Saver(skipPartiallyExpanded, confirmValueChange, LocalDensity.current)"
            )
        )
        @Suppress("Deprecation")
        fun Saver(
            skipPartiallyExpanded: Boolean,
            confirmValueChange: (FlexSheetValue) -> Boolean
        ) = Saver<FlexSheetState, FlexSheetValue>(
            save = { it.currentValue },
            restore = { savedValue ->
                FlexSheetState(skipPartiallyExpanded, savedValue, confirmValueChange)
            }
        )
    }
}

enum class FlexSheetValue{
    BOTTOM,
    PART,
    FULL
}