const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
  platform: process.platform,
  isElectron: true,
  openFile: () => ipcRenderer.invoke('dialog:openFile'),
  saveFileAs: (content) => ipcRenderer.invoke('dialog:saveFileAs', content),
  saveFile: (filePath, content) => ipcRenderer.invoke('file:save', { filePath, content }),
  saveBlob: (fileName, buffer) => ipcRenderer.invoke('file:saveBlob', fileName, buffer)
})
