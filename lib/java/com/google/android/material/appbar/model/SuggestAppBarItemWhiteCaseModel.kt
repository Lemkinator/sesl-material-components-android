package com.google.android.material.appbar.model

import android.content.Context
import androidx.annotation.RequiresApi
import com.google.android.material.R
import com.google.android.material.appbar.model.view.SuggestAppBarItemWhiteCaseView
import kotlin.reflect.KClass

/*
 * Original code by Samsung, all rights reserved to the original author. Added in sesl7
 */
/**
 * A model for the [SuggestAppBarItemWhiteCaseView].
 * This class extends [SuggestAppBarItemModel] and is specifically designed for views
 * that have a white background and require specific styling for their buttons and title.
 *
 * @param T The type of the view that this model will be associated with. Must extend [SuggestAppBarItemWhiteCaseView].
 * @param kclazz The KClass of the view.
 * @param context The context used to access resources.
 * @param title The title to be displayed in the app bar item.
 * @param onClickListener The listener for close button click events.
 * @param buttonListModel The model for the list of buttons to be displayed.
 *
 * @see SuggestAppBarItemModel
 * @see SuggestAppBarItemWhiteCaseView
 */
@RequiresApi(23)
open class SuggestAppBarItemWhiteCaseModel<T : SuggestAppBarItemWhiteCaseView>(
    kclazz: KClass<T>,
    context: Context,
    title: String?,
    onClickListener: OnClickListener?,
    buttonListModel: ButtonListModel
) : SuggestAppBarItemModel<T>(kclazz, context, title, onClickListener, buttonListModel) {

    override fun init(moduleView: T): T {
        return moduleView.apply {
            setModel(this@SuggestAppBarItemWhiteCaseModel)
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

        fun build(): SuggestAppBarItemWhiteCaseModel<SuggestAppBarItemWhiteCaseView> {

            if (buttonStyle == null) {
                buttonStyle = ButtonStyle(
                    R.style.Basic_CollapsingToolbar_Button_Light,
                    R.style.Basic_CollapsingToolbar_Button
                )
            }

            return SuggestAppBarItemWhiteCaseModel(
                SuggestAppBarItemWhiteCaseView::class,
                context,
                title,
                closeClickListener,
                ButtonListModel(buttonStyle!!, this.buttons)
            )
        }

    }

}
