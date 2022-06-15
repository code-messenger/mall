package cool.yunlong.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.mapper.*;
import cool.yunlong.mall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
    private SpuInfoMapper spuInfoMapper;

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

}
