package jp.app.ctrl;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.oiyokan.OiyokanConstants;

@Controller
public class ThIndexCtrl {
    @RequestMapping(value = { "/", "/index.html" })
    public String oiyokanUnittest(Model model) throws IOException {
        model.addAttribute("odataRootpath", OiyokanConstants.ODATA_ROOTPATH);

        IndexBean index = new IndexBean();
        model.addAttribute("indexBean", index);

        return "index";
    }

    public static class IndexBean {
        public String getVersion() {
            return OiyokanConstants.VERSION;
        }
    }
}
