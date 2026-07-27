# 项目实例化任务模板索引

本目录为 [`PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`](../PROJECT-CUSTOMIZATION-ASSIGNMENTS.md) 中每个任务提供可复制参考。

这些文件：

- 仅供产品、前端、后端、测试和协调人**人工填写**
- 不属于运行时 Rule/Skill，Orchestrator 不应装载
- 不预设具体业务或技术栈；`<待填写>`、`REPLACE_ME` 必须在下游替换
- 不适用项必须写 `N/A` 和原因，不得保留模糊占位后标记完成

| 任务范围 | 模板 |
|---|---|
| P-01～P-09 | [`PRODUCT.md`](./PRODUCT.md) |
| FE-01～FE-08 | [`FRONTEND.md`](./FRONTEND.md) |
| BE-01～BE-11 | [`BACKEND.md`](./BACKEND.md) |
| QA-01～QA-07 | [`QA.md`](./QA.md) |
| C-01～C-07 | [`COORDINATOR.md`](./COORDINATOR.md) |
| OPT-01～OPT-05 | [`OPTIONAL.md`](./OPTIONAL.md) |

使用方式：

1. 用 **Markdown 预览** 打开 `PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`，再点击 Template（源码视图里 Ctrl+点击对跨文件 `#锚点` 经常无效）。
2. 将该章节复制到目标交付物或工作 PR。
3. 由任务 R 负责人填写，C 角色复核，A 角色批准。
4. 按章节“完成检查”验收。
5. 只有真正的 Rule/Skill 文件才按流程从 `draft` 改为 `active`。

## 关于直达链接

任务标题统一为 `## P-01`、`## FE-01` 等形式，以便预览根据标题生成 `#p-01`、`#fe-01` 锚点。

- 分配表链接形如 `./customization-templates/PRODUCT.md#p-01`，与上述标题对应。
- 若仍停在文件顶部：打开目标文件后，用编辑器大纲跳到对应任务标题。
- 不要使用单独的 `<a id="...">`；Cursor / VS Code 预览通常不认。
