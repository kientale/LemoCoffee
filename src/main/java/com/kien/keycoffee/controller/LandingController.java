package com.kien.keycoffee.controller;

import com.kien.keycoffee.dto.DrinkTableDTO;
import com.kien.keycoffee.service.DrinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LandingController {

    private static final int MENU_PAGE_SIZE = 9;

    private final DrinkService drinkService;

    @GetMapping({"/", "/landing"})
    public String showLanding() {
        return "pages/landing";
    }

    @GetMapping("/menu")
    public String showMenu(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        int pageNo = Math.max(1, page);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        Page<DrinkTableDTO> drinkPage = drinkService.getAvailableDrinks(
                pageNo,
                MENU_PAGE_SIZE,
                normalizedKeyword
        );

        int totalPages = Math.max(drinkPage.getTotalPages(), 1);
        if (pageNo > totalPages) {
            pageNo = totalPages;
            drinkPage = drinkService.getAvailableDrinks(pageNo, MENU_PAGE_SIZE, normalizedKeyword);
        }

        model.addAttribute("drinks", drinkPage.getContent());
        model.addAttribute("page", pageNo);
        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("prevPage", Math.max(1, pageNo - 1));
        model.addAttribute("nextPage", Math.min(totalPages, pageNo + 1));
        model.addAttribute("prevDisabled", pageNo <= 1);
        model.addAttribute("nextDisabled", pageNo >= totalPages);

        return "pages/landing-menu";
    }

    @GetMapping("/service")
    public String showService() {
        return "pages/landing-service";
    }

    @GetMapping("/space")
    public String showSpace() {
        return "pages/landing-space";
    }
}
