 //控制层 
app.controller('contentController' ,function($scope,$controller   ,contentService){
	
	$controller('baseController',{$scope:$scope});//继承

    $scope.contentList=[];  //广告列表

    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
        	function (response) {
                $scope.contentList[categoryId] = response;
            }
		)
    }
});
