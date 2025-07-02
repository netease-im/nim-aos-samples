package com.netease.nim.samples.login.constants;

public interface V2NIMLoginServiceConstants {

	String METHOD_login                          = "login";
	String METHOD_logout                         = "logout";
	String METHOD_getLoginUser                   = "getLoginUser";
	String METHOD_getLoginStatus                 = "getLoginStatus";
	String METHOD_getCurrentLoginClient          = "getCurrentLoginClient";
	String METHOD_getLoginClients               = "getLoginClients";
	String METHOD_kickOffline                    = "kickOffline";
	String METHOD_getKickedOfflineDetail         = "getKickedOfflineDetail";
	String METHOD_getConnectStatus               = "getConnectStatus";
	String METHOD_getDataSync                    = "getDataSync";
	String METHOD_getChatroomLinkAddress         = "getChatroomLinkAddress";
	String METHOD_setReconnectDelayProvider      = "setReconnectDelayProvider";
	String METHOD_addLoginListener               = "addLoginListener";
	String METHOD_removeLoginListener            = "removeLoginListener";
	String METHOD_addLoginDetailListener         = "addLoginDetailListener";
	String METHOD_removeLoginDetailListener      = "removeLoginDetailListener";

	String DESC_login                          = "登录";
	String DESC_logout                         = "登出";
	String DESC_getLoginUser                   = "获取当前登录用户";
	String DESC_getLoginStatus                 = "获取登录状态";
	String DESC_getCurrentLoginClient          = "获取当前登录终端相关信息";
	String DESC_getLoginClients               = "获取登录客户端列表（不包含当前端）";
	String DESC_kickOffline                    = "踢掉登录客户端下线";
	String DESC_getKickedOfflineDetail         = "获取被踢下线原因";
	String DESC_getConnectStatus               = "获取连接状态";
	String DESC_getDataSync                    = "获取当前数据同步项";
	String DESC_getChatroomLinkAddress         = "获取聊天室link地址";
	String DESC_setReconnectDelayProvider      = "设置获取重连延时回调";
	String DESC_addLoginListener               = "添加登录监听";
	String DESC_removeLoginListener            = "移除登录监听";
	String DESC_addLoginDetailListener         = "添加登录详情监听";
	String DESC_removeLoginDetailListener      = "移除登录详情监听";
}
