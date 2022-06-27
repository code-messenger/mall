package cool.yunlong.mall.web.controller;

import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.list.client.ListFeignClient;
import cool.yunlong.mall.model.list.SearchParam;
import cool.yunlong.mall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全文检索控制器
 *
 * @author yunlong
 * @since 2022/6/27 15:52
 */
@Controller
public class ListController {

    @Qualifier("cool.yunlong.mall.list.client.ListFeignClient")
    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("list.html")
    public String list(SearchParam searchParam, Model model) {

        Result<SearchResponseVo> result = listFeignClient.List(searchParam);
        // 转换为 map
        BeanMap map = BeanMap.create(result.getData());
        model.addAllAttributes(map);

        // 存储 urlParam --记录用户通过哪些条件进行了检索！
        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);

        // 品牌面包屑
        String trademarkParam = makeTradeMareParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam", trademarkParam);

        // 平台属性面包屑
        List<Map<String, String>> propsParamList = makeProps(searchParam.getProps());
        model.addAttribute("propsParamList", propsParamList);

        // 设置排序
        HashMap<String, Object> orderMap = makeOrderMap(searchParam.getOrder());
        model.addAttribute("orderMap", orderMap);

        // 封装前端查询条件
        model.addAttribute("searchParam", searchParam);
        // 返回视图
        return "list/index";
    }

    /**
     * 排序   order=2:desc
     *
     * @param order 排序字段
     * @return map
     */
    private HashMap<String, Object> makeOrderMap(String order) {
        // 创建hashMap对象
        HashMap<String, Object> map = new HashMap<>();
        // 判空
        if (!StringUtils.isEmpty(order)) {
            // 分割字符串
            String[] split = order.split(":");
            if (split.length == 2) {
                map.put("type", split[0]);
                map.put("sort", split[1]);
            }
        } else {
            // 默认规则
            map.put("type", "1");
            map.put("sort", "desc");
        }
        return map;
    }

    /**
     * 生成品牌面包屑
     *
     * @param trademark 品牌
     * @return 品牌面包屑
     */
    private String makeTradeMareParam(String trademark) {
        // 判空
        if (!StringUtils.isEmpty(trademark)) {
            // 分割字符串
            String[] split = trademark.split(":");
            if (split.length == 2) {
                return "品牌:" + split[1];
            }
        }
        return "";
    }

    /**
     * 生成平台属性面包屑
     *
     * @param props 平台属性
     * @return 平台属性面包屑
     */
    private List<Map<String, String>> makeProps(String[] props) {
        //  声明一个集合
        List<Map<String, String>> mapList = new ArrayList<>();
        //  判断
        if (props != null && props.length > 0) {
            //  循环遍历
            for (String prop : props) {
                //  prop = 23:8G:运行内存
                //  字符串分割
                String[] split = prop.split(":");
                if (split.length == 3) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("attrId", split[0]);
                    hashMap.put("attrValue", split[1]);
                    hashMap.put("attrName", split[2]);
                    //  将面包屑添加到集合中。
                    mapList.add(hashMap);
                }
            }
        }
        return mapList;
    }

    /**
     * 根据检索条件动态生成请求路径
     *
     * @param searchParam 检索条件
     * @return urlParam
     */
    private String makeUrlParam(SearchParam searchParam) {
        // 创建一个stringBuilder对象
        StringBuilder stringBuilder = new StringBuilder();

        // 分类入口     http://list.mall.com/list.html?category3Id=61
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            // 根据三级分类 id 进行检索
            stringBuilder.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            // 根据二级分类 id 进行检索
            stringBuilder.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            // 根据一级分类 id 进行检索
            stringBuilder.append("category1Id=").append(searchParam.getCategory1Id());
        }

        // 关键词入口
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            // 根据一级分类 id 进行检索
            stringBuilder.append("keyword=").append(searchParam.getKeyword());
        }

        // 在入口的基础上拼接品牌 id 进行过滤
        // 分类 id + 品牌 id 进行检索       http://list.mall.com/list.html?category3Id=61&trademark=1:小米
        // 关键词 + 品牌 id 进行检索       http://list.mall.com/list.html?keyword=手机&trademark=1:小米
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&trademark=").append(searchParam.getTrademark());
            }
        }

        // 在上面的基础上拼接平台属性进行过滤
        // http://list.html?category3Id=61&trademark=1:小米&props=23:8G:运行内存&props=24:128G:机身内存
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("&props=").append(prop);
                }
            }
        }
        // 返回 url
        return "list.html?" + stringBuilder;
    }
}
