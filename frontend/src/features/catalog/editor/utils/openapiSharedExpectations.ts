/**
 * 与 301 导入路径共享 fixture 的关键字段期望
 *（`tests/fixtures/import/openapi-shared-minimal.yaml` /
 * `openapi-shared-import.csv`）。
 */
export const OPENAPI_SHARED_CRITICAL = {
  method: 'POST',
  path: '/enterprise/security/verify',
  summary: '企业安全信息核验',
} as const
