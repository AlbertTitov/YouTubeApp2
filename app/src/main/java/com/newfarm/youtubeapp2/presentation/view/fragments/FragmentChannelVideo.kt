@file:Suppress("PrivatePropertyName")

package com.newfarm.youtubeapp2.presentation.view.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar
import com.marshalchen.ultimaterecyclerview.ItemTouchListenerAdapter
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView
import com.newfarm.youtubeapp2.MySingleton
import com.newfarm.youtubeapp2.R
import com.newfarm.youtubeapp2.presentation.Utils
import com.newfarm.youtubeapp2.presentation.view.adapter.AdapterList
import com.newfarm.youtubeapp2.remote.common.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class FragmentChannelVideo : Fragment(), View.OnClickListener {

    private val TAG = FragmentChannelVideo::class.java.simpleName
    private val TAGS = "URL"

    private lateinit var mLblNoResult: TextView
    private lateinit var mLytRetry: LinearLayout
    private lateinit var mPrgLoading: CircleProgressBar
    private lateinit var mUltimateRecyclerView: UltimateRecyclerView

    private var mVideoType: Int? = null
    private var mChannelId: String? = null

    private var mCallback: OnVideoSelectedListener? = null

    private var mAdapterList: AdapterList? = null

    private val mTempVideoData: ArrayList<HashMap<String, String>> = arrayListOf()
    private val mVideoData: ArrayList<HashMap<String, String>> = arrayListOf()

    private var mNextPageToken = ""
    private var mVideoIds = ""
    private var mDuration = "00:00"

    private var mIsStillLoading = true
    private var mIsAppFirstLaunched: Boolean? = null
    private var mIsFirstVideo: Boolean? = null

    interface OnVideoSelectedListener {
        fun onVideoSelected(ID: String)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_video_list, container, false)
        setHasOptionsMenu(true)

        val bundle = this.arguments
        mVideoType = bundle?.getString(TAG_VIDEO_TYPE)?.toIntOrNull()
        mChannelId = bundle?.getString(TAG_CHANNEL_ID)
        mUltimateRecyclerView = view.findViewById(R.id.ultimate_recycler_view)
        mLblNoResult = view.findViewById(R.id.lblNoResult)
        mLytRetry = view.findViewById(R.id.lytRetry)
        mPrgLoading = view.findViewById(R.id.prgLoading)

        val btnRetry = view.findViewById<AppCompatButton>(R.id.raisedRetry)
        btnRetry.setOnClickListener(this)

        mPrgLoading.setColorSchemeResources(R.color.primary_color)
        mPrgLoading.visibility = View.VISIBLE

        mIsAppFirstLaunched = true
        mIsFirstVideo = true

        mAdapterList = AdapterList(activity!!, mVideoData)
        mUltimateRecyclerView.setAdapter(mAdapterList)
        mUltimateRecyclerView.setHasFixedSize(false)

        val linearLayoutManager = LinearLayoutManager(activity)
        mUltimateRecyclerView.layoutManager = linearLayoutManager
        mUltimateRecyclerView.enableLoadmore()

        mAdapterList!!.customLoadMoreView = LayoutInflater.from(activity).inflate(R.layout.progress_bar, null)
        mUltimateRecyclerView.setOnLoadMoreListener { _, _ ->

            if (mIsStillLoading) {
                mIsStillLoading = false
                mAdapterList!!.customLoadMoreView = LayoutInflater.from(activity).inflate(R.layout.progress_bar, null)

                val handler = Handler()
                handler.postDelayed({
                    getVideoData()
                }, 1000)
            }
            else {
                disableLoadMore()
            }
        }

        val itemTouchListenerAdapter = ItemTouchListenerAdapter(mUltimateRecyclerView.mRecyclerView,
            object : ItemTouchListenerAdapter.RecyclerViewOnItemClickListener {

            override fun onItemLongClick(parent: RecyclerView?, clickedView: View?, position: Int) {}

            override fun onItemClick(parent: RecyclerView?, clickedView: View?, position: Int) {
                if (position < mVideoData.size) {
                    mCallback?.onVideoSelected(mVideoData[position][KEY_VIDEO_ID]!!)
                }
            }
        })

        mUltimateRecyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter)

        getVideoData()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            mCallback = activity as OnVideoSelectedListener
        }
        catch (e : ClassCastException) {
            throw ClassCastException(activity.toString()
                    + " must implement OnVideoSelectedListener")
        }
    }

    private fun getVideoData() {
        mVideoIds = ""
        val videoId: Array<String> = arrayOf("")

        val url: String = if (mVideoType == 2) {
            "$BASE_URL$FUNCTION_PLAYLIST_ITEMS_YOUTUBE${PARAM_PART_YOUTUBE}snippet,id&" +
                    "${PARAM_FIELD_PLAYLIST_YOUTUBE}&$PARAM_KEY_YOUTUBE${YOUTUBE_APIKEY}&" +
                    "$PARAM_PLAYLIST_ID_YOUTUBE${mChannelId}&$PARAM_PAGE_TOKEN_YOUTUBE" +
                    "${mNextPageToken}&$PARAM_MAX_RESULT_YOUTUBE$PARAM_RESULT_PER_PAGE"

        } else {
            "$BASE_URL$FUNCTION_SEARCH_YOUTUBE${PARAM_PART_YOUTUBE}snippet,id&${PARAM_ORDER_YOUTUBE}" +
                    "&${PARAM_TYPE_YOUTUBE}&${PARAM_FIELD_SEARCH_YOUTUBE}&$PARAM_KEY_YOUTUBE" +
                    "${YOUTUBE_APIKEY}&$PARAM_CHANNEL_ID_YOUTUBE${mChannelId}&" +
                    "$PARAM_PAGE_TOKEN_YOUTUBE${mNextPageToken}&$PARAM_MAX_RESULT_YOUTUBE$PARAM_RESULT_PER_PAGE"
        }
        Log.d(TAGS, url)

        val request = JsonObjectRequest(url, null, object : Response.Listener<JSONObject> {

            var dataItemArray: JSONArray? = null
            var itemIdObject: JSONObject? = null
            var itemSnippetObject: JSONObject? = null
            var itemSnippetThumbnailsObject: JSONObject? = null
            var itemSnippetResourceIdObject: JSONObject? = null

            override fun onResponse(response: JSONObject?) {

                if (activity != null && isAdded) {
                    try {
                        dataItemArray = response?.getJSONArray(ARRAY_ITEMS)
                        if (dataItemArray != null && dataItemArray!!.length() > 0) {
                            haveResultView()
                            for (i in 0 until dataItemArray!!.length()) {
                                val dataMap = HashMap<String, String>()
                                val itemsObject = dataItemArray!!.getJSONObject(i)
                                itemSnippetObject = itemsObject.getJSONObject(OBJECT_ITEMS_SNIPPET)

                                if (mVideoType == 2) {
                                    itemSnippetResourceIdObject = itemSnippetObject?.getJSONObject(OBJECT_ITEMS_SNIPPET_RESOURCEID)
                                    if (itemSnippetResourceIdObject != null) {
                                        dataMap[KEY_VIDEO_ID] = itemSnippetResourceIdObject!!.getString(KEY_VIDEO_ID)
                                        videoId[0] = itemSnippetResourceIdObject!!.getString(KEY_VIDEO_ID)
                                    }
                                    mVideoIds += itemSnippetResourceIdObject?.getString(KEY_VIDEO_ID) + ","
                                }
                                else {
                                    itemIdObject = itemsObject.getJSONObject(OBJECT_ITEMS_ID)
                                    if (itemIdObject != null) {
                                        dataMap[KEY_VIDEO_ID] = itemIdObject!!.getString(KEY_VIDEO_ID)
                                        videoId[0] = itemIdObject!!.getString(KEY_VIDEO_ID)
                                        mVideoIds += itemIdObject!!.getString(KEY_VIDEO_ID) + ","
                                    }
                                }
                                if (mIsFirstVideo != null && mIsFirstVideo!! && i == 0) {
                                    mIsFirstVideo = false
                                    mCallback?.onVideoSelected(videoId[0])
                                }
                                dataMap[KEY_TITLE] = itemSnippetObject!!.getString(KEY_TITLE)
                                val formattedPublishedDate = Utils.formatPublishedDate(activity!!, itemSnippetObject!!.getString(KEY_PUBLISHEDAT))
                                if (formattedPublishedDate != null) {
                                    dataMap[KEY_PUBLISHEDAT] = formattedPublishedDate
                                }
                                itemSnippetThumbnailsObject = itemSnippetObject!!.getJSONObject(OBJECT_ITEMS_SNIPPET_THUMBNAILS)
                                itemSnippetThumbnailsObject = itemSnippetThumbnailsObject!!.getJSONObject(OBJECT_ITEMS_SNIPPET_THUMBNAILS_MEDIUM)
                                dataMap[KEY_URL_THUMBNAILS] = itemSnippetThumbnailsObject!!.getString(KEY_URL_THUMBNAILS)
                                mTempVideoData.add(dataMap)
                            }
                            getDuration()
                            if (dataItemArray != null && dataItemArray!!.length() == PARAM_RESULT_PER_PAGE && response != null) {
                                mNextPageToken = response.getString(ARRAY_PAGE_TOKEN)
                            }
                            else {
                                mNextPageToken = ""
                                disableLoadMore()
                            }
                            mIsAppFirstLaunched = false
                        }
                        else {
                            if (mIsAppFirstLaunched != null && mIsAppFirstLaunched!! && mAdapterList!!.adapterItemCount <= 0) {
                                noResultView()
                            }
                            disableLoadMore()
                        }
                    } catch (e: JSONException) {
                        Log.d(TAG_FANDROID + TAG, "JSON parsing error: " + e.message)
                        mPrgLoading.visibility = View.GONE
                    }
                    mPrgLoading.visibility = View.GONE
                }
            }

        }, Response.ErrorListener { error ->
            if (activity != null && isAdded) {
                Log.d(TAG_FANDROID + TAG, "on Error Response: " + error?.message)

                try {
                    val msgSnackBar: String = if (error is NoConnectionError) {
                        resources.getString(R.string.no_internet_connection)
                    } else {
                        resources.getString(R.string.response_error)
                    }
                    if (mVideoData.size == 0) {
                        retryView()
                    }
                    Utils.showSnackBar(activity!!, msgSnackBar)
                    mPrgLoading.visibility = View.GONE
                } catch (e: Exception) {
                    Log.d(TAG_FANDROID + TAG, "failed catch volley $e")
                    mPrgLoading.visibility = View.GONE
                }
            }
        })
        request.retryPolicy =
            DefaultRetryPolicy(ARG_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        MySingleton.getInstance(activity!!).requestQueue.add(request)
    }

    private fun getDuration() {
        val url = BASE_URL + FUNCTION_VIDEO_YOUTUBE + PARAM_PART_YOUTUBE + "contentDetails&" + PARAM_FIELD_VIDEO_YOUTUBE + "&" +
                PARAM_KEY_YOUTUBE + YOUTUBE_APIKEY + "&" + PARAM_VIDEO_ID_YOUTUBE + mVideoIds

        val request = JsonObjectRequest(url,
            Response.Listener<JSONObject> { response ->




                if (activity != null && isAdded) {
                    try {
                        haveResultView()
                        val dataItemArrays: JSONArray? = response?.getJSONArray(ARRAY_ITEMS)
                        if (dataItemArrays != null && dataItemArrays.length() > 0 && mTempVideoData.isNotEmpty()) {
                            for (i in 0 until dataItemArrays.length()) {
                                val dataMap = HashMap<String, String>()
                                val itemsObjects = dataItemArrays.getJSONObject(i)

                                val itemContentObject = itemsObjects.getJSONObject(OBJECT_ITEMS_CONTENT_DETAIL)
                                mDuration = itemContentObject.getString(KEY_DURATION)

                                val mDurationInTimeFormat = Utils.getTimeFromString(mDuration)

                                dataMap[KEY_DURATION] = mDurationInTimeFormat
                                dataMap[KEY_URL_THUMBNAILS] = mTempVideoData[i][KEY_URL_THUMBNAILS]!!
                                dataMap[KEY_TITLE] = mTempVideoData[i][KEY_TITLE]!!
                                dataMap[KEY_VIDEO_ID] = mTempVideoData[i][KEY_VIDEO_ID]!!
                                dataMap[KEY_PUBLISHEDAT] = mTempVideoData[i][KEY_PUBLISHEDAT]!!

                                mVideoData.add(dataMap)
                                mAdapterList?.notifyItemInserted(mVideoData.size)
                            }
                            mIsStillLoading = true

                            mTempVideoData.clear()
                        } else {
                            if (mIsAppFirstLaunched!! && mAdapterList!!.adapterItemCount <= 0) {
                                noResultView()
                            }
                            disableLoadMore()
                        }
                    } catch (e: JSONException) {
                        Log.d(TAG_FANDROID + TAG, "JSON Parsing error: " + e.message)
                        mPrgLoading.visibility = View.GONE
                    }
                    mPrgLoading.visibility = View.GONE
                }
            },
            Response.ErrorListener { error ->
                if (activity != null && isAdded) {
                    Log.d(TAG_FANDROID + TAG, "on Error Response: " + error?.message)
                    try {
                        val msgSnackBar: String = if (error is NoConnectionError) {
                            resources.getString(R.string.no_internet_connection)
                        } else {
                            resources.getString(R.string.response_error)
                        }
                        if (mVideoData.size == 0) {
                            retryView()
                        }
                        activity?.let { Utils.showSnackBar(it, msgSnackBar) }
                        mPrgLoading.visibility = View.GONE

                    } catch (e: Exception) {
                        Log.d(TAG_FANDROID + TAG, "failed catch volley $e")
                        mPrgLoading.visibility = View.GONE
                    }
                }
            })

        request.retryPolicy = DefaultRetryPolicy(ARG_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        MySingleton.getInstance(activity!!).requestQueue.add(request)
    }

    private fun retryView() {
        mLytRetry.visibility = View.VISIBLE
        mUltimateRecyclerView.visibility = View.GONE
        mLblNoResult.visibility = View.GONE
    }

    private fun haveResultView() {
        mLytRetry.visibility = View.GONE
        mUltimateRecyclerView.visibility = View.VISIBLE
        mLblNoResult.visibility = View.GONE
    }

    private fun noResultView() {
        mLytRetry.visibility = View.GONE
        mUltimateRecyclerView.visibility = View.GONE
        mLblNoResult.visibility = View.VISIBLE
    }

    private fun disableLoadMore() {
        mIsStillLoading = false
        if (mUltimateRecyclerView.isLoadMoreEnabled) {
            //mUltimateRecyclerView.disableLoadmore()
        }
        mAdapterList?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id) {
                R.id.raisedRetry -> {
                    mPrgLoading.visibility = View.VISIBLE
                    haveResultView()
                    getVideoData()
                }
                else -> {}
            }
        }
    }
}