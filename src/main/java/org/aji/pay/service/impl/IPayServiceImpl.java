package org.aji.pay.service.impl;

import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.aji.pay.service.IPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class IPayServiceImpl implements IPayService {
    @Autowired
    private BestPayService bestPayService;

    @Override
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {
        //app后台将下单信息首先存储到数据库

        //对闯传入的支付方式进行校验
        if(bestPayTypeEnum != BestPayTypeEnum.ALIPAY_PC && bestPayTypeEnum != BestPayTypeEnum.WXPAY_NATIVE){
            throw new RuntimeException("不是合法的支付方式");
        }
        //调用微信统一下单接口
        PayRequest request = new PayRequest();
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setOrderName("8601122-通用支付SDK");
        request.setPayTypeEnum(bestPayTypeEnum);
        PayResponse response = bestPayService.pay(request);

        return response;
    }

    @Override
    public String asyncNotify(String notifyData) {
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("payResponse:{}",payResponse);

        //签名校验
        System.out.print("alise");
        //金额校验(异步通知的返回的金额信息和app订单中的金额是否相等)

        //订单支付支付状态修改

        //返回成功的响应给微信，告诉微信不要再发通知了(这里需要判断是哪一种支付方式，根据支付方式判断如何响应异步通知)
        if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY){
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }
}
