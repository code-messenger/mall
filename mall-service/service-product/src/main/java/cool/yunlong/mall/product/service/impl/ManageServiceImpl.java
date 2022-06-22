package cool.yunlong.mall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cool.yunlong.mall.common.cache.MallCache;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.mapper.*;
import cool.yunlong.mall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yunlong
 * @since 2022-6-11 15:44
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category1_id", category1Id);
        return baseCategory2Mapper.selectList(queryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category2_id", category2Id);
        return baseCategory3Mapper.selectList(queryWrapper);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    /**
     * 保存/修改平台属性
     *
     * @param baseAttrInfo 平台属性数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   // 如果不写 rollbackFor = Exception.class , 则只会回滚 RuntimeException 异常
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 通过 id 判断 是新增还是修改; id 为空则新增 否则修改;
        if (baseAttrInfo.getId() == null) {
            // 保存平台属性数据
            baseAttrInfoMapper.insert(baseAttrInfo);
        } else {
            // 修改平台属性数据
            baseAttrInfoMapper.updateById(baseAttrInfo);
            // 修改平台属性值数据 (删除原有数据, 再添加新数据)
            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(queryWrapper);
        }

        // 获取平台属性值数据集合
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        // 循环保存平台属性值数据
        if (!CollectionUtils.isEmpty(attrValueList)) {
            attrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }

    /**
     * 根据平台属性id 查询平台属性值数据
     *
     * @param attrId 平台属性id
     * @return 平台属性值数据
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id", attrId);
        return baseAttrValueMapper.selectList(queryWrapper);
    }

    /**
     * 根据平台属性 id 获取平台属性信息
     *
     * @param id 平台属性id
     * @return 平台属性信息
     */
    @Override
    public BaseAttrInfo getBaseAttrInfo(Long id) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(id);
        if (baseAttrInfo != null) {
            // 获取平台属性值数据集合  base_attr_info.id = base_attr_value.attr_id
            List<BaseAttrValue> attrValueList = getAttrValueList(id);
            // 设置平台属性值数据集合
            baseAttrInfo.setAttrValueList(attrValueList);
        }
        return baseAttrInfo;
    }

    /**
     * 获取所有分类信息
     *
     * @return 所有分类信息
     */
    @Override
    @MallCache(prefix = "categoryList")
    public List<JSONObject> getBaseCategoryList() {
        // 创建一个返回对象
        List<JSONObject> list = new ArrayList<>();

        // 获取所有分类数据集合
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        // 遍历集合并根据一级分类id进行分组
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList
                .stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        int index = 1;
        // 获取一级分类下的所有数据
        for (Map.Entry<Long, List<BaseCategoryView>> entry1 : category1Map.entrySet()) {
            // 获取一级分类Id
            Long category1Id = entry1.getKey();
            // 获取一级分类下面的所有集合
            List<BaseCategoryView> category2List1 = entry1.getValue();
            //
            JSONObject category1 = new JSONObject();
            category1.put("index", index);
            category1.put("categoryId", category1Id);
            // 一级分类名称
            category1.put("categoryName", category2List1.get(0).getCategory1Name());
            // 变量迭代
            index++;
            // 循环获取二级分类数据
            Map<Long, List<BaseCategoryView>> category2Map = category2List1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            // 声明二级分类对象集合
            List<JSONObject> category2Child = new ArrayList<>();
            // 循环遍历
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
                // 获取二级分类Id
                Long category2Id = entry2.getKey();
                // 获取二级分类下的所有集合
                List<BaseCategoryView> category3List = entry2.getValue();
                // 声明二级分类对象
                JSONObject category2 = new JSONObject();

                category2.put("categoryId", category2Id);
                category2.put("categoryName", category3List.get(0).getCategory2Name());
                // 添加到二级分类集合
                category2Child.add(category2);

                List<JSONObject> category3Child = new ArrayList<>();

                // 循环三级分类数据
                category3List.forEach(category3View -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", category3View.getCategory3Id());
                    category3.put("categoryName", category3View.getCategory3Name());

                    category3Child.add(category3);
                });

                // 将三级数据放入二级里面
                category2.put("categoryChild", category3Child);
            }
            // 将二级数据放入一级里面
            category1.put("categoryChild", category2Child);
            list.add(category1);
        }


        return list;
    }

}
