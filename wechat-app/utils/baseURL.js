/* eslint-disable */
const accountInfo = wx.getAccountInfoSync()
let baseURL = "";
var envVersion = accountInfo.miniProgram.envVersion;
switch (envVersion) {
  case 'develop'://开发版
    baseURL = "http://localhost:8090";
    break;
  case 'trial'://体验版
    baseURL = "https://your-domain.com:8090";
    break;
  case 'release'://正式版
    baseURL = "https://your-domain.com:8090";
    break;
  default:
    baseURL = "";
    break;
}
export default baseURL;
