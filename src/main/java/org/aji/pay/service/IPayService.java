package org.aji.pay.service;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import org.aji.pay.pojo.PayInfo;

import java.math.BigDecimal;

public interface IPayService {

    /**
     * 订单创建
     * @param orderId
     * @param amount
     * @return
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 接收异步通知并处理
     * @param notifyData
     * @return
     */
    String asyncNotify(String notifyData);

    /**
     * 查询支付记录（通过订单号）
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);

}
