package com.example.currencyexchanger.listeners;

import com.zaxxer.hikari.HikariDataSource;
import com.example.currencyexchanger.repositories.CurrencyRepository;
import com.example.currencyexchanger.repositories.ExchangeRatesRepository;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.Properties;

@WebListener
public class ContextListener implements ServletContextListener {

    public ContextListener() {

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        HikariDataSource hikariDataSource = makeHikariDataSource(context);

        CurrencyRepository currencyRepository = new CurrencyRepository(hikariDataSource);
        context.setAttribute("currencyRepository", currencyRepository);

        ExchangeRatesRepository exchangeRatesRepository = new ExchangeRatesRepository(hikariDataSource, context);
        context.setAttribute("exchangeRatesRepository", exchangeRatesRepository);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private HikariDataSource makeHikariDataSource(ServletContext context) {
        try {
            Properties properties = new Properties();
            HikariDataSource hikariDataSource = new HikariDataSource();

            properties.load(context.getResourceAsStream("WEB-INF/properties/db.properties"));

            hikariDataSource.setDriverClassName(properties.getProperty("db.driver.name"));
            hikariDataSource.setJdbcUrl(properties.getProperty("db.url"));
            hikariDataSource.setUsername(properties.getProperty("db.username"));
            hikariDataSource.setPassword(properties.getProperty("db.password"));

            return hikariDataSource;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}