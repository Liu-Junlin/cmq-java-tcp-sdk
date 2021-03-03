package demo;

import com.qcloud.cmq.client.common.ClientConfig;
import com.qcloud.cmq.client.common.ResponseCode;
import com.qcloud.cmq.client.exception.MQClientException;
import com.qcloud.cmq.client.exception.MQServerException;
import com.qcloud.cmq.client.producer.Producer;
import com.qcloud.cmq.client.producer.PublishResult;

import java.util.Arrays;
import java.util.List;

public class PublishInterceptor {

    public static void main(String args[]) throws MQClientException, MQServerException {

        Interceptor interceptor = new Interceptor();

        Producer producer = new Producer(Arrays.asList(interceptor));
        // 设置 Name Server地址。必须设置 不同地域不同网络不同
        // 地域对应的缩写 bj:北京 sh：上海 gz:广州 in:孟买 ca:北美 cd:成都 cq: 重庆
        //  hk:香港 kr:韩国 ru:俄罗斯 sg:新加坡 shjr:上海金融 szjr:深圳金融 th:曼谷 use: 弗吉尼亚 usw： 美西
        // 私有网络地址：http://cmq-nameserver-vpc-{region}.api.tencentyun.com 支持腾讯云私有网络的云服务器内网访问
        // 公网地址：    http://cmq-nameserver-{region}.tencentcloudapi.com
        producer.setNameServerAddress("http://cmq-nameserver-gz.tencentcloudapi.com");
        // 设置SecretId，在控制台上获取，必须设置
        producer.setSecretId("xxx");
        // 设置SecretKey，在控制台上获取，必须设置
        producer.setSecretKey("xxx");
        // 设置签名方式，可以不设置，默认为SHA1
        producer.setSignMethod(ClientConfig.SIGN_METHOD_SHA256);
        // 设置发送消息失败时，重试的次数，设置为0表示不重试，默认为2
        producer.setRetryTimesWhenSendFailed(3);
        // 设置请求超时时间， 默认3000ms
        producer.setRequestTimeoutMS(5000);

        producer.start();

        // 1. 向标签过滤的topic发布同步消息（也可发布异步消息和批量发布消息）
        String msgTag = "msg with tag";
        // 需要先在控制台创建一个 消息过滤类型为'标签'的主题
        String topicWithTag = "tag-topic-test";
        List<String> tagList = Arrays.asList("test-tag", "apple");

        try {
            PublishResult result = producer.publish(topicWithTag, msgTag, tagList);
            if (result.getReturnCode() == ResponseCode.SUCCESS)
                System.out.println("===> publish tag message success, msgId : " + result.getMsgId());
            else {
                System.out.println("===> publish error : " + result.getErrorMsg() + ", msgId :" + result.getMsgId());
            }

        }catch (MQClientException e) {
            e.printStackTrace();
        } catch (MQServerException e) {
            e.printStackTrace();
        }


        // 2. 向路由消息匹配主题发布同步消息（也可发布异步消息和批量发布消息）
        String msgRoute = "msg with route";
        // 需要现在控制台创建一个 消息过滤类型为'路由匹配'的主题
        String topicWithRoute = "tag-topic-test";
        // routingKey与rabbitMQ中的相关概念相同，与订阅时指定的bindingKey配合使用
        String routingKey = "test-route";
        try {
            PublishResult result = producer.publish(topicWithRoute, msgRoute, routingKey);

            if (result.getReturnCode() == ResponseCode.SUCCESS)
                System.out.println("===> publish route message success, msgId : " + result.getMsgId());
            else {
                System.out.println("===> publish error : " + result.getErrorMsg() + ", msgId :" + result.getMsgId());
            }
        }catch (MQClientException e) {
            e.printStackTrace();
        } catch (MQServerException e) {
            e.printStackTrace();
        }

    }
}
