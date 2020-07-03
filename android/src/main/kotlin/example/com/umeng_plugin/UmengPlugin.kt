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
public class UmengPlugin : FlutterPlugin, MethodCallHandler {
    private var isLogEnable: Boolean = false
    private var context: Context? = null
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val umengPlugin = UmengPlugin()
        umengPlugin.context = flutterPluginBinding.applicationContext;
        val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), UMENG_PLUGIN)
        channel.setMethodCallHandler(umengPlugin)
        val infos = getTestDeviceInfo(flutterPluginBinding.applicationContext)
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
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val umengPlugin = UmengPlugin()
            umengPlugin.context = registrar.activity()
            val channel = MethodChannel(registrar.messenger(), UMENG_PLUGIN)
            channel.setMethodCallHandler(umengPlugin)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            GET_PLATFORM_VERSION -> result.success("Android ${Build.VERSION.RELEASE}")
            INIT -> init(call, result)
            BEGING_PAGE_VIEW -> beginPageView(call, result)
            END_PAGE_VIEW -> endPageView(call, result)
            PROFILE_SIGNIN -> profileSignIn(call, result)
            PROFILE_SIGNOFF -> logOutPageView(call, result)
            EVENT_COUNT -> eventCounts(call, result)
            SET_CATCH_UNCAUGHT -> setCatchUncaught(call, result)
            REPORT_ERR -> reporteErr(call, result)
            CUSTOM_EVENT -> customEvent(call, result)
            GET_CHANNEL -> getChannel(call, result)
            GET_DEVICE_INFO -> getDeviceInfo(call, result)
            else -> result.notImplemented()
        }
    }

    private fun init(call: MethodCall, result: Result) {
        /**
         * 初始化common库 参数1：上下文，不能为空 参数2：【友盟+】Appkey名称 参数3：【友盟+】Channel名称
         * 参数4：设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
         * 参数5：Push推送业务的secret
         */
        UMConfigure.init(context, call.argument<Any>(KEY) as String?, call.argument<Any>(CHANNEL) as String?, UMConfigure.DEVICE_TYPE_PHONE, null)
        call.argument<Double>(INTERVAL).let {
            if (it == null) MobclickAgent.setSessionContinueMillis(30000L) else MobclickAgent.setSessionContinueMillis(java.lang.Double.valueOf(it).toLong())
        }

        // 设置组件化的Log开关，参数默认为false，如需查看LOG设置为true
        call.argument<Boolean>(LOG_ENABLE)?.let {
            isLogEnable = it
            UMConfigure.setLogEnabled(it)
        }
        // 设置日志加密 参数：boolean 默认为false（不加密）
        call.argument<Boolean>(ENCRYPT)?.let {
            UMConfigure.setEncryptEnabled(it)
        }
        call.argument<Boolean>(REPORT_CRASH)?.let {
            MobclickAgent.setCatchUncaughtExceptions(it)
        }

        // 页面采集的两种模式：AUTO和MANUAL，Android 4.0及以上版本使用AUTO,4.0以下使用MANUAL
        call.argument<Int>(MODE).let {
            val isLegacy = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            if (it == 0) {
                MobclickAgent.setPageCollectionMode(if (isLegacy) MobclickAgent.PageMode.AUTO else MobclickAgent.PageMode.LEGACY_AUTO)
            } else {
                MobclickAgent.setPageCollectionMode(if (isLegacy) MobclickAgent.PageMode.MANUAL else MobclickAgent.PageMode.LEGACY_MANUAL)
            }
        }
        result.success(true)
    }

    // 打开页面时进行统计
    private fun beginPageView(call: MethodCall, result: Result) {
        MobclickAgent.onPageStart(call.argument<Any>(NAME) as String?)
        MobclickAgent.onResume(context)
        result.success(null)
    }

    // 关闭页面时结束统计
    private fun endPageView(call: MethodCall, result: Result) {
        MobclickAgent.onPageEnd(call.argument<Any>(NAME) as String?)
        MobclickAgent.onPause(context)
        result.success(null)
    }

    // 登陆统计
    private fun profileSignIn(call: MethodCall, result: Result) {
        MobclickAgent.onProfileSignIn(call.argument(PROVIDER), call.argument(ID))
        // Session间隔时长,单位是毫秒，默认Session间隔时间是30秒,一般情况下不用修改此值
        call.argument<Long>(INTERVAL)?.let { MobclickAgent.setSessionContinueMillis(it) }
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
        MobclickAgent.onEvent(context, call.argument(EVENT_ID), call.argument<String>(LABEL))
        result.success(null)
    }

    /**
     * 是否开启错误统计功能
     */
    private fun setCatchUncaught(call: MethodCall, result: Result) {
        call.argument<Boolean>(CATCH_EXCEPTION_IS_ENABLE)?.let {
            MobclickAgent.setCatchUncaughtExceptions(it)
        }
    }

    /**
     * 上报错误，有两种方式：1、错误内容字符串，2、错误内容异常对象
     */
    private fun reporteErr(call: MethodCall, result: Result) {
        var isThrowable = false
        call.argument<Boolean>(IS_THROWABLE)?.let {
            isThrowable = it
        }
        if (isThrowable) {
            call.argument<Throwable>(THROWABLE)?.let {
                MobclickAgent.reportError(context, it)
            }
        } else {
            call.argument<String>(ERR_STRING)?.let {
                MobclickAgent.reportError(context, it)
            }
        }
    }

    /**
     * 自定义事件上传
     */
    private fun customEvent(call: MethodCall, result: Result) {
        val params = mutableMapOf<String, Any>()
        call.argument<Map<String, Any>>(PARAMS)?.let {
            params.putAll(it)
        }
        call.argument<String>(EVENT_ID)?.let {
            if (params.isEmpty()) {
                log("method:onEvent,eventid:$it,params:${params}")
                MobclickAgent.onEvent(context, it)
            } else {
                log("method:onEventObject,eventid:$it,params:${params}")
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
            appInfo?.metaData?.getString(UMENG_CHANNEL)?.let {
                channel = it
                log("channel:$channel")
            }
            result.success(channel)
        } catch (ignored: PackageManager.NameNotFoundException) {
            result.error(GET_CHANNEL, "${ignored.message}", "${ignored.cause}")
        }

    }

    /**
     * onEventObject()
     *
     */
    private fun onEventObject(call: MethodCall, result: Result) {
        val params = mutableMapOf<String, Any>()
        if (call.hasArgument(PARAMS)) {
            call.argument<Map<String, Any>>(PARAMS)?.let {
                params.putAll(it)
            }
        }
        call.argument<String>(EVENT_ID)?.let {
            log("method:onEventObject,eventid:$it,params:${params}")
            if (params.isEmpty()) {
                // todo:错误调用
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
        if (call.hasArgument(LABEL)) {
            call.argument<String>(LABEL)?.let {
                label = it
            }
        }
        call.argument<String>(EVENT_ID)?.let {
            log("method:onEvent,eventId$it,label:$label")
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

    private fun getDeviceInfo(call: MethodCall, result: Result) {
        var str = "未成功获取"
        call.method?.let {
            val infos = getTestDeviceInfo(context)
            str = "{\"device_id\":\"${infos[0]}\",\"mac\":\"${infos[1]}}\""
            log(str)
        }
        result.success(str)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }

    private fun log(message: String) {
        if (isLogEnable) Log.d("umengPlugin", message)
    }
}
