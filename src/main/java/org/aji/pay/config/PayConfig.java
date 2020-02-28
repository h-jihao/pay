package org.aji.pay.config;

import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayConfig {
    @Autowired
    private WXAccountConfig wxAccountConfig;

    @Autowired
    private AliAccountConfig aliAccountConfig;

    /**
     * 微信native方式的支付配置
     * @return
     */
    @Bean
    public BestPayService bestPayService(WxPayConfig wxPayConfig){
        //支付宝支付的请求参数
        AliPayConfig aliPayConfig = new AliPayConfig();
        aliPayConfig.setAppId(aliAccountConfig.getAppId());
        aliPayConfig.setPrivateKey(aliAccountConfig.getPrivateKey());
        aliPayConfig.setAliPayPublicKey(aliAccountConfig.getAliPayPublicKey());
        aliPayConfig.setNotifyUrl(aliAccountConfig.getNotifyUrl());
        aliPayConfig.setReturnUrl(aliAccountConfig.getReturnUrl());
        //支付类。所有的支付方法都在该类中
        BestPayServiceImpl bestPayService = new BestPayServiceImpl();
        bestPayService.setWxPayConfig(wxPayConfig);
        bestPayService.setAliPayConfig(aliPayConfig);
        return bestPayService;
    }

    @Bean
    public WxPayConfig wxPayConfig(){
        //设置微信支付方式的请求参数
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(wxAccountConfig.getAppId());
        payConfig.setMchId(wxAccountConfig.getMchId());
        payConfig.setMchKey(wxAccountConfig.getMchKey());
        payConfig.setNotifyUrl(wxAccountConfig.getNotifyUrl());
        payConfig.setReturnUrl(wxAccountConfig.getReturnUrl());
        return payConfig;
    }
}
