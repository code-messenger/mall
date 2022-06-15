package cool.yunlong.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cool.yunlong.mall.model.product.BaseTrademark;
import cool.yunlong.mall.product.mapper.BaseTrademarkMapper;
import cool.yunlong.mall.product.service.BaseTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yunlong
 * @since 2022/6/13 10:08
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 分页查询品牌列表
     *
     * @param pageParam 分页参数
     * @return 品牌列表
     */
    @Override
    public IPage<BaseTrademark> getPage(Page<BaseTrademark> pageParam) {
        QueryWrapper<BaseTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        return baseTrademarkMapper.selectPage(pageParam, queryWrapper);
    }

}

