const { app, BrowserWindow, ipcMain, dialog, shell } = require('electron')
const path = require('path')
const { spawn } = require('child_process')
const net = require('net')
const fs = require('fs')

let mainWindow = null
let javaProcess = null

const isDev = !app.isPackaged

// 资源路径
function getResourcePath(...segments) {
  if (isDev) {
    return path.join(__dirname, 'resources', ...segments)
  }
  return path.join(process.resourcesPath, ...segments)
}

// 获取应用根路径（开发模式下为项目根目录，打包后为 app.asar 所在目录）
function getAppRootPath() {
  if (isDev) {
    return path.join(__dirname, '..')
  }
  return path.join(process.resourcesPath, '..')
}

// 用户数据路径（日志、文件存储）
const userDataPath = app.getPath('userData')
const logPath = path.join(userDataPath, 'logs')
const filePath = path.join(userDataPath, 'files')

// 确保目录存在
function ensureDirs() {
  ;[logPath, filePath].forEach(dir => {
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true })
  })
}

// 检测端口是否就绪
function waitForPort(port, timeout = 60000) {
  return new Promise((resolve, reject) => {
    const start = Date.now()
    const check = () => {
      const sock = net.createConnection({ port }, () => {
        sock.end()
        resolve()
      })
      sock.on('error', () => {
        if (Date.now() - start > timeout) {
          reject(new Error(`端口 ${port} 等待超时`))
        } else {
          setTimeout(check, 500)
        }
      })
    }
    check()
  })
}

// 启动 Java 后端
async function startJava() {
  const javaExe = getResourcePath('jre', 'jdk-21', 'bin', 'java.exe')
  const jarPath = getResourcePath('app.jar')
  const configPath = getResourcePath('application-electron.yml')

  if (!fs.existsSync(javaExe)) {
    throw new Error('JRE 未找到: ' + javaExe)
  }
  if (!fs.existsSync(jarPath)) {
    throw new Error('后端 JAR 未找到: ' + jarPath)
  }

  console.log('[Java] 启动后端服务...')
  javaProcess = spawn(javaExe, [
    '-jar', jarPath,
    `--spring.config.location=file:${configPath}`,
    `--file-storage.path=${filePath}${path.sep}`,
    `--logging.file.path=${logPath}`
  ], {
    stdio: 'pipe',
    windowsHide: true,
    env: { ...process.env, JAVA_HOME: getResourcePath('jre', 'jdk-21') }
  })

  javaProcess.stdout?.on('data', d => console.log('[Java]', d.toString().trim()))
  javaProcess.stderr?.on('data', d => console.error('[Java]', d.toString().trim()))
  javaProcess.on('error', e => {
    console.error('[Java] 进程错误:', e)
    const { dialog } = require('electron')
    dialog.showErrorBox('Java 启动错误', e.message)
  })
  javaProcess.on('exit', code => {
    console.log('[Java] 进程退出:', code)
    if (code !== null && code !== 0) {
      const { dialog } = require('electron')
      dialog.showErrorBox('Java 异常退出', `退出码: ${code}`)
    }
  })

  await waitForPort(8099, 120000)
  console.log('[Java] 后端服务就绪')
}

// 停止服务
function stopServices() {
  console.log('[App] 停止服务...')
  if (javaProcess) {
    javaProcess.kill()
    javaProcess = null
  }
}

// JSON 文件操作 IPC
ipcMain.handle('dialog:openFile', async () => {
  const win = BrowserWindow.getFocusedWindow()
  const result = await dialog.showOpenDialog(win, {
    title: '打开 JSON 文件',
    filters: [
      { name: 'JSON 文件', extensions: ['json'] },
      { name: '所有文件', extensions: ['*'] }
    ],
    properties: ['openFile']
  })
  if (result.canceled || !result.filePaths.length) return null
  const filePath = result.filePaths[0]
  const content = fs.readFileSync(filePath, 'utf-8')
  return { filePath, content }
})

ipcMain.handle('dialog:saveFileAs', async (_event, content) => {
  const win = BrowserWindow.getFocusedWindow()
  const result = await dialog.showSaveDialog(win, {
    title: '另存为',
    filters: [
      { name: 'JSON 文件', extensions: ['json'] },
      { name: '所有文件', extensions: ['*'] }
    ]
  })
  if (result.canceled || !result.filePath) return null
  fs.writeFileSync(result.filePath, content, 'utf-8')
  return result.filePath
})

ipcMain.handle('file:save', async (_event, { filePath, content }) => {
  fs.writeFileSync(filePath, content, 'utf-8')
  return true
})

ipcMain.handle('download', async (_event, url) => {
  if (mainWindow) {
    mainWindow.webContents.downloadURL(url)
  }
})

ipcMain.handle('file:saveBlob', async (_event, fileName, buffer) => {
  const result = await dialog.showSaveDialog(mainWindow, {
    title: '保存文件',
    defaultPath: fileName,
  })
  if (result.canceled || !result.filePath) return null
  fs.writeFileSync(result.filePath, Buffer.from(buffer))
  return result.filePath
})

// 创建窗口
function createWindow() {
  const iconPath = isDev
    ? path.join(__dirname, '..', 'ninetwo.ico')
    : path.join(process.resourcesPath, 'ninetwo.ico')

  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 800,
    minHeight: 600,
    title: 'NineTwoTool',
    icon: iconPath,
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(app.getAppPath(), 'electron', 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  })

  if (isDev) {
    mainWindow.loadURL('http://localhost:3000')
    mainWindow.webContents.openDevTools()
  } else {
    const indexPath = path.join(app.getAppPath(), 'web-app', 'dist', 'index.html')
    if (!fs.existsSync(indexPath)) {
      console.error('[App] 前端文件未找到:', indexPath)
      dialog.showErrorBox('启动失败', '前端文件未找到: ' + indexPath)
      return
    }
    mainWindow.loadFile(indexPath)
  }

  // 处理下载
  mainWindow.webContents.session.on('will-download', (event, item) => {
    const fileName = item.getFilename()
    const savePath = path.join(app.getPath('downloads'), fileName)
    item.setSavePath(savePath)
    item.on('done', (event, state) => {
      if (state === 'completed') {
        dialog.showMessageBox(mainWindow, {
          type: 'info',
          title: '下载完成',
          message: `文件已保存到：\n${savePath}`,
          buttons: ['打开文件', '打开文件夹', '确定']
        }).then(({ response }) => {
          if (response === 0) shell.openPath(savePath)
          else if (response === 1) shell.showItemInFolder(savePath)
        })
      }
    })
  })

  mainWindow.on('closed', () => { mainWindow = null })
}

// 启动
app.setAppUserModelId('com.ninetwo.toolbox.app')
app.whenReady().then(async () => {
  try {
    ensureDirs()
    await startJava()
    createWindow()
  } catch (e) {
    console.error('[App] 启动失败:', e)
    const { dialog } = require('electron')
    dialog.showErrorBox('启动失败', e.message)
    app.quit()
  }
})

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow()
})

app.on('before-quit', () => {
  stopServices()
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
