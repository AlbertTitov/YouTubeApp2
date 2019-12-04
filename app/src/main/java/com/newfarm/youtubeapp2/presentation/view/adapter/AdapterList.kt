package com.newfarm.youtubeapp2.presentation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.ImageLoader
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter
import com.marshalchen.ultimaterecyclerview.animators.internal.ViewHelper
import com.newfarm.youtubeapp2.MySingleton
import com.newfarm.youtubeapp2.R
import com.newfarm.youtubeapp2.remote.common.KEY_DURATION
import com.newfarm.youtubeapp2.remote.common.KEY_PUBLISHEDAT
import com.newfarm.youtubeapp2.remote.common.KEY_TITLE
import com.newfarm.youtubeapp2.remote.common.KEY_URL_THUMBNAILS
import java.util.*

class AdapterList(
    context: Context,
    list: ArrayList<HashMap<String, String>>
) : UltimateViewAdapter<RecyclerView.ViewHolder>() {
    private val DATA: ArrayList<HashMap<String, String>> = list
    private val imageLoader: ImageLoader = MySingleton.getInstance(context).imageLoader
    private val interpolator: Interpolator = LinearInterpolator()
    private var lastPosition = 5
    private val ANIMATION_DURATION = 300
    override fun getViewHolder(view: View): RecyclerView.ViewHolder {
        return UltimateRecyclerviewViewHolder(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_video_list, parent, false)
        return ViewHolder(v)
    }

    override fun getAdapterItemCount(): Int {
        return DATA.size
    }

    override fun generateHeaderId(position: Int): Long {
        return 0
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (position < itemCount && (if (customHeaderView != null) position <= DATA.size else position < DATA.size) && (customHeaderView == null || position > 0)) {
            val item: HashMap<String, String> = DATA[if (customHeaderView != null) position - 1 else position]
            // Set data to the view
            (holder as ViewHolder).txtTitle.text = item[KEY_TITLE]
            holder.txtDuration.text = item[KEY_DURATION]
            holder.txtPublished.text = item[KEY_PUBLISHEDAT]
            // Set image to imageview
            imageLoader[item[KEY_URL_THUMBNAILS], ImageLoader.getImageListener(
                holder.imgThumbnail,
                R.mipmap.ic_launcher, R.mipmap.ic_launcher
            )]
        }
        val isFirstOnly = true
        if (!isFirstOnly || position > lastPosition) { // Add animation to the item
            for (anim in getAdapterAnimations(
                holder.itemView,
                AdapterAnimationType.SlideInLeft
            )) {
                anim.setDuration(ANIMATION_DURATION.toLong()).start()
                anim.interpolator = interpolator
            }
            lastPosition = position
        } else {
            ViewHelper.clear(holder.itemView)
        }
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder? {
        return null
    }

    override fun onBindHeaderViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
    }

    class ViewHolder(v: View) : UltimateRecyclerviewViewHolder(v) {
        val txtTitle: TextView = v.findViewById<View>(R.id.txtTitle) as TextView
        val txtPublished: TextView = v.findViewById<View>(R.id.txtPublishedAt) as TextView
        val txtDuration: TextView = v.findViewById<View>(R.id.txtDuration) as TextView
        val imgThumbnail: ImageView = v.findViewById<View>(R.id.imgThumbnail) as ImageView
    }

}