package com.netease.nim.samples

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.multidex.MultiDex
import com.netease.nim.samples.utils.CutoutHelper
import com.netease.nim.samples.utils.LogUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.util.NIMUtil
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginDetailListener
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginListener
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMKickedOfflineReason
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientChange
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient


class CustomApplication : Application() {

    companion object {
        private const val TAG = "CustomApplication"
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        //主进程中初始化
        if (NIMUtil.isMainProcess(this)){

            // 全局处理刘海屏适配
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    // 自动为所有Activity设置安全区域
                    CutoutHelper.setupSafeAreaForNormalScreen(activity)
                }

                // 其他方法为空实现
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })

            initIMSDK()
            setupLoginListeners()
        }
    }

    private fun initIMSDK(){
        val sdkOption = SDKOptions()
        // 设置云信平台申请的appKey
        sdkOption.appKey = BuildConfig.APP_KEY
        //设置sdk缓存目标，包含sdk日志，sdk下载的缩略图，文件等，如果不设置，sdk缓存目录默认设置在内部存储中，无法使用文件管理器查看，建议设置在外部私有目录中
        sdkOption.sdkStorageRootPath = "${externalCacheDir?.getCanonicalPath()}/nim"
        // 配置是否需要预下载附件缩略图，默认true
        sdkOption.preloadAttach = false
        // 配置附件缩略图的尺寸大小
        sdkOption.thumbnailSize = (165.0 / 320.0 * resources.displayMetrics.widthPixels).toInt()
        // 在线多端同步未读数，使用未读数功能时必须开启
        sdkOption.sessionReadAck = true
        // 采用异步加载SDK，加快初始化速度
        sdkOption.asyncInitSDK = true
        // 是否检查manifest 配置，调试阶段打开，调试通过之后请关掉
        sdkOption.checkManifestConfig = BuildConfig.DEBUG
        // 会话置顶是否漫游,使用置顶功能时必须开启
        sdkOption.notifyStickTopSession = true
        //是否开启logcat中sdk日志打印，建议调试阶段打开
        sdkOption.consoleLogEnabled = BuildConfig.DEBUG
        //禁止自启动，建议开启，不然影响上架
        sdkOption.disableAwake = true
        //数据库加密密钥，不设置或者为空则不加密
        sdkOption.databaseEncryptKey = ""
        //是否开启数据库备份功能，数据库损坏时可以帮助恢复数据库
        sdkOption.enableDatabaseBackup = true
        //配置专属服务器的地址，常用于私有化部署,具体可咨询技术支持。
        sdkOption.serverConfig = null
        //第三方离线推送配置。包括配置第三方推送证书的信息,具体如何配置详见官网文档
        sdkOption.mixPushConfig = null

        //初始化IM SDK，只有初始化后才可以使用SDK接口，使用V2API时，必须调用此接口
        NIMClient.initV2(this, sdkOption)
    }

    /**
     * 设置登录监听器
     */
    private fun setupLoginListeners() {
        val loginService = NIMClient.getService(V2NIMLoginService::class.java)

        // 设置登录监听器
        loginService.addLoginListener(object : V2NIMLoginListener {

            override fun onLoginStatus(status: V2NIMLoginStatus) {
                LogUtils.i(TAG, "登录状态变化: $status")
                when (status) {
                    V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> {
                        LogUtils.i(TAG, "登录状态: 登出")
                        showToast("onLoginStatus:V2NIM_LOGIN_STATUS_LOGOUT(登出)")
                    }
                    V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> {
                        LogUtils.i(TAG, "登录状态: 未登录")
                        showToast("onLoginStatus:V2NIM_LOGIN_STATUS_UNLOGIN(未登录)")
                    }
                    V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> {
                        LogUtils.i(TAG, "登录状态: 登录中")
                        showToast("onLoginStatus:V2NIM_LOGIN_STATUS_LOGINING(登录中)")
                    }
                    V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                        LogUtils.i(TAG, "登录状态: 已登录")
                        showToast("onLoginStatus:V2NIM_LOGIN_STATUS_LOGINED(已登录)")
                    }
                }
            }

            override fun onLoginFailed(error: V2NIMError) {
                LogUtils.e(TAG, "登录失败: ${error.desc} (code: ${error.code})")
                showToast("onLoginFailed:${error.desc}")
            }

            override fun onKickedOffline(detail: V2NIMKickedOfflineDetail) {
                LogUtils.w(TAG, "被踢下线 - 原因: ${detail.reason}, 描述: ${detail.reasonDesc}")
                when (detail.reason) {
                    V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT_EXCLUSIVE -> {
                        LogUtils.w(TAG, "被踢原因: 多端登录下线")
                        showToast("onKickedOffline:多端登录下线")
                    }
                    V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_SERVER -> {
                        LogUtils.w(TAG, "被踢原因: 服务器踢下线")
                        showToast("onKickedOffline:服务器踢下线")
                    }
                    V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT -> {
                        LogUtils.w(TAG, "被踢原因: 被其他客户端踢下线")
                        showToast("onKickedOffline:被其他客户端踢下线")
                    }
                }
            }

            override fun onLoginClientChanged(change: V2NIMLoginClientChange, clients: List<V2NIMLoginClient>) {
                LogUtils.i(TAG, "登录客户端变化: $change, 当前客户端数量: ${clients.size}")
                when (change) {
                    V2NIMLoginClientChange.V2NIM_LOGIN_CLIENT_CHANGE_LIST -> {
                        LogUtils.i(TAG, "客户端变化: 客户端列表更新")
                    }
                    V2NIMLoginClientChange.V2NIM_LOGIN_CLIENT_CHANGE_LOGIN -> {
                        LogUtils.i(TAG, "客户端变化: 有新客户端登录")
                    }
                    V2NIMLoginClientChange.V2NIM_LOGIN_CLIENT_CHANGE_LOGOUT -> {
                        LogUtils.i(TAG, "客户端变化: 有客户端登出")
                    }
                }

                // 打印当前所有在线客户端信息
                clients.forEachIndexed { index, client ->
                    LogUtils.i(TAG, "客户端[$index]: ${client.type} - ${client.os} - ${client.timestamp}")
                }
            }
        })

        // 设置登录详情监听器
        loginService.addLoginDetailListener(object : V2NIMLoginDetailListener {

            override fun onConnectStatus(status: V2NIMConnectStatus) {
                LogUtils.i(TAG, "连接状态变化: $status")
                when (status) {
                    V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> {
                        LogUtils.i(TAG, "连接状态: 未连接")
                    }
                    V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> {
                        LogUtils.i(TAG, "连接状态: 连接中")
                    }
                    V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> {
                        LogUtils.i(TAG, "连接状态: 已连接")
                    }
                    V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> {
                        LogUtils.i(TAG, "连接状态: 等待中")
                    }
                }
            }

            override fun onDisconnected(error: V2NIMError) {
                LogUtils.w(TAG, "连接断开: ${error.desc} (code: ${error.code})")
            }

            override fun onConnectFailed(error: V2NIMError) {
                LogUtils.e(TAG, "连接失败: ${error.desc} (code: ${error.code})")
            }

            override fun onDataSync(dataSync: V2NIMDataSyncType, state: V2NIMDataSyncState, error: V2NIMError?) {
                when (state) {
                    V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_WAITING -> {
                        LogUtils.i(TAG, "数据同步等待: $dataSync")
                    }
                    V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_SYNCING -> {
                        LogUtils.i(TAG, "数据同步中: $dataSync")
                    }
                    V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED -> {
                        if (error != null) {
                            LogUtils.e(TAG, "数据同步失败: $dataSync, 错误: ${error.desc}")
                        } else {
                            LogUtils.i(TAG, "数据同步完成: $dataSync")
                        }
                    }
                }

                when (dataSync) {
                    V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN -> {
                        LogUtils.i(TAG, "数据同步类型: 主要数据")
                    }
                    V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER -> {
                        LogUtils.i(TAG, "数据同步类型: 群组成员数据")
                    }
                    V2NIMDataSyncType.V2NIM_DATA_SYNC_SUPER_TEAM_MEMBER -> {
                        LogUtils.i(TAG, "数据同步类型: 超大群成员数据")
                    }
                }
            }
        })

        LogUtils.i(TAG, "登录监听器设置完成")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}