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

import java.io.IOException;

@EnableRabbit
@Service("EventLogListener")
public class EventLogListener {
    private final Gson gson;

    @Autowired
    public EventLogListener(Gson gson) {
        this.gson = gson;
    }

    @RabbitListener(queues = {
            "EventLog"
    })
    public void fetchEventLogMessage(String msg, Message message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel ch) throws IOException {
        System.out.println(message);
        ch.basicAck(deliveryTag,false);
        System.out.println(message.getMessageProperties());
        System.out.println(msg);
    }


}
