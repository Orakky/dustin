/**
 * @typedef {Object} FetchSSEOptions
 * @property {string} url
 * @property {RequestInit} [fetchOptions]
 * @property {(evt: any) => void} [success]
 * @property {() => void} [fail]
 * @property {(ok: boolean, message?: string) => void} [complete]
 */

/**
 * 简化的 SSE 客户端适配 OpenAI-like 流式接口
 * @param {FetchSSEOptions} options
 */
export const fetchSSE = async (options = {}) => {
  const url = options.url
  const fetchOptions = options.fetchOptions
  const success = options.success
  const fail = options.fail
  const complete = options.complete

  try {
    const response = await fetch(url, fetchOptions)
    if (!response || !response.ok) {
      if (complete) complete(false, response ? response.statusText : 'Request failed')
      if (fail) fail()
      throw new Error('Request failed')
    }

    const reader = response.body && response.body.getReader ? response.body.getReader() : null
    if (!reader) {
      if (complete) complete(false, 'No reader available')
      throw new Error('No reader available')
    }

    const decoder = new TextDecoder()
    const bufferArr = []
    const event = { type: null, data: null }

    const processText = async ({ done, value }) => {
      if (done) {
        if (complete) complete(true)
        return
      }

      const chunk = decoder.decode(value)
      const buffers = chunk.toString().split(/\r?\n/)
      Array.prototype.push.apply(bufferArr, buffers)

      let i = 0
      while (i < bufferArr.length) {
        const line = bufferArr[i]
        if (line && line.indexOf('data:') === 0) {
          const payload = line.slice(5).trim()
          if (payload === '[DONE]') {
            event.type = 'finish'
          } else {
            try {
              const choices = JSON.parse(payload).choices
              const deltaChoice = choices && choices[0]
              if (deltaChoice && deltaChoice.finish_reason === 'stop') {
                event.type = 'finish'
              } else if (deltaChoice) {
                event.type = 'delta'
                event.data = deltaChoice
              }
            } catch (err) {
              // 忽略解析失败
            }
          }
        }

        if (event.type && (event.data || event.type === 'finish')) {
          const dataCopy = JSON.parse(JSON.stringify(event))
          if (success) success(dataCopy)
          event.type = null
          event.data = null
        }
        bufferArr.splice(i, 1)
      }

      return reader.read().then(processText)
    }

    return reader.read().then(processText)
  } catch (error) {
    if (complete) complete(false, error && error.message ? error.message : 'Stream error')
    if (fail) fail()
    throw error
  }
}
