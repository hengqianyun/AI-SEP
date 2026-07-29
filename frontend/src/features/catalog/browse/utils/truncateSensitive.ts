/**
 * §3.6 敏感标识截断展示（完整值仍可通过复制获得）。
 * browse 内自包含，约定对齐 CHAIN SensitiveId。
 */
export function truncateSensitiveId(value: string, head = 10, tail = 6): string {
  const v = value ?? ''
  if (v.length <= head + tail + 1) {
    return v
  }
  return `${v.slice(0, head)}…${v.slice(-tail)}`
}
