<template>
  <div class="app-container">
    <el-main>
      <div class="tabs-header">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="JSON 格式化" name="json-formatter">
          <el-card class="json-tab-card">
            <div class="json-editors-scroll">
              <div v-for="(editor, idx) in jsonEditors" :key="editor.id" class="json-editor-block" :style="editor.width > 0 ? { width: editor.width + 'px', flex: 'none' } : {}">
                <div class="json-editor-toolbar">
                  <span class="json-editor-label">{{ idx + 1 }}{{ getEditorFileName(editor) ? ' - ' + getEditorFileName(editor) : '' }}</span>
                  <el-button size="small" @click="openJsonFile(editor.id)" title="打开"><el-icon><FolderOpened /></el-icon></el-button>
                  <el-button size="small" @click="saveJsonFile(editor.id)" title="保存"><el-icon><Download /></el-icon></el-button>
                  <el-button size="small" @click="saveJsonFileAs(editor.id)" title="另存为"><el-icon><DocumentCopy /></el-icon></el-button>
                  <el-button size="small" @click="restructureJson(editor.id)" title="递归"><el-icon><RefreshRight /></el-icon></el-button>
                  <el-button size="small" @click="copyJsonOutput(editor.id)" title="复制"><el-icon><CopyDocument /></el-icon></el-button>
                  <el-button v-if="jsonEditors.length > 1" size="small" type="danger" plain @click="removeJsonEditor(editor.id)" title="关闭"><el-icon><Close /></el-icon></el-button>
                  <div style="flex: 1;"></div>
                  <el-button type="primary" size="small" @click="beautifyJson(editor.id)" title="美化"><el-icon><MagicStick /></el-icon></el-button>
                  <el-button size="small" type="primary" plain @click="addJsonEditor" title="新建窗口"><el-icon><Plus /></el-icon></el-button>
                </div>
                <div class="json-single-editor">
                  <MonacoEditor
                    :value="editor.content"
                    @update:value="(v) => editor.content = v"
                    language="json"
                    :options="editorOptions"
                    style="height: 100%"
                    @mount="(e) => onEditorMount(editor.id, e)"
                  />
                </div>
                <div
                  class="json-resize-handle"
                  @mousedown="(e) => startResize(editor.id, e)"
                  @dblclick="resetEditorWidths"
                ></div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="翻译" name="translate">
          <iframe
            src="https://fanyi.baidu.com/mtpe-individual/transText?ext_channel=Aldtype01&from=auto&to=zh&query=#/"
            style="width: 100%; height: calc(100vh - 150px); border: none; border-radius: 4px;"
            allow="clipboard-read; clipboard-write"
          ></iframe>
        </el-tab-pane>

        <el-tab-pane label="图片压缩" name="image-compress">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>图片压缩</span>
              </div>
            </template>
            <el-upload
              class="upload-demo"
              :auto-upload="false"
              :on-change="handleImageChange"
              :limit="1"
              accept="image/*"
            >
              <el-button type="primary">选择图片</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  请选择要压缩的图片
                </div>
              </template>
            </el-upload>
            <el-form :model="imageForm" label-width="80px" style="margin-top: 20px;">
              <el-form-item label="宽度">
                <el-input v-model.number="imageForm.width" type="number"></el-input>
              </el-form-item>
              <el-form-item label="高度">
                <el-input v-model.number="imageForm.height" type="number"></el-input>
              </el-form-item>
              <el-form-item label="质量">
                <el-slider v-model="imageForm.quality" :min="0.1" :max="1" :step="0.1"></el-slider>
              </el-form-item>
              <el-form-item label="格式">
                <el-select v-model="imageForm.suffix">
                  <el-option label="JPG" value="jpg"></el-option>
                  <el-option label="PNG" value="png"></el-option>
                  <el-option label="WEBP" value="webp"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="compressImage" :disabled="!imageFile">压缩图片</el-button>
              </el-form-item>
            </el-form>
            <el-alert
              v-if="imageResult"
              :title="'压缩成功：' + imageResult.name"
              :description="'大小：' + (imageResult.size / 1024).toFixed(2) + 'KB'"
              type="success"
              show-icon
              style="margin-top: 20px;"
            >
              <template #default>
                <el-button type="text" @click="downloadImage(imageResult.name)">下载图片</el-button>
              </template>
            </el-alert>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="文档工具" name="document">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>PDF 转 Word</span>
              </div>
            </template>
            <el-upload
              class="upload-demo"
              :auto-upload="false"
              :on-change="handlePdfChange"
              :limit="1"
              accept=".pdf"
            >
              <el-button type="primary">选择 PDF 文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  请选择要转换的 PDF 文件
                </div>
              </template>
            </el-upload>
            <el-form-item style="margin-top: 20px;">
              <el-button type="primary" @click="pdfToWord" :disabled="!pdfFile">转换为 Word</el-button>
            </el-form-item>
            <el-alert
              v-if="pdfResult"
              :title="'转换成功：' + pdfResult.oldname"
              type="success"
              show-icon
              style="margin-top: 20px;"
            >
              <template #default>
                <el-button type="text" @click="downloadFile(pdfResult.filename, pdfResult.oldname)">下载 Word 文件</el-button>
              </template>
            </el-alert>
          </el-card>
          
          <el-card style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>Word 转 PDF</span>
              </div>
            </template>
            <el-upload
              class="upload-demo"
              :auto-upload="false"
              :on-change="handleWordChange"
              :limit="1"
              accept=".docx,.doc"
            >
              <el-button type="primary">选择 Word 文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  请选择要转换的 Word 文件
                </div>
              </template>
            </el-upload>
            <el-form-item style="margin-top: 20px;">
              <el-button type="primary" @click="wordToPdf" :disabled="!wordFile">转换为 PDF</el-button>
            </el-form-item>
            <el-alert
              v-if="wordResult"
              :title="'转换成功：' + wordResult.oldname"
              type="success"
              show-icon
              style="margin-top: 20px;"
            >
              <template #default>
                <el-button type="text" @click="downloadFile(wordResult.filename, wordResult.oldname)">下载 PDF 文件</el-button>
              </template>
            </el-alert>
          </el-card>
          
          <el-card style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>TXT 转 PDF</span>
              </div>
            </template>
            <el-form :model="txtForm" label-width="80px">
              <el-form-item label="文本内容">
                <el-input
                  v-model="txtForm.text"
                  type="textarea"
                  rows="10"
                  placeholder="请输入要转换的文本内容"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="txtToPdf" :disabled="!txtForm.text">转换为 PDF</el-button>
              </el-form-item>
            </el-form>
            <el-alert
              v-if="txtResult"
              :title="'转换成功：' + txtResult.oldname"
              type="success"
              show-icon
              style="margin-top: 20px;"
            >
              <template #default>
                <el-button type="text" @click="downloadFile(txtResult.filename, txtResult.oldname)">下载 PDF 文件</el-button>
              </template>
            </el-alert>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="邮件工具" name="mail">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>发送邮件</span>
              </div>
            </template>
            <el-form :model="mailForm" label-width="80px">
              <el-form-item label="收件人">
                <el-input v-model="mailForm.to" placeholder="请输入收件人邮箱"></el-input>
              </el-form-item>
              <el-form-item label="主题">
                <el-input v-model="mailForm.subject" placeholder="请输入邮件主题"></el-input>
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                  v-model="mailForm.content"
                  type="textarea"
                  rows="5"
                  placeholder="请输入邮件内容"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="sendMail" :disabled="!mailForm.to || !mailForm.subject || !mailForm.content">发送邮件</el-button>
              </el-form-item>
            </el-form>
            <el-alert
              v-if="mailResult"
              title="邮件发送成功"
              type="success"
              show-icon
              style="margin-top: 20px;"
            ></el-alert>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="图片OCR(暂停)" name="image-ocr">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>图片 OCR</span>
              </div>
            </template>
            <el-upload
              class="upload-demo"
              :auto-upload="false"
              :on-change="handleOcrImageChange"
              :limit="1"
              accept="image/*"
            >
              <el-button type="primary">选择图片</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  请选择要识别的图片
                </div>
              </template>
            </el-upload>
            <el-form-item style="margin-top: 20px;">
              <el-button type="primary" @click="ocrImage" :loading="ocrLoading" :disabled="!ocrImageFile || ocrLoading">{{ ocrLoading ? '识别中...' : '识别图片' }}</el-button>
            </el-form-item>
            <div v-if="ocrResult" class="ocr-result-container" style="margin-top: 20px; padding: 20px; border: 1px solid #e4e7ed; border-radius: 4px; background-color: #f9fafc;">
              <h3 style="margin-top: 0; margin-bottom: 15px;">识别结果</h3>
              <div class="ocr-text" style="margin-bottom: 20px; padding: 15px; background-color: white; border: 1px solid #e4e7ed; border-radius: 4px; white-space: pre-wrap;">{{ ocrResult.strRes }}</div>
              <div v-if="ocrResult.base64" class="ocr-image">
                <h4 style="margin-top: 0; margin-bottom: 10px;">识别标记</h4>
                <img :src="ocrImageSrc" style="max-width: 100%; border: 1px solid #e4e7ed; border-radius: 4px;" />
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
      </div>

    </el-main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'

