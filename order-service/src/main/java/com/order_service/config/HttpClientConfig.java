package com.order_service.config;

import com.order_service.client.ProductServiceClient;
import com.order_service.client.UserServiceClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(basePackages = "com.order_service.client",
types = {ProductServiceClient.class, UserServiceClient.class})
public class HttpClientConfig {
}
