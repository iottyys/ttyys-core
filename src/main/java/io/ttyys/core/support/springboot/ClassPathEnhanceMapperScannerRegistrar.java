package io.ttyys.core.support.springboot;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

public class ClassPathEnhanceMapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableEnhanceSupport.class.getName()));
        assert attributes != null;
        String[] servicePackages = attributes.getStringArray("mapperPackages");
        if (servicePackages.length == 0 && importingClassMetadata instanceof StandardAnnotationMetadata) {
            servicePackages = new String[] { ((StandardAnnotationMetadata) importingClassMetadata)
                    .getIntrospectedClass().getPackage().getName() };
        }
        if (servicePackages.length == 0) {
            return;
        }
        ClassPathEnhanceMapperScanner scanner = new ClassPathEnhanceMapperScanner(registry);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.doScan(servicePackages);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
