package io.ttyys.algo;

import io.ttyys.avro.annotation.Protocol;
import io.ttyys.avro.annotation.Protocols;

@Protocols(@Protocol(value = "avro/similarity.avpr", factory = "SIMILARITY"))
public interface AlgorithmFactory {
}
