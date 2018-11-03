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


});
