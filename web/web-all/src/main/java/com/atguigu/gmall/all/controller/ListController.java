package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    //@RequestMapping("list.html")
    @GetMapping("list.html")
    public String list(SearchParam searchParam, Model model){

        Result<Map> result = listFeignClient.list(searchParam);
        model.addAllAttributes(result.getData());

        //页面渲染需要一个 urlParam 作用是记录拼接的url的参数列表
        //接受用户点击的查询条件
        String urlParam = makeUrlParam(searchParam);
        //获取品牌传递过来的参数
        String trademark = getTrademark(searchParam.getTrademark());
        //获取平台属性
        List<Map<String, String>> propsList = getMakeProps(searchParam.getProps());

        //获取排序规则
        Map<String, Object> map = getOrder(searchParam.getOrder());

        //保存用户查询的数据
        model.addAttribute("searchParam",searchParam);

        model.addAttribute("urlParam",urlParam);
        //存储品牌
        model.addAttribute("trademarkParam",trademark);
        //存储平台属性
        model.addAttribute("propsParamList",propsList);
        //存储排序规则
        model.addAttribute("orderMap",map);

        return "list/index";
    }


    //记录查询的条件参数
    private String makeUrlParam(SearchParam searchParam) {

        StringBuilder urlParam = new StringBuilder();
        //判断是否是关键字
        if(searchParam.getKeyword() != null){
            //记录keyWord
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }

        //判断是否根据分类id查询
        if(searchParam.getCategory1Id() != null){
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if(searchParam.getCategory2Id() != null){
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        //http://list.gmall.com/list.html?category3Id=61
        if(searchParam.getCategory3Id() != null){
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }

        //http://list.gmall.com/&trademark=2:苹果&order=
        //判断品牌
        //方法一：
        if(searchParam.getTrademark() != null){
            if(urlParam.length()>0){
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        //方法二：
//        if(searchParam.getTrademark() != null && urlParm.length()>0){
//                urlParm.append("&trademark=").append(searchParam.getTrademark());
//        }

        //根据平台属性判断
        if(searchParam.getProps() != null){
            for (String prop : searchParam.getProps()) {
                if(urlParam.length()>0){
                    urlParam.append("&props=").append(prop);
                }
            }
        }
        //记录的是拼接条件
        String urlParamStr = urlParam.toString();
        return "list.html?" + urlParamStr;
    }


    //获取品牌名称 品牌：品牌名称 trademark = 2: 华为
    private String getTrademark(String trademark){

        //可使用Stringutils
        if(trademark != null && trademark.length()>0){
            //将字符串进行分割
            //String[] split = trademark.split(":");
            String[] split = StringUtils.split(trademark, ":");
            //if(split != null && split.length==2)
            //为什么用&&而不用||
            if(split != null && split.length==2){
                return "品牌：" + split[1];
            }
        }
        return "";
    }


    //获取平台属性
    private List<Map<String,String>> getMakeProps(String[] props){

        List<Map<String,String>> list = new ArrayList<>();

        if(props != null && props.length>0){
            //循环获取里面的数组
            for (String prop : props) {
                //prop中每个值，组成的格式[2：6.55-6.90英寸：屏幕尺寸]
                //进行分割
                String[] split = prop.split(":");
                //循环这个数组中的每个数据 符合数据格式
                if(split != null && split.length == 3){
                    //将数组中的字符串放在map中
                    HashMap<String, String> map = new HashMap<>();
                    //保存平台属性Id 平台属性值的名称 平台属性名
                    map.put("attrId",split[0]);
                    map.put("attrValue",split[1]);
                    map.put("attrName",split[2]);

                    list.add(map);
                }
            }
        }
        return list;
    }


    //获取排序规则
    private Map<String,Object> getOrder(String order){
        HashMap<String, Object> map = new HashMap<>();
        if(StringUtils.isNotEmpty(order)){
            //order=2:asc
            String[] split = order.split(":");
            if(split != null && split.length==2){
                //type代表用户点击的那个字段
                map.put("type",split[0]);
                //sort 代表排序规则
                map.put("sort",split[1]);
            }
        }else{
            //如果没有规定排序规则，给一个默认的排序规则
            //type代表用户点击的那个字段
            map.put("type","1");
            //sort 代表排序规则
            map.put("sort","asc");
        }
        return map;
    }
}
