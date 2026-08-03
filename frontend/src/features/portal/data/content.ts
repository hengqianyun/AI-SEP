/**
 * 门户静态文案（移植自源站 mock `/api/site|docs|workspace`，无 React / 无运行时 API 依赖）
 */

export const siteSummary = {
  hero: {
    eyebrow: '数据要素流通门户',
    title: '以可信链接促进数据要素高效流通',
    description:
      '围绕主体可信、目录可发现、交易可协同、履约可追溯四个关键环节，帮助生态伙伴快速理解数据流通链方案价值与接入路径。',
  },
  highlights: [
    {
      label: '已链接生态主体',
      value: '128 家',
      description: '覆盖数据提供方、数据服务商、运营方与场景使用方，形成多方协同网络。',
    },
    {
      label: '标准流通链路',
      value: '42 条',
      description: '沉淀从主体认证、目录发布、交易签约到履约清算的标准协作路径。',
    },
    {
      label: '月度履约调用',
      value: '2,846 万次',
      description: '支撑 API 调用、批量交换、回执上报、账单核验与全链路留痕。',
    },
    {
      label: '供需撮合效率提升',
      value: '35%',
      description: '通过标准目录与统一流程，缩短从资源发现到达成交易的沟通链路。',
    },
  ],
  domains: [
    {
      id: 'identity',
      title: '身份标识域',
      summary:
        '支持企业主体认证、签约状态展示、证书信息核验与可信身份同步，先建立“谁可以参与流通”的信任基础。',
      metric: '接入认证平均 2 个工作日完成',
      capabilities: ['主体认证状态可视', '证书与签章链路留痕', '多角色接入权限分层'],
    },
    {
      id: 'catalog',
      title: '数据目录域',
      summary: '沉淀目录编目、标签分类、资产上下架与质量评估能力，让数据资源可发现、可理解、可交易。',
      metric: '目录标准化覆盖率 93%',
      capabilities: ['目录标签与筛选', '资产状态流转', '详情信息统一编排'],
    },
    {
      id: 'orders',
      title: '订单控制域',
      summary: '围绕订单发起、审核、签署、执行与异常处理，构建透明、可协同的交易过程。',
      metric: '订单协同节点缩短 35%',
      capabilities: ['订单全状态追踪', '审批与签署进度透明', '异常回退与补充材料提示'],
    },
    {
      id: 'settlement',
      title: '履约清算域',
      summary: '打通调用统计、对账、结算账单与履约事件，让交易结果可追溯、可核验、可清算。',
      metric: '账单核验效率提升 58%',
      capabilities: ['履约事件可回溯', '账单状态清晰分层', '结算争议快速定位'],
    },
  ],
  features: [
    {
      title: '可信身份先行',
      description: '以主体认证、证书绑定、签约备案为前置条件，先确认参与流通的各方身份与权限。',
      detail: '解决“谁可以提供数据、谁可以使用数据、谁对交易负责”的基础信任问题。',
    },
    {
      title: '标准目录促进发现',
      description: '通过统一编目、标签治理、版本管理和质量说明，让数据资源更容易被检索、比较与理解。',
      detail: '降低供需双方的信息不对称，提升数据产品被发现和撮合成交的效率。',
    },
    {
      title: '订单协同降低摩擦',
      description: '把申请、审核、签署、执行和异常处理串成统一状态机，减少跨机构反复沟通。',
      detail: '让交易过程从线下多轮确认转变为线上可视协同，缩短数据流通达成周期。',
    },
    {
      title: '履约清算形成闭环',
      description: '对调用回执、计费核验、账单确认和结算结果进行统一留痕，保障交易结果可信可核。',
      detail: '让数据要素从“可交易”走向“可持续运营”，支撑后续生态规模化流通。',
    },
  ],
  scenarios: [
    {
      title: '政务数据授权流通',
      description: '围绕公共数据授权、目录发布与履约监管，展示多方在可信规则下完成授权与使用的过程。',
      role: '平台运营方 / 数据提供方 / 使用方',
      outcome: '形成从授权到清算的透明业务链路',
    },
    {
      title: '产业链数据协同',
      description: '针对制造、物流、金融等场景，统一管理供应链数据目录、订单执行状态与履约回执。',
      role: '核心企业 / 服务商 / 金融协作方',
      outcome: '缩短跨系统对接与协同确认周期',
    },
    {
      title: '数据产品运营发布',
      description: '以产品化方式发布 API、报表与数据服务，并同步披露版本、规则与可用状态。',
      role: '产品经理 / 生态伙伴 / 实施团队',
      outcome: '提升目录转化率与版本沟通效率',
    },
  ],
  updates: [
    {
      title: '首页升级为数据流通链门户版式',
      summary: '首屏新增情境图、价值信息与流通闭环表达，首页内容进一步聚焦数据要素流通方案宣传。',
      date: '2026-06-25',
      type: '版本动态',
    },
    {
      title: '标准接入文档进入联调准备阶段',
      summary: '文档目录与快速开始能力持续完善，后续将补充更多接口示例、联调说明与验收检查项。',
      date: '2026-06-20',
      type: '新闻公告',
    },
    {
      title: '接入端工作台持续补强运营视角',
      summary: '总览页与业务模块将继续扩展筛选、列表、步骤型交互与状态演示，强化交易运营与履约跟踪能力。',
      date: '2026-06-18',
      type: '版本动态',
    },
  ],
} as const

