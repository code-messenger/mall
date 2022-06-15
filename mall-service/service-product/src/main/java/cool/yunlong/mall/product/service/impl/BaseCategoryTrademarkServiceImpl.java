package cool.yunlong.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cool.yunlong.mall.model.product.BaseCategoryTrademark;
import cool.yunlong.mall.model.product.BaseTrademark;
import cool.yunlong.mall.model.product.CategoryTrademarkVo;
import cool.yunlong.mall.product.mapper.BaseCategoryTrademarkMapper;
import cool.yunlong.mall.product.mapper.BaseTrademarkMapper;
import cool.yunlong.mall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yunlong
 * @since 2022/6/13 11:22
 */
@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark>
        implements BaseCategoryTrademarkService {

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 保存分类品牌关联
     *
     * @param categoryTrademarkVo 分类品牌关联
     */
    @Override
    public void saveBaseCategoryTrademark(CategoryTrademarkVo categoryTrademarkVo) {
        // 获取品牌id集合
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();

        if (!CollectionUtils.isEmpty(trademarkIdList)) {
            // 映射到品牌关联表
            List<BaseCategoryTrademark> collect = trademarkIdList.stream().map(trademarkId -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                baseCategoryTrademark.setTrademarkId(trademarkId);
                return baseCategoryTrademark;
            }).collect(Collectors.toList());
            // 批量插入
            this.saveBatch(collect);
        }
    }

    /**
     * 删除分类品牌关联
     *
     * @param category3Id 三级分类id
     * @param trademarkId 品牌id
     */
    @Override
    public void removeBaseCategoryTrademarkById(Long category3Id, Long trademarkId) {
        QueryWrapper<BaseCategoryTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", category3Id);
        queryWrapper.eq("trademark_id", trademarkId);
        baseCategoryTrademarkMapper.delete(queryWrapper);
    }

    /**
     * 根据三级分类id查询品牌列表
     *
     * @param category3Id 三级分类id
     * @return 品牌列表
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        // 先根据三级分类id查询品牌集合
        List<BaseCategoryTrademark> baseCategoryTrademarkList = getBaseCategoryTrademarkList(category3Id);

        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            // 获取品牌id集合
            List<Long> trademarkIdList = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId)
                    .collect(Collectors.toList());
            // 根据品牌id集合查询品牌集合
            return baseTrademarkMapper.selectBatchIds(trademarkIdList);
        }
        return null;
    }

    /**
     * 根据三级分类id查询可选品牌列表
     *
     * @param category3Id 三级分类id
     * @return 可选品牌列表
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        List<BaseCategoryTrademark> baseCategoryTrademarkList = getBaseCategoryTrademarkList(category3Id);

        //  判断
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            //  获取关联品牌id集合
            List<Long> tradeMarkIdList = baseCategoryTrademarkList.stream()
                    .map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            //  查询所有品牌列表
            List<BaseTrademark> baseTrademarks = baseTrademarkMapper.selectList(null);
            //  过滤掉已关联的品牌
            return baseTrademarks.stream()
                    .filter(baseTrademark -> !tradeMarkIdList.contains(baseTrademark.getId()))
                    .collect(Collectors.toList());
        }
        //  如果为空，则查询所有品牌列表
        return baseTrademarkMapper.selectList(null);
    }

    /**
     * 根据三级分类id查询品牌集合
     *
     * @param category3Id 三级分类id
     * @return 品牌集合
     */
    private List<BaseCategoryTrademark> getBaseCategoryTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> baseCategoryTrademarkQueryWrapper = new QueryWrapper<>();
        baseCategoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        return baseCategoryTrademarkMapper.selectList(baseCategoryTrademarkQueryWrapper);
    }
}



