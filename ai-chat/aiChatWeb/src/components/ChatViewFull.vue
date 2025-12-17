<template>
  <div
    class="chat-full"
    :class="{
      'chat-full--widget': isWidgetMode,
      'chat-full--compact': showCompactLauncher,
    }"
  >
    <button
      v-if="showCompactLauncher"
      class="chat-launcher"
      type="button"
      aria-label="Open chat"
      :style="launcherStyle"
      @mouseenter="handleLauncherMouseEnter"
      @mouseleave="handleLauncherMouseLeave"
      @focus="handleLauncherMouseEnter"
      @blur="handleLauncherMouseLeave"
      @click="handleExpand"
    >
      <img
        :key="launcherGifKey"
        :src="launcherGifSrc"
        class="chat-launcher__gif"
        alt="AI assistant"
        decoding="async"
      />
    </button>
    <div v-else class="chat-full__content">
      <header class="chat-full__header">
        <div class="chat-full__heading">
          <h1 class="chat-full__title">中远海运重工集团 AI 助手</h1>
        </div>
        <button
          v-if="isWidgetMode"
          class="chat-full__collapse"
          type="button"
          aria-label="Open chat"
          @click="handleCollapse"
        >
          关闭
        </button>
      </header>

      <div class="chat-full__shell">
        <aside class="chat-full__sidebar">
          <div class="chat-full__sessions-header">
            <span>会话列表</span>
            <button class="chat-full__add-session" type="button" @click="handleAddSession">
              + 新建会话
            </button>
          </div>
          <ul class="chat-full__sessions-list">
            <li
              v-for="session in sessions"
              :key="session.id"
              class="chat-full__session-item"
              :class="{ 'chat-full__session-item--active': session.id === activeSessionId }"
            >
              <button
                class="chat-full__session-switch"
                type="button"
                @click="handleSwitchSession(session.id)"
              >
                <span class="chat-full__session-title">{{ session.title }}</span>
                <span v-if="session.loading" class="chat-full__session-badge">生成中</span>
              </button>
              <button
                class="chat-full__session-remove"
                type="button"
                :disabled="sessions.length === 1"
                @click.stop="handleDeleteSession(session.id)"
              >
                删除
              </button>
            </li>
          </ul>
        </aside>

        <div class="chat-full__panel">
          <t-chat
            ref="chatRef"
            :data="messages"
            :clear-history="true"
            :text-loading="false"
            :is-stream-load="loading"
            layout="both"
            :reverse="false"
            @clear="handleClear"
          >
            <template #footer>
              <div class="chat-full__composer">
                <textarea
                  ref="textareaRef"
                  v-model="draft"
                  class="chat-full__textarea"
                  placeholder="Enter 发送,Shift+Enter 换行"
                  rows="1"
                  @input="autoResize"
                  @keydown="handleTextareaKeydown"
                />
                <button
                  class="chat-full__send-btn"
                  :class="{ 'chat-full__send-btn--stop': loading }"
                  type="button"
                  :disabled="!loading && !draft.trim().length"
                  @click="loading ? handleStop() : triggerSend()"
                >
                  <StopCircleIcon v-if="loading" :size="18" />
                  <svg
                    v-else
                    fill="none"
                    viewBox="0 0 24 24"
                    width="1em"
                    height="1em"
                    class="t-icon t-icon-send"
                    style="fill: none;"
                    aria-hidden="true"
                  >
                    <g clip-path="url(#clip0_543_8119)">
                      <path fill="transparent" d="M2 3.5L21.5 12L2 20.5L5 12L2 3.5Z"></path>
                      <path
                        stroke="currentColor"
                        d="M5 12L2 20.5L21.5 12L2 3.5L5 12ZM5 12H10"
                        stroke-linecap="square"
                        stroke-width="2"
                      ></path>
                    </g>
                    <defs>
                      <clipPath id="clip0_543_8119">
                        <rect width="24" height="24" fill="white"></rect>
                      </clipPath>
                    </defs>
                  </svg>
                </button>
              </div>
            </template>
          </t-chat>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  reactive,
  ref,
  onMounted,
  onBeforeUnmount,
  watch,
  watchEffect,
} from 'vue'
import { DialogPlugin } from 'tdesign-vue-next'
import { StopCircleIcon } from 'tdesign-icons-vue-next'

