package com.example.duoduopin.tool;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {
	/**
	 *  开始定位
	 */
	public final static int MSG_LOCATION_START = 0;
	/**
	 * 定位完成
	 */
	public final static int MSG_LOCATION_FINISH = 1;
	/**
	 * 停止定位
	 */
	public final static int MSG_LOCATION_STOP= 2;
	
	public final static String KEY_URL = "URL";
	public final static String URL_H5LOCATION = "file:///android_asset/location.html";
	/**
	 * 根据定位结果返回定位信息的字符串
	 * @param location
	 * @return
	 */
	public synchronized static String getLocationStr(AMapLocation location){
		if(null == location){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
		if(location.getErrorCode() == 0){
			sb.append("定位成功" + "\n");
			sb.append("定位类型: ").append(location.getLocationType()).append("\n");
			sb.append("经    度    : ").append(location.getLongitude()).append("\n");
			sb.append("纬    度    : ").append(location.getLatitude()).append("\n");
			sb.append("精    度    : ").append(location.getAccuracy()).append("米").append("\n");
			sb.append("提供者    : ").append(location.getProvider()).append("\n");

			sb.append("速    度    : ").append(location.getSpeed()).append("米/秒").append("\n");
			sb.append("角    度    : ").append(location.getBearing()).append("\n");
			// 获取当前提供定位服务的卫星个数
			sb.append("星    数    : ").append(location.getSatellites()).append("\n");
			sb.append("国    家    : ").append(location.getCountry()).append("\n");
			sb.append("省            : ").append(location.getProvince()).append("\n");
			sb.append("市            : ").append(location.getCity()).append("\n");
			sb.append("城市编码 : ").append(location.getCityCode()).append("\n");
			sb.append("区            : ").append(location.getDistrict()).append("\n");
			sb.append("区域 码   : ").append(location.getAdCode()).append("\n");
			sb.append("地    址    : ").append(location.getAddress()).append("\n");
			sb.append("兴趣点    : ").append(location.getPoiName()).append("\n");
			//定位完成的时间
			sb.append("定位时间: ").append(formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss")).append("\n");
		} else {
			//定位失败
			sb.append("定位失败" + "\n");
			sb.append("错误码:").append(location.getErrorCode()).append("\n");
			sb.append("错误信息:").append(location.getErrorInfo()).append("\n");
			sb.append("错误描述:").append(location.getLocationDetail()).append("\n");
		}
		//定位之后的回调时间
		sb.append("回调时间: ").append(formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")).append("\n");
		return sb.toString();
	}

	private static SimpleDateFormat sdf = null;
	public  static String formatUTC(long l, String strPattern) {
		if (TextUtils.isEmpty(strPattern)) {
			strPattern = "yyyy-MM-dd HH:mm:ss";
		}
		if (sdf == null) {
			try {
				sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
			} catch (Throwable ignored) {
			}
		} else {
			sdf.applyPattern(strPattern);
		}
		return sdf == null ? "NULL" : sdf.format(l);
	}

	/**
	 * 获取app的名称
	 * @param context
	 * @return
	 */
	public static String getAppName(Context context) {
		String appName = "";
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			appName =  context.getResources().getString(labelRes);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return appName;
	}
}
