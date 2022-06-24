package cool.yunlong.mall.web.controller;

import com.alibaba.fastjson.JSONObject;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Controller
public class IndexController {

    @Qualifier("cool.yunlong.mall.product.client.ProductFeignClient")
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private TemplateEngine templateEngine;

    // 通过 http://www.mall.com/   或 http://www.mall.com/index.html
    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        Result<List<JSONObject>> result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", result.getData());
        return "index/index";
    }

    /**
     * 创建首页静态资源模板数据
     *
     * @return 响应结果
     */
    @GetMapping("/createIndex")
    @ResponseBody
    public Result<String> createIndex() throws IOException {
        // 远程调用 productFeignClient 获取数据
        Result<List<JSONObject>> result = productFeignClient.getBaseCategoryList();

        // 创建Context对象
        Context context = new Context();
        // 设置存储的数据  Key:Value
        context.setVariable("list", result.getData());

        // 创建FilterWriter对象
        try (FileWriter fileWriter = new FileWriter("C:\\Users\\beife\\Desktop\\index.html")) {
            // 解析模板
            templateEngine.process("index/index.html", context, fileWriter);
        }
        return Result.ok("创建成功！");
    }
}
