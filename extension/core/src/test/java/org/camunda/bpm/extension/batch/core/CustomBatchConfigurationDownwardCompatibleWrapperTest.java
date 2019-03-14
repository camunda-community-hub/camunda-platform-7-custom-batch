package org.camunda.bpm.extension.batch.core;

import org.apache.commons.lang3.SerializationUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CustomBatchConfigurationDownwardCompatibleWrapperTest extends AbstractSetupWithEngineConfiguration {

  private CustomBatchConfigurationHelper configurationHelper;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    final CustomBatchConfigurationJsonHelper<Serializable> jsonHelper = CustomBatchConfigurationJsonHelper.of(CustomBatchConfigurationJsonConverter.of());
    configurationHelper = CustomBatchConfigurationDownwardCompatibleWrapper.of(jsonHelper);
  }

  @Test
  public void read_method_downwards_compatibility() {
    final ArrayList<Integer> testData = Lists.newArrayList(1, 2, 3, 4, 5, 6);
    final byte[] bytes = SerializationUtils.serialize(testData);

    final CustomBatchConfiguration<Integer> batchConfiguration = configurationHelper.readConfiguration(bytes);

    assertThat(batchConfiguration.getData()).containsExactlyElementsOf(testData);
  }

  @Test
  public void read_method_downwards_compatibility_with_objects() {
    final ArrayList<TestConfigObject> testData = Lists.newArrayList(new TestConfigObject("abc"), new TestConfigObject("def"), new TestConfigObject("ghi"));
    final byte[] bytes = SerializationUtils.serialize(testData);

    final CustomBatchConfiguration<TestConfigObject> batchConfiguration = configurationHelper.readConfiguration(bytes);

    assertThat(batchConfiguration.getData()).containsExactlyElementsOf(testData);
  }

  static class TestConfigObject implements Serializable {

    private static final long serialVersionUID = -6177454776892282471L;
    public String text;
    public Integer something = 1;

    public TestConfigObject(final String text) {
      this.text = text;
    }

    @Override
    public boolean equals(final Object obj) {
      return obj instanceof TestConfigObject && Objects.equals(((TestConfigObject) obj).text, this.text);
    }
  }

}
