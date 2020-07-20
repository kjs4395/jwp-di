package core.di;

import core.annotation.ComponentScan;
import core.di.factory.BeanFactory;
import core.di.factory.scanner.ClasspathBeanScanner;
import core.di.factory.scanner.ConfigurationBeanScanner;
import core.mvc.tobe.HandlerExecution;
import core.mvc.tobe.HandlerKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created By kjs4395 on 7/20/20
 */
public class ApplicationContext {
    private final Set<Class<?>> configs = new HashSet<>();
    private final BeanFactory beanFactory;
    private final ConfigurationBeanScanner configurationScanner;
    private ClasspathBeanScanner classpathBeanScanner;

    public ApplicationContext(Class<?> config) {
        configs.add(config);
        this.beanFactory = new BeanFactory();
        this.configurationScanner = new ConfigurationBeanScanner(beanFactory);
        this.initialize();
    }

    public ApplicationContext(Set<Class<?>> configs) {
        this.configs.addAll(configs);
        this.beanFactory = new BeanFactory();
        this.configurationScanner = new ConfigurationBeanScanner(beanFactory);
        this.initialize();
    }

    private void initialize() {
        initializeConfigBean();
        this.classpathBeanScanner = new ClasspathBeanScanner(this.beanFactory);

        classpathBeanScanner.doScan(readBasePackage());
    }

    private void initializeConfigBean() {
        this.configs.forEach(this.configurationScanner::register);
    }

    private Object[] readBasePackage() {
        Object[] basePackage = this.configs.stream()
                .filter(clazz -> clazz.isAnnotationPresent(ComponentScan.class))
                .map(clazz->clazz.getAnnotation(ComponentScan.class))
                .map(ComponentScan::value)
                .flatMap(Arrays::stream)
                .toArray();

        if(basePackage.length == 0) {
            return new Object[]{""};
        }
        return basePackage;
    }

    public Map<HandlerKey, HandlerExecution> getHandler() {
        return this.classpathBeanScanner.scan();
    }
}
