package com.dergoogler.mmrl.ext

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UnusedReceiverParameter")
fun BottomSheetDefaults.expandedShape(size: Dp) = RoundedCornerShape(topStart = size, topEnd = size)
