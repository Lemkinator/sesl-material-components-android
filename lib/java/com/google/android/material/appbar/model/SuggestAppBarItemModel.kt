package com.google.android.material.appbar.model

import android.content.Context
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.google.android.material.R
import com.google.android.material.appbar.model.view.SuggestAppBarItemView
import org.jetbrains.annotations.NotNull
import kotlin.reflect.KClass

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * A model class extends [SuggestAppBarModel] and provides functionality to manage the data and
 * behavior of a [SuggestAppBarItemView] or its subclass.
 *
 * @param T The view, which must extend [SuggestAppBarItemView], associated with this model
 * @param kclazz The Kotlin class of the view.
 * @param context The context used to access resources.
 * @param title The title to be displayed in the app bar item. Can be null.
 * @param onClickListener The click listener for the close button. Can be null.
 * @param buttonListModel The model for the list of buttons to be displayed.
 *
 * @see SuggestAppBarModel
 * @see SuggestAppBarItemView
 */
@RequiresApi(23)
open class SuggestAppBarItemModel<T : SuggestAppBarItemView> (
    @NotNull kclazz: KClass<T>,
    @NotNull context: Context,
    @Nullable title: String?,
    @Nullable onClickListener: OnClickListener?,
    @NotNull buttonListModel: ButtonListModel
) : SuggestAppBarModel<T> (kclazz, context, title, onClickListener, buttonListModel) {

    override fun init(moduleView: T): T {
        return moduleView.apply {
            setModel(this@SuggestAppBarItemModel)
            setTitle(title)
            setCloseClickListener(closeClickListener)
            setButtonModules(buttonListModel)
            updateResource(context);
        }
    }

    class Builder(private val context: Context) {
        private var buttonStyle: ButtonStyle? = null
        private var buttons: List<ButtonModel> = ArrayList()
        private var closeClickListener: OnClickListener? = null
        private var title: String? = null

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

        fun build(): SuggestAppBarItemModel<SuggestAppBarItemView> {

            if (buttonStyle == null) {
                buttonStyle = ButtonStyle(
                    R.style.Basic_CollapsingToolbar_Button_Light,
                    R.style.Basic_CollapsingToolbar_Button
                )
            }

            return SuggestAppBarItemModel(
                SuggestAppBarItemView::class,
                context,
                title,
                closeClickListener,
                ButtonListModel(buttonStyle!!, this.buttons)
            )
        }

    }
}
