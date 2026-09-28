<template>
  <footer class="footer">
    <div class="abc-container">
      <div class="footer__promise">
        <div class="footer__promise-item" v-for="item in promises" :key="item.title">
          <el-icon :size="26"><component :is="item.icon" /></el-icon>
          <div>
            <b>{{ item.title }}</b>
            <span>{{ item.desc }}</span>
          </div>
        </div>
      </div>

      <div class="footer__links">
        <div class="footer__col">
          <h4>购物指南</h4>
          <router-link to="/feature/help-center">帮助中心</router-link>
          <router-link to="/feature/faq">常见问题</router-link>
          <router-link to="/feature/online-service">在线客服</router-link>
          <router-link to="/feature/ai-service">智能客服</router-link>
        </div>
        <div class="footer__col">
          <h4>配送方式</h4>
          <router-link to="/feature/store-pickup">门店自提</router-link>
          <router-link to="/feature/city-delivery">同城配送</router-link>
          <router-link to="/feature/appointment-delivery">预约配送</router-link>
          <router-link to="/feature/freight-template">运费模板</router-link>
          <router-link to="/feature/delivery-range">配送范围</router-link>
        </div>
        <div class="footer__col">
          <h4>售后服务</h4>
          <router-link to="/user/aftersale">我的售后</router-link>
          <router-link to="/feature/refund">退款</router-link>
          <router-link to="/feature/refund-partial">部分退款</router-link>
          <router-link to="/feature/refund-progress">退款进度</router-link>
          <router-link to="/feature/repair">维修</router-link>
        </div>
        <div class="footer__col">
          <h4>营销活动</h4>
          <router-link to="/feature/seckill">秒杀</router-link>
          <router-link to="/feature/groupon">拼团</router-link>
          <router-link to="/feature/bargain">砍价</router-link>
          <router-link to="/feature/lottery">抽奖</router-link>
          <router-link to="/feature/invite">邀请有礼</router-link>
        </div>
        <div class="footer__col">
          <h4>会员服务</h4>
          <router-link to="/user/points">我的积分</router-link>
          <router-link to="/feature/member-level">会员等级</router-link>
          <router-link to="/feature/growth">成长值</router-link>
          <router-link to="/feature/real-name">实名认证</router-link>
          <router-link to="/user/messages">消息中心</router-link>
        </div>
      </div>

      <div class="footer__bottom">
        <p>AbcMall 阿帕茨商城 · 课程演示项目 · 前端 Vue3 + ElementPlus + Axios · 后端 SpringBoot3 + MyBatis-Plus + Session</p>
        <p class="footer__copy">© 2026 AbcMall. 仅供学习交流使用，所有商品与价格均为演示数据。</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
const promises = [
  { icon: 'CircleCheckFilled', title: '正品保障', desc: '官方授权 假一赔十' },
  { icon: 'Van', title: '极速发货', desc: '24 小时内出库' },
  { icon: 'RefreshLeft', title: '七天无理由', desc: '不满意可退换' },
  { icon: 'Service', title: '售后无忧', desc: '专属客服全程跟进' }
]
</script>

<style scoped>
.footer {
  margin-top: 40px;
  background: #fff;
  border-top: 1px solid var(--abc-border);
  padding: 28px 0 20px;
}
.footer__promise {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--abc-border);
}
.footer__promise-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--abc-primary);
}
.footer__promise-item b {
  display: block;
  font-size: 15px;
  color: var(--abc-text);
}
.footer__promise-item span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.footer__links {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  padding: 24px 0;
}
.footer__col h4 {
  margin: 0 0 12px;
  font-size: 14px;
}
.footer__col a {
  display: block;
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 26px;
  transition: color 0.2s;
}
.footer__col a:hover {
  color: var(--abc-primary);
}
.footer__bottom {
  border-top: 1px solid var(--abc-border);
  padding-top: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--abc-text-3);
  line-height: 22px;
}
.footer__copy {
  margin: 4px 0 0;
}
@media (max-width: 900px) {
  .footer__promise,
  .footer__links {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
