/**
 * TASK-WSC-903 tester P0 assertions（PLAN-WSC-8.3 §3.1 / §5 testScope）。
 * 證據腳本，不改 contracts / frontend 源碼。
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../..')
const failures = []
const passes = []
const knownP2 = []

function read(rel) {
  return fs.readFileSync(path.join(root, rel), 'utf8')
}

function assert(name, cond, detail = '') {
  if (cond) passes.push({ name, detail })
  else failures.push({ name, detail })
}

function noteP2(name, detail) {
  knownP2.push({ name, detail })
}

const version = read('contracts/VERSION').trim()
const openapi = read('contracts/openapi/openapi.yaml')
const matrix = read('contracts/rbac/matrix.yaml')
const stateMatrix = read('contracts/ui/state-matrix.md')
const reqCoverage = read('contracts/req-coverage.md')
const codes = read('contracts/errors/codes.yaml')
const client = read('frontend/src/api/client.ts')
const catalog = read('frontend/src/api/catalog.ts')
const auth = read('frontend/src/api/auth.ts')
const adminTs = read('frontend/src/api/admin.ts')
const apiIndex = read('frontend/src/api/index.ts')
const overview = read('frontend/src/api/overview.ts')
const chain = read('frontend/src/api/chain.ts')

const openapiHead = openapi.split(/\r?\n/).slice(0, 20).join('\n')

assert('VERSION==2.3.3', version === '2.3.3', `got=${version}`)
assert(
  'OpenAPI info.version==2.3.3',
  /^info:\r?\n(?:  .+\r?\n)*?  version: 2\.3\.3$/m.test(openapiHead) ||
    /title: WSC Access Workbench API\r?\n  version: 2\.3\.3/.test(openapi),
  openapiHead,
)
assert('matrix.yaml version==2.3.3', /version:\s*"2\.3\.3"/.test(matrix.split(/\r?\n/).slice(0, 8).join('\n')))
assert(
  'state-matrix.md 版本頭 2.3.3',
  /wsc-contracts@2\.3\.3/.test(stateMatrix.split(/\r?\n/).slice(0, 5).join('\n')),
)
assert('codes.yaml version==2.3.3', /version:\s*"2\.3\.3"/.test(codes.split(/\r?\n/).slice(0, 10).join('\n')))
assert('client CONTRACT_VERSION 2.3.3', /CONTRACT_VERSION = '2\.3\.3'/.test(client))
assert('api index 2.3.3', /wsc-contracts@2\.3\.3/.test(apiIndex))
assert('overview 2.3.3', /wsc-contracts@2\.3\.3/.test(overview))
assert('chain 2.3.3', /wsc-contracts@2\.3\.3/.test(chain))
assert('catalog 2.3.3', /wsc-contracts@2\.3\.3/.test(catalog))
assert('auth 2.3.3', /wsc-contracts@2\.3\.3/.test(auth))
assert('admin 2.3.3', /wsc-contracts@2\.3\.3/.test(adminTs))

assert('OpenAPI 以 openapi: 3.0.3 開頭', openapi.startsWith('openapi: 3.0.3'))
assert('OpenAPI 含 /catalog/products', openapi.includes('/catalog/products:'))
assert('OpenAPI 含 /catalog/l2-distribution', openapi.includes('/catalog/l2-distribution:'))
assert('OpenAPI 含 /catalog/maintenance/entries', openapi.includes('/catalog/maintenance/entries:'))

assert(
  'Session required 含 enterpriseId+enterpriseName',
  /Session:\r?\n[\s\S]*?required: \[userId, displayName, role, enterpriseId, enterpriseName\]/.test(openapi),
)
assert(
  'AdminUser required 含 enterpriseId+enterpriseName',
  /AdminUser:\r?\n[\s\S]*?required: \[userId, username, displayName, role, enterpriseId, enterpriseName, deleted\]/.test(
    openapi,
  ),
)
assert('AdminUserCreate 含 enterpriseId', /AdminUserCreate:[\s\S]*?enterpriseId:/.test(openapi))

assert(
  'mine=true → 本企業（info description）',
  /mine=true → \*\*本企业\*\*产品（非 create_by-only）/.test(openapiHead),
)
assert(
  'listProducts mine 本企業（非 create_by-only）',
  /mine=true 时返回\*\*本企业\*\*产品[\s\S]*非 create_by-only/.test(openapi),
)
assert(
  'mine parameter description 本企業',
  /true 时仅返回\*\*本企业\*\*产品（同 enterpriseId；REQ-CAT-016）/.test(openapi),
)

assert(
  'maintenance scope enum full|myCatalog',
  /name: scope[\s\S]*?enum: \[full, myCatalog\]/.test(openapi),
)
assert('maintenance scope required: true', /name: scope[\s\S]*?required: true[\s\S]*?enum: \[full, myCatalog\]/.test(openapi))
assert(
  'PROVIDER scope=full → 403 敘述',
  /PROVIDER → 403/.test(openapi) && /scope=full：ADMIN \*\*全平台\*\*/.test(openapi),
)

