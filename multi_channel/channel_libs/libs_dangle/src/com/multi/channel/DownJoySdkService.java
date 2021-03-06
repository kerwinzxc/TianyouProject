package com.multi.channel;

import android.app.Activity;
import android.util.Log;

import com.downjoy.CallbackListener;
import com.downjoy.CallbackStatus;
import com.downjoy.Downjoy;
import com.downjoy.InitListener;
import com.downjoy.LoginInfo;
import com.downjoy.LogoutListener;
import com.downjoy.ResultListener;
import com.tianyou.channel.bean.PayParam;
import com.tianyou.channel.bean.RoleInfo;
import com.tianyou.channel.bean.OrderInfo.ResultBean.OrderinfoBean;
import com.tianyou.channel.interfaces.BaseSdkService;
import com.tianyou.channel.interfaces.TianyouCallback;
import com.tianyou.channel.utils.AppUtils;
import com.tianyou.channel.utils.ConfigHolder;
import com.tianyou.channel.utils.LogUtils;

public class DownJoySdkService extends BaseSdkService{

	private String merchantId; // 当乐分配的MERCHANT_ID
	private String appId; // 当乐分配的APP_ID
	private String appKey; // 当乐分配的 APP_KEY
	private String serverSeqNum = "1";
	private Downjoy downjoy;
	private static String sid;
	
	@Override
	public void doActivityInit(Activity activity, TianyouCallback tianyouCallback) {
		super.doActivityInit(activity, tianyouCallback);
		mChannelInfo = ConfigHolder.getChannelInfo(mActivity);
		appId = mChannelInfo.getAppId();
		appKey = mChannelInfo.getAppKey();
		merchantId = mChannelInfo.getClientId();
//		downjoy = Downjoy.getInstance(mActivity, merchantId, appId,serverSeqNum, appKey, new InitListener() {
//            @Override
//            public void onInitComplete() {
//            	downjoy.showDownjoyIconAfterLogined(true);
//            	downjoy.setInitLocation(Downjoy.LOCATION_LEFT_CENTER_VERTICAL);
//            	mTianyouCallback.onResult(TianyouCallback.CODE_INIT, "SDK初始化完成");
//            }
//		});
		downjoy = Downjoy.getInstance();
		mTianyouCallback.onResult(TianyouCallback.CODE_INIT, "SDK初始化完成");
		downjoy.showDownjoyIconAfterLogined(true);
    	downjoy.setInitLocation(Downjoy.LOCATION_LEFT_CENTER_VERTICAL);
		downjoy.setLogoutListener(mLogoutListener);
	}
	
	
	private LogoutListener mLogoutListener = new LogoutListener() {
        @Override
        public void onLogoutSuccess() {
//        	mLogoutCallback.onSuccess("注销成功");
        	mTianyouCallback.onResult(TianyouCallback.CODE_LOGOUT, "注销成功");
        	Log.d("TAG", "dangle logout success--------");
        }

        @Override
        public void onLogoutError(String msg) {
//        	mLogoutCallback.onFailed("注销失败");
        	LogUtils.e("注销失败");
        }
    };
	
	@Override
	public void doLogin() {
		downjoy.openLoginDialog(mActivity, new CallbackListener<LoginInfo>() {
			@Override
			public void callback(int status, LoginInfo data) {
				switch (status) {
					case CallbackStatus.SUCCESS:
						sid = data.getUmid();
						String session = data.getToken();
						mLoginInfo.setChannelUserId(sid);
						mLoginInfo.setUserToken(session);
						checkLogin();
						break;
	
					case CallbackStatus.FAIL:
						// 登录失败
						mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_FAILED, "登录失败");
						break;
						
					case CallbackStatus.CANCEL:
						mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_CANCEL,"用户取消登录");
						break;
				}
			}
		});
	}
	
	@Override
	public void doUploadRoleInfo(RoleInfo roleInfo) {
		super.doUploadRoleInfo(roleInfo);
		downjoy.submitGameRoleData(mRoleInfo.getServerId(), mRoleInfo.getServerName(), mRoleInfo.getRoleId(), 
				roleInfo.getRoleName(), mRoleInfo.getCreateTime(), AppUtils.getSystemTime(), mRoleInfo.getRoleLevel(), new ResultListener() {
					
					@Override
					public void onResult(Object result) {
						Boolean isSuccess = (Boolean) result;
						LogUtils.e("submitResult= "+isSuccess.toString());
					}
				});
	}
	
	@Override
	public void doChannelPay(PayParam payInfo, OrderinfoBean orderInfo) {
		super.doChannelPay(payInfo, orderInfo);
		
		String price = orderInfo.getMoNey();
		final String orderID = orderInfo.getOrderID();
		String productName = mPayInfo.getProductName();
		String productDesc = mPayInfo.getProductDesc();
		String serverName = mRoleInfo.getServerName();
		String playerName = mRoleInfo.getRoleName();
		String serverID = mRoleInfo.getServerId();
		String roleID = mRoleInfo.getRoleId();
		downjoy.openPaymentDialog(mActivity, Float.parseFloat(price), productName, productDesc, orderID, orderID,serverID,serverName, roleID,playerName, new CallbackListener<String>() {
			@Override
			public void callback(int status, String data) {
				switch (status) {
					case CallbackStatus.SUCCESS:
						checkOrder(orderID);
						break;

					case CallbackStatus.CANCEL:
						mTianyouCallback.onResult(TianyouCallback.CODE_PAY_CANCEL, "用户取消支付");
						break;

					case CallbackStatus.FAIL:
						mTianyouCallback.onResult(TianyouCallback.CODE_PAY_FAILED, "支付失败");
						break;
				}
			}
		});
	}
	
	@Override
	public void doLogout() {
		super.doLogout();
		downjoy.logout(mActivity);
	}
	
	@Override
	public void doExitGame() {
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				downjoy.openExitDialog(mActivity, new CallbackListener<String>() {
					@Override
					public void callback(int status, String data) {
						if (status == CallbackStatus.SUCCESS){
							mTianyouCallback.onResult(TianyouCallback.CODE_QUIT_SUCCESS, "用户退出");
						} else {
							mTianyouCallback.onResult(TianyouCallback.CODE_QUIT_CANCEL, "用户取消退出");
						}
					}
				});
			}
		});
	}
	
	@Override
	public void doResume() {
		if (downjoy != null) {
	        downjoy.resume(mActivity);
	    }
	}
	
	@Override
	public void doPause() {
		if (downjoy != null) {
	        downjoy.pause();
	    }
	}
	
	@Override
	public void doDestory() {
		if (downjoy != null) {
	        downjoy.destroy();
	        downjoy = null;
	    }
	}
}
