package cool.yunlong.mall.product.api;

import com.alibaba.fastjson.JSONObject;
import cool.yunlong.mall.common.result.Result;
import cool.yunlong.mall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class IndexApiController {

    @Autowired
    private ManageService manageService;

    @GetMapping("/getBaseCategoryList")
    public Result<List<JSONObject>> getBaseCategoryList() {
        List<JSONObject> baseCategoryList = manageService.getBaseCategoryList();
        return Result.ok(baseCategoryList);
    }
}