// Electron 环境下直接调用后端，去掉 /api 前缀
const isElectron = typeof window !== 'undefined' && window.electronAPI?.isElectron
if (isElectron) {
  axios.defaults.baseURL = 'http://localhost:8099'
  axios.interceptors.request.use(config => {
    config.url = config.url.replace(/^\/api/, '')
    return config
  })
}
import { ElMessage } from 'element-plus'
import MonacoEditor from '@guolao/vue-monaco-editor'
import { Plus, FolderOpened, Download, DocumentCopy, MagicStick, RefreshRight, CopyDocument, Close } from '@element-plus/icons-vue'

const activeTab = ref('json-formatter')

// 从 localStorage 恢复上次选中的选项卡
onMounted(() => {
  const savedTab = localStorage.getItem('activeTab')
  if (savedTab) {
    activeTab.value = savedTab
  }
})

// 监听选项卡变化，保存到 localStorage
watch(activeTab, (newTab) => {
  localStorage.setItem('activeTab', newTab)
})

// 图片压缩相关
const imageFile = ref(null)
const imageForm = ref({
  width: 800,
  height: 600,
  quality: 0.8,
  suffix: 'jpg'
})
const imageResult = ref(null)

// 图片 OCR 相关
const ocrImageFile = ref(null)
const ocrResult = ref(null)
const ocrImageSrc = computed(() => {
  if (!ocrResult.value?.base64) return ''
  return isElectron
    ? `http://localhost:8099/image/image/${ocrResult.value.base64}`
    : `/api/image/image/${ocrResult.value.base64}`
})

