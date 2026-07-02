import Message from 'tdesign-miniprogram/message/index';
import baseURL from '../../../utils/baseURL';

Component({
  data: {
    host: baseURL,
    uploadFileName: "",

    pdfFilePath: null,

    style: 'border-radius: 12rpx;',
    msgVisible: false,
    filename: '',
    oldname: ''
  },
  methods: {
    onUploadFile() {
      let _this = this
      wx.chooseMessageFile({
        count: 1,
        type: 'file',
        extension: ['pdf','PDF'],
        success (res) {
          // tempFilePath可以作为img标签的src属性显示图片
          const tempFilePaths = res.tempFiles
          _this.setData({
            'pdfFilePath': tempFilePaths[0].path,
            'uploadFileName': tempFilePaths[0].name,
            'oldname': ''
          });
        }
      })
    },
  
    // 消息提示
    showTextMessage(content) {
      Message.warning({
        context: this,
        offset: [90, 32],
        duration: 5000,
        content: content,
        align: 'center'
      });
    },
    // 图片上传组件 - 上传图片
    onUpload() {
      if (!this.data.pdfFilePath) {
        this.showTextMessage('请先选择文件')
        return
      }
      let _this = this
      wx.showLoading({
        title: '加载中',
        mask: true
      });
      wx.uploadFile({
        url: this.data.host + '/pdf/toword', // 仅为示例，非真实的接口地址
        filePath: this.data.pdfFilePath,
        name: 'file',
        timeout: 6000000,
        formData: {},
        success: (res) => {
          wx.hideLoading();
          let data = JSON.parse(res.data)
          let n = this.data.uploadFileName
          this.setData({
            'filename': data.filename,
            'oldname': n.substring(0, n.length - 4) + '.docx'
          });
        },fail: ()=> {
          wx.hideLoading();
          this.showTextMessage('请求出错！')
        }
      });
    },
    
    // 压缩后 - 预览
    onClick() {
      if (this.data.filename) {
        wx.downloadFile({
          // 示例 url，并非真实存在
          url: this.data.host + '/image/image/' + this.data.filename,
          success: function (res) {
            const filePath = res.tempFilePath
            wx.openDocument({
              filePath: filePath,
              fileType: 'docx',
              showMenu: true,
              success: function (res) {
                console.log('打开文档成功')
              }
            })
          }
        })
      }
    },
  },
});