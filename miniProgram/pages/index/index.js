// pages/index/index.js
Page({
  data: {
    profileAvatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuB0kGPs...',
    goals: [],
    weeklyStats: [
      { day: '周一', value: 40 },
      { day: '周二', value: 60 },
      { day: '周三', value: 55 },
      { day: '周四', value: 85 },
      { day: '今天', value: 95, active: true },
      { day: '周六', value: 10 },
      { day: '周日', value: 12 }
    ]
  },

  onShow() {
    // 从缓存读取最新目标与学科数据
    const goals = wx.getStorageSync('tutormate_goals') || [];
    this.setData({ goals });
  },

  onGoalClick(e) {
    const id = e.currentTarget.dataset.id;
    let goals = this.data.goals.map(g => {
      if (g.id === id) {
        g.currentMinutes = Math.min(g.targetMinutes, g.currentMinutes + 2);
      }
      return g;
    });
    this.setData({ goals });
    wx.setStorageSync('tutormate_goals', goals);
    wx.showToast({
      title: '练习积累 +2分钟',
      icon: 'success'
    });
  },

  goToScan() {
    wx.switchTab({
      url: '/pages/scan/scan'
    });
  },

  goToReport() {
    wx.navigateTo({
      url: '/pages/reports/reports'
    });
  }
})