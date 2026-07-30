# 接入端工作台（WSC）术语表

| 术语 | 英文/代码标识 | 定义 | 边界/不包含 | 易混淆术语 | 维护人 |
|---|---|---|---|---|---|
| 接入端工作台 | Workspace Console / WSC | 可信数据空间接入端运营与提供方使用的 Web 控制台 | 不含连接器运行时本体、不含交易撮合引擎 | 数据空间门户 | productOwner |
| 数据目录 | Data Catalog | 已登记数据产品的结构化索引与检索视图 | 不含「数据登记」独立业务流（本版 Out of Scope） | 数据登记 | productOwner |
| 数据产品 | Data Product | 可上架共享的数据集、报告、接口或其他产品条目 | 不含未登记的原始库表 | SKU / 商品 | productOwner |
| 行业分类 | Industry Category | V1.1 起为三级：一级空间 → 二级行业 → 三级子类；用于浏览、筛选与挂载 | 不含产品类型枚举 | 产品类型；空间 | productOwner |
| 一级分类（空间） | Domain / Space | 分类树顶层，如「可信空间」「航贸数字化」 | 不等于企业租户或多组织树 | 二级行业 | productOwner |
| 二级分类（行业） | Industry | 挂在某一级空间下的行业节点 | 不含三级子类 | 一级空间 | productOwner |
| 三级分类（子类） | Subcategory | 挂在某二级下的子类；**产品默认挂载点** | 不含产品本身 | 二级行业 | productOwner |
| 目录维护 | Catalog Maintenance | 管理员将目录/产品条目关联到三级路径的运营页（含待关联） | 不含「数据登记」独立业务流 | 数据目录浏览 | productOwner |
| 批量导入 | Batch Import | 通过 xlsx/csv 模板批量创建数据产品 | 不含连接器同步或外部系统全量 ETL | 新增产品（单条） | productOwner |
| 上链 / 存证 | Chain Deposit | 对产品元数据生成版本化存证记录（哈希、DID、时间戳） | V1 不含真实链共识确认 | 交易上链 | techLead |
| 权属证书 | Ownership Certificate | 与存证关联的权属方与时间戳展示信息 | 不含法定产权登记机关对接 | 数据产权登记证书编号 | productOwner |
| 管理员 | Admin | 可维护三级分类与目录关联，并增改/导入产品的运营角色 | 不等于系统超级管理员多租户配置 | 提供方 | productOwner |
| 提供方 | Provider | 可增改/导入数据产品、不可维护分类与目录关联 | 不等于普通浏览用户 | 管理员 | productOwner |
| 普通用户 | User | 只读浏览总览、目录、详情与上链信息 | 无写权限 | 提供方 | productOwner |

## 待确认术语

- 「目录维护」条目与「数据产品」是否永久同一实体：见 SNAP-WSC-002 OQ-V11-003（暂定同一可关联条目）。
