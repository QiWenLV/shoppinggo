 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService, uploadService, itemCatService, typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//增加
	$scope.add=function(){

	    $scope.entity.goodsDesc.introduction=editor.html();

        goodsService.add($scope.entity).success(
			function(response){
				if(response.success){

					alert("商品添加成功");
					//清空
					$scope.entity = {};
                    editor.html("");    //清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//上传图片
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(
			function (response) {
                if(response.success){
                    $scope.image_entity.imageUrl = response.message;
                }else{
                    alert(response.message);
                }

            }
		)
    }

    //定义组合实体类
    $scope.entity={goods:{}, goodsDesc:{itemImages:[], specificationItems:[]}};

    //保存上传的图片字段
    $scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //从列表删除图片
    $scope.remove_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }


    $scope.gread = 0;

    // $scope.listenList={
     //    a:$scope.entity.goods.category1Id,
    	// b:$scope.entity.goods.category2Id
	// }
    //查询一级分类
	$scope.selectItemCat1List=function () {
        itemCatService.findByParentId(0).success(
        	function (response) {
                $scope.itemCat1List = response;
                $scope.itemCat3List = {};
            }
		)
    }

    //查询二级分类
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {

        if(newValue == null){
        	return;
		}
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        )
    });
    //查询三级分类
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if(newValue == null){
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    },true);
    //查询模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if(newValue == null){
            return;
        }
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId = response.typeId;
            }
		)
    });
    //根据模板ID查询各种信息
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
              	$scope.typeTemplateId = response;
              	//将字符串转化为JSON
              	$scope.typeTemplateId.brandIds = JSON.parse($scope.typeTemplateId.brandIds);
              	//查询扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplateId.customAttributeItems);
            }
        );
        //查询规格
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response
            }
        )

    });


    $scope.updateSpecAttribute=function($event,name,value){
        //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]

		//已经有条目，则获得条目，没有则为null
		var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
		if(obj != null){
			if($event.target.checked){
				obj.attributeValue.push(value);
			}else {
				var index = obj.attributeValue.indexOf(obj);   //查找值的位置
                obj.attributeValue.splice(index, 1);  //参数1，移除的位置。参数2，移除的个数

				//如果选项都取消了，将一条记录都移除
				if(obj.attributeValue.length == 0){
                    $scope.entity.goodsDesc.specificationItems.splice(
                    	$scope.entity.goodsDesc.specificationItems.indexOf(obj),
						1);
				}
			}
		}else {
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name, "attributeValue":[value]});
		}
    };

    //创建SKU列表
    $scope.createItemList=function(){
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'} ];//列表初始化

		var items = $scope.entity.goodsDesc.specificationItems;
		//循环取出设置数据[{"attributeName":"机身内存","attributeValue":["64G","16G"]},{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}]
        for(var i=0; i<items.length; i++){
        	//每个条目的数据交给addColumn方法，进行交叉组合
            $scope.entity.itemList= addColumn($scope.entity.itemList, items[i].attributeName,items[i].attributeValue );
        }
	};

    addColumn = function (list, columnName, columnValues) {
		var newList=[];
        //遍历现有的行{spec:{columnName:'网络', columnValues},price:0,num:99999,status:'0',isDefault:'0'}
		for(var i=0; i<list.length; i++){
            var oldRow = list[i];	//保存旧的数据行

			//遍历规格选项，交叉组合"attributeValue":["移动3G","移动4G"]
            for(var j=0; j<columnValues.length; j++){

                var newRow = JSON.parse(JSON.stringify(oldRow));	//深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
		}

		return newList;
    }


});
