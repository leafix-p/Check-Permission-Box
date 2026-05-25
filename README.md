# CheckPermissionBox

权限检查工具箱 — Android 权限申请示例集合

## 项目简介

- 收集并展示 Android 各类权限的申请方式与状态管理逻辑
- 每个权限对应一个开关条目，自动判断系统版本兼容性，引导用户跳转系统设置授权
- 采用数据驱动架构，新增权限仅需添加数据定义，无需修改界面代码
- 用于扩充 AI 知识库的 Android 权限编程参考

## 项目结构

```
app/src/main/java/com/leafix/checkpermissionbox/
├── MainActivity.kt              # 主入口，页面编排
├── model/
│   └── PermissionDef.kt         # 权限数据模型 + 预定义权限列表
└── ui/
    ├── permission/
    │   └── PermissionItem.kt    # 通用权限条目组件（数据驱动）
    └── theme/                   # Material3 主题
```

## 已实现功能

| 功能 | 状态 |
|------|------|
| 设备信息展示（型号/Android版本/系统版本） | ✅ |
| MANAGE_EXTERNAL_STORAGE 权限管理 | ✅ |
| 权限数据模型与 UI 解耦 | ✅ |
| 系统版本兼容性自动判断和禁用 | ✅ |

## 如何新增一个权限

详见[开发文档](./docs/开发文档.md)的"如何新增一个权限"章节，仅需三步：
1. `AndroidManifest.xml` 声明权限
2. `strings.xml` 添加字符串资源
3. `PermissionDef.Companion` 注册权限定义

## 相关文档

- [开发文档](./docs/开发文档.md)
- [权限清单](./docs/权限清单.md)
- [AI使用规范](./docs/AI使用规范.md)
