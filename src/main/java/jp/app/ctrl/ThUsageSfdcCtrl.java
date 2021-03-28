package jp.app.ctrl;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ThUsageSfdcCtrl {
    @RequestMapping(value = { "/usage-sfdc.html" })
    public String oiyokanUnittest(Model model) throws IOException {

        return "usage-sfdc";
    }
}
