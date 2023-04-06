package org.feuyeux.espoir.embedded;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service("toLower")
public class ToLowerCase implements JavaDelegate {

  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {
    String var = (String) delegateExecution.getVariable("x2");
    var = var.toLowerCase();
    delegateExecution.setVariable("x3", var);
  }
}
