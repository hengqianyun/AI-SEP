#!/usr/bin/env node
/**
 * Minimal contract consistency check for wsc-contracts@1.1.0
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..')
const errors = []

function read(rel) {
  const p = path.join(root, rel)
  if (!fs.existsSync(p)) {
    errors.push(`MISSING_FILE ${rel}`)
    return ''
  }
  return fs.readFileSync(p, 'utf8')
}

const codesYaml = read('contracts/errors/codes.yaml')
const openapi = read('contracts/openapi/openapi.yaml')
const reqCoverage = read('contracts/req-coverage.md')
const version = read('contracts/VERSION').trim()
const rbac = read('contracts/rbac/matrix.yaml')
const stream = read('contracts/overview/stream-events.yaml')

if (version !== '1.1.0') {
  errors.push(`VERSION expected 1.1.0, got ${version || '(empty)'}`)
}

const requiredCodes = [
  'ERR_PRODUCT_CODE_FORMAT',
  'ERR_PRODUCT_CODE_CONFLICT',
  'ERR_CATEGORY_HAS_PRODUCTS',
  'ERR_FORBIDDEN',
]

for (const code of requiredCodes) {
  if (!codesYaml.includes(code)) errors.push(`codes.yaml missing ${code}`)
  if (!openapi.includes(code)) errors.push(`openapi.yaml missing ${code}`)
}

for (const field of ['code', 'message', 'data', 'correlationId']) {
  if (!openapi.includes(field)) errors.push(`openapi envelope field missing mention: ${field}`)
}

const reqs = [
  'REQ-SHELL-001',
  'REQ-RBAC-001',
  'REQ-OVW-001',
  'REQ-OVW-002',
  'REQ-OVW-003',
  'REQ-OVW-004',
  'REQ-OVW-005',
  'REQ-CAT-001',
  'REQ-CAT-002',
  'REQ-CAT-003',
  'REQ-CAT-004',
  'REQ-CAT-005',
  'REQ-CAT-006',
  'REQ-CHAIN-001',
]

for (const req of reqs) {
  if (!reqCoverage.includes(req)) errors.push(`req-coverage.md missing ${req}`)
}

for (const role of ['ADMIN', 'PROVIDER', 'USER']) {
  if (!rbac.includes(role)) errors.push(`rbac/matrix.yaml missing ${role}`)
}

for (const t of ['CATALOG_REGISTER', 'DATA_REGISTER', 'TRADE_ORDER']) {
  if (!stream.includes(t)) errors.push(`stream-events.yaml missing ${t}`)
}

if (!openapi.includes('SessionCookie') && !openapi.includes('BearerAuth')) {
  errors.push('openapi missing auth securitySchemes')
}

if (!openapi.includes('OQ-004') && !openapi.includes('OTHER')) {
  errors.push('openapi missing OQ-004 / OTHER product type notes')
}

if (errors.length) {
  console.error('CONTRACT CHECK FAILED')
  for (const e of errors) console.error(' -', e)
  process.exit(1)
}

console.log('CONTRACT CHECK PASSED (error codes, REQ coverage, RBAC, stream, version 1.1.0)')
