package com.google.android.material.appbar.model

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
data class ButtonModel @JvmOverloads constructor(
    val text: String? = null,
    val clickListener: AppBarModel.OnClickListener? = null,
    val contentDescription: String? = null
)
