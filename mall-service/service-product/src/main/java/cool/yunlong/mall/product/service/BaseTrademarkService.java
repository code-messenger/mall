package cool.yunlong.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cool.yunlong.mall.model.product.BaseTrademark;

/**
 * @author yunlong
 * @since 2022/6/13 10:08
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {

    /**
     * 分页查询品牌列表
     *
     * @param pageParam 分页参数
     * @return 品牌列表
     */
    IPage<BaseTrademark> getPage(Page<BaseTrademark> pageParam);
}
