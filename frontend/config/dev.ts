import type { UserConfigExport } from '@tarojs/cli';
export default {
  logger: {
    quiet: false,
    stats: true,
  },
  mini: {},
  h5: {
    devServer: {
      open: false,
      port: 3030,
      host: '127.0.0.1',
      hot: 'only',
      liveReload: false,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  },
} satisfies UserConfigExport<'webpack5'>;
