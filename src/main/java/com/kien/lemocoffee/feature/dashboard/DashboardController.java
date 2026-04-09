package com.kien.lemocoffee.feature.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("content", "pages/dashboard/dashboard");
        return "layouts/admin-layout";
    }
}