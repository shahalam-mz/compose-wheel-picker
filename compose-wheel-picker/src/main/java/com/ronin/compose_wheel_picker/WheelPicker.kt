package com.ronin.compose_wheel_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.kaaveh.sdpcompose.sdp

/**
 * WheelPicker is a customizable composable UI element that displays a scrollable wheel picker with both horizontal and vertical modes.
 * The picker can display a set of items with varying sizes and line colors to indicate selected and unselected items.
 *
 * @param modifier Modifier to be applied to the WheelPicker.
 * @param wheelPickerWidth Optional width for the wheel picker, used in horizontal mode.
 * @param wheelPickerHeight Optional height for the wheel picker, used in vertical mode.
 * @param totalItems Total number of items to display in the wheel picker.
 * @param initialSelectedItem The index of the item that is initially selected.
 * @param mode The mode of the wheel picker, either horizontal or vertical. Default is [WheelPickerMode.HORIZONTAL].
 * @param lineThickness The thickness of the lines representing items in the picker. Default is 2.sdp.
 * @param selectedLineSize The size of the selected item line. Default is 64.sdp.
 * @param multipleOfFiveLineSize The size of the lines that represent multiples of 5. Default is 40.sdp.
 * @param normalLineSize The size of normal items. Default is 30.sdp.
 * @param selectedMultipleOfFiveLinePadding Padding around multiples of 5 when selected. Default is 0.sdp.
 * @param normalMultipleOfFiveLinePadding Padding around multiples of 5 when not selected. Default is 6.sdp.
 * @param normalLinePadding Padding around normal items. Default is 8.sdp.
 * @param lineSpacing Spacing between the lines in the picker. Default is 8.sdp.
 * @param lineRoundedCorners The rounding radius of the corners of the lines. Default is 2.sdp.
 * @param selectedLineColor The color of the selected item line. Default is [Color(0xFF00D1FF)].
 * @param unselectedLineColor The color of the unselected item lines. Default is [Color.LightGray].
 * @param fadeOutLinesCount The number of lines to fade out at the edges. Default is 4.
 * @param maxFadeTransparency The maximum transparency of the faded lines. Default is 0.7f.
 * @param onItemSelected Lambda that is triggered when an item is selected.
 *
 * @author Shah Alam
 */
@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    wheelPickerWidth: Dp? = null,
    wheelPickerHeight: Dp? = null,
    totalItems: Int,
    initialSelectedItem: Int,
    mode: WheelPickerMode = WheelPickerMode.HORIZONTAL,
    lineThickness: Dp = 2.sdp,
    selectedLineSize: Dp = 64.sdp,
    multipleOfFiveLineSize: Dp = 40.sdp,
    normalLineSize: Dp = 30.sdp,
    selectedMultipleOfFiveLinePadding: Dp = 0.sdp,
    normalMultipleOfFiveLinePadding: Dp = 6.sdp,
    normalLinePadding: Dp = 8.sdp,
    lineSpacing: Dp = 8.sdp,
    lineRoundedCorners: Dp = 2.sdp,
    selectedLineColor: Color = Color(0xFF00D1FF),
    unselectedLineColor: Color = Color.LightGray,
    fadeOutLinesCount: Int = 4,
    maxFadeTransparency: Float = 0.7f,
    onItemSelected: (Int) -> Unit
) {
    val screenWidthDp = LocalContext.current.resources.displayMetrics.run {
        widthPixels / density
    }.dp

    val screenHeightDp = LocalContext.current.resources.displayMetrics.run {
        heightPixels / density
    }.dp

    val effectiveWidth =
        if (mode == WheelPickerMode.HORIZONTAL) wheelPickerWidth ?: screenWidthDp else lineThickness
    val effectiveHeight =
        if (mode == WheelPickerMode.VERTICAL) wheelPickerHeight ?: screenHeightDp else lineThickness

    var currentSelectedItem by remember { mutableIntStateOf(initialSelectedItem) }
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialSelectedItem)

    val visibleItemsInfo by remember { derivedStateOf { scrollState.layoutInfo.visibleItemsInfo } }
    val firstVisibleItemIndex = visibleItemsInfo.firstOrNull()?.index ?: -1
    val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index ?: -1
    val totalVisibleItems = lastVisibleItemIndex - firstVisibleItemIndex + 1
    val middleIndex = firstVisibleItemIndex + totalVisibleItems / 2
    val bufferIndices = totalVisibleItems / 2

    LaunchedEffect(currentSelectedItem) {
        onItemSelected(currentSelectedItem)
    }

    if (mode == WheelPickerMode.HORIZONTAL) {
        LazyRow(
            modifier = modifier.width(effectiveWidth),
            state = scrollState,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(totalItems + totalVisibleItems) { index ->
                val adjustedIndex = index - bufferIndices

                if (index == middleIndex) {
                    currentSelectedItem = adjustedIndex
                }

                val lineSize = when {
                    index == middleIndex -> selectedLineSize
                    adjustedIndex % 5 == 0 -> multipleOfFiveLineSize
                    else -> normalLineSize
                }

                val padding = when {
                    index == middleIndex -> selectedMultipleOfFiveLinePadding
                    adjustedIndex % 5 == 0 -> normalMultipleOfFiveLinePadding
                    else -> normalLinePadding
                }

                val lineTransparency = calculateLineTransparency(
                    lineIndex = index,
                    totalLines = totalItems,
                    bufferIndices = bufferIndices,
                    firstVisibleItemIndex = firstVisibleItemIndex,
                    lastVisibleItemIndex = lastVisibleItemIndex,
                    fadeOutLinesCount = fadeOutLinesCount,
                    maxFadeTransparency = maxFadeTransparency
                )

                WheelPickerItem(
                    lineThickness = lineThickness,
                    lineSize = lineSize,
                    padding = padding,
                    roundedCorners = lineRoundedCorners,
                    indexAtCenter = index == middleIndex,
                    lineTransparency = lineTransparency,
                    selectedLineColor = selectedLineColor,
                    unselectedLineColor = unselectedLineColor,
                    isHorizontal = true // Horizontal line in horizontal mode
                )

                Spacer(modifier = Modifier.width(lineSpacing))
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.height(effectiveHeight),
            state = scrollState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(totalItems + totalVisibleItems) { index ->
                val adjustedIndex = index - bufferIndices

                if (index == middleIndex) {
                    currentSelectedItem = adjustedIndex
                }

                val lineSize = when {
                    index == middleIndex -> selectedLineSize
                    adjustedIndex % 5 == 0 -> multipleOfFiveLineSize
                    else -> normalLineSize
                }

                val padding = when {
                    index == middleIndex -> selectedMultipleOfFiveLinePadding
                    adjustedIndex % 5 == 0 -> normalMultipleOfFiveLinePadding
                    else -> normalLinePadding
                }

                val lineTransparency = calculateLineTransparency(
                    lineIndex = index,
                    totalLines = totalItems,
                    bufferIndices = bufferIndices,
                    firstVisibleItemIndex = firstVisibleItemIndex,
                    lastVisibleItemIndex = lastVisibleItemIndex,
                    fadeOutLinesCount = fadeOutLinesCount,
                    maxFadeTransparency = maxFadeTransparency
                )

                WheelPickerItem(
                    lineThickness = lineThickness,
                    lineSize = lineSize,
                    padding = padding,
                    roundedCorners = lineRoundedCorners,
                    indexAtCenter = index == middleIndex,
                    lineTransparency = lineTransparency,
                    selectedLineColor = selectedLineColor,
                    unselectedLineColor = unselectedLineColor,
                    isHorizontal = false // Vertical line in vertical mode
                )

                Spacer(modifier = Modifier.height(lineSpacing))
            }
        }
    }
}

