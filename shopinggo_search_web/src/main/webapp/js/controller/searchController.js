 //控制层 
app.controller('searchController' ,function($scope,$controller, $location   ,searchService){
	
	$controller('baseController',{$scope:$scope});//继承

    //搜索
    $scope.search=function(){
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);    //将页码转换为数字

        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                //如果搜索关键字中包含品牌，则给品牌条件赋值
                keywordsIsBrand();

                //构建分页栏的页码数组
                buildPageLabel();
            }
        );
    }

    buildPageLabel=function () {
        //构建分页栏的页码数组
        $scope.pageLabel = [];

        var firstPage = 1;  //显示的开始页码
        var lastPage = $scope.resultMap.totalPages; //显示的截止页码

        $scope.firstDot = true; //前面有点
        $scope.lastDot = true; //后面有点

        if($scope.resultMap.totalPages > 6){    //如果页码数量大于5

            if($scope.searchMap.pageNo <= 3){   //当前页码小于等于3，显示前5页
                lastPage = 5;
                $scope.firstDot = false; //前面没点
            }else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages-2){
                firstPage = $scope.resultMap.totalPages - 4;
                $scope.lastDot = false; //后面没点
            }else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage =  $scope.searchMap.pageNo + 2;
            }
        }else{
            $scope.firstDot = false; //前面没点
            $scope.lastDot = false; //后面没点
        }

        for(var i=firstPage; i <= lastPage; i++){
            $scope.pageLabel.push(i);
        }
    };

    // //定义搜索条件对象的结构
    // $scope.searchMap={
    //     "keywords":"",
    //     "category":"",
    //     "brand":"",
    //     "price":"",
    //     "spec":{},
    //     "pageNo":1,
    //     "pageSize":30,
    //     "sort":"",
    //     "sortField":""
    // };

    //初始化方法
    $scope.searchMapInit = function (keywords) {
        //定义搜索条件对象的结构
        $scope.searchMap={
            "keywords":keywords,
            "category":"",
            "brand":"",
            "price":"",
            "spec":{},
            "pageNo":1,
            "pageSize":30,
            "sort":"",
            "sortField":""
        };
    };



    //添加搜索项
    $scope.addSearchItem=function (key, value) {

        //如果用户点击的是分类或者品牌或者价格
        if(key=="category" || key=="brand" || key=='price'){
            $scope.searchMap[key] = value;

        }else{  //否则用户点的一定是规格
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();    //查询
    }

    //移除已选条件
    $scope.removeSearchItem=function (key) {
        //如果用户点击的是分类或者品牌
        if(key=="category" || key=="brand" || key=='price'){
            $scope.searchMap[key] = "";

        }else{  //否则用户点的一定是规格
            //删除key
            delete $scope.searchMap.spec[key];
        }
        $scope.search();    //查询
    }

    //分页查询
    $scope.queryByPage=function (pageNO) {
        if(pageNO < 1 || pageNO > $scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo = pageNO;
        $scope.search();
    }

    //判断当前页是否是第一页
    $scope.isTopPage=function () {
        if($scope.searchMap.pageNo == 1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否是最后一页
    $scope.isEndPage=function () {
        if($scope.searchMap.pageNo == $scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }

    //排序查询
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;

        $scope.search();    //查询
    }

    keywordsIsBrand=function () {
        if($scope.resultMap.brandList != null){
            for(var i=0; i<$scope.resultMap.brandList.length; i++){
                if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
                    $scope.searchMap.brand = $scope.resultMap.brandList[i].text;
                }
            }
        }
    }

    //自动加载关键字
    $scope.loadKeywords=function () {
        $scope.searchMapInit($location.search()['keywords']);

        $scope.search();
    }


});
