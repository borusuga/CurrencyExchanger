package com.example.currencyexchanger.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.currencyexchanger.models.Currency;
import com.example.currencyexchanger.models.ExchangeRate;
import com.example.currencyexchanger.repositories.CurrencyRepository;
import com.example.currencyexchanger.repositories.ExchangeRatesRepository;
import com.example.currencyexchanger.utils.Utils;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@MultipartConfig
@WebServlet(name = "exchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private ExchangeRatesRepository exchangeRatesRepository;
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        exchangeRatesRepository = (ExchangeRatesRepository) config.getServletContext().getAttribute("exchangeRatesRepository");
        currencyRepository = (CurrencyRepository) config.getServletContext().getAttribute("currencyRepository");
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        new ObjectMapper().writeValue(resp.getWriter(), exchangeRatesRepository.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String baseCurrencyCode = Utils.getStringFromPartName(req, "baseCurrencyCode");
        String targetCurrencyCode = Utils.getStringFromPartName(req, "targetCurrencyCode");
        String rate = Utils.getStringFromPartName(req, "rate");

        if (Utils.isNotValidExchangeArgs(baseCurrencyCode, targetCurrencyCode, rate)
                || !Utils.isStringDouble(rate)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Не правильно введены данные. Пример: baseCurrency = 'USD', targetCurrency = 'EUR', rate = '1.05'");
            return;
        }

        Optional<Currency> baseCurrency = currencyRepository.findByName(baseCurrencyCode);
        Optional<Currency> targetCurrency = currencyRepository.findByName(targetCurrencyCode);

        if (baseCurrency.isEmpty() || targetCurrency.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Валюта не найдена");
            return;
        }

        if (exchangeRatesRepository.findByCodes(baseCurrencyCode, targetCurrencyCode).isPresent()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Валютная пара с таким кодом уже существует");
            return;
        }

        ExchangeRate exchangeRate = new ExchangeRate(baseCurrency.get(), targetCurrency.get(), BigDecimal.valueOf(Double.parseDouble(rate)));

        exchangeRatesRepository.save(exchangeRate);

        doGet(req, resp);
    }
}