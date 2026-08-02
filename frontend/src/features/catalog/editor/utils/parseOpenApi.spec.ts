import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import { OPENAPI_SHARED_CRITICAL } from './openapiSharedExpectations'
import { OpenApiParseError, parseOpenApiEndpoints } from './parseOpenApi'

const here = fileURLToPath(new URL('.', import.meta.url))
const sharedYaml = readFileSync(
  resolve(here, '../../../../../../tests/fixtures/import/openapi-shared-minimal.yaml'),
  'utf8',
)

const sharedJson = `{
  "openapi": "3.0.3",
  "info": { "title": "Shared Minimal API", "version": "1.0.0" },
  "paths": {
    "/enterprise/security/verify": {
      "post": {
        "summary": "企业安全信息核验",
        "description": "根据统一社会信用代码核验",
        "responses": {
          "200": { "description": "查询成功" },
          "400": { "description": "请求参数错误" }
        }
      }
    }
  }
}`

describe('parseOpenApiEndpoints', () => {
  it('parses shared YAML fixture critical fields (aligned with 301 import)', () => {
    const eps = parseOpenApiEndpoints(sharedYaml)
    expect(eps.length).toBeGreaterThanOrEqual(1)
    expect(eps[0].method).toBe(OPENAPI_SHARED_CRITICAL.method)
    expect(eps[0].path).toBe(OPENAPI_SHARED_CRITICAL.path)
    expect(eps[0].summary).toBe(OPENAPI_SHARED_CRITICAL.summary)
    expect(eps[0].parameters?.some((p) => p.name === 'X-Request-Id')).toBe(true)
    expect(eps[0].responses?.map((r) => r.code)).toEqual(expect.arrayContaining(['200', '400']))
    expect(eps[0].requestBodySchema).toContain('uscc')
  })

  it('parses shared JSON fixture critical fields identically', () => {
    const eps = parseOpenApiEndpoints(sharedJson)
    expect(eps[0].method).toBe(OPENAPI_SHARED_CRITICAL.method)
    expect(eps[0].path).toBe(OPENAPI_SHARED_CRITICAL.path)
    expect(eps[0].summary).toBe(OPENAPI_SHARED_CRITICAL.summary)
  })

  it('throws on non-empty garbage text', () => {
    expect(() => parseOpenApiEndpoints('not-openapi {{{')).toThrow(OpenApiParseError)
  })

  it('returns empty for blank input', () => {
    expect(parseOpenApiEndpoints('')).toEqual([])
    expect(parseOpenApiEndpoints('   ')).toEqual([])
  })
})