assert(
  'l2-distribution 可選 l1CategoryId',
  /\/catalog\/l2-distribution:[\s\S]*?name: l1CategoryId[\s\S]*?required: false/.test(openapi),
)
assert(
  'l2-distribution 不按登入企業過濾',
  /可选 l1CategoryId[\s\S]*?\*\*不按\*\*登录用户企业过滤（REQ-CAT-018）/.test(openapi),
)

assert(
  '安全說明：僅信 SessionPrincipal；拒絕客戶端 enterpriseId 授權',
  /\*\*仅\*\*信服务端 SessionPrincipal/.test(openapi) && /\*\*拒绝\/忽略\*\*客户端 enterpriseId/.test(openapi),
)

const createByBlock = openapi.match(/createBy:\r?\n[\s\S]*?(?=\n            [a-zA-Z]|\n          [a-zA-Z])/)
assert('Product.createBy 存在', !!createByBlock)
assert(
  'createBy 非 mine 過濾依據',
  createByBlock
    ? /审计\/归属字段，非本企业 scope 判定依据/.test(createByBlock[0]) &&
        /非 create_by-only/.test(createByBlock[0]) &&
        !/mine=true 过滤依据/.test(createByBlock[0])
    : false,
  createByBlock ? createByBlock[0].slice(0, 400) : 'missing',
)
assert('OpenAPI 無「mine=true 过滤依据」', !openapi.includes('mine=true 过滤依据'))

assert('supplierName 未回退', /supplierName：产品字段模糊匹配/.test(openapi) && /name: supplierName/.test(openapi))

function roleBlock(role) {
  const re = new RegExp(`- role: ${role}\\r?\\n([\\s\\S]*?)(?=\\r?\\n  - role: |\\r?\\nnotes:)`)
  const m = matrix.match(re)
  return m ? m[0] : ''
}

const admin = roleBlock('ADMIN')
const provider = roleBlock('PROVIDER')
const user = roleBlock('USER')

assert('matrix ADMIN 塊', !!admin)
assert('matrix PROVIDER 塊', !!provider)
assert('matrix USER 塊', !!user)

