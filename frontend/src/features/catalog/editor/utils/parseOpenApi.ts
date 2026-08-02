import type { ApiEndpoint, ApiEndpointParameter, ApiEndpointResponse } from '@/api/catalog'

const METHODS = new Set(['get', 'post', 'put', 'delete', 'patch', 'head', 'options', 'trace'])

export class OpenApiParseError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'OpenApiParseError'
  }
}

function newEndpointId(): string {
  return `ep-${Math.random().toString(36).slice(2, 10)}`
}

function stringOrEmpty(v: unknown): string {
  return v == null ? '' : String(v)
}

function extractParameters(raw: unknown): ApiEndpointParameter[] {
  if (!Array.isArray(raw)) return []
  const out: ApiEndpointParameter[] = []
  for (const item of raw) {
    if (!item || typeof item !== 'object') continue
    const m = item as Record<string, unknown>
    const schema = m.schema
    let type = 'string'
    if (schema && typeof schema === 'object' && (schema as Record<string, unknown>).type != null) {
      type = String((schema as Record<string, unknown>).type)
    } else if (m.type != null) {
      type = String(m.type)
    }
    out.push({
      name: stringOrEmpty(m.name),
      type,
      required: m.required === true,
      description: stringOrEmpty(m.description),
    })
  }
  return out
}

function extractResponses(raw: unknown): ApiEndpointResponse[] {
  if (!raw || typeof raw !== 'object') return []
  const out: ApiEndpointResponse[] = []
  for (const [code, val] of Object.entries(raw as Record<string, unknown>)) {
    const desc =
      val && typeof val === 'object'
        ? stringOrEmpty((val as Record<string, unknown>).description)
        : ''
    out.push({ code: String(code), description: desc })
  }
  return out
}

function extractBodySchema(requestBody: unknown): string | undefined {
  if (!requestBody || typeof requestBody !== 'object') return undefined
  const content = (requestBody as Record<string, unknown>).content
  if (!content || typeof content !== 'object') return undefined
  for (const media of Object.values(content as Record<string, unknown>)) {
    if (media && typeof media === 'object' && (media as Record<string, unknown>).schema != null) {
      try {
        return JSON.stringify((media as Record<string, unknown>).schema)
      } catch {
        return String((media as Record<string, unknown>).schema)
      }
    }
  }
  return undefined
}

function extractFirstResponseSchema(responses: unknown): string | undefined {
  if (!responses || typeof responses !== 'object') return undefined
  for (const val of Object.values(responses as Record<string, unknown>)) {
    if (!val || typeof val !== 'object') continue
    const content = (val as Record<string, unknown>).content
    if (!content || typeof content !== 'object') continue
    for (const media of Object.values(content as Record<string, unknown>)) {
      if (media && typeof media === 'object' && (media as Record<string, unknown>).schema != null) {
        try {
          return JSON.stringify((media as Record<string, unknown>).schema)
        } catch {
          return String((media as Record<string, unknown>).schema)
        }
      }
    }
  }
  return undefined
}

type YamlNode = Record<string, unknown> | unknown[]

/**
 * 最小缩进 YAML 解析（OpenAPI 文档子集；无依赖，方案 B）。
 * 支持：映射、列表、标量、字面量/折叠块标量（| / >）、单/双引号、布尔/数字/null。
 */
