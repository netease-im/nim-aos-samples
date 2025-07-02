package com.netease.nim.samples.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonHelper {
	private static final String TAG = "GsonHelper";

	private GsonBuilder gsonBuilder = new GsonBuilder();
	private Gson        gson = null;

	private Gson        serializeNullsGson = null;
	private static class InstanceHolder {

		static GsonHelper instance = new GsonHelper();
	}

	public static GsonHelper getInstance() {
		return InstanceHolder.instance;
	}

	public void init(){
		gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().create();
		serializeNullsGson = gsonBuilder.serializeNulls().disableHtmlEscaping().create();
	}

	public Gson getGson(){
		if(gson == null){
			init();
		}
		return gson;
	}

	public Gson getSerializeNullsGson(){
		return serializeNullsGson;
	}

	public static String toJson(Object obj){
		try {
			return getInstance().getGson().toJson(obj);
		} catch (Exception e) {
			LogUtils.e(TAG, "toJson error with " + obj, e);
			return "";
		}
	}
}
