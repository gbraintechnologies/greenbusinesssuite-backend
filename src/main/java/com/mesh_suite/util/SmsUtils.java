package com.mesh_suite.util;
import com.mesh_suite.dto.SmsDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@Component
public class SmsUtils {
    public static final String SMS_QUEUE = "sms_queue";
    private final RabbitTemplate rabbitTemplate;

    public SmsUtils(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public  void pushSMSToQueue(SmsDTO smsDTO){
        rabbitTemplate.convertAndSend(SMS_QUEUE, smsDTO);
    }
}
