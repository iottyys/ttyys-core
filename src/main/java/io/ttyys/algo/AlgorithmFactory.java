package io.ttyys.algo;

import io.ttyys.avro.annotation.Protocol;
import io.ttyys.avro.annotation.Protocols;

@Protocols(@Protocol(value = "avro/corpus.avpr", factory = "ALGORITHM"))
public interface AlgorithmFactory<T> {
    T invoker();
}
