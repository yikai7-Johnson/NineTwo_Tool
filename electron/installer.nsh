; 安装完成后刷新图标缓存
!macro customInit
  ; 安装开始时不做额外操作
!macroend

!macro customInstall
  ; 安装完成后清除图标缓存
  nsExec::ExecToLog 'cmd.exe /c ie4uinit.exe -ClearIconCache'
!macroend

!macro customUnInit
  ; 卸载完成后清除图标缓存
  nsExec::ExecToLog 'cmd.exe /c ie4uinit.exe -ClearIconCache'
!macroend
