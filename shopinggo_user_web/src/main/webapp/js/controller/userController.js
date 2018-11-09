 //控制层 
app.controller('userController' ,function($scope,$controller ,userService){

    //注册
    $scope.reg=function () {

        if($scope.password != $scope.entity.password){
            alert("两次输入密码不一致，请重新输入");
            $scope.password = "";
            return;
        }
        
        userService.add($scope.entity).success(
            function () {
                
            }
        );
        
    }


});
