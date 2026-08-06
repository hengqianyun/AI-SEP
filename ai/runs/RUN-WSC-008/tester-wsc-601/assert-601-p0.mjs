/**
 * TASK-WSC-601 tester P0 assertions (§3.1 hard freeze + 2.1.0 regression).
 * Evidence-only script under tester-wsc-601/; does not modify contracts/frontend.
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

// tester-wsc-601 → RUN-WSC-008 → runs → ai → repo root
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../../..");
const failures = [];
const passes = [];

function read(rel) {
  return fs.readFileSync(path.join(root, rel), "utf8");
}

function assert(name, cond, detail = "") {
  if (cond) passes.push({ name, detail });
  else failures.push({ name, detail });
}

const version = read("contracts/VERSION").trim();
assert("VERSION==2.2.0", version === "2.2.0", `got=${version}`);

const openapi = read("contracts/openapi/openapi.yaml");
assert("OpenAPI info.version 2.2.0", /version:\s*["']?2\.2\.0["']?/.test(openapi.split("\n").slice(0, 20).join("\n")));

const codes = read("contracts/errors/codes.yaml");
assert("codes.yaml ERR_ROLE_SWITCH_DISABLED", codes.includes("ERR_ROLE_SWITCH_DISABLED"));
assert("codes.yaml ERR_USER_USERNAME_CONFLICT", codes.includes("ERR_USER_USERNAME_CONFLICT"));
assert("codes.yaml ERR_USER_NOT_FOUND", codes.includes("ERR_USER_NOT_FOUND"));
assert("codes.yaml ERR_FORBIDDEN", codes.includes("ERR_FORBIDDEN"));

// OpenAPI enum hard sync for role-switch + user codes
assert(
  "OpenAPI ApiResponseError.code enum has ERR_ROLE_SWITCH_DISABLED",
  /ERR_ROLE_SWITCH_DISABLED/.test(openapi) &&
    openapi.includes("ApiResponseError") &&
    /code:\s*\n(?:\s+[^\n]+\n)*?\s+enum:[\s\S]*?ERR_ROLE_SWITCH_DISABLED/.test(openapi)
);

assert("OpenAPI enum ERR_USER_USERNAME_CONFLICT", /enum:[\s\S]*?ERR_USER_USERNAME_CONFLICT/.test(openapi));
assert("OpenAPI enum ERR_USER_NOT_FOUND", /enum:[\s\S]*?ERR_USER_NOT_FOUND/.test(openapi));

// POST /auth/session/role → 410
assert(
  "POST /auth/session/role path exists",
  /\/auth\/session\/role:/.test(openapi) || /['"]\/auth\/session\/role['"]/.test(openapi) || openapi.includes("/auth/session/role:")
);
assert("session/role 410", /\/auth\/session\/role:[\s\S]*?['"]?410['"]?:/.test(openapi) || /post:[\s\S]*?410:[\s\S]*?ERR_ROLE_SWITCH_DISABLED/.test(openapi));
assert("session/role example ERR_ROLE_SWITCH_DISABLED", openapi.includes("ERR_ROLE_SWITCH_DISABLED"));

/** Extract OpenAPI components.schemas block for a named schema (4-space key). */
function schemaBlock(name) {
  const re = new RegExp(`(?:^|\\r?\\n) {4}${name}:\\r?\\n([\\s\\S]*?)(?=(?:\\r?\\n) {4}[A-Z][A-Za-z0-9_]*:\\r?\\n|$)`);
  const m = openapi.match(re);
  return m ? m[0] : null;
}

// LoginRequest: no role property
const loginBlock = schemaBlock("LoginRequest");
assert("LoginRequest schema present", !!loginBlock, loginBlock ? "found" : "missing");
if (loginBlock) {
  assert("LoginRequest has no role property", !/(?:^|\r?\n)\s+role:/.test(loginBlock), loginBlock.slice(0, 400));
  assert("LoginRequest has username", /username:/.test(loginBlock));
  assert("LoginRequest has password", /password:/.test(loginBlock));
}

