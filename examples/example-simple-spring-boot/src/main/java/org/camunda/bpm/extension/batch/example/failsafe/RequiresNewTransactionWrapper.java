package org.camunda.bpm.extension.batch.example.failsafe;

import org.camunda.bpm.engine.delegate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Wraps either a {@link JavaDelegate}, a {@link ExecutionListener} or a {@link TaskListener}
 * and executes their code in a separate transaction. This is necessary to be able to catch Exceptions
 * from inside the wrapped code and handle them accordingly. Otherwise the transaction will be rolled
 * back although exceptions are caught explicitly.
 *
 * @see <a href="https://app.camunda.com/jira/browse/CAM-6872">CAM-6872 - Error handling of camunda service calls fails with engine-spring</a>
 */
@Component
public class RequiresNewTransactionWrapper {

  /**
   * Wrapper that just returns the original target without wrapping, used for Testing.
   */
  public final static RequiresNewTransactionWrapper NOOP = new RequiresNewTransactionWrapper(null) {
    @Override
    public <T> T requireNewTransaction(T target) {
      return target;
    }
  };

  private final Properties transactionAttributes;
  private final PlatformTransactionManager transactionManager;

  @Autowired
  public RequiresNewTransactionWrapper(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
    this.transactionAttributes = new Properties() {{
      put("*", "PROPAGATION_REQUIRES_NEW");
    }};
  }

  protected class Wrapper<W extends Wrapper<W, T>, T> {

    protected final T delegate;
    protected Consumer<Exception> errorHandler;

    protected Wrapper(T delegate) {
      this.delegate = requireNewTransaction(delegate);
    }

    public W ignoreException() {
      return onException(e -> {
      });
    }

    public W throwRuntimeException() {
      return onException(e -> {
        throw new RuntimeException(e);
      });
    }

    public W onException(Consumer<Exception> errorHandler) {
      this.errorHandler = errorHandler;
      return (W) this;
    }
  }

  public class JavaDelegateWrapper extends Wrapper<JavaDelegateWrapper, JavaDelegate> implements JavaDelegate {

    public JavaDelegateWrapper(JavaDelegate delegate) {
      super(delegate);
    }

    @Override
    public void execute(DelegateExecution execution) {
      Objects.requireNonNull(errorHandler, "must specify errorHandler (use .ignoreException if not needed)");
      try {
        delegate.execute(execution);
      } catch (Exception e) {
        errorHandler.accept(e);
      }
    }
  }

  public class ExecutionListenerWrapper extends Wrapper<ExecutionListenerWrapper, ExecutionListener> implements ExecutionListener {

    public ExecutionListenerWrapper(ExecutionListener delegate) {
      super(delegate);
    }

    @Override
    public void notify(DelegateExecution execution) {
      Objects.requireNonNull(errorHandler, "must specify errorHandler (use .ignoreException if not needed)");
      try {
        delegate.notify(execution);
      } catch (Exception e) {
        errorHandler.accept(e);
      }
    }
  }

  public class TaskListenerWrapper extends Wrapper<TaskListenerWrapper, TaskListener> implements TaskListener {

    public TaskListenerWrapper(TaskListener delegate) {
      super(delegate);
    }

    @Override
    public void notify(DelegateTask task) {
      Objects.requireNonNull(errorHandler, "must specify errorHandler (use .ignoreException if not needed)");
      try {
        delegate.notify(task);
      } catch (Exception e) {
        errorHandler.accept(e);
      }
    }
  }

  public JavaDelegateWrapper wrap(JavaDelegate delegate) {
    return new JavaDelegateWrapper(delegate);
  }

  public ExecutionListenerWrapper wrap(ExecutionListener delegate) {
    return new ExecutionListenerWrapper(delegate);
  }

  public TaskListenerWrapper wrap(TaskListener delegate) {
    return new TaskListenerWrapper(delegate);
  }

  public <T> T requireNewTransaction(T target) {
    final TransactionProxyFactoryBean proxy = new TransactionProxyFactoryBean();

    // Inject transaction manager here
    proxy.setTransactionManager(transactionManager);

    // Define wich object instance is to be proxied (your bean)
    proxy.setTarget(target);

    // Programmatically setup transaction attributes
    proxy.setTransactionAttributes(transactionAttributes);

    // Finish FactoryBean setup
    proxy.afterPropertiesSet();
    return (T) proxy.getObject();
  }

}


