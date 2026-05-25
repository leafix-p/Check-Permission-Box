package com.leafix.checkpermissionbox.ui.permission

import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.leafix.checkpermissionbox.model.PermissionDef

/**
 * 通用权限条目组件
 *
 * 根据 [PermissionDef] 数据渲染一个权限管理条目,包含:
 * - 权限名称、描述、最低系统版本要求
 * - 权限状态开关(Switch)
 * - 当设备系统版本不满足最低要求时,整行半透明禁用
 * - 通过 ActivityResultLauncher 和 onResume 生命周期实时刷新权限状态
 *
 * 此组件完全由数据驱动,新增权限只需定义 [PermissionDef] 实例即可复用。
 *
 * @param permission 权限定义数据
 * @param modifier Modifier
 */
@Composable
fun PermissionItem(
    permission: PermissionDef,
    modifier: Modifier = Modifier
) {
    // 判断当前设备是否满足最低 API 要求
    val isSupported = Build.VERSION.SDK_INT >= permission.minSdk

    // 获取当前 context,用于启动 Intent
    val context = LocalContext.current

    // 权限授权状态:true=已授权,false=未授权
    var isGranted by remember { mutableStateOf(permission.checkPermission()) }

    // 注册 ActivityResultLauncher,用于监听从系统设置页面返回的结果
    val requestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // 用户从设置页面返回后,重新检查权限状态
        isGranted = permission.checkPermission()
    }

    // 监听生命周期事件,在 onResume 时重新检查权限状态
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = permission.checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 权限条目布局:名称/描述/版本要求(左) + 状态开关(右)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            // 设备不满足最低 API 要求时,整行半透明显示
            .alpha(if (isSupported) 1f else 0.4f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧:权限名称、描述、适用版本提示
        PermissionInfo(
            name = stringResource(permission.nameResId),
            description = stringResource(permission.descriptionResId),
            requiredApiText = stringResource(permission.requiredApiTextResId),
            modifier = Modifier.weight(1f)
        )

        // 右侧:开关
        Switch(
            checked = if (isSupported) isGranted else false,
            onCheckedChange = if (isSupported) { targetChecked ->
                if (targetChecked && !isGranted) {
                    // 关闭 -> 打开: 创建并启动授权引导 Intent
                    val intent: Intent? = permission.createRequestIntent(context)
                    if (intent != null) {
                        requestLauncher.launch(intent)
                    }
                }
            } else null
        )
    }
}

/**
 * 权限信息展示组件
 *
 * 显示权限名称、描述文本以及适用的最低系统版本。
 *
 * @param name 权限名称
 * @param description 权限用途描述
 * @param requiredApiText 适用的最低系统版本提示（如"需要 Android 11 (API 30) 及以上"）
 * @param modifier Modifier
 */
@Composable
private fun PermissionInfo(
    name: String,
    description: String,
    requiredApiText: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 权限名称
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        // 权限描述
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // 最低系统版本提示
        if (requiredApiText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = requiredApiText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
