const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');
const ts = require('typescript');

function loadModule(file, dependencies) {
  const filename = path.join(__dirname, '..', file);
  const { outputText } = ts.transpileModule(fs.readFileSync(filename, 'utf8'), {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2020,
      esModuleInterop: true,
      jsx: ts.JsxEmit.ReactJSX,
    },
  });
  const context = {
    exports: {},
    require: (id) => {
      assert.ok(id in dependencies, `Unexpected dependency: ${id}`);
      return dependencies[id];
    },
    process: { env: { TARO_ENV: 'weapp' } },
    API_BASE_URL: 'https://example.test/api',
  };
  vm.runInNewContext(outputText, context, { filename });
  return context.exports;
}

const expired = { code: 20002, message: '令牌已过期' };
const refreshed = { code: 0, data: { accessToken: 'fresh', refreshToken: 'next-refresh' } };

function requestHarness(respond, refreshToken = 'refresh') {
  const storage = { AUTH_TOKEN: 'expired', AUTH_REFRESH_TOKEN: refreshToken };
  const calls = [];
  const toasts = [];
  let logoutCount = 0;
  let redirectCount = 0;
  const taro = {
    getStorageSync: (key) => storage[key],
    setStorageSync: (key, value) => { storage[key] = value; },
    removeStorageSync: (key) => { delete storage[key]; },
    showToast: ({ title }) => toasts.push(title),
    getCurrentPages: () => [{ route: 'pages/home/index' }],
    reLaunch: () => { redirectCount++; return Promise.resolve(); },
    request: (options) => {
      calls.push(options);
      queueMicrotask(() => {
        const result = respond(options, calls.length);
        if (result.fail) options.fail(result.fail);
        else options.success({ statusCode: result.statusCode || 200, data: result });
      });
    },
  };
  const store = {
    getState: () => ({ token: storage.AUTH_TOKEN, logout: () => { logoutCount++; } }),
    setState: () => {},
  };
  return {
    ...loadModule('src/utils/request.ts', {
      '@tarojs/taro': taro,
      '@/store/auth': { default: store },
    }),
    storage, calls, toasts,
    get logoutCount() { return logoutCount; },
    get redirectCount() { return redirectCount; },
  };
}

for (const scenario of [
  { name: '业务拒绝', response: { code: 30001, message: '业务拒绝' }, message: '业务拒绝' },
  { name: '网络异常', response: { fail: { errMsg: '网络不可用' } }, message: '网络不可用' },
]) {
  test(`刷新成功后重试出现${scenario.name}，保留登录并返回原错误`, async () => {
    const responses = [expired, refreshed, scenario.response];
    const h = requestHarness(() => responses.shift());
    await assert.rejects(h.request('/business/example'), (error) =>
      (error.message || error.errMsg) === scenario.message);
    assert.equal(h.storage.AUTH_TOKEN, 'fresh');
    assert.equal(h.storage.AUTH_REFRESH_TOKEN, 'next-refresh');
    assert.equal(h.logoutCount, 0);
    assert.equal(h.redirectCount, 0);
    assert.equal(h.calls[2].header.Authorization, 'Bearer fresh');
  });
}

for (const response of [
  { fail: { errMsg: '网络不可用' } },
  { code: 500, message: '服务器暂不可用', statusCode: 500 },
  { code: 0, data: {} },
]) {
  test(`续期暂时失败保留登录：${JSON.stringify(response)}`, async () => {
    const responses = [expired, response];
    const h = requestHarness(() => responses.shift());
    await assert.rejects(h.request('/business/example'));
    assert.equal(h.storage.AUTH_TOKEN, 'expired');
    assert.equal(h.storage.AUTH_REFRESH_TOKEN, 'refresh');
    assert.equal(h.logoutCount, 0);
    assert.equal(h.redirectCount, 0);
    assert.equal(h.calls.length, 2);
  });
}

