package com.netease.nim.samples.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetBlockListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import java.text.SimpleDateFormat
import java.util.*

class GetBlockListFragment : BaseMethodExecuteFragment<FragmentGetBlockListBinding>() {

    /**
     * 时间格式化器
     */
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetBlockListBinding {
        return FragmentGetBlockListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }


    private fun initUI() {
        // 初始化UI状态
        refreshPreview()
        updateStatisticsInfo(null)
        updateBlockListPreview(null)
    }

    /**
     * 刷新预览状态
     */
    private fun refreshPreview() {
        val currentTime = dateTimeFormatter.format(Date())
        binding.tvRequestPreview.text = "🚫 准备获取黑名单列表\n⏰ 预览时间: $currentTime"
    }

    /**
     * 更新黑名单预览
     */
    private fun updateBlockListPreview(blockList: List<String>?) {
        if (blockList == null) {
            binding.tvBlockListPreview.text = "暂未获取黑名单数据"
            return
        }

        if (blockList.isEmpty()) {
            binding.tvBlockListPreview.text = "✅ 黑名单为空，当前没有屏蔽任何用户"
            return
        }

        val previewText = StringBuilder()
        previewText.append("🚫 黑名单用户列表:\n")
        
        blockList.forEachIndexed { index, accountId ->
            previewText.append("${index + 1}. $accountId\n")
        }
        
        if (blockList.size > 10) {
            previewText.append("... 还有${blockList.size - 10}个用户")
        }
        
        binding.tvBlockListPreview.text = previewText.toString().trimEnd()
    }

    /**
     * 更新统计信息
     */
    private fun updateStatisticsInfo(blockList: List<String>?) {
        val statisticsText = if (blockList == null) {
            "📊 黑名单用户数量: 未知\n⏰ 统计时间: ${dateTimeFormatter.format(Date())}"
        } else {
            val count = blockList.size
            val status = when {
                count == 0 -> "✅ 良好"
                count <= 5 -> "⚠️ 适中"
                count <= 10 -> "🔶 较多"
                else -> "🔴 过多"
            }
            "📊 黑名单用户数量: $count 个\n📈 状态评估: $status\n⏰ 统计时间: ${dateTimeFormatter.format(Date())}"
        }
        
        binding.tvStatisticsInfo.text = statisticsText
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        // 更新请求状态
        binding.tvRequestPreview.text = "🔄 正在获取黑名单列表..."
        
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMUserService::class.java).getBlockList(
            { blockList -> updateResult(blockList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(blockList: List<String>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            // 更新请求状态为失败
            binding.tvRequestPreview.text = "❌ 获取黑名单列表失败\n⏰ 失败时间: ${dateTimeFormatter.format(Date())}"
            activityViewModel.refresh("获取黑名单列表失败: $error")
            return
        }

        blockList?.let {
            // 更新UI显示
            binding.tvRequestPreview.text = "✅ 获取黑名单列表成功\n⏰ 成功时间: ${dateTimeFormatter.format(Date())}"
            updateBlockListPreview(it)
            updateStatisticsInfo(it)
            
            // 显示详细结果
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(blockList: List<String>): String {
        val sb = StringBuilder()
        
        sb.append("获取黑名单列表成功:\n\n")
        
        sb.append("📊 基本统计:\n")
        sb.append("• 黑名单用户总数: ${blockList.size}个\n")
        sb.append("• 获取时间: ${dateTimeFormatter.format(Date())}\n")
        sb.append("• 数据类型: 用户账号字符串列表\n\n")
        
        if (blockList.isEmpty()) {
            sb.append("✅ 黑名单状态:\n")
            sb.append("• 当前黑名单为空\n")
            sb.append("• 没有屏蔽任何用户\n")
            sb.append("• 可以正常接收所有用户消息\n\n")
            
            sb.append("💡 操作建议:\n")
            sb.append("• 黑名单为空是正常状态\n")
            sb.append("• 如需屏蔽用户，可使用添加到黑名单功能\n")
            sb.append("• 建议谨慎使用黑名单功能")
        } else {
            sb.append("🚫 黑名单详情:\n")
            
            blockList.forEachIndexed { index, accountId ->
                sb.append("${index + 1}. 用户账号: $accountId\n")
            }
            
            sb.append("\n📈 状态分析:\n")
            val status = when {
                blockList.size <= 5 -> {
                    sb.append("• 黑名单用户数量适中\n")
                    sb.append("• 屏蔽状态: ⚠️ 适中\n")
                    "适中"
                }
                blockList.size <= 10 -> {
                    sb.append("• 黑名单用户数量较多\n")
                    sb.append("• 屏蔽状态: 🔶 较多\n")
                    "较多"
                }
                else -> {
                    sb.append("• 黑名单用户数量过多\n")
                    sb.append("• 屏蔽状态: 🔴 过多\n")
                    "过多"
                }
            }
            
            sb.append("• 建议状态: ${if (blockList.size > 10) "建议清理部分黑名单" else "当前状态良好"}\n\n")
            
            sb.append("🔧 管理操作:\n")
            sb.append("• 可以使用移除黑名单功能解除屏蔽\n")
            sb.append("• 建议定期清理不必要的黑名单用户\n")
            sb.append("• 黑名单用户无法向你发送消息\n")
            sb.append("• 黑名单设置会同步到其他设备")
        }
        
        sb.append("\n\n💭 温馨提示:\n")
        sb.append("• 黑名单列表实时反映当前状态\n")
        sb.append("• 添加或移除黑名单后可重新获取查看\n")
        sb.append("• 黑名单功能用于保护用户免受骚扰\n")
        sb.append("• 如有疑问可随时调整黑名单设置")

        return sb.toString()
    }
}