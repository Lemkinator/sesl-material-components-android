package com.google.android.material.appbar.model.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.util.theme.SeslThemeResourceHelper
import androidx.appcompat.util.theme.SeslThemeResourceHelper.getDrawable
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.OpenThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.ThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceDrawable.OpenThemeResourceDrawable
import androidx.appcompat.util.theme.resource.SeslThemeResourceDrawable.ThemeResourceDrawable
import com.google.android.material.R

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * A subclass of [SuggestAppBarItemView] that customizes the appearance of the item for a white background.
 */
@RequiresApi(23)
open class SuggestAppBarItemWhiteCaseView(
    context: Context
) : SuggestAppBarItemView(context, null) {

    override fun updateResource(context: Context) {
        super.updateResource(context)
        rootView?.setBackgroundTintList(
            ColorStateList.valueOf(
                getViewPagerItemBackgroundWhiteWhiteCaseColor(context)
            )
        )
        titleView?.setTextColor(getSuggestTitleWithWhiteCaseColor(context))
        close?.background = getCloseDrawable(context)
        updateButtons(getButtons());
    }

    private fun getViewPagerItemBackgroundWhiteWhiteCaseColor(context: Context): Int =
        SeslThemeResourceHelper.getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    R.color.sesl_viewpager_item_background_with_white_case,
                    R.color.sesl_viewpager_item_background_dark
                ),
                ThemeResourceColor(
                    R.color.sesl_viewpager_item_background_with_white_case_for_theme,
                    R.color.sesl_viewpager_item_background_dark
                )
            )
        )

    private fun getSuggestTitleWithWhiteCaseColor(context: Context): Int =
        SeslThemeResourceHelper.getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    R.color.sesl_appbar_suggest_title_with_white_case,
                    R.color.sesl_appbar_suggest_title_dark
                ),
                ThemeResourceColor(
                    R.color.sesl_appbar_suggest_title_with_white_case,
                    R.color.sesl_appbar_suggest_title_dark_for_theme
                )
            )
        )


    private fun getCloseDrawable(context: Context): Drawable? =
        if (Build.VERSION.SDK_INT >= 29) getDrawable(
            context,
            OpenThemeResourceDrawable(
                ThemeResourceDrawable(
                    R.drawable.sesl_close_button_recoil_background_with_white_case,
                    R.drawable.sesl_close_button_recoil_background_dark
                ),
                ThemeResourceDrawable(
                    R.drawable.sesl_close_button_recoil_background_with_white_case_for_theme,
                    R.drawable.sesl_close_button_recoil_background_dark_for_theme
                )
            )
        ) else getDrawable(
            context,
            OpenThemeResourceDrawable(
                ThemeResourceDrawable(
                    R.drawable.sesl_ic_close_with_white_case,
                    R.drawable.sesl_ic_close_dark
                ),
                ThemeResourceDrawable(
                    R.drawable.sesl_ic_close_with_white_case_for_theme,
                    R.drawable.sesl_ic_close_dark_for_theme
                )
            )
        )
}