for (const responses of [
  [expired, { code: 20001, message: '续期令牌无效' }],
  [expired, refreshed, expired],
]) {
  test(`明确认证失效时退出且不会无限续期（${responses.length} 次请求）`, async () => {
    const h = requestHarness(() => responses.shift());
    await assert.rejects(h.request('/business/example'));
    assert.equal(h.storage.AUTH_TOKEN, undefined);
    assert.equal(h.logoutCount, 1);
    assert.equal(h.redirectCount, 1);
  });
}

test('没有续期令牌时认证失效直接退出', async () => {
  const h = requestHarness(() => expired, null);
  await assert.rejects(h.request('/business/example'));
  assert.equal(h.calls.length, 1);
  assert.equal(h.logoutCount, 1);
});

test('并发认证失败共享一次续期并各自重试', async () => {
  const h = requestHarness((options) => {
    if (options.url.endsWith('/auth/refresh')) return refreshed;
    return options.header.Authorization === 'Bearer fresh'
      ? { code: 0, data: 'ok' } : expired;
  });
  const results = await Promise.all([h.request('/business/a'), h.request('/business/b')]);
  assert.deepEqual(results, ['ok', 'ok']);
  assert.equal(h.calls.filter((call) => call.url.endsWith('/auth/refresh')).length, 1);
  assert.equal(h.logoutCount, 0);
});

test('体测分项统一字段和 ID，保留零分及缺失成绩', async () => {
  const api = loadModule('src/services/api.ts', {
    '@/utils/request': {
      get: async () => ({ list: [{ id: 1, studentId: 2, scores: [
        { id: 3, recordId: 1, itemId: 4, rawValue: 0, convertedScore: 0, level: 'FAIL', unit: 'cm' },
        { itemId: 5, rawValue: null, convertedScore: null, level: null },
        { itemId: 6, name: '兼容项目', value: 12, score: 80, convertedLevel: 'GOOD' },
      ] }] }),
    },
    './cloud': { callWithFallback: (fn) => fn() },
    '@/utils/pagination': loadModule('src/utils/pagination.ts', {}),
  });
  const { records } = await api.getTeacherPhysicalTests();
  const [zero, missing, legacy] = records[0].details;
  assert.equal(zero.id, '3');
  assert.equal(zero.recordId, '1');
  assert.equal(zero.itemId, '4');
  assert.equal(zero.rawValue, 0);
  assert.equal(zero.score, 0);
  assert.equal(zero.level, 'FAIL');
  assert.equal(missing.rawValue, undefined);
  assert.equal(missing.convertedScore, undefined);
  assert.equal(missing.level, undefined);
  assert.equal(legacy.itemName, '兼容项目');
  assert.equal(legacy.rawValue, 12);
  assert.equal(legacy.convertedScore, 80);
  assert.equal(legacy.level, 'GOOD');
  assert.equal(records[0].details, records[0].scores);
});

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((yes, no) => { resolve = yes; reject = no; });
  return { promise, resolve, reject };
}

const flush = () => new Promise((resolve) => setImmediate(resolve));

function appHarness() {
  let onShow;
  let requests = 0;
  const pending = deferred();
  const storage = {
    getStoredToken: () => 'old-token',
    getStoredRefreshToken: () => 'old-refresh',
    getStoredUser: () => ({ id: 'old-user', role: 'TEACHER' }),
    getStoredProfile: () => null,
    setStoredToken() {}, setStoredRefreshToken() {},
    setStoredUser() {}, setStoredProfile() {}, clearAuthStorage() {},
  };
  const store = loadModule('src/store/auth.ts', {
    zustand: require('zustand'), '@/utils/request': storage,
  }).default;
  const App = loadModule('src/app.tsx', {
    react: { useEffect: (effect) => effect(), useRef: (value) => ({ current: value }) },
    '@tarojs/taro': { useDidShow(fn) { onShow = fn; }, useDidHide() {} },
    '@/services/api': { getCurrentUser: () => { requests++; return pending.promise; } },
    '@/store/auth': { __esModule: true, default: store },
    '@/utils/profile': { mergeProfile: (_, incoming) => incoming },
    './app.scss': {},
  }).default;
  App({ children: null });
  return { store, pending, show: () => onShow(), get requests() { return requests; } };
}

