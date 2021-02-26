package io.ttyys.core.support.springboot;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import io.ttyys.core.compiler.StringSourceDynamicCompiler;
import io.ttyys.core.support.architecture.EnhanceMapper;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

public class ClassPathEnhanceMapperScanner extends ClassPathBeanDefinitionScanner {
    public ClassPathEnhanceMapperScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        this.addIncludeFilter(new AnnotationTypeFilter(EnhanceMapper.class));
        Set<BeanDefinitionHolder> holders = super.doScan(basePackages);
        for (BeanDefinitionHolder holder : holders) {
            try {
                InputStream is = new FileInputStream("/Volumes/works/work/workspace/products/micrc/ttyys-core/src/main/java/io/ttyys/core/support/architecture/UserMapper.test");
                String str = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);
                System.out.println(str);
                DiagnosticCollector<JavaFileObject> compileCollector = new DiagnosticCollector<>();
                byte[] classBytes = StringSourceDynamicCompiler.compile(str, compileCollector);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("test");
        }
        return null;
    }
}
