const fs = require('fs')
const path = require('path')
const { NtExecutable, NtExecutableResource, Resource, Data } = require('resedit')

module.exports = async function afterPack(context) {
  if (context.electronPlatformName !== 'win32') {
    return
  }

  const exeName = `${context.packager.appInfo.productFilename}.exe`
  const exePath = path.join(context.appOutDir, exeName)
  const iconPath = path.join(context.packager.projectDir, 'ninetwo.ico')

  if (!fs.existsSync(exePath) || !fs.existsSync(iconPath)) {
    return
  }

  const exeData = fs.readFileSync(exePath)
  const exe = NtExecutable.from(exeData)
  const resource = NtExecutableResource.from(exe)
  const iconFile = Data.IconFile.from(fs.readFileSync(iconPath))

  let lang = 1033
  const versionInfoList = Resource.VersionInfo.fromEntries(resource.entries)
  if (versionInfoList.length > 0) {
    const languages = versionInfoList[0].getAllLanguagesForStringValues()
    if (languages.length > 0) {
      lang = languages[0].lang
    }
  }

  const iconData = iconFile.icons.map(item => item.data)
  const iconGroups = Resource.IconGroupEntry.fromEntries(resource.entries)

  if (iconGroups.length === 0) {
    Resource.IconGroupEntry.replaceIconsForResource(
      resource.entries,
      101,
      lang,
      iconData
    )
  } else {
    for (const group of iconGroups) {
      Resource.IconGroupEntry.replaceIconsForResource(
        resource.entries,
        group.id,
        group.lang,
        iconData
      )
    }
  }

  resource.outputResource(exe)
  fs.writeFileSync(exePath, Buffer.from(exe.generate()))
}
