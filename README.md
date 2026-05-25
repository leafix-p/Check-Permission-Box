# CheckPermissionBox

权限检查工具箱 — Android 权限申请示例集合

## 项目简介

- 收集并展示 Android 各类权限的申请方式与状态管理逻辑
- 每个权限对应一个开关条目，自动判断系统版本兼容性
- 支持两类权限申请方式：**系统运行时权限对话框** 和 **跳转系统设置引导授权**
- 采用解耦的**数据驱动架构**，新增权限仅需添加数据定义，无需修改界面代码
- 用于扩充 AI 知识库的 Android 权限编程参考

## 项目结构

```
app/src/main/java/com/leafix/checkpermissionbox/
├── MainActivity.kt              # 主入口，页面编排
├── model/
│   └── PermissionDef.kt         # 权限数据模型 + RequestMethod 密封类 + 18 个预定义权限
└── ui/
    ├── permission/
    │   └── PermissionItem.kt    # 通用权限条目组件（数据驱动）
    └── theme/                   # Material3 主题
```

## 已实现权限（18 个）

### 特殊权限
| 权限 | 最低 API | 申请方式 |
|------|----------|----------|
| 管理所有文件 (MANAGE_EXTERNAL_STORAGE) | API 30 | 跳转系统设置 |

### 媒体与传感器
| 权限 | 最低 API |
|------|----------|
| 相机 (CAMERA) | 全部 |
| 麦克风 (RECORD_AUDIO) | 全部 |
| 媒体图片 (READ_MEDIA_IMAGES) | API 33 |
| 媒体视频 (READ_MEDIA_VIDEO) | API 33 |
| 媒体音频 (READ_MEDIA_AUDIO) | API 33 |

### 位置
| 权限 | 最低 API |
|------|----------|
| 精确位置 (ACCESS_FINE_LOCATION) | 全部 |
| 粗略位置 (ACCESS_COARSE_LOCATION) | 全部 |
| 后台位置 (ACCESS_BACKGROUND_LOCATION) | API 29 |

### 通讯与数据
| 权限 | 最低 API |
|------|----------|
| 通讯录 (READ_CONTACTS) | 全部 |
| 日历 (READ_CALENDAR) | 全部 |
| 短信 (READ_SMS) | 全部 |
| 设备信息 (READ_PHONE_STATE) | 全部 |
| 身体传感器 (BODY_SENSORS) | 全部 |

### 通知
| 权限 | 最低 API | 说明 |
|------|----------|------|
| 通知 (POST_NOTIFICATIONS) | API 33 | 系统对话框授权 |
| 通知设置 (NOTIFICATION_SETTINGS) | 全部 | 跳转设置页管理 |

### 蓝牙
| 权限 | 最低 API |
|------|----------|
| 蓝牙连接 (BLUETOOTH_CONNECT) | API 31 |
| 蓝牙扫描 (BLUETOOTH_SCAN) | API 31 |

## 架构设计

```
PermissionDef (数据层)  →  requestMethod.rememberLauncher()  →  PermissionLauncher
     ↓ 数据驱动                                                   ↓ 统一调用
PermissionItem (UI层)  ─────────────────────────────────────  permissionLauncher.launch()
```

- **`RequestMethod.SettingsIntent`**：跳转系统设置（特殊权限）
- **`RequestMethod.RuntimePermission`**：系统对话框（运行时权限）
- **`PermissionLauncher`**：统一的请求执行器，UI 层无需关心底层差异

## 如何新增一个权限

详见[开发文档](./docs/开发文档.md)的"如何新增一个权限"章节，仅需三步：
1. `AndroidManifest.xml` 声明权限
2. `strings.xml` 添加字符串资源
3. `PermissionDef.Companion` 注册权限定义并加入 `ALL_PERMISSIONS` 列表

## 相关文档

- [开发文档](./docs/开发文档.md)
- [权限清单](./docs/权限清单.md)
- [AI使用规范](./docs/AI使用规范.md)
