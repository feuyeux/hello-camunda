package com.feuyeux.camunda.external.externaltaskjava;

import jakarta.annotation.PostConstruct;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalTaskService {

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
    public void toUpper() {
        getClient()
                .subscribe("ask_upper")
                .processDefinitionKey("Process_external_asking")
                .lockDuration(2000)
                .handler((externalTask, externalTaskService) -> {
                    String x1 = externalTask.getVariable("x1").toString();
                    Map<String, Object> goodVariable = Variables.createVariables()
                            .putValue("x2", x1.toUpperCase());
                    externalTaskService.complete(externalTask, goodVariable);
                }).open();
    }

    @PostConstruct
    public void delivery() {
        getClient()
                .subscribe("ask_summary")
                .processDefinitionKey("Process_external_asking")
                .lockDuration(2000)
                .handler((externalTask, externalTaskService) -> {
                    Map<String, Object> variables = externalTask.getAllVariables();
                    variables.forEach((k, v) -> {
                        log.info("[{}]:{}", k, v);
                    });
                    externalTaskService.complete(externalTask);
                }).open();
    }
}
