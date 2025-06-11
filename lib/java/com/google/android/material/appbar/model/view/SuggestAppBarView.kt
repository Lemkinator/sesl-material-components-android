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
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * Base view class for displaying a single suggestion or action page within an expanded AppBar.
 *
 * This class extends [AppBarView] and is responsible for inflating and managing its own layout,
 * including adapting its appearance (such as colors and drawables) based on the current theme (light or dark).
 *
 * The view is composed of the following configurable components:
 * - A title, set via [setTitle].
 * - An optional close button in the top-right corner, with its click listener set by [setCloseClickListener].
 * - One or more action buttons at the bottom, configured using [setButtonModules].
 *
 * The data and behavior for this view are provided by an associated [SuggestAppBarModel] (or subclass),
 * which is set via [setModel].
 *
 * @param context The context in which the view is running, providing access to resources, themes, etc.
 * @param attrs The attribute set from XML used to inflate the view, or null if created programmatically.
 *
 * @see AppBarView
 * @see SuggestAppBarModel
 */
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

    /**
     * Inflates the layout using the `sesl_app_bar_suggest.xml` resource and adds it as a child view.
     *
     * This method initializes the main components of the inflated layout:
     * - Title view
     * - Close button
     * - Bottom layout (for action buttons)
     *
     * It also disables hover popups for the close button (if present) and updates resources based on the current theme.
     *
     * This method is called during this view's initialization.
     */
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


    /**
     * Configures and displays a list of buttons in the bottom layout of this view.
     * This method clears any existing buttons, then generates and adds new buttons based on the
     * provided [ButtonListModel]. The appearance of the buttons (style, max width) is
     * dynamically adjusted based on the current theme (light/dark) and the number of buttons.
     *
     * @param buttonListModel The [ButtonListModel] containing the data for the buttons to be displayed,
     *                        including their text, content descriptions, click listeners, and style.
     */
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

    /**
     * Sets a click listener for the close button.
     * If the listener is null, the close button will be hidden. Otherwise, it will be visible.
     * When the close button is clicked, the provided listener's `onClick` method will be invoked.
     *
     * @param onClickListener The [AppBarModel.OnClickListener] to be invoked when the close button is clicked.
     *                        Pass `null` to remove the listener and hide the button.
     */
    fun setCloseClickListener(onClickListener: AppBarModel.OnClickListener?) {
        close?.apply {
            visibility = if (onClickListener != null) VISIBLE else GONE
            setOnClickListener { onClickListener?.onClick(this, model!!) }
        }
    }

    /**
     * Sets the data model for this view.
     * The model contains the data and logic that this view will display and interact with.
     *
     * @param model The [SuggestAppBarModel] to be associated with this view.
     */
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

    /**
     * Sets the title text to be shown in the title area.
     * If the title is null or empty, the title view will be hidden.
     *
     * @param title The title string to display.
     */
    fun setTitle(title: String?) =
        titleView?.apply { text = title; visibility = if (TextUtils.isEmpty(title)) GONE else VISIBLE }

    /**
     * Retrieves the list of buttons currently displayed in this view.
     * This list contains `Button` instances that were generated based on the [ButtonListModel]
     * provided via [setButtonModules].
     *
     * @return A `List` of [Button] objects. This list may be empty if no buttons have been set.
     */
    fun getButtons(): List<Button> = this.buttons
}
