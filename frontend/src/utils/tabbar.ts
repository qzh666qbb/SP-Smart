import Taro from '@tarojs/taro';

export const TABBAR_ACTIVE_EVENT = 'tabbar:active';

export function notifyTabBarActive(pagePath: string) {
  Taro.eventCenter.trigger(TABBAR_ACTIVE_EVENT, pagePath);
}
