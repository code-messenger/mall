package cool.yunlong.mall.product.service;

import com.alibaba.fastjson.JSONObject;
import cool.yunlong.mall.model.product.*;

import java.util.List;

public interface ManageService {

    /**
     * 查询所有的一级分类信息
     *
     * @return 一级分类数据
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级分类id 查询二级分类数据
     *
     * @param category1Id 一级分类id
     * @return 二级分类数据
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级分类id 查询三级分类数据
     *
     * @param category2Id 二级分类id
     * @return 三级分类数据
     */
    List<BaseCategory3> getCategory3(Long category2Id);


    /**
     * 根据分类Id 获取平台属性数据
     * 接口说明：
     * 1，平台属性可以挂在一级分类、二级分类和三级分类
     * 2，查询一级分类下面的平台属性，传：category1Id，0，0；   取出该分类的平台属性
     * 3，查询二级分类下面的平台属性，传：category1Id，category2Id，0；
     * 取出对应一级分类下面的平台属性与二级分类对应的平台属性
     * 4，查询三级分类下面的平台属性，传：category1Id，category2Id，category3Id；
     * 取出对应一级分类、二级分类与三级分类对应的平台属性
     *
     * @param category1Id 一级分类id
     * @param category2Id 二级分类id
     * @param category3Id 三级分类id
     * @return 平台属性数据
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存/修改平台属性
     *
     * @param baseAttrInfo 平台属性数据
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据平台属性id 查询平台属性值数据
     *
     * @param attrId 平台属性id
     * @return 平台属性值数据
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 根据平台属性 id 获取平台属性信息
     *
     * @param attrId 平台属性id
     * @return 平台属性信息
     */
    BaseAttrInfo getBaseAttrInfo(Long attrId);

    /**
     * 获取所有分类信息
     *
     * @return 所有分类信息
     */
    List<JSONObject> getBaseCategoryList();
}

