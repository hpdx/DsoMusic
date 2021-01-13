package com.dirror.music.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dirror.music.MyApplication
import com.dirror.music.databinding.ActivitySettingsBinding
import com.dirror.music.util.*

/**
 * 设置 Activity
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initView()
        initListener()
    }
    
    private fun initData() {

    }

    private fun initView() {
        // 按钮
        binding.apply {
            switchPlayOnMobile.isChecked = MyApplication.mmkv.decodeBool(Config.PLAY_ON_MOBILE, false)
            switchPauseSongAfterUnplugHeadset.isChecked = MyApplication.mmkv.decodeBool(Config.PAUSE_SONG_AFTER_UNPLUG_HEADSET, true)
            switchSkipErrorMusic.isChecked = MyApplication.mmkv.decodeBool(Config.SKIP_ERROR_MUSIC, true)
            switchFilterRecord.isChecked = MyApplication.mmkv.decodeBool(Config.FILTER_RECORD, true)
            switchLocalMusicParseLyric.isChecked = MyApplication.mmkv.decodeBool(Config.PARSE_INTERNET_LYRIC_LOCAL_MUSIC, true)
        }

        if (!Secure.isDebug()) {
            binding.itemTestCookie.visibility = View.GONE
        }

    }

    private fun initListener() {
        binding.apply {
            itemFeedback.setOnClickListener {
                startActivity(Intent(this@SettingsActivity, FeedbackActivity::class.java))
            }
            itemSourceCode.setOnClickListener {
                openUrlByBrowser(this@SettingsActivity, "https://github.com/Moriafly/dirror-music")
            }

            // Kart Jim
            itemJim.setOnClickListener {
                MyApplication.activityManager.startWebActivity(this@SettingsActivity, "https://moriafly.xyz/MoreAndroid/#/")
                // openJim(this@SettingsActivity)
            }
            // Cookie 导出
            itemTestCookie.setOnClickListener {
                if (Secure.isDebug()) {
                    val cookie = MyApplication.userManager.getCloudMusicCookie()
                    if (cookie != "") {
                        toast("Cookie 存在，是否过时未知，已经导入剪贴板")
                        copyToClipboard(this@SettingsActivity, cookie)
                    } else {
                        toast("Cookie 不存在")
                    }
                } else {
                    toast("非开发版")
                }
            }

            itemFilterRecord.setOnClickListener { switchFilterRecord.isChecked = !switchFilterRecord.isChecked }
            switchFilterRecord.setOnCheckedChangeListener { _, isChecked ->
                MyApplication.mmkv.encode(Config.FILTER_RECORD, isChecked)
            }

            itemLocalMusicParseLyric.setOnClickListener { switchLocalMusicParseLyric.isChecked = !switchLocalMusicParseLyric.isChecked }
            switchLocalMusicParseLyric.setOnCheckedChangeListener { _, isChecked ->
                MyApplication.mmkv.encode(Config.PARSE_INTERNET_LYRIC_LOCAL_MUSIC, isChecked)
            }

            itemSkipErrorMusic.setOnClickListener { switchSkipErrorMusic.isChecked = !switchSkipErrorMusic.isChecked }
            switchSkipErrorMusic.setOnCheckedChangeListener { _, isChecked ->
                MyApplication.mmkv.encode(Config.SKIP_ERROR_MUSIC, isChecked)
            }

            itemPlayOnMobile.setOnClickListener {
                binding.switchPlayOnMobile.isChecked = !binding.switchPlayOnMobile.isChecked
            }

            switchPlayOnMobile.setOnCheckedChangeListener { _, isChecked ->
                MyApplication.mmkv.encode(Config.PLAY_ON_MOBILE, isChecked)
            }

            itemPauseSongAfterUnplugHeadset.setOnClickListener {
                binding.switchPauseSongAfterUnplugHeadset.isChecked = !binding.switchPauseSongAfterUnplugHeadset.isChecked
            }

            switchPauseSongAfterUnplugHeadset.setOnCheckedChangeListener { _, isChecked ->
                MyApplication.mmkv.encode(Config.PAUSE_SONG_AFTER_UNPLUG_HEADSET, isChecked)
            }

            itemAbout.setOnClickListener {
                startActivity(Intent(this@SettingsActivity, AboutActivity::class.java))
            }

        }



    }

}