export function parseYamlDocument(text: string): unknown {
  type Frame = { indent: number; node: YamlNode }

  const stripInlineComment = (s: string): string => {
    let inSingle = false
    let inDouble = false
    for (let i = 0; i < s.length; i++) {
      const ch = s[i]
      if (ch === "'" && !inDouble) inSingle = !inSingle
      else if (ch === '"' && !inSingle) inDouble = !inDouble
      else if (ch === '#' && !inSingle && !inDouble) return s.slice(0, i).trimEnd()
    }
    return s
  }

  const unquote = (s: string): string => {
    const t = s.trim()
    if (
      (t.startsWith('"') && t.endsWith('"')) ||
      (t.startsWith("'") && t.endsWith("'"))
    ) {
      return t.slice(1, -1)
    }
    return t
  }

  const parseScalar = (raw: string): unknown => {
    const s = raw.trim()
    if (s === '' || s === '~' || s === 'null') return null
    if (s === 'true') return true
    if (s === 'false') return false
    if (
      (s.startsWith('"') && s.endsWith('"')) ||
      (s.startsWith("'") && s.endsWith("'"))
    ) {
      return s.slice(1, -1)
    }
    if (/^-?\d+(\.\d+)?$/.test(s)) return Number(s)
    return s
  }

  /** `|` / `>`，可选 chomping/indent 指示（不完整实现 chomping 语义）。 */
  const isBlockScalarIndicator = (v: string): boolean => /^[|>][+-]?(?:\d+)?$/.test(v)

  const lines = text.replace(/\r\n/g, '\n').split('\n')
  const root: Record<string, unknown> = {}
  const stack: Frame[] = [{ indent: -1, node: root }]

  const peekNextSignificant = (from: number): { indent: number; content: string } | null => {
    for (let j = from; j < lines.length; j++) {
      const L = lines[j]
      if (!L.trim() || L.trimStart().startsWith('#')) continue
      const ind = L.search(/\S/)
      return { indent: ind, content: stripInlineComment(L.slice(ind)) }
    }
    return null
  }

  /**
   * 收集字面量/折叠块标量：吞掉缩进更深的行拼成字符串。
   * 返回值的 endLine 为最后消费行下标（调用方应 `i = endLine`）。
   */
  const readBlockScalar = (
    startLine: number,
    keyIndent: number,
  ): { value: string; endLine: number } => {
    const collected: string[] = []
    let contentIndent: number | null = null
    let j = startLine
    let lastConsumed = startLine - 1

    while (j < lines.length) {
      const L = lines[j]
      if (!L.trim()) {
        const next = peekNextSignificant(j + 1)
        if (next && next.indent > keyIndent) {
          collected.push('')
          lastConsumed = j
          j++
          continue
        }
        break
      }
      const ind = L.search(/\S/)
      if (ind <= keyIndent) break
      if (contentIndent === null) contentIndent = ind
      collected.push(L.length >= contentIndent ? L.slice(contentIndent) : L.trimStart())
      lastConsumed = j
      j++
    }

    return { value: collected.join('\n'), endLine: lastConsumed }
  }

  const ensureMap = (frame: Frame): Record<string, unknown> => {
    if (Array.isArray(frame.node)) {
      throw new OpenApiParseError('接口定义解析失败：期望映射')
    }
    return frame.node
  }

  for (let i = 0; i < lines.length; i++) {
    const rawLine = lines[i]
    if (!rawLine.trim() || rawLine.trimStart().startsWith('#')) continue
    const indent = rawLine.search(/\S/)
    const content = stripInlineComment(rawLine.slice(indent))
    if (!content) continue

    while (stack.length > 1 && indent <= stack[stack.length - 1].indent) {
      stack.pop()
    }
    const frame = stack[stack.length - 1]

    if (content.startsWith('- ')) {
      if (!Array.isArray(frame.node)) {
        throw new OpenApiParseError('接口定义解析失败：列表项缩进非法')
      }
      const rest = content.slice(2)
      const itemIndent = indent + 2

      if (rest.includes(':')) {
        const colon = rest.indexOf(':')
        const k = unquote(rest.slice(0, colon).trim())
        const v = rest.slice(colon + 1).trim()
        const obj: Record<string, unknown> = {}
        frame.node.push(obj)
        stack.push({ indent, node: obj })
        if (isBlockScalarIndicator(v)) {
          const block = readBlockScalar(i + 1, indent)
          obj[k] = block.value
          i = block.endLine
        } else if (v === '') {
          const next = peekNextSignificant(i + 1)
          if (next && next.indent > indent && next.content.startsWith('- ')) {
            const arr: unknown[] = []
            obj[k] = arr
            stack.push({ indent: itemIndent - 1, node: arr })
          } else {
            const nested: Record<string, unknown> = {}
            obj[k] = nested
            stack.push({ indent: itemIndent - 1, node: nested })
          }
        } else {
          obj[k] = parseScalar(v)
        }
      } else if (rest.trim() === '') {
        const obj: Record<string, unknown> = {}
        frame.node.push(obj)
        stack.push({ indent, node: obj })
      } else {
        frame.node.push(parseScalar(rest))
      }
      continue
    }

    const colon = content.indexOf(':')
    if (colon < 0) {
      throw new OpenApiParseError('接口定义解析失败：非合法 YAML 行')
    }
    const key = unquote(content.slice(0, colon).trim())
    const valuePart = content.slice(colon + 1).trim()
    const map = ensureMap(frame)

    if (isBlockScalarIndicator(valuePart)) {
      const block = readBlockScalar(i + 1, indent)
      map[key] = block.value
      i = block.endLine
    } else if (valuePart === '') {
      const next = peekNextSignificant(i + 1)
      if (next && next.indent > indent && next.content.startsWith('- ')) {
        const arr: unknown[] = []
        map[key] = arr
        stack.push({ indent, node: arr })
      } else {
        const nested: Record<string, unknown> = {}
        map[key] = nested
        stack.push({ indent, node: nested })
      }
    } else {
      map[key] = parseScalar(valuePart)
    }
  }

  return root
}

