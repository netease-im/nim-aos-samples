package com.netease.nim.samples.message.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.R
import com.netease.nim.samples.common.getV2NIMMessageJson
import com.netease.nim.samples.common.getV2NIMSendMessageResultJson
import com.netease.nim.samples.databinding.FragmentReplayMessageBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageSelectFragment
import com.netease.nim.samples.utils.PickUtil
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallContent
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageAntispamConfig
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageConfig
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessagePushConfig
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageRobotConfig
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageRouteConfig
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageTargetConfig
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageAIConfigParams
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ReplayMessageFragment : BaseMethodExecuteFragment<FragmentReplayMessageBinding>() {

    /**
     * 当前消息
     */
    private var currentMsg: V2NIMMessage? = null
    private var replyMsg: V2NIMMessage? = null
    /**
     * 消息类型
     */
    private val messageTypes = arrayListOf(
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_TIPS,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM
    )

    private val fileMessageTypes = setOf(
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO,
        V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE,
    )

    /**
     * 当前消息类型
     */
    private var currentMessageType = messageTypes[0]

    /**
     * 图片选择Launcher
     */
    private lateinit var imageLauncher: ActivityResultLauncher<String>

    /**
     * 视频选择Launcher
     */
    private lateinit var videoLauncher: ActivityResultLauncher<String>
    /**
     * 文件选择Launcher
     */
    private lateinit var fileLauncher: ActivityResultLauncher<String>

    /**
     * 权限申请 Launcher
     */
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    /**
     * 是否按着
     */
    private var isAudioBtnTouched = false

    /**
     * 是否开始录音
     */
    private val isAudioRecordStarted = false

    /**
     * 是否取消录音
     */
    private var isAudioRecordCancelled = false

    /**
     * 录音工具类
     */
    private var audioRecorder: AudioRecorder? = null

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel
    /**
     * 选择消息的 ViewModel
     */
    private lateinit var selectMessageViewModel: V2MessageSelectViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注册 launcher
        imageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                // 处理图片 Uri
                dealWithImage(uri)
            }
        }
        videoLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                // 处理视频 Uri
                dealWithVideo(uri)
            }
        }

        fileLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                // 处理文件 Uri
                dealWithFile(uri)
            }
        }

        // 初始化权限请求 Launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // 权限已授予
                showToast("录音权限已授权")
            } else {
                // 权限被拒绝
                showToast("录音权限被拒绝")
            }
        }
    }

    /**
     * 处理文件
     */
    private fun dealWithFile(uri: Uri?) {
        uri ?: return

        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) {
                PickUtil.getFile(context, uri)
            }
            binding.spinnerMessageType.setSelection(messageTypes.indexOf(V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE))
            binding.edtPath.setText(file.path)
        }
    }


    /**
     * 处理视频
     */
    private fun dealWithVideo(uri: Uri?) {
        uri ?: return

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val file = PickUtil.getFile(context, uri)
                val mediaPlayer: MediaPlayer? = PickUtil.getMediaPlayer(requireContext(), file)
                val duration = mediaPlayer?.duration?.toLong() ?: 0L
                val height = mediaPlayer?.videoHeight ?: 0
                val width = mediaPlayer?.videoWidth ?: 0
                mediaPlayer?.release()
                Pair(file, Triple(duration, height, width))

            }
            binding.spinnerMessageType.setSelection(messageTypes.indexOf(V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO))
            binding.edtPath.setText(result.first.path)
            binding.edtDisplayName.setText(result.first.name)
            binding.edtDuration.setText("${result.second.first}")
            binding.edtHeight.setText("${result.second.second}")
            binding.edtWidth.setText("${result.second.third}")
        }
    }

    /**
     * 处理图片
     */
    private fun dealWithImage(uri: Uri?) {
        uri ?: return

        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) {
                PickUtil.getFile(context, uri)
            }
            binding.spinnerMessageType.setSelection(messageTypes.indexOf(V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE))
            binding.edtPath.setText(file.path)
            binding.edtDisplayName.setText(file.name)
        }
    }


    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentReplayMessageBinding {
        return FragmentReplayMessageBinding.inflate(inflater, container, false)
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            //初始化会话列表
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, messageTypes)
            binding.spinnerMessageType.adapter = spinnerAdapter
            binding.spinnerMessageType.setSelection(0)
            binding.spinnerMessageType.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        currentMessageType = messageTypes[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            //创建消息
            binding.btnCreateMsg.setOnClickListener { v: View? ->
                if (currentMessageType in fileMessageTypes) {
                    if (binding.edtPath.text.isEmpty()) {
                        showToast("path is empty")
                        return@setOnClickListener
                    }
                }

                when (currentMessageType) {
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT -> currentMsg = createTextMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE -> currentMsg = createImageMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO -> currentMsg = createAudioMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO -> currentMsg = createVideoMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION -> currentMsg = createLocationMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE -> currentMsg = createFileMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_TIPS -> currentMsg = createTipsMessage()
                    V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM -> currentMsg = createCustomMessage()
                    else -> {}
                }

                binding.tvMessageJson.text = getV2NIMMessageJson(currentMsg)
            }

            binding.btnSendImage.setOnClickListener {
                // 启动图片选择器
                imageLauncher.launch("image/*")
            }
            binding.btnSendVideo.setOnClickListener {
                // 启动视频选择器
                videoLauncher.launch("video/*")
            }
            // 启动录音
            binding.btnSendAudio.setOnTouchListener {view: View, motionEvent: MotionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isAudioBtnTouched = true
                        initAudioRecord()
                        onStartAudioRecord()
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        isAudioBtnTouched = false
                        onEndAudioRecord(isCancelled(view, motionEvent))
                    }
                    MotionEvent.ACTION_MOVE -> {
                        isAudioBtnTouched = false
                        cancelAudioRecord(isCancelled(view, motionEvent))
                    }
                }
                false
            }

            binding.btnSendFile.setOnClickListener {
                // 启动文件选择器
                fileLauncher.launch("*/*")
            }

            //选择会话
            binding.btnGetConversation.setOnClickListener {
                activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
            }
            //选择消息
            binding.btnSelectMsg.setOnClickListener {
                val conversationId = binding.etConversationId.text.toString()
                if (conversationId.isNotEmpty()) {
                    activityViewModel.addFragment(V2NIMMessageSelectFragment.NAME,conversationId)
                }else{
                    showToast("请输入会话ID")
                }
            }

            binding.cbMessageConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llMessageConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            binding.cbRouteConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llRouteConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            binding.cbPushConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llPushConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            binding.cbAntispamConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llAntispamConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            binding.cbRobotConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llRobotConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            binding.cbAiConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llAiConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            binding.cbTargetConfig.setOnCheckedChangeListener { _, isChecked ->
                binding.llTargetConfig.visibility = if(isChecked) View.VISIBLE else View.GONE
            }

            selectConversationViewModel = getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
            selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
                activityViewModel.popFragment()
                binding.etConversationId.setText(it.conversationId)
            })

            selectMessageViewModel = getActivityViewModel(V2MessageSelectViewModel::class.java)
            selectMessageViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
                activityViewModel.popFragment()
                replyMsg = it
                binding.tvReplyMsg.text = getV2NIMMessageJson(it)
            })

            grantPermission()
        }

    /**
     * 申请权限
     */
    private fun grantPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // 权限未授予，需要请求权限
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onRequest() {
        if(currentMsg == null){
            showToast("请先创建消息")
            return
        }
        if (replyMsg == null){
            showToast("请先选择回复消息")
            return
        }

        val params: V2NIMSendMessageParams = generateSendMessageParams()
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).replyMessage(currentMsg,replyMsg,params,
            { result -> updateResult(null,result) },
            { error -> updateResult(error,null) },
            {
                //  上传进度
            })

    }

    private fun updateResult(error: V2NIMError?,result: V2NIMSendMessageResult?){
        activityViewModel.dismissLoadingDialog()
        if(error != null){
            activityViewModel.refresh("回复失败: $error")
            return
        }
        if(result != null){
            activityViewModel.refresh("回复成功: ${getV2NIMSendMessageResultJson(result)}")
            return
        }
    }

    /**
     * 生成发送消息参数
     */
    private fun generateSendMessageParams(): V2NIMSendMessageParams {
        val messageConfig = if(binding.cbMessageConfig.isChecked){
            V2NIMMessageConfig.V2NIMMessageConfigBuilder.builder()
                .withReadReceiptEnabled(binding.swReadReceiptEnabled.isChecked)
                .withLastMessageUpdateEnabled(binding.swLastMessageUpdateEnabled.isChecked)
                .withHistoryEnabled(binding.swHistoryEnabled.isChecked)
                .withRoamingEnabled(binding.swRoamingEnabled.isChecked)
                .withOnlineSyncEnabled(binding.swOnlineSyncEnabled.isChecked)
                .withOfflineEnabled(binding.swOfflineEnabled.isChecked)
                .withUnreadEnabled(binding.swUnreadEnabled.isChecked)
                .build()
        } else {
            null
        }

        val routeEnabled = binding.swRouteEnabled.isChecked
        val routeEnvironment = binding.edtRouteEnvironment.text.toString()
        val routeConfig = if (binding.cbRouteConfig.isChecked) {
            V2NIMMessageRouteConfig.V2NIMMessageRouteConfigBuilder.builder()
                .withRouteEnabled(routeEnabled)
                .withRouteEnvironment(routeEnvironment)
                .build()
        } else null

        val pushEnabled = binding.swPushEnabled.isChecked
        val pushNickEnabled = binding.swPushNickEnabled.isChecked
        val forcePush = binding.swForcePush.isChecked
        val pushContent = binding.edtPushContent.text.toString()
        val pushPayload = binding.edtPushPayload.text.toString()
        val forcePushContent = binding.edtForcePushContent.text.toString()
        val forcePushAccountIds = binding.edtForcePushAccountIds.text.toString().split(",").map(String::trim)

        val pushConfig = if (binding.cbPushConfig.isChecked) {
            V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder.builder()
                .withPushEnabled(pushEnabled)
                .withPushNickEnabled(pushNickEnabled)
                .withForcePush(forcePush)
                .withContent(pushContent)
                .withPayload(pushPayload)
                .withForcePushContent(forcePushContent)
                .withForcePushAccountIds(forcePushAccountIds)
                .build()
        } else null

        val antispamEnabled = binding.swAntispamEnabled.isChecked
        val businessId = binding.edtAntispamBusinessId.text.toString()
        val customMessage = binding.edtAntispamCustomMessage.text.toString()
        val cheating = binding.edtAntispamCheating.text.toString()
        val extension = binding.edtAntispamExtension.text.toString()

        val antispamConfig = if (binding.cbAntispamConfig.isChecked) {
            V2NIMMessageAntispamConfig.V2NIMMessageAntispamConfigBuilder.builder()
                .withAntispamEnabled(antispamEnabled)
                .withAntispamBusinessId(businessId)
                .withAntispamCustomMessage(customMessage)
                .withAntispamCheating(cheating)
                .withAntispamExtension(extension)
                .build()
        } else null

        val accountId = binding.edtRobotAccountId.text.toString()
        val topic = binding.edtRobotTopic.text.toString()
        val function = binding.edtRobotFunction.text.toString()
        val customContent = binding.edtRobotCustomContent.text.toString()

        val robotConfig = if (binding.cbRobotConfig.isChecked) {
            V2NIMMessageRobotConfig.V2NIMMessageRobotConfigBuilder.builder()
                .withAccountId(accountId.takeIf { accountId.isNotBlank() })
                .withTopic(topic.takeIf { topic.isNotBlank() })
                .withFunction(function.takeIf { function.isNotBlank() })
                .withCustomContent(customContent.takeIf { customContent.isNotBlank() })
                .build()
        } else null

        val aiAccountId = binding.edtAiAccountId.text.toString()
        val aiMsg = binding.edtAiContentMsg.text.toString()
        val aiType = binding.edtAiContentType.text.toString().toIntOrNull()
        val aiStream = binding.swAiStream.isChecked

        val aiConfig = if (binding.cbAiConfig.isChecked) {
            V2NIMMessageAIConfigParams.Builder()
                .accountId(aiAccountId.takeIf { aiAccountId.isNotBlank() })
                .content(V2NIMAIModelCallContent(aiMsg, aiType))
                .aiStream(aiStream)
                .build()
        } else null

        val inclusive = binding.swInclusive.isChecked
        val receiverIds = binding.edtReceiverIds.text.toString().split(",").map(String::trim)
        val newMemberVisible = binding.swNewMemberVisible.isChecked

        val targetConfig = if (binding.cbTargetConfig.isChecked) {
            V2NIMMessageTargetConfig(inclusive,receiverIds,newMemberVisible)
        } else null

        val clientAntispamEnabled = binding.cbClientAntispamEnabled.isChecked
        val clientAntispamReplace = binding.edtClientAntispamReplace.text.toString()

        val sendParams = V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder()
            .withMessageConfig(messageConfig)
            .withRouteConfig(routeConfig)
            .withPushConfig(pushConfig)
            .withAntispamConfig(antispamConfig)
            .withRobotConfig(robotConfig)
            .withAIConfig(aiConfig)
            .withTargetConfig(targetConfig)
            .withClientAntispamEnabled(clientAntispamEnabled)
            .withClientAntispamReplace(clientAntispamReplace)
            .build()

        return sendParams
    }

    private fun createLocationMessage(): V2NIMMessage {
        return V2NIMMessageCreator.createLocationMessage(
            binding.longitude.text.toString().toDoubleOrNull() ?: 0.0,
            binding.latitude.text.toString().toDoubleOrNull() ?: 0.0,
            binding.etAdrress.text.toString()
        )
    }

    private fun createTextMessage(): V2NIMMessage {
        return V2NIMMessageCreator.createTextMessage(binding.messageText.getText().toString())
    }

    private fun createTipsMessage(): V2NIMMessage {
        return V2NIMMessageCreator.createTipsMessage(binding.messageText.getText().toString())
    }

    private fun createCustomMessage(): V2NIMMessage {
        return V2NIMMessageCreator.createCustomMessage(
            binding.messageText.getText().toString(),
            binding.messageAttachment.getText().toString())
    }

    private fun createFileMessage(): V2NIMMessage {
        return V2NIMMessageCreator.createFileMessage(
            binding.edtPath.text.toString(),
            binding.edtDisplayName.text.toString(),
            binding.sceneName.text.toString()
        )
    }

    private fun createAudioMessage():V2NIMMessage {
        return V2NIMMessageCreator.createAudioMessage(
            binding.edtPath.text.toString(),
            binding.edtDisplayName.text.toString(),
            binding.sceneName.text.toString(),
            binding.edtDuration.text.toString().toIntOrNull()
        )
    }

    private fun createVideoMessage():V2NIMMessage {
        return V2NIMMessageCreator.createVideoMessage(
            binding.edtPath.text.toString(),
            binding.edtDisplayName.text.toString(),
            binding.sceneName.text.toString(),
            binding.edtDuration.text.toString().toIntOrNull(),
            binding.edtWidth.text.toString().toIntOrNull(),
            binding.edtHeight.text.toString().toIntOrNull()
        )
    }

    private fun createImageMessage(): V2NIMMessage {
        return V2NIMMessageCreator.createImageMessage(
            binding.edtPath.text.toString(), binding.edtDisplayName.text.toString(),
            binding.sceneName.text.toString(),
            binding.edtWidth.text.toString().toIntOrNull(),
            binding.edtHeight.text.toString().toIntOrNull()
        )
    }


    /*********************** 语音 **********************************/
    private fun initAudioRecord() {
        if (audioRecorder == null) {
            audioRecorder = AudioRecorder(
                activity, RecordType.AAC,
                AudioRecorder.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND,
                object : IAudioRecordCallback {
                    override fun onRecordReady() {
                        showToast("录音初始化完成")
                        if (!isAudioBtnTouched) {
                            return
                        }
                    }

                    override fun onRecordStart(
                        file: File,
                        recordType: RecordType
                    ) {
                        showToast("开始录音")
                    }

                    override fun onRecordSuccess(
                        file: File, l: Long,
                        recordType: RecordType
                    ) {
                        setAudioMsg(file, l)
                    }

                    override fun onRecordFail() {
                        showToast("录音失败")
                    }

                    override fun onRecordCancel() {
                        showToast("取消录音")
                    }

                    override fun onRecordReachedMaxTime(i: Int) {
                        showToast("到达最大录音时间")
                        audioRecorder?.handleEndRecord(true, i)
                    }
                })
        }
    }

    private fun setAudioMsg(file: File, duration: Long) {
        binding.spinnerMessageType.setSelection(messageTypes.indexOf(V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO))
        binding.edtPath.setText(file.path)
        binding.edtDuration.setText("$duration")
    }

    // 上滑取消录音判断
    private fun isCancelled(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        if (event.rawX < location[0] || event.rawX > location[0] + view.width || event.rawY < location[1] - 40) {
            return true
        }
        return false
    }

    /**
     * 开始语音录制
     */
    private fun onStartAudioRecord() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        audioRecorder?.startRecord()
        isAudioRecordCancelled = false
        binding.btnSendAudio.setText(
            R.string.record_audio_end
        )
    }

    /**
     * 结束语音录制
     *
     * @param cancel
     */
    private fun onEndAudioRecord(cancel: Boolean) {
        activity?.window?.setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        audioRecorder?.completeRecord(cancel)
        binding.btnSendAudio.setText(R.string.record_audio)
    }

    /**
     * 取消语音录制
     *
     * @param cancel
     */
    private fun cancelAudioRecord(cancel: Boolean) {
        // reject
        if (!isAudioRecordStarted) {
            return
        }
        // no change
        if (isAudioRecordCancelled == cancel) {
            return
        }
        isAudioRecordCancelled = cancel
    }

}
