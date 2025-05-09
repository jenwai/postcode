package com.assessment.postcode.config;

import com.assessment.postcode.entity.UkPostcode;
import com.assessment.postcode.model.PostcodeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class BatchConfiguration {

  @Bean
  @StepScope
  public FlatFileItemReader<PostcodeRecord> reader(
    @Value("#{jobParameters['csvPath']}") String csvPath) {

    var tokenizer = new DelimitedLineTokenizer(",");
    tokenizer.setNames("id", "postcode", "latitude", "longitude");

    FieldSetMapper<PostcodeRecord> fieldSetMapper = fieldSet -> PostcodeRecord.builder()
      .id(fieldSet.readLong("id"))
      .postcode(fieldSet.readString("postcode"))
      .latitude(fieldSet.readBigDecimal("latitude"))
      .longitude(fieldSet.readBigDecimal("longitude"))
      .build();

    LineMapper<PostcodeRecord> nonEmptyLineMapper = (line, lineNumber) -> {
      if (!StringUtils.hasText(line)) {
        return null;
      }
      FieldSet fieldSet = tokenizer.tokenize(line);
      return fieldSetMapper.mapFieldSet(fieldSet);
    };

    DefaultRecordSeparatorPolicy separatorPolicy = new DefaultRecordSeparatorPolicy() {
      @Override
      public boolean isEndOfRecord(String line) {
        return StringUtils.hasText(line);
      }

      @Override
      public String postProcess(String record) {
        return record.trim();
      }
    };

    return new FlatFileItemReaderBuilder<PostcodeRecord>()
      .name("itemReader")
      .resource(new ClassPathResource(csvPath))
      .linesToSkip(1)
      .recordSeparatorPolicy(separatorPolicy)
      .lineTokenizer(tokenizer)
      .lineMapper(nonEmptyLineMapper)
      .build();
  }

  @Bean
  public ItemProcessor<PostcodeRecord, UkPostcode> processor() {
    return r -> UkPostcode.builder()
      .id(r.id())
      .postcode(r.postcode())
      .latitude(r.latitude())
      .longitude(r.longitude())
      .build();
  }

  @Bean
  public JdbcBatchItemWriter<UkPostcode> postcodeWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<UkPostcode>()
      .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
      .sql(
        "INSERT INTO UK_POSTCODE (id, postcode, latitude, longitude) VALUES (:id, :postcode, :latitude, :longitude)")
      .dataSource(dataSource)
      .build();
  }

  @Bean
  public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager,
    FlatFileItemReader<PostcodeRecord> reader,
    ItemProcessor<PostcodeRecord, UkPostcode> processor,
    ItemWriter<UkPostcode> writer) {
    return new StepBuilder("step1", jobRepository)
      .<PostcodeRecord, UkPostcode>chunk(1000, transactionManager)
      .reader(reader)
      .processor(processor)
      .writer(writer)
      .build();
  }

  @Bean
  public Job importJob(JobRepository jobRepository, Step step1) {
    return new JobBuilder("importJob", jobRepository)
      .start(step1)
      .build();
  }

  @Bean
  public CommandLineRunner runJob(JobLauncher jobLauncher, Job importJob) {
    return args -> {
      log.info("Checking if importJob should run...");

      String csvPath = null;
      for (String arg : args) {
        if (arg.startsWith("--csvPath=")) {
          csvPath = arg.substring("--csvPath=".length());
        }
      }

      if (csvPath == null || csvPath.isBlank()) {
        log.info("csvPath is not provided. Skipping job execution.");
        return;
      }

      log.info("RUNNING importJob with csvPath={}", csvPath);

      var jobParametersBuilder = new JobParametersBuilder()
        .addLong("run.id", System.currentTimeMillis())
        .addString("csvPath", csvPath);

      jobLauncher.run(importJob, jobParametersBuilder.toJobParameters());
    };
  }
}
