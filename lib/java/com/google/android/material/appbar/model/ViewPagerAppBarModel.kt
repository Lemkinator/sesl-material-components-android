package com.google.android.material.appbar.model

import android.content.Context
import com.google.android.material.appbar.model.view.AppBarView
import com.google.android.material.appbar.model.view.ViewPagerAppBarView
import kotlin.reflect.KClass

/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
open class ViewPagerAppBarModel<T : ViewPagerAppBarView> (
    kclazz: KClass<T>,
    context: Context,
    private val appBarModels: List<AppBarModel<out AppBarView>>
) : AppBarModel<T>(kclazz, context) {

    override fun init(moduleView: T): T {
        return moduleView
    }

    class Builder(private val context: Context,
                  private var appBarModels: List<AppBarModel<out AppBarView>> = emptyList()) {

        fun setModels(models: List<AppBarModel<out AppBarView>>): Builder {
            appBarModels = models
            return this
        }

        fun build(): ViewPagerAppBarModel<ViewPagerAppBarView> {
            return ViewPagerAppBarModel(
                ViewPagerAppBarView::class,
                this.context,
                appBarModels
            )
        }

    }
}