package com.google.android.material.appbar.model

import android.content.Context
import android.view.View
import com.google.android.material.appbar.model.view.AppBarView
import org.jetbrains.annotations.NotNull
import kotlin.reflect.KClass


/*
 * Original code by Samsung, all rights reserved to the original author.
 */
//Added in sesl7
open class AppBarModel<T : AppBarView>(
    @NotNull private val kclazz: KClass<T>,
    @NotNull private val context: Context
) {

    fun interface OnClickListener {
        fun onClick(view: View, module: AppBarModel<out AppBarView>)
    }

    @NotNull
    fun create(): T {
        return init(kclazz.constructors.first().call(this.context, null))
    }

    @NotNull
    open fun init(@NotNull moduleView: T): T{
        return moduleView
    }
}


