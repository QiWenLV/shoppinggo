 //控制层 
app.controller('goodsController' ,function($scope,$controller, $location   ,goodsService, uploadService, itemCatService, typeTemplateService){
	
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
    //定义组合实体类
    $scope.entity={goods:{}, goodsDesc:{itemImages:[], specificationItems:[]}};

	//查询实体 
	$scope.findOne=function(){
		//获取页面所有的参数，数组
        var id = $location.search()['id'];
        if(id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response) {
                $scope.entity = response;
                //给富文本编辑器赋值
                editor.html($scope.entity.goodsDesc.introduction);//商品介绍

                //图片，json字符串转对象
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

                //扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格选择
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                //SKU
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }

		);				
	}
	
	//增加
	$scope.save=function(){

	    $scope.entity.goodsDesc.introduction=editor.html();
        var serviceObjec;   //服务层对象

        //有ID表示修改
        if($scope.entity.goods.id != null){
            serviceObjec = goodsService.update($scope.entity);  //修改
        }else{
            serviceObjec = goodsService.add($scope.entity); //增加
        }

        serviceObjec.success(
			function(response){
				if(response.success){
					alert("商品保存成功");
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



    //保存上传的图片字段
    $scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //从列表删除图片
    $scope.remove_image_entity=function(index){
        uploadService.deleteFile($scope.image_entity.imageUrl).success(
        	function (response) {
                if(response.success){
                    $scope.entity.goodsDesc.itemImages.splice(index,1);
                }else{
                    alert(response.message);
                }
            }
		)
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
                $scope.itemCat3List = {};
                $scope.entity.goods.typeTemplateId = "无"
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
                $scope.entity.goods.typeTemplateId = "无"
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
        if(newValue == null){
            return;
        }
        typeTemplateService.findOne(newValue).success(
            function (response) {
              	$scope.typeTemplateId = response;
              	//将字符串转化为JSON
              	$scope.typeTemplateId.brandIds = JSON.parse($scope.typeTemplateId.brandIds);
              	//查询扩展属性
                // $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplateId.customAttributeItems);
                //扩展属性
                if( $location.search()['id']==null ){//如果是增加商品
                    $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.typeTemplateId.customAttributeItems);
                }
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


    $scope.status=['未审核','已审核','审核未通过','已关闭'];

    $scope.itemCatList=[];//商品分类列表
    //查询商品分类列表
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(
			function (response) {
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
		)
    }
    //返回规格选择框的勾选状态
	$scope.checkAttributeValue=function (specName, optionName) {

        var items = $scope.entity.goodsDesc.specificationItems;

        if($scope.entity.goods.isEnableSpec == 1){
            var obj = $scope.searchObjectByKey(items, 'attributeName', specName);

            if(obj != null){
                if(obj.attributeValue.indexOf(optionName) >= 0){		//如果你能查到
                    // createItemList();
                    return true;
                }else {
                    return false;
                }

            }else {
                return false;
            }
        }else{
            return false;
        }
    }

    $scope.dataAdapter=function () {

    }
});