const cells = [
  ['catalogMaintenanceUI', 'ADMIN', 'visible', /catalogMaintenanceUI: visible/, admin],
  ['catalogMaintenanceUI', 'PROVIDER', 'hidden', /catalogMaintenanceUI: hidden/, provider],
  ['catalogMaintenanceUI', 'USER', 'hidden', /catalogMaintenanceUI: hidden/, user],
  [
    'catalogMaintenanceApi scope=full',
    'ADMIN',
    '200',
    /catalogMaintenanceApi: \{ expect: 200, on: full, scope: full \}/,
    admin,
  ],
  [
    'catalogMaintenanceApi scope=full',
    'PROVIDER',
    '403',
    /catalogMaintenanceApi: \{ expect: 403,[\s\S]*?scope: full \}/,
    provider,
  ],
  [
    'catalogMaintenanceApi scope=full',
    'USER',
    '403',
    /catalogMaintenanceApi: \{ expect: 403,[\s\S]*?scope: full \}/,
    user,
  ],
  ['myCatalogUI', 'ADMIN', 'visible', /myCatalogUI: visible/, admin],
  ['myCatalogUI', 'PROVIDER', 'visible', /myCatalogUI: visible/, provider],
  ['myCatalogUI', 'USER', 'hidden', /myCatalogUI: hidden/, user],
  [
    'myCatalogApi scope=myCatalog',
    'ADMIN',
    '200 ownEnterprise',
    /myCatalogApi: \{ expect: 200, on: ownEnterprise, scope: myCatalog \}/,
    admin,
  ],
  [
    'myCatalogApi scope=myCatalog',
    'PROVIDER',
    '200 ownCreateBy',
    /myCatalogApi: \{ expect: 200, on: ownCreateBy, scope: myCatalog \}/,
    provider,
  ],
  [
    'myCatalogApi scope=myCatalog',
    'USER',
    '403',
    /myCatalogApi: \{ expect: 403,[\s\S]*?scope: myCatalog \}/,
    user,
  ],
  ['myProductsUI', 'ADMIN', 'visible', /myProductsUI: visible/, admin],
  ['myProductsUI', 'PROVIDER', 'visible', /myProductsUI: visible/, provider],
  ['myProductsUI', 'USER', 'hidden', /myProductsUI: hidden/, user],
  ['productWriteUI', 'ADMIN', 'visible', /productWriteUI: visible/, admin],
  ['productWriteUI', 'PROVIDER', 'visible', /productWriteUI: visible/, provider],
  ['productWriteUI', 'USER', 'hidden', /productWriteUI: hidden/, user],
  ['productImportUI', 'ADMIN', 'visible', /productImportUI: visible/, admin],
  ['productImportUI', 'PROVIDER', 'visible', /productImportUI: visible/, provider],
  ['productImportUI', 'USER', 'hidden', /productImportUI: hidden/, user],
  ['productWriteApi', 'ADMIN', '200 ownEnterprise', /productWriteApi: \{ expect: 200, on: ownEnterprise \}/, admin],
  ['productWriteApi', 'PROVIDER', '200 ownCreateBy', /productWriteApi: \{ expect: 200, on: ownCreateBy \}/, provider],
  ['productWriteApi', 'USER', '403', /productWriteApi: \{ expect: 403/, user],
  ['productImportApi', 'ADMIN', '200 ownEnterprise', /productImportApi: \{ expect: 200, on: ownEnterprise \}/, admin],
  ['productImportApi', 'USER', '403', /productImportApi: \{ expect: 403/, user],
]

for (const [key, role, expect, re, block] of cells) {
  assert(`§3.1 cell ${role}.${key} = ${expect}`, re.test(block), block.slice(0, 200))
}

const providerImportOnValid = /productImportApi: \{ expect: 200, on: valid \}/.test(provider)
const providerImportOwnCreateBy = /productImportApi: \{ expect: 200, on: ownCreateBy \}/.test(provider)
assert('PROVIDER.productImportApi expect=200', /productImportApi: \{ expect: 200/.test(provider))
if (providerImportOwnCreateBy) {
  assert('PROVIDER.productImportApi on=ownCreateBy（§3.1 字面）', true)
} else if (providerImportOnValid) {
  noteP2(
    'PROVIDER.productImportApi.on=valid ≠ §3.1 ownCreateBy',
    '已知 FIND-SEC-WSC-903-003 P2；expect 仍為 200；本輪不升格 FAIL',
  )
} else {
  assert('PROVIDER.productImportApi on 對齊 §3.1', false, provider)
}

assert('state-matrix 側欄「我的目錄」', /我的目录（`\/my-catalog`/.test(stateMatrix))
assert('state-matrix ADMIN 我的產品可見', /我的数据产品[\s\S]*?\| 可见 \| 可见 \| 隐藏 \|/.test(stateMatrix))
assert('state-matrix PROVIDER 無目錄維護', /目录维护[\s\S]*?\| 可见 \| \*\*隐藏\*\* \| 隐藏 \|/.test(stateMatrix))
assert('state-matrix ADMIN 雙入口說明', /ADMIN \*\*双入口\*\*/.test(stateMatrix))

const v16Reqs = [
  'REQ-CAT-014',
  'REQ-CAT-015',
  'REQ-CAT-016',
  'REQ-CAT-017',
  'REQ-CAT-018',
  'REQ-CAT-019',
  'REQ-SHELL-009',
  'REQ-SHELL-010',
  'REQ-USER-002',
  'REQ-RBAC-002',
]
for (const req of v16Reqs) {
  assert(`req-coverage 含 ${req}`, reqCoverage.includes(req))
}

assert('client getL2Distribution({ l1CategoryId })', /export function getL2Distribution\(query: L2DistributionQuery/.test(catalog))
assert('client L2DistributionQuery.l1CategoryId', /export type L2DistributionQuery = \{[\s\S]*?l1CategoryId\?: string/.test(catalog))
assert("client MaintenanceScope = 'full' | 'myCatalog'", /export type MaintenanceScope = 'full' \| 'myCatalog'/.test(catalog))
assert('client listMaintenanceEntries 傳 scope', /export function listMaintenanceEntries/.test(catalog))
assert('client ListProductsQuery.mine 本企業', /true → 本企业产品（REQ-CAT-016）/.test(catalog))
assert(
  'client Product.createBy 非過濾依據',
  /审计\/归属字段，非本企业 scope 判定依据/.test(catalog) &&
    /非 create_by-only/.test(catalog) &&
    !catalog.includes('mine=true 过滤依据'),
)
assert('auth Session.enterpriseId', /enterpriseId\?: string/.test(auth) || /enterpriseId: string/.test(auth))
assert('admin AdminUser.enterpriseId 必填型', /export type AdminUser = \{[\s\S]*?enterpriseId: string/.test(adminTs))

const apiDir = path.join(root, 'frontend/src/api')
for (const f of fs.readdirSync(apiDir).filter((n) => n.endsWith('.ts'))) {
  const txt = fs.readFileSync(path.join(apiDir, f), 'utf8')
  assert(`${f} 無 mine=true 过滤依据`, !txt.includes('mine=true 过滤依据'))
  assert(`${f} 無 2.2.0 版本釘`, !/CONTRACT_VERSION = '2\.2\.0'/.test(txt) && !/@2\.2\.0/.test(txt))
  assert(`${f} 無 2.3.2 版本釘`, !/CONTRACT_VERSION = '2\.3\.2'/.test(txt) && !/@2\.3\.2/.test(txt))
}

assert('預落地漂移：matrix 非 2.3.2', !/version:\s*"2\.3\.2"/.test(matrix.split(/\r?\n/).slice(0, 8).join('\n')))
assert(
  '預落地漂移：OpenAPI info 非 2.2.0',
  !/title: WSC Access Workbench API\r?\n  version: 2\.2\.0/.test(openapi),
)

console.log(`PASS ${passes.length}`)
console.log(`FAIL ${failures.length}`)
console.log(`KNOWN_P2 ${knownP2.length}`)
for (const p of passes) console.log(`  OK  ${p.name}${p.detail ? ' — ' + p.detail : ''}`)
for (const k of knownP2) console.log(`  P2  ${k.name} — ${k.detail}`)
for (const f of failures) console.log(`  BAD ${f.name}${f.detail ? ' — ' + f.detail : ''}`)

if (failures.length) {
  console.error('903 P0 ASSERTIONS FAILED')
  process.exit(1)
}
console.log('ALL 903 P0 ACCEPTANCE CHECKS PASSED')
process.exit(0)
