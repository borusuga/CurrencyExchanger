package com.example.currencyexchanger.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.currencyexchanger.models.Currency;
import com.example.currencyexchanger.repositories.CurrencyRepository;
import com.example.currencyexchanger.utils.Utils;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

@MultipartConfig
@WebServlet(name = "indexCurrenciesServlet", value = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyRepository currencyRepository;
    @Override
    public void init(ServletConfig config) throws ServletException {
        currencyRepository = (CurrencyRepository) config.getServletContext().getAttribute("currencyRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        new ObjectMapper().writeValue(resp.getWriter(), currencyRepository.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = Utils.getStringFromPartName(req, "code");
        String name = Utils.getStringFromPartName(req, "name");
        String sign = Utils.getStringFromPartName(req, "sign");

        if (Utils.isNotValidCurrenciesArgs(code, name, sign)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Не правильно введены данные. Пример: code = 'USD', name = 'US Dollar', sign = '$'");
            return;
        }

        if (currencyRepository.findByName(code).isPresent()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Валюта с таким кодом уже существует");
            return;
        }

        currencyRepository.save(new Currency(code, name, sign));

        doGet(req, resp);
    }
}