package com.dell.isg.smi.virtualnetwork.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.dell.isg.smi.commons.elm.model.CreatedResponse;
import com.dell.isg.smi.virtualnetwork.model.Network;
import com.dell.isg.smi.virtualnetwork.model.NetworkType;
import com.dell.isg.smi.virtualnetwork.model.StaticIpv4NetworkConfiguration;
import com.dell.isg.smi.virtualnetwork.test.integration.context.ServiceProperties;
import com.dell.isg.smi.virtualnetwork.test.integration.context.TestContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ TestContext.class})
public class CrudNetworkIT {

	@Autowired
	private ServiceProperties serviceProperties;

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void valid_crud_network_with_no_ip_pools() throws Exception{
		String protocol = "http";
		String ip = serviceProperties.getIp();
		String port = serviceProperties.getPort();
		String url = protocol + "://" + ip + ":" + port + "/api/1.0/networks";


		// create a network
		Network network = exampleNetwork();
		ResponseEntity<CreatedResponse> createResponse = this.restTemplate.postForEntity(url, network, CreatedResponse.class);
		HttpStatus createdHttpStatus = createResponse.getStatusCode();
		assertThat(createdHttpStatus.value()).isEqualTo(201);
		CreatedResponse createdResponse = createResponse.getBody();
		assertThat(createdResponse.getId()).isGreaterThan(0);

		// read network
		ResponseEntity<Network> readResponse = this.restTemplate.getForEntity(url + "/" + createdResponse.getId(), Network.class);
		Network retrievedNetwork = readResponse.getBody();
		HttpStatus retrievedHttpStatus = readResponse.getStatusCode();
		assertThat(retrievedHttpStatus.value()).isEqualTo(200);
		assertThat(retrievedNetwork.getName()).isEqualTo(network.getName());

		// updateNetwork
		retrievedNetwork.setId(createdResponse.getId());
		retrievedNetwork.setName("new name");
		this.restTemplate.put(url + "/" + createdResponse.getId(), retrievedNetwork);

		// delete a network
		this.restTemplate.delete(url + "/" + createdResponse.getId());

		// read network - should return 404
		ResponseEntity verifyDeletedResponse = this.restTemplate.getForEntity(url + "/" + createdResponse.getId(), null);
		HttpStatus verifyDeletedHttpStatus = verifyDeletedResponse.getStatusCode();
		assertThat(verifyDeletedHttpStatus.value()).isEqualTo(404);
	}

	private Network exampleNetwork(){
		StaticIpv4NetworkConfiguration staticIpv4NetworkConfiguration = new StaticIpv4NetworkConfiguration();
		staticIpv4NetworkConfiguration.setGateway("100.68.123.63");
		staticIpv4NetworkConfiguration.setSubnet("255.255.255.192");
		staticIpv4NetworkConfiguration.setPrimaryDns("100.68.126.200");
		staticIpv4NetworkConfiguration.setDnsSuffix("delllabs.net");

		Network network = new Network();
		network.setName("MyNetwork");
		network.setDescription("My Test Network");
		network.setStatic(false);
		network.setType(NetworkType.PRIVATE_LAN);
		network.setVlanId(1231);
		network.setStaticIpv4NetworkConfiguration(staticIpv4NetworkConfiguration);

		return network;
	}
}
