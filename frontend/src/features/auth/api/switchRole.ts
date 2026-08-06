/**
 * 特性内切角色辅助：契约硬禁用（HTTP 410 / ERR_ROLE_SWITCH_DISABLED）。
 * 权威 client 在 `@/api/auth`；本文件仅再导出供 feature 内引用。
 */
export { switchSessionRole } from '@/api/auth'
