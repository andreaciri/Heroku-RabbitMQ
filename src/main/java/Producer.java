import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Producer {

  private final static String QUEUE_NAME = "hello";

  public void publish(String messageToSend) throws Exception {
    String uri = System.getenv("CLOUDAMQP_URL");
    if (uri == null) uri = "amqp://guest:guest@localhost";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri(uri);
    factory.setRequestedHeartbeat(30);
    factory.setConnectionTimeout(30);
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    channel.basicPublish("", QUEUE_NAME, null, messageToSend.getBytes());
    System.out.println(" [x] Producer: Sent '" + messageToSend + "'");

    channel.close();
    connection.close();
  }
}