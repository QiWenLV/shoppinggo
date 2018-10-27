app.controller("baseController", function ($scope) {

    //重新加载列表 数据
    $scope.reloadList = function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            //分页插件加载的时候也会自动加载这个方法，不需要再手动初始化
            $scope.reloadList();//重新加载
        }
    };

    $scope.selectIds = [];   //用户勾选的ids集合
    //更新勾选集合
    $scope.updateSelectIds = function ($event, id) {

        if($event.target.checked){
            $scope.selectIds.push(id);     //向集合中添加元素
        }else {
            var index = $scope.selectIds.indexOf(id);   //查找值的位置
            $scope.selectIds.splice(index, 1);  //参数1，移除的位置。参数2，移除的个数
        }
    };
})