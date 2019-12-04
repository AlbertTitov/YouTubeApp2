package com.newfarm.youtubeapp2.presentation.view.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.YouTubeApiServiceUtil
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.newfarm.youtubeapp2.R
import com.newfarm.youtubeapp2.presentation.view.fragments.FragmentChannelVideo
import com.newfarm.youtubeapp2.presentation.view.fragments.FragmentVideo
import com.newfarm.youtubeapp2.remote.common.TAG_CHANNEL_ID
import com.newfarm.youtubeapp2.remote.common.TAG_VIDEO_TYPE

class HomeActivity : AppCompatActivity(), YouTubePlayer.OnFullscreenListener,
    FragmentChannelVideo.OnVideoSelectedListener {

    companion object {
        private const val LANDSCAPE_VIDEO_PADDING_DP = 5
        private const val RECOVERY_DIALOG_REQUEST = 1
    }

    private var fragmentVideo: FragmentVideo? = null
    private var isFullscreen: Boolean = false

    private lateinit var drawer: Drawer
    private lateinit var toolbar: Toolbar
    private lateinit var decorView: View

    private var channelNames: Array<String>? = null
    private var channelId: Array<String>? = null
    private var videoTypes: Array<String>? = null

    private var selectedDrawerItem: Int = 0

    private lateinit var layoutList: FrameLayout

    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.home_activity)
        layoutList = findViewById(R.id.fragment_container)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        decorView = window.decorView
        fragmentVideo = fragmentManager.findFragmentById(R.id.video_fragment_container) as FragmentVideo

        channelNames = resources.getStringArray(R.array.channel_names)
        channelId = resources.getStringArray(R.array.channel_id)
        videoTypes = resources.getStringArray(R.array.video_types)

        checkYouTubeApi()

        val primaryDrawerItems: ArrayList<IDrawerItem<*>> = arrayListOf()

        for (i in 0 until channelId!!.size - 1) {
            val name = channelNames!![i]
            primaryDrawerItems.add(
                PrimaryDrawerItem()
                    .withName(name)
                    .withIdentifier(i)
                    .withSelectable(true))
        }

        val accountHeader = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.drawable.header)
            .build()

        drawer = DrawerBuilder(this)
            .withActivity(this)
            .withAccountHeader(accountHeader)
            .withDisplayBelowStatusBar(true)
            .withActionBarDrawerToggleAnimated(true)
            .withSavedInstance(savedInstanceState)
            .withDrawerItems(primaryDrawerItems)
            .addStickyDrawerItems(
                SecondaryDrawerItem()
                    .withName(getString(R.string.about))
                    .withIdentifier(channelId!!.size - 1)
                    .withSelectable(false)
            )
            .withOnDrawerItemClickListener { _, position, drawerItem ->
                selectedDrawerItem = position
                if (drawerItem != null) {
                    if (drawerItem.identifier >= 0 && selectedDrawerItem != -1) {

                        setToolbarAndSelectedDrawerItem(channelNames!![selectedDrawerItem-1], (selectedDrawerItem-1))

                        val bundle = Bundle()
                        bundle.putString(
                            TAG_VIDEO_TYPE,
                            videoTypes!![selectedDrawerItem-1])
                        bundle.putString(TAG_CHANNEL_ID,
                            channelId!![selectedDrawerItem-1])

                        fragment = FragmentChannelVideo()
                        fragment.arguments = bundle

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit()
                    } else if (selectedDrawerItem == -1) {
                        val aboutIntent = Intent(
                            applicationContext,
                            AboutActivity::class.java)
                        startActivity(aboutIntent)
                        overridePendingTransition(R.anim.open_next, R.anim.close_main)
                    }
                }
                false
            }
            .withSavedInstance(savedInstanceState)
            .withShowDrawerOnFirstLaunch(true)
            .build()

        setToolbarAndSelectedDrawerItem(channelNames!![0], 0)

        val bundle = Bundle()
        bundle.putString(
            TAG_VIDEO_TYPE,
            videoTypes!![selectedDrawerItem]
        )
        bundle.putString(
            TAG_CHANNEL_ID,
            channelId!![selectedDrawerItem]
        )

        fragment = FragmentChannelVideo()
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        supportFragmentManager.addOnBackStackChangedListener {
            val f = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (f != null) {
                updateTitleAndDrawer(f)
            }
        }

        if (savedInstanceState == null) {
            drawer.setSelection(0, false)
        }
    }

    private fun checkYouTubeApi() {
        val errorReason = YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this)
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            val errorMessage = String.format(
                getString(R.string.error_player),
                errorReason.toString()
            )
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun setToolbarAndSelectedDrawerItem(title: String, selectedDrawerItem: Int) {
        toolbar.title = title
        drawer.setSelection(selectedDrawerItem, false)
    }

    private fun updateTitleAndDrawer(mFragment: Fragment) {
        val fragClassName = mFragment.javaClass.name

        if (fragClassName == FragmentChannelVideo::class.java.name) {
            setToolbarAndSelectedDrawerItem(
                channelNames!![selectedDrawerItem],
                selectedDrawerItem
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuAbout -> {
                val aboutIntent = Intent(
                    applicationContext,
                    AboutActivity::class.java
                )
                startActivity(aboutIntent)
                overridePendingTransition(R.anim.open_next, R.anim.close_main)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            recreate()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        layout()
    }

    override fun onFullscreen(isFullscreen: Boolean) {
        this.isFullscreen = isFullscreen
        layout()
    }


    private fun layout() {
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        when {
            isFullscreen -> {
                toolbar.visibility = View.GONE
                layoutList.visibility = View.GONE
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                setLayoutSize(fragmentVideo!!.view!!,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )


            }
            isPortrait -> {
                toolbar.visibility = View.VISIBLE
                layoutList.visibility = View.VISIBLE
                setLayoutSize(fragmentVideo!!.view!!,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            else -> {
                toolbar.visibility = View.VISIBLE
                layoutList.visibility = View.VISIBLE
                val screenWidth = dpToPx(resources.configuration.screenWidthDp)
                val videoWidth = screenWidth - screenWidth / 4 - dpToPx(LANDSCAPE_VIDEO_PADDING_DP)
                setLayoutSize(fragmentVideo!!.view!!, videoWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
    }

    override fun onVideoSelected(ID: String) {
        val fragmentVideo = fragmentManager.findFragmentById(R.id.video_fragment_container) as FragmentVideo
        fragmentVideo.setVideoId(ID)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun setLayoutSize(view: View, width: Int, height: Int) {
        val params = view.layoutParams
        params.width = width
        params.height = height
        view.layoutParams = params
    }

    override fun onBackPressed() {
        if (isFullscreen) {
            fragmentVideo!!.backnormal()
        } else {
            super.onBackPressed()
        }
    }
}
