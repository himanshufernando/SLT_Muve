package com.project.himanshu.sltmuve.ui.adaptars

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.project.himanshu.sltmuve.R

object  CustomBindingAdapter {

    @BindingAdapter("typeImg")
    @JvmStatic
    fun typeImg(view: ImageView, typeImg: String?) {

        Glide.with(view.context)
            .load(typeImg)
            .placeholder(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(view)
    }


}