test('退出后忽略尚未完成的启动校验响应', async () => {
  const { store, pending } = appHarness();
  store.getState().logout();
  pending.resolve({ user: { id: 'old-user' }, profile: null });
  await flush();
  assert.equal(store.getState().isLoggedIn, false);
  assert.equal(store.getState().token, null);
  assert.equal(store.getState().user, null);
});

test('退出后未完成的续期响应不会重新写入令牌', async () => {
  const h = requestHarness((options) => {
    if (!options.url.endsWith('/auth/refresh')) return expired;
    for (const key of Object.keys(h.storage)) delete h.storage[key];
    return refreshed;
  });
  await assert.rejects(h.request('/auth/me'), /登录状态已变化/);
  assert.equal(h.storage.AUTH_TOKEN, undefined);
  assert.equal(h.storage.AUTH_REFRESH_TOKEN, undefined);
  assert.equal(h.logoutCount, 0);
});

test('重新登录同一账户后忽略旧会话返回的资料', async () => {
  const { store, pending } = appHarness();
  store.getState().logout();
  store.getState().login('new-token', { id: 'old-user', nickname: '新昵称' }, null, 'new-refresh');
  pending.resolve({ user: { id: 'old-user', nickname: '旧昵称' }, profile: null });
  await flush();
  assert.equal(store.getState().token, 'new-token');
  assert.equal(store.getState().user.nickname, '新昵称');
});

test('同一会话刷新令牌后仍接受启动校验响应', async () => {
  const { store, pending } = appHarness();
  store.setState({ token: 'refreshed-token', refreshToken: 'refreshed-refresh' });
  pending.resolve({ user: { id: 'old-user', nickname: '最新资料' }, profile: null });
  await flush();
  assert.equal(store.getState().isValidated, true);
  assert.equal(store.getState().token, 'refreshed-token');
  assert.equal(store.getState().user.nickname, '最新资料');
});

test('学员和体测列表继续加载第 200 条之后的数据', async () => {
  const calls = [];
  const api = loadModule('src/services/api.ts', {
    '@/utils/request': { get: async (url, params) => {
      calls.push({ url, ...params });
      const start = (params.pageNum - 1) * params.pageSize;
      return { total: 401, list: Array.from({ length: Math.min(200, 401 - start) }, (_, i) => ({
        id: start + i + 1, studentId: start + i + 1, name: `学员${start + i + 1}`,
      })) };
    } },
    './cloud': { callWithFallback: (fn) => fn() },
    '@/utils/pagination': loadModule('src/utils/pagination.ts', {}),
  });
  const students = await api.getAllTeacherStudents('姓名');
  const records = await api.getAllTeacherPhysicalTests();
  assert.equal(students.length, 401);
  assert.equal(records.length, 401);
  assert.equal(students[400].id, '401');
  assert.equal(records[400].id, '401');
  assert.deepEqual(calls.map((call) => call.pageNum), [1, 2, 3, 1, 2, 3]);
  assert.ok(calls.slice(0, 3).every((call) => call.keyword === '姓名'));
});

test('退出登录调用后端接口', async () => {
  const calls = [];
  const api = loadModule('src/services/api.ts', {
    '@/utils/request': { post: async (url, data) => { calls.push({ url, data }); } },
    './cloud': { callWithFallback: (fn) => fn() },
    '@/utils/pagination': loadModule('src/utils/pagination.ts', {}),
  });
  await api.logoutSession();
  assert.deepEqual(calls, [{ url: '/auth/logout', data: undefined }]);
});

test('体测提交使用选择的批次，不写死批次 ID', async () => {
  let request;
  const api = loadModule('src/services/api.ts', {
    '@/utils/request': { post: async (url, data) => { request = { url, data }; return data; } },
    './cloud': { callWithFallback: (fn) => fn() },
    '@/utils/pagination': loadModule('src/utils/pagination.ts', {}),
  });
  await api.addPhysicalTest({
    batchId: 27,
    studentId: '8',
    scores: [{ itemId: '5', rawValue: 12, convertedScore: 80, level: 'GOOD' }],
  });
  assert.equal(request.url, '/business/physical-tests');
  assert.equal(request.data.batchId, 27);
  assert.equal(request.data.studentId, 8);
  assert.equal(request.data.scores[0].itemId, 5);
});

