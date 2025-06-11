package com.google.android.material.appbar.model

import android.content.Context
import androidx.annotation.RequiresApi
import com.google.android.material.R
import com.google.android.material.appbar.model.view.SuggestAppBarView
import org.jetbrains.annotations.NotNull
import kotlin.reflect.KClass

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * This model class extends [AppBarModel] and provides functionality to manage the data and
 * behavior of a [SuggestAppBarView] or its subclass which designed to show a single suggestion or action page.
 * This class provides functionality to set the title, define action buttons, and handle click events for both
 * the close button and the action buttons.
 *
 * Use the [Builder] class to construct instances of [SuggestAppBarModel].
 *
 * @param T The type of [SuggestAppBarView] this model is associated with.
 * @param kclazz The KClass of the [SuggestAppBarView] implementation.
 * @param context The context used to access resources.
 * @param title The title to be displayed in the app bar. Can be null.
 * @param closeClickListener The [OnClickListener] to be invoked when the close button is clicked. Can be null.
 * @param buttonListModel The [ButtonListModel] containing the list of buttons and their styles.
 */
@RequiresApi(23)
open class SuggestAppBarModel<T : SuggestAppBarView> (
    @NotNull kclazz: KClass<T>,
    @NotNull context: Context,
    @NotNull val title: String?,
    @NotNull val closeClickListener: OnClickListener?,
    val buttonListModel: ButtonListModel
) : AppBarModel<T>(kclazz, context) {

    override fun init(moduleView: T): T {
        return moduleView.apply {
            setModel(this@SuggestAppBarModel)
            setTitle(title)
            setCloseClickListener(closeClickListener)
            setButtonModules(buttonListModel)
            updateResource(context)
        }
    }

    class Builder(private val context: Context) {
        private var buttonStyle: ButtonStyle? = null
        private var buttons: List<ButtonModel> = ArrayList()
        private var closeClickListener: OnClickListener? = null
        private var title: String? = null

        fun build(): SuggestAppBarModel<SuggestAppBarView> {
            if (buttonStyle == null) {
                buttonStyle = ButtonStyle(
                    R.style.Basic_CollapsingToolbar_Button_Light,
                    R.style.Basic_CollapsingToolbar_Button
                )
            }
            return SuggestAppBarModel(
                SuggestAppBarView::class,
                context,
                title,
                closeClickListener,
                ButtonListModel(buttonStyle!!, this.buttons)
            )
        }

        @JvmOverloads
        fun setButtons(buttons: List<ButtonModel>, buttonStyle: ButtonStyle? = null): Builder {
            this.buttons = buttons
            buttonStyle?.let { this.buttonStyle = it }
            return this
        }

        fun setCloseClickListener(onClickListener: OnClickListener?): Builder {
            this.closeClickListener = onClickListener
            return this
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

    }
}
