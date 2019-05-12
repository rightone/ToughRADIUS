package org.toughradius.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.toughradius.common.DateTimeUtil;
import org.toughradius.common.PageResult;
import org.toughradius.common.RestResult;
import org.toughradius.common.ValidateUtil;
import org.toughradius.component.SubscribeService;
import org.toughradius.component.Memarylogger;
import org.toughradius.entity.Subscribe;
import org.toughradius.entity.SubscribeForm;
import org.toughradius.entity.SubscribeQuery;

import java.util.List;

@Controller
public class SubsribeController {

    @Autowired
    protected Memarylogger logger;

    @Autowired
    protected SubscribeService subscribeService;

    @GetMapping(value = {"/admin/subscribe/query"})
    @ResponseBody
    public PageResult<Subscribe> querySubscribe(@RequestParam(defaultValue = "0") int start,
                                                @RequestParam(defaultValue = "40") int count,
                                                String  createTime, String expireTime, String status, String keyword){
        if(ValidateUtil.isNotEmpty(expireTime)&&expireTime.length() == 16){
            expireTime += ":00";
        }
        if(ValidateUtil.isNotEmpty(createTime)&&createTime.length() == 16){
            createTime += ":59";
        }
        int page = start / count;
        Page<Object> objects = PageHelper.startPage(page + 1, count);
        PageResult<Subscribe> result = new PageResult<>(0,0,null);
        try{
            SubscribeQuery query = new SubscribeQuery();
            if(ValidateUtil.isNotEmpty(expireTime))
                query.setExpireTime(DateTimeUtil.toTimestamp(expireTime));
            if(ValidateUtil.isNotEmpty(createTime))
                query.setCreateTime(DateTimeUtil.toTimestamp(createTime));
            query.setStatus(status);
            query.setKeyword(keyword);
            List<Subscribe> data = subscribeService.queryForList(query);
            return new PageResult<>(start,(int) objects.getTotal(), data);

        }catch(Exception e){
            logger.error("query subscribe error",e, Memarylogger.SYSTEM);
        }
        return result;
    }

    @GetMapping(value = {"/admin/subscribe/detail"})
    @ResponseBody
    public RestResult<Subscribe> querySubscribeDetail(Long id){
        try{
            return new RestResult<Subscribe>(0,"ok",subscribeService.findById(id));
        }catch(Exception e){
            logger.error("查询用户详情失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"查询用户详情失败");
        }
    }

    @PostMapping(value = {"/admin/subscribe/create"})
    @ResponseBody
    public RestResult addSubscribe(SubscribeForm form){
        try{
            if(subscribeService.findSubscribe(form.getSubscriber())!=null){
                return new RestResult(1,"用户已经存在");
            }
            Subscribe subscribe = form.getSubscribeData();
            subscribe.setBeginTime(DateTimeUtil.nowTimestamp());
            subscribe.setCreateTime(DateTimeUtil.nowTimestamp());
            subscribe.setUpdateTime(DateTimeUtil.nowTimestamp());
            subscribe.setBeginTime(DateTimeUtil.nowTimestamp());
            subscribe.setStatus("enabled");
            subscribe.setUpPeakRate(subscribe.getUpRate());
            subscribe.setDownPeakRate(subscribe.getDownPeakRate());
            subscribeService.insertSubscribe(subscribe);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("创建用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"创建用户失败");
        }
    }

    @PostMapping(value = {"/admin/subscribe/uppwd"})
    @ResponseBody
    public RestResult updateSubscribe(SubscribeForm form){
        try{
            if(subscribeService.findById(form.getId())==null){
                return new RestResult(1,"用户不存在");
            }
            if(form.getPassword().equals(form.getCpassword())){
                return new RestResult(1,"确认密码不符");
            }
            subscribeService.updatePassword(form.getId(),form.getPassword());
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("更新用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"更新用户失败");
        }
    }

    @PostMapping(value = {"/admin/subscribe/update"})
    @ResponseBody
    public RestResult updatePassword(SubscribeForm form){
        try{
            if(subscribeService.findById(form.getId())==null){
                return new RestResult(1,"用户不存在");
            }
            Subscribe subscribe = form.getSubscribeData();
            subscribe.setUpdateTime(DateTimeUtil.nowTimestamp());
            subscribeService.updateSubscribe(subscribe);
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("更新用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"更新用户失败");
        }
    }

    @GetMapping(value = {"/admin/subscribe/delete"})
    @ResponseBody
    public RestResult delete(String ids){
        try{
            for (String id : ids.split(",") ) {
                subscribeService.deleteById(Long.valueOf(id));
            }
            return RestResult.SUCCESS;
        }catch(Exception e){
            logger.error("删除用户失败",e, Memarylogger.SYSTEM);
            return new RestResult(1,"删除用户失败");
        }
    }


}