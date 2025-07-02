package com.netease.nim.samples.base.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.netease.nim.samples.R
import java.util.Objects

class LoadingDialog : DialogFragment() {
    var isShowing: Boolean = false
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setStyle(STYLE_NO_TITLE, R.style.loading_dialog)
        return inflater.inflate(R.layout.dialog_loading, container)
    }

    fun show(manager: FragmentManager?) {
        super.show(manager!!, "loading_dialog")
        isShowing = true
    }

    override fun dismiss() {
        if (isShowing) {
            super.dismiss()
            isShowing = false
        }
    }
}
