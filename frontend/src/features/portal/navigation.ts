export interface PortalNavItem {
  label: string
  path: string
  description: string
}

/** 门户顶栏导航（对齐源站 siteNavigation） */
export const portalNavigation: PortalNavItem[] = [
  {
    label: '首页',
    path: '/',
    description: '门户概览与接入入口',
  },
  {
    label: '标准接入文档',
    path: '/docs',
    description: '简介与查看入口',
  },
  {
    label: '接入端简介',
    path: '/workspace',
    description: '简介与打开入口',
  },
]
