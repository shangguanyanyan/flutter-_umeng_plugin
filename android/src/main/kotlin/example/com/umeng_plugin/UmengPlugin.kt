package example.com.umeng_plugin

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull;
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.commonsdk.UMConfigure.getTestDeviceInfo
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** UmengPlugin */
public class UmengPlugin: FlutterPlugin, MethodCallHandler {
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val umengPlugin = UmengPlugin()
    umengPlugin.context = flutterPluginBinding.applicationContext;
    Log.d("onAttachedToEngine","${umengPlugin.context}")
    val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "umeng_plugin")
    channel.setMethodCallHandler(umengPlugin)
    val infos = getTestDeviceInfo(flutterPluginBinding.applicationContext)
    Log.d("umenginfo", "{\"device_id\":\"${infos[0]}\",\"mac\":\"${infos[1]}}\"")
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  var context: Context? = null
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val umengPlugin = UmengPlugin()
      umengPlugin.context = registrar.activity()
      Log.d("context in registerWith","${umengPlugin.context}")
      val channel = MethodChannel(registrar.messenger(), "umeng_plugin")
      channel.setMethodCallHandler(umengPlugin)
      val infos = getTestDeviceInfo(registrar.activity())
      Log.d("umenginfo", "{\"device_id\":\"${infos[0]}\",\"mac\":\"${infos[1]}}\"")
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
      Log.d("customEvent", call.method)
    when (call.method) {
        "getPlatformVersion" -> result.success("Android ${Build.VERSION.RELEASE}")
        "init" -> init(call, result)
        "beginPageView" -> beginPageView(call, result)
        "endPageView" -> endPageView(call, result)
        "loginPageView" -> loginPageView(call, result)
        "logoutPageView" -> logOutPageView(call, result)
        "eventCounts" -> eventCounts(call, result)
        "setCatchUncaught" -> setaCatchUncaught(call, result)
        "reportErr" -> reporteErr(call, result)
        "customEvent" -> customEvent(call, result)
        "getChannel" -> getChannel(call, result)
        "getDeviceInfo" -> getDeviceInfo(call,result)
        else -> result.notImplemented()
    }
  }
  private fun init(call: MethodCall, result: Result) {
    // 设置组件化的Log开关，参数默认为false，如需查看LOG设置为true
    call.argument<Boolean>("logEnable")?.let { UMConfigure.setLogEnabled(it) }
    /**
     * 初始化common库 参数1：上下文，不能为空 参数2：【友盟+】Appkey名称 参数3：【友盟+】Channel名称
     * 参数4：设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
     * 参数5：Push推送业务的secret
     */
    Log.d("customEvent","channel : ${call.argument<Any>("channel") as String?}")
    Log.d("context in init","${context}")
    UMConfigure.init(context, call.argument<Any>("key") as String?, call.argument<Any>("channel") as String?, UMConfigure.DEVICE_TYPE_PHONE, null)
    // 设置日志加密 参数：boolean 默认为false（不加密）

    UMConfigure.setEncryptEnabled((call.argument<Any>("encrypt") as Boolean?)!!)

    val interval = call.argument<Double>("interval")!!
    if (call.argument<Any>("interval") != null) {
      // Session间隔时长,单位是毫秒，默认Session间隔时间是30秒,一般情况下不用修改此值
      MobclickAgent.setSessionContinueMillis(java.lang.Double.valueOf(interval).toLong())
    } else {
      // Session间隔时长,单位是毫秒，默认Session间隔时间是30秒,一般情况下不用修改此值
      MobclickAgent.setSessionContinueMillis(30000L)
    }


    // true表示打开错误统计功能，false表示关闭 默认为打开
    MobclickAgent.setCatchUncaughtExceptions((call.argument<Any>("reportCrash") as Boolean?)!!)

    // 页面采集的两种模式：AUTO和MANUAL，Android 4.0及以上版本使用AUTO,4.0以下使用MANUAL
    val results = call.argument<Int>("mode")!!
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      if (results == 0) {
        // 大于等于4.4选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
      }else{
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
      }
    } else if (results == 1) {
      MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
    }
    result.success(true)
  }
  // 打开页面时进行统计
  private fun beginPageView(call: MethodCall, result: Result) {
    MobclickAgent.onPageStart(call.argument<Any>("name") as String?)
    MobclickAgent.onResume(context)
    result.success(null)
  }

  // 关闭页面时结束统计
  private fun endPageView(call: MethodCall, result: Result) {
    MobclickAgent.onPageEnd(call.argument<Any>("name") as String?)
    MobclickAgent.onPause(context)
    result.success(null)
  }

  // 登陆统计
  private fun loginPageView(call: MethodCall, result: Result) {
    MobclickAgent.onProfileSignIn(call.argument<String>("privider"), call.argument<Any>("id") as String?)
    // Session间隔时长,单位是毫秒，默认Session间隔时间是30秒,一般情况下不用修改此值
    (call.argument<Any>("interval"))?.let { MobclickAgent.setSessionContinueMillis(it as Long) }
    //MobclickAgent.setSessionContinueMillis((call.argument<Any>("interval") as Long?)!!)
  }

  //登出统计
  private fun logOutPageView(call: MethodCall, result: Result) {
    MobclickAgent.onProfileSignOff()
    result.success(null)
  }
  /**
   * 计数事件统计 例如：统计微博应用中”转发”事件发生的次数，那么在转发的函数里调用该函数
   *
   * @param call
   * @param result
   */
  private fun eventCounts(call: MethodCall, result: Result) {
    /**
     * 参数1： context 当前宿主进程的ApplicationContext上下文 参数2： eventId 为当前统计的事件ID 参数3： label
     * 为事件的标签属性
     */
    MobclickAgent.onEvent(context, call.argument<Any>("eventId") as String?, call.argument<Any>("label") as String?)
    result.success(null)
  }

  /**
   * 是否开启错误统计功能
   */
  private fun setaCatchUncaught(call: MethodCall, result: Result) {
    call.argument<Boolean>("catchExceptionIsEnable")?.let {
      MobclickAgent.setCatchUncaughtExceptions(it)
    }
  }
  /**
   * 上报错误，有两种方式：1、错误内容字符串，2、错误内容异常对象
   */
  private fun reporteErr(call: MethodCall, result: Result) {
    var isThrowable = false
    call.argument<Boolean>("isThrowable")?.let {
      isThrowable = it
    }
    if (isThrowable) {
      call.argument<Throwable>("throwable")?.let {
        MobclickAgent.reportError(context, it)
      }
    } else {
      call.argument<String>("errString")?.let {
        MobclickAgent.reportError(context, it)
      }
    }
  }

  /**
   * 自定义事件上传
   */
  private fun customEvent(call: MethodCall, result: Result) {
    val params = mutableMapOf<String, Any>()
    call.argument<Map<String, Any>>("params")?.let {
      params.putAll(it)
    }
    call.argument<String>("eventId")?.let {
      if (params.isEmpty()) {
        Log.d("customEvent", "method:onEvent,eventid:$it,params:${params}")
        MobclickAgent.onEvent(context, it)
      } else {
        Log.d("customEvent", "method:onEventObject,eventid:$it,params:${params}")
        MobclickAgent.onEventObject(context, it, params)
      }
    }
  }
  /**
   * 获取渠道名
   */
  private fun getChannel(call: MethodCall, result: Result) {
    var channel = ""
    try {
      val pm = context?.packageManager
      val appInfo = pm?.getApplicationInfo(context?.packageName, PackageManager.GET_META_DATA)
      appInfo?.metaData?.getString("UMENG_CHANNEL")?.let {
        channel = it
        Log.d("customEvent","channel:$channel")
      }
      result.success(channel)
    } catch (ignored: PackageManager.NameNotFoundException) {
      result.error("getChannel", "${ignored.message}", "${ignored.cause}")
    }

  }

  fun getChannel(context: Context): String? {
    try {
      val pm = context.packageManager
      val appInfo = pm.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
      return appInfo.metaData.getString("UMENG_CHANNEL")
    } catch (ignored: PackageManager.NameNotFoundException) {
    }

    return ""
  }
  /**
   * onEventObject()
   *
   */
  private fun onEventObject(call: MethodCall, result: Result) {
    val params = mutableMapOf<String, Any>()
    if (call.hasArgument("params")) {
      call.argument<Map<String, Any>>("params")?.let {
        params.putAll(it)
      }
    }
    call.argument<String>("eventId")?.let {
      Log.d("customEvent", "method:onEventObject,eventid:$it,params:${params}")
      if (params.isEmpty()) {
        MobclickAgent.onEventObject(context, it, null)
      } else {
        MobclickAgent.onEventObject(context, it, params)
      }
    }
  }

  /**
   * onEvent()
   */
  private fun onEvent(call: MethodCall, result: Result) {
    var label = ""
    if (call.hasArgument("label")) {
      call.argument<String>("label")?.let {
        label = it
      }
    }
    call.argument<String>("eventId")?.let {
      Log.d("customEvent", "method:onEvent,eventId$it,label:$label")
      if (label != "") {
        MobclickAgent.onEvent(context, it)
      } else {
        MobclickAgent.onEvent(context, it, label)
      }
    }

  }
  /**
   * onEventValue()
   */
  private fun onEventValue(call: MethodCall, result: Result) {

  }
  private fun getDeviceInfo(call: MethodCall, result: Result){
    var str = "未成功获取"
    if(call.hasArgument("getDeviceInfo")){
      call.argument<String>("getDeviceInfo")?.let {
        val infos = getTestDeviceInfo(context)
        str = "{\"device_id\":\"${infos[0]}\",\"mac\":\"${infos[1]}}\"";
        Log.d("umenginfo", "{\"device_id\":\"${infos[0]}\",\"mac\":\"${infos[1]}}\"")
      }
    }
    result.success(str)
  }
  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }
}
