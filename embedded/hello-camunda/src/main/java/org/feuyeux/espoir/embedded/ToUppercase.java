package org.feuyeux.espoir.embedded;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ToUppercase implements JavaDelegate {
  public void execute(DelegateExecution execution) throws Exception {
    String var = (String) execution.getVariable("x1");
    var = var.toUpperCase();
    execution.setVariable("x2", var);
  }
}