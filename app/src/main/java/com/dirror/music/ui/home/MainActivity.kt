package com.dirror.music.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dirror.music.MyApplication
import com.dirror.music.R
import com.dirror.music.databinding.ActivityMainBinding
import com.dirror.music.manager.ActivityCollector
import com.dirror.music.ui.activity.AboutActivity
import com.dirror.music.ui.activity.FeedbackActivity
import com.dirror.music.ui.activity.SearchActivity
import com.dirror.music.ui.activity.SettingsActivity
import com.dirror.music.ui.base.BaseActivity
import com.dirror.music.ui.home.fragment.HomeFragment
import com.dirror.music.ui.home.fragment.MyFragment
import com.dirror.music.util.*
import com.dirror.music.util.cache.ACache
import com.dirror.music.util.extensions.dp
import com.google.android.material.tabs.TabLayoutMediator
import eightbitlab.com.blurview.RenderScriptBlur
import kotlin.concurrent.thread

/**
 * MainActivity
 */
class MainActivity : BaseActivity() {

    companion object {
        private const val ACTION_LOGIN = "com.dirror.music.LOGIN"
    }

    private lateinit var binding: ActivityMainBinding

    /* 登录广播接受 */
    private lateinit var loginReceiver: LoginReceiver

    /* 设置改变广播接收 */
    private lateinit var settingsChangeReceiver: SettingsChangeReceiver

    private val mainViewModel: MainViewModel by viewModels()

    override fun initBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        miniPlayer = binding.miniPlayer
        setContentView(binding.root)
    }

    override fun initData() {
        // Intent 过滤器
        var intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_LOGIN)
        loginReceiver = LoginReceiver()
        registerReceiver(loginReceiver, intentFilter)

        intentFilter = IntentFilter()
        intentFilter.addAction(SettingsActivity.ACTION)
        settingsChangeReceiver = SettingsChangeReceiver()
        registerReceiver(settingsChangeReceiver, intentFilter)

        // 检查新版本
        UpdateUtil.checkNewVersion(this, false)
    }

    override fun initView() {
        mainViewModel.updateUI()
        thread {
            ACache.get(this).getAsBitmap(Config.APP_THEME_BACKGROUND)?.let {
                runOnMainThread {
                    binding.ivTheme.setImageBitmap(it)
                    val pixelsPair = ScreenUtil.getDisplayPixels()

                }
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            window.allowEnterTransitionOverlap = true
            window.allowReturnTransitionOverlap = true
        }

        val radius = 20f
        val decorView: View = window.decorView
        val windowBackground: Drawable = decorView.background
        binding.blurViewTop.setupWith(decorView.findViewById(R.id.clTheme))
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setHasFixedTransformationMatrix(true)
        binding.blurViewPlay.setupWith(decorView.findViewById(R.id.clTheme))
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setHasFixedTransformationMatrix(true)

        // 适配状态栏
        val statusBarHeight = getStatusBarHeight(window, this) // px
        mainViewModel.statusBarHeight.value = statusBarHeight
        (binding.blurViewTop.layoutParams as ConstraintLayout.LayoutParams).apply {
            height = 56.dp() + statusBarHeight
        }
        (binding.titleBar.layoutParams as ConstraintLayout.LayoutParams).apply {
            topMargin = statusBarHeight
        }
        // 侧滑状态栏适配
        (binding.menuMain.cvOthers.layoutParams as LinearLayout.LayoutParams).apply {
            topMargin = statusBarHeight + 8.dp()
        }


        binding.viewPager2.offscreenPageLimit = 2
        binding.viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> MyFragment()
                    else -> HomeFragment()
                }
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.my)
                else -> getString(R.string.home)
            }
        }.attach()

        ViewPager2Util.changeToNeverMode(binding.viewPager2)

        updateView()
    }

    override fun initListener() {
        with(binding) {
            // 搜索按钮
            ivSearch.setOnClickListener {
                startActivity(Intent(this@MainActivity, SearchActivity::class.java))
            }
            // 设置按钮
            ivSettings.setOnClickListener {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            // 侧滑
            with(menuMain) {
                itemSponsor.setOnClickListener {
                    // startActivity(Intent(this@MainActivity, SponsorActivity::class.java))
                    MyApplication.activityManager.startWebActivity(this@MainActivity, AboutActivity.SPONSOR)
                }
                itemSwitchAccount.setOnClickListener {
                    MyApplication.activityManager.startLoginActivity(this@MainActivity)
                }
                itemSettings.setOnClickListener {
                    MyApplication.activityManager.startSettingsActivity(this@MainActivity)
                }
                itemPrivateLetter.setOnClickListener {
                    val cookie = MyApplication.userManager.getCloudMusicCookie()
                    if (cookie != "") {
                        MyApplication.activityManager.startPrivateLetterActivity(this@MainActivity)
                    } else {
                        toast("当前为离线模式，请登录")
                    }
                }
                // 反馈
                itemFeedback.setOnClickListener {
                    startActivity(Intent(this@MainActivity, FeedbackActivity::class.java))
                }
                itemAbout.setOnClickListener {
                    startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                }
                itemExitApp.setOnClickListener {
                    MyApplication.musicController.value?.stopMusicService()
                    ActivityCollector.finishAll()

                    object : Thread() {
                        override fun run() {
                            super.run()
                            sleep(500)
                            Secure.killMyself()
                        }
                    }.start()

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 解绑广播接收
        unregisterReceiver(loginReceiver)
        unregisterReceiver(settingsChangeReceiver)
    }

    inner class LoginReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 通知 viewModel
            mainViewModel.setUserId()
        }
    }

    inner class SettingsChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mainViewModel.updateUI()
            updateView()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.close()
        } else {
            super.onBackPressed()
        }
    }

    private var startX = 0
    private var startY = 0
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = ev.x.toInt()
                startY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = ev.x.toInt()
                val endY = ev.y.toInt()
                val disX = kotlin.math.abs(endX - startX)
                val disY = kotlin.math.abs(endY - startY)
                if (disX < disY) {
                    // 禁止 ViewPager2
                    binding.viewPager2.isUserInputEnabled = false
                }
            }
            MotionEvent.ACTION_UP -> {
                startX = 0
                startY = 0
                // 恢复
                binding.viewPager2.isUserInputEnabled = true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 更新界面
     */
    private fun updateView() {
        // 适配导航栏
        val navigationBarHeight = if (MyApplication.mmkv.decodeBool(Config.PARSE_NAVIGATION, true)) {
            getNavigationBarHeight(this)
        } else {
            0
        }

        (binding.miniPlayer.root.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomMargin = navigationBarHeight
        }
        (binding.blurViewPlay.layoutParams as ConstraintLayout.LayoutParams).apply {
            height = 52.dp() + navigationBarHeight
        }
    }

}