const chatRef = ref(null)
const textareaRef = ref(null)
const initialHeight = ref(32)
const isWidgetMode = ref(false)
const isCompact = ref(false)
const launcherConfig = reactive({
  horizontal: 'right',
  vertical: 'bottom',
  offsetX: '0px',
  offsetY: '0px',
})

const DEFAULT_LAUNCHER_GIF = '/pandaeyeT.gif'
const HOVER_LAUNCHER_GIF = '/pandahandT.gif'
const HOVER_GIF_DURATION = 3600
const RANDOM_LAUNCHER_GIFS = ['/panda1.gif', '/panda2.gif', '/panda3.gif', '/panda5.gif', '/panda6.gif', '/panda8.gif']
const RANDOM_GIF_INTERVAL = 10 * 1000
const RANDOM_GIF_DURATION = 4 * 1000

const launcherGifSrc = ref(DEFAULT_LAUNCHER_GIF)
const launcherGifKey = ref(0)
const hoverGifTimer = ref(null)
const randomGifTimer = ref(null)
const randomGifResetTimer = ref(null)

const avatars = {
  user: '/cosco.jpg',
  assistant: '/ai.png',
}

const DEFAULT_ASSISTANT_NAME = '中远海运重工AI助手'
const DEFAULT_GREETING = '你好,我是你的 AI 助手,有什么可以帮你?'

