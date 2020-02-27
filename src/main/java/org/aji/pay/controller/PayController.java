package org.aji.pay.controller;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.aji.pay.service.IPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@Slf4j
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private IPayService payService;

    @RequestMapping("/create")
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("bestPayTypeEnum") BestPayTypeEnum bestPayTypeEnum){

        PayResponse response = payService.create(orderId, amount,bestPayTypeEnum);
        Map<String,String> map = new ConcurrentHashMap<>();
        if(bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE) {
            map.put("codeUrl", response.getCodeUrl());
            return new ModelAndView("createWXpayNATIVE", map);
        }else if(bestPayTypeEnum == BestPayTypeEnum.ALIPAY_PC){
            map.put("body",response.getBody());
            return new ModelAndView("createAlipayPC",map);
        }
        throw new RuntimeException("非法的支付方式");
    }

    @RequestMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData){
        return payService.asyncNotify(notifyData);
    }
}
