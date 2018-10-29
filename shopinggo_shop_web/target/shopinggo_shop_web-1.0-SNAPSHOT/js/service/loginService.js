//服务层
app.service('loginService',function($http){
	    	
	//获取用户名
	this.loginName=function(){
		return $http.get('../login/name.do');
	}

});
