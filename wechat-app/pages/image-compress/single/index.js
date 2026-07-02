import Message from 'tdesign-miniprogram/message/index';
import baseURL from '../../../utils/baseURL';

Component({
  data: {
    // host: 'https://your-domain.com:8090',
    host: baseURL,
    fileList: [],
    imageBase64: "",
    imgWidth: '',
    imgHeight: '',
    imgSize: '',

    style: 'border-radius: 12rpx;',

    imgSuffixText: '',
    imgSuffixValue: [''],
    imgSuffixVisible: false,
    imgSuffixArr: [{
        label: 'png',
        value: 'png',
      },
      {
        label: 'jpg',
        value: 'jpg',
      },
      {
        label: 'jpeg',
        value: 'jpeg',
      },
    ],

    cityText: '1.0',
    cityValue: ['1.0'],
    cityVisible: false,
    citys: [{
        label: '1.0',
        value: '1.0',
      },
      {
        label: '0.9',
        value: '0.9',
      },
      {
        label: '0.8',
        value: '0.8'
      },
      {
        label: '0.7',
        value: '0.7'
      },
      {
        label: '0.6',
        value: '0.6'
      },
      {
        label: '0.5',
        value: '0.5'
      },
      {
        label: '0.4',
        value: '0.4'
      },
      {
        label: '0.3',
        value: '0.3'
      },
      {
        label: '0.2',
        value: '0.2'
      },
      {
        label: '0.1',
        value: '0.1'
      },
    ],

    visible: false,
    showIndex: false,
    closeBtn: false,
    deleteBtn: false,
    images: [],

    psfWidth: '',
    psfHeight: '',
    psfSize: '',
    psfType: '',

    msgVisible: false
  },
  methods: {
    onColumnChange(e) {
      console.log('picker pick:', e);
    },

    onPickerChange(e) {
      const {
        key
      } = e.currentTarget.dataset;
      const {
        value
      } = e.detail;
      this.setData({
        [`${key}Visible`]: false,
        [`${key}Value`]: value,
        [`${key}Text`]: value.join(' '),
      });
      console.log(this.data);
    },

    onPickerCancel(e) {
      const {
        key
      } = e.currentTarget.dataset;
      this.setData({
        [`${key}Visible`]: false,
      });
    },

    onCityPicker() {
      this.setData({
        cityVisible: true
      });
    },
    onImgSuffixPicker() {
      this.setData({
        imgSuffixVisible: true
      });
    },


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
      this.setData({
        'imgSize': Math.round(files[0].size / 1024) + ' KB'
      })
      const _this = this
      wx.getImageInfo({
        src: files[0].url,
        success(res) {
          _this.setData({
            'imgWidth': res.width,
            'imgHeight': res.height,
            'imgSuffixText': res.type.toLocaleLowerCase()
          });
        }
      })
      // 方法2：每次选择图片都上传，展示每次上传图片的进度
      // files.forEach(file => this.uploadFile(file))
    },
    // 图片上传组件 - 删除
    handleRemove(e) {
      // const {
      //   index
      // } = e.detail;
      // const {
      //   fileList
      // } = this.data;
      // fileList.splice(index, 1);
      this.setData({
        fileList: [],
      });
      this.setData({
        imgSize: '',
        imgWidth: '',
        imgHeight: '',
        imgSuffixText: '',
        imgSuffixValue: [],
        cityText: '1.0',
        cityValue: ['1.0'],
        imageBase64: '',
        psfSize: '',
        psfType: '',
        psfHeight: '',
        psfWidth: ''
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

      const _this = this
      console.log("图片上传：" + this.data.host);
      wx.showLoading({
        title: '加载中',
        mask: true
      });
      const task = wx.uploadFile({
        url: this.data.host + '/image/compress', // 仅为示例，非真实的接口地址
        filePath: fileList[0].url,
        name: 'file',
        formData: {
          "height": this.data.imgHeight,
          "quality": this.data.cityText,
          "suffix": this.data.imgSuffixText,
          "width": this.data.imgWidth
        },
        success: (res) => {
          wx.hideLoading();
          let data = JSON.parse(res.data)

          wx.getImageInfo({
            src: this.data.host + '/image/previewImage/' + data.name,
            success(res) {
              _this.setData({
                'psfWidth': res.width,
                'psfHeight': res.height,
                'psfType': res.type
              });
            }
          })

          this.setData({
            [`fileList[${length}].status`]: 'done',
            'imageBase64': this.data.host + '/image/previewImage/' + data.name,
            'psfSize': Math.round(data.size / 1024) + ' KB'
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
    //
    handleInputChange(e){
      // 取出实时的变量值
      let value = e.detail.value;
      let fieldName = e.target.dataset.fieldName;
      this.setData({
        [`${fieldName}`]: value
      })
      console.log('绑定的输入框：',[`${fieldName}`],'value值:',value)
    },
  },
});