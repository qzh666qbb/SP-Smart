const downloadBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
}

const chooseDestination = async (filename) => {
  if (typeof window.showSaveFilePicker !== 'function') return null
  try {
    return await window.showSaveFilePicker({
      suggestedName: filename,
      types: [{
        description: 'PDF 文档',
        accept: { 'application/pdf': ['.pdf'] }
      }]
    })
  } catch (error) {
    if (error?.name === 'AbortError') return 'cancelled'
    throw error
  }
}

const buildExportSource = (element) => {
  const source = element.cloneNode(true)
  source.classList.add('pdf-single-page')
  const originalCharts = element.querySelectorAll('.radar-chart')
  const clonedCharts = source.querySelectorAll('.radar-chart')

  originalCharts.forEach((chart, index) => {
    const canvas = chart.querySelector('canvas')
    const clonedChart = clonedCharts[index]
    if (!canvas || !clonedChart) return

    const image = document.createElement('img')
    image.src = canvas.toDataURL('image/png')
    image.alt = chart.getAttribute('aria-label') || '雷达图'
    image.style.display = 'block'
    image.style.width = '100%'
    image.style.height = '100%'
    image.style.objectFit = 'contain'
    clonedChart.replaceChildren(image)
  })

  return source
}

const renderReportCanvas = async (source, html2canvas, width) => {
  const staging = document.createElement('div')
  staging.style.position = 'fixed'
  staging.style.left = '-100000px'
  staging.style.top = '0'
  staging.style.width = `${width}px`
  staging.style.background = '#f7fbf9'
  staging.appendChild(source)
  document.body.appendChild(staging)

  try {
    if (document.fonts?.ready) await document.fonts.ready
    return await html2canvas(source, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#f7fbf9',
      logging: false,
      scrollX: 0,
      scrollY: 0,
      windowWidth: width
    })
  } finally {
    staging.remove()
  }
}

export const exportElementToPdf = async (element, filename) => {
  // 选择保存位置必须紧跟用户点击触发，否则浏览器会拦截文件选择器。
  const destination = await chooseDestination(filename)
  if (destination === 'cancelled') return 'cancelled'

  // ECharts 使用高分屏 Canvas。直接复制 Canvas 会让 html2canvas 把像素尺寸当成布局尺寸，
  // 导致雷达图在 PDF 中放大溢出。导出副本中改用等尺寸 PNG，保留清晰度并锁定布局。
  const exportSource = buildExportSource(element)
  const [{ default: html2canvas }, { jsPDF }] = await Promise.all([
    import('html2canvas'),
    import('jspdf')
  ])
  const sourceWidth = Math.round(element.getBoundingClientRect().width) || 794
  const canvas = await renderReportCanvas(exportSource, html2canvas, sourceWidth)

  const pdf = new jsPDF({ unit: 'mm', format: 'a4', orientation: 'portrait', compress: true })
  const pageWidth = pdf.internal.pageSize.getWidth()
  const pageHeight = pdf.internal.pageSize.getHeight()
  const margin = 4
  const availableWidth = pageWidth - margin * 2
  const availableHeight = pageHeight - margin * 2
  const scale = Math.min(availableWidth / canvas.width, availableHeight / canvas.height)
  const renderWidth = canvas.width * scale
  const renderHeight = canvas.height * scale
  const offsetX = (pageWidth - renderWidth) / 2
  const offsetY = (pageHeight - renderHeight) / 2

  pdf.setFillColor(247, 251, 249)
  pdf.rect(0, 0, pageWidth, pageHeight, 'F')
  pdf.addImage(canvas.toDataURL('image/jpeg', 0.98), 'JPEG', offsetX, offsetY,
    renderWidth, renderHeight, undefined, 'FAST')
  const blob = pdf.output('blob')
  if (destination) {
    const writable = await destination.createWritable()
    await writable.write(blob)
    await writable.close()
    return 'saved'
  }

  downloadBlob(blob, filename)
  return 'downloaded'
}
