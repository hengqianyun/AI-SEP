import type { RouteRecordRaw } from 'vue-router'
import PlaceholderView from '@/views/PlaceholderView.vue'

/**
 * 根路由清单（TASK-WSC-001 唯一写；后续任务只读）。
 * 对齐 PLAN §3.1 路由清单与 §3.5 UI 状态矩阵引用。
 */
export const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/overview' },
  {
    path: '/overview',
    name: 'overview',
    component: PlaceholderView,
    meta: { title: '总览', hint: '占位：总览指标/趋势/流/分布由 TASK-WSC-003 实现。' },
  },
  {
    path: '/catalog',
    name: 'catalog',
    component: PlaceholderView,
    meta: { title: '数据目录', hint: '占位：目录浏览由 TASK-WSC-004 实现。' },
  },
  {
    path: '/catalog/products/:productId',
    name: 'catalog-detail',
    component: PlaceholderView,
    meta: { title: '产品详情', hint: '占位：详情由 TASK-WSC-005 实现。' },
  },
  {
    path: '/catalog/products/:productId/edit',
    name: 'catalog-edit',
    component: PlaceholderView,
    meta: { title: '编辑产品', hint: '占位：编辑由 TASK-WSC-005 实现。' },
  },
  {
    path: '/catalog/products/new',
    name: 'catalog-create',
    component: PlaceholderView,
    meta: { title: '新增产品', hint: '占位：新增由 TASK-WSC-005 实现。' },
  },
  {
    path: '/chain/products/:productId',
    name: 'chain',
    component: PlaceholderView,
    meta: { title: '上链信息', hint: '占位：上链列表与快照由 TASK-WSC-006 实现。' },
  },
  {
    path: '/unavailable/:feature?',
    name: 'unavailable',
    component: PlaceholderView,
    meta: {
      title: '功能未开放',
      hint: '数据登记 / 交易订单 / 连接器管理等 Out of Scope 菜单占位。',
      unavailable: true,
    },
  },
]