test('体测项目加载失败直接报错，不使用本地假项目', async () => {
  const api = loadModule('src/services/api.ts', {
    '@/utils/request': { get: async () => { throw new Error('项目接口失败'); } },
    './cloud': { callWithFallback: (fn) => fn() },
    '@/utils/pagination': loadModule('src/utils/pagination.ts', {}),
  });
  await assert.rejects(api.getPhysicalTestItems(), /项目接口失败/);
});

test('体测录入页使用全量学员和后端批次，且没有本地项目 ID', () => {
  const source = fs.readFileSync(path.join(__dirname, '..', 'src/pages/physical-add/index.tsx'), 'utf8');
  assert.match(source, /getAllTeacherStudents\(\)/);
  assert.match(source, /getPhysicalTestBatches\(\)/);
  assert.doesNotMatch(source, /getTeacherStudents\(\{\s*pageNum:\s*1/);
  assert.doesNotMatch(source, /const\s+defaultItems/);
});

test('分页中途失败不会把部分结果当作完整列表', async () => {
  const { collectPages } = loadModule('src/utils/pagination.ts', {});
  await assert.rejects(collectPages(async (page) => {
    if (page === 2) throw new Error('网络错误');
    return { items: [1], total: 2 };
  }), /网络错误/);
  await assert.rejects(collectPages(async (page) => ({
    items: page === 1 ? [1] : [], total: 2,
  })), /重新加载/);
});

function pageHarness(file, api, overrides = {}) {
  const slots = [];
  const effects = [];
  let cursor = 0;
  let onShow;
  const hooks = {
    useState: (initial) => {
      const index = cursor++;
      if (!(index in slots)) slots[index] = initial;
      return [slots[index], (value) => { slots[index] = typeof value === 'function' ? value(slots[index]) : value; }];
    },
    useRef: (initial) => {
      const index = cursor++;
      if (!(index in slots)) slots[index] = { current: initial };
      return slots[index];
    },
    useEffect: (effect, deps) => {
      const index = cursor++;
      const previous = slots[index];
      if (!previous || deps.some((dep, i) => dep !== previous.deps[i])) {
        effects.push(() => {
          previous?.cleanup?.();
          slots[index] = { deps, cleanup: effect() };
        });
      }
    },
  };
  const jsx = (type, props) => ({ type, props });
  const component = loadModule(file, {
    react: hooks,
    'react/jsx-runtime': { jsx, jsxs: jsx, Fragment: 'Fragment' },
    '@tarojs/components': { View: 'View', Text: 'Text', Input: 'Input', ScrollView: 'ScrollView', Picker: 'Picker', Image: 'Image' },
    '@tarojs/taro': { useDidShow: (fn) => { onShow = fn; }, showToast() {} },
    '@/components/Card': { default: 'Card' },
    '@/components/Tag': { default: 'Tag' },
    '@/components/Button': { default: 'Button' },
    '@/components/BottomNav': { default: 'BottomNav' },
    '@/utils/format': { formatDate: (value) => { const date = new Date(value); return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`; } },
    '@/store/auth': { __esModule: true, default: () => ({ user: { role: 'TEACHER' }, profile: null }) },
    '@/utils/tabbar': { notifyTabBarActive() {} },
    '@/services/api': api,
    './index.module.scss': {},
    ...overrides,
  }).default;
  return {
    render: () => {
      cursor = 0;
      const tree = component();
      while (effects.length) effects.shift()();
      return tree;
    },
    show: () => onShow(),
  };
}

function nodes(tree) {
  if (Array.isArray(tree)) return tree.flatMap(nodes);
  if (!tree || typeof tree !== 'object') return [];
  return [tree, ...nodes(tree.props?.children)];
}

test('新搜索先返回时，旧搜索结果不会覆盖新结果', async () => {
  const oldSearch = deferred();
  const newSearch = deferred();
  const requests = [];
  const h = pageHarness('src/pages/teacher-students/index.tsx', {
    getAllTeacherStudents: (keyword, isCurrent) => {
      requests.push({ keyword, isCurrent });
      return requests.length === 1 ? oldSearch.promise : newSearch.promise;
    },
  });
  h.render();
  const input = nodes(h.render()).find((node) => node.type === 'Input');
  input.props.onInput({ detail: { value: '新搜索' } });
  nodes(h.render()).find((node) => node.type === 'Input').props.onConfirm();
  assert.equal(requests[0].isCurrent(), false);
  assert.equal(requests[1].keyword, '新搜索');
  newSearch.resolve([{ id: 'new', name: '新学员' }]);
  await flush();
  oldSearch.resolve([{ id: 'old', name: '旧学员' }]);
  await flush();
  const output = JSON.stringify(h.render());
  assert.ok(output.includes('新学员'));
  assert.ok(!output.includes('旧学员'));
});

test('体测页再次显示时重新加载记录，并忽略较早的响应', async () => {
  const oldRecords = deferred();
  const newRecords = deferred();
  let count = 0;
  const h = pageHarness('src/pages/physical/index.tsx', {
    getAllTeacherStudents: async () => [{ id: '1', name: '学员' }],
    getAllTeacherPhysicalTests: () => ++count === 1 ? oldRecords.promise : newRecords.promise,
  });
  h.render();
  h.show();
  h.show();
  newRecords.resolve([{ id: 'new', studentId: '1', testDate: '2026-10-28', totalScore: 88 }]);
  await flush();
  oldRecords.resolve([]);
  await flush();
  assert.equal(count, 2);
  const output = JSON.stringify(h.render());
  assert.ok(output.includes('2026-10-28'));
  assert.ok(output.includes('88'));
});


test('教师切月同步选中日期，课程加载覆盖日历全部42天', async () => {
  const calls = [];
  const h = pageHarness('src/pages/home/index.tsx', {
    getSchedules: async (params) => { calls.push(params); return []; },
  });
  h.render();
  await flush();
  nodes(h.render()).find((n) => n.props?.children === '›' && n.props.onClick).props.onClick();
  h.render();
  await flush();
  const range = calls.at(-1);
  const start = new Date(range.startDate + 'T00:00:00');
  const end = new Date(range.endDate + 'T00:00:00');
  assert.equal(start.getDay(), 0);
  assert.equal(Math.round((end - start) / 86400000), 41);
  const nextMonth = new Date();
  nextMonth.setDate(1);
  nextMonth.setMonth(nextMonth.getMonth() + 1);
  const expected = `${nextMonth.getFullYear()}-${String(nextMonth.getMonth() + 1).padStart(2, '0')}-01`;
  assert.ok(JSON.stringify(h.render()).includes(expected));
});

for (const parent of [false, true]) {
  test(`${parent ? '家长' : '教师'}课程失败显示重试，成功后恢复课程列表`, async () => {
    let attempts = 0;
    const load = async () => {
      if (++attempts === 1) throw new Error('offline');
      const date = new Date();
      return [{ id: 'lesson', lessonDate: `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`, courseName: '恢复的课程' }];
    };
    const h = pageHarness(`src/pages/${parent ? 'parent-home' : 'home'}/index.tsx`, {
      getSchedules: load, getLessons: load,
      listParentChildren: async () => [{ id: 'child', name: '孩子' }],
    });
    h.render();
    await flush();
    h.render();
    await flush();
    const tree = h.render();
    assert.ok(JSON.stringify(tree).includes('课程加载失败，点击重试'));
    const retry = nodes(tree).find((n) => n.type?.default === 'Card' && JSON.stringify(n).includes('课程加载失败'));
    await retry.props.onClick();
    assert.ok(JSON.stringify(h.render()).includes('恢复的课程'));
    assert.equal(attempts, 2);
  });
}

test('孩子资料请求失败不显示无孩子提示，重试后显示已有孩子', async () => {
  let attempts = 0;
  const h = pageHarness('src/pages/parent-home/index.tsx', {
    listParentChildren: async () => {
      if (++attempts === 1) throw new Error('offline');
      return [{ id: 'child', name: '已有孩子' }];
    },
    getLessons: async () => [],
  });
  h.render();
  await flush();
  const tree = h.render();
  assert.ok(!JSON.stringify(tree).includes('还没有孩子资料'));
  const retry = nodes(tree).find((n) => n.type?.default === 'Card' && JSON.stringify(n).includes('孩子资料加载失败'));
  assert.ok(retry);
  await retry.props.onClick();
  h.render();
  await flush();
  assert.ok(JSON.stringify(h.render()).includes('已有孩子'));
});

for (const role of ['TEACHER', 'PARENT']) {
  test(`${role}密码登录需同意协议，成功后跳转对应首页`, async () => {
    const calls = [];
    const redirects = [];
    let state = { isLoggedIn: false, isValidated: true, user: null };
    const h = pageHarness('src/pages/login/index.tsx', {
      login: async (params) => {
        calls.push(params);
        return { token: 'test-token', refreshToken: 'refresh', user: { role, profileComplete: true }, profile: null };
      },
    }, {
      '@/store/auth': { __esModule: true, default: () => ({ ...state, login: (token, user) => { state = { isLoggedIn: true, isValidated: true, user }; } }) },
      '@/components/AgreementConsent': { __esModule: true, default: 'AgreementConsent' },
      '@/assets/tabbar/physical-selected.png': 'logo.png',
      '@tarojs/taro': { __esModule: true, default: { showToast() {}, reLaunch: ({ url }) => redirects.push(url) } },
    });
    let tree = h.render();
    assert.equal(nodes(tree).find((n) => n.type?.default === 'Button').props.disabled, true);
    const inputs = nodes(tree).filter((n) => n.type === 'Input');
    inputs[0].props.onInput({ detail: { value: 'test-account' } });
    inputs[1].props.onInput({ detail: { value: 'test-password' } });
    nodes(tree).find((n) => n.type === 'AgreementConsent').props.onChange(true);
    tree = h.render();
    const submit = nodes(tree).find((n) => n.type?.default === 'Button');
    assert.equal(submit.props.disabled, false);
    await submit.props.onClick();
    h.render();
    assert.deepEqual(JSON.parse(JSON.stringify(calls)), [{ username: 'test-account', password: 'test-password' }]);
    assert.deepEqual(redirects, [role === 'PARENT' ? '/pages/parent-home/index' : '/pages/home/index']);
  });
}

for (const role of ['PARENT', 'TEACHER']) {
  test(`${role}周课表显示七天并使用对应权限接口，切换周后忽略旧结果`, async () => {
    const first = deferred();
    const second = deferred();
    const requests = [];
    const fetchWeek = (params) => { requests.push(params); return requests.length === 1 ? first.promise : second.promise; };
    const h = pageHarness('src/pages/schedule/index.tsx', {
      getPublicTimetable: role === 'PARENT' ? fetchWeek : () => { throw new Error('教师不应访问家长接口'); },
      getSchedules: role === 'TEACHER' ? fetchWeek : () => { throw new Error('家长不应访问教师接口'); },
    }, {
      '../parent-course/index.module.scss': {},
      '@/store/auth': { __esModule: true, default: () => ({ user: { role } }) },
    });
    h.render();
    nodes(h.render()).find(n => n.props?.children === '下周 ›').props.onClick();
    h.render();
    const day = 86400000;
    assert.equal((new Date(requests[0].endDate) - new Date(requests[0].startDate)) / day, 6);
    assert.equal((new Date(requests[1].startDate) - new Date(requests[0].startDate)) / day, 7);
    second.resolve([{ id: 'next', courseName: '下周课程', lessonDate: requests[1].startDate, startTime: '10:00', endTime: '11:00', status: 'SCHEDULED' }]);
    await flush();
    first.resolve([{ id: 'old', courseName: '过期课程', lessonDate: requests[0].startDate, startTime: '10:00', endTime: '11:00', status: 'SCHEDULED' }]);
    await flush();
    const output = JSON.stringify(h.render());
    assert.ok(output.includes('下周课程'));
    assert.ok(!output.includes('过期课程'));
    assert.ok(!output.includes('确认排课'));
  });
}

for (const page of ['parent-home', 'parent-course']) {
  test(`${page}返回页面重新获取孩子名单，忽略先前请求`, async () => {
    const first = deferred();
    let calls = 0;
    const h = pageHarness(`src/pages/${page}/index.tsx`, {
      [page === 'parent-home' ? 'listParentChildren' : 'getParentStudents']: () => ++calls === 1 ? first.promise : Promise.resolve([{ id: 'new', name: '最新孩子' }]),
      getLessons: async () => [],
    }, { '@/store/auth': { __esModule: true, default: () => ({ user: { id: 'parent', role: 'PARENT' }, profile: null }) } });
    h.render();
    h.show();
    await flush();
    first.resolve([{ id: 'old', name: '过期孩子' }]);
    await flush();
    const tree = JSON.stringify(h.render());
    assert.equal(calls, 2);
    assert.ok(tree.includes('最新孩子'));
    assert.ok(!tree.includes('过期孩子'));
  });
}

test('课程详情按ID获取，返回页面重新获取课程', async () => {
  let calls = 0;
  let show;
  const h = pageHarness('src/pages/course-detail/index.tsx', {
    getLessonDetail: async (id) => { assert.equal(id, '123'); calls++; return { id, status: 'PENDING', courseName: '新课程' }; },
    getLessonAttendance: async () => [],
  }, {
    '@tarojs/taro': { useDidShow: fn => { show = fn; }, getCurrentInstance: () => ({ router: { params: { id: '123' } } }) },
    '@/utils/format': { formatDate: value => value, formatTime: value => value },
  });
  h.render();
  await flush();
  assert.ok(JSON.stringify(h.render()).includes('新课程'));
  show();
  await flush();
  assert.equal(calls, 2);
});

test('请求携带客户端版本，区分新版主动选择批次和旧版默认批次', async () => {
  const h = requestHarness(() => ({ code: 0, data: [] }));
  await h.request('/business/example');
  assert.equal(h.calls[0].header['X-Client-Version'], '20261102');
});

test('家长课程页请求失败显示重试，不误报没有孩子', async () => {
  const h = pageHarness('src/pages/parent-course/index.tsx', {
    getParentStudents: async () => { throw new Error('offline'); },
  });
  h.render();
  await flush();
  const tree = JSON.stringify(h.render());
  assert.ok(tree.includes('课程加载失败，点击重试'));
  assert.ok(!tree.includes('暂无孩子资料'));
});

test('小程序回到前台重新同步身份，角色不变时保留会话版本', async () => {
  const h = appHarness();
  const version = h.store.getState().sessionVersion;
  h.show();
  assert.equal(h.requests, 2);
  h.pending.resolve({ user: { id: 'old-user', role: 'TEACHER', name: '最新姓名' }, profile: null });
  await flush();
  assert.equal(h.store.getState().user.name, '最新姓名');
  assert.equal(h.store.getState().sessionVersion, version);
});

for (const role of ['TEACHER', 'PARENT']) {
  test(`${role}已登录但资料为空时个人页正常显示`, () => {
    const h = pageHarness('src/pages/mine/index.tsx', {}, {
      '@/store/auth': { __esModule: true, default: () => ({ user: { id: '1', role, name: '测试用户' }, profile: null, isLoggedIn: true }) },
      '@/utils/profile': { mergeProfile: (_, value) => value },
      '@/assets/tabbar/mine-selected.png': 'mine.png',
      '@/assets/tabbar/schedule-selected.png': 'schedule.png',
      '@/assets/tabbar/physical-selected.png': 'physical.png',
    });
    assert.doesNotThrow(() => h.render());
  });
}
