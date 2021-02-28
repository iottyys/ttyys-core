package io.ttyys.core.support.springboot;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.ttyys.core.compiler.DynamicCompiler;
import io.ttyys.core.support.architecture.EnhanceMapper;
import lombok.SneakyThrows;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ClassPathEnhanceMapperScanner extends ClassPathBeanDefinitionScanner {

    public ClassPathEnhanceMapperScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @SneakyThrows(ClassNotFoundException.class)
    @Override
    protected Set<BeanDefinitionHolder> doScan( String... basePackages) {
        assert super.getRegistry() != null;
        this.addIncludeFilter(new AnnotationTypeFilter(EnhanceMapper.class));
        Set<BeanDefinitionHolder> holders = super.doScan(basePackages);
        Set<String> interfaces = new HashSet<>(holders.size());
        DynamicCompiler compiler = new DynamicCompiler(Thread.currentThread().getContextClassLoader());
        for (BeanDefinitionHolder holder : holders) {
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) holder.getBeanDefinition();
            GenericBeanDefinition genericBeanDefinition = (GenericBeanDefinition) holder.getBeanDefinition();
            genericBeanDefinition.resolveBeanClass(Thread.currentThread().getContextClassLoader());
            AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                    annotatedBeanDefinition.getMetadata().getAnnotationAttributes(EnhanceMapper.class.getName()));

            String packageName = genericBeanDefinition.getBeanClass().getPackage().getName();
            String poClassName = genericBeanDefinition.getBeanClass().getSimpleName();
            String className = poClassName + "Mapper";
            if (attributes != null && StringUtils.hasText(attributes.getString("value"))) {
                className = attributes.getString("value");
            }
            String classQualifierName = packageName + "." + className;
            String source = this.genericMapperSource(packageName, className, poClassName);

            compiler.addSource(classQualifierName, source);
            interfaces.add(classQualifierName);
        }
        compiler.build(true);
        this.registerMapper(super.getRegistry(), interfaces);
        return new HashSet<>(0);
    }

    private String genericMapperSource(String packageName, String mapperClassName, String poClassName) {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get("com.baomidou.mybatisplus.core.mapper", "BaseMapper"),
                ClassName.get(packageName, poClassName));
        TypeSpec mapper = TypeSpec
                .interfaceBuilder(mapperClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(typeName)
                .build();
        return JavaFile.builder(packageName, mapper).build().toString();
    }

    private void registerMapper(BeanDefinitionRegistry registry, Set<String> interfaces) {
        for (String interfaceName : interfaces) {
            GenericBeanDefinition interfaceDefinition = new GenericBeanDefinition();
            interfaceDefinition.setBeanClassName(interfaceName);
            String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(interfaceDefinition, registry);
            BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(interfaceDefinition, beanName);

            GenericBeanDefinition definition = (GenericBeanDefinition) definitionHolder.getBeanDefinition();
            definition.setBeanClassName(interfaceName);
            definition.getConstructorArgumentValues().addGenericArgumentValue(interfaceName);
            definition.setBeanClass(MapperFactoryBean.class);
            definition.getPropertyValues().add("addToConfig", true);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            definition.setLazyInit(false);
            definition.setPrimary(false);

            super.registerBeanDefinition(definitionHolder, registry);
        }
    }
}
