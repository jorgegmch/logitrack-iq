package com.jorgegmch.logitrack.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext contexto;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        contexto = applicationContext;
    }

    public static <T> T obtenerBean(Class<T> clase) {
        return contexto.getBean(clase);
    }
}