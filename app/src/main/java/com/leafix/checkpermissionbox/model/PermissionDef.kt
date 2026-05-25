package com.leafix.checkpermissionbox.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.leafix.checkpermissionbox.R

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
 * @param checkPermission 检查权限是否已授予的 lambda,返回 true=已授权
 * @param createRequestIntent 创建跳转系统设置引导授权 Intent 的 lambda,
 *                            若返回 null 则表示无需跳转
 */
data class PermissionDef(
    val nameResId: Int,
    val descriptionResId: Int,
    val requiredApiTextResId: Int,
    val minSdk: Int,
    val checkPermission: () -> Boolean,
    val createRequestIntent: (context: Context) -> Intent?
) {
    companion object {
        /**
         * MANAGE_EXTERNAL_STORAGE 权限定义
         *
         * Android 11+ 外置存储全部文件读写权限。
         * 通过 Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION 引导授权。
         */
        val MANAGE_EXTERNAL_STORAGE = PermissionDef(
            nameResId = R.string.manage_storage_permission,
            descriptionResId = R.string.manage_storage_permission_desc,
            requiredApiTextResId = R.string.manage_storage_required_api,
            minSdk = Build.VERSION_CODES.R,
            checkPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    false
                }
            },
            createRequestIntent = { context ->
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        )

        /**
         * 所有已定义的权限列表
         *
         * 按预期显示顺序排列。新增权限时只需在此列表中添加定义即可。
         */
        val ALL_PERMISSIONS: List<PermissionDef> = listOf(
            MANAGE_EXTERNAL_STORAGE
        )
    }
}
