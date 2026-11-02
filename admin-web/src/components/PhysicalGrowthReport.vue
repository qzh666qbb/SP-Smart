<template>
  <article ref="reportRoot" class="growth-report">
    <header class="report-topbar">
      <div>
        <span class="eyebrow">儿童体质健康档案</span>
        <h1>个人体质成长报告</h1>
      </div>
      <div class="report-date">
        <span>报告日期</span>
        <strong>{{ report.basic_info?.report_date || '--' }}</strong>
      </div>
    </header>

    <section class="overview-grid">
      <div class="panel student-card">
        <div class="avatar">{{ avatarText }}</div>
        <div class="student-main">
          <div class="student-name">{{ report.basic_info?.name || '--' }}</div>
          <div class="info-grid">
            <span><small>性别</small>{{ report.basic_info?.gender || '--' }}</span>
            <span><small>班级</small>{{ report.basic_info?.class || '--' }}</span>
            <span><small>年龄</small>{{ report.basic_info?.age || '--' }}</span>
            <span><small>出生日期</small>{{ report.basic_info?.birthday || '--' }}</span>
          </div>
        </div>
      </div>

      <div class="panel score-card">
        <div class="score-ring">
          <strong>{{ formatScore(report.summary_score?.total_score) }}</strong>
          <span>综合得分</span>
        </div>
        <div class="score-meta">
          <div class="score-heading">
            <span :class="['level-pill', levelClass(report.summary_score?.level)]">{{ report.summary_score?.level || '--' }}</span>
            <span class="eval-type">{{ report.summary_score?.eval_type || '--' }}</span>
          </div>
          <p>{{ report.summary_score?.desc || '--' }}</p>
          <div class="ranking-row">
            <span><small>班级排名</small><b>{{ report.summary_score?.rank_class || '--' }}</b></span>
            <span><small>年级排名</small><b>{{ report.summary_score?.rank_grade || '--' }}</b></span>
          </div>
        </div>
      </div>
    </section>

    <section class="dimension-strip">
      <div v-for="item in dimensions" :key="item.name" class="dimension-card">
        <span class="dimension-name">{{ item.name }}</span>
        <strong>{{ formatScore(item.score) }}</strong>
        <span :class="['mini-pill', levelClass(item.level)]">{{ item.level }}</span>
      </div>
    </section>

    <section class="analysis-grid">
      <div class="panel chart-panel">
        <SectionTitle title="七维能力雷达图" subtitle="各项能力均按 100 分制展示" />
        <div ref="radarRef" class="radar-chart" aria-label="七维能力雷达图" />
      </div>

      <div class="panel result-panel">
        <SectionTitle title="各项指标测试结果" subtitle="原始测量值与标准化得分" />
        <div class="result-table">
          <div class="result-head"><span>指标</span><span>测试值</span><span>得分</span><span>评价</span></div>
          <div v-for="item in testItems" :key="item.item_name" class="result-row">
            <div>
              <b>{{ item.item_name }}</b>
              <div class="progress-track"><i :style="{ width: progressWidth(item.score) }" /></div>
            </div>
            <span>{{ item.value }}</span>
            <strong>{{ formatScore(item.score) }}</strong>
            <span :class="['mini-pill', levelClass(item.level)]">{{ item.level }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="trend-grid">
      <div class="panel trend-card">
        <SectionTitle title="成长趋势分析" subtitle="历次综合得分变化" />
        <div class="trend-summary">
          <div class="baseline-score"><span>成长基线</span><strong>{{ formatScore(report.growth_trend?.base_score) }}</strong><small>分</small></div>
          <div v-if="scorePoints.length > 1" class="trend-points">
            <div v-for="point in scorePoints" :key="point.date" class="trend-point">
              <i :style="{ height: progressWidth(point.score) }" />
              <b>{{ formatScore(point.score) }}</b>
              <small>{{ shortDate(point.date) }}</small>
            </div>
          </div>
          <div v-else class="waiting-state">
            <span class="waiting-icon">↗</span>
            <div><b>等待第二次体测</b><small>完成后自动生成成长曲线</small></div>
          </div>
        </div>
        <p class="trend-tip">{{ report.growth_trend?.tip }}</p>
      </div>

      <div class="panel baseline-card">
        <SectionTitle title="基线数据" subtitle="首次体测原始记录" />
        <div class="baseline-list">
          <div v-for="item in baseData" :key="item.item_name">
            <span>{{ item.item_name }}</span><strong>{{ item.base_value }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="panel ai-card">
      <div class="ai-heading">
        <span class="ai-badge">AI</span>
        <div><h2>AI 成长画像</h2><p>基于本次体测数据生成的综合解读</p></div>
      </div>
      <div class="profile-grid">
        <div class="profile-summary"><h3>AI 总结</h3><p>{{ report.ai_profile?.summary || '--' }}</p></div>
        <div class="profile-list advantage"><h3>优势能力</h3><span v-for="text in advantageList" :key="text">✓ {{ text }}</span><em v-if="!advantageList.length">暂无显著优势项</em></div>
        <div class="profile-list focus"><h3>重点关注</h3><span v-for="text in focusList" :key="text">↗ {{ text }}</span><em v-if="!focusList.length">各项能力均衡</em></div>
      </div>
    </section>

    <section class="suggestion-grid">
      <div v-for="item in suggestionCards" :key="item.key" class="suggestion-card">
        <span class="suggestion-icon">{{ item.icon }}</span>
        <div><h3>{{ item.title }}</h3><p>{{ report.suggestions?.[item.key] || '--' }}</p></div>
      </div>
    </section>

    <footer class="report-footer">
      <span>ⓘ</span>{{ report.warning }}
    </footer>
  </article>
</template>

<script setup>
import { computed, defineComponent, h, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([RadarChart, TooltipComponent, CanvasRenderer])

const props = defineProps({
  report: { type: Object, required: true }
})

const reportRoot = ref(null)
defineExpose({ getElement: () => reportRoot.value })

const SectionTitle = defineComponent({
  props: { title: String, subtitle: String },
  setup(p) {
    return () => h('div', { class: 'section-heading' }, [
      h('h2', p.title), h('p', p.subtitle)
    ])
  }
})

const radarRef = ref(null)
let chart = null
let resizeObserver = null

const dimensions = computed(() => Array.isArray(props.report.seven_dimension) ? props.report.seven_dimension : [])
const testItems = computed(() => Array.isArray(props.report.test_items) ? props.report.test_items : [])
const baseData = computed(() => Array.isArray(props.report.growth_trend?.base_data) ? props.report.growth_trend.base_data : [])
const scorePoints = computed(() => Array.isArray(props.report.growth_trend?.score_points) ? props.report.growth_trend.score_points : [])
const advantageList = computed(() => Array.isArray(props.report.ai_profile?.advantage_list) ? props.report.ai_profile.advantage_list : [])
const focusList = computed(() => Array.isArray(props.report.ai_profile?.focus_list) ? props.report.ai_profile.focus_list : [])
const avatarText = computed(() => String(props.report.basic_info?.name || '学').slice(0, 1))

const suggestionCards = [
  { key: 'sport', title: '运动建议', icon: '⚽' },
  { key: 'nutrition', title: '营养建议', icon: '🥛' },
  { key: 'sleep', title: '作息建议', icon: '☾' },
  { key: 'mental', title: '心理建议', icon: '♡' }
]

const renderRadar = async () => {
  await nextTick()
  if (!radarRef.value || !dimensions.value.length) return
  if (!chart) chart = echarts.init(radarRef.value, null, { renderer: 'canvas' })
  chart.setOption({
    animation: false,
    tooltip: { trigger: 'item', formatter: params => params.value.map((v, i) => `${dimensions.value[i]?.name || ''}：${v}分`).join('<br>') },
    radar: {
      center: ['50%', '54%'], radius: '52%', splitNumber: 4, nameGap: 6,
      indicator: dimensions.value.map(item => ({ name: item.name, max: 100 })),
      axisName: { color: '#3d5b52', fontSize: 10, lineHeight: 12, fontWeight: 600 },
      splitArea: { areaStyle: { color: ['#fbfefd', '#f3faf7', '#eaf6f0', '#e2f2ea'] } },
      splitLine: { lineStyle: { color: '#cfe4d9' } },
      axisLine: { lineStyle: { color: '#d3e6dc' } }
    },
    series: [{
      type: 'radar', symbol: 'circle', symbolSize: 5,
      data: [{ value: dimensions.value.map(item => Number(item.score) || 0) }],
      lineStyle: { color: '#1f9d68', width: 2 },
      itemStyle: { color: '#137f55', borderColor: '#fff', borderWidth: 1 },
      areaStyle: { color: 'rgba(31, 157, 104, .28)' }
    }]
  }, true)
}

watch(dimensions, renderRadar, { deep: true })
onMounted(() => {
  renderRadar()
  if (radarRef.value && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => chart?.resize())
    resizeObserver.observe(radarRef.value)
  }
})
onBeforeUnmount(() => { resizeObserver?.disconnect(); chart?.dispose(); chart = null })

const formatScore = value => Number.isFinite(Number(value)) ? Number(value).toFixed(Number(value) % 1 ? 1 : 0) : '--'
const progressWidth = value => `${Math.max(0, Math.min(100, Number(value) || 0))}%`
const shortDate = value => String(value || '').slice(5).replace('-', '/') || '--'
const levelClass = level => {
  if (['优秀', '正常'].includes(level)) return 'excellent'
  if (level === '良好') return 'good'
  if (level === '合格') return 'pass'
  return 'attention'
}
</script>

<style scoped>
.growth-report { box-sizing: border-box; width: 794px; min-height: 1123px; margin: 0 auto; padding: 28px 30px 24px; color: #243b34; background: #f7fbf9; font-family: "PingFang SC", "Microsoft YaHei", sans-serif; }
.report-topbar { display: flex; align-items: flex-end; justify-content: space-between; padding: 4px 2px 20px; border-bottom: 2px solid #168a5d; }
.eyebrow { color: #168a5d; font-size: 10px; font-weight: 700; letter-spacing: 2px; }
h1 { margin: 4px 0 0; color: #183d31; font-size: 25px; letter-spacing: 1px; }
.report-date { display: flex; flex-direction: column; align-items: flex-end; gap: 3px; color: #789087; font-size: 10px; }
.report-date strong { color: #34564b; font-size: 13px; }
.panel { box-sizing: border-box; border: 1px solid #dcebe4; border-radius: 10px; background: #fff; box-shadow: 0 3px 12px rgba(28, 88, 66, .045); }
.overview-grid { display: grid; grid-template-columns: 1fr 1.12fr; gap: 12px; margin-top: 14px; }
.student-card { display: flex; align-items: center; min-height: 126px; padding: 18px; }
.avatar { display: grid; flex: 0 0 66px; height: 66px; place-items: center; border: 5px solid #e1f3ea; border-radius: 50%; color: #fff; background: linear-gradient(145deg, #37b883, #157c55); font-size: 27px; font-weight: 700; }
.student-main { min-width: 0; margin-left: 16px; }
.student-name { margin-bottom: 10px; color: #173e31; font-size: 21px; font-weight: 700; }
.info-grid { display: grid; grid-template-columns: 1fr 1.45fr; gap: 8px 14px; font-size: 11px; white-space: nowrap; }
.info-grid span { overflow: hidden; text-overflow: ellipsis; }
.info-grid small, .ranking-row small { display: block; margin-bottom: 2px; color: #91a39d; font-size: 9px; }
.score-card { display: flex; align-items: center; min-height: 126px; padding: 14px 18px; background: linear-gradient(135deg, #fff 30%, #eff9f4); }
.score-ring { display: flex; flex: 0 0 90px; height: 90px; align-items: center; justify-content: center; flex-direction: column; border: 7px solid #35ae7a; border-radius: 50%; background: #fff; box-shadow: inset 0 0 0 3px #dff2e8; }
.score-ring strong { color: #137c54; font-size: 30px; line-height: 1; }
.score-ring span { margin-top: 5px; color: #789087; font-size: 9px; }
.score-meta { flex: 1; margin-left: 17px; }
.score-heading { display: flex; align-items: center; gap: 7px; }
.level-pill, .mini-pill { display: inline-block; border-radius: 99px; font-weight: 700; white-space: nowrap; }
.level-pill { padding: 4px 11px; font-size: 12px; }
.mini-pill { padding: 2px 7px; font-size: 9px; }
.excellent { color: #087d50; background: #dff5e9; }.good { color: #237696; background: #e3f3fa; }.pass { color: #a26a05; background: #fff1ce; }.attention { color: #b44949; background: #fde8e7; }
.eval-type { color: #799088; font-size: 10px; }
.score-meta p { margin: 7px 0 10px; color: #678078; font-size: 10px; }
.ranking-row { display: flex; gap: 32px; }.ranking-row b { color: #193e32; font-size: 14px; }
.dimension-strip { display: grid; grid-template-columns: repeat(7, 1fr); gap: 7px; margin: 12px 0; }
.dimension-card { min-width: 0; padding: 10px 3px 9px; text-align: center; border: 1px solid #ddece5; border-radius: 8px; background: #fff; }
.dimension-card strong { display: block; margin: 4px 0; color: #16845a; font-size: 20px; }.dimension-name { color: #526c63; font-size: 10px; font-weight: 600; white-space: nowrap; }
.analysis-grid { display: grid; grid-template-columns: .88fr 1.42fr; gap: 12px; }.chart-panel, .result-panel, .trend-card, .baseline-card { padding: 15px; }
.section-heading h2 { margin: 0; color: #24483d; font-size: 14px; }.section-heading h2::before { display: inline-block; width: 3px; height: 13px; margin-right: 7px; vertical-align: -2px; border-radius: 2px; background: #20a36d; content: ''; }.section-heading p { margin: 3px 0 0 10px; color: #9aaba5; font-size: 9px; }
.radar-chart { width: 100%; height: 245px; overflow: hidden; }
.radar-chart :deep(canvas), .radar-chart img { max-width: 100% !important; max-height: 100% !important; }
.result-table { margin-top: 10px; }.result-head, .result-row { display: grid; grid-template-columns: 1.55fr .85fr .45fr .65fr; align-items: center; gap: 6px; }.result-head { padding: 5px 7px; color: #8a9c96; background: #f4f8f6; font-size: 9px; }.result-row { min-height: 27px; padding: 3px 7px; border-bottom: 1px solid #edf3f0; font-size: 9px; }.result-row b { color: #3b5850; font-weight: 600; }.result-row strong { color: #147d55; font-size: 11px; }
.progress-track { width: 74px; height: 3px; margin-top: 3px; overflow: hidden; border-radius: 3px; background: #e7efeb; }.progress-track i { display: block; height: 100%; border-radius: 3px; background: linear-gradient(90deg, #6dc99e, #168a5d); }
.trend-grid { display: grid; grid-template-columns: 1.45fr .85fr; gap: 12px; margin-top: 12px; }.trend-summary { display: flex; align-items: center; min-height: 86px; margin-top: 8px; padding: 8px 12px; border-radius: 8px; background: #f2f9f5; }.baseline-score { flex: 0 0 90px; text-align: center; border-right: 1px solid #d8e9e0; }.baseline-score span { display: block; color: #83968f; font-size: 9px; }.baseline-score strong { color: #14845a; font-size: 27px; }.baseline-score small { color: #749087; font-size: 9px; }
.waiting-state { display: flex; align-items: center; gap: 10px; margin-left: 18px; }.waiting-icon { display: grid; width: 28px; height: 28px; place-items: center; border-radius: 50%; color: #14845a; background: #dcefe5; }.waiting-state b,.waiting-state small { display: block; }.waiting-state b { color: #3a5d51; font-size: 11px; }.waiting-state small { margin-top: 3px; color: #91a39d; font-size: 9px; }
.trend-points { display: flex; flex: 1; height: 70px; align-items: flex-end; justify-content: space-around; margin-left: 14px; }.trend-point { display: flex; width: 28px; height: 64px; align-items: center; justify-content: flex-end; flex-direction: column; }.trend-point i { width: 9px; max-height: 42px; min-height: 2px; border-radius: 5px 5px 1px 1px; background: #3ab17d; }.trend-point b { margin-top: 2px; color: #36705c; font-size: 8px; }.trend-point small { color: #8da098; font-size: 7px; }
.trend-tip { margin: 7px 2px 0; color: #7b9189; font-size: 9px; line-height: 1.55; }.baseline-list { display: grid; grid-template-columns: 1fr 1fr; gap: 0 12px; margin-top: 8px; }.baseline-list div { display: flex; justify-content: space-between; padding: 6px 2px; border-bottom: 1px dashed #e0ebe6; font-size: 9px; }.baseline-list span { color: #748980; }.baseline-list strong { color: #35574c; }
.ai-card { margin-top: 12px; padding: 15px; background: linear-gradient(135deg, #fff 15%, #f0f9f5); }.ai-heading { display: flex; align-items: center; gap: 9px; }.ai-badge { display: grid; width: 31px; height: 31px; place-items: center; border-radius: 9px; color: #fff; background: linear-gradient(145deg, #3abb85, #117852); font-size: 12px; font-weight: 800; }.ai-heading h2 { margin: 0; color: #20473a; font-size: 14px; }.ai-heading p { margin: 2px 0 0; color: #91a39d; font-size: 9px; }.profile-grid { display: grid; grid-template-columns: 1.8fr 1fr 1fr; gap: 10px; margin-top: 11px; }.profile-grid > div { padding: 10px 12px; border-radius: 7px; background: rgba(255,255,255,.85); }.profile-grid h3 { margin: 0 0 6px; color: #45635a; font-size: 10px; }.profile-summary p { margin: 0; color: #5f776f; font-size: 9px; line-height: 1.7; }.profile-list span,.profile-list em { display: block; margin: 4px 0; font-size: 9px; font-style: normal; }.advantage span { color: #25835e; }.focus span { color: #a66c28; }.profile-list em { color: #8da098; }
.suggestion-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-top: 12px; }.suggestion-card { display: flex; gap: 8px; min-height: 75px; padding: 11px 9px; border: 1px solid #dcebe4; border-radius: 8px; background: #fff; }.suggestion-icon { font-size: 18px; }.suggestion-card h3 { margin: 0 0 5px; color: #38594e; font-size: 10px; }.suggestion-card p { margin: 0; color: #7b8f88; font-size: 8px; line-height: 1.55; }
.report-footer { margin-top: 13px; padding-top: 9px; text-align: center; border-top: 1px solid #dbe8e2; color: #8b9d96; font-size: 8px; }.report-footer span { margin-right: 5px; color: #258b65; }
@media (max-width: 840px) { .growth-report { width: 100%; min-width: 720px; } }
@media print { .growth-report { width: 210mm; min-height: 297mm; margin: 0; box-shadow: none; print-color-adjust: exact; -webkit-print-color-adjust: exact; } }
:global(.html2pdf__container .panel),
:global(.html2pdf__container .dimension-strip),
:global(.html2pdf__container .suggestion-grid),
:global(.html2pdf__container .report-footer) { break-inside: avoid; page-break-inside: avoid; }

.growth-report.pdf-single-page { min-height: 0; padding: 18px 26px 16px; }
.pdf-single-page .report-topbar { padding-bottom: 12px; }
.pdf-single-page .overview-grid { margin-top: 10px; }
.pdf-single-page .student-card, .pdf-single-page .score-card { min-height: 108px; padding-top: 11px; padding-bottom: 11px; }
.pdf-single-page .avatar { flex-basis: 58px; height: 58px; font-size: 24px; }
.pdf-single-page .score-ring { flex-basis: 78px; height: 78px; }
.pdf-single-page .score-ring strong { font-size: 27px; }
.pdf-single-page .dimension-strip { margin: 8px 0; }
.pdf-single-page .dimension-card { padding: 7px 3px 6px; }
.pdf-single-page .dimension-card strong { margin: 2px 0; font-size: 18px; }
.pdf-single-page .chart-panel, .pdf-single-page .result-panel,
.pdf-single-page .trend-card, .pdf-single-page .baseline-card { padding: 11px 13px; }
.pdf-single-page .radar-chart { height: 205px; }
.pdf-single-page .result-table { margin-top: 6px; }
.pdf-single-page .result-row { min-height: 23px; padding-top: 2px; padding-bottom: 2px; }
.pdf-single-page .trend-grid, .pdf-single-page .ai-card,
.pdf-single-page .suggestion-grid { margin-top: 8px; }
.pdf-single-page .trend-summary { min-height: 68px; margin-top: 5px; padding-top: 5px; padding-bottom: 5px; }
.pdf-single-page .ai-card { padding: 11px 13px; }
.pdf-single-page .profile-grid { margin-top: 7px; }
.pdf-single-page .profile-grid > div { padding: 7px 9px; }
.pdf-single-page .suggestion-card { min-height: 58px; padding: 7px 8px; }
.pdf-single-page .suggestion-card p { line-height: 1.4; }
.pdf-single-page .report-footer { margin-top: 8px; padding-top: 6px; }
</style>
