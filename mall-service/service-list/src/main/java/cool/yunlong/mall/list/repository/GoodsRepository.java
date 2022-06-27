package cool.yunlong.mall.list.repository;

import cool.yunlong.mall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author yunlong
 * @since 2022/6/24 20:59
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {

}
