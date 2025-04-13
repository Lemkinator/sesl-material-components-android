package com.google.android.material.appbar.model

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
data class ButtonListModel(
    @JvmField
    val buttonStyle: ButtonStyle,
    @JvmField
    val buttonModels: List<ButtonModel>
)
