package cool.yunlong.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cool.yunlong.mall.model.product.*;
import cool.yunlong.mall.product.mapper.*;
import cool.yunlong.mall.product.service.SpuManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author yunlong
 * @since 2022/6/13 10:27
 */
@Service
public class SpuManageServiceImpl implements SpuManageService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;


    /**
     * 分页查询 spu 列表
     *
     * @param pageInfo 分页信息
     * @param spuInfo  spu 数据
     * @return 分页结果
     */
    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageInfo, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", spuInfo.getCategory3Id());
        queryWrapper.orderByDesc("id");
        return spuInfoMapper.selectPage(pageInfo, queryWrapper);
    }

    /**
     * 查询所有销售属性
     *
     * @return 销售属性列表
     */
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 保存 spu
     *
     * @param spuInfo spu 信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 1. 保存 spu 信息
        spuInfoMapper.insert(spuInfo);

        saveList(spuInfo);
    }

    private void saveList(SpuInfo spuInfo) {
        // 2. 保存 spu 图片信息
        // 获取 spu Image 列表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        // 循环保存 spu Image
        if (!CollectionUtils.isEmpty(spuImageList)) {
            spuImageList.forEach(spuImage -> {
                // 设置 spu id
                spuImage.setSpuId(spuInfo.getId());
                // 保存 spu Image
                spuImageMapper.insert(spuImage);
            });
        }

        // 3. 保存 spu 销售属性信息
        //  获取销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //  判断
        if (!CollectionUtils.isEmpty(spuSaleAttrList)) {
            //  循环遍历
            spuSaleAttrList.forEach(spuSaleAttr -> {
                // 设置SpuId
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                // 添加Spu属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)) {
                    spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                        // 设置SpuId
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        // 将spuSaleAttr放入
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    });
                }
            });
        }

        // 4. 保存 spu 商品海报信息
        // 获取 spu 商品海报列表
        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        // 循环保存 spu 商品海报
        if (!CollectionUtils.isEmpty(spuPosterList)) {
            spuPosterList.forEach(spuPoster -> {
                // 设置 spu id
                spuPoster.setSpuId(spuInfo.getId());
                // 保存 spu 商品海报
                spuPosterMapper.insert(spuPoster);
            });
        }
    }

    /**
     * 根据 spuId 查询 spu 图片列表
     *
     * @param spuId spu 编号
     * @return spu 图片列表
     */
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(queryWrapper);
    }

    /**
     * 根据 spuId 查询 spu 销售属性
     *
     * @param spuId spu 编号
     * @return spu 销售属性
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    /**
     * 根据spuId查询spu的销售属性值信息
     *
     * @param spuId spu编号
     * @return 销售属性值信息
     */
    public List<SpuSaleAttrValue> getSpuSaleAttrValueList(Long spuId) {
        QueryWrapper<SpuSaleAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        return spuSaleAttrValueMapper.selectList(queryWrapper);
    }


    /**
     * 根据spuId查询spu的海报
     *
     * @param spuId spu编号
     * @return spu的海报
     */
    @Override
    public List<SpuPoster> getSpuPosterBySpuId(Long spuId) {
        // select * from spu_poster where spu_id = #{spuId} and is_delete = 0
        QueryWrapper<SpuPoster> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        return spuPosterMapper.selectList(queryWrapper);
    }

    /**
     * 更新 spu
     *
     * @param spuInfo spu 信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSpuInfo(SpuInfo spuInfo) {
        // 1. 先删除 spu 图片信息
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuInfo.getId());
        spuImageMapper.delete(queryWrapper);
        // 2. 删除 spu 销售属性信息
        QueryWrapper<SpuSaleAttr> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("spu_id", spuInfo.getId());
        spuSaleAttrMapper.delete(queryWrapper2);
        // 3. 删除 spu 销售属性值信息
        QueryWrapper<SpuSaleAttrValue> queryWrapper4 = new QueryWrapper<>();
        queryWrapper4.eq("spu_id", spuInfo.getId());
        spuSaleAttrValueMapper.delete(queryWrapper4);

        // 4. 删除 spu 商品海报信息
        QueryWrapper<SpuPoster> queryWrapper3 = new QueryWrapper<>();
        queryWrapper3.eq("spu_id", spuInfo.getId());
        spuPosterMapper.delete(queryWrapper3);
        // 5. 保存 spu 信息
        spuInfoMapper.updateById(spuInfo);
        saveList(spuInfo);
    }

    /**
     * 根据spuId查询spu的信息
     *
     * @param spuId spu编号
     * @return 销售属性信息
     */
    @Override
    public SpuInfo getSpuInfo(Long spuId) {
        SpuInfo spuInfo = spuInfoMapper.selectById(spuId);
        // 获取 spu 图片列表
        List<SpuImage> spuImageList = getSpuImageList(spuId);
        spuInfo.setSpuImageList(spuImageList);

        // 获取 spu 销售属性列表
        List<SpuSaleAttr> spuSaleAttrList = getSpuSaleAttrList(spuId);
        spuInfo.setSpuSaleAttrList(spuSaleAttrList);

        // 获取 spu 商品海报列表
        List<SpuPoster> spuPosterList = getSpuPosterBySpuId(spuId);
        spuInfo.setSpuPosterList(spuPosterList);
        return spuInfo;
    }
}