function loadRoot(raw: string): Record<string, unknown> {
  const text = raw.trim()
  if (!text) {
    throw new OpenApiParseError('接口定义解析失败：内容为空')
  }
  let root: unknown
  try {
    if (text.startsWith('{') || text.startsWith('[')) {
      root = JSON.parse(text)
    } else {
      root = parseYamlDocument(text)
    }
  } catch (e) {
    if (e instanceof OpenApiParseError) throw e
    throw new OpenApiParseError('接口定义解析失败：非合法 OpenAPI/Swagger 文本')
  }
  if (!root || typeof root !== 'object' || Array.isArray(root)) {
    throw new OpenApiParseError('接口定义解析失败：根节点须为对象')
  }
  return root as Record<string, unknown>
}

/**
 * 将 OpenAPI/Swagger 原文解析为 endpoints[]（与 301 ImportOpenApiParser 关键字段对齐）。
 */
export function parseOpenApiEndpoints(raw: string): ApiEndpoint[] {
  if (raw == null || !String(raw).trim()) {
    return []
  }
  const map = loadRoot(String(raw))
  const pathsObj = map.paths
  if (pathsObj == null) {
    if (!('openapi' in map) && !('swagger' in map)) {
      throw new OpenApiParseError('接口定义解析失败：缺少 openapi/swagger 或 paths')
    }
    return []
  }
  if (typeof pathsObj !== 'object' || Array.isArray(pathsObj)) {
    throw new OpenApiParseError('接口定义解析失败：paths 非法')
  }

  const endpoints: ApiEndpoint[] = []
  for (const [path, opsVal] of Object.entries(pathsObj as Record<string, unknown>)) {
    if (!opsVal || typeof opsVal !== 'object' || Array.isArray(opsVal)) continue
    for (const [methodKey, opVal] of Object.entries(opsVal as Record<string, unknown>)) {
      const method = methodKey.toLowerCase()
      if (!METHODS.has(method)) continue
      if (!opVal || typeof opVal !== 'object' || Array.isArray(opVal)) continue
      const op = opVal as Record<string, unknown>
      const ep: ApiEndpoint = {
        id: newEndpointId(),
        method: method.toUpperCase(),
        path,
        summary: stringOrEmpty(op.summary),
        description: stringOrEmpty(op.description),
        parameters: extractParameters(op.parameters),
        responses: extractResponses(op.responses),
      }
      const reqSchema = extractBodySchema(op.requestBody)
      if (reqSchema != null) ep.requestBodySchema = reqSchema
      const resSchema = extractFirstResponseSchema(op.responses)
      if (resSchema != null) ep.responseBodySchema = resSchema
      endpoints.push(ep)
    }
  }
  return endpoints
}

/** 校验非空原文可解析；失败抛 OpenApiParseError。 */
export function assertSwaggerParsable(raw: string): void {
  const text = String(raw ?? '').trim()
  if (!text) return
  parseOpenApiEndpoints(text)
}

export function createEmptyEndpoint(): ApiEndpoint {
  return {
    id: newEndpointId(),
    method: 'GET',
    path: '/',
    summary: '',
    description: '',
    parameters: [],
    responses: [{ code: '200', description: '成功' }],
  }
}
