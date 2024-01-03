package com.example.currencyexchanger.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.currencyexchanger.dto.Exchange;
import com.example.currencyexchanger.models.Currency;
import com.example.currencyexchanger.models.ExchangeRate;
import com.example.currencyexchanger.repositories.CurrencyRepository;
import com.example.currencyexchanger.repositories.ExchangeRatesRepository;
import com.example.currencyexchanger.utils.Utils;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet(name = "ExchangeServlet", value = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private CurrencyRepository currencyRepository;
    private ExchangeRatesRepository exchangeRatesRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        currencyRepository = (CurrencyRepository) config.getServletContext().getAttribute("currencyRepository");
        exchangeRatesRepository = (ExchangeRatesRepository) config.getServletContext().getAttribute("exchangeRatesRepository");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amount = request.getParameter("amount");

        if (Utils.isNotValidExchangeArgs(from, to, amount) || !Utils.isStringDouble(amount)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Неправильно введен запрос. Пример: /exchange?from=USD&to=RUB&amount=10");
            return;
        }

        if (!isCurrenciesValid(from, to)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Указана не существующая валюта. Пример: /exchange?from=USD&to=RUB&amount=10");
            return;
        }

        BigDecimal rate = getRate(from, to);

        if (rate == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Не существует курс обмена");
            return;
        }

        new ObjectMapper().writeValue(response.getWriter(), new Exchange(
                currencyRepository.findByName(from).get(),
                currencyRepository.findByName(to).get(),
                rate,
                new BigDecimal(amount),
                rate.multiply(new BigDecimal(amount))
        ));
    }

    private boolean isCurrenciesValid(String from, String to) {
        Optional<Currency> fromCurrency = currencyRepository.findByName(from);
        Optional<Currency> toCurrency = currencyRepository.findByName(to);

        return (fromCurrency.isPresent() && toCurrency.isPresent());
    }

    private BigDecimal getRate(String from, String to) {
        Optional<ExchangeRate> exchangeRate = exchangeRatesRepository.findByCodes(from, to);

        if (exchangeRate.isPresent())
            return exchangeRate.get().getRate();

        Optional<ExchangeRate> reverseExchangeRate = exchangeRatesRepository.findByCodes(to, from);

        if (reverseExchangeRate.isPresent())
            return reverseExchangeRate.get().getRate();

        Optional<ExchangeRate> exchangeRateUSD_A = exchangeRatesRepository.findByCodes("USD", from);
        Optional<ExchangeRate> exchangeRateUSD_B = exchangeRatesRepository.findByCodes("USD", to);

        if (exchangeRateUSD_A.isPresent() && exchangeRateUSD_B.isPresent()) {
            return exchangeRateUSD_A.get().getRate().divide(exchangeRateUSD_B.get().getRate());
        }

        return null;
    }
}