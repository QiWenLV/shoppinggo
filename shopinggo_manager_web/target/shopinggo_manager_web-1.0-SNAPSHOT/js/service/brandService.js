//品牌服务
app.service("brandService", function ($http) {
    //查询所有信息
    this.findAll = function () {
        return $http.get('../brand/findAll.do');
    }
    //分页查询
    this.findPage = function (page, size) {
        return $http.get('../brand/findPage.do?page=' + page + '&size=' + size);
    }
    //查询一个
    this.findOne = function (id) {
        return $http.get("../brand/findOne.do?id="+id);
    }
    //添加
    this.add = function (entity) {
        return $http.post("../brand/add.do", entity);
    }
    //修改
    this.update = function (entity) {
        return $http.post("../brand/update.do", entity);
    }
    //批量删除
    this.delete = function (selectIds) {
        return $http.get("../brand/delete.do?ids=" + selectIds);
    }
    //按条件分页查询
    this.search = function (page, size, searchEntity) {
        return $http.post('../brand/search.do?page=' + page + '&size=' + size, searchEntity);
    }
    //下拉列表数据
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do');
    }

});