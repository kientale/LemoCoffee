package com.kien.lemocoffee.controller;

import com.kien.lemocoffee.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticController {

    private static final int TOP_LIMIT = 10;
    private static final String CONTENT = "pages/statistic";
    private static final String LAYOUT = "layouts/admin-layout";

    private final StatisticService statisticService;

    @GetMapping
    public String showStatistics(Model model) {
        model.addAttribute("activePage", "statistics");
        model.addAttribute("content", CONTENT);

        model.addAttribute("revenueToday", statisticService.getRevenueToday());
        model.addAttribute("revenueThisMonth", statisticService.getRevenueThisMonth());
        model.addAttribute("revenueThisYear", statisticService.getRevenueThisYear());
        model.addAttribute("topCustomers", statisticService.getTopCustomers(TOP_LIMIT));
        model.addAttribute("topDrinks", statisticService.getTopDrinks(TOP_LIMIT));

        return LAYOUT;
    }
}
