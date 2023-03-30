package org.feuyeux.espoir;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.camunda.bpm.spring.boot.starter.event.PreUndeployEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@Slf4j
@EnableProcessApplication("Hello Process Application")
public class Application {

  public static void main(String... args) {
    SpringApplication.run(Application.class, args);
  }

  @EventListener
  public void onPostDeploy(PostDeployEvent event) {
    log.info("Process application has been deployed");
  }

  @EventListener
  public void onPreUndeploy(PreUndeployEvent event) {
    log.info("Process application will be un-deployed");
  }
}