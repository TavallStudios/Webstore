package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.tavall.webstore.media.service.MediaAssetService;

@Controller
public class AdminMediaController {

    private final MediaAssetService mediaAssetService;

    public AdminMediaController(MediaAssetService mediaAssetService) {
        this.mediaAssetService = mediaAssetService;
    }

    @GetMapping("/admin/media")
    public String mediaLibrary(Model model) {
        model.addAttribute("assets", mediaAssetService.listAssets());
        return "admin/media";
    }

    @PostMapping("/admin/media")
    public String uploadMedia(@RequestParam("file") MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            mediaAssetService.store(file);
        }
        return "redirect:/admin/media";
    }
}
