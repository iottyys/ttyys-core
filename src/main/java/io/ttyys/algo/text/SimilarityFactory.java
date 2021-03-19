//package io.ttyys.algo.text;
//
//import algo.text.Message;
//import algo.text.Similarity;
//import io.ttyys.algo.AlgorithmFactory;
//import org.apache.avro.AvroRemoteException;
//import org.apache.avro.ipc.Transceiver;
//import org.apache.avro.ipc.netty.NettyTransceiver;
//import org.apache.avro.ipc.specific.SpecificRequestor;
//
//import java.io.Closeable;
//import java.io.IOException;
//import java.net.InetSocketAddress;
//
//public class SimilarityFactory implements AlgorithmFactory, Similarity, Closeable {
//
//    private static final int PORT = 22222;
//
//    private Transceiver transceiver;
//    private Similarity proxy;
//
//    public static Message.Builder newMessageBuilder() {
//        return Message.newBuilder();
//    }
//
//    @Override
//    public void close() {
//        if (this.transceiver != null && this.transceiver.isConnected()) {
//            try {
//                this.transceiver.close();
//                this.transceiver = null;
//                this.proxy = null;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public CharSequence send(Message message) throws AvroRemoteException {
//        this.check();
//        return this.proxy.send(message);
//    }
//
//    @Override
//    public CharSequence similarity(CharSequence doc_file, CharSequence corpus_file, CharSequence stop_word_file, CharSequence user_dict) throws AvroRemoteException {
//        this.check();
//        return this.proxy.similarity(doc_file, corpus_file, stop_word_file, user_dict);
//    }
//
//    private void check() {
//        try {
//            if (this.transceiver == null || this.proxy == null) {
//                this.transceiver = new NettyTransceiver(new InetSocketAddress(PORT));
//                this.proxy = SpecificRequestor.getClient(Similarity.class, this.transceiver);
//            }
//        } catch (IOException e) {
//            throw new IllegalStateException("Invoke Failed. State of internal server unexpect. ", e);
//        }
//    }
//}
