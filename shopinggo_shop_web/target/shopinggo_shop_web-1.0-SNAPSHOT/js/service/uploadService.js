//服务层
app.service('uploadService',function($http){

    //上传图片
    this.uploadFile=function(){
        //封装文件(图片)的二进制载体
        var formData = new FormData();
        //文件上传框的name必须是file
        formData.append("file", file.files[0]);
        return $http({
            url : '../upload.do',
            method : 'post',
            data : formData,
            headers : {'Content-Type' : undefined},
            transformRequest: angular.identity
        });
        //第四行，头信息
        //第五行，表单序列化，固定写法
    }

});
