// app.js
App({
  onLaunch() {
    // 初始化本地存储 state
    const mistakes = wx.getStorageSync('tutormate_mistakes');
    if (!mistakes) {
      wx.setStorageSync('tutormate_mistakes', this.globalData.initialMistakes);
    }
    const goals = wx.getStorageSync('tutormate_goals');
    if (!goals) {
      wx.setStorageSync('tutormate_goals', this.globalData.initialGoals);
    }
  },
  
  globalData: {
    userInfo: null,
    initialGoals: [
      { id: 'g1', title: '数学专项练习', currentMinutes: 15, targetMinutes: 20, subject: '数学' },
      { id: 'g2', title: '英语绘本阅读', currentMinutes: 0, targetMinutes: 15, subject: '英语' }
    ],
    initialMistakes: [
      {
        id: 'm1',
        title: '三位数除法应用题',
        image: 'https://lh3.googleusercontent.com/aida-public/...',
        difficulty: '难',
        tags: ['除法', '逻辑思维'],
        date: '2023-11-24',
        subject: '数学',
        correctSolve: '由于没考虑余数的进位处理，正确答案应该是 125 余 3，需进一位。',
        explanation: '对于实际生活情况装载货物等，有余数时应该向上取整应用运输单位。',
        errorRate: 35,
        solved: false
      }
    ]
  }
})