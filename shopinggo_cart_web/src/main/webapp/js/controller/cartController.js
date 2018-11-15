//购物车控制层
app.controller('cartController',function($scope,cartService){
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
			function(response){
				$scope.cartList=response;
				//求合计数
				$scope.totalValue= cartService.sum($scope.cartList);
			}
		);
	}
	
	//数量加减
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
			function(response){
				if(response.success){//如果成功
					$scope.findCartList();//刷新列表
				}else{
					alert(response.message);
				}				
			}		
		);		
	}
	
	//求合计
	/*
	sum=function(){
		$scope.totalNum=0;//总数量
		$scope.totalMoney=0;//总金额
		
		for(var i=0;i<$scope.cartList.length ;i++){
			var cart=$scope.cartList[i];//购物车对象
			for(var j=0;j<cart.orderItemList.length;j++){
				var orderItem=  cart.orderItemList[j];//购物车明细
				$scope.totalNum+=orderItem.num;//累加数量
				$scope.totalMoney+=orderItem.totalFee;//累加金额				
			}			
		}
	}
	*/

	//获取当前用户的地址列表信息
	$scope.findAddressList=function () {
		cartService.findAddressList().success(
			function (response) {
				$scope.addressList=response;
                for(var i=0;i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
		)
    }

    //查询地址
	$scope.addressList=function (address) {
		$scope.address = address;
    }

    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }
    //当前地址是否选中
    $scope.isSelectedAddress=function (address) {
		if(address==$scope.address){
			return true;
		}else{
			return false;
		}
    }

    $scope.order={paymentType:'1'};//订单对象

    //选择支付类型
    $scope.selectPayType=function(type){
        $scope.order.paymentType=type;
    }

    //提交订单
	$scope.submitOrder=function () {

        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人

		cartService.submitOrder($scope.order).success(
			function (response) {
				if(response.success){
					//页面跳转
					if($scope.order.paymentType == '1'){	//如果是微信支付
						location.href="pay.html";
					}else {	//货到付款
                        location.href="paysuccess.html";
					}
				}else {
                    alert(response.message);
				}
            }
		)
    }

});