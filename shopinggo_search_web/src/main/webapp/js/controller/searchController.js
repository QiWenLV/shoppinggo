 //控制层 
app.controller('searchController' ,function($scope,$controller   ,searchService){
	
	$controller('baseController',{$scope:$scope});//继承

    //搜索
    $scope.search=function(){
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
            }
        );
    }

    //定义搜索条件对象的结构
    $scope.searchMap={
        "keywords":"",
        "category":"",
        "brand":"",
        "spec":{}
    };

    //添加搜索项
    $scope.addSearchItem=function (key, value) {

        //如果用户点击的是分类或者品牌
        if(key=="category" || key=="brand"){
            $scope.searchMap[key] = value;

        }else{  //否则用户点的一定是规格
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();    //查询
    }

    //
    $scope.removeSearchItem=function (key) {
        //如果用户点击的是分类或者品牌
        if(key=="category" || key=="brand"){
            $scope.searchMap[key] = "";

        }else{  //否则用户点的一定是规格
            //删除key
            delete $scope.searchMap.spec[key];
        }
        $scope.search();    //查询
    }




});
