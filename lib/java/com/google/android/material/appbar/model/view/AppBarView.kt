package com.google.android.material.appbar.model.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Nullable
import org.jetbrains.annotations.NotNull

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
abstract class AppBarView @JvmOverloads constructor(
  @NotNull context: Context,
  @Nullable attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

  abstract fun inflate()
  abstract fun updateResource(context: Context)

}
