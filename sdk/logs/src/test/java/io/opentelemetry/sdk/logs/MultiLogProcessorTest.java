/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiLogProcessorTest {

  @Mock private LogProcessor logProcessor1;
  @Mock private LogProcessor logProcessor2;
  @Mock private ReadWriteLogRecord logRecord;

  @BeforeEach
  void setup() {
    when(logProcessor1.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor2.forceFlush()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor1.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    when(logProcessor2.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void empty() {
    LogProcessor multiLogProcessor = LogProcessor.composite();
    assertThat(multiLogProcessor).isInstanceOf(NoopLogProcessor.class);
    multiLogProcessor.onEmit(logRecord);
    multiLogProcessor.shutdown();
  }

  @Test
  void oneLogProcessor() {
    LogProcessor multiLogProcessor = LogProcessor.composite(logProcessor1);
    assertThat(multiLogProcessor).isSameAs(logProcessor1);
  }

  @Test
  void twoLogProcessor() {
    LogProcessor multiLogProcessor = LogProcessor.composite(logProcessor1, logProcessor2);
    multiLogProcessor.onEmit(logRecord);
    verify(logProcessor1).onEmit(same(logRecord));
    verify(logProcessor2).onEmit(same(logRecord));

    multiLogProcessor.forceFlush();
    verify(logProcessor1).forceFlush();
    verify(logProcessor2).forceFlush();

    multiLogProcessor.shutdown();
    verify(logProcessor1).shutdown();
    verify(logProcessor2).shutdown();
  }
}