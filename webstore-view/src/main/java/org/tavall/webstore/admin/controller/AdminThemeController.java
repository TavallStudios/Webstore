package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.tavall.webstore.theme.service.ThemeAssetService;

@Controller
public class AdminThemeController {

    private final ThemeAssetService themeAssetService;

    public AdminThemeController(ThemeAssetService themeAssetService) {
        this.themeAssetService = themeAssetService;
    }

    @GetMapping("/admin/themes")
    public String themeStudio(@RequestParam(required = false) String file, Model model) {
        String selectedFile = themeAssetService.selectFile(file);
        model.addAttribute("themeFiles", themeAssetService.listFiles());
        model.addAttribute("selectedThemeFile", selectedFile);
        model.addAttribute("selectedThemeFileContent", themeAssetService.readFile(selectedFile));
        return "admin/theme-studio";
    }

    @PostMapping("/admin/themes/files")
    public String saveThemeFile(@RequestParam String fileName, @RequestParam String content) {
        themeAssetService.saveFile(fileName, content);
        return "redirect:/admin/themes?file=" + fileName + "&saved=1";
    }

    @PostMapping("/admin/themes/import")
    public String importThemeFiles(@RequestParam("files") MultipartFile[] files) {
        themeAssetService.importFiles(files);
        return "redirect:/admin/themes?imported=1";
    }
}
