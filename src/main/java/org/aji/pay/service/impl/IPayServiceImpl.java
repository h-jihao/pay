package org.aji.pay.service.impl;

import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.aji.pay.dao.PayInfoMapper;
import org.aji.pay.enums.PayPlatformEnum;
import org.aji.pay.pojo.PayInfo;
import org.aji.pay.service.IPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class IPayServiceImpl implements IPayService {
    @Autowired
    private BestPayService bestPayService;
    @Autowired
    private PayInfoMapper payInfoMapper;

    @Override
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {
        //app后台将下单信息首先存储到数据库
        PayInfo payinfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount
                );
        int flag = payInfoMapper.insertSelective(payinfo);
        System.out.print("是否存储成功："+flag);

        //对传入的支付方式进行校验
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
        //签名校验
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("payResponse:{}",payResponse);

        //金额校验(异步通知的返回的金额信息和app订单中的金额是否相等)
            //1.根据返回的异步通知消息中的订单号查询数据库中对应消息是否存在
            //2.数据库中订单支付状态为未支付并且订单金额和通知中的金额一致，则此时才能修改订单状态
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.valueOf(payResponse.getOrderId()));
        if(payInfo == null){
            throw new RuntimeException("数据库中查不到相关订单信息，这里是短信通知。。。。");
        }
        if(!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
            //Double类型的数据，精度问题不好控制
            if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) == 0){
                //订单支付状态修改
                payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
                payInfo.setPlatformNumber(payResponse.getOutTradeNo());
                payInfo.setUpdateTime(null);
                payInfoMapper.updateByPrimaryKeySelective(payInfo);
            }
        }

        //TODO pay发送MQ消息， mall接收MQ消息

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

    @Override
    public PayInfo queryByOrderId(String orderId) {
        log.info("查询支付记录...");
        return payInfoMapper.selectByOrderNo(Long.valueOf(orderId));
    }


}