export const circulationSteps = [
  {
    title: '主体认证与接入授权',
    detail: '完成主体认证、签约备案和接入授权，建立可信参与前提。',
  },
  {
    title: '目录编目与资源发现',
    detail: '通过标准目录提升资源的发现效率、理解效率和比较效率。',
  },
  {
    title: '交易协同与订单履约',
    detail: '通过统一流程降低供需双方在线下多轮沟通的交易摩擦。',
  },
  {
    title: '清算留痕与持续运营',
    detail: '通过事件、账单和回执实现全过程留痕，支撑后续运营。',
  },
] as const

export const workspaceIntro = {
  session: {
    enterpriseName: '星枢数据科技有限公司',
    role: '运营管理员',
    environment: 'POC 演示环境',
    accessStatus: '已登录 / 主体已签约',
  },
  stats: [
    {
      id: 'enterprise',
      label: '企业接入状态',
      value: '已认证',
      trend: '主体身份校验、签约备案与 DID 绑定均已通过。',
    },
    {
      id: 'catalog',
      label: '目录条目',
      value: '128 项',
      trend: '覆盖金融、政务、风控三类目录，支持筛选与详情查看。',
    },
    {
      id: 'orders',
      label: '进行中订单',
      value: '26 单',
      trend: '待审批、履约中与异常订单可在控制台统一跟踪。',
    },
  ],
  menus: [
    {
      label: '总览驾驶舱',
      description: '查看指标、待办、告警和最近动态。',
    },
    {
      label: '身份与凭证',
      description: '管理主体认证、DID、签约信息与凭证状态。',
    },
    {
      label: '目录与订单',
      description: '筛选目录、处理订单流程并跟踪履约进度。',
    },
    {
      label: '清算与设置',
      description: '查看履约清算账单、日志和系统配置。',
    },
  ],
  demos: [
    {
      label: '目录筛选与详情联动',
      description: '可按分类筛选目录，并查看目录详情、热度、开放策略和标签信息。',
    },
    {
      label: '订单协同与步骤处理',
      description: '支持订单筛选、步骤流展示和处理动作提交，用于验证业务协同闭环。',
    },
    {
      label: '设置与审计日志',
      description: '展示接入参数、通知配置和最近操作记录，贴近真实 SaaS 工作台管理体验。',
    },
  ],
} as const

export const docsSummary = {
  hero: {
    eyebrow: 'Documentation Hub',
    title: '标准接入文档中心',
    description:
      '面向开发者、实施顾问与评审方的标准接入文档简介，覆盖接入总览、快速开始与业务域说明，帮助团队建立统一接入认知。',
    version: 'v0.3.0',
    updatedAt: '2026-06-25',
    audience: ['开发负责人', '实施顾问', '测试联调', '方案评审'],
  },
  support: {
    owner: '标准接入方案组',
  },
  stats: [
    {
      label: '章节覆盖',
      value: '10 个',
      detail: '首页、总览、快速开始、四大业务域、SDK、FAQ、更新日志完整齐备。',
    },
    {
      label: '接入耗时',
      value: '15 分钟',
      detail: '通过演示环境完成本地启动、调试脚本与示例代码走查。',
    },
    {
      label: '示例资产',
      value: '6 份',
      detail: '包含脚本命令、接口约定、SDK 资源与前端调用示例。',
    },
  ],
  sections: [
    {
      id: 'home',
      title: '文档首页',
      description: '快速了解接入文档中心的结构、适用角色与阅读路径。',
      badge: 'Start',
    },
    {
      id: 'overview',
      title: '接入总览',
      description: '介绍平台定位、能力边界、前置条件与标准接入流程。',
      badge: 'Core',
    },
    {
      id: 'quick-start',
      title: '快速开始',
      description: '说明本地启动、联调命令与交付检查项。',
      badge: '15 min',
    },
    {
      id: 'identity',
      title: '身份标识域',
      description: '主体认证、证书绑定、签约状态与接入凭证管理。',
      badge: 'P0',
    },
    {
      id: 'catalog',
      title: '数据目录域',
      description: '目录上架、字段审查、标签治理与可见范围控制。',
      badge: 'P0',
    },
    {
      id: 'sdk',
      title: 'SDK / 示例代码',
      description: '汇总 SDK 资产、请求签名、脚本命令与前端示例代码。',
      badge: 'Dev',
    },
  ],
  overviewIntro: [
    '标准接入文档中心用于统一沉淀产品能力边界、业务域职责、联调步骤与示例代码，降低评审与交接成本。',
    '本页为最小可用简介：呈现覆盖范围与阅读路径；完整内容树可在后续迭代接入真实文档服务。',
  ],
  quickStart: [
    {
      id: 'install',
      title: '安装依赖',
      description: '初始化工程并锁定当前 POC 所需依赖版本。',
    },
    {
      id: 'dev',
      title: '启动本地环境',
      description: '运行开发服务器，访问门户与接入端工作台。',
    },
    {
      id: 'quality',
      title: '执行质量校验',
      description: '在提交前执行测试、类型检查与构建验证。',
    },
  ],
} as const
