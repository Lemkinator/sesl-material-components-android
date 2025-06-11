package com.google.android.material.appbar.model.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.util.SeslMisc
import androidx.appcompat.util.theme.SeslThemeResourceHelper
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.OpenThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.ThemeResourceColor
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.R
import com.google.android.material.util.MaxFontScaleRatio
import com.google.android.material.util.checkMaxFontScale

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * A view representing a single suggestion or action page within a [ViewPager2], extending [SuggestAppBarView].
 * This class provides functionality for handling title, close button, and bottom layout elements.
 * It also manages resource updates based on the current theme (light or dark) and applies font scaling for accessibility.
 *
 * @param context The Context the view is running in, through which it can
 *                access the current theme, resources, etc.
 * @param attributeSet The attributes of the XML tag that is inflating the view.
 */
@RequiresApi(23)
open class SuggestAppBarItemView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : SuggestAppBarView(context, attributeSet) {

    private var _rootView: ViewGroup? = null

    init {
        this.inflate()
    }

    override fun inflate() {
        val context = context

        val appBarSuggestInViewPager = LayoutInflater.from(context)
            .inflate(R.layout.sesl_app_bar_suggest_in_viewpager, this, false) as? ViewGroup
            ?: return

        appBarSuggestInViewPager.apply {
            _rootView = findViewById(R.id.sesl_appbar_suggest_in_viewpager)
            titleView = findViewById(R.id.suggest_app_bar_title)
            close = findViewById(R.id.suggest_app_bar_close)
            bottomLayout = findViewById(R.id.suggest_app_bar_bottom_layout)
        }

        updateResource(context)

        addView(appBarSuggestInViewPager)
    }

    fun setRootView(viewGroup: ViewGroup?) {
        _rootView = viewGroup
    }

    override fun getRootView(): ViewGroup? = _rootView

    override fun updateResource(context: Context) {
        super.updateResource(context)
        _rootView?.setBackgroundResource(
            if (SeslMisc.isLightTheme(context)) R.drawable.sesl_viewpager_item_background
            else R.drawable.sesl_viewpager_item_background_dark
        )
        titleView?.let {
            checkMaxFontScale(
                it,
                R.dimen.sesl_appbar_suggest_title_text_size,
                MaxFontScaleRatio.SMALL
            )
        }
        updateButtons(getButtons())
    }

    private fun getButtonTextColor(): Int = SeslThemeResourceHelper.getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    R.color.sesl_button_text_color,
                    R.color.sesl_button_text_color_dark
                ),
                ThemeResourceColor(
                    R.color.sesl_button_text_color,
                    R.color.sesl_button_text_color_dark_for_theme
                )
            )
        )

    private fun getSuggestButtonTextColor(): Int = SeslThemeResourceHelper.getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    R.color.sesl_suggest_button_text_color,
                    R.color.sesl_suggest_button_text_color_dark
                ),
                ThemeResourceColor(
                    R.color.sesl_suggest_button_text_color,
                    R.color.sesl_suggest_button_text_color_dark_for_theme
                )
            )
        )

    private fun updateButton(button: Button) {
        button.setTextColor(getButtonTextColor())
        checkMaxFontScale(button, R.dimen.sesl_appbar_button_text_size, MaxFontScaleRatio.MEDIUM)
    }

    /**
     * Updates the text color and font scale of the given buttons.
     *
     * @param buttons The list of buttons to update.
     */
    fun updateButtons(buttons: List<Button>) = buttons.forEach { updateButton(it) }
}