function createSessionId() {
  return `session-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
}

function createAssistantMessage(content = DEFAULT_GREETING) {
  return {
    role: 'assistant',
    name: DEFAULT_ASSISTANT_NAME,
    avatar: avatars.assistant,
    datetime: getTime(),
    content,
    reasoning: null,
    textLoading: false,
  }
}

function createSession({ title } = {}) {
  return {
    id: createSessionId(),
    title: title || '新会话',
    messages: [createAssistantMessage()],
    draft: '',
    loading: false,
    controller: null,
  }
}

const sessions = reactive([createSession()])
const activeSessionId = ref(sessions[0].id)

const activeSession = computed(() => sessions.find((item) => item.id === activeSessionId.value) || null)
const messages = computed(() => (activeSession.value ? activeSession.value.messages : []))
const loading = computed(() => activeSession.value?.loading ?? false)
const showCompactLauncher = computed(() => isWidgetMode.value && isCompact.value)
const launcherStyle = computed(() => {
  if (!showCompactLauncher.value) return {}
  const horizontal = launcherConfig.horizontal === 'left' ? 'left' : 'right'
  const vertical = launcherConfig.vertical === 'top' ? 'top' : 'bottom'
  const translateX =
    horizontal === 'right' ? `calc(-1 * ${launcherConfig.offsetX})` : launcherConfig.offsetX
  const translateY =
    vertical === 'bottom' ? `calc(-1 * ${launcherConfig.offsetY})` : launcherConfig.offsetY
  return {
    position: 'absolute',
    [horizontal]: 0,
    [vertical]: 0,
    zIndex: 2,
    transform: `translate(${translateX}, ${translateY})`,
  }
})

const clearHoverTimer = () => {
  if (hoverGifTimer.value) {
    clearTimeout(hoverGifTimer.value)
    hoverGifTimer.value = null
  }
}

const clearRandomGifTimer = () => {
  if (randomGifTimer.value) {
    clearTimeout(randomGifTimer.value)
    randomGifTimer.value = null
  }
}

const clearRandomGifResetTimer = () => {
  if (randomGifResetTimer.value) {
    clearTimeout(randomGifResetTimer.value)
    randomGifResetTimer.value = null
  }
}

const WIDGET_COMPACT_CLASS = 'chat-widget-compact'
let widgetBackdropApplied = false

const syncWidgetBackdrop = (active) => {
  if (typeof document === 'undefined') return
  const html = document.documentElement
  if (!html) return
  if (active) {
    if (widgetBackdropApplied) return
    html.classList.add(WIDGET_COMPACT_CLASS)
    widgetBackdropApplied = true
  } else if (widgetBackdropApplied) {
    html.classList.remove(WIDGET_COMPACT_CLASS)
    widgetBackdropApplied = false
  }
}

const applyLauncherGif = (src) => {
  launcherGifSrc.value = src
  launcherGifKey.value += 1
}

const showDefaultLauncherGif = (force = false) => {
  if (launcherGifSrc.value !== DEFAULT_LAUNCHER_GIF || force) {
    applyLauncherGif(DEFAULT_LAUNCHER_GIF)
  }
}

function stopRandomGifRoutine(resetDefault = false) {
  clearRandomGifTimer()
  clearRandomGifResetTimer()
  if (resetDefault) {
    showDefaultLauncherGif(true)
  }
}

function scheduleRandomGifPlayback() {
  if (typeof window === 'undefined') return
  if (!showCompactLauncher.value) return
  if (randomGifTimer.value) return
  randomGifTimer.value = window.setTimeout(() => {
    randomGifTimer.value = null
    playRandomLauncherGif()
  }, RANDOM_GIF_INTERVAL)
}

function playRandomLauncherGif() {
  if (typeof window === 'undefined') return
  if (!showCompactLauncher.value) return
  clearRandomGifResetTimer()
  clearHoverTimer()
  const pool = RANDOM_LAUNCHER_GIFS.length ? RANDOM_LAUNCHER_GIFS : [DEFAULT_LAUNCHER_GIF]
  const candidates = pool.filter((item) => item && item !== launcherGifSrc.value)
  const source =
    candidates.length > 0
      ? candidates[Math.floor(Math.random() * candidates.length)]
      : pool[Math.floor(Math.random() * pool.length)]
  applyLauncherGif(source || DEFAULT_LAUNCHER_GIF)
  if (typeof window !== 'undefined') {
    randomGifResetTimer.value = window.setTimeout(() => {
      randomGifResetTimer.value = null
      showDefaultLauncherGif(true)
      scheduleRandomGifPlayback()
    }, RANDOM_GIF_DURATION)
  }
}

function handleLauncherMouseEnter() {
  if (!showCompactLauncher.value) return
  clearHoverTimer()
  stopRandomGifRoutine(false)
  applyLauncherGif(HOVER_LAUNCHER_GIF)
  if (typeof window !== 'undefined') {
    hoverGifTimer.value = window.setTimeout(() => {
      showDefaultLauncherGif(true)
      hoverGifTimer.value = null
    }, HOVER_GIF_DURATION)
  }
}

function handleLauncherMouseLeave() {
  clearHoverTimer()
  showDefaultLauncherGif(false)
  scheduleRandomGifPlayback()
}

const draft = computed({
  get: () => activeSession.value?.draft ?? '',
  set: (val) => {
    if (activeSession.value) activeSession.value.draft = val
  },
})

function formatSessionTitle(text) {
  const pure = String(text || '')
    .replace(/\s+/g, ' ')
    .trim()
  if (!pure) return '新会话'
  return pure.length > 18 ? `${pure.slice(0, 18)}…` : pure
}

function postToParent(command, detail) {
  if (typeof window === 'undefined') return
  const target = window.parent
  if (!target || target === window) return
  if (detail === undefined) {
    target.postMessage(command, '*')
  } else if (typeof detail === 'object') {
    target.postMessage({ type: command, ...detail }, '*')
  } else {
    target.postMessage(detail, '*')
  }
}

function handleExpand() {
  clearHoverTimer()
  stopRandomGifRoutine(true)
  showDefaultLauncherGif(true)
  postToParent('OPEN')
  setTimeout(updateCompactFlag, 60)
}

function handleCollapse() {
  postToParent('CLOSE')
  setTimeout(updateCompactFlag, 60)
}

function ensureWidgetMode() {
  if (typeof window === 'undefined') return
  const params = new URLSearchParams(window.location.search)
  isWidgetMode.value = params.get('widget') === '1'
  if (isWidgetMode.value) {
    applyLauncherConfig(params)
  }
}

watchEffect(() => {
  const shouldApply = isWidgetMode.value && showCompactLauncher.value
  syncWidgetBackdrop(shouldApply)
})

watch(
  showCompactLauncher,
  (value) => {
    if (typeof window === 'undefined') return
    if (value) {
      showDefaultLauncherGif(true)
      scheduleRandomGifPlayback()
    } else {
      stopRandomGifRoutine(true)
    }
  },
  { immediate: true },
)

function updateCompactFlag() {
  if (typeof window === 'undefined' || !isWidgetMode.value) {
    isCompact.value = false
    return
  }
  const width = window.innerWidth || 0
  const height = window.innerHeight || 0
  isCompact.value = width < 260 || height < 260
}

function formatOffset(value, fallback) {
  if (value == null) return fallback
  const trimmed = String(value).trim()
  if (!trimmed) return fallback
  if (/^\d+(\.\d+)?$/.test(trimmed)) {
    return `${trimmed}px`
  }
  return trimmed
}

function applyLauncherConfig(params) {
  const rawPosition = (params.get('launcherPosition') || '').toLowerCase()
  if (rawPosition.includes('left')) {
    launcherConfig.horizontal = 'left'
  } else if (rawPosition.includes('right')) {
    launcherConfig.horizontal = 'right'
  }
  if (rawPosition.includes('top')) {
    launcherConfig.vertical = 'top'
  } else if (rawPosition.includes('bottom')) {
    launcherConfig.vertical = 'bottom'
  }

  const horizontalParam = (params.get('launcherHorizontal') || params.get('launcherAlign')) || ''
  if (horizontalParam) {
    launcherConfig.horizontal = horizontalParam.toLowerCase() === 'left' ? 'left' : 'right'
  }
  const verticalParam = params.get('launcherVertical') || ''
  if (verticalParam) {
    launcherConfig.vertical = verticalParam.toLowerCase() === 'top' ? 'top' : 'bottom'
  }

  const offsetXParam =
    params.get('launcherOffsetX') ??
    params.get('launcherOffset') ??
    params.get('launcherPadding')
  const offsetYParam = params.get('launcherOffsetY') ?? params.get('launcherOffset')

  launcherConfig.offsetX = formatOffset(offsetXParam, launcherConfig.offsetX)
  launcherConfig.offsetY = formatOffset(offsetYParam, launcherConfig.offsetY)
}

function handleSend(value) {
  const session = activeSession.value
  if (!session) return
  const content = (typeof value === 'string' ? value : draft.value).trim()
  if (!content) return

  if (
    session.messages.length <= 1 ||
    !session.title ||
    session.title === '新会话' ||
    /^会话\s*\d+$/i.test(session.title)
  ) {
    session.title = formatSessionTitle(content)
  }

  pushMessage(session, {
    role: 'user',
    name: '用户',
    avatar: avatars.user,
    content,
  })

  session.draft = ''
  nextTick(resetTextarea)
  scrollToBottom()

  session.loading = true

  const assistantMsg = {
    role: 'assistant',
    name: DEFAULT_ASSISTANT_NAME,
    avatar: avatars.assistant,
    content: '',
    reasoning: '',
    textLoading: true,
  }
  session.messages.push({
    datetime: getTime(),
    ...assistantMsg,
  })
  const targetIndex = session.messages.length - 1
  scrollToBottom()

  const rawBase = import.meta.env.VITE_LLM_BASE_URL || 'http://172.24.7.11:8080'
  const baseUrl = rawBase.endsWith('/') ? rawBase : `${rawBase}/`

  if (session.controller && typeof session.controller.abort === 'function') {
    session.controller.abort()
  }
  session.controller = new AbortController()
  const controller = session.controller

  const requestUrl = new URL('sse2', baseUrl)
  requestUrl.searchParams.set('message', `"${content}"`)
  requestUrl.searchParams.set('sessionId', `"${session.id}"`)

  const finalizeRequest = () => {
    const current = session.messages[targetIndex]
    if (current) current.textLoading = false
    if (session.controller === controller) {
      session.loading = false
      session.controller = null
    }
  }

  ;(async () => {
    try {
      const response = await fetch(requestUrl.toString(), {
        method: 'GET',
        headers: {
          Accept: 'text/event-stream',
        },
        signal: controller.signal,
      })

      if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}`.trim())
      }

      const message = session.messages[targetIndex]
      if (!message) return

      const updateMessage = () => {
        const currentMessage = session.messages[targetIndex]
        if (!currentMessage) return null
        return currentMessage
      }

      let reasoningText = ''
      let visibleText = ''

      const applyItem = (item) => {
        if (!item || typeof item !== 'object') return
        const targetMessage = updateMessage()
        if (!targetMessage) return

        const rawChunk =
          typeof item.contentRaw === 'string'
            ? item.contentRaw
            : typeof item.content === 'string'
            ? item.content
            : ''

        if (rawChunk === null || rawChunk === undefined) return
        const sanitized = rawChunk.replace(/<\/?think>/gi, '')

        if (item.isThinking) {
          if (rawChunk.includes('<think>')) {
            reasoningText = ''
          }
          if (sanitized) {
            reasoningText += sanitized
          }
          if (rawChunk.includes('</think>')) {
            const trimmed = reasoningText.trim()
            targetMessage.reasoning = trimmed ? trimmed : null
          } else {
            targetMessage.reasoning = reasoningText
          }
        } else {
          if (sanitized) {
            visibleText += sanitized
          }
          targetMessage.content = visibleText
        }

        scrollToBottom()
      }

      const finalizeMessage = () => {
        const targetMessage = updateMessage()
        if (!targetMessage) return
        const trimmed = reasoningText.trim()
        targetMessage.reasoning = trimmed ? trimmed : null
        targetMessage.content = visibleText
      }

      const handleDataPayload = (payload) => {
        if (payload === '[DONE]') {
          finalizeMessage()
          return true
        }
        try {
          const parsed = JSON.parse(payload)
          applyItem(parsed)
        } catch (err) {
          console.warn('解析流式响应失败', err)
        }
        return false
      }

      const processEvent = (eventText) => {
        if (!eventText) return false
        const lines = eventText.split(/\r?\n/)
        for (const line of lines) {
          if (!line || line[0] === ':') continue
          if (!line.startsWith('data:')) continue
          const payload = line.slice(5).trim()
          if (!payload) continue
          const stop = handleDataPayload(payload)
          if (stop) return true
          if (session.controller !== controller) return true
        }
        return false
      }

      if (!response.body || !response.body.getReader) {
        const text = await response.text()
        if (text) {
          const events = text.split(/\r?\n\r?\n/)
          for (const event of events) {
            if (processEvent(event)) break
          }
        }
        finalizeMessage()
        return
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let shouldStop = false

      const findSeparatorIndex = (text) => {
        const idxCRLF = text.indexOf('\r\n\r\n')
        const idxLF = text.indexOf('\n\n')
        if (idxCRLF === -1) return idxLF
        if (idxLF === -1) return idxCRLF
        return Math.min(idxCRLF, idxLF)
      }

      const drainBuffer = () => {
        while (!shouldStop) {
          const sepIndex = findSeparatorIndex(buffer)
          if (sepIndex === -1) break
          const delimiterLength = buffer.startsWith('\r\n\r\n', sepIndex) ? 4 : 2
          const eventText = buffer.slice(0, sepIndex)
          buffer = buffer.slice(sepIndex + delimiterLength)
          if (processEvent(eventText)) {
            shouldStop = true
            break
          }
        }
      }

      while (!shouldStop) {
        const { value, done } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        drainBuffer()
        if (session.controller !== controller) {
          shouldStop = true
          break
        }
      }

      buffer += decoder.decode()
      drainBuffer()
      finalizeMessage()
    } catch (error) {
      const message = session.messages[targetIndex]
      if (message) {
        if (error && error.name === 'AbortError') {
          if (!message.content) message.content = '[生成已停止]'
        } else {
          const detail = error && error.message ? error.message : '未知错误'
          if (!message.content) {
            message.content = `[生成失败:${detail}]`
          }
        }
      }
      if (!error || error.name !== 'AbortError') {
        console.error('请求模型失败', error)
      }
      scrollToBottom()
    } finally {
      finalizeRequest()
    }
  })()
}

