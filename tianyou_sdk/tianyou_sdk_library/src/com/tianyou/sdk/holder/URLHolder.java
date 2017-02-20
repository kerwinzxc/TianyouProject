package com.tianyou.sdk.holder;

public class URLHolder {

	// 服务器域名地址
	public static final String URL_BASE = "http://api.tianyouxi.com/index.php";
//	public static final String URL_BASE = "http://vpntest.tianyouxi.com/index.php";
	public static final String URL_OVERSEAS = "http://testapi.tianyouxi.com/index.php";
//	public static final String URL_TEST = "http://192.168.1.176/index.php";
	
	/** ------------------------- 登录接口 ------------------------- */
	// 获取登录方式
	public static final String URL_LOGIN_WAY = URL_BASE + "?c=Member&a=GetLoginSwitch";
	// 获取验证码
	public static final String URL_GET_CODE = URL_BASE + "?c=sms&a=SendMsg";
	// 公告接口
	public static final String URL_ANNOUNCE = URL_BASE + "?c=Member&a=Getnotice";
	// QQ登录
	public static final String URL_QQ_LOGIN = URL_BASE + "?c=register&a=QQQuick";
	// QQ WebView URL
	public static final String URL_QQ_WEB = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=101322155&daid=383&pt_skey_valid=1&style=35&s_url=http%3A%2F%2Fconnect.qq.com&refer_cgi=authorize&which=&auth_time=1470121319621&client_id=101322155&src=1&state=&response_type=token&scope=add_share%2Cadd_topic%2Clist_album%2Cupload_pic%2Cget_simple_userinfo&redirect_uri=auth%3A%2F%2Ftauth.qq.com%2F";
	// 一键登录
	public static final String URL_KEY_LOGIN = URL_BASE + "?c=NewReg&a=ShortMessageReg";
	// 用户登录
	public static final String URL_CODE_LOGIN = (ConfigHolder.IS_OVERSEAS ? URL_OVERSEAS : URL_BASE) + "?c=NewLog&a=VerificationLog";
	// 快速登录
	public static final String URL_LOGIN_QUICK = "?c=NewReg&a=FastReg";
	// 完善用户信息
	public static final String URL_LOGIN_PERFECT = URL_BASE + "?c=NewReg&a=perfect";
	// 找回密码
	public static final String URL_FIND_PASS = URL_BASE +  "?c=login&a=FindPassByPhone";
	// 微信回调地址
	public static final String URL_NOTIFY_WECHAT = "http://www.tianyouxi.com/tianyousdk/weixin/notify.php";
	// 微信回调地址
	public static final String URL_NOTIFY_ALIPAY = "http://www.tianyouxi.com/tianyousdk/index.php/Home/AliPay/notifyurl";
	// 进入游戏
	public static final String URL_ENTER_GAME = URL_BASE + "?c=UidSellInfo&a=EnterGame";
	// 创建角色
	public static final String URL_CREATE_ROLE = URL_BASE + "?c=UidSellInfo&a=CreateRole";
	// 创建角色
	public static final String URL_BINDING_PHONE = URL_BASE + "?c=login&a=BindPhone";
	// facebook 用户验证
	public static final String URL_FACEBOOK_LOGIN = "http://testapi.tianyouxi.com/index.php?s=/Facebook/fbSdk";
	
	// Google 用户验证
	public static final String URL_GOOGLE_LOGIN = "http://testapi.tianyouxi.com/index.php?s=Google/GoogleSdk";
	
	/** ------------------------- 支付接口 ------------------------- */
	// 控制支付方式接口
	public static String URL_PAY_TYPE_ON_OFF = URL_BASE + "?c=Member&a=GetPayType";
	// 产品信息接口
	public static String URL_GET_PRODUCT_INFO = URL_BASE + "?c=GameProduct&a=GetProduct";
	// 创建订单
	public static String URL_CREATE_ORDER = URL_BASE + "?c=Order&a=CreatOrder";
	// 获取金额数值
	public static String URL_MONEY_VALUE = URL_BASE + "?c=Order&a=getRecharge";
	// 获取金额数值
	public static String URL_WALLET_MONEY_VALUE = URL_BASE + "?c=Order&a=getWalletRecharge";
	// 钱包余额
	public static String URL_PAY_WALLET_REMAIN = URL_BASE + "?c=Order&a=getWallet";
	// 钱包支付
	public static String URL_PAY_WALLET = URL_BASE + "?c=OrderWallet&a=CreatOrder";
	// 客服图片接口
	public static String URL_SERVER_IMG = URL_BASE + "/Member/GetCustom";
	// 查询订单接口
	public static String URL_QUERY_ORDER = URL_BASE + "?c=Order&a=GetOrderStatus";
	// 支付方式控制
	public static String URL_PAY_WAY_CONTROL = "http://testapi.tianyouxi.com/index.php?c=Member&a=GetPayType";
	// 创建订单海外接口
	public static String URL_CREATE_ORDER_OVERSEAS = "http://testapi.tianyouxi.com/index.php?s=Order/CreatOrder";
	// 谷歌支付校验地址
	public static String URL_CHECK_ORDER_GOOGLE = "http://testapi.tianyouxi.com/index.php?s=Order/GoogleUpdateOrder";
	// PayPal支付校验地址
	public static String URL_CHECK_ORDER_PAYPAL = "http://testapi.tianyouxi.com/index.php?s=Order/PaypalUpdateOrder";
	
	/** ------------------------- 悬浮窗接口 ------------------------- */
	// 论坛接口
	public static String URL_BBS = "http://bbs.tianyouxi.com/member.php?mod=logging&action=sdktianyouxilogin";
	// 首页接口
	public static String URL_INDEX = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/CheckSdk/sdkindex";
	// 个人中心接口
	public static String URL_CENTER = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/CheckSdk/sdkpersonal";
	// 礼包接口
	public static String URL_GIFT = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/CheckSdk/sdkgift";
	// 帮助游戏
	public static String URL_MENU_HELP = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/CustomService/index.html";
	// 退出游戏
	public static String URL_EXIT_MORE = URL_BASE + "?c=Game&a=GetAndroidGames";
	// 热门游戏推荐
	public static String URL_HOT_GAME = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/GameCentre/index.html";
	// 忘记密码
	public static String URL_FORGET_PASS = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/Public/getBackPassword.html";
	// 去品台支付
	public static String URL_PAY_ONPLAT = "http://www.tianyouxi.com/svnonethink/mobilesdk.php?s=/CheckSdk/sdkpayCentre&";
	// 获取汇款二维码
	public static String URL_GET_REMIT_CODE = "http://www.tianyouxi.com/Public/tyx/shewm.png";
	// 悬浮球开关控制
	public static String URL_FLOAT_CONTROL = "http://testapi.tianyouxi.com/index.php?s=/Member/GetFrame";
}