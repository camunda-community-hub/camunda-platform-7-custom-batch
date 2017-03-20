package org.camunda.bpm.extension.batch.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CustomBatchConfigurationHelperTest {

    @Test
    public void convert_to_bytearray_and_back_for_string_config() throws Exception {
        List<String> data = Arrays.asList("Test", "Test2");
        CustomBatchConfiguration configuration = new CustomBatchConfiguration<>(data);

        byte[] bytes = CustomBatchConfigurationHelper.writeConfiguration(configuration);
        assertThat(bytes).isNotEmpty();

        CustomBatchConfiguration<String> readConfiguration = CustomBatchConfigurationHelper.<String>of().readConfiguration(bytes);
        assertThat(readConfiguration).isNotNull();
        assertThat(readConfiguration.getData()).isNotEmpty();
        assertThat(readConfiguration.getData()).hasSize(2);
        assertThat(readConfiguration.getData().get(1)).isEqualTo("Test2");
    }

    @Test
    public void convert_to_bytearray_and_back_for_Object_config() throws Exception {
        List<TestConfigObject> data = Arrays.asList(new TestConfigObject("test"),
            new TestConfigObject("test2"), new TestConfigObject("test3") );
        CustomBatchConfiguration<TestConfigObject> configuration = new CustomBatchConfiguration<>(data);

        byte[] bytes = CustomBatchConfigurationHelper.writeConfiguration(configuration);
        assertThat(bytes).isNotEmpty();

        CustomBatchConfiguration<TestConfigObject> readConfiguration = CustomBatchConfigurationHelper.<TestConfigObject>of().readConfiguration(bytes);
        assertThat(readConfiguration).isNotNull();
        assertThat(readConfiguration.getData()).isNotEmpty();
        assertThat(readConfiguration.getData()).hasSize(3);
        assertThat(readConfiguration.getData().get(2).text).isEqualTo("test3");
    }
}
