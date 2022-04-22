<template>
  <!-- 扫码登录 -->
  <div class="login_box">
    <router-link to="/input">
      <div class="login_close"></div>
    </router-link>
    <div class="qrcode">
      <img class="img" :src="imgURL" alt="登錄碼" v-show="state === 1||state === 3"/>
      <div class="empty" v-show="state === 0"></div>
      <div class="refresh" v-show="state === 3">
        <i class="refresh_mask"></i>
        <i class="refresh_icon" @click="getToken"></i>
      </div>
      <div class="result" v-show="state === 2">
        <img class="u_avatar" :src="userAvatar" alt="使用者頭像"/>
        <p class="u_name">{{userName}}</p>
      </div>
      <div>
        <p class="sub_title">{{tip}}</p>
        <p class="sub_desc">掃碼登錄，更簡易、更快、更安全</p>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'Auth',
  data () {
    return {
      state: 0, // 場景：0無登錄碼，1有登錄碼，2正在登錄，3登錄碼過期
      count: 30, // 登錄碼有效倒計時（S）
      tip: '正在獲取登錄碼，請稍等', // 提示
      imgURL: '', // 登錄碼路徑
      authToken: '', // 驗證口令
      userId: '', // 掃碼登錄的用戶ID
      userAvatar: '', // 掃碼登錄的用戶頭像
      userName: '', // 掃碼登錄的用戶名
      tokenApi: 'http://localhost/auth/token', // 獲取口令
      tokenImgApi: 'http://localhost/auth/img/', // 獲取口令對應的登錄碼
      tokenInfoApi: 'http://localhost/auth/info/', // 獲取口令信息
      userInfoApi: 'http://localhost/login/getUser' // 獲取用戶信息
    }
  },
  created () {
    this.getToken()
  },
  methods: {
    getToken () {
      console.log('開始獲取')
      // 所有參數重置
      this.state = 0 // 場景為無二維碼
      this.tip = '正在獲取登錄碼，請稍等'
      this.count = 30
      clearInterval(this.timeCount)
      // 開始獲取新的token
      this.$ajax({
        method: 'post',
        url: this.tokenApi // 獲取口令的API
      }).then((response) => {
        // 保存token，改變場景，顯示登錄碼，開始輪詢
        this.authToken = response.data.data
        this.state = 1 // 場景為有登錄碼
        this.tip = '請使用手機口令掃碼登錄'
        this.imgURL = this.tokenImgApi + response.data.data // 拼裝獲得登錄碼鏈接
        this.timeCount = setInterval(this.getTokenInfo, 1000) // 開啟每隔1S的輪詢，向服務器請求App資訊
      }).catch((error) => {
        console.log(error)
        this.getToken()
      })
    },
    getTokenInfo () {
      // 登錄碼有效時間減少
      this.count--
      // 登錄碼到期，改變場景
      if (this.count === 0) {
        this.state = 3 // 場景為登錄碼過期
        this.tip = '二維碼已過期，請刷新'
      }
      // 防止計數溢出
      if (this.count < -1000) {
        this.count = -1
      }
      // 輪詢查詢token狀態
      this.$ajax({
        method: 'post',
        url: this.tokenInfoApi + this.authToken // 拼裝獲得口令信息API
      }).then((response) => {
        let auth = response.data.data
        // token狀態為登錄成功
        if (auth.authState === 1) {
          this.$message({
            message: '登錄成功！ ',
            type: 'success'
          })
          clearInterval(this.timeCount) // 關閉輪詢，溜了
          // token狀態為正在登陸，改變場景，請求掃碼用戶信息
        } else if (auth.authState === 2) {
          this.userId = auth.userId
          this.getUserInfo()
          this.state = 2
          this.tip = '掃碼成功，請在手機上確認'
          // token狀態為過期（服務器），改變場景
        } else if (auth.authState === 3) {
          this.state = 3
          this.tip = '二維碼已過期，請刷新'
        }
      }).catch((error) => {
        console.log(error)
      })
    },
    getUserInfo () {
      this.$ajax({
        method: 'post',
        url: this.userInfoApi,
        data: this.qs.stringify({
          userId: this.userId
        })
      }).then((response) => {
        // 獲取用戶信息，並進行顯示
        this.userName = response.data.data.userName
        this.userAvatar = response.data.data.userAvatar
        console.log(response.data.data)
      }).catch((error) => {
        console.log(error)
      })
    }
  }
}
</script>

<style scoped>
  /*登錄框*/
  .login_box {
    z-index: 99;
    position: absolute;
    width: 380px;
    height: 540px;
    top: 50%;
    left: 50%;
    margin-left: -190px;
    margin-top: -270px;
    border-radius: 6px;
    background-color: #fff;
    box-shadow: 0 2px 10px #999;
  }

  .login_close {
    position: absolute;
    top: 0;
    right: 0;
    width: 64px;
    height: 64px;
    background: url(../assets/img/pcinput.png) no-repeat right top;
    background-size: 100% 100%;
    border-top-right-radius: 5px;
    cursor: pointer;
    z-index: 99;
  }
  /*二維碼*/
  .qrcode {
    position: relative;
    text-align: center;
  }

  /* 二維碼獲取 */
  .qrcode .img {
    display: block;
    width: 240px;
    height: 240px;
    margin: 70px auto 25px;
  }

  .qrcode .sub_title {
    text-align: center;
    font-size: 19px;
    color: #353535;
    margin-bottom: 23px;
  }

  .qrcode .sub_desc {
    text-align: center;
    color: #a3a3a3;
    font-size: 14px;
    padding: 0 40px;
    line-height: 1.8;
  }

  /* 二維碼為空 */
  .qrcode .empty {
    display: block;
    width: 240px;
    height: 240px;
    margin: 70px auto 25px;
    background: #d7e8fc;
  }

  /* 二維碼刷新 */
  .qrcode .refresh {
    display: block;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 240px;
  }

  .qrcode .refresh .refresh_mask {
    position: absolute;
    left: 50%;
    top: 50%;
    margin-left: -120px;
    margin-top: -120px;
    width: 240px;
    height: 240px;
    background: #ffffffe0;
  }

  .qrcode .refresh .refresh_icon {
    position: absolute;
    left: 50%;
    top: 50%;
    margin-left: -48px;
    margin-top: -48px;
    height: 96px;
    width: 96px;
    cursor: pointer;
    background: url(../assets/img/refresh.png) no-repeat;
  }

  .qrcode .refresh .refresh_icon:hover {
    animation: refresh 1s linear infinite;
  }

  @keyframes refresh {
    0% {
      transform: rotate(0deg);
    }

    100% {
      transform: rotate(360deg);
    }
  }

  /* 二維碼結果 */
  .qrcode .result {
    display: block;
    width: 240px;
    height: 240px;
    margin: 70px auto 25px;
  }

  .qrcode .result .u_avatar {
    height: 150px;
    width: 150px;
    margin-top: 15px;
    border-radius: 5px;
    box-shadow: 0 2px 10px #999;
    -moz-box-shadow: #999 0 2px 10px;
    -webkit-box-shadow: #999 0 2px 10px;
  }

  .qrcode .result .u_name {
    text-align: center;
    font-size: 19px;
    color: #353535;
    margin-top: 20px;
  }
</style>
