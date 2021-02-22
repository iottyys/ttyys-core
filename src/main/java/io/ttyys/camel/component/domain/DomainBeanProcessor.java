//package io.ttyys.camel.component.domain;
//
//import org.apache.camel.*;
//import org.apache.camel.component.bean.*;
//import org.apache.camel.support.service.ServiceSupport;
//
//import java.util.concurrent.CompletableFuture;
//
//public class DomainBeanProcessor extends BeanProcessor {
//    private final DelegateBeanProcessor delegate;
//
//    public DomainBeanProcessor(Object pojo, CamelContext camelContext) {
//        this(new ConstantBeanHolder(
//                pojo, camelContext, ParameterMappingStrategyHelper.createParameterMappingStrategy(camelContext),
//                camelContext.getComponent("domain", DomainComponent.class)));
//    }
//
//    public DomainBeanProcessor(Object pojo, BeanInfo beanInfo) {
//        super(pojo, beanInfo);
//        this.delegate = new DelegateBeanProcessor(pojo, beanInfo);
//    }
//
//    public DomainBeanProcessor(BeanHolder beanHolder) {
//        super(beanHolder);
//        this.delegate = new DelegateBeanProcessor(beanHolder);
//    }
//
//    @Override
//    public boolean process(Exchange exchange, AsyncCallback callback) {
//        return super.process(exchange, callback);
//    }
//
//    @Override
//    public void process(Exchange exchange) throws Exception {
//
//    }
//
//    @Override
//    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
//        return null;
//    }
//
//    private static final class DelegateBeanProcessor extends AbstractBeanProcessor {
//
//        public DelegateBeanProcessor(Object pojo, BeanInfo beanInfo) {
//            super(pojo, beanInfo);
//        }
//
//        public DelegateBeanProcessor(BeanHolder beanHolder) {
//            super(beanHolder);
//        }
//
//        @Override
//        protected Processor getProcessor() {
//            return super.getProcessor();
//        }
//
//        @Override
//        protected BeanHolder getBeanHolder() {
//            return super.getBeanHolder();
//        }
//    }
//}
