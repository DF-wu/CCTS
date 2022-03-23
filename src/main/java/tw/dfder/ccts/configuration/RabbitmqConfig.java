package tw.dfder.ccts.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Df
 * @Comment rabbitMQ for spring boot configuration
 */

@Configuration
@EnableRabbit
public class RabbitmqConfig {
    // apply the exchange and related setting when this service on.

    public static final String EXCHANG_ORCHESTRATOR = "EXCHANGE_CCTS_ORCHESTRATOR";

    public static final String QUEUE_PAYMENT_REQUEST = "QUEUE_PAYMENT_REQUEST";
    public static final String QUEUE_PAYMENT_RESPONSE = "QUEUE_PAYMENT_RESPONSE";

    public static final String QUEUE_UPDATEPOINT_REQUEST = "QUEUE_UPDATEPOINT_REQUEST";
    public static final String QUEUE_UPDATEPOINT_RESPONSE = "QUEUE_UPDATEPOINT_RESPONSE";

    public static final String QUEUE_LOGGGING_REQUEST = "QUEUE_LOGGGING_REQUEST";
    public static final String QUEUE_LOGGGING_RESPONSE = "QUEUE_LOGGGING_RESPONSE";



    // below for biding key setup
    public static final String BINDINGKEY_PAYMENT_REQUEST = "payment.req.#";
    public static final String BINDINGKEY_PAYMENT_RESPONSE = "payment.res.#";

    public static final String BINDINGKEY_UPDATEPOINT_REQUEST = "updatepoint.req.#";
    public static final String BINDINGKEY_UPDATEPOINT_RESPONSE = "updatepoint.res.#";

    public static final String BINDINGKEY_LOGGING_REQUEST = "logging.req.#";
    public static final String BINDINGKEY_LOGGING_RESPONSE = "logging.res.#";


    // routing key prefix
    public static final String ROUTING_PAYMENT_REQUEST = "payment.req";
    public static final String ROUTING_PAYMENT_RESPONSE = "payment.res";

    public static final String ROUTING_UPDATEPOINT_REQUEST = "updatepoint.req";
    public static final String ROUTING_UPDATEPOINT_RESPONSE = "updatepoint.res";

    public static final String ROUTING_LOGGING_REQUEST = "logging.req";
    public static final String ROUTING_LOGGING_RESPONSE = "logging.res";





    // 宣告topic模式的exchange
    @Bean
    public Exchange EXCHANGE_CCTS(){
        // durable(true) 表面重启之后交换机还在
        return ExchangeBuilder.topicExchange(EXCHANG_ORCHESTRATOR).durable(true).build();
    }

    // Queue for payment req/res
    @Bean
    public Queue queuePaymentRequest(){
        // durable == data persistence
        return new Queue(QUEUE_PAYMENT_REQUEST,true);
    }

    @Bean
    public Queue queuePaymentResponse(){
        // durable == data persistence
        return new Queue(QUEUE_PAYMENT_RESPONSE,true);
    }


    // Queue for update point req/res
    @Bean
    public Queue queueUpdatePointsRequest(){
        // durable == data persistence
        return new Queue(QUEUE_UPDATEPOINT_REQUEST,true);
    }

    @Bean
    public Queue queueUpdatePointsResponse(){
        // durable == data persistence
        return new Queue(QUEUE_UPDATEPOINT_RESPONSE,true);
    }

    // Queue for logging req/res
    @Bean
    public Queue queueLoggingRequest(){
        // durable == data persistence
        return new Queue(QUEUE_LOGGGING_REQUEST,true);
    }

    @Bean
    public Queue queueLoggingResponse(){
        // durable == data persistence
        return new Queue(QUEUE_LOGGGING_RESPONSE,true);
    }



    // binding payment req&res queue
    @Bean
    public Binding bindingQueuePaymentReq(){
        return BindingBuilder.bind(queuePaymentRequest())
                .to(EXCHANGE_CCTS())
                .with(BINDINGKEY_PAYMENT_REQUEST)
                .noargs();
    }

    @Bean
    public Binding bindingQueuePaymentRes(){
        return BindingBuilder.bind(queuePaymentResponse())
                .to(EXCHANGE_CCTS())
                .with(BINDINGKEY_PAYMENT_RESPONSE)
                .noargs();
    }


    // binding updateUpoints req&res queue
    @Bean
    public Binding bindQueueUpdatePointsReq(){
        return BindingBuilder.bind(queueUpdatePointsRequest())
                .to(EXCHANGE_CCTS())
                .with(BINDINGKEY_UPDATEPOINT_REQUEST)
                .noargs();
    }

    @Bean
    public Binding bindQueueUpdatePointsRes(){
        return BindingBuilder.bind(queueUpdatePointsResponse())
                .to(EXCHANGE_CCTS())
                .with(BINDINGKEY_UPDATEPOINT_RESPONSE)
                .noargs();
    }


    // binding logging req&res queue
    @Bean
    public Binding bindQueueLoggingReq() {
        return BindingBuilder.bind(queueLoggingRequest())
                .to(EXCHANGE_CCTS())
                .with(BINDINGKEY_LOGGING_REQUEST)
                .noargs();
    }

    @Bean
    public Binding bindQueueLoggingRes(){
        return BindingBuilder.bind(queueLoggingResponse())
                .to(EXCHANGE_CCTS())
                .with(BINDINGKEY_LOGGING_RESPONSE)
                .noargs();
    }



    /**
     * 將自定義的消息類序列化成json格式，再轉成byte構造 Message，在接收消息時，會將接收到的 Message 再反序列化成自定義的類。
     * @param objectMapper
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }


    /**
     * EventLog colloection queue
     */
    @Bean
    public Queue queueCCTSEventLog(){
        // durable == data persistence
        return new Queue("EventLog",true);
    }

    @Bean
    public Binding bindingQueueCCTSEventLog(){
        return BindingBuilder.bind(queueCCTSEventLog())
                .to(EXCHANGE_CCTS())
                .with("#")
                .noargs();
    }

}
