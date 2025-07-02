package com.netease.nim.samples.login.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.netease.nim.samples.databinding.ItemLoginClientBinding
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientType
import java.text.SimpleDateFormat
import java.util.*

class LoginClientsAdapter : RecyclerView.Adapter<LoginClientsAdapter.ViewHolder>() {

    private val clientsList = mutableListOf<V2NIMLoginClient>()

    fun updateData(newClients: List<V2NIMLoginClient>) {
        clientsList.clear()
        clientsList.addAll(newClients)
        notifyDataSetChanged()
    }

    fun clearData() {
        clientsList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLoginClientBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(clientsList[position], position + 1)
    }

    override fun getItemCount(): Int = clientsList.size

    class ViewHolder(private val binding: ItemLoginClientBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(client: V2NIMLoginClient, index: Int) {
            binding.tvClientIndex.text = "#$index"
            binding.tvClientType.text = getClientTypeDisplayName(client.type)
            binding.tvClientTypeValue.text = "(${client.type.value})"
            binding.tvClientOs.text = client.os ?: "未知"
            binding.tvClientId.text = client.clientId ?: "未知"
            binding.tvLoginTimestamp.text = formatTimestamp(client.timestamp)
            binding.tvCustomTag.text = if (client.customTag.isNullOrEmpty()) "无" else client.customTag
            binding.tvCustomClientType.text = client.customClientType.toString()

            // 设置客户端类型颜色
            val typeColor = getClientTypeColor(client.type)
            binding.tvClientType.setTextColor(typeColor)
        }

        private fun getClientTypeDisplayName(type: V2NIMLoginClientType): String {
            return when (type) {
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_ANDROID -> "Android"
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_IOS -> "iOS"
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_PC -> "PC"
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_WINPHONE -> "Windows Phone"
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_WEB -> "Web"
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_RESTFUL -> "REST API"
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_UNKNOWN -> "未知类型"
                else -> "未知类型"
            }
        }

        private fun getClientTypeColor(type: V2NIMLoginClientType): Int {
            val context = binding.root.context
            return when (type) {
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_ANDROID -> 
                    context.resources.getColor(android.R.color.holo_green_dark)
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_IOS -> 
                    context.resources.getColor(android.R.color.holo_blue_dark)
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_PC -> 
                    context.resources.getColor(android.R.color.holo_purple)
                V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_WEB -> 
                    context.resources.getColor(android.R.color.holo_orange_dark)
                else -> 
                    context.resources.getColor(android.R.color.darker_gray)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            return if (timestamp > 0) {
                val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
                sdf.format(Date(timestamp))
            } else {
                "未知时间"
            }
        }
    }
}