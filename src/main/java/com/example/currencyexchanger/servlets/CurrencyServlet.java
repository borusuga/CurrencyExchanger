package com.example.currencyexchanger.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.currencyexchanger.models.Currency;
import com.example.currencyexchanger.repositories.CurrencyRepository;


import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "CurrencyServlet", value = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrencyRepository currencyRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        currencyRepository = (CurrencyRepository) config.getServletContext().getAttribute("currencyRepository");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Указана не корректная валюта. Пример: .../currency/USD");
            return;
        }

        String currencyCode = request.getPathInfo().replaceFirst("/", "").toUpperCase();

        Optional<Currency> currency = currencyRepository.findByName(currencyCode);

        if (currency.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Валюта не найдена. Пример: .../currency/USD");
            return;
        }

        response.getWriter().write(new ObjectMapper().writeValueAsString(currency.get()));
    }
}