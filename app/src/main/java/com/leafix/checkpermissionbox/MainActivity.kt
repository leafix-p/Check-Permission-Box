package com.leafix.checkpermissionbox

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leafix.checkpermissionbox.model.PermissionDef
import com.leafix.checkpermissionbox.model.PermissionGroup
import com.leafix.checkpermissionbox.ui.permission.PermissionItem
import com.leafix.checkpermissionbox.ui.theme.CheckPermissionBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckPermissionBoxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionList(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * 权限列表主界面
 *
 * 顶部显示设备基础信息，下方按分类分组显示权限条目，
 * 每组包含一个分类标题和对应的权限开关列表。
 */
@Composable
fun PermissionList(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        // 顶部：设备基础信息区域
        item {
            DeviceInfoSection()
        }

        // 分割线
        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 按分类遍历权限分组
        PermissionDef.GROUPED_PERMISSIONS.forEach { group ->
            // 分类标题
            item {
                CategoryHeader(titleResId = group.titleResId)
            }

            // 该分类下的权限条目
            items(group.permissions.size) { index ->
                PermissionItem(permission = group.permissions[index])
            }
        }
    }
}

/**
 * 分类标题组件
 *
 * @param titleResId 分类名称字符串资源 ID
 */
@Composable
fun CategoryHeader(titleResId: Int) {
    Text(
        text = stringResource(titleResId),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

/**
 * 设备基础信息展示组件
 *
 * 显示当前设备的基本信息，包括：
 * - 设备型号 (Build.MODEL)
 * - Android 版本 (Build.VERSION.RELEASE)
 * - 系统镜像版本 (Build.DISPLAY)
 */
@Composable
fun DeviceInfoSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 区域标题
            Text(
                text = stringResource(R.string.device_info_section),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 设备型号
            DeviceInfoRow(
                label = stringResource(R.string.device_model_label),
                value = Build.MODEL
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Android 版本
            DeviceInfoRow(
                label = stringResource(R.string.android_version_label),
                value = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 系统镜像版本
            DeviceInfoRow(
                label = stringResource(R.string.build_version_label),
                value = Build.DISPLAY
            )
        }
    }
}

/**
 * 设备信息单行展示组件
 *
 * @param label 信息标签（如"设备型号"）
 * @param value 信息值（如"Pixel 8"）
 */
@Composable
fun DeviceInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionListPreview() {
    CheckPermissionBoxTheme {
        PermissionList()
    }
}
