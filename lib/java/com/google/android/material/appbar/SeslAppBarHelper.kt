package com.google.android.material.appbar

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.R
import org.jetbrains.annotations.NotNull

class SeslAppBarHelper {

    companion object {
        const val TAG: String = "SeslAppBarHelper"

        @JvmStatic
        fun getAppBarProPortion(@NotNull context: Context): Float {
            val resources = context.resources
            val configuration: Configuration = resources.configuration
            if (Build.VERSION.SDK_INT < 35) {
                return ResourcesCompat.getFloat(resources, R.dimen.sesl_appbar_height_proportion)
            }
            val fullWindowHeightDp = getFullWindowHeightDp(context)
            Log.d(TAG, "orientation=${configuration.orientation}, fullWindowHeightDp=$fullWindowHeightDp")
            return if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                when {
                    fullWindowHeightDp < 639.0f -> 0.0f
                    fullWindowHeightDp < 696.0f -> 0.48f
                    fullWindowHeightDp < 780.0f -> 0.43f
                    fullWindowHeightDp < 960.0f -> 0.38f
                    else -> 0.305f
                }
            } else {
                when {
                    fullWindowHeightDp < 580.0f -> 0.0f
                    fullWindowHeightDp < 640.0f -> 0.51f
                    fullWindowHeightDp < 670.0f -> 0.475f
                    fullWindowHeightDp < 710.0f -> 0.45f
                    fullWindowHeightDp < 750.0f -> 0.425f
                    fullWindowHeightDp < 800.0f -> 0.4f
                    fullWindowHeightDp < 1080.0f -> 0.37f
                    else -> 0.27f
                }
            }
        }

        @JvmStatic
        @RequiresApi(34)
        fun getFullWindowHeightDp(@NotNull context: Context): Float {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = context.resources.displayMetrics
            val currentWindowMetrics = windowManager.currentWindowMetrics
            val bounds = currentWindowMetrics.bounds
            val height = bounds.height()
            val deriveDimension: Float = TypedValue.deriveDimension(TypedValue.COMPLEX_UNIT_DIP, height.toFloat(), displayMetrics)
            Log.d(TAG, "fullWindowHeight(dp)=$deriveDimension, fullWindowHeight(px)=$height, screenHeightDp=${context.resources.configuration.screenHeightDp}")
            return deriveDimension
        }

        @JvmStatic
        fun getScreenHeight(@NotNull view: View): Int {
            val context: Context = view.context
            if (Build.VERSION.SDK_INT < 35) {
                return context.resources.displayMetrics.heightPixels
            }
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val currentWindowMetrics = windowManager.currentWindowMetrics
            val rootWindowInsets = ViewCompat.getRootWindowInsets(view)
            val insets = rootWindowInsets?.getInsets(WindowInsetsCompat.Type.systemBars()) ?: Insets.NONE
            val bounds = currentWindowMetrics.bounds
            val height = bounds.height() - insets.top - insets.bottom
            Log.d(TAG, "screenHeight(px)=$height, status=${insets.top}, navi=${insets.bottom}")
            return height
        }
    }
}