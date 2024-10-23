package io.mosip.kafkaadminclient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MosipKafkaAdminClient {

	private static Logger logger = LoggerFactory.getLogger(MosipKafkaAdminClient.class);
	
	private Properties properties;

	public MosipKafkaAdminClient(String bootstrapServers) {
		properties = new Properties();
		properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		logger.info("bootstrapServers :: " + bootstrapServers);
	}

	public void createTopic(String topicName) throws Exception {
		logger.info("Request received for creating topic with name :: " + topicName);
		try (Admin admin = Admin.create(properties)) {
			NewTopic newTopic = new NewTopic(topicName, Optional.of(1), Optional.empty());
			CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
			// get the async result for the new topic creation
			KafkaFuture<Void> future = result.values().get(topicName);
			// call get() to block until topic creation has completed or failed
				future.get();
			logger.info("Created topic with name :: " + topicName);
		}catch (Exception e) {
			logger.error("Error occurred while creating the topic :: " + topicName + ". Error :: " + e.getMessage());
			e.printStackTrace();
			throw new Exception();
		}
	}


	public boolean isTopicsPresent(String topics) throws Exception {
	List<String> topicsList = Arrays.asList(topics.split(","));	
	Set<String> kafkaTopics = getAllTopics();
	return topicsList.stream().allMatch(kafkaTopics::contains);	
	}

	public Set<String> getAllTopics() throws Exception {
		logger.info("Request received for getting all the topics.");
		try (Admin admin = Admin.create(properties)) {
			ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
			listTopicsOptions.listInternal(true);
			logger.info("Request received for getting all the topics.");
			return admin.listTopics(listTopicsOptions).names().get();
		}catch (Exception e) {
			logger.error("Error occurred while getting all the topics. Error :: " + e.getMessage());
			e.printStackTrace();
			throw new Exception();
		}
	}


	public Map<String, TopicDescription> describeTopic(String topic) throws Exception {
		try (Admin admin = Admin.create(properties)) {
			DescribeTopicsResult result = admin.describeTopics(Collections.singleton(topic));
			return result.all().get();
		}catch (Exception e) {
			logger.error("Error occurred while describeTopic. Error :: " + e.getMessage());
			e.printStackTrace();
			throw new Exception();
		}
	}
}
