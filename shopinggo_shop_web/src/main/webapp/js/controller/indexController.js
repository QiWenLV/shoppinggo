 //控制层 
app.controller('indexController' ,function($scope, loginService){

    //读取列表数据绑定到表单中  
	$scope.showLoginName=function(){
        loginService.loginName().success(
			function(response){
				$scope.loginUserName=response.loginName;
			}			
		);
	}    
	

});	
