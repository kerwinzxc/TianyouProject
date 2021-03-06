package com.tianyou.sdk.base;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.tianyou.sdk.fragment.login.AccountFragment;
import com.tianyou.sdk.fragment.login.PerfectInfoFragment;
import com.tianyou.sdk.holder.ConfigHolder;
import com.tianyou.sdk.holder.LoginHandler;
import com.tianyou.sdk.holder.URLHolder;
import com.tianyou.sdk.utils.AppUtils;
import com.tianyou.sdk.utils.HttpUtils;
import com.tianyou.sdk.utils.LogUtils;
import com.tianyou.sdk.utils.ResUtils;
import com.tianyou.sdk.utils.ToastUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public abstract class BaseActivity extends FragmentActivity implements OnClickListener {

	protected FragmentManager mFragmentManager;
	protected String mFragmentTag;
	protected TextView mTextTitle;
	protected Activity mActivity;
	
	public boolean mIsLogout;
	
	protected Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Bundle data = msg.getData();
				LogUtils.d("start check google login------");
				checkGoogleLogin(data.getString("id"), data.getString("token"),data.getString("nickname"));
				break;
			case 2:	//完善QQ登陆信息
				switchFragment(PerfectInfoFragment.getInstance((String)msg.obj));
				break;
			}
		};
	};
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ConfigHolder.isLandscape ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE 
				: ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(setContentView());
        mActivity = this;
        PushAgent.getInstance(this).onAppStart();
        mFragmentManager = getFragmentManager();
        mTextTitle = (TextView) findViewById(ResUtils.getResById(this, "text_title", "id"));
        initView();
		initData();
	}
	
	/**
	 * 切换Fragment
	 * @param fragment
	 * @param TAG
	 */
	public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        transaction.replace(ResUtils.getResById(this, "layout_content", "id"), fragment, fragment.getClass().getSimpleName());
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
    }
	
	/**
	 * 设置title
	 * @param title
	 */
	public void setFragmentTitle(String title) {
    	mTextTitle.setText(title);
    }
    
    /**
     * 设置setFragmentTag
     * @param TAG
     */
	public void setFragmentTag(String TAG) {
    	mFragmentTag = TAG;
    }
    
	/**
	 * 处理返回按钮
	 */
    @Override
    public void onBackPressed() {
    	if ("HomeFragment".equals(mFragmentTag) || "WxScanFragment".equals(mFragmentTag) ||
    			"SuccessFragment".equals(mFragmentTag) || "OneKeyFragment".equals(mFragmentTag) || 
    			"PersonalCenterFragment".equals(mFragmentTag)) {
			finish();
		} else if ("TouristTipFragment".equals(mFragmentTag) || mFragmentTag.equals("IdentifiFragment")  && !ConfigHolder.isNoticeGame) {
			finish();
			LoginHandler.onNoticeLoginSuccess();
		} else if ("PhoneRegisterFragment".equals(mFragmentTag)||"UserRegisterFragment".equals(mFragmentTag)) {
			switchFragment(new AccountFragment());
		} else if ("PerfectInfoFragment".equals(mFragmentTag) || "AccountFragment".equals(mFragmentTag)) { 
		} else {
			getFragmentManager().popBackStack();
		}
    }
    
    /**
     * 设置布局文件
     * @return 布局文件id
     */
    protected abstract int setContentView();
    
    /**
     * 初始化View
     */
    protected abstract void initView();

    /**
     * 初始化数据
     */
    protected abstract void initData();
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// 友盟统计
    	MobclickAgent.onResume(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	// 友盟统计
    	MobclickAgent.onPause(this);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (ev.getAction() == MotionEvent.ACTION_DOWN) {
    		View v = getCurrentFocus();
    		if (isShouldHideKeyboard(v,ev)) {
    			hideKeyboard(v.getWindowToken());
    		}
    	}
    	return super.dispatchTouchEvent(ev);
    }
    
    private boolean isShouldHideKeyboard(View v,MotionEvent ev) {
    	if (v != null && (v instanceof EditText)) {
    		int[] l = {0,0};
    		v.getLocationInWindow(l);
    		int left = l[0],top = l[1],bottom = top + v.getHeight(),right = left + v.getWidth();
    		if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
    			return false;
    		} else {
    			return true;
    		}
    	}
    	return false;
    }
    
    private void hideKeyboard (IBinder token) {
    	if (token != null) {
    		InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    		im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
    	}
    }
    
    private void checkGoogleLogin(String id,String token,String nickname) {
		Map<String,String> googleParam = new HashMap<String, String>();
		googleParam.put("id_token",token);
		googleParam.put("id",id);
		googleParam.put("nickname",nickname);
		googleParam.put("googleappid", AppUtils.getMetaDataValue(mActivity,"google_client_id"));
		googleParam.put("channel", ConfigHolder.channelId);
		googleParam.put("sign", AppUtils.MD5(id+ConfigHolder.gameId+ConfigHolder.gameToken));
		HttpUtils.post(mActivity, URLHolder.URL_PAY_GOOGLE, googleParam, new HttpUtils.HttpCallback() {
			@Override
			public void onSuccess(String response) {
				LogUtils.d("login success response= "+response);
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONObject result = jsonObject.getJSONObject("result");
					int code = result.getInt("code");
					if (code == 200) {
						String userName = result.getString("username");
						String userPass = result.getString("password");
						LoginHandler.getInstance().doUserLogin(userName, userPass, false);
//						LogUtils.d("google login success userName= "+userName+",userPass= "+userPass);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					ToastUtils.show(mActivity, (ConfigHolder.isOverseas? "The server seems deserted..." : "服务器好像开小差了..."));
					LogUtils.d(e.getMessage());
				}
			}

			@Override
			public void onFailed() {
				LogUtils.d("login failed-------------");
				ToastUtils.show(mActivity, (ConfigHolder.isOverseas? "Network connection error, please check your network Settings..." : "网络连接出错,请检查您的网络设置..."));
			}
		});
    }
}
