package com.cyanspring.transport;

import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Ignore;

@Ignore
public class ActiveMQProducerTest {
	// ActiveMQ configuration parameters
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = "nio://localhost:61616";
    private boolean transacted;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;

	/**
	 * @param args
	 * @throws Exception 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException, Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		BrokerService broker = new BrokerService();
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("nio://localhost:61616"));
		broker.addConnector(connector);
		broker.setPersistent(false);
		broker.start();

		ActiveMQProducerTest test = new ActiveMQProducerTest();
		test.testQueue();
	}
	
	public void testQueue() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(transacted, ackMode);
		
		Destination dest = session.createQueue("subject1");
        MessageProducer senderProducer = session.createProducer(dest);
        senderProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		for (int i=0; i<1000; i++) {
			TextMessage txt = session.createTextMessage("hello" + i);
			senderProducer.send(txt);
		}
		session.close();
		connection.close();
	}
	
	public void testPubSub() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(transacted, ackMode);
		
		Destination dest = session.createTopic("subject1");
        MessageProducer senderProducer = session.createProducer(dest);
        senderProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		for (int i=0; i<1000; i++) {
			TextMessage txt = session.createTextMessage("hello" + i);
			senderProducer.send(txt);
		}
		session.close();
		connection.close();
	}
	
}
