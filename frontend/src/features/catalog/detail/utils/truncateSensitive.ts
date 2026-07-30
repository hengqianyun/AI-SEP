/** §3.6 敏感标识截断展示。 */
export function truncateSensitiveId(value: string, head = 10, tail = 6): string {
  if (!value) return '—'
  if (value.length <= head + tail + 1) return value
  return `${value.slice(0, head)}…${value.slice(-tail)}`
}
