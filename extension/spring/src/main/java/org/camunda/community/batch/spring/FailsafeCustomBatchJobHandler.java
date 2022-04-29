package org.camunda.community.batch.spring;

import java.io.Serializable;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.community.batch.CustomBatchJobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

/**
 * Jobhandler that encapsulates the call to {@link #executeFailsafe(Serializable, CommandContext)}
 * in a {@link TransactionProxyFactoryBean#getObject()} to enforce a new transaction.
 *
 * Use this if you need to ensure the batch job does not fail, even when a {@link RuntimeException}
 * occurs.
 *
 * By default, any excepion risen is ignored, this behavior can be overwritten via {@link #handleRuntimeException(RuntimeException)},
 * use this to apply a custom exception handling (logging, re-throw, ...).
 *
 * @param <T> type of the batch
 */
public abstract class FailsafeCustomBatchJobHandler<T extends Serializable> extends CustomBatchJobHandler<T> {

  private static Properties TRANSACTION_ATTRIBUTES = new Properties() {{
    put("*", "PROPAGATION_REQUIRES_NEW");
  }};

  private final PlatformTransactionManager transactionManager;

  @Autowired
  public FailsafeCustomBatchJobHandler(final PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  /**
   * Wraps the target (probably a BiConsumer(T, context)) with a proxy that enforces a new
   * spring transaction.
   *
   * Note: this might be better placed in the engine-spring directly, so it will probably be
   * replaced with some central implemenation.
   *
   * @param target the wrapped instance
   * @param <T> wrapped type
   * @return proxy
   */
  protected  <T> T requireNewTransaction(T target) {
    final TransactionProxyFactoryBean proxy = new TransactionProxyFactoryBean();

    // Inject transaction manager here
    proxy.setTransactionManager(transactionManager);

    // Define wich object instance is to be proxied (your bean)
    proxy.setTarget(target);

    // Programmatically setup transaction attributes
    proxy.setTransactionAttributes(TRANSACTION_ATTRIBUTES);

    proxy.setProxyTargetClass(true);

    // Finish FactoryBean setup
    proxy.afterPropertiesSet();
    return (T) proxy.getObject();
  }

  @Override
  public void execute(final List<T> data, final CommandContext commandContext) {
    data.forEach(s -> {
      final BiConsumer<T, CommandContext> executeFailsafe = this::executeFailsafe;

      try {
        requireNewTransaction( executeFailsafe).accept(s, commandContext);
      } catch (final RuntimeException e) {
        handleRuntimeException(e);
      }

    });
  }

  /**
   * Handles execution of a single element in the batch data list.
   *
   * @param data the current data element
   * @param commandContext the camunda {@link CommandContext}
   */
  public abstract void executeFailsafe(T data, CommandContext commandContext);

  /**
   * Overwrite this for custom exception handling. Default: ignore.
   *
   * @param runtimeException exception risen in {@link #executeFailsafe(Serializable, CommandContext)}.
   */
  public void handleRuntimeException(final RuntimeException runtimeException) {
    // default: ignore
  }
}
