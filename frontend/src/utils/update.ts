import Taro from '@tarojs/taro';

let registered = false;

export function registerUpdatePrompt(): void {
  if (registered || process.env.TARO_ENV !== 'weapp' || typeof Taro.getUpdateManager !== 'function') return;
  const manager = Taro.getUpdateManager();
  registered = true;
  manager.onUpdateReady(() => {
    Taro.showModal({
      title: '新版本已准备好',
      content: '请保存正在填写的内容。更新后可使用最新课表与登录功能，是否现在重启更新？',
      confirmText: '重启更新',
      cancelText: '稍后更新',
      success: result => { if (result.confirm) manager.applyUpdate(); },
    });
  });
  manager.onUpdateFailed(() => {
    Taro.showModal({
      title: '新版本下载失败',
      content: '请检查网络，关闭小程序后重新进入再试。',
      showCancel: false,
    });
  });
}
