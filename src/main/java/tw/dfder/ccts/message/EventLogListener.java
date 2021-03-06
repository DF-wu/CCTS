package tw.dfder.ccts.message;


import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import tw.dfder.ccts.entity.EventLog;
import tw.dfder.ccts.repository.EventLogRepository;

import java.io.IOException;

@EnableRabbit
@Service("EventLogListener")
public class EventLogListener {
    private final Gson gson;
    private final EventLogRepository eventLogRepository;

    @Autowired
    public EventLogListener(Gson gson, EventLogRepository eventLogRepository) {
        this.gson = gson;
        this.eventLogRepository = eventLogRepository;
    }

    @RabbitListener(queues = {
            "EventLog"
    })
    public void fetchEventLogMessage(String msg, Message message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel ch) throws IOException {
//        System.out.println(message);

//        System.out.println(message.getMessageProperties());
//        System.out.println(msg);

        ch.basicAck(deliveryTag,false);
        //  aspect CCTS message spec
        try {
            EventLog el = new EventLog(
                    Long.parseLong(message.getMessageProperties().getHeaders().get("CCTSTimestamp").toString()),
                    message.getMessageProperties().getHeaders().get("provider").toString(),
                    message.getMessageProperties().getHeaders().get("consumer").toString(),
                    message.getMessageProperties().getHeaders().get("testCaseId").toString(),
                    Integer.valueOf(message.getMessageProperties().getHeaders().get("timeSequenceLabel").toString())
                    );
            eventLogRepository.save(el);

        }catch (Exception e) {
            System.out.println("EventLog error!!");
            System.out.println(e);
        }



    }


}
