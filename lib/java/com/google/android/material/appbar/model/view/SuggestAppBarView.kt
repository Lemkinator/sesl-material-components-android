package com.google.android.material.appbar.model.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.util.SeslMisc.isLightTheme
import androidx.appcompat.util.theme.SeslThemeResourceHelper
import androidx.appcompat.util.theme.SeslThemeResourceHelper.getDrawable
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.OpenThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceColor.ThemeResourceColor
import androidx.appcompat.util.theme.resource.SeslThemeResourceDrawable.OpenThemeResourceDrawable
import androidx.appcompat.util.theme.resource.SeslThemeResourceDrawable.ThemeResourceDrawable
import androidx.appcompat.widget.TooltipCompat
import androidx.reflect.view.SeslViewReflector
import androidx.reflect.widget.SeslHoverPopupWindowReflector
import com.google.android.material.R
import com.google.android.material.appbar.model.AppBarModel
import com.google.android.material.appbar.model.ButtonListModel
import com.google.android.material.appbar.model.ButtonModel
import com.google.android.material.appbar.model.SuggestAppBarModel
import org.jetbrains.annotations.NotNull

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
@RequiresApi(23)
open class SuggestAppBarView @JvmOverloads constructor(
    @NotNull context: Context,
    @Nullable attrs: AttributeSet? = null
) : AppBarView(context, attrs) {

    @NotNull
    private val buttons = mutableListOf<Button>()
    private var model: SuggestAppBarModel<out SuggestAppBarView>? = null

    @Nullable
    var bottomLayout: ViewGroup? = null

    @Nullable
    var close: ImageButton? = null

    @Nullable
    var titleView: TextView? = null

    private val isLightTheme = isLightTheme(context)

    init {
        this.inflate()
    }

    override fun inflate() {
        val context = context

        val viewGroup = LayoutInflater.from(context).inflate(
            R.layout.sesl_app_bar_suggest, this, false
        ) as? ViewGroup ?: return

        viewGroup.apply {
            titleView = findViewById(R.id.suggest_app_bar_title)
            close = findViewById<ImageButton?>(R.id.suggest_app_bar_close)?.also {
                SeslViewReflector.semSetHoverPopupType(
                    it,
                    SeslHoverPopupWindowReflector.getField_TYPE_NONE()
                );
            }
            bottomLayout = findViewById(R.id.suggest_app_bar_bottom_layout)
        }

        updateResource(context)
        addView(viewGroup)
    }

    private fun addMargin() {
        bottomLayout?.addView(
            View(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    resources.getDimensionPixelOffset(com.google.android.material.R.dimen.sesl_appbar_button_side_margin),
                    MATCH_PARENT
                )
            })
    }

    private fun generateButton(buttonModel: ButtonModel, buttonStyleRes: Int): Button {
        return Button(context, null, 0, buttonStyleRes).apply {
            text = buttonModel.text
            contentDescription = buttonModel.contentDescription
            setOnClickListener {
                buttonModel.clickListener?.onClick(it, model!!)
            }
        }
    }


    fun setButtonModules(buttonListModel: ButtonListModel) {
        bottomLayout?.removeAllViews()
        buttons.clear()

        val buttonModels = buttonListModel.buttonModels
        val buttonStyle = buttonListModel.buttonStyle.let {
            if (isLightTheme) it.defStyleRes else it.defStyleResDark
        }

        for (i in buttonModels.indices) {
            val button = generateButton(buttonModels[i], buttonStyle).apply {
                maxWidth = resources.getDimensionPixelSize(
                    if (buttonModels.size > 1) R.dimen.sesl_appbar_button_max_width
                    else R.dimen.sesl_appbar_button_max_width_multi
                )
            }
            if (i != 0) addMargin()
            buttons.add(button)
            bottomLayout?.addView(button)
        }
    }

    fun setCloseClickListener(onClickListener: AppBarModel.OnClickListener?) {
        close?.apply {
            visibility = if (onClickListener != null) VISIBLE else GONE
            setOnClickListener { onClickListener?.onClick(this, model!!) }
        }
    }

    fun setModel(model: SuggestAppBarModel<out SuggestAppBarView>) {
        this.model = model
    }

    override fun updateResource(@NotNull context: Context) {
        titleView?.setTextColor(getAppBarSuggestTitleColor(context))
        close?.apply {
            resources.getString(androidx.appcompat.R.string.sesl_appbar_suggest_dismiss)
                .let { contentDescription = it; TooltipCompat.setTooltipText(this, it) }
            background = getCloseDrawable(context);
        }
    }

    private fun getCloseDrawable(context: Context): Drawable? =
        if (Build.VERSION.SDK_INT >= 29) getDrawable(
            context,
            OpenThemeResourceDrawable(
                ThemeResourceDrawable(
                    R.drawable.sesl_close_button_recoil_background,
                    R.drawable.sesl_close_button_recoil_background_dark
                ),
                ThemeResourceDrawable(
                    R.drawable.sesl_close_button_recoil_background_for_theme,
                    R.drawable.sesl_close_button_recoil_background_dark_for_theme
                )
            )
        ) else getDrawable(
            context,
            OpenThemeResourceDrawable(
                ThemeResourceDrawable(
                    R.drawable.sesl_ic_close,
                    R.drawable.sesl_ic_close_dark
                ),
                ThemeResourceDrawable(
                    R.drawable.sesl_ic_close_for_theme,
                    R.drawable.sesl_ic_close_dark_for_theme
                )
            )
        )

    private fun getAppBarSuggestTitleColor(context: Context): Int =
        SeslThemeResourceHelper.getColorInt(
            context,
            OpenThemeResourceColor(
                ThemeResourceColor(
                    R.color.sesl_appbar_suggest_title,
                    R.color.sesl_appbar_suggest_title_dark
                ),
                ThemeResourceColor(
                    R.color.sesl_appbar_suggest_title,
                    R.color.sesl_appbar_suggest_title_dark_for_theme
                )
            )
        )

    fun setTitle(title: String?) =
        titleView?.apply { text = title; visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE }

    fun getButtons(): List<Button> = this.buttons
}
