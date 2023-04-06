package org.feuyeux.espoir.embedded;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

@Service("prefix")
@Slf4j
public class AddPrefix {

  public String add(DelegateExecution execution) {
    String var = (String) execution.getVariable("x3");
    String currentActivityId = execution.getCurrentActivityId();
    String currentActivityName = execution.getCurrentActivityName();
    log.info("{}:{}",currentActivityId,currentActivityName);
    return LocalDate.now().getDayOfMonth() + "_" + var;
  }
}
