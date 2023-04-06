package org.feuyeux.espoir.embedded;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Service;

@Service("notice")
@Slf4j
public class NoticeListener implements ExecutionListener {

  @Override
  public void notify(DelegateExecution delegateExecution) throws Exception {
    Object x1 = delegateExecution.getVariable("x1");
    Object x2 = delegateExecution.getVariable("x2");
    Object x3 = delegateExecution.getVariable("x3");
    Object x4 = delegateExecution.getVariable("x4");

    log.info("x1:{},x2:{},x3:{},x4:{}", x1, x2, x3, x4);
  }
}
