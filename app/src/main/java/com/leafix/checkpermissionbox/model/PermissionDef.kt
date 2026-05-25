package com.leafix.checkpermissionbox.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leafix.checkpermissionbox.R

/**
 * 权限请求方式
 *
 * - [SettingsIntent]: 跳转系统设置页面引导授权（如 MANAGE_EXTERNAL_STORAGE）
 * - [RuntimePermission]: 通过系统对话框申请运行时权限（如 CAMERA、LOCATION）
 */
sealed class RequestMethod {
    /**
     * 跳转系统设置页面
     *
     * @param createIntent 创建跳转 Intent 的 lambda
     */
    data class SettingsIntent(val createIntent: (context: Context) -> Intent) : RequestMethod()

    /**
     * 系统对话框运行时权限
     *
     * @param permissionName Manifest.permission 常量名
     */
    data class RuntimePermission(val permissionName: String) : RequestMethod()
}

/**
 * 权限请求执行器
 *
 * 封装了 ActivityResultLauncher 的创建和请求逻辑，
 * 对外提供统一的 [launch] 方法。
 * UI 组件无需关心底层使用的是 [SettingsIntent] 还是 [RuntimePermission]。
 *
 * @param launch 执行权限请求的回调，接收 Context 参数
 */
class PermissionLauncher(
    val launch: (Context) -> Unit
)

/**
 * [RequestMethod] 的 Compose 扩展函数
 *
 * 根据请求方式创建并记住对应的 ActivityResultLauncher，
 * 返回统一的 [PermissionLauncher] 供调用方使用。
 *
 * @param onResult 用户响应权限请求后的回调（用于重新检查权限状态）
 * @return [PermissionLauncher] 封装后的请求执行器
 */
@Composable
fun RequestMethod.rememberLauncher(
    onResult: () -> Unit
): PermissionLauncher {
    return when (this) {
        is RequestMethod.SettingsIntent -> {
            // 创建跳转系统设置页面的 Launcher
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { onResult() }
            PermissionLauncher { context ->
                launcher.launch(createIntent(context))
            }
        }
        is RequestMethod.RuntimePermission -> {
            // 创建系统运行时权限对话框的 Launcher
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { onResult() }
            PermissionLauncher { context ->
                launcher.launch(permissionName)
            }
        }
    }
}

/**
 * 权限定义数据类
 *
 * 描述一个权限的所有必要信息,包括显示文本、系统版本要求、
 * 权限状态检查逻辑和申请方法。
 * 通过此模型,通用组件 [PermissionItem] 可以渲染任意类型的权限条目。
 *
 * @param nameResId 权限名称字符串资源 ID
 * @param descriptionResId 权限用途描述字符串资源 ID
 * @param requiredApiTextResId 适用的最低系统版本提示字符串资源 ID
 * @param minSdk 该权限的最低 API 级别
 * @param checkPermission 检查权限是否已授予的 lambda,接收 Context 参数,返回 true=已授权
 * @param requestMethod 权限请求方式 [SettingsIntent] 或 [RuntimePermission]
 */
