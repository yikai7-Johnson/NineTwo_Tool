import Message from 'tdesign-miniprogram/message/index';
import baseURL from '../../../utils/baseURL';

Component({
  data: {
    // host: 'https://your-domain.com:8090',
    host: baseURL,
    fileList: [],
    imageBase64: "",

    style: 'border-radius: 12rpx;',
    msgVisible: false,
    imageStr: ''
  },
  methods: {
    // 图片上传组件 - 添加图片
    handleAdd(e) {
      const {
        fileList
      } = this.data;
      const {
        files
      } = e.detail;
      // 方法1：选择完所有图片之后，统一上传，因此选择完就直接展示
      this.setData({
        fileList: [...fileList, ...files], // 此时设置了 fileList 之后才会展示选择的图片
      });
    
    },
    // 图片上传组件 - 删除
    handleRemove(e) {
      this.setData({
        fileList: [],
      });
      this.setData({
        imageBase64: '',
        imageStr: ''
      });
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
    onUpload(file) {
      this.showTextMessage('服务器配置低，暂停服务')
        return
      const {
        fileList
      } = this.data;
      if (fileList.length == 0) {
        this.showTextMessage('请先选择图片')
        return
      }
      this.setData({
        fileList: [...fileList, {
          ...file,
          status: 'loading'
        }],
      });
      const {
        length
      } = fileList;

      console.log("图片上传：" + this.data.host);
      wx.showLoading({
        title: '加载中',
        mask: true
      });
      const task = wx.uploadFile({
        url: this.data.host + '/image/ocr', // 仅为示例，非真实的接口地址
        filePath: fileList[0].url,
        name: 'file',
        formData: {},
        success: (res) => {
          wx.hideLoading();
          let data = JSON.parse(res.data)
          let a = data.strRes.replace(/\\n/,'\n')
          console.log(a);
          this.setData({
            [`fileList[${length}].status`]: 'done',
            'imageBase64': this.data.host + '/image/previewImage/' + data.base64,
            'imageStr': a
          });
        },fail: ()=> {
          wx.hideLoading();
          this.showTextMessage('请求出错！')
        }
      });
      task.onProgressUpdate((res) => {
        this.setData({
          [`fileList[${length}].percent`]: res.progress,
        });
      });
    },
    
    // 压缩后 - 预览
    onClick() {
      if (this.data.imageBase64) {
        wx.previewImage({
          current: this.data.imageBase64, // 当前显示的图片链接
          urls: [this.data.imageBase64] // 需要预览的图片链接列表
        })
      }
    },
  },
});