function handleStop() {
  const session = activeSession.value
  if (!session || !session.controller) return
  if (typeof session.controller.abort === 'function') session.controller.abort()
  session.controller = null
  session.loading = false
  const current = session.messages[session.messages.length - 1]
  if (current && current.role === 'assistant') {
    current.textLoading = false
    if (!current.content) current.content = '[生成已停止]'
  }
}

function handleClear() {
  const session = activeSession.value
  if (!session) return
  if (session.controller && typeof session.controller.abort === 'function') {
    session.controller.abort()
  }
  session.controller = null
  session.loading = false
  session.messages.splice(0, session.messages.length, createAssistantMessage())
  session.draft = ''
  nextTick(() => {
    resetTextarea()
    scrollToBottom()
  })
}

function handleTextareaKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    triggerSend()
  }
  if (event.key === 'Escape') {
    event.preventDefault()
    handleStop()
  }
}

function triggerSend() {
  handleSend(draft.value)
}

function autoResize() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
  if (!initialHeight.value) {
    initialHeight.value = el.scrollHeight
  }
}

function resetTextarea() {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${initialHeight.value || 32}px`
}

onMounted(() => {
  ensureWidgetMode()
  updateCompactFlag()
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', updateCompactFlag)
  }
  nextTick(() => {
    const el = textareaRef.value
    if (!el) return
    initialHeight.value = el.scrollHeight || initialHeight.value
    resetTextarea()
    scrollToBottom()
    if (showCompactLauncher.value) {
      scheduleRandomGifPlayback()
    }
  })
})

onBeforeUnmount(() => {
  clearHoverTimer()
  stopRandomGifRoutine(false)
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', updateCompactFlag)
  }
  sessions.forEach((session) => {
    if (session.controller && typeof session.controller.abort === 'function') {
      session.controller.abort()
    }
  })
  syncWidgetBackdrop(false)
})

function pushMessage(session, item) {
  session.messages.push({
    datetime: getTime(),
    ...item,
  })
}

function getTime() {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date())
}

function scrollToBottom() {
  nextTick(() => {
    if (chatRef.value && typeof chatRef.value.scrollToBottom === 'function') {
      chatRef.value.scrollToBottom({ behavior: 'smooth' })
      return
    }
    const el =
      chatRef.value &&
      chatRef.value.$el &&
      typeof chatRef.value.$el.querySelector === 'function'
        ? chatRef.value.$el.querySelector('.t-chat__list')
        : null
    if (el) {
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
    }
  })
}

function handleAddSession() {
  const newSession = createSession()
  sessions.unshift(newSession)
  activeSessionId.value = newSession.id
  nextTick(() => {
    resetTextarea()
    scrollToBottom()
  })
}

function handleDeleteSession(id) {
  if (sessions.length === 1) return
  const index = sessions.findIndex((item) => item.id === id)
  if (index === -1) return
  const targetSession = sessions[index] || null

  const removeSession = () => {
    const [removed] = sessions.splice(index, 1)
    if (removed?.controller && typeof removed.controller.abort === 'function') {
      removed.controller.abort()
    }
    if (activeSessionId.value === id) {
      const nextSession = sessions[index] || sessions[index - 1] || sessions[0] || null
      activeSessionId.value = nextSession ? nextSession.id : ''
      nextTick(() => {
        if (draft.value) {
          autoResize()
        } else {
          resetTextarea()
        }
        scrollToBottom()
      })
    }
  }

  const dialog = DialogPlugin.confirm({
    header: '确认删除会话',
    body:
      (targetSession?.title
        ? `确定删除会话「${targetSession.title}」吗?删除后无法恢复。`
        : '确定删除该会话吗?删除后无法恢复。'),
    theme: 'warning',
    confirmBtn: { content: '删除', theme: 'danger' },
    cancelBtn: { content: '取消' },
    onConfirm: () => {
      removeSession()
      if (dialog && typeof dialog.hide === 'function') {
        dialog.hide()
      }
    },
    onClose: () => dialog?.destroy?.(),
  })
}

function handleSwitchSession(id) {
  if (activeSessionId.value === id) return
  const exists = sessions.find((item) => item.id === id)
  if (!exists) return
  activeSessionId.value = id
  nextTick(() => {
    if (draft.value) {
      autoResize()
    } else {
      resetTextarea()
    }
    scrollToBottom()
  })
}
</script>

<style scoped>
.chat-full {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  padding: 0;
  background: radial-gradient(circle at top, rgba(59, 130, 246, 0.08), transparent 55%), #f7f8fa;
  box-sizing: border-box;
  overflow: hidden;
  position: relative;
}

.chat-full--widget {
  background: transparent;
}

.chat-full--compact {
  padding: 0;
  background: transparent;
  justify-content: center;
  align-items: center;
}

.chat-full__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: clamp(18px, 4vw, 32px);
  gap: 12px;
}

.chat-full--widget .chat-full__content {
  padding: 0;
  gap: 0;
}

.chat-full__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
}

.chat-full--widget .chat-full__header {
  padding: 16px 16px 12px;
}

.chat-full__heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-full__title {
  margin: 0;
  font-size: clamp(20px, 4vw, 26px);
  font-weight: 700;
  color: rgba(15, 23, 42, 0.95);
}

.chat-full__subtitle {
  margin: 0;
  color: rgba(15, 23, 42, 0.65);
  font-size: 14px;
  line-height: 1.4;
}

.chat-full__collapse {
  border: none;
  background: rgba(37, 99, 235, 0.1);
  color: rgba(37, 99, 235, 0.92);
  border-radius: 999px;
  padding: 0 18px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
  font-size: 14px;
  font-weight: 600;
}

.chat-full__collapse:hover {
  background: rgba(37, 99, 235, 0.2);
  color: rgba(15, 23, 42, 0.95);
}

.chat-full__shell {
  flex: 1;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  align-items: stretch;
  border-radius: 22px;
  border: 1px solid rgba(37, 99, 235, 0.12);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.14);
  overflow: hidden;
  min-height: 0;
}

.chat-full--widget .chat-full__shell {
  border-radius: 0;
  border: none;
  box-shadow: none;
}

.chat-full__sidebar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px 14px;
  border-right: 1px solid rgba(37, 99, 235, 0.12);
  background: linear-gradient(150deg, rgba(37, 99, 235, 0.08), rgba(248, 250, 252, 0.92));
  overflow: hidden;
}

.chat-full__sessions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.85);
}

.chat-full__add-session {
  border: none;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  color: #fff;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.chat-full__add-session:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.25);
}

.chat-full__sessions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
  flex: 1;
  overflow: hidden auto;
}

.chat-full__sessions-list::-webkit-scrollbar {
  width: 6px;
}

.chat-full__sessions-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.6);
}

.chat-full__session-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border-radius: 12px;
  transition: background 0.2s ease;
}

.chat-full__session-item:hover {
  background: rgba(255, 255, 255, 0.25);
}

.chat-full__session-item--active {
  background: rgba(255, 255, 255, 0.4);
  box-shadow: inset 0 0 0 1px rgba(37, 99, 235, 0.2);
}

.chat-full__session-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  border: none;
  background: none;
  padding: 0;
  color: rgba(15, 23, 42, 0.85);
  cursor: pointer;
  text-align: left;
}

.chat-full__session-title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-full__session-badge {
  flex-shrink: 0;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.12);
  color: rgba(37, 99, 235, 0.8);
}

.chat-full__session-remove {
  flex-shrink: 0;
  border: none;
  background: none;
  color: rgba(15, 23, 42, 0.45);
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.chat-full__session-remove:hover:not(:disabled) {
  color: rgba(220, 38, 38, 0.9);
}

.chat-full__session-remove:disabled {
  cursor: not-allowed;
  color: rgba(148, 163, 184, 0.7);
}

.chat-full__panel {
  display: flex;
  flex-direction: column;
  padding: 16px 16px 10px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.chat-full--widget .chat-full__panel {
  padding: 16px 16px 12px;
}

::v-deep(.t-chat) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

::v-deep(.t-chat__list-wrap),
::v-deep(.t-chat__messages),
::v-deep(.t-chat__list) {
  flex: 1;
  min-height: 0;
}

::v-deep(.t-chat__list-wrap) {
  overflow-y: auto;
  max-height: none;
}

::v-deep(.t-chat__footer) {
  border-top: 1px solid rgba(37, 99, 235, 0.12);
  padding: 10px 14px 4px;
  background: rgba(248, 250, 252, 0.9);
  border-radius: 0 0 22px 22px;
}

.chat-full__composer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 30px;
  background: rgba(248, 250, 252, 0.96);
  border: 1px solid rgba(37, 99, 235, 0.18);
  box-shadow: inset 0 1px 2px rgba(15, 23, 42, 0.08), 0 6px 14px rgba(15, 23, 42, 0.05);
}

.chat-full__textarea {
  flex: 1;
  border: none;
  resize: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  padding: 4px 0;
  color: rgba(15, 23, 42, 0.9);
  outline: none;
}

.chat-full__textarea::placeholder {
  color: rgba(15, 23, 42, 0.4);
}

.chat-full__send-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: linear-gradient(135deg, #2563eb, #38bdf8);
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.chat-full__send-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.25);
}

.chat-full__send-btn--stop {
  background: linear-gradient(135deg, #f97316, #ef4444);
}

.chat-launcher {
  display: inline-block;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: transform 0.2s ease;
  width: 100px;
  height: 100px;
  max-width: 100px;
  max-height: 100px;
  overflow: visible;
}

.chat-launcher:hover,
.chat-launcher:focus-visible {
  transform: translateY(-2px);
}

.chat-launcher__gif {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  pointer-events: none;
  filter: brightness(1.15) saturate(3.3);
}

@media (max-width: 1024px) {
  .chat-full__shell {
    grid-template-columns: minmax(0, 1fr);
  }

  .chat-full__sidebar {
    display: none;
  }
}

@media (max-width: 640px) {
  .chat-full {
    padding: 12px;
  }
}
</style>