// PDF 转 Word 相关
const pdfFile = ref(null)
const pdfResult = ref(null)

// Word 转 PDF 相关
const wordFile = ref(null)
const wordResult = ref(null)

// TXT 转 PDF 相关
const txtForm = ref({
  text: ''
})
const txtResult = ref(null)

// 邮件发送相关
const mailForm = ref({
  to: '',
  subject: '',
  content: ''
})
const mailResult = ref(null)

// JSON 格式化相关
let jsonEditorIdCounter = 0
const jsonEditors = ref([{ id: ++jsonEditorIdCounter, content: '', editorRef: null, filePath: '', fileHandle: null }])

const getEditorFileName = (editor) => {
  if (editor.filePath) return editor.filePath.split(/[\\/]/).pop()
  if (editor.fileHandle) return editor.fileHandle.name
  return ''
}

const editorOptions = {
  automaticLayout: true,
  minimap: { enabled: false },
  fontSize: 13,
  scrollBeyondLastLine: false,
  tabSize: 2
}

const addJsonEditor = () => {
  jsonEditors.value.push({ id: ++jsonEditorIdCounter, content: '', editorRef: null, filePath: '', fileHandle: null, width: 500 })
}

const removeJsonEditor = (id) => {
  jsonEditors.value = jsonEditors.value.filter(e => e.id !== id)
}

const startResize = (id, e) => {
  e.preventDefault()
  const editors = jsonEditors.value
  const leftIdx = editors.findIndex(ed => ed.id === id)
  if (leftIdx === -1) return
  const left = editors[leftIdx]
  const right = leftIdx < editors.length - 1 ? editors[leftIdx + 1] : null
  const startX = e.clientX
  const leftStart = e.target.parentElement.offsetWidth
  const rightStart = right ? e.target.parentElement.nextElementSibling?.offsetWidth || 0 : 0
  const onMove = (ev) => {
    const delta = ev.clientX - startX
    if (right) {
      const newLeft = Math.max(500, leftStart + delta)
      const newRight = Math.max(500, rightStart - delta)
      if (newLeft >= 500 && newRight >= 500) {
        left.width = newLeft
        right.width = newRight
      }
    } else {
      left.width = Math.max(500, leftStart + delta)
    }
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

const resetEditorWidths = () => {
  jsonEditors.value.forEach(e => { e.width = 0 })
}

const onEditorMount = (id, editor) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (item) item.editorRef = editor
}

const beautifyJson = (id) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (item?.editorRef) {
    item.editorRef.getAction('editor.action.formatDocument')?.run()
    item.editorRef.setScrollPosition({ scrollTop: 0 })
  }
}

