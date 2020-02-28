package org.aji.pay.enums;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import lombok.Getter;


@Getter
public enum PayPlatformEnum {

    ALIPAY(1),
    WX(2);

    Integer code;

    PayPlatformEnum(Integer code) {
        this.code = code;
    }

   public static PayPlatformEnum getBestPayTypeEnum(BestPayTypeEnum bestPayTypeEnum){
        for (PayPlatformEnum value : PayPlatformEnum.values()) {
            if(bestPayTypeEnum.getPlatform().name().equals(value.name())) {
                return value;
            }
        }
       throw new RuntimeException("错误的支付平台");
    }

}
