// index.js
const app = getApp()

Page({
  data: {
    messages: [],
    inputValue: '',
    loading: false,
    sending: false,
    ingestLoading: false,
    ingestButtonText: '📚 知识库Ingest',
    documentOptions: [
      { label: '空', value: '' },
      { label: 'all', value: 'all' }
    ],
    documentIndex: 0,
    scrollIntoView: '',
    messageId: 0
  },

  getBaseUrl() {
    return app.globalData.baseUrl || 'http://localhost:8080'
  },

  onInputChange(e) {
    this.setData({ inputValue: e.detail.value })
  },

  onDocumentChange(e) {
    this.setData({ documentIndex: Number(e.detail.value) })
  },

  scrollToBottom() {
    const target = this.data.loading ? 'loading-msg' : `msg-${this.data.messageId}`
    this.setData({ scrollIntoView: target })
  },

  addMessage(content, isUser) {
    const id = this.data.messageId + 1
    const messages = [...this.data.messages, { id, content, isUser }]
    this.setData({ messages, messageId: id }, () => {
      this.scrollToBottom()
    })
  },

  updateDocumentSelect(documents) {
    const options = [
      { label: '空', value: '' },
      { label: 'all', value: 'all' }
    ]
    if (Array.isArray(documents)) {
      documents.forEach(doc => {
        if (doc.name) {
          options.push({ label: doc.name, value: doc.name })
        }
      })
    }
    this.setData({ documentOptions: options, documentIndex: 0 })
  },

  ingestKnowledge() {
    this.setData({
      ingestLoading: true,
      ingestButtonText: '⏳ Ingest中...'
    })

    wx.request({
      url: `${this.getBaseUrl()}/ragIngest`,
      method: 'GET',
      success: (res) => {
        const data = res.data
        const count = Array.isArray(data) ? data.length : 0
        this.setData({ ingestButtonText: `📚 知识库${count}` })
        this.updateDocumentSelect(data)
        this.addMessage(`✅ 知识库导入完成！共导入 ${count} 个Document。`, false)
      },
      fail: () => {
        this.setData({ ingestButtonText: '❌ Ingest失败' })
        this.addMessage('❌ 知识库导入失败，请稍后重试。', false)
        setTimeout(() => {
          this.setData({ ingestButtonText: '📚 知识库Ingest' })
        }, 3000)
      },
      complete: () => {
        this.setData({ ingestLoading: false })
      }
    })
  },

  sendMessage() {
    const message = this.data.inputValue.trim()
    if (!message || this.data.sending) return

    this.addMessage(message, true)
    this.setData({
      inputValue: '',
      sending: true,
      loading: true
    }, () => {
      this.scrollToBottom()
    })

    const selectedDocument = this.data.documentOptions[this.data.documentIndex].value
    const requestBody = { message }
    if (selectedDocument) {
      requestBody.documentName = selectedDocument
    }

    wx.request({
      url: `${this.getBaseUrl()}/chat`,
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: requestBody,
      success: (res) => {
        this.addMessage(res.data || '', false)
      },
      fail: () => {
        this.addMessage('抱歉，发生了错误，请稍后重试。', false)
      },
      complete: () => {
        this.setData({ sending: false, loading: false })
      }
    })
  }
})
