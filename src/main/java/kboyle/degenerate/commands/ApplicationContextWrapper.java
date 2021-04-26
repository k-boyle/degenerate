package kboyle.degenerate.commands;

import kboyle.oktane.core.BeanProvider;
import org.springframework.context.ApplicationContext;

public class ApplicationContextWrapper implements BeanProvider {
    private final ApplicationContext applicationContext;

    public ApplicationContextWrapper(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
