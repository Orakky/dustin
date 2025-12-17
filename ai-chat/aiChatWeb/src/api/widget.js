// Make the chat-ai app embeddable as a floating widget
((w, d) => {
  const script = d.currentScript
  if (!script) return

  const dataset = script.dataset || {}
  const scriptUrl = new URL(script.src, w.location.href)

  const numberPattern = /^\d+(?:\.\d+)?$/
  const pixelPattern = /^\d+(?:\.\d+)?px$/i

  const toSize = (value, fallback) => {
    if (value === undefined || value === null) return fallback
    const str = String(value).trim()
    if (!str) return fallback
    if (numberPattern.test(str)) return `${str}px`
    return str
  }

  const resolvedWidgetUrl = (() => {
    const target =
      dataset.widgetUrl ||
      dataset.appUrl ||
      dataset.url ||
      dataset.entryPath ||
      '/'
    return new URL(target, scriptUrl)
  })()

  // Append common query parameters for the widget surface
  resolvedWidgetUrl.searchParams.set('widget', dataset.widgetFlag || '1')
  if (!resolvedWidgetUrl.searchParams.has('from')) {
    resolvedWidgetUrl.searchParams.set('from', w.location.hostname || '')
  }

  const queryMappings = {
    themeColor: 'themeColor',
    title: 'title',
    welcome: 'welcome',
    logo: 'logo',
    id: 'id',
    hostId: 'host',
    loginToken: 'token',
    launcherPosition: 'launcherPosition',
    launcherOffset: 'launcherOffset',
    launcherOffsetX: 'launcherOffsetX',
    launcherOffsetY: 'launcherOffsetY',
    launcherHorizontal: 'launcherHorizontal',
    launcherVertical: 'launcherVertical',
    launcherAlign: 'launcherAlign',
  }

  Object.entries(queryMappings).forEach(([key, param]) => {
    const value = dataset[key]
    if (typeof value === 'string' && value.trim().length) {
      resolvedWidgetUrl.searchParams.set(param, value.trim())
    }
  })

  const expandedSize = {
    width: toSize(
      dataset.expandedWidth ?? dataset.openWidth ?? dataset.width,
      '1100px',
    ),
    height: toSize(
      dataset.expandedHeight ?? dataset.openHeight ?? dataset.height,
      '740px',
    ),
  }

  const collapsedSize = {
    width: toSize(dataset.collapsedWidth ?? dataset.closeWidth ?? '100', '100px'),
    height: toSize(dataset.collapsedHeight ?? dataset.closeHeight ?? '100', '100px'),
  }

  const initialState = (() => {
    const desired = (dataset.initialState || dataset.defaultState || '').toLowerCase()
    if (desired === 'open' || desired === 'close' || desired === 'closed') {
      return desired === 'open' ? 'open' : 'closed'
    }
    if ((dataset.autoOpen || '').toLowerCase() === 'true') return 'open'
    return 'closed'
  })()

  const position = (dataset.position || 'right').toLowerCase() === 'left' ? 'left' : 'right'
  const horizontalOffset = toSize(dataset.offsetX ?? dataset.horizontalOffset ?? dataset.offset ?? 16, '16px')
  const verticalPosition =
    (dataset.verticalPosition || dataset.vertical || '').toLowerCase() === 'top'
      ? 'top'
      : 'bottom'
  const verticalOffset = toSize(dataset.offsetY ?? dataset.verticalOffset ?? 16, '16px')

  const wrapper = d.createElement('iframe')
  wrapper.id = dataset.widgetId || 'CHAT_AI_WIDGET'
  wrapper.src = resolvedWidgetUrl.toString()
  wrapper.title = dataset.iframeTitle || dataset.title || 'AI Assistant'
  wrapper.referrerPolicy =
    dataset.referrerPolicy || 'strict-origin-when-cross-origin'
  wrapper.allow =
    dataset.allow || 'microphone; clipboard-write; cross-origin-isolated'
  wrapper.setAttribute('allowtransparency', 'true')
  wrapper.style.position = dataset.positioning || 'fixed'
  wrapper.style.border = 'none'
  const defaultExpandedBorderRadius = dataset.borderRadius || '18px'
  const defaultExpandedBoxShadow =
    dataset.boxShadow || 'rgb(0 0 0 / 25%) 0px 25px 50px -12px'
  const collapsedBorderRadius = dataset.collapsedBorderRadius || '0px'
  const collapsedBoxShadow = dataset.collapsedBoxShadow || 'none'
  const expandedBackground =
    dataset.expandedBackground || dataset.background || '#ffffff'
  const collapsedBackground =
    dataset.collapsedBackground ??
    dataset.launcherBackground ??
    'transparent'

  wrapper.style.borderRadius = defaultExpandedBorderRadius
  wrapper.style.boxShadow = defaultExpandedBoxShadow
  wrapper.style.background = expandedBackground
  wrapper.style.zIndex = String(dataset.zIndex || 9999)
  wrapper.style.maxWidth = toSize(dataset.maxWidth, '100vw')
  wrapper.style.maxHeight = toSize(dataset.maxHeight, '100vh')
  wrapper.style[position] = horizontalOffset
  wrapper.style[verticalPosition] = verticalOffset

  const applySize = (size) => {
    if (!size) return
    if (size.width) {
      const widthValue = toSize(size.width, null)
      if (widthValue) {
        wrapper.style.width = widthValue
        if (pixelPattern.test(widthValue)) {
          wrapper.setAttribute('width', String(parseFloat(widthValue)))
        } else {
          wrapper.removeAttribute('width')
        }
      }
    }
    if (size.height) {
      const heightValue = toSize(size.height, null)
      if (heightValue) {
        wrapper.style.height = heightValue
        if (pixelPattern.test(heightValue)) {
          wrapper.setAttribute('height', String(parseFloat(heightValue)))
        } else {
          wrapper.removeAttribute('height')
        }
      }
    }
  }

  let currentState = initialState === 'closed' ? 'closed' : 'open'

  const commitState = (nextState) => {
    currentState = nextState === 'closed' ? 'closed' : 'open'
    wrapper.dataset.widgetState = currentState
    wrapper.setAttribute('aria-expanded', currentState === 'open' ? 'true' : 'false')
    applySize(currentState === 'open' ? expandedSize : collapsedSize)
    if (currentState === 'open') {
      wrapper.style.borderRadius = defaultExpandedBorderRadius
      wrapper.style.boxShadow = defaultExpandedBoxShadow
      wrapper.style.background = expandedBackground
    } else {
      wrapper.style.borderRadius = collapsedBorderRadius
      wrapper.style.boxShadow = collapsedBoxShadow
      wrapper.style.background = collapsedBackground
    }
  }

  commitState(currentState)

  const widgetOrigin = resolvedWidgetUrl.origin

  const handleResize = (payload) => {
    const nextWidth = payload.width ?? payload.w
    const nextHeight = payload.height ?? payload.h
    const nextSize = {
      width: nextWidth ? toSize(nextWidth, null) : null,
      height: nextHeight ? toSize(nextHeight, null) : null,
    }
    applySize(nextSize)
    if (currentState === 'open') {
      if (nextSize.width) expandedSize.width = nextSize.width
      if (nextSize.height) expandedSize.height = nextSize.height
    } else {
      if (nextSize.width) collapsedSize.width = nextSize.width
      if (nextSize.height) collapsedSize.height = nextSize.height
    }
  }

  const handleMessage = (event) => {
    if (event.source !== wrapper.contentWindow) return
    if (
      widgetOrigin &&
      widgetOrigin !== 'null' &&
      event.origin &&
      event.origin !== widgetOrigin
    ) {
      return
    }

    const payload = event.data
    if (!payload) return

    if (typeof payload === 'string') {
      switch (payload.toUpperCase()) {
        case 'OPEN':
        case 'EXPAND':
          commitState('open')
          return
        case 'CLOSE':
        case 'COLLAPSE':
          commitState('closed')
          return
        case 'TOGGLE':
          commitState(currentState === 'open' ? 'closed' : 'open')
          return
        default:
          return
      }
    }

    if (typeof payload !== 'object') return
    const type = (payload.type || payload.command || payload.event || '').toString().toUpperCase()

    switch (type) {
      case 'OPEN':
      case 'EXPAND':
        commitState('open')
        break
      case 'CLOSE':
      case 'COLLAPSE':
        commitState('closed')
        break
      case 'TOGGLE':
        commitState(currentState === 'open' ? 'closed' : 'open')
        break
      case 'RESIZE':
        handleResize(payload)
        break
      default:
        if (payload.width || payload.height || payload.w || payload.h) {
          handleResize(payload)
        }
        break
    }
  }

  w.addEventListener('message', handleMessage, false)

  const appendWidget = () => {
    if (!d.body) return
    if (!d.getElementById(wrapper.id)) {
      d.body.appendChild(wrapper)
    }
  }

  if (d.readyState === 'loading') {
    d.addEventListener('DOMContentLoaded', appendWidget, { once: true })
  } else {
    appendWidget()
  }
})(window, document)
