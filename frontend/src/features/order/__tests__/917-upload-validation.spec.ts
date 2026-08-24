/**
 * TASK-WSC-917 测试：附件上传 — 非白名单格式/超 20MB 前端即时校验。
 */
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { validateFile, ALLOWED_EXTENSIONS, MAX_FILE_SIZE } from '../composables/useUploadContract'

/** 创建 mock File */
function makeFile(name: string, size: number): File {
  const buffer = new ArrayBuffer(size)
  return new File([buffer], name, { type: 'application/octet-stream' })
}

describe('917-upload-validation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('allowed extensions pass validation: .doc, .docx, .pdf', () => {
    expect(validateFile(makeFile('合约.doc', 1024))).toBeNull()
    expect(validateFile(makeFile('合约.docx', 1024))).toBeNull()
    expect(validateFile(makeFile('合约.pdf', 1024))).toBeNull()
  })

  it('non-whitelist format fails: .txt', () => {
    const result = validateFile(makeFile('说明.txt', 1024))
    expect(result).not.toBeNull()
    expect(result).toContain('.txt')
    expect(result).toContain('不支持')
  })

  it('non-whitelist format fails: .jpg', () => {
    const result = validateFile(makeFile('图片.jpg', 1024))
    expect(result).not.toBeNull()
    expect(result).toContain('.jpg')
  })

  it('non-whitelist format fails: .xlsx', () => {
    const result = validateFile(makeFile('表格.xlsx', 1024))
    expect(result).not.toBeNull()
    expect(result).toContain('.xlsx')
  })

  it('file over 20MB fails validation', () => {
    const overSize = MAX_FILE_SIZE + 1
    const result = validateFile(makeFile('大文件.pdf', overSize))
    expect(result).not.toBeNull()
    expect(result).toContain('20MB')
  })

  it('file exactly 20MB passes validation', () => {
    const result = validateFile(makeFile('刚好.pdf', MAX_FILE_SIZE))
    expect(result).toBeNull()
  })

  it('file just under 20MB passes validation', () => {
    const result = validateFile(makeFile('刚好.pdf', MAX_FILE_SIZE - 1))
    expect(result).toBeNull()
  })

  it('non-whitelist + oversize returns format error first', () => {
    const result = validateFile(makeFile('大文件.zip', MAX_FILE_SIZE + 1))
    expect(result).not.toBeNull()
    expect(result).toContain('.zip')
  })

  it('ALLOWED_EXTENSIONS contains .doc, .docx, .pdf', () => {
    expect(ALLOWED_EXTENSIONS).toEqual(['.doc', '.docx', '.pdf'])
  })

  it('MAX_FILE_SIZE is 20MB', () => {
    expect(MAX_FILE_SIZE).toBe(20 * 1024 * 1024)
  })
})