const restructureJson = (id) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (!item?.content?.trim()) {
    ElMessage.warning('请先输入 JSON 内容')
    return
  }
  try {
    let data = JSON.parse(item.content)
    // 自动查找数组：顶层是数组，或 data 字段是数组
    let arr = null
    if (Array.isArray(data)) {
      arr = data
    } else if (data && typeof data === 'object') {
      for (const key of ['data', 'list', 'items', 'rows', 'records']) {
        if (Array.isArray(data[key])) {
          arr = data[key]
          break
        }
      }
    }
    if (!arr) {
      ElMessage.warning('未找到可递归的数组数据')
      return
    }
    // 如果是嵌套树结构（含 children），先打平再递归
    arr = flatten(arr)
    // 查找 parentId 字段
    const parentKey = findParentKey(arr[0])
    if (!parentKey) {
      ElMessage.warning('未找到 parentId / parent_id 字段')
      return
    }
    const idKey = arr[0].id !== undefined ? 'id' : 'id_'
    const result = restructure(arr, parentKey, idKey)
    item.content = JSON.stringify(data && typeof data === 'object' && !Array.isArray(data) ? { ...data, data: result } : result, null, 2)
    ElMessage.success('递归重构完成')
  } catch (e) {
    ElMessage.error('JSON 解析失败: ' + e.message)
  }
}

function findParentKey(obj) {
  if (!obj || typeof obj !== 'object') return null
  const candidates = ['parentId', 'parent_id', 'parentID', 'parent', 'pid']
  for (const key of candidates) {
    if (key in obj) return key
  }
  return null
}

function flatten(arr) {
  const result = []
  const walk = (items) => {
    items.forEach(item => {
      const copy = { ...item }
      const children = copy.children
      delete copy.children
      result.push(copy)
      if (Array.isArray(children)) walk(children)
    })
  }
  walk(arr)
  return result
}

function restructure(arr, parentKey, idKey) {
  const map = new Map()
  arr.forEach(item => map.set(item[idKey], { ...item }))
  const roots = []
  map.forEach(item => {
    const pid = item[parentKey]
    if (pid && map.has(pid)) {
      const parent = map.get(pid)
      if (!parent.children) parent.children = []
      parent.children.push(item)
    } else {
      roots.push(item)
    }
  })
  return roots
}

const copyJsonOutput = async (id) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (!item?.content) return
  try {
    await navigator.clipboard.writeText(item.content)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 打开文件
const openJsonFile = async (id) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (!item) return
  // Electron 环境
  if (window.electronAPI?.isElectron) {
    const result = await window.electronAPI.openFile()
    if (!result) return
    item.content = result.content
    item.filePath = result.filePath
    item.fileHandle = null
    ElMessage.success('文件已打开')
    return
  }
  // 浏览器：File System Access API
  if (window.showOpenFilePicker) {
    try {
      const [handle] = await window.showOpenFilePicker({
        types: [{ description: 'JSON 文件', accept: { 'application/json': ['.json'] } }],
        multiple: false
      })
      const file = await handle.getFile()
      item.content = await file.text()
      item.fileHandle = handle
      item.filePath = ''
      ElMessage.success('文件已打开')
    } catch (e) {
      if (e.name !== 'AbortError') ElMessage.error('打开失败')
    }
    return
  }
  // 降级：input[type=file]
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    item.content = await file.text()
    item.fileHandle = null
    item.filePath = ''
    ElMessage.success('文件已打开')
  }
  input.click()
}

// 保存文件
const saveJsonFile = async (id) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (!item) return
  // Electron：直接写入已知路径
  if (window.electronAPI?.isElectron) {
    if (item.filePath) {
      await window.electronAPI.saveFile(item.filePath, item.content)
      ElMessage.success('文件已保存')
    } else {
      await saveJsonFileAs(id)
    }
    return
  }
  // 浏览器：有 fileHandle 则直接写回
  if (item.fileHandle) {
    try {
      const writable = await item.fileHandle.createWritable()
      await writable.write(item.content)
      await writable.close()
      ElMessage.success('文件已保存')
    } catch (e) {
      if (e.name !== 'AbortError') ElMessage.error('保存失败')
    }
    return
  }
  // 没有 fileHandle，走另存为
  await saveJsonFileAs(id)
}

