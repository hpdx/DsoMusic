package com.dirror.music.ui.home.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import coil.size.ViewSizeResolver
import coil.transform.CircleCropTransformation
import com.dirror.music.MyApplication
import com.dirror.music.adapter.MyPlaylistAdapter
import com.dirror.music.data.PlaylistData
import com.dirror.music.databinding.FragmentMyBinding
import com.dirror.music.ui.activity.LocalMusicActivity
import com.dirror.music.ui.activity.PlayHistoryActivity
import com.dirror.music.ui.activity.UserCloudActivity
import com.dirror.music.ui.base.BaseFragment
import com.dirror.music.ui.playlist.SongPlaylistActivity
import com.dirror.music.ui.playlist.TAG_LOCAL_MY_FAVORITE
import com.dirror.music.ui.home.MainViewModel
import com.dirror.music.util.*
import com.dirror.music.util.extensions.dp

/**
 * 我的
 */
class MyFragment : BaseFragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    // activity 和 fragment 共享数据
    private val mainViewModel: MainViewModel by activityViewModels()
    private val myFragmentViewModel: MyFragmentViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun initView() {

    }

    override fun initListener() {
        binding.apply {
            clUser.setOnClickListener {
                AnimationUtil.click(it)
                if (MyApplication.userManager.getCurrentUid() == 0L) {
                    MyApplication.activityManager.startLoginActivity(requireActivity())
                } else {
                    MyApplication.activityManager.startUserActivity(
                        requireActivity(),
                        MyApplication.userManager.getCurrentUid()
                    )
                }
            }
            // 我喜欢的音乐
            clFavorite.setOnClickListener {
                AnimationUtil.click(it)
                val intent = Intent(this@MyFragment.context, SongPlaylistActivity::class.java).apply {
                    putExtra(SongPlaylistActivity.EXTRA_TAG, TAG_LOCAL_MY_FAVORITE)
//                    putExtra(PlaylistActivity2.EXTRA_LONG_PLAYLIST_ID, 0L)
//                    putExtra(PlaylistActivity2.EXTRA_INT_TAG, PLAYLIST_TAG_MY_FAVORITE)
                }
                startActivity(intent)
            }
            // 新建歌单
            clNewPlaylist.setOnClickListener {
                toast("功能开发中，敬请期待")
            }
            // 本地音乐
            clLocal.setOnClickListener {
                AnimationUtil.click(it)
                val intent = Intent(this@MyFragment.context, LocalMusicActivity::class.java)
                startActivity(intent)
            }
            // 播放历史
            clLatest.setOnClickListener {
                AnimationUtil.click(it)
                val intent = Intent(this@MyFragment.context, PlayHistoryActivity::class.java)
                startActivity(intent)
            }
            clPersonalFM.setOnClickListener {
                AnimationUtil.click(it)
                if (MyApplication.userManager.hasCookie()) {
                    if (MyApplication.musicController.value?.personFM?.value != true) {
                        MyApplication.musicController.value?.setPersonFM(true)
                        MyApplication.activityManager.startPlayerActivity(requireActivity())
                    } else {
                        MyApplication.activityManager.startPlayerActivity(requireActivity())
                    }
                } else {
                    ErrorCode.toast(ErrorCode.ERROR_NOT_COOKIE)
                }
            }
            // 用户云盘
            clUserCloud.setOnClickListener {
                AnimationUtil.click(it)
                if (MyApplication.userManager.hasCookie()) {
                    startActivity(Intent(requireContext(), UserCloudActivity::class.java))
                } else {
                    ErrorCode.toast(ErrorCode.ERROR_NOT_COOKIE)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initObserver() {
        mainViewModel.userId.observe(viewLifecycleOwner, {
            // 清空歌单
            myFragmentViewModel.clearPlaylist()
            // toast("更新歌单")
            myFragmentViewModel.updatePlaylist()
        })
        // 用户歌单的观察
        myFragmentViewModel.userPlaylistList.observe(viewLifecycleOwner, {
            setPlaylist(it)
        })
        mainViewModel.statusBarHeight.observe(viewLifecycleOwner, {
            (binding.clUser.layoutParams as LinearLayout.LayoutParams).apply {
                topMargin = it + 56.dp() + 8.dp()
                // setMargins(0, it + 56.dp(), 0, 0)
            }
        })
        mainViewModel.userId.observe(viewLifecycleOwner, { userId ->
            if (userId == 0L) {
                binding.tvUserName.text = "立即登录"
            } else {
                MyApplication.cloudMusicManager.getUserDetail(userId, {
                    runOnMainThread {
                        binding.ivUser.load(it.profile.avatarUrl) {
                             transformations(CircleCropTransformation())
                             size(ViewSizeResolver(binding.ivUser))
                            // error(R.drawable.ic_song_cover)
                        }
                        binding.tvUserName.text = it.profile.nickname
                        binding.tvLevel.text = "Lv.${it.level}"
                    }
                }, {

                })
            }
        })
        mainViewModel.singleColumnPlaylist.observe(viewLifecycleOwner, {
            val count = if (it) {
                1
            } else {
                2
            }
            binding.rvPlaylist.layoutManager = GridLayoutManager(this.context, count)
        })
        mainViewModel.neteaseLiveVisibility.observe(viewLifecycleOwner, {
            if (it) {
                binding.clUserCloud.visibility = View.VISIBLE
                binding.clPersonalFM.visibility = View.VISIBLE
            } else {
                binding.clUserCloud.visibility = View.GONE
                binding.clPersonalFM.visibility = View.GONE
            }

        })
    }

    /**
     * 设置歌单
     */
    private fun setPlaylist(playlist: ArrayList<PlaylistData>) {
        runOnMainThread {
            binding.rvPlaylist.adapter = activity?.let { it1 -> MyPlaylistAdapter(playlist, it1) }
        }
    }

}