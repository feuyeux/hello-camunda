package com.feuyeux.camunda.external.externaltaskjava;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShoppingTask {

  private static final String CAMUNDA_URL = "http://localhost:8080/engine-rest";
  public static final int LONG_POLLING_TIME_OUT = 10_000;
  private ExternalTaskClient client = null;

  private ExternalTaskClient getClient() {
    if (client == null) {
      client = ExternalTaskClient.create()
          .baseUrl(CAMUNDA_URL)
          .asyncResponseTimeout(LONG_POLLING_TIME_OUT)
          .build();
    }
    return client;
  }

  @PostConstruct
  public void shoppingCart() {
    getClient()
        .subscribe("shopping_cart")
        .processDefinitionKey("Process_shopping")
        .lockDuration(2000)
        .handler((externalTask, externalTaskService) -> {
          log.info("订阅加入购物车任务");
          Map<String, Object> goodVariable = Variables.createVariables()
              .putValue("size", "xl")
              .putValue("count", 2);
          externalTaskService.complete(externalTask, goodVariable);
        }).open();
  }

  @PostConstruct
  public void delivery() {
    getClient()
        .subscribe("logistic_delivery")
        .processDefinitionKey("Process_shopping")
        .lockDuration(2000)
        .handler((externalTask, externalTaskService) -> {
          Object toWhere = externalTask.getVariable("toWhere");
          log.info("发货 目的地:{}", toWhere);
          externalTaskService.complete(externalTask);
        }).open();
  }
}
