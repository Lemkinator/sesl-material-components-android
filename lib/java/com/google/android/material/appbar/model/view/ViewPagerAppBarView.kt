@file:Suppress("MemberVisibilityCanBePrivate")

package com.google.android.material.appbar.model.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.RequiresApi
import androidx.appcompat.util.theme.SeslThemeResourceHelper.getColorInt
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.OpenThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.ThemeResourceColor
import androidx.appcompat.widget.SeslIndicator
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.R

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
@RequiresApi(23)
open class ViewPagerAppBarView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : AppBarView(context, attributeSet) {

    var bottomLayout: ViewGroup? = null

    var indicator: SeslIndicator? = null

    var viewpager: ViewPager2? = null

    init {
        this.inflate()
    }

    override fun inflate() {
        val context = context

        val appBarViewPagerVG = LayoutInflater.from(context).inflate(
            R.layout.sesl_app_bar_viewpager, this, false
        ) as? ViewGroup ?: return

        appBarViewPagerVG.apply {
            viewpager = findViewById(R.id.app_bar_viewpager)
            bottomLayout = findViewById(R.id.bottom_layout)
        }

        indicator = SeslIndicator(context, null).apply {
            setOnItemClickListener { _, i -> viewpager?.setCurrentItem(i, true) }
        }

        viewpager?.seslSetSuggestionPaging(true)
        bottomLayout?.addView(
            indicator,
            LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { gravity = Gravity.CENTER })
        addView(appBarViewPagerVG)
    }


    override fun updateResource(context: Context) {
        viewpager?.setBackgroundTintList(getViewPagerBackgroundColorStateList(context));

        val indicator = indicator ?: return

        val offStateDrawable =
            context.getDrawable(R.drawable.sesl_viewpager_indicator_on_off)?.mutate()?.apply {
                setTint(getViewPagerIndicatorOffColor(context))
            }
        indicator.defaultCircle = offStateDrawable

        val onStateDrawable =
            context.getDrawable(R.drawable.sesl_viewpager_indicator_on_off)?.mutate()?.apply {
                setTint(getViewPagerIndicatorOnColor(context))
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

    private fun getViewPagerIndicatorOffColor(context: Context): Int =
        getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    androidx.appcompat.R.color.sesl_appbar_viewpager_indicator_off,
                    androidx.appcompat.R.color.sesl_appbar_viewpager_indicator_off_dark
                ),
                ThemeResourceColor(
                    androidx.appcompat.R.color.sesl_appbar_viewpager_indicator_off_for_theme,
                    androidx.appcompat.R.color.sesl_appbar_viewpager_indicator_off_dark_for_theme
                )
            )
        )

    private fun getViewPagerIndicatorOnColor(context: Context): Int =
        getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(androidx.appcompat.R.color.sesl_appbar_viewpager_indicator_on),
                ThemeResourceColor(androidx.appcompat.R.color.sesl_appbar_viewpager_indicator_on_for_theme)
            )
        )

}
