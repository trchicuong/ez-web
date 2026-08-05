package trchicuong.ezweb.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import trchicuong.ezweb.config.AppProperties;
import trchicuong.ezweb.models.User;
import trchicuong.ezweb.services.TopupService;
import trchicuong.ezweb.services.UserService;

@Controller
@RequestMapping("/topup")
@RequiredArgsConstructor
public class TopupController {

    private final TopupService topupService;
    private final UserService userService;
    private final AppProperties appProperties;

    @GetMapping
    public String topupPage(@AuthenticationPrincipal User principal,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        User user = userService.findById(principal.getId()).orElseThrow();
        String transferCode = appProperties.getTransferCode(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("appProperties", appProperties);
        model.addAttribute("transferCode", transferCode);
        model.addAttribute("topupLogs", topupService.findByUser(user.getId(), page));
        return "pages/topup";
    }
}
