package com.tencent.virtualman_demo_app.view.dialog

import android.content.Context
import com.tencent.virtualman_demo_app.R
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.virtualman_demo_app.ui.adapter.SingleChoiceAdapter
import com.tencent.virtualman_demo_app.utils.ArmUtils
import com.tencent.virtualman_demo_app.utils.GlideLoader
import com.tencent.virtualman_demo_app.view.SpacesItemDecoration

class SingleChoiceDialog(context: Context, private val list: List<String?>, val content: String?, val style: String?, private val imageUrl: String?) : BaseDialog(context) {

    var onClickSingleChoiceListener: OnClickSingleChoiceListener ?= null

    override fun getLayoutId(): Int {
        return R.layout.dialog_single_choice
    }

    override fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val rvSingleTitle = findViewById<RecyclerView>(R.id.rvSingleTitle)
        val tvSingleTitle = findViewById<TextView>(R.id.tvSingleTitle)
        val ivPreviewImage = findViewById<ImageView>(R.id.ivPreviewImage)
        if (content?.isNotEmpty() == true){
            tvSingleTitle.text = content
        }
        if (imageUrl?.isNotEmpty() == true){
            GlideLoader.loaderCenterCrop(getContext(), ivPreviewImage, imageUrl);
        }
        findViewById<ImageButton>(R.id.ibtnSingleError).setOnClickListener {
            dismiss()
        }
        findViewById<RelativeLayout>(R.id.rl_single_choice).setOnClickListener {
            dismiss()
        }

        val adapter = SingleChoiceAdapter(list, object : SingleChoiceAdapter.OnItemClickListener{
            override fun onClick(position: Int, url: String?) {
                if (onClickSingleChoiceListener != null) {
                    onClickSingleChoiceListener?.confirmSingle(url)
                }
                dismiss()
            }

        })
//        val isLinearLayout = if(list.isNotEmpty()) list.any { it!!.length>8 || it.length == 8 } else false
        val isLinearLayout = if (style == "3") true else false
        rvSingleTitle.layoutManager = if (isLinearLayout) LinearLayoutManager(context)  else GridLayoutManager(context, 2)
        rvSingleTitle.adapter = adapter
        rvSingleTitle.addItemDecoration(SpacesItemDecoration(ArmUtils.dip2px(context, 20f), 2))
    }

    interface OnClickSingleChoiceListener{
        fun confirmSingle(choice: String?)
    }
}