/**
 * WheelPickerItem represents an individual item in the wheel picker.
 * Each item is displayed as a line with various customization options such as size, transparency, and padding.
 *
 * @param lineThickness The thickness of the line representing the item.
 * @param lineSize The size of the line representing the item.
 * @param padding Padding applied around the line.
 * @param roundedCorners The radius for rounding the corners of the line.
 * @param indexAtCenter Indicates if the item is at the center (selected) position.
 * @param lineTransparency The transparency level of the line.
 * @param selectedLineColor The color of the line when it is selected.
 * @param unselectedLineColor The color of the line when it is unselected.
 * @param isHorizontal Whether the line is rendered horizontally or vertically.
 */
@Composable
private fun WheelPickerItem(
    lineThickness: Dp,
    lineSize: Dp,
    padding: Dp,
    roundedCorners: Dp,
    indexAtCenter: Boolean,
    lineTransparency: Float,
    selectedLineColor: Color,
    unselectedLineColor: Color,
    isHorizontal: Boolean
) {
    Box(
        modifier = Modifier
            .then(
                if (isHorizontal)
                    Modifier
                        .width(lineThickness)
                        .height(lineSize)
                else
                    Modifier
                        .width(lineSize)
                        .height(lineThickness)
            )
            .clip(RoundedCornerShape(roundedCorners))
            .alpha(lineTransparency)
            .background(if (indexAtCenter) selectedLineColor else unselectedLineColor)
            .padding(
                bottom = if (isHorizontal) padding else 0.dp,
                end = if (!isHorizontal) padding else 0.dp
            )
    )
}

/**
 * Calculate the transparency level for a line based on its position within the visible range.
 * The further the line is from the center, the more transparent it becomes.
 *
 * @param lineIndex The index of the line.
 * @param totalLines The total number of lines in the picker.
 * @param bufferIndices The number of lines that should be buffered (invisible).
 * @param firstVisibleItemIndex The index of the first visible item.
 * @param lastVisibleItemIndex The index of the last visible item.
 * @param fadeOutLinesCount The number of lines to fade out at the edges.
 * @param maxFadeTransparency The maximum transparency for faded lines.
 * @return The calculated transparency value for the line.
 */
private fun calculateLineTransparency(
    lineIndex: Int,
    totalLines: Int,
    bufferIndices: Int,
    firstVisibleItemIndex: Int,
    lastVisibleItemIndex: Int,
    fadeOutLinesCount: Int,
    maxFadeTransparency: Float
): Float {
    val actualCount = fadeOutLinesCount + 1
    val transparencyStep = maxFadeTransparency / actualCount

    return when {
        lineIndex < bufferIndices || lineIndex > (totalLines + bufferIndices) -> 0.0f
        lineIndex in firstVisibleItemIndex until firstVisibleItemIndex + fadeOutLinesCount -> {
            transparencyStep * (lineIndex - firstVisibleItemIndex + 1)
        }

        lineIndex in (lastVisibleItemIndex - fadeOutLinesCount + 1)..lastVisibleItemIndex -> {
            transparencyStep * (lastVisibleItemIndex - lineIndex + 1)
        }

        else -> 1.0f
    }
}