// 另存为
const saveJsonFileAs = async (id) => {
  const item = jsonEditors.value.find(e => e.id === id)
  if (!item) return
  // Electron
  if (window.electronAPI?.isElectron) {
    const savedPath = await window.electronAPI.saveFileAs(item.content)
    if (savedPath) {
      item.filePath = savedPath
      ElMessage.success('文件已保存')
    }
    return
  }
  // 浏览器：File System Access API
  if (window.showSaveFilePicker) {
    try {
      const handle = await window.showSaveFilePicker({
        suggestedName: getEditorFileName(item) || 'untitled.json',
        types: [{ description: 'JSON 文件', accept: { 'application/json': ['.json'] } }]
      })
      const writable = await handle.createWritable()
      await writable.write(item.content)
      await writable.close()
      item.fileHandle = handle
      item.filePath = ''
      ElMessage.success('文件已保存')
    } catch (e) {
      if (e.name !== 'AbortError') ElMessage.error('保存失败')
    }
    return
  }
  // 降级：下载链接
  const blob = new Blob([item.content], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = getEditorFileName(item) || 'untitled.json'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('文件已下载')
}

// 处理图片选择
const handleImageChange = (file) => {
  imageFile.value = file.raw
}

// 处理 OCR 图片选择
const handleOcrImageChange = (file) => {
  ocrImageFile.value = file.raw
}

// 处理 PDF 文件选择
const handlePdfChange = (file) => {
  pdfFile.value = file.raw
}

// 处理 Word 文件选择
const handleWordChange = (file) => {
  wordFile.value = file.raw
}

// 压缩图片
const compressImage = async () => {
  const formData = new FormData()
  formData.append('file', imageFile.value)
  formData.append('width', imageForm.value.width)
  formData.append('height', imageForm.value.height)
  formData.append('quality', imageForm.value.quality)
  formData.append('suffix', imageForm.value.suffix)

  try {
    const response = await axios.post('/api/image/compress', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    imageResult.value = response.data
    ElMessage.success('压缩成功')
  } catch (error) {
    console.error('压缩失败:', error)
    ElMessage.error('压缩失败: ' + (error.response?.data || error.message))
  }
}

// 识别图片
const ocrLoading = ref(false)
const ocrImage = async () => {
  if (!ocrImageFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  ocrLoading.value = true
  ocrResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', ocrImageFile.value)
    const response = await axios.post('/api/image/ocr', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ocrResult.value = response.data
    ElMessage.success('识别完成')
  } catch (error) {
    console.error('识别失败:', error)
    ElMessage.error('识别失败: ' + (error.response?.data || error.message))
  } finally {
    ocrLoading.value = false
  }
}

// PDF 转 Word
const pdfToWord = async () => {
  const formData = new FormData()
  formData.append('file', pdfFile.value)

  try {
    const response = await axios.post('/api/pdf/toword', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    pdfResult.value = response.data
    ElMessage.success('转换成功')
  } catch (error) {
    console.error('转换失败:', error)
    ElMessage.error('转换失败: ' + (error.response?.data || error.message))
  }
}

// Word 转 PDF
const wordToPdf = async () => {
  const formData = new FormData()
  formData.append('file', wordFile.value)

  try {
    const response = await axios.post('/api/word/topdf', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    wordResult.value = response.data
    ElMessage.success('转换成功')
  } catch (error) {
    console.error('转换失败:', error)
    ElMessage.error('转换失败: ' + (error.response?.data || error.message))
  }
}

// TXT 转 PDF
const txtToPdf = async () => {
  try {
    const response = await axios.post('/api/TxtTo/toPdf', {
      text: txtForm.value.text
    })
    txtResult.value = response.data
    ElMessage.success('TXT converted to PDF successfully')
  } catch (error) {
    console.error('Conversion failed:', error)
    ElMessage.error('Conversion failed: ' + (error.response?.data || error.message))
  }
}

// 发送邮件
const sendMail = async () => {
  try {
    const response = await axios.post('/api/MailTo/sendMail', {
      to: mailForm.value.to,
      subject: mailForm.value.subject,
      content: mailForm.value.content
    })
    mailResult.value = true
    ElMessage.success('Mail sent successfully')
  } catch (error) {
    console.error('Send failed:', error)
    ElMessage.error('Send failed: ' + (error.response?.data || error.message))
  }
}

// 下载图片
const downloadImage = async (imageName) => {
  if (window.electronAPI?.isElectron) {
    try {
      const resp = await fetch(`http://localhost:8099/image/image/${imageName}`)
      const blob = await resp.blob()
      const arrayBuffer = await blob.arrayBuffer()
      const savedPath = await window.electronAPI.saveBlob(imageName, Array.from(new Uint8Array(arrayBuffer)))
      if (savedPath) ElMessage.success('文件已保存')
    } catch (e) {
      ElMessage.error('下载失败: ' + e.message)
    }
  } else {
    window.open(`/api/image/image/${imageName}`)
  }
}

// 下载文件
const downloadFile = async (filename, oldname) => {
  if (window.electronAPI?.isElectron) {
    try {
      const resp = await fetch(`http://localhost:8099/image/image/${filename}`)
      const blob = await resp.blob()
      const arrayBuffer = await blob.arrayBuffer()
      const savedPath = await window.electronAPI.saveBlob(oldname || filename, Array.from(new Uint8Array(arrayBuffer)))
      if (savedPath) ElMessage.success('文件已保存')
    } catch (e) {
      ElMessage.error('下载失败: ' + e.message)
    }
  } else {
    window.open(`/api/image/image/${filename}`)
  }
}
</script>

<style scoped>
/* 全局隐藏滚动条 */
:deep(html),
:deep(body) {
  overflow: hidden;
}

.app-container {
  height: 98vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}


.tabs-header {
  display: flex;
  align-items: flex-start;
}

.tabs-header :deep(.el-tabs) {
  flex: 1;
}

.el-main {
  position: relative;
  flex: 1;
  min-width: 0;
  padding: 8px 20px 20px 20px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* JSON tab: 让 tabs → tab-pane → card → wrapper 逐层 flex 撑满 */
:deep(.el-tabs) {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__content) {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

:deep(.el-tab-pane) {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 92vh;
}

.el-footer {
  background-color: #f5f7fa;
  text-align: center;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.upload-demo {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
}

/* JSON 格式化样式 */
.json-editors-scroll {
  flex: 1;
  height: 0;
  min-width: 0;
  display: flex;
  flex-direction: row;
  gap: 12px;
  overflow-x: auto;
  overflow-y: hidden;
}

:deep(.json-tab-card) {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.json-tab-card > .el-card__header) {
  padding: 6px 20px;
}

:deep(.json-tab-card > .el-card__body) {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.json-editor-block {
  display: flex;
  flex-direction: column;
  flex: 1 0 0;
  min-width: 500px;
  position: relative;
}

.json-editor-toolbar {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  gap: 0;
}

.json-editor-toolbar :deep(.el-button) {
  border: none;
  background: transparent;
  padding: 4px 6px;
  box-shadow: none;
  margin-left: 0;
}

.json-editor-toolbar :deep(.el-button .el-icon) {
  font-size: 16px;
}

.json-editor-toolbar :deep(.el-button):hover {
  background: #e8eaed;
}

.json-editor-toolbar :deep(.el-button--danger) {
  color: #f56c6c;
}

.json-editor-toolbar :deep(.el-button--danger):hover {
  background: #fef0f0;
}

.json-editor-toolbar :deep(.el-button--primary) {
  color: #409eff;
}

.json-editor-toolbar :deep(.el-button--primary):hover {
  background: #ecf5ff;
}

.json-editor-label {
  font-weight: 500;
  font-size: 13px;
  color: #606266;
}

.json-single-editor {
  flex: 1;
  min-height: 250px;
  border: 1px solid #dcdfe6;
  border-radius: 0 0 4px 4px;
  overflow: hidden;
}

.json-resize-handle {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 12px;
  cursor: col-resize;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.json-resize-handle::after {
  content: '';
  width: 2px;
  height: 30px;
  background: #dcdfe6;
  border-radius: 1px;
  transition: background 0.15s, height 0.15s;
}

.json-resize-handle:hover::after {
  background: #409EFF;
  height: 50px;
}
</style>