// AdminUser required fields
const adminBlock = schemaBlock("AdminUser");
assert("AdminUser schema present", !!adminBlock);
if (adminBlock) {
  const reqMatch = adminBlock.match(/required:\s*\[([^\]]+)\]/);
  assert("AdminUser has required list", !!reqMatch, adminBlock.slice(0, 200));
  if (reqMatch) {
    const reqs = reqMatch[1].split(",").map((s) => s.trim());
    for (const f of ["userId", "username", "displayName", "role", "enterpriseName", "deleted"]) {
      assert(`AdminUser.required includes ${f}`, reqs.includes(f), `required=[${reqs.join(",")}]`);
    }
  }
  const propsIdx = adminBlock.indexOf("properties:");
  const props = propsIdx >= 0 ? adminBlock.slice(propsIdx) : "";
  assert("AdminUser schema has no password property", !/(?:^|\r?\n)\s+password:/.test(props));
  assert("AdminUser schema has no passwordHash property", !/(?:^|\r?\n)\s+passwordHash:/.test(props));
}

// Paths
assert("OpenAPI /admin/users", openapi.includes("/admin/users"));
assert("OpenAPI /catalog/l2-distribution", openapi.includes("/catalog/l2-distribution"));
assert(
  "OpenAPI mine param on products",
  /\/catalog\/products:[\s\S]*?\n\s+-\s+name:\s*mine\b/.test(openapi) ||
    /\/catalog\/products:[\s\S]*?\n\s+mine:/.test(openapi) ||
    (openapi.includes("/catalog/products") && /name:\s*mine\b/.test(openapi)),
  "expect query param mine on list products"
);
assert("L2Distribution named schema", openapi.includes("L2Distribution"));
assert("soft-delete via deleted", /AdminUserUpdate:[\s\S]*?deleted:/.test(openapi));

// Narrative: createProduct only PROVIDER
assert(
  "createProduct narrative only PROVIDER",
  /summary:\s*新增数据产品（仅 PROVIDER/.test(openapi) &&
    /产品写仅 PROVIDER；ADMIN 与 USER 调用返回 ERR_FORBIDDEN/.test(openapi)
);