data class PermissionDef(
    val nameResId: Int,
    val descriptionResId: Int,
    val requiredApiTextResId: Int,
    val minSdk: Int,
    val checkPermission: (Context) -> Boolean,
    val requestMethod: RequestMethod
) {
    companion object {
        // ====================================================================
        // 特殊权限（需跳转系统设置）
        // ====================================================================

        /**
         * MANAGE_EXTERNAL_STORAGE
         *
         * Android 11+ 外置存储全部文件读写权限。
         * 通过 Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION 引导授权。
         */
        val MANAGE_EXTERNAL_STORAGE = PermissionDef(
            nameResId = R.string.manage_storage_permission,
            descriptionResId = R.string.manage_storage_permission_desc,
            requiredApiTextResId = R.string.manage_storage_required_api,
            minSdk = Build.VERSION_CODES.R,
            checkPermission = { _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    false
                }
            },
            requestMethod = RequestMethod.SettingsIntent { context ->
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        )

        // ====================================================================
        // 运行时权限（通过系统对话框申请）
        // ====================================================================

        /**
         * CAMERA
         *
         * 相机权限，始终可用的运行时权限。
         */
        val CAMERA = PermissionDef(
            nameResId = R.string.permission_camera,
            descriptionResId = R.string.permission_camera_desc,
            requiredApiTextResId = R.string.permission_all_api,
            minSdk = 1,
            checkPermission = { ctx ->
                checkRuntimePermission(ctx, Manifest.permission.CAMERA)
            },
            requestMethod = RequestMethod.RuntimePermission(Manifest.permission.CAMERA)
        )

        /**
         * ACCESS_FINE_LOCATION
         *
         * 精确定位权限，始终可用的运行时权限。
         */
        val ACCESS_FINE_LOCATION = PermissionDef(
            nameResId = R.string.permission_location,
            descriptionResId = R.string.permission_location_desc,
            requiredApiTextResId = R.string.permission_all_api,
            minSdk = 1,
            checkPermission = { ctx ->
                checkRuntimePermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
            },
            requestMethod = RequestMethod.RuntimePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        )

        /**
         * RECORD_AUDIO
         *
         * 录音权限，始终可用的运行时权限。
         */
        val RECORD_AUDIO = PermissionDef(
            nameResId = R.string.permission_microphone,
            descriptionResId = R.string.permission_microphone_desc,
            requiredApiTextResId = R.string.permission_all_api,
            minSdk = 1,
            checkPermission = { ctx ->
                checkRuntimePermission(ctx, Manifest.permission.RECORD_AUDIO)
            },
            requestMethod = RequestMethod.RuntimePermission(Manifest.permission.RECORD_AUDIO)
        )

        /**
         * POST_NOTIFICATIONS
         *
         * 通知权限 - Android 13+ 运行时权限方式。
         * 通过系统对话框申请 POST_NOTIFICATIONS 运行时权限。
         */
        val POST_NOTIFICATIONS = PermissionDef(
            nameResId = R.string.permission_notification,
            descriptionResId = R.string.permission_notification_desc,
            requiredApiTextResId = R.string.permission_required_api_33,
            minSdk = Build.VERSION_CODES.TIRAMISU,
            checkPermission = { ctx ->
                checkRuntimePermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            },
            requestMethod = RequestMethod.RuntimePermission(Manifest.permission.POST_NOTIFICATIONS)
        )

        /**
         * NOTIFICATION_SETTINGS
         *
         * 通知设置 - 所有版本通用方式。
         * 通过 Settings.ACTION_APP_NOTIFICATION_SETTINGS 跳转系统通知设置页面管理。
         */
        val NOTIFICATION_SETTINGS = PermissionDef(
            nameResId = R.string.permission_notification,
            descriptionResId = R.string.permission_notification_desc,
            requiredApiTextResId = R.string.permission_all_api,
            minSdk = 1,
            checkPermission = { ctx ->
                NotificationManagerCompat.from(ctx).areNotificationsEnabled()
            },
            requestMethod = RequestMethod.SettingsIntent { context ->
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            }
        )

        /**
         * READ_MEDIA_IMAGES
         *
         * 读取媒体图片权限，Android 13 (API 33) 替代 READ_EXTERNAL_STORAGE。
         */
        val READ_MEDIA_IMAGES = PermissionDef(
            nameResId = R.string.permission_read_media_images,
            descriptionResId = R.string.permission_read_media_images_desc,
            requiredApiTextResId = R.string.permission_required_api_33,
            minSdk = Build.VERSION_CODES.TIRAMISU,
            checkPermission = { ctx ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkRuntimePermission(ctx, Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    // API 33 以下使用 READ_EXTERNAL_STORAGE 检查
                    checkRuntimePermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            },
            requestMethod = RequestMethod.RuntimePermission(Manifest.permission.READ_MEDIA_IMAGES)
        )

        /**
         * BLUETOOTH_CONNECT
         *
         * 蓝牙连接权限，Android 12 (API 31) 引入的运行时权限。
         */
        val BLUETOOTH_CONNECT = PermissionDef(
            nameResId = R.string.permission_bluetooth,
            descriptionResId = R.string.permission_bluetooth_desc,
            requiredApiTextResId = R.string.permission_required_api_31,
            minSdk = Build.VERSION_CODES.S,
            checkPermission = { ctx ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkRuntimePermission(ctx, Manifest.permission.BLUETOOTH_CONNECT)
                } else {
                    // API 31 以下无需此运行时权限
                    true
                }
            },
            requestMethod = RequestMethod.RuntimePermission(Manifest.permission.BLUETOOTH_CONNECT)
        )

        /**
         * 所有已定义的权限列表
         *
         * 按预期显示顺序排列。新增权限时只需在此列表中添加定义即可。
         */
        val ALL_PERMISSIONS: List<PermissionDef> = listOf(
            MANAGE_EXTERNAL_STORAGE,
            CAMERA,
            ACCESS_FINE_LOCATION,
            RECORD_AUDIO,
            POST_NOTIFICATIONS,
            NOTIFICATION_SETTINGS,
            READ_MEDIA_IMAGES,
            BLUETOOTH_CONNECT
        )

        /**
         * 通用的运行时权限检查方法
         *
         * @param context Context
         * @param permission Manifest.permission 常量
         * @return true=已授权
         */
        private fun checkRuntimePermission(context: Context, permission: String): Boolean {
            val result: Int = ContextCompat.checkSelfPermission(context, permission)
            return result == PackageManager.PERMISSION_GRANTED
        }
    }
}
