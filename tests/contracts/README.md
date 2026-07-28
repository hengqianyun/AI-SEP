# Contract consistency checks (TASK-WSC-001)

## 1. Error code consistency

Compare:

- `contracts/errors/codes.yaml`
- `contracts/openapi/openapi.yaml` examples / `ApiResponseError.code` enum

Required codes:

| Code | Present in codes.yaml | Present in OpenAPI examples/enum |
|---|---|---|
| ERR_PRODUCT_CODE_FORMAT | ☐ | ☐ |
| ERR_PRODUCT_CODE_CONFLICT | ☐ | ☐ |
| ERR_CATEGORY_HAS_PRODUCTS | ☐ | ☐ |
| ERR_FORBIDDEN | ☐ | ☐ |

Envelope fields must match: `code`, `message`, `data`, `correlationId`.

## 2. REQ coverage checklist

See `contracts/req-coverage.md` — all 14 REQ must map to endpoint/model/route/test constraint.

## 3. Script

Run from repo root:

```bash
node tests/contracts/check-contracts.mjs
```

Exit 0 = PASSED; non-zero = FAILED with missing items printed.