// matrix
const matrix = read("contracts/rbac/matrix.yaml");
assert("matrix version 2.2.0", /version:\s*["']?2\.2\.0["']?/.test(matrix));
assert("matrix ADMIN productWriteUI hidden", /role:\s*ADMIN[\s\S]*?productWriteUI:\s*hidden/.test(matrix));
assert("matrix ADMIN productWriteApi 403", /role:\s*ADMIN[\s\S]*?productWriteApi:[\s\S]*?403/.test(matrix));
assert("matrix ADMIN productImportUI hidden", /role:\s*ADMIN[\s\S]*?productImportUI:\s*hidden/.test(matrix));
assert("matrix ADMIN productImportApi 403", /role:\s*ADMIN[\s\S]*?productImportApi:[\s\S]*?403/.test(matrix));
assert("matrix PROVIDER productWriteUI visible", /role:\s*PROVIDER[\s\S]*?productWriteUI:\s*visible/.test(matrix));
assert("matrix PROVIDER myProductsUI visible", /role:\s*PROVIDER[\s\S]*?myProductsUI:\s*visible/.test(matrix));
assert("matrix USER productWriteUI hidden", /role:\s*USER[\s\S]*?productWriteUI:\s*hidden/.test(matrix));
assert("matrix USER myProductsUI hidden", /role:\s*USER[\s\S]*?myProductsUI:\s*hidden/.test(matrix));
assert("matrix ADMIN userManageUI visible", /role:\s*ADMIN[\s\S]*?userManageUI:\s*visible/.test(matrix));
assert("matrix PROVIDER userManageApi 403", /role:\s*PROVIDER[\s\S]*?userManageApi:[\s\S]*?403/.test(matrix));
assert("matrix sessionRoleSwitch 410 all roles", (matrix.match(/sessionRoleSwitch:[\s\S]*?410/g) || []).length >= 3);

// state-matrix
const stateMatrix = read("contracts/ui/state-matrix.md");
assert("state-matrix version header 2.2.0", /2\.2\.0/.test(stateMatrix.slice(0, 500)));
assert("state-matrix import host 我的产品", /我的(数据)?产品/.test(stateMatrix) && /导入/.test(stateMatrix));

// req-coverage
const coverage = read("contracts/req-coverage.md");
for (const req of ["REQ-SHELL-001", "REQ-RBAC-001", "REQ-CAT-009", "REQ-USER-001", "REQ-CAT-010"]) {
  assert(`req-coverage has ${req}`, coverage.includes(req));
}

// --- 2.1.0 regression ---
assert("2.1.0 TypeSpecificApi present", openapi.includes("TypeSpecificApi") || openapi.includes("typeSpecific"));
assert("2.1.0 ERR_IMPORT_TEMPLATE_UNSUPPORTED", codes.includes("ERR_IMPORT_TEMPLATE_UNSUPPORTED"));
assert("2.1.0 ERR_IMPORT_FORMAT_INVALID", codes.includes("ERR_IMPORT_FORMAT_INVALID"));
assert("2.1.0 ERR_IMPORT_FILE_TOO_LARGE", codes.includes("ERR_IMPORT_FILE_TOO_LARGE"));
assert("2.1.0 reportFieldsWhitelist", /reportFieldsWhitelist|rowNumber.*productCode.*reasonCode.*reasonMessage/s.test(codes) || codes.includes("reportFieldsWhitelist"));
assert("2.1.0 ImportTemplateColumns / NO_UPDATE", openapi.includes("NO_UPDATE") || codes.includes("NO_UPDATE") || openapi.includes("ImportTemplateColumns"));

// frontend api alignment
const client = read("frontend/src/api/client.ts");
assert("client CONTRACT_VERSION 2.2.0", /CONTRACT_VERSION.*=.*['"]2\.2\.0['"]/.test(client) || client.includes("2.2.0"));
const auth = read("frontend/src/api/auth.ts");
assert("auth LoginRequest no role field", !/role\s*[?:]/.test(auth.match(/export (?:type|interface) LoginRequest[\s\S]*?}/)?.[0] || "role:") || !(auth.match(/LoginRequest[\s\S]{0,300}/)?.[0] || "").includes("role"));
// more precise LoginRequest check
{
  const m = auth.match(/(?:export )?(?:type|interface) LoginRequest\s*=?\s*\{[\s\S]*?\}/);
  assert("auth.ts LoginRequest block has no role", m ? !/\brole\b/.test(m[0]) : false, m ? m[0] : "missing LoginRequest");
}
assert("auth switchSessionRole present", auth.includes("switchSessionRole") || auth.includes("session/role"));
const admin = read("frontend/src/api/admin.ts");
assert("admin.ts exists with AdminUser", admin.includes("AdminUser") && (admin.includes("listUsers") || admin.includes("/admin/users")));
const catalog = read("frontend/src/api/catalog.ts");
assert("catalog getL2Distribution", catalog.includes("getL2Distribution") || catalog.includes("l2-distribution"));
assert("catalog createBy", /createBy/.test(catalog));

console.log("=== TASK-WSC-601 P0 ASSERTIONS ===");
console.log(`PASS=${passes.length} FAIL=${failures.length}`);
for (const p of passes) console.log(`  PASS  ${p.name}${p.detail ? " — " + p.detail : ""}`);
for (const f of failures) console.log(`  FAIL  ${f.name}${f.detail ? " — " + f.detail : ""}`);
if (failures.length) {
  process.exit(1);
}
console.log("ALL P0 ASSERTIONS PASSED");
process.exit(0);
