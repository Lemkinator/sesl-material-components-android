package com.google.android.material.appbar.model.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.util.theme.SeslThemeResourceHelper.getColorInt
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.OpenThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.ThemeResourceColor
import com.google.android.material.R

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * Abstract subclass of [BasicViewPagerAppBarView] that applies a white background and theme-specific indicator colors.
 *
 * @param context The context in which the view is running, providing access to resources, themes, and more.
 * @param attributeSet The set of XML attributes used to inflate the view, or null if created programmatically.
 */
@RequiresApi(23)
abstract class BasicViewPagerAppBarWhiteCaseView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : BasicViewPagerAppBarView(context, attributeSet) {

    override fun updateResource(context: Context) {
        viewpager?.backgroundTintList = getViewPagerBackgroundColorStateList(context)
        val indicator = indicator ?: return

        val offStateDrawable =
            context.getDrawable(R.drawable.sesl_viewpager_indicator_on_off)?.mutate()?.apply {
                setTint(getViewPagerIndicatorOffWithWhiteCaseColor(context))
            }
        indicator.defaultCircle = offStateDrawable

        val onStateDrawable =
            context.getDrawable(R.drawable.sesl_viewpager_indicator_on_off)?.mutate()?.apply {
                setTint(getViewPagerIndicatorOnWithWhiteCaseColor(context))
            }
        indicator.selectCircle = onStateDrawable
    }

    private fun getViewPagerBackgroundColorStateList(context: Context): ColorStateList =
        ColorStateList.valueOf(
            getColorInt(
                context,
                OpenThemeResourceColor(
                    ThemeResourceColor(
                        R.color.sesl_viewpager_background,
                        R.color.sesl_viewpager_background_dark
                    ),
                    ThemeResourceColor(R.color.sesl_viewpager_background_for_theme)
                )
            )
        )

    private fun getViewPagerIndicatorOffWithWhiteCaseColor(context: Context): Int =
        getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    R.color.sesl_appbar_viewpager_indicator_off_with_white_case,
                    R.color.sesl_appbar_viewpager_indicator_off_dark
                ),
                ThemeResourceColor(
                    R.color.sesl_appbar_viewpager_indicator_off_with_white_case_for_theme,
                    R.color.sesl_appbar_viewpager_indicator_off_dark_for_theme
                )
            )
        )

    private fun getViewPagerIndicatorOnWithWhiteCaseColor(context: Context): Int =
        getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(R.color.sesl_appbar_viewpager_indicator_on_with_white_case),
                ThemeResourceColor(R.color.sesl_appbar_viewpager_indicator_on_with_white_case_for_theme)
            